package simple.library.wsd.xqrcode.internal;

/**
 * 相机亮度方向与预览裁剪的纯计算。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class FrameGeometry {
	/**
	 * 禁止创建工具实例。
	 */
	private FrameGeometry() {
	}

	/**
	 * 将 Y 平面旋转到屏幕方向，前摄时另作水平镜像。
	 *
	 * @param source NV21 帧。
	 * @param width 输入宽度。
	 * @param height 输入高度。
	 * @param degrees 顺时针旋转角度，取 0、90、180 或 270。
	 * @param mirror 是否在旋转后水平镜像。
	 * @return 只包含亮度的新数组。
	 */
	public static byte[] orient(byte[] source, int width, int height, int degrees, boolean mirror) {
		if (width <= 0 || height <= 0 || source.length < (long) width * height
				|| (degrees != 0 && degrees != 90 && degrees != 180 && degrees != 270)) {
			throw new IllegalArgumentException("相机帧尺寸或角度无效");
		}
		int outWidth = degrees % 180 == 0 ? width : height;
		int outHeight = degrees % 180 == 0 ? height : width;
		byte[] output = new byte[width * height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int dx = x;
				int dy = y;
				if (degrees == 90) {
					dx = height - 1 - y;
					dy = x;
				} else if (degrees == 180) {
					dx = width - 1 - x;
					dy = height - 1 - y;
				} else if (degrees == 270) {
					dx = y;
					dy = width - 1 - x;
				}
				if (mirror) {
					dx = outWidth - 1 - dx;
				}
				output[dy * outWidth + dx] = source[y * width + x];
			}
		}
		return output;
	}

	/**
	 * 将居中裁剪预览上的矩形映射到旋转后的亮度图像。
	 *
	 * @param width 旋转后的图像宽度。
	 * @param height 旋转后的图像高度。
	 * @param viewWidth 预览容器宽度。
	 * @param viewHeight 预览容器高度。
	 * @param left 界面矩形左边。
	 * @param top 界面矩形上边。
	 * @param right 界面矩形右边。
	 * @param bottom 界面矩形下边。
	 * @return 图像左、上、宽、高。
	 */
	public static int[] crop(int width, int height, int viewWidth, int viewHeight, float left, float top, float right, float bottom) {
		if (width <= 0 || height <= 0 || viewWidth <= 0 || viewHeight <= 0) {
			throw new IllegalArgumentException("预览尚无有效尺寸");
		}
		float scale = Math.max((float) viewWidth / width, (float) viewHeight / height);
		float offsetX = (viewWidth - width * scale) / 2;
		float offsetY = (viewHeight - height * scale) / 2;
		int x = Math.max(0, Math.min(width - 1, (int) ((left - offsetX) / scale)));
		int y = Math.max(0, Math.min(height - 1, (int) ((top - offsetY) / scale)));
		int endX = Math.max(x + 1, Math.min(width, (int) Math.ceil((right - offsetX) / scale)));
		int endY = Math.max(y + 1, Math.min(height, (int) Math.ceil((bottom - offsetY) / scale)));
		return new int[] {x, y, endX - x, endY - y};
	}
}
