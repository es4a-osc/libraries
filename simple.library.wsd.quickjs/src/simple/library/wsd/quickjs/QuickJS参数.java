package simple.library.wsd.quickjs;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 同步宿主回调的参数视图；值为借用引用，仅在当前回调内有效。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class QuickJS参数 {

	private final QuickJS值[] values;

	/**
	 * 建立参数视图。
	 *
	 * @param values 本次参数。
	 */
	QuickJS参数(QuickJS值[] values) {
		this.values = values;
	}

	/**
	 * 取得实际参数数量。
	 *
	 * @return 实际传入的参数数量。
	 */
	@SimpleFunction
	public int 取数量() {
		return values.length;
	}

	/**
	 * 取得借用参数；越界报告错误，参数本身可为 undefined。
	 *
	 * @param index 从 0 开始的索引。
	 * @return 当前回调内有效的借用参数引用。
	 */
	@SimpleFunction
	public QuickJS值 取值(int index) {
		if (index < 0 || index >= values.length) {
			throw new QuickJS错误("回调参数索引超出范围");
		}
		return values[index];
	}

	/**
	 * 复制参数引用数组，数组中的值仍为本次回调借用值。
	 *
	 * @return 数组副本；元素仍是当前回调的借用引用。
	 */
	@SimpleFunction
	public QuickJS值[] 取全部() {
		return values.clone();
	}
}
