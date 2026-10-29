package simple.library.wsd.xui;

import android.view.View;
import android.widget.TextView;

import com.xuexiang.xui.widget.textview.supertextview.BaseTextView;

import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * XUI基础文本的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI基础文本Impl extends XUI视图组件基类 implements XUI基础文本 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private BaseTextView view;

	/**
	 * 创建XUI基础文本。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI基础文本Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new BaseTextView(ComponentSupport.createContext());

		return view;
	}

	@Override
	public void 置顶部文本(String s) {
		view.setTopTextString(s);
	}

	@Override
	public void 置中间文本(String s) {
		view.setCenterTextString(s);
	}

	@Override
	public void 置底部文本(String s) {
		view.setBottomTextString(s);
	}

	@Override
	public void 置最大字宽(int topMaxEms, int centerMaxEms, int bottomMaxEms) {
		view.setMaxEms(topMaxEms, centerMaxEms, bottomMaxEms);
	}

	@Override
	public void 置中间间距高度(int centerSpaceHeight) {
		view.setCenterSpaceHeight(centerSpaceHeight);
	}

	@Override
	public void 置区域字体(int region, String asset, float size) {
		TextView[] regions = {view.getTopTextView(), view.getCenterTextView(), view.getBottomTextView()};
		ComponentSupport.validateIndex(region, regions.length);
		if (!Float.isFinite(size) || size <= 0) {
			throw new 非法参数错误();
		}
		TextView target = regions[region];
		if (target == null) {
			throw new ComponentStateException("该文字区域尚未创建，请先设置区域文字。");
		}
		target.setTypeface(ThemeSupport.loadFont(asset));
		target.setTextSize(size);
	}

}
