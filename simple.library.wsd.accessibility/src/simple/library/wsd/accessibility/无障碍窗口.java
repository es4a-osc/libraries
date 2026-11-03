package simple.library.wsd.accessibility;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityWindowInfo;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 可交互窗口的只读视图，不代表系统中的所有窗口。使用后释放，界面变化后应重新查询。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 无障碍窗口 {

	/**
	 * 本包装拥有的原生窗口，释放后为空。
	 */
	private AccessibilityWindowInfo value;

	/**
	 * 接管原生窗口。
	 *
	 * @param value 非空原生窗口
	 */
	无障碍窗口(AccessibilityWindowInfo value) {
		this.value = value;
	}

	/**
	 * 包装可空的系统窗口。
	 *
	 * @param value 系统窗口，可以为空
	 * @return 窗口包装或空值
	 */
	static 无障碍窗口 wrap(AccessibilityWindowInfo value) {

		return value == null ? null : new 无障碍窗口(value);
	}

	/**
	 * 拒绝访问已释放窗口。
	 *
	 * @return 有效窗口
	 */
	private AccessibilityWindowInfo require() {
		if (value == null) {
			throw new IllegalStateException("窗口已释放");
		}

		return value;
	}

	/* 属性 */

	/**
	 * 编号。
	 *
	 * @return 编号。
	 */
	@SimpleProperty
	public int 编号() {
		return require().getId();
	}

	/**
	 * 类型。
	 *
	 * @return 类型。
	 */
	@SimpleProperty
	public int 类型() {
		return require().getType();
	}

	/**
	 * 层次。
	 *
	 * @return 层次。
	 */
	@SimpleProperty
	public int 层次() {
		return require().getLayer();
	}

	/**
	 * 子窗口数。
	 *
	 * @return 子窗口数。
	 */
	@SimpleProperty
	public int 子窗口数() {
		return require().getChildCount();
	}

	/**
	 * 标题。
	 *
	 * @return 标题。
	 */
	@SimpleProperty
	public String 标题() {
		return AccessibilitySupport.text(require().getTitle());
	}

	/**
	 * 活动。
	 *
	 * @return 活动。
	 */
	@SimpleProperty
	public boolean 活动() {
		return require().isActive();
	}

	/**
	 * 输入焦点。
	 *
	 * @return 输入焦点。
	 */
	@SimpleProperty
	public boolean 输入焦点() {
		return require().isFocused();
	}

	/**
	 * 无障碍焦点。
	 *
	 * @return 无障碍焦点。
	 */
	@SimpleProperty
	public boolean 无障碍焦点() {
		return require().isAccessibilityFocused();
	}

	/**
	 * 窗口是否处于画中画，需要 API 26 及以上。
	 *
	 * @return 窗口是否处于画中画，需要 API 26 及以上。
	 */
	@SimpleProperty
	public boolean 画中画() {
		AccessibilitySupport.api(26, "画中画状态");
		return require().isInPictureInPictureMode();
	}

	/* 方法 */

	/**
	 * 取得关联引用，使用后单独释放。
	 *
	 * @return 对应引用；不存在时为空值
	 */
	@SimpleFunction
	public 无障碍节点 取根节点() {
		return 无障碍节点.wrap(require().getRoot());
	}

	/**
	 * 取得关联引用，使用后单独释放。
	 *
	 * @return 对应引用；不存在时为空值
	 */
	@SimpleFunction
	public 无障碍节点 取锚节点() {
		return 无障碍节点.wrap(require().getAnchor());
	}

	/**
	 * 取得关联引用，使用后单独释放。
	 *
	 * @return 对应引用；不存在时为空值
	 */
	@SimpleFunction
	public 无障碍窗口 取父窗口() {
		return 无障碍窗口.wrap(require().getParent());
	}

	/**
	 * 按索引取得子窗口，使用后释放。
	 *
	 * @param index 从零开始的子窗口索引
	 * @return 子窗口；系统未提供时为空值
	 */
	@SimpleFunction
	public 无障碍窗口 取子窗口(int index) {
		AccessibilityWindowInfo window = require();
		if (index < 0 || index >= window.getChildCount()) {
			throw new IndexOutOfBoundsException("子窗口索引越界：" + index);
		}

		return wrap(window.getChild(index));
	}

	/**
	 * 取得屏幕像素边界快照。
	 *
	 * @return 像素矩形
	 */
	@SimpleFunction
	public 无障碍边界 取屏幕边界() {

		Rect bounds = new Rect();
		require().getBoundsInScreen(bounds);
		return new 无障碍边界(bounds);
	}

	/**
	 * 释放原生窗口，可重复调用。
	 */
	@SimpleFunction
	public void 释放() {
		if (value != null) {
			value.recycle();
			value = null;
		}
	}
}
