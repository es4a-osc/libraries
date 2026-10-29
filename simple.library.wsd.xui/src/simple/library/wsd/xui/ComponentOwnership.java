package simple.library.wsd.xui;

import android.view.View;

import simple.runtime.components.impl.android.视图组件;
import simple.runtime.components.可视组件;
import simple.runtime.errors.非法参数错误;

/**
 * 校验自定义可视内容的同树归属，拒绝循环包含及借出宿主自身。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class ComponentOwnership {

	/**
	 * 校验工具不创建实例。
	 */
	private ComponentOwnership() {
	}

	/**
	 * 在移动前验证目标，允许预先隐藏的同树组件。
	 *
	 * @param host 接收内容的视图。
	 * @param component 将被暂借的 Simple 组件。
	 * @return 可移动的原生视图。
	 */
	static View requireSibling(View host, 可视组件 component) {
		if (!(component instanceof 视图组件)) {
			throw new 非法参数错误();
		}
		View target = ((视图组件) component).getView();
		if (target == null || target.getParent() == null || target.getRootView() != host.getRootView()) {
			throw new 非法参数错误();
		}
		for (View current = host; current != null; current = current.getParent() instanceof View ? (View) current.getParent() : null) {
			if (current == target) {
				throw new 非法参数错误();
			}
		}
		return target;
	}
}
