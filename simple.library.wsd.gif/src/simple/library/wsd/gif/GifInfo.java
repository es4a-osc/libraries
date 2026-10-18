package simple.library.wsd.gif;

import java.io.IOException;

/**
 * 在交给平台解码前检查 GIF 结构、输入规模和估算帧像素规模。
 *
 * <p>只解析容器，不实现 LZW 解码。拒绝截断数据、无图像及超过限定规模的输入。</p>
 *
 * @author 树先生 xhwsd@qq.com
 */
final class GifInfo {

	static final int MAX_BYTES = 8 * 1024 * 1024;
	private static final long MAX_FRAME_PIXELS = 16L * 1024 * 1024;

	final int width;
	final int height;
	final int frames;

	/**
	 * 保存已经通过容器检查的图像信息，不重复校验。
	 *
	 * @param width 逻辑画布宽度，单位像素。
	 * @param height 逻辑画布高度，单位像素。
	 * @param frames 已计数的图像帧数，不包含扩展块。
	 */
	private GifInfo(int width, int height, int frames) {
		this.width = width;
		this.height = height;
		this.frames = frames;
	}

	/**
	 * 检查 GIF 容器结构及规模限制，返回画布尺寸和图像帧数。
	 *
	 * <p>接受 GIF87a 和 GIF89a，输入不超过 8 MiB，宽高各不超过 2048 像素，
	 * 帧数不超过 512，画布面积乘帧数不超过 16777216。
	 * 在首个结束标记处返回，不扫描其后的数据。</p>
	 *
	 * <p>检查数据块边界、图像矩形和 LZW 初始码长，但不解码压缩数据，
	 * 也不校验扩展块的具体语义；通过检查不保证平台一定能够解码。</p>
	 *
	 * @param data 完整的 GIF 文件字节；检查期间调用方不得修改内容。
	 * @return 已验证容器的画布尺寸和图像帧数，不保留输入字节的引用。
	 * @throws IOException 数据为空、签名或容器结构无效、缺少图像或结束标记，
	 * 或输入规模超过限制。
	 */
	static GifInfo read(byte[] data) throws IOException {
		// 先限制输入规模并确认容器签名，再读取尺寸和数据块。
		if (data == null || data.length < 14 || data.length > MAX_BYTES) {
			throw new IOException("GIF 数据为空、过短或超过 8 MiB");
		}

		String signature = new String(data, 0, 6, "US-ASCII");
		if (!"GIF87a".equals(signature) && !"GIF89a".equals(signature)) {
			throw new IOException("仅支持 GIF87a 或 GIF89a 图片");
		}

		int width = word(data, 6);
		int height = word(data, 8);
		if (width == 0 || height == 0 || width > 2048 || height > 2048) {
			throw new IOException("GIF 宽高须在 1～2048 像素内");
		}

		// 跳过可选的全局颜色表，后续逐块检查图像和扩展。
		int position = 13;
		int packed = data[10] & 255;
		if ((packed & 128) != 0) {
			position = skip(data, position, 3 * (2 << (packed & 7)));
		}

		int frames = 0;
		while (position < data.length) {
			int block = data[position++] & 255;
			if (block == 0x3b) {
				if (frames == 0) {
					throw new IOException("GIF 不包含图像帧");
				}
				return new GifInfo(width, height, frames);
			}
			if (block == 0x21) {
				position = skip(data, position, 1);
			} else if (block == 0x2c) {
				int header = position;
				position = skip(data, position, 9);
				if (word(data, header + 4) == 0 || word(data, header + 6) == 0
					|| word(data, header) + word(data, header + 4) > width
					|| word(data, header + 2) + word(data, header + 6) > height) {
					throw new IOException("GIF 图像帧尺寸无效");
				}

				int flags = data[header + 8] & 255;
				if ((flags & 128) != 0) {
					position = skip(data, position, 3 * (2 << (flags & 7)));
				}
				position = skip(data, position, 1);
				int codeSize = data[position - 1] & 255;
				if (codeSize < 2 || codeSize > 8) {
					throw new IOException("GIF 压缩码长无效");
				}

				frames++;
				if (frames > 512 || (long) width * height * frames > MAX_FRAME_PIXELS) {
					throw new IOException("GIF 帧数或估算解码规模过大");
				}
			} else {
				throw new IOException("GIF 数据块无效");
			}

			// 扩展及图像数据均以长度子块组织，每个子块都必须完整。
			while (true) {
				position = skip(data, position, 1);
				int size = data[position - 1] & 255;
				if (size == 0) {
					break;
				}
				position = skip(data, position, size);
			}
		}
		throw new IOException("GIF 缺少结束标记");
	}

	/**
	 * 按 GIF 小端顺序读取一个无符号 16 位整数。
	 *
	 * <p>本方法不检查边界；调用方须先保证当前位置起的两个字节存在。</p>
	 *
	 * @param data 已检查为非空的 GIF 数据。
	 * @param position 低位字节的索引。
	 * @return 0～65535 范围内的整数。
	 * @throws ArrayIndexOutOfBoundsException 调用方未保证两个字节均在数据范围内。
	 */
	private static int word(byte[] data, int position) {
		return (data[position] & 255) | ((data[position + 1] & 255) << 8);
	}

	/**
	 * 检查待跳过的区间完整后，返回区间结束位置。
	 *
	 * <p>用剩余长度比较避免位置相加溢出；允许零长度区间及结束位置等于数据长度。</p>
	 *
	 * @param data 已检查为非空的 GIF 数据。
	 * @param position 区间起始索引，可以等于数据长度。
	 * @param count 跳过的字节数，不得为负数。
	 * @return 下一段数据的起始索引，或数据末尾索引。
	 * @throws IOException 位置或长度为负数，或区间超出数据范围。
	 */
	private static int skip(byte[] data, int position, int count) throws IOException {
		if (count < 0 || position < 0 || position > data.length - count) {
			throw new IOException("GIF 数据不完整");
		}
		return position + count;
	}
}
