package simple.library.wsd.bluetoothprinter;

import java.io.ByteArrayOutputStream;

/**
 * 24 点 ESC/POS 位图编码，末行不足 24 点时补白，不追加未使用的缓冲区字节。
 */
final class EscPosImage {

	private EscPosImage() {
	}

	/* 位图编码 */

	static byte[] encode(int[] pixels, int width, int height) {
		if (width <= 0 || width > 4096 || height <= 0 || height > 4096 || pixels == null || pixels.length != width * height)
			throw new IllegalArgumentException("图片尺寸或像素数据无效");
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		output.write(27);
		output.write(51);
		output.write(0);

		// 每 24 行生成一组位图指令，不足的行保持白色。
		for (int top = 0; top < height; top += 24) {
			output.write(27);
			output.write(42);
			output.write(33);
			output.write(width & 255);
			output.write(width >>> 8);

			for (int x = 0; x < width; x++) {
				for (int group = 0; group < 3; group++) {
					int value = 0;

					for (int bit = 0; bit < 8; bit++) {
						int y = top + group * 8 + bit;
						value <<= 1;
						if (y >= height) continue;

						int pixel = pixels[y * width + x];

						// 先将透明像素与白底合成，再按灰度阈值生成打印点。
						int alpha = pixel >>> 24;
						int gray = (299 * ((pixel >>> 16) & 255) + 587 * ((pixel >>> 8) & 255) + 114 * (pixel & 255)) / 1000;

						gray = (gray * alpha + 255 * (255 - alpha)) / 255;
						if (gray <= 95) value |= 1;
					}

					output.write(value);
				}
			}

			output.write(10);
		}

		// 位图打印完成恢复缺省行距，后续文本不会继续使用零行距。
		output.write(27);
		output.write(50);
		return output.toByteArray();
	}
}
