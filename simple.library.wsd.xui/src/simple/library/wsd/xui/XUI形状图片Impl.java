package simple.library.wsd.xui;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.xuexiang.xui.widget.button.shinebutton.PorterShapeImageView;

import simple.runtime.components.组件容器;

/**
 * XUI形状图片的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI形状图片Impl extends XUI视图组件基类 implements XUI形状图片 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private PorterShapeImageView view;

	/**
	 * 创建XUI形状图片。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI形状图片Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new PorterShapeImageView(ComponentSupport.createContext());

		return view;
	}

	@Override
	public void 置着色颜色(int color) {
		view.setTintColor(color);
	}

	@Override
	public void 置图标图像(String image) {
		view.setIconDrawable(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置图片(String image) {
		Drawable drawable = ComponentSupport.getDrawable(image);
		view.setImageDrawable(drawable);
		view.setIconDrawable(drawable);
	}

	@Override
	public void 置图片缩放模式(int mode) {
		ComponentSupport.validateIndex(mode, ImageView.ScaleType.values().length);
		view.setScaleType(ImageView.ScaleType.values()[mode]);
	}

}
