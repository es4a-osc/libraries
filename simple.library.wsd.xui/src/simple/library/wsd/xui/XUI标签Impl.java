package simple.library.wsd.xui;

import android.view.View;
import android.view.ViewGroup;

import com.xuexiang.xui.widget.textview.label.LabelView;

import simple.runtime.components.impl.android.视图组件;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * XUI标签的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI标签Impl extends XUI文本组件基类 implements XUI标签 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private LabelView view;

	/**
	 * 创建XUI标签。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI标签Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new LabelView(ComponentSupport.createContext());

		return view;
	}

	@Override
	public void 绑定目标(可视组件 target, int distance, boolean right) {
		View nativeTarget = ((视图组件) target).getView();
		if (nativeTarget == view || nativeTarget.getParent() == null) {
			throw new 非法参数错误();
		}
		view.remove();
		if (view.getParent() instanceof ViewGroup) {
			((ViewGroup) view.getParent()).removeView(view);
		}
		view.setTargetView(nativeTarget, distance, right ? LabelView.Gravity.RIGHT_TOP : LabelView.Gravity.LEFT_TOP);
	}

	@Override
	public void 销毁() {
		view.remove();
		super.销毁();
	}
}
