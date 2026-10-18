package simple.library.wsd.barcode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

import simple.runtime.android.MainActivity;
import simple.runtime.components.组件容器;
import simple.runtime.components.impl.组件Impl;
import simple.runtime.events.EventDispatcher;
import simple.library.wsd.barcode.internal.zbar.scan.ScanCaptureAct;
import simple.library.wsd.barcode.internal.zxing.CaptureActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Hashtable;

import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

/**
 * 实现二维码和条形码生成、图片识别及相机扫描。
 * 编码算法及两种扫描界面沿用原实现，Android 资源由应用包按名称解析。
 */
public final class 条码Impl extends 组件Impl implements 条码 {

	// 原组件的扫描设置由应用内各实例共享。
	private static final int QRCODE_RESULT = 444;
	private static int style = 1;
	private static boolean beep;
	private static boolean continuous;
	private static boolean flashlight;
	private static boolean nativeLoaded;

	private final MainActivity activity;
	private final MainActivity.OnDestroyListener destroyListener;
	private BroadcastReceiver receiver;
	private boolean receiverRegistered;
	private ImageScanner imageScanner;

	/**
	 * 创建条码组件并登记所属 Activity 的清理回调。
	 *
	 * @param container 所属组件容器
	 */
	public 条码Impl(组件容器 container) {
		super(container);
		activity = MainActivity.getContext();
		destroyListener = new MainActivity.OnDestroyListener() {
			@Override
			public void onDestroy() {
				releaseResources();
			}
		};
		activity.addOnDestroyListener(destroyListener);
	}

	/* 事件 */

	@Override
	public void 扫描完毕(String result) {
		// 广播接收器由主线程调用，保持 Simple 事件在该线程分派。
		EventDispatcher.dispatchEvent(this, "扫描完毕", result);
		if (!continuous) {
			unregisterResultReceiver();
		}
	}

	/* 方法 */

