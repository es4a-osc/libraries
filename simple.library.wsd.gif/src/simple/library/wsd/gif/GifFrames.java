package simple.library.wsd.gif;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 保留按毫秒绘制、暂停继续和回到首帧的统一 GIF 后端。
 *
 * <p>AnimatedImageDrawable 的 start 会重置已停止的动画，不能等价实现继续播放。
 * API 28 起使用 ImageDecoder 解码独立帧并合成；旧系统继续使用 Movie。</p>
 *
 * @author 树先生 xhwsd@qq.com
 */
abstract class GifFrames {
	private final int width;
	private final int height;
	private final int duration;

	/**
	 * 保存已经解码的画布和周期信息。
	 *
	 * @param width 画布宽度
	 * @param height 画布高度
	 * @param duration 播放周期，单位毫秒
	 */
	GifFrames(int width, int height, int duration) {
		this.width = width;
		this.height = height;
		this.duration = Math.max(1, duration);
	}

	/**
	 * 读取画布宽度，单位像素。
	 *
	 * @return 画布宽度，单位像素
	 */
	final int width() {
		return width;
	}

	/**
	 * 读取画布高度，单位像素。
	 *
	 * @return 画布高度，单位像素
	 */
	final int height() {
		return height;
	}

	/**
	 * 读取一次播放的时长，单位毫秒。
	 *
	 * @return 一次播放的时长，单位毫秒
	 */
	final int duration() {
		return duration;
	}

	/**
	 * 绘制指定时刻的完整画面，不改变播放时钟。
	 *
	 * @param canvas 目标画布
	 * @param x 绘制横坐标
	 * @param y 绘制纵坐标
	 * @param position 周期内的毫秒位置
	 */
	abstract void draw(Canvas canvas, float x, float y, int position);

	/**
	 * 释放本后端拥有的帧位图；停止使用后调用。
	 */
	void close() {
	}

	/**
	 * 输入已通过容器及像素规模检查，再按系统选择解码器。
	 *
	 * @param data GIF 完整字节
	 * @param info 已验证的画布和帧数
	 * @return 支持随机时刻绘制的图像
	 * @throws IOException 解码失败或任务取消
	 */
	static GifFrames decode(byte[] data, GifInfo info) throws IOException {
		return Build.VERSION.SDK_INT >= 28 ? Modern.decode(data, info) : decodeLegacy(data, info);
	}

	/**
	 * API 27 及以下没有 ImageDecoder，使用旧平台的 GIF 时间定位能力。
	 *
	 * @param data 已检查的 GIF 字节
	 * @param info 已检查的尺寸
	 * @return 旧平台动画后端
	 * @throws IOException 平台拒绝解码
	 */
	@SuppressWarnings("deprecation")
	private static GifFrames decodeLegacy(byte[] data, GifInfo info) throws IOException {
		final android.graphics.Movie movie = android.graphics.Movie.decodeByteArray(data, 0, data.length);
		if (movie == null || movie.width() != info.width || movie.height() != info.height) {
			throw new IOException("平台无法解码此 GIF");
		}
		return new GifFrames(info.width, info.height, movie.duration()) {
			@Override
			void draw(Canvas canvas, float x, float y, int position) {
				movie.setTime(position);
				movie.draw(canvas, x, y);
			}
		};
	}

	/**
	 * 新平台类型隔离在本类中，低版本不加载 ImageDecoder。
	 */
	private static final class Modern extends GifFrames {
		private final Bitmap[] frames;
		private final int[] ends;

		/**
		 * 接管已完成的完整帧与累计时间表。
		 *
		 * @param info 容器信息
		 * @param frames 合成后的帧
		 * @param ends 各帧结束时刻
		 */
		Modern(GifInfo info, Bitmap[] frames, int[] ends) {
			super(info.width, info.height, ends[ends.length - 1]);
			this.frames = frames;
			this.ends = ends;
		}

		@Override
		void draw(Canvas canvas, float x, float y, int position) {
			int index = 0;
			while (index < ends.length - 1 && position >= ends[index]) {
				index++;
			}
			canvas.drawBitmap(frames[index], x, y, null);
		}

		@Override
		void close() {
			for (Bitmap frame : frames) {
				frame.recycle();
			}
		}

