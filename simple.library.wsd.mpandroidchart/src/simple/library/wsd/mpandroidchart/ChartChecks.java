package simple.library.wsd.mpandroidchart;

/**
 * 配置与模型共同使用的参数约束，不依赖 Android 环境。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class ChartChecks {
	/**
	 * 静态校验工具。
	 */
	private ChartChecks() {
	}

	/**
	 * 检查有限单精度范围。
	 *
	 * @param value 待检查数值。
	 * @param min   最小值。
	 * @param max   最大值。
	 */
	static void range(float value, float min, float max) {
		if (Float.isNaN(value) || Float.isInfinite(value) || value < min || value > max) {
			throw new IllegalArgumentException("数值必须位于 " + min + " 至 " + max + " 之间。");
		}
	}

	/**
	 * 检查整数范围。
	 *
	 * @param value 待检查数值。
	 * @param min   最小值。
	 * @param max   最大值。
	 */
	static void integer(int value, int min, int max) {
		if (value < min || value > max) {
			throw new IllegalArgumentException("整数必须位于 " + min + " 至 " + max + " 之间。");
		}
	}

	/**
	 * 拒绝空引用。
	 *
	 * @param value 配置或数据引用。
	 */
	static void required(Object value) {
		if (value == null) {
			throw new IllegalArgumentException("参数不能是空引用。");
		}
	}

	/**
	 * 校验文本引用，允许空文本。
	 *
	 * @param value 文本。
	 */
	static void text(String value) {
		required(value);
	}

	/**
	 * 校验数组及文本元素。
	 *
	 * @param value 数组。
	 */
	static void array(Object value) {
		required(value);
		if (value instanceof String[]) {
			for (String item : (String[]) value) {
				required(item);
			}
		}
	}

	/**
	 * 校验等长图例数组。
	 *
	 * @param names  名称数组。
	 * @param colors 颜色数组。
	 */
	static void labels(String[] names, int[] colors) {
		array(names);
		array(colors);
		if (names.length != colors.length) {
			throw new IllegalArgumentException("名称与颜色数组须等长。");
		}
	}

	/**
	 * 检查五类图形的完整绘制顺序。
	 *
	 * @param order 按绘制先后排列的类型编号。
	 */
	static void order(int[] order) {
		required(order);
		if (order.length != 5) {
			throw new IllegalArgumentException("绘制顺序必须包含五类图形。");
		}
		boolean[] seen = new boolean[5];
		for (int value : order) {
			integer(value, 0, 4);
			if (seen[value]) {
				throw new IllegalArgumentException("绘制类型不能重复。");
			}
			seen[value] = true;
		}
	}
}
