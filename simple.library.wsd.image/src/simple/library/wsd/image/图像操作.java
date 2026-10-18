package simple.library.wsd.image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.media.MediaScannerConnection;

import simple.runtime.android.MainActivity;
import simple.runtime.android.MainApplication;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.collections.集合;
import simple.runtime.variants.ArrayVariant;

/**
 * 以图片文件的字节集执行图像变换，像素尺寸不随设备密度缩放。
 *
 * <p>字节集必须是 PNG、JPEG 等可解码的图片文件内容，不是原始像素数组。
 * 除文件压缩外，处理结果统一为 PNG。大图处理会占用较多内存，建议在后台线程调用；
 * 本类只回收自身创建的位图，不改写传入的字节数组。</p>
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 图像操作 {

	private 图像操作() {
	}

	/* 几何变换 */

	/**
	 * 旋转图片，并自动扩展输出画布容纳旋转后的内容。
	 *
	 * @param data 原始图片文件的字节集。
	 * @param degrees 旋转角度，单位为度；正数在图片坐标中顺时针旋转。
	 * @return 旋转后的 PNG 字节集。
	 * @throws IllegalArgumentException 图片无法解码或角度不是有限数值。
	 */
	@SimpleFunction
	public static byte[] 旋转图片(byte[] data, float degrees) {
		requireFinite(degrees);
		Matrix matrix = new Matrix();
		matrix.postRotate(degrees);

		return transform(data, matrix);
	}

	/**
	 * 按水平和垂直错切系数倾斜图片。
	 *
	 * @param data 原始图片文件的字节集。
	 * @param horizontal 水平错切系数，不是角度；例如 0.25。
	 * @param vertical 垂直错切系数，不是角度。
	 * @return 自动扩展画布后的 PNG 字节集。
	 * @throws IllegalArgumentException 图片无法解码或系数不是有限数值。
	 */
	@SimpleFunction
	public static byte[] 倾斜图片(byte[] data, float horizontal, float vertical) {
		requireFinite(horizontal);
		requireFinite(vertical);
		Matrix matrix = new Matrix();
		matrix.postSkew(horizontal, vertical);

		return transform(data, matrix);
	}

	/**
	 * 将图片缩放到指定像素宽高，不保持原始宽高比例。
	 *
	 * @param data 原始图片文件的字节集。
	 * @param width 目标宽度，单位为像素，必须大于零。
	 * @param height 目标高度，单位为像素，必须大于零。
	 * @return 缩放后的 PNG 字节集。
	 * @throws IllegalArgumentException 图片无法解码或目标尺寸无效。
	 */
	@SimpleFunction
	public static byte[] 缩放图片(byte[] data, int width, int height) {
		requireSize(width, height);
		Bitmap source = decode(data);
		Bitmap result = null;
		try {
			Matrix matrix = new Matrix();
			matrix.postScale((float) width / source.getWidth(), (float) height / source.getHeight());
			result = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

			return encode(result, Bitmap.CompressFormat.PNG, 100);
		} finally {
			recycle(result, source);
		}
	}

	/**
	 * 对图片执行水平或垂直镜像。
	 *
	 * @param data 原始图片文件的字节集。
	 * @param direction 0 表示左右反转，1 表示上下反转；其他值不处理。
	 * @return PNG 字节集；方向不是 0 或 1 时直接返回原字节数组。
	 * @throws IllegalArgumentException 需要反转的图片无法解码。
	 */
	@SimpleFunction
	public static byte[] 反转图片(byte[] data, int direction) {
		if (direction != 0 && direction != 1) {
			return data;
		}

		Matrix matrix = new Matrix();
		matrix.setScale(direction == 0 ? -1 : 1, direction == 1 ? -1 : 1);
		return transform(data, matrix);
	}

	/* 尺寸读取：仅解析图片头，不创建完整像素位图。 */

	/**
	 * 读取资产文件或绝对文件路径对应图片的像素宽度。
	 *
	 * @param path 资产文件名，或以 / 开头的绝对文件路径；不支持网址或 content://。
	 * @return 像素宽度；文件打开失败返回 0，内容无法识别为图片返回 -1。
	 */
	@SimpleFunction
	public static int 取图片宽度(String path) {
		return bounds(path).outWidth;
	}

	/**
	 * 读取资产文件或绝对文件路径对应图片的像素高度。
	 *
	 * @param path 资产文件名，或以 / 开头的绝对文件路径；不支持网址或 content://。
	 * @return 像素高度；文件打开失败返回 0，内容无法识别为图片返回 -1。
	 */
	@SimpleFunction
	public static int 取图片高度(String path) {
		return bounds(path).outHeight;
	}

	/**
	 * 读取图片字节集的像素宽度。
	 *
	 * @param data 图片文件字节集，不是原始像素数组。
	 * @return 像素宽度；无法识别图片时返回 -1。
	 * @throws IllegalArgumentException 字节集为空。
	 */
	@SimpleFunction
	public static int 取图片宽度2(byte[] data) {
		return bounds(data).outWidth;
	}

	/**
	 * 读取图片字节集的像素高度。
	 *
	 * @param data 图片文件字节集，不是原始像素数组。
	 * @return 像素高度；无法识别图片时返回 -1。
	 * @throws IllegalArgumentException 字节集为空。
	 */
	@SimpleFunction
	public static int 取图片高度2(byte[] data) {
		return bounds(data).outHeight;
	}

	/* 压缩与分割 */

	/**
	 * 尝试降低 JPEG 画质，再按既有输出约定重新编码为 PNG。
	 *
	 * <p>中间 JPEG 超过约 100 KiB 时按 10 递减质量，最低为 0。
	 * 不缩放尺寸，透明度会在 JPEG 阶段丢失；最终 PNG 不保证比原图小，也不保证低于 100 KiB。</p>
	 *
	 * @param data 原始图片文件的字节集。
	 * @return 经 JPEG 画质处理后重新编码的 PNG 字节集。
	 * @throws IllegalArgumentException 图片无法解码。
	 */
	@SimpleFunction
	public static byte[] 压缩图片(byte[] data) {
		Bitmap source = decode(data);
		Bitmap compressed = null;
		try {
			// 仅降低中间 JPEG 的质量，质量降到零后停止，避免无终止重试。
			byte[] jpeg = encode(source, Bitmap.CompressFormat.JPEG, 100);
			for (int quality = 90; jpeg.length / 1024 > 100 && quality >= 0; quality -= 10) {
				jpeg = encode(source, Bitmap.CompressFormat.JPEG, quality);
			}

			compressed = decode(jpeg);
			return encode(compressed, Bitmap.CompressFormat.PNG, 100);
		} finally {
			recycle(compressed, source);
		}
	}

	/**
	 * 按目标高度估算解码采样率，并将图片写为质量 100 的 JPEG 文件。
	 *
	 * <p>采样率为原高除以目标高度后向下取整，最低为 1；实际尺寸由 Android 解码器决定，
	 * 不保证等于目标高度。输出不保留透明度；覆盖目标文件，不自动创建父目录。</p>
	 *
	 * @param sourcePath 原始图片的绝对文件路径。
	 * @param targetPath 目标 JPEG 的绝对文件路径，建议使用 .jpg 后缀。
	 * @param targetHeight 用于估算采样率的目标高度，单位为像素，必须大于零。
	 * @throws IllegalArgumentException 路径、图片或目标高度无效。
	 * @throws IllegalStateException 无法写入目标文件。
	 */
	@SimpleFunction
	public static void 压缩图片2(String sourcePath, String targetPath, int targetHeight) {
		File sourceFile = requireFile(sourcePath);
		File targetFile = absoluteFile(targetPath);
		if (targetHeight <= 0) {
			throw new IllegalArgumentException("目标高度必须大于零。");
		}

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(sourceFile.getAbsolutePath(), options);
		requireSize(options.outWidth, options.outHeight);

		// 先完整解码需要的像素，再打开输出文件，同一路径也不会在解码前被截断。
		options.inJustDecodeBounds = false;
		options.inSampleSize = Math.max(1, options.outHeight / targetHeight);
		Bitmap bitmap = BitmapFactory.decodeFile(sourceFile.getAbsolutePath(), options);
		if (bitmap == null) {
			throw new IllegalArgumentException("无法解码图片文件。");
		}

		try (FileOutputStream output = new FileOutputStream(targetFile)) {
			if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)) {
				throw new IllegalStateException("无法编码 JPEG 图片。");
			}
			output.flush();
		} catch (IOException exception) {
			throw new IllegalStateException("无法写入图片文件。", exception);
		} finally {
			recycle(bitmap);
		}
	}

	/**
	 * 将图片按列数和行数均分，按从左到右、从上到下的顺序返回分块。
	 *
	 * <p>分块宽高使用整数除法，最右侧、最底部不能整除的剩余像素不进入分块。</p>
	 *
	 * @param data 原始图片文件的字节集。
	 * @param columns 列数，必须大于零且不超过图片宽度。
	 * @param rows 行数，必须大于零且不超过图片高度。
	 * @return 集合，每个项目都是一块 PNG 文件的字节集。
	 * @throws IllegalArgumentException 图片无法解码或分割数量无效。
	 */
	@SimpleFunction
	public static 集合 切割图片(byte[] data, int columns, int rows) {
		Bitmap source = decode(data);
		try {
			requireSize(columns, rows);
			int width = source.getWidth() / columns;
			int height = source.getHeight() / rows;
			requireSize(width, height);

			集合 result = new 集合();
			for (int row = 0; row < rows; row++) {
				for (int column = 0; column < columns; column++) {
					Bitmap tile = Bitmap.createBitmap(source, column * width, row * height, width, height);
					try {
						result.加入(ArrayVariant.getArrayVariant(encode(tile, Bitmap.CompressFormat.PNG, 100)));
					} finally {
						// 单块覆盖整图时 Android 可能返回原位图，留到外层统一回收。
						if (tile != source) {
							recycle(tile);
						}
					}
				}
			}

			return result;
		} finally {
			recycle(source);
		}
	}

	/* 遮罩、倒影与裁剪 */

	/**
	 * 为图片添加透明圆角，或按既有阈值规则生成圆形图片。
	 *
	 * @param data 原始图片文件的字节集。
	 * @param radius 圆角半径，单位为像素；0–99 保持原尺寸，达到 100 时居中截取圆形。
	 * @return PNG 字节集；圆形直径为原图较短边的一半向下取整后再乘以 2。
	 * @throws IllegalArgumentException 图片无法解码、半径为负或圆形尺寸不足。
	 */
	@SimpleFunction
	public static byte[] 设置图片圆角(byte[] data, int radius) {
		if (radius < 0) {
			throw new IllegalArgumentException("圆角半径不能为负数。");
		}

		Bitmap source = decode(data);
		Bitmap rounded = null;
		Bitmap circle = null;
		try {
			rounded = roundedBitmap(source, radius);
			if (radius < 100) {
				return encode(rounded, Bitmap.CompressFormat.PNG, 100);
			}

			circle = circularBitmap(rounded, Math.min(source.getWidth(), source.getHeight()) / 2);
			return encode(circle, Bitmap.CompressFormat.PNG, 100);
		} finally {
			recycle(circle, rounded, source);
		}
	}

	/**
	 * 在原图下方添加下半部分的垂直镜像及透明渐变。
	 *
	 * <p>保持原输出高度为原高加原高的一半；原图与倒影间隔 4 像素，
	 * 因而倒影底部会被画布截去 4 像素。</p>
	 *
	 * @param data 原始图片文件的字节集，高度至少为 2 像素。
	 * @return 带原图及倒影的 PNG 字节集。
	 * @throws IllegalArgumentException 图片无法解码或高度不足。
	 */
	@SimpleFunction
	public static byte[] 设置图片倒影(byte[] data) {
		Bitmap source = decode(data);
		Bitmap reflection = null;
		Bitmap result = null;
		try {
			int width = source.getWidth();
			int height = source.getHeight();
			requireSize(width, height / 2);

			Matrix matrix = new Matrix();
			matrix.preScale(1, -1);
			reflection = Bitmap.createBitmap(source, 0, height / 2, width, height / 2, matrix, false);
			result = Bitmap.createBitmap(width, height + height / 2, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(result);
			canvas.drawBitmap(source, 0, 0, null);

			// 保留四像素间隔及原渐变范围，不改变既有倒影布局。
			Paint gap = new Paint();
			canvas.drawRect(0, height, width, height + 4, gap);
			canvas.drawBitmap(reflection, 0, height + 4, null);

			Paint fade = new Paint();
			fade.setShader(new LinearGradient(0, height, 0, result.getHeight() + 4,
				0x70FFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
			fade.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
			canvas.drawRect(0, height, width, result.getHeight() + 4, fade);

			return encode(result, Bitmap.CompressFormat.PNG, 100);
		} finally {
			recycle(result, reflection, source);
		}
	}

	/**
	 * 按图片像素坐标截取矩形区域。
	 *
	 * @param data 原始图片文件的字节集。
	 * @param x 左上角横坐标，从 0 开始。
	 * @param y 左上角纵坐标，从 0 开始。
	 * @param width 截取宽度，必须大于零。
	 * @param height 截取高度，必须大于零。
	 * @return 截取区域的 PNG 字节集。
	 * @throws IllegalArgumentException 图片无法解码或截取区域越界。
	 */
	@SimpleFunction
	public static byte[] 取图片部分(byte[] data, int x, int y, int width, int height) {
		requireSize(width, height);
		Bitmap source = decode(data);
		Bitmap result = null;
		try {
			if (x < 0 || y < 0 || x > source.getWidth() - width || y > source.getHeight() - height) {
				throw new IllegalArgumentException("截取区域超出图片范围。");
			}

			result = Bitmap.createBitmap(source, x, y, width, height);
			return encode(result, Bitmap.CompressFormat.PNG, 100);
		} finally {
			recycle(result, source);
		}
	}

	/**
	 * 请求系统媒体扫描器重新扫描已经存在的图片文件。
	 *
	 * <p>这是异步扫描请求，不移动或发布文件，也不保证私有目录图片出现在相册。
	 * 目标文件必须处于应用可访问且系统允许扫描的位置。</p>
	 *
	 * @param path 已存在图片的绝对文件路径，不是 content:// URI。
	 * @throws IllegalArgumentException 路径不是可访问的文件。
	 */
	@SimpleFunction
	public static void 更新系统相册(String path) {
		File file = requireFile(path);
		MediaScannerConnection.scanFile(context(), new String[] {file.getAbsolutePath()}, null, null);
	}

	/* 内部图片编解码与资源管理 */

	/**
	 * 解码自身负责回收的位图，避免让无效图片延迟为后续空指针错误。
	 *
	 * @param data 编码后的图片文件字节集。
	 * @return 新解码的位图。
	 */
	private static Bitmap decode(byte[] data) {
		requireData(data);
		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		if (bitmap == null) {
			throw new IllegalArgumentException("无法解码图片字节集。");
		}

		return bitmap;
	}

	/**
	 * 编码位图，不转移位图所有权。
	 *
	 * @param bitmap 待编码位图。
	 * @param format 图片文件格式。
	 * @param quality 压缩质量。
	 * @return 编码后的图片文件字节集。
	 */
	private static byte[] encode(Bitmap bitmap, Bitmap.CompressFormat format, int quality) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		if (!bitmap.compress(format, quality, output)) {
			throw new IllegalStateException("图片编码失败。");
		}

		return output.toByteArray();
	}

	/**
	 * 统一执行几何变换和 PNG 编码，并处理恒等变换返回原位图的情况。
	 *
	 * @param data 原始图片文件字节集。
	 * @param matrix 几何变换矩阵。
	 * @return 变换后的 PNG 字节集。
	 */
	private static byte[] transform(byte[] data, Matrix matrix) {
		Bitmap source = decode(data);
		Bitmap result = null;
		try {
			result = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
			return encode(result, Bitmap.CompressFormat.PNG, 100);
		} finally {
			recycle(result, source);
		}
	}

	/**
	 * 解析字节集的尺寸，避免为读取宽高分配完整像素缓冲。
	 *
	 * @param data 图片文件字节集。
	 * @return 解码器读取的图片头信息。
	 */
	private static BitmapFactory.Options bounds(byte[] data) {
		requireData(data);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, options);
		return options;
	}

	/**
	 * 打开文件或资产读取尺寸，所有分支均关闭输入流。
	 *
	 * @param path 资产名或绝对文件路径。
	 * @return 图片头信息；打开失败时宽高为 0。
	 */
	private static BitmapFactory.Options bounds(String path) {
		if (path == null || path.length() == 0) {
			throw new IllegalArgumentException("图片路径不能为空。");
		}

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		try (InputStream input = path.startsWith("/") ? new FileInputStream(path) : context().getAssets().open(path)) {
			BitmapFactory.decodeStream(input, null, options);
		} catch (IOException exception) {
			options.outWidth = 0;
			options.outHeight = 0;
		}

		return options;
	}

	/**
	 * 绘制圆角遮罩，保持源图尺寸。
	 *
	 * @param source 原始位图，不回收。
	 * @param radius 圆角半径。
	 * @return 圆角位图。
	 */
	private static Bitmap roundedBitmap(Bitmap source, int radius) {
		Bitmap result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(0xFF424242);
		Rect rect = new Rect(0, 0, source.getWidth(), source.getHeight());
		canvas.drawRoundRect(new RectF(rect), radius, radius, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(source, rect, rect, paint);
		return result;
	}

	/**
	 * 居中截取正方形后缩放至圆形直径，保留偶数直径约定。
	 *
	 * @param source 原始位图，不回收。
	 * @param radius 圆形半径，必须大于零。
	 * @return 圆形位图。
	 */
	private static Bitmap circularBitmap(Bitmap source, int radius) {
		requireSize(radius, radius);
		int side = Math.min(source.getWidth(), source.getHeight());
		Bitmap square = Bitmap.createBitmap(source, (source.getWidth() - side) / 2,
			(source.getHeight() - side) / 2, side, side);
		Bitmap scaled = null;
		try {
			int diameter = radius * 2;
			scaled = Bitmap.createScaledBitmap(square, diameter, diameter, true);
			Bitmap result = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(result);
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
			canvas.drawCircle(radius, radius, radius, paint);
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
			canvas.drawBitmap(scaled, 0, 0, paint);
			return result;
		} finally {
			if (scaled != source) {
				recycle(scaled);
			}
			if (square != source) {
				recycle(square);
			}
		}
	}

	/**
	 * 回收本次调用拥有的位图，允许同一个对象在参数中重复出现。
	 *
	 * @param bitmaps 已不再使用的位图。
	 */
	private static void recycle(Bitmap... bitmaps) {
		for (Bitmap bitmap : bitmaps) {
			if (bitmap != null && !bitmap.isRecycled()) {
				bitmap.recycle();
			}
		}
	}

	/**
	 * 检查图片字节集不为空。
	 *
	 * @param data 图片文件字节集。
	 */
	private static void requireData(byte[] data) {
		if (data == null || data.length == 0) {
			throw new IllegalArgumentException("图片字节集不能为空。");
		}
	}

	/**
	 * 检查像素尺寸有效。
	 *
	 * @param width 宽度。
	 * @param height 高度。
	 */
	private static void requireSize(int width, int height) {
		if (width <= 0 || height <= 0) {
			throw new IllegalArgumentException("图片尺寸或分割数量必须大于零。");
		}
	}

	/**
	 * 拒绝无穷和非数值的几何变换参数。
	 *
	 * @param value 几何变换参数。
	 */
	private static void requireFinite(float value) {
		if (Float.isNaN(value) || Float.isInfinite(value)) {
			throw new IllegalArgumentException("几何变换参数必须是有限数值。");
		}
	}

	/**
	 * 检查文件路径为绝对路径。
	 *
	 * @param path 文件路径。
	 * @return 文件对象。
	 */
	private static File absoluteFile(String path) {
		if (path == null || !new File(path).isAbsolute()) {
			throw new IllegalArgumentException("请使用绝对文件路径。");
		}

		return new File(path);
	}

	/**
	 * 检查原始文件存在且不是目录。
	 *
	 * @param path 文件路径。
	 * @return 已存在文件对象。
	 */
	private static File requireFile(String path) {
		File file = absoluteFile(path);
		if (!file.isFile()) {
			throw new IllegalArgumentException("未找到可访问的图片文件。");
		}

		return file;
	}

	/**
	 * 获取应用上下文，兼容应用尚未提供全局 Application 上下文的情况。
	 *
	 * @return 可用的应用上下文。
	 */
	private static Context context() {
		Context context = MainApplication.getContext();
		if (context == null) {
			context = MainActivity.getContext();
		}
		if (context == null) {
			throw new IllegalStateException("应用上下文尚未初始化。");
		}

		return context.getApplicationContext();
	}
}
