package simple.library.wsd.xui;

import android.view.View;

import com.xuexiang.xui.widget.textview.autofit.AutoFitTextView;

import simple.runtime.components.组件容器;

/**
 * XUI自适应文本的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI自适应文本Impl extends XUI文本组件基类 implements XUI自适应文本 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private AutoFitTextView view;

	/**
	 * 创建XUI自适应文本。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI自适应文本Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new AutoFitTextView(ComponentSupport.createContext());

		return view;
	}

	@Override
	public void 置行数(int lines) {
		view.setLines(lines);
	}

	@Override
	public void 置最大行数(int maxLines) {
		view.setMaxLines(maxLines);
	}

	@Override
	public boolean 是否启用适应() {
		return view.isEnableFit();
	}

	@Override
	public void 置启用适应(boolean enableFit) {
		view.setEnableFit(enableFit);
	}

	@Override
	public float 取最大文本尺寸() {
		return view.getMaxTextSize();
	}

	@Override
	public void 置最大文本尺寸(float size) {
		view.setMaxTextSize(size);
	}

	@Override
	public void 置最大文本尺寸(int unit, float size) {
		view.setMaxTextSize(unit, size);
	}

	@Override
	public float 取最小文本尺寸() {
		return view.getMinTextSize();
	}

	@Override
	public void 置最小文本尺寸(int minSize) {
		view.setMinTextSize(minSize);
	}

	@Override
	public void 置最小文本尺寸(int unit, float minSize) {
		view.setMinTextSize(unit, minSize);
	}

	@Override
	public float 取精度() {
		return view.getPrecision();
	}

	@Override
	public void 置精度(float precision) {
		view.setPrecision(precision);
	}
}
