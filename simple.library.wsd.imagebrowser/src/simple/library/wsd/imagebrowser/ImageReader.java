package simple.library.wsd.imagebrowser;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

/**
 * 在工作线程读取有界图片数据，并按预览尺寸采样解码。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class ImageReader {

	private static final int MAX_BYTES = 20 * 1024 * 1024;
	private static final int MAX_SIDE = 2048;

	private ImageReader() {
	}

	/**
	 * 读取一张静态图片，调用方负责在取消后丢弃返回的位图。
	 *
	 * @param context 应用资源和已授权 URI 的访问上下文。
	 * @param source 图片来源。
	 * @return 采样后的位图。
	 * @throws IOException 读取、大小检查或解码失败。
	 */
	static Bitmap read(Context context, String source) throws IOException {
		HttpURLConnection connection = null;
		InputStream input = null;
		try {
			if (source.startsWith("https://") || source.startsWith("http://")) {
				connection = (HttpURLConnection) new URL(source).openConnection();
				connection.setConnectTimeout(10000);
				connection.setReadTimeout(10000);
				connection.setUseCaches(false);
				int code = connection.getResponseCode();
				if (code < 200 || code >= 300) {
					throw new IOException("服务器返回 HTTP " + code + "。");
				}
				if (connection.getContentLength() > MAX_BYTES) {
					throw new IOException("图片文件超过 20 MiB。");
				}
				input = connection.getInputStream();
			} else if (source.startsWith("content://") || source.startsWith("file://")) {
				input = context.getContentResolver().openInputStream(Uri.parse(source));
			} else if (source.startsWith("/")) {
				input = new FileInputStream(source);
			} else if (source.startsWith("@")) {
				int id = context.getResources().getIdentifier(source.substring(1), null, context.getPackageName());
				if (id == 0) {
					throw new IOException("图片资源不存在。");
				}
				input = context.getResources().openRawResource(id);
			} else {
				input = context.getAssets().open(source);
			}

			if (input == null) {
				throw new IOException("无法打开图片。");
			}
			byte[] data = readBytes(input);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(data, 0, data.length, options);
			if (options.outWidth <= 0 || options.outHeight <= 0) {
				throw new IOException("不支持的图片格式或图片已经损坏。");
			}

			// 先探测尺寸再分配像素内存，超大图片按 2 的幂采样。
			options.inSampleSize = 1;
			while ((options.outWidth + (long) options.inSampleSize - 1) / options.inSampleSize > MAX_SIDE
				|| (options.outHeight + (long) options.inSampleSize - 1) / options.inSampleSize > MAX_SIDE) {
				options.inSampleSize *= 2;
			}
			checkCancelled();
			options.inJustDecodeBounds = false;
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
			if (bitmap == null) {
				throw new IOException("无法解码图片。");
			}
			return bitmap;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException ignored) {
					// 不覆盖原始读取或解码异常。
				}
			}
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private static byte[] readBytes(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[8192];
		int count;
		while ((count = input.read(buffer)) != -1) {
			checkCancelled();
			if (count > MAX_BYTES - output.size()) {
				throw new IOException("图片文件超过 20 MiB。");
			}
			output.write(buffer, 0, count);
		}
		checkCancelled();
		return output.toByteArray();
	}

	private static void checkCancelled() throws InterruptedIOException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedIOException("图片载入已取消。");
		}
	}
}
