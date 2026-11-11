package simple.library.wsd.achartengine;

/**
 * 统一检查数值、索引和引用，保证失败时不修改图表模型。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class Checks {

	/**
	 * 创建不可实例化的校验工具。
	 */
	private Checks() {
	}

	/**
	 * 检查非空引用。
	 *
	 * @param value 待检查引用。
	 */
	static void required(Object value) {
		if (value == null) {
			throw new IllegalArgumentException("参数不能是空引用。");
		}
	}

	/**
	 * 检查文本引用，允许空文本。
	 *
	 * @param value 待检查文本。
	 */
	static void text(String value) {
		required(value);
	}

	/**
	 * 检查整数范围。
	 *
	 * @param value 待检查值。
	 * @param min   最小值。
	 * @param max   最大值。
	 */
	static void index(int value, int min, int max) {
		if (value < min || value > max) {
			throw new IllegalArgumentException("整数须在 " + min + " 至 " + max + " 之间。");
		}
	}

	/**
	 * 检查有限数值范围。
	 *
	 * @param value 待检查值。
	 * @param min   最小值。
	 * @param max   最大值。
	 */
	static void range(double value, double min, double max) {
		if (Double.isNaN(value) || Double.isInfinite(value) || value < min || value > max) {
			throw new IllegalArgumentException("数值须有限且在 " + min + " 至 " + max + " 之间。");
		}
	}

	/**
	 * 检查绘制值并为中间运算保留空间。
	 *
	 * @param value 绘制数值。
	 */
	static void finite(double value) {
		range(value, -1e100, 1e100);
	}

	/**
	 * 复制并检查文本数组。
	 *
	 * @param values 原数组。
	 * @return 独立数组。
	 */
	static String[] names(String[] values) {
		required(values);
		for (String value : values) {
			text(value);
		}
		return values.clone();
	}

	/**
	 * 检查递增范围。
	 *
	 * @param min 最小值。
	 * @param max 最大值。
	 */
	static void bounds(double min, double max) {
		finite(min);
		finite(max);
		if (min >= max) {
			throw new IllegalArgumentException("最小值必须小于最大值。");
		}
	}
}