	@Override
	public byte[] 生成条形码(String content, int requestedWidth, int requestedHeight) {
		if (content == null || "".equals(content) || content.length() < 1) {
			return null;
		}

		MultiFormatWriter writer = new MultiFormatWriter();
		BitMatrix result = null;

		try {
			result = writer.encode(content, BarcodeFormat.CODE_128, requestedWidth, requestedHeight, null);
		} catch (WriterException e) {
			e.printStackTrace();
		}

		int width = result.getWidth();
		int height = result.getHeight();

		int[] pixels = new int[width * height];

		for (int y = 0; y < height; ++y) {
			int offset = y * width;

			for (int x = 0; x < width; ++x) {
				pixels[offset + x] = result.get(x, y) ? -16777216 : -1;
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	/**
	 * 生成指定正方形尺寸的二维码图片，沿用完整方法的格式和失败约定。
	 *
	 * @param content 要编码的文本。
	 * @param size 正方形图片的边长，单位像素。
	 * @return 二维码图片的字节集。
	 */
	@Override
	public byte[] 生成二维码(String content, int size) {
		return 生成二维码(content, size, size);
	}

	@Override
	public byte[] 生成二维码(String content, int width, int height) {
		try {
			if (content == null || "".equals(content) || content.length() < 1) {
				return new byte[0];
			}

			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

			int[] pixels = new int[width * height];

			for (int y = 0; y < height; ++y) {
				for (int x = 0; x < width; ++x) {
					pixels[y * width + x] = bitMatrix.get(x, y) ? -16777216 : -1;
				}
			}

			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
			return baos.toByteArray();
		} catch (WriterException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	/**
	 * 生成带中心图标的正方形二维码图片，沿用完整方法的格式和失败约定。
	 *
	 * @param content 要编码的文本。
	 * @param logoPath 中心图标路径，与完整方法一致。
	 * @param size 正方形图片的边长，单位像素。
	 * @return 二维码图片的字节集。
	 */
	@Override
	public byte[] 生成二维码2(String content, String logoPath, int size) {
		return 生成二维码2(content, logoPath, size, size);
	}

	@Override
	public byte[] 生成二维码2(String content, String logoPath, int width, int height) {
		try {
			if (content == null || "".equals(content) || content.length() < 1) {
				return new byte[0];
			}

			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

			int[] pixels = new int[width * height];

			for (int y = 0; y < height; ++y) {
				for (int x = 0; x < width; ++x) {
					pixels[y * width + x] = bitMatrix.get(x, y) ? -16777216 : -1;
				}
			}

			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
			Bitmap bitmap2 = 条码Impl.createBitmap(bitmap, 条码Impl.zoomBitmap(条码Impl.convertToBitmap(logoPath, 60, 60), 100, 100));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap2.compress(Bitmap.CompressFormat.PNG, 100, baos);
			return baos.toByteArray();
		} catch (WriterException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	@Override
	public String 识别二维码(String imagePath) {
		loadNativeLibraries();
		Bitmap bmp = 条码Impl.getCompressedBitmap(imagePath);
		int w = bmp.getWidth();
		int h = bmp.getHeight();

		int[] pixels = new int[w * h];
		bmp.getPixels(pixels, 0, w, 0, 0, w, h);
		String resultStr = null;
		if (this.imageScanner == null) {
			this.imageScanner = new ImageScanner();
			this.imageScanner.setConfig(0, 256, 3);
			this.imageScanner.setConfig(0, 257, 3);
		}

		// 由 ARGB 像素转换为 ZBar 使用的灰度数据。
		Image barcode = new Image(w, h, "RGB4");
		barcode.setData(pixels);
		int result = this.imageScanner.scanImage(barcode.convert("Y800"));
		if (result != 0) {
			SymbolSet syms = this.imageScanner.getResults();

			for (Symbol sym : syms) {
				resultStr = sym.getData();
			}
		}

		if (resultStr == null) {
			return "";
		}

		return resultStr;
	}

	@Override
	public void 置扫描框样式(int value) {
		style = value;
	}

	@Override
	public void 置连续扫描(boolean enabled) {
		continuous = enabled;
	}

	@Override
	public void 置闪光灯开启(boolean enabled) {
		flashlight = enabled;
	}

	@Override
	public void 置播放音效(boolean enabled) {
		beep = enabled;
	}

	@Override
	public void 开始扫描() {
		unregisterResultReceiver();
		registerResultReceiver();

		Intent intent = new Intent(activity, style == 2 ? CaptureActivity.class : ScanCaptureAct.class);
		intent.putExtra("style", style == 2 ? 2 : 1);
		intent.putExtra("beep", beep);
		intent.putExtra("continuous", continuous);
		intent.putExtra("flashlight", flashlight);
		activity.startActivityForResult(intent, QRCODE_RESULT);
	}

	@Override
	public void 停止扫描() {
		activity.sendBroadcast(new Intent(activity.getPackageName() + ".barcode.STOP")
			.setPackage(activity.getPackageName()));
		unregisterResultReceiver();
	}

	@Override
	public void 销毁() {
		releaseResources();
		activity.removeOnDestroyListener(destroyListener);
		super.销毁();
	}

	/* 内部辅助 */

	/**
	 * 只在实际调用 ZBar 图片识别时加载原生库，避免干扰 Java 类型分析与纯编码。
	 */
	private static synchronized void loadNativeLibraries() {
		if (!nativeLoaded) {
			System.loadLibrary("iconv");
			System.loadLibrary("ZBarDecoder");
			nativeLoaded = true;
		}
	}

	/**
	 * 扫描结果广播只在当前应用内接收。
	 */
	private void registerResultReceiver() {
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (receiverRegistered && (activity.getPackageName() + ".barcode.RESULT").equals(intent.getAction())) {
					扫描完毕(intent.getStringExtra("code"));
				}
			}
		};
		activity.registerReceiver(receiver, new IntentFilter(activity.getPackageName() + ".barcode.RESULT"));
		receiverRegistered = true;
	}

	/**
	 * 广播注册状态独立记录，停止、返回结果与销毁均可安全清理。
	 */
	private void unregisterResultReceiver() {
		if (receiverRegistered) {
			activity.unregisterReceiver(receiver);
			receiverRegistered = false;
			receiver = null;
		}
	}

	private void releaseResources() {
		if (receiverRegistered) {
			停止扫描();
		}

		if (imageScanner != null) {
			imageScanner.destroy();
			imageScanner = null;
		}
	}

	/**
	 * 读取图片；绝对路径沿用原 API，相对路径先查应用私有文件，再查资产。
	 *
	 * @param path 图片标识
	 * @param options 解码参数
	 * @return 解码后的位图，无法读取时为空引用
	 */
	private static Bitmap decodeBitmap(String path, BitmapFactory.Options options) {
		File file = new File(path);
		if (!file.isAbsolute()) {
			file = new File(MainActivity.getContext().getFilesDir(), path);
		}

		if (file.isFile()) {
			return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
		}

		try (InputStream input = MainActivity.getContext().getAssets().open(path)) {
			return BitmapFactory.decodeStream(input, null, options);
		} catch (IOException exception) {
			return null;
		}
	}

	/**
	 * 先按目标尺寸解码，再沿用原组件的固定图标缩放。
	 */
	private static Bitmap convertToBitmap(String path, int w, int h) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
		decodeBitmap(path, opts);
		int width = opts.outWidth;
		int height = opts.outHeight;
		float scaleWidth = 0.0f;
		float scaleHeight = 0.0f;

		if (width > w || height > h) {
			scaleWidth = (float)width / (float)w;
			scaleHeight = (float)height / (float)h;
		}

		opts.inJustDecodeBounds = false;
		float scale = Math.max(scaleWidth, scaleHeight);
		opts.inSampleSize = (int)scale;
		WeakReference<Bitmap> weak = new WeakReference<Bitmap>(decodeBitmap(path, opts));
		return Bitmap.createScaledBitmap((Bitmap)weak.get(), w, h, true);
	}

	private static Bitmap zoomBitmap(Bitmap src, int destWidth, int destHeight) {
		if (src == null) {
			return null;
		}

		int w = src.getWidth();
		int h = src.getHeight();
		float scaleWidth = (float)destWidth / (float)w;
		float scaleHeight = (float)destHeight / (float)h;
		Matrix m = new Matrix();

		m.postScale(scaleWidth, scaleHeight);
		Bitmap resizedBitmap = Bitmap.createBitmap(src, 0, 0, w, h, m, true);
		return resizedBitmap;
	}

	/**
	 * 把图标叠加到二维码中心，不改变原编码的容错级别。
	 */
	private static Bitmap createBitmap(Bitmap src, Bitmap watermark) {
		if (src == null) {
			return null;
		}

		int w = src.getWidth();
		int h = src.getHeight();
		int ww = watermark.getWidth();
		int wh = watermark.getHeight();
		Bitmap newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas cv = new Canvas(newb);

		cv.drawBitmap(src, 0.0f, 0.0f, null);
		cv.drawBitmap(watermark, w / 2 - ww / 2, h / 2 - wh / 2, null);
		cv.save();
		cv.restore();
		return newb;
	}

	/**
	 * 按原算法缩小待识别图片，避免把完整大图交给 ZBar。
	 */
	private static Bitmap getCompressedBitmap(String path) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		decodeBitmap(path, options);
		options.inSampleSize = 条码Impl.calculateInSampleSize(options, 480, 800);
		options.inJustDecodeBounds = false;
		return decodeBitmap(path, options);
	}

	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		int height = options.outHeight;
		int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			int widthRatio;
			int heightRatio = Math.round((float)height / (float)reqHeight);

			inSampleSize = heightRatio < (widthRatio = Math.round((float)width / (float)reqWidth)) ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}
}
