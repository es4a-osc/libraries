package simple.library.wsd.xui;

import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.view.ContextThemeWrapper;

import com.xuexiang.xui.R;
import com.xuexiang.xui.widget.imageview.crop.CropImageView;

/**
 * 在 Android 进程中验证图片转换、裁剪坐标及 PNG 输出。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class BitmapConversionProbe {

	/**
	 * 禁止创建无状态验证入口的实例。
	 */
	private BitmapConversionProbe() {
	}

	/**
	 * 执行多密度位图及非位图资源的转换检查。
	 *
	 * @param args 本验证不接收参数。
	 * @throws Exception 验证环境、资源加载或图片输出失败时抛出。
	 */
	public static void main(String[] args) throws Exception {
		for (int density : new int[] { 160, 320, 480, 640 }) {
			verifyBitmap(density);
		}
		verifyDrawable();
		verifyCrop();
		System.out.println("BITMAP_CONVERSION_PASS");
	}

	/**
	 * 使用低目标密度的可绘制对象复现固有尺寸缩小，检查转换不损失像素。
	 *
	 * @param density 原位图密度。
	 */
	private static void verifyBitmap(int density) {
		Bitmap source = Bitmap.createBitmap(960, 540, Bitmap.Config.ARGB_8888);
		source.setDensity(density);
		source.eraseColor(Color.CYAN);
		BitmapDrawable drawable = new BitmapDrawable(Resources.getSystem(), source);
		drawable.setTargetDensity(160);
		Bitmap converted = ComponentSupport.toBitmap(drawable);
		try {
			check(converted != source, "转换结果必须独立拥有像素");
			check(converted.getWidth() == 960 && converted.getHeight() == 540, "转换损失原始尺寸");
			check(converted.getDensity() == density, "转换改变图片密度");
			check(converted.getPixel(959, 539) == Color.CYAN, "转换损失边缘像素");
			converted.setPixel(0, 0, Color.MAGENTA);
			check(source.getPixel(0, 0) == Color.CYAN, "编辑转换结果修改了原图");
			System.out.println("density=" + density + " intrinsic=" + drawable.getIntrinsicWidth()
				+ " converted=" + converted.getWidth() + "x" + converted.getHeight());
		} finally {
			converted.recycle();
			source.recycle();
		}
	}

	/**
	 * 验证非位图资源继续按固有尺寸栅格化，保留颜色内容。
	 */
	private static void verifyDrawable() {
		GradientDrawable drawable = new GradientDrawable();
		drawable.setSize(64, 32);
		drawable.setColor(Color.YELLOW);
		Bitmap converted = ComponentSupport.toBitmap(drawable);
		try {
			check(converted.getWidth() == 64 && converted.getHeight() == 32, "非位图资源尺寸错误");
			check(converted.getPixel(32, 16) == Color.YELLOW, "非位图资源颜色错误");
		} finally {
			converted.recycle();
		}
	}


	/**
	 * 在独立 Android 验证进程中加载正式应用资源，检查裁剪边界与导出内容。
	 *
	 * @throws Exception 验证进程上下文、资产或 PNG 输出不可用时抛出。
	 */
	private static void verifyCrop() throws Exception {
		Looper.prepare();
		// app_process 不附带应用上下文；反射仅用于此设备验证入口，不进入产品代码。
		Class<?> activityThread = Class.forName("android.app.ActivityThread");
		Object thread = activityThread.getMethod("systemMain").invoke(null);
		Context system = (Context) activityThread.getMethod("getSystemContext").invoke(thread);
		Context application = system.createPackageContext("simple.library.wsd.xui.example", Context.CONTEXT_IGNORE_SECURITY);
		Context context = new ContextThemeWrapper(application, R.style.XUITheme_Phone) {
			@Override
			public ClassLoader getClassLoader() {
				return BitmapConversionProbe.class.getClassLoader();
			}
		};
		Bitmap source;
		try (InputStream stream = application.getAssets().open("coast.png")) {
			source = BitmapFactory.decodeStream(stream);
		}
		check(source != null, "无法解码正式示例资产");
		BitmapDrawable drawable = new BitmapDrawable(context.getResources(), source);
		drawable.setTargetDensity(160);
		Bitmap image = ComponentSupport.toBitmap(drawable);
		CropImageView view = new CropImageView(context);
		view.setLayoutParams(new FrameLayout.LayoutParams(836, 380));
		view.setImageBitmap(image);
		view.setFixedAspectRatio(true);
		view.setAspectRatio(4, 3);
		view.measure(View.MeasureSpec.makeMeasureSpec(836, View.MeasureSpec.EXACTLY),
			View.MeasureSpec.makeMeasureSpec(380, View.MeasureSpec.EXACTLY));
		view.layout(0, 0, 836, 380);
		RectF rectangle = view.getActualCropRect();
		Bitmap cropped = view.getCroppedImage();
		try {
			check(cropped != null, "未生成裁剪结果");
			check(Math.abs(cropped.getWidth() * 3 - cropped.getHeight() * 4) <= 4, "裁剪比例不正确");
			check(Math.abs(cropped.getWidth() - rectangle.width()) <= 1, "裁剪宽度与原图坐标不一致");
			check(Math.abs(cropped.getHeight() - rectangle.height()) <= 1, "裁剪高度与原图坐标不一致");
			check(cropped.getPixel(10, 10) == image.getPixel((int) rectangle.left + 10, (int) rectangle.top + 10), "导出像素与选择区域不一致");
			String output = "/data/local/tmp/es4a-density-crop-20261028.png";
			try (FileOutputStream stream = new FileOutputStream(output)) {
				check(cropped.compress(Bitmap.CompressFormat.PNG, 100, stream), "PNG 编码失败");
			}
			Bitmap saved = BitmapFactory.decodeFile(output);
			check(saved != null && saved.getWidth() == cropped.getWidth() && saved.getHeight() == cropped.getHeight(), "PNG 重读尺寸不一致");
			saved.recycle();
			System.out.println("CROP_PNG_PASS size=" + cropped.getWidth() + "x" + cropped.getHeight());
		} finally {
			if (cropped != null) {
				cropped.recycle();
			}
			image.recycle();
			source.recycle();
		}
	}

	/**
	 * 不满足验证条件时终止进程，避免把失败报告为通过。
	 *
	 * @param condition 实际条件。
	 * @param message 失败说明。
	 */
	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