		/**
		 * 逐帧交给平台解压，按 GIF 处置方式合成透明和局部更新。
		 *
		 * @param data 已通过 GifInfo 验证的字节
		 * @param info 画布尺寸及像素预算
		 * @return 完整帧后端
		 * @throws IOException 解码、帧控制或任务取消错误
		 */
		static GifFrames decode(byte[] data, GifInfo info) throws IOException {
			int headerSize = 13 + ((data[10] & 128) == 0 ? 0 : 3 * (2 << (data[10] & 7)));
			int background = Color.TRANSPARENT;
			int backgroundIndex = data[11] & 255;
			int palette = 13 + backgroundIndex * 3;
			if (palette + 2 < headerSize) {
				background = Color.rgb(data[palette] & 255, data[palette + 1] & 255, data[palette + 2] & 255);
			}
			List<Bitmap> completed = new ArrayList<>();
			int[] ends = new int[info.frames];
			Bitmap working = Bitmap.createBitmap(info.width, info.height, Bitmap.Config.ARGB_8888);
			Bitmap restore = null;
			Canvas canvas = new Canvas(working);
			int position = headerSize;
			byte[] control = null;
			int previousDisposal = 0;
			int previousBackground = Color.TRANSPARENT;
			android.graphics.Rect previousRect = new android.graphics.Rect();
			int elapsed = 0;
			boolean success = false;
			try {
				while ((data[position] & 255) != 0x3b) {
					if (Thread.currentThread().isInterrupted()) {
						throw new IOException("GIF 载入已取消");
					}
					int start = position++;
					int block = data[start] & 255;
					if (block == 0x21) {
						int label = data[position++] & 255;
						int end = skipBlocks(data, position);
						if (label == 0xf9) {
							if ((data[position] & 255) != 4 || end - start != 8) {
								throw new IOException("GIF 帧控制块无效");
							}
							control = java.util.Arrays.copyOfRange(data, start, end);
						}
						position = end;
						continue;
					}
					int left = word(data, position);
					int top = word(data, position + 2);
					int width = word(data, position + 4);
					int height = word(data, position + 6);
					int flags = data[position + 8] & 255;
					position += 9 + ((flags & 128) == 0 ? 0 : 3 * (2 << (flags & 7)));
					position = skipBlocks(data, position + 1);
					int controlFlags = control == null ? 0 : control[3] & 255;
					int disposal = (controlFlags >> 2) & 7;
					int clear = (controlFlags & 1) != 0 ? Color.TRANSPARENT : background;
					if (completed.isEmpty()) {
						canvas.drawColor(clear, PorterDuff.Mode.SRC);
					} else if (previousDisposal == 2) {
						int saved = canvas.save();
						canvas.clipRect(previousRect);
						canvas.drawColor(previousBackground, PorterDuff.Mode.SRC);
						canvas.restoreToCount(saved);
					} else if (previousDisposal == 3 && restore != null) {
						canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
						canvas.drawBitmap(restore, 0, 0, null);
					}
					if (restore != null) {
						restore.recycle();
						restore = null;
					}
					if (disposal == 3) {
						restore = working.copy(Bitmap.Config.ARGB_8888, false);
					}

					// 每段生成独立的局部 GIF，平台负责 LZW、交错和透明色解码。
					byte[] header = java.util.Arrays.copyOf(data, headerSize);
					putWord(header, 6, width);
					putWord(header, 8, height);
					byte[] frame = java.util.Arrays.copyOfRange(data, start, position);
					putWord(frame, 1, 0);
					putWord(frame, 3, 0);
					ByteArrayOutputStream encoded = new ByteArrayOutputStream();
					encoded.write(header);
					if (control != null) {
						encoded.write(control);
					}
					encoded.write(frame);
					encoded.write(0x3b);
					Bitmap patch = android.graphics.ImageDecoder.decodeBitmap(
						android.graphics.ImageDecoder.createSource(ByteBuffer.wrap(encoded.toByteArray())),
						(decoder, image, source) -> decoder.setAllocator(android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE));
					try {
						canvas.drawBitmap(patch, left, top, null);
					} finally {
						patch.recycle();
					}
					completed.add(working.copy(Bitmap.Config.ARGB_8888, false));
					int delay = control == null ? 0 : word(control, 4) * 10;
					elapsed += delay <= 10 ? 100 : delay;
					ends[completed.size() - 1] = elapsed;
					previousDisposal = disposal;
					previousBackground = clear;
					previousRect.set(left, top, left + width, top + height);
					control = null;
				}
				Modern result = new Modern(info, completed.toArray(new Bitmap[0]), ends);
				success = true;
				return result;
			} finally {
				working.recycle();
				if (restore != null) {
					restore.recycle();
				}
				if (!success) {
					for (Bitmap frame : completed) {
						frame.recycle();
					}
				}
			}
		}

		/**
		 * 跳过以零长度结束的 GIF 数据子块；输入边界已由容器检查。
		 *
		 * @param data GIF 字节
		 * @param position 首个长度字节的位置
		 * @return 子块结束后的索引
		 */
		private static int skipBlocks(byte[] data, int position) {
			int size;
			while ((size = data[position++] & 255) != 0) {
				position += size;
			}
			return position;
		}

		/**
		 * 读取小端无符号字。
		 *
		 * @param data 已检查的字节
		 * @param offset 字的起始位置
		 * @return 无符号整数
		 */
		private static int word(byte[] data, int offset) {
			return (data[offset] & 255) | ((data[offset + 1] & 255) << 8);
		}

		/**
		 * 写入小端字，用于局部单帧容器。
		 *
		 * @param data 要更新的容器头
		 * @param offset 字的起始位置
		 * @param value 已检查的尺寸或零偏移
		 */
		private static void putWord(byte[] data, int offset, int value) {
			data[offset] = (byte) value;
			data[offset + 1] = (byte) (value >> 8);
		}
	}
}
