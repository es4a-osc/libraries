package simple.library.wsd.xui;

import android.content.res.ColorStateList;
import android.view.View;

import com.xuexiang.xui.widget.button.roundbutton.RoundButton;
import com.xuexiang.xui.widget.button.roundbutton.RoundDrawable;

import simple.runtime.components.组件容器;

/**
 * XUI圆角按钮的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI圆角按钮Impl extends XUI文本组件基类 implements XUI圆角按钮 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private RoundButton view;

	/**
	 * 创建XUI圆角按钮。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI圆角按钮Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new RoundButton(ComponentSupport.createContext());

		return view;
	}

	@Override
	public void 置填充颜色(int color) {
		getRoundBackground().setBgData(ColorStateList.valueOf(color));
	}

	@Override
	public void 置圆角(float radius) {
		getRoundBackground().setIsRadiusAdjustBounds(false);
		getRoundBackground().setCornerRadius(radius);
	}

	@Override
	public void 置描边(int width, int color) {
		getRoundBackground().setStrokeData(width, ColorStateList.valueOf(color));
	}

	/**
	 * 获取可配置的圆角背景；标准背景属性替换背景后重新建立原生形状。
	 *
	 * @return 当前圆角背景。
	 */
	private RoundDrawable getRoundBackground() {
		if (!(view.getBackground() instanceof RoundDrawable)) {
			view.setBackground(RoundDrawable.fromAttributeSet(view.getContext(), null));
		}
		return (RoundDrawable) view.getBackground();
	}
}
