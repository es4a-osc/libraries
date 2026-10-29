package simple.library.wsd.xui;

import android.view.View;
import android.widget.ImageView;

import com.xuexiang.xui.widget.button.shadowbutton.ShadowImageView;

import simple.runtime.components.组件容器;

/**
 * XUI阴影图片的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI阴影图片Impl extends XUI视图组件基类 implements XUI阴影图片 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private ShadowImageView view;

	/**
	 * 创建XUI阴影图片。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI阴影图片Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new ShadowImageView(ComponentSupport.createContext());

		return view;
	}

	@Override
	public int 取按下颜色() {
		return view.getPressedColor();
	}

	@Override
	public void 置按下颜色(int pressedColor) {
		view.setPressedColor(pressedColor);
	}

	@Override
	public void 置图片(String image) {
		view.setImageDrawable(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置图片缩放模式(int mode) {
		ComponentSupport.validateIndex(mode, ImageView.ScaleType.values().length);
		view.setScaleType(ImageView.ScaleType.values()[mode]);
	}

}
