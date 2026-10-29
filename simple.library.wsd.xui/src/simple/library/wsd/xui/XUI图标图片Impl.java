package simple.library.wsd.xui;

import android.view.View;
import android.widget.ImageView;

import com.xuexiang.xui.widget.imageview.IconImageView;

import simple.runtime.components.组件容器;

/**
 * XUI图标图片的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI图标图片Impl extends XUI视图组件基类 implements XUI图标图片 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private IconImageView view;

	/**
	 * 创建XUI图标图片。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI图标图片Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new IconImageView(ComponentSupport.createContext());

		return view;
	}

	@Override
	public void 置是否显示图标(boolean isShowIcon) {
		view.setIsShowIcon(isShowIcon);
	}

	@Override
	public void 置图标缩放(float iconScale) {
		view.setIconScale(iconScale);
	}

	@Override
	public void 置图片(String image) {
		view.setImageDrawable(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置图标图像(String image) {
		view.setIconDrawable(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置图片缩放模式(int mode) {
		ComponentSupport.validateIndex(mode, ImageView.ScaleType.values().length);
		view.setScaleType(ImageView.ScaleType.values()[mode]);
	}

}
