package simple.library.wsd.calendarview.internal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.*;
import java.util.*;

/**
 * 绘制前显式载入的有界位图集合，绘制期间不访问磁盘。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class ImagePool {
	private final Map<Integer, Bitmap> images = new HashMap<>();
	private int sequence;
	private long pixels;

	/**
	 * 创建不含图片的实例集合。
	 */
	public ImagePool() {
	}

	/**
	 * 读取资产、文件或系统内容 URI。
	 *
	 * @param context 应用上下文。
	 * @param path 输入路径。
	 * @return 本集合中的图片编号。
	 */
	public int load(Context context, String path) {
		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("图片路径为空");
		}
		try {
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
				int count;
				while ((count = input.read(buffer)) != -1) {
					if (output.size() + count > 16777216) {
						throw new IOException("压缩图片超过 16 MiB");
					}
					output.write(buffer, 0, count);
				}
				return load(output.toByteArray());
			}
		} catch (IOException | SecurityException error) {
			throw new IllegalArgumentException("图片读取失败：" + error.getMessage(), error);
		}
	}

	/**
	 * 采样解码图片并登记，整池最多占用 1600 万像素。
	 *
	 * @param bytes 完整压缩图像。
	 * @return 正整数图片编号。
	 */
	public int load(byte[] bytes) {
		if (bytes == null || bytes.length == 0 || bytes.length > 16777216) {
			throw new IllegalArgumentException("图片数据为空或超过 16 MiB");
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
		if (options.outWidth <= 0 || options.outHeight <= 0) {
			throw new IllegalArgumentException("无法读取图像尺寸");
		}
		options.inSampleSize = 1;
		while ((Math.max(options.outWidth, options.outHeight) + options.inSampleSize - 1L) / options.inSampleSize > 1024) {
			options.inSampleSize *= 2;
		}
		options.inJustDecodeBounds = false;
		Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
		if (image == null) {
			throw new IllegalArgumentException("图像解码失败");
		}
		long size = (long) image.getWidth() * image.getHeight();
		if (images.size() >= 64 || pixels + size > 16777216L) {
			image.recycle();
			throw new simple.library.wsd.calendarview.日历错误("绘制图片集合已满，请先释放不用的图片");
		}
		sequence = Math.incrementExact(sequence);
		images.put(sequence, image);
		pixels += size;
		return sequence;
	}

	/**
	 * 查找未释放的图片。
	 *
	 * @param id 图片编号。
	 * @return 可读取的位图。
	 */
	public Bitmap get(int id) {
		Bitmap image = images.get(id);
		if (image == null) {
			throw new IllegalArgumentException("绘制图片不存在或已释放");
		}
		return image;
	}

	/**
	 * 释放指定图片，重复调用安全。
	 *
	 * @param id 图片编号。
	 */
	public void release(int id) {
		Bitmap image = images.remove(id);
		if (image != null) {
			pixels -= (long) image.getWidth() * image.getHeight();
			image.recycle();
		}
	}

	/**
	 * 释放全部图片。
	 */
	public void close() {
		for (Integer id : new ArrayList<>(images.keySet())) {
			release(id);
		}
	}
}
