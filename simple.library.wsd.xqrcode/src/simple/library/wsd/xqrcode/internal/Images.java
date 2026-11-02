package simple.library.wsd.xqrcode.internal;

import android.content.Context;
import android.graphics.*;
import android.media.ExifInterface;
import android.net.Uri;
import java.io.*;
import java.util.*;
import simple.library.wsd.xqrcode.*;
import simple.library.wsd.xqrcode.internal.zxing.*;
import simple.library.wsd.xqrcode.internal.zxing.common.HybridBinarizer;
import simple.library.wsd.xqrcode.vendor.util.QRCodeProduceUtils;

/**
 * 有界图像读取、二维码生成和无界面解码。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class Images {
	private static final int MAX_BYTES = 32 * 1024 * 1024;

	/**
	 * 阻止创建无状态工具实例。
	 */
	private Images() {
	}

	/**
	 * 读取资产、绝对文件及内容 URI，限制压缩数据体积。
	 *
	 * @param context 应用上下文。
	 * @param path 资产名、asset://、file://、绝对路径或 content://。
	 * @return 输入图像字节。
	 * @throws IOException 输入不可读或超过限制。
	 */
	public static byte[] read(Context context, String path) throws IOException {
		if (path == null || path.isEmpty()) {
			throw new IOException("图片路径为空");
		}
		InputStream stream;
		if (path.startsWith("content://")) {
			stream = context.getContentResolver().openInputStream(Uri.parse(path));
		} else if (path.startsWith("/") || path.startsWith("file://")) {
			stream = new FileInputStream(path.startsWith("file://") ? Uri.parse(path).getPath() : path);
		} else {
			stream = context.getAssets().open(path.startsWith("asset://") ? path.substring(8) : path);
		}
		if (stream == null) {
			throw new IOException("无法打开图片");
		}
		try (InputStream input = stream; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[8192];
			int length;
			while ((length = input.read(buffer)) != -1) {
				if (output.size() + length > MAX_BYTES) {
					throw new IOException("图片压缩数据不能超过 32 MiB");
				}
				output.write(buffer, 0, length);
			}
			return output.toByteArray();
		}
	}

	/**
	 * 采样读取位图并应用 EXIF 方向，避免原图直接占满内存。
	 *
	 * @param bytes 压缩图像数据。
	 * @return 最长边不超过 2048 的位图，由调用方释放。
	 * @throws IOException 无效图像。
	 */
	public static Bitmap bitmap(byte[] bytes) throws IOException {
		if (bytes == null || bytes.length == 0 || bytes.length > MAX_BYTES) {
			throw new IOException("图片数据为空或超过 32 MiB");
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
		if (options.outWidth <= 0 || options.outHeight <= 0) {
			throw new IOException("无法读取图片尺寸");
		}
		options.inSampleSize = 1;
		while ((long) options.outWidth * options.outHeight / options.inSampleSize / options.inSampleSize > 4194304L
				|| (Math.max(options.outWidth, options.outHeight) + options.inSampleSize - 1L) / options.inSampleSize > 2048) {
			options.inSampleSize *= 2;
		}
		options.inJustDecodeBounds = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
		if (image == null) {
			throw new IOException("图片解码失败");
		}
		int orientation = ExifInterface.ORIENTATION_NORMAL;
		try {
			orientation = new ExifInterface(new ByteArrayInputStream(bytes)).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
		} catch (IOException ignored) {
			// 不携带 EXIF 的格式按原方向处理。
		}
		Matrix matrix = new Matrix();
		switch (orientation) {
			case 2:
				matrix.setScale(-1, 1);
				break;
			case 3:
				matrix.setRotate(180);
				break;
			case 4:
				matrix.setScale(1, -1);
				break;
			case 5:
				matrix.setRotate(90);
				matrix.postScale(-1, 1);
				break;
			case 6:
				matrix.setRotate(90);
				break;
			case 7:
				matrix.setRotate(270);
				matrix.postScale(-1, 1);
				break;
			case 8:
				matrix.setRotate(270);
				break;
			default:
				break;
		}
		if (!matrix.isIdentity()) {
			Bitmap rotated = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, false);
			if (rotated != image) {
				image.recycle();
				image = rotated;
			}
		}
		return image;
	}

	/**
	 * 将位图编码成 PNG 数据。
	 *
	 * @param image 输入位图。
	 * @return 完整 PNG 数据。
	 */
	public static byte[] png(Bitmap image) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		if (!image.compress(Bitmap.CompressFormat.PNG, 100, output)) {
			throw new IllegalStateException("PNG 编码失败");
		}
		return output.toByteArray();
	}

	/**
	 * 生成艺术二维码并按配置叠加图标和矩形画布。
	 *
	 * @param context 应用上下文。
	 * @param text 二维码内容。
	 * @param options 已复制的配置。
	 * @return PNG 图像。
	 * @throws IOException 附加图片读取失败。
	 */
	public static byte[] generate(Context context, String text, 二维码配置 options) throws IOException {
		if (text == null || text.isEmpty() || text.length() > 16000) {
			throw new IllegalArgumentException("二维码内容为空或过长");
		}
		Bitmap background = null;
		Bitmap logo = null;
		Bitmap code = null;
		Bitmap output = null;
		try {
			byte[] data = options.取背景字节();
			if (data.length != 0 || !options.背景图片().isEmpty()) {
				background = bitmap(data.length == 0 ? read(context, options.背景图片()) : data);
			}
			data = options.取图标字节();
			if (data.length != 0 || !options.图标图片().isEmpty()) {
				logo = bitmap(data.length == 0 ? read(context, options.图标图片()) : data);
			}
			int width = options.图像宽度() == 0 ? options.尺寸() : options.图像宽度();
			int height = options.图像高度() == 0 ? options.尺寸() : options.图像高度();
			int side = Math.min(width, height);
			code = QRCodeProduceUtils.newBuilder(text).setSize(side).setMargin(options.边距())
				.setDataDotScale(options.数据点比例()).setColorDark(options.深色()).setColorLight(options.浅色())
				.setBackgroundImage(background).setWhiteMargin(options.白色边缘()).setAutoColor(options.自动取色())
				.setBinarize(options.二值化()).setBinarizeThreshold(options.二值化阈值()).build();
			if (code == null) {
				throw new IllegalArgumentException("内容无法编码为二维码");
			}
			output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(output);
			canvas.drawColor(options.浅色());
			canvas.drawBitmap(code, (width - side) / 2f, (height - side) / 2f, null);
			if (logo != null) {
				float maxSide = side * options.图标比例();
				float scale = maxSide / Math.max(logo.getWidth(), logo.getHeight());
				float left = (width - logo.getWidth() * scale) / 2;
				float top = (height - logo.getHeight() * scale) / 2;
				canvas.drawBitmap(logo, null, new RectF(left, top, width - left, height - top), new Paint(Paint.FILTER_BITMAP_FLAG));
			}
			return png(output);
		} finally {
			for (Bitmap image : new Bitmap[] {output, code, logo, background}) {
				if (image != null && !image.isRecycled()) {
					image.recycle();
				}
			}
		}
	}

	/**
	 * 将格式与字符集配置转换为本次解码提示。
	 *
	 * @param options 独立扫描配置。
	 * @return 本次使用的提示表。
	 */
	public static Map<DecodeHintType, Object> hints(扫码配置 options) {
		Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
		String[] names = options.取识别格式();
		if (names.length == 0) {
			names = new String[] {"UPC_A", "UPC_E", "EAN_13", "EAN_8", "CODE_39", "CODE_93", "CODE_128", "ITF", "QR_CODE", "DATA_MATRIX"};
		}
		List<BarcodeFormat> formats = new ArrayList<>();
		for (String name : names) {
			formats.add(BarcodeFormat.valueOf(name));
		}
		hints.put(DecodeHintType.POSSIBLE_FORMATS, formats);
		hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
		if (!options.字符集().isEmpty()) {
			hints.put(DecodeHintType.CHARACTER_SET, options.字符集());
		}
		return hints;
	}

	/**
	 * 对亮度图像执行正色及反色识别。
	 *
	 * @param source 亮度数据。
	 * @param options 本次格式配置。
	 * @return ZXing 识别结果。
	 * @throws NotFoundException 图像中没有可识别的码。
	 */
	public static Result decode(LuminanceSource source, 扫码配置 options) throws NotFoundException {
		return decode(source, options, null);
	}

	/**
	 * 解码时按需采集候选点，候选点不能代替成功结果。
	 *
	 * @param source 亮度数据。
	 * @param options 识别配置。
	 * @param callback 候选点监听，空时不采集。
	 * @return 成功的解码结果。
	 * @throws NotFoundException 没有识别到码。
	 */
	public static Result decode(LuminanceSource source, 扫码配置 options, ResultPointCallback callback)
			throws NotFoundException {
		Map<DecodeHintType, Object> decodeHints = hints(options);
		if (callback != null) {
			decodeHints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, callback);
		}
		MultiFormatReader reader = new MultiFormatReader();
		try {
			return reader.decode(new BinaryBitmap(new HybridBinarizer(source)), decodeHints);
		} catch (NotFoundException error) {
			reader.reset();
			return reader.decode(
					new BinaryBitmap(new HybridBinarizer(source.invert())), decodeHints);
		} finally {
			reader.reset();
		}
	}

	/**
	 * 识别一张静态图片，尝试四个旋转方向并返回相应方向的图像及坐标。
	 *
	 * @param bytes 图片数据。
	 * @param options 图片识别配置，只使用格式、字符集及返回图像设置。
	 * @return 成功识别结果。
	 * @throws Exception 图片无效或未找到码。
	 */
	public static 识别结果 recognize(byte[] bytes, 扫码配置 options) throws Exception {
		Bitmap image = bitmap(bytes);
		try {
			for (int rotation = 0; rotation < 4; rotation++) {
				int width = image.getWidth();
				int height = image.getHeight();
				int[] pixels = new int[width * height];
				image.getPixels(pixels, 0, width, 0, 0, width, height);
				try {
					Result result = decode(new RGBLuminanceSource(width, height, pixels), options);
					return 识别结果.from(result, options.返回识别图像() ? png(image) : new byte[0]);
				} catch (NotFoundException error) {
					if (rotation == 3) {
						throw new IOException("没有识别到所选格式的条码或二维码", error);
					}
				}
				Matrix matrix = new Matrix();
				matrix.postRotate(90);
				Bitmap rotated = Bitmap.createBitmap(image, 0, 0, width, height, matrix, false);
				image.recycle();
				image = rotated;
			}
			throw new IOException("没有识别到二维码");
		} finally {
			image.recycle();
		}
	}
}
