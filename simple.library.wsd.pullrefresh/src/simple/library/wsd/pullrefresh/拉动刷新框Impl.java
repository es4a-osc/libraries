package simple.library.wsd.pullrefresh;

import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import simple.runtime.android.MainActivity;
import simple.runtime.components.布局;
import simple.runtime.components.组件;
import simple.runtime.components.组件容器;
import simple.runtime.components.impl.android.视图组件;
import simple.runtime.components.impl.android.视图组件容器;
import simple.runtime.components.impl.android.面板Impl;
import simple.runtime.events.EventDispatcher;
import simple.runtime.errors.未初始化实例错误;
import simple.runtime.variants.IntegerVariant;
import simple.runtime.variants.Variant;

/**
 * 把双向刷新视图接入现有 Simple 面板布局，不另设子组件布局规则。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 拉动刷新框Impl extends 视图组件 implements 拉动刷新框, 视图组件容器 {

	private 面板Impl content;
	private boolean destroyed;

	/**
	 * 创建刷新容器，内部内容面板负责已有布局和子组件尺寸契约。
	 *
	 * @param container 所属可视容器。
	 */
	public 拉动刷新框Impl(组件容器 container) {
		super(container);

		// 子组件布局交给现有面板，刷新视图只负责滚动和手势。
		content = new 面板Impl(this);
		content.布局(IntegerVariant.getIntegerVariant(组件.布局_线性));

		// 原生回调转为 Simple 事件；销毁后不再向应用派发。
		view().setListener(new PullRefreshView.Listener() {
			@Override
			public void started(int direction) {
				if (!destroyed) {
					if (direction == PullRefreshView.DOWN) {
						下拉刷新开始();
					} else {
						上拉刷新开始();
					}
				}
			}

			@Override
			public void scrolled(int distance, boolean edge) {
				if (!destroyed) {
					被滚动(distance, edge);
				}
			}
		});
	}

	@Override
	protected View createView() {
		return new PullRefreshView(MainActivity.getContext());
	}

	/* 组件事件 */

	@Override
	public void 下拉刷新开始() {
		EventDispatcher.dispatchEvent(this, "下拉刷新开始");
	}

	@Override
	public void 上拉刷新开始() {
		EventDispatcher.dispatchEvent(this, "上拉刷新开始");
	}

	@Override
	public void 被滚动(int distance, boolean edge) {
		EventDispatcher.dispatchEvent(this, "被滚动", distance, edge);
	}

	/* 组件属性 */

	@Override
	public boolean 允许下拉() {
		checkAlive();
		return view().enabled(PullRefreshView.DOWN);
	}

	@Override
	public void 允许下拉(boolean enabled) {
		checkAlive();
		view().enabled(PullRefreshView.DOWN, enabled);
	}

	@Override
	public boolean 允许上拉() {
		checkAlive();
		return view().enabled(PullRefreshView.UP);
	}

	@Override
	public void 允许上拉(boolean enabled) {
		checkAlive();
		view().enabled(PullRefreshView.UP, enabled);
	}

	@Override
	public boolean 正在下拉刷新() {
		checkAlive();
		return view().active() == PullRefreshView.DOWN;
	}

	@Override
	public boolean 正在上拉刷新() {
		checkAlive();
		return view().active() == PullRefreshView.UP;
	}

	/* 上下拉提示与辅助文本 */

	@Override
	public String 下拉提示文本() {
		checkAlive();
		return view().text(PullRefreshView.DOWN, 0);
	}

	@Override
	public void 下拉提示文本(String text) {
		checkAlive();
		view().text(PullRefreshView.DOWN, 0, text);
	}

	@Override
	public String 下拉释放文本() {
		checkAlive();
		return view().text(PullRefreshView.DOWN, 1);
	}

	@Override
	public void 下拉释放文本(String text) {
		checkAlive();
		view().text(PullRefreshView.DOWN, 1, text);
	}

	@Override
	public String 下拉处理文本() {
		checkAlive();
		return view().text(PullRefreshView.DOWN, 2);
	}

	@Override
	public void 下拉处理文本(String text) {
		checkAlive();
		view().text(PullRefreshView.DOWN, 2, text);
	}

	@Override
	public String 下拉辅助文本() {
		checkAlive();
		return view().text(PullRefreshView.DOWN, 3);
	}

	@Override
	public void 下拉辅助文本(String text) {
		checkAlive();
		view().text(PullRefreshView.DOWN, 3, text);
	}

	@Override
	public String 上拉提示文本() {
		checkAlive();
		return view().text(PullRefreshView.UP, 0);
	}

	@Override
	public void 上拉提示文本(String text) {
		checkAlive();
		view().text(PullRefreshView.UP, 0, text);
	}

	@Override
	public String 上拉释放文本() {
		checkAlive();
		return view().text(PullRefreshView.UP, 1);
	}

	@Override
	public void 上拉释放文本(String text) {
		checkAlive();
		view().text(PullRefreshView.UP, 1, text);
	}

	@Override
	public String 上拉处理文本() {
		checkAlive();
		return view().text(PullRefreshView.UP, 2);
	}

	@Override
	public void 上拉处理文本(String text) {
		checkAlive();
		view().text(PullRefreshView.UP, 2, text);
	}

	@Override
	public String 上拉辅助文本() {
		checkAlive();
		return view().text(PullRefreshView.UP, 3);
	}

	@Override
	public void 上拉辅助文本(String text) {
		checkAlive();
		view().text(PullRefreshView.UP, 3, text);
	}

	/* 内容布局、背景与填充 */

	@Override
	public Variant 布局() {
		checkAlive();
		return content.布局();
	}

	@Override
	public void 布局(Variant type) {
		checkAlive();
		content.布局(type);
	}

	@Override
	public void 背景颜色(int color) {
		checkAlive();
		super.背景颜色(color);
		content.背景颜色(color);
	}

	@Override
	public Variant 左填充() {
		checkAlive();
		return content.左填充();
	}

	@Override
	public void 左填充(Variant value) {
		checkAlive();
		content.左填充(value);
	}

	@Override
	public Variant 顶填充() {
		checkAlive();
		return content.顶填充();
	}

	@Override
	public void 顶填充(Variant value) {
		checkAlive();
		content.顶填充(value);
	}

	@Override
	public Variant 右填充() {
		checkAlive();
		return content.右填充();
	}

	@Override
	public void 右填充(Variant value) {
		checkAlive();
		content.右填充(value);
	}

	@Override
	public Variant 底填充() {
		checkAlive();
		return content.底填充();
	}

	@Override
	public void 底填充(Variant value) {
		checkAlive();
		content.底填充(value);
	}

	@Override
	public String 背景图片() {
		checkAlive();
		return content.背景图片();
	}

	@Override
	public void 背景图片(String path) {
		checkAlive();
		content.背景图片(path);
	}

	@Override
	public void 置填充(Variant left, Variant top, Variant right, Variant bottom) {
		checkAlive();

		// 内容填充不能覆盖外层用于隐藏刷新头尾的内部偏移。
		content.置填充(left, top, right, bottom);
	}

	/* 刷新与滚动方法 */

	@Override
	public void 停止刷新() {
		checkAlive();
		view().finish();
	}

	@Override
	public void 滚动到顶部() {
		checkAlive();
		view().scrollToEdge(false);
	}

	@Override
	public void 滚动到底部() {
		checkAlive();
		view().scrollToEdge(true);
	}

	/* 容器接口 */

	@Override
	public void addComponent(组件 component) {
		checkAlive();

		if (component == content) {
			// 内容面板首次建布局或更换空布局时，只替换 ScrollView 的唯一内容视图。
			view().setContent(content.getView());
		} else {
			content.addComponent(component);
		}
	}

	@Override
	public 布局 getLayout() {
		checkAlive();
		return content.getLayout();
	}

	@Override
	public ViewGroup getLayoutManager() {
		checkAlive();
		return content.getLayoutManager();
	}

	/* 生命周期与访问检查 */

	@Override
	public void 销毁() {
		if (destroyed) {
			return;
		}

		checkAlive();
		destroyed = true;
		view().dispose();
		content.销毁();
		super.销毁();
	}

	/**
	 * 取得本组件原生视图。
	 *
	 * @return 刷新视图。
	 */
	private PullRefreshView view() {
		return (PullRefreshView) getView();
	}

	/**
	 * 检查组件生命周期和调用线程。
	 *
	 * @throws 未初始化实例错误 组件已销毁。
	 * @throws IllegalArgumentException 未在主线程调用。
	 */
	private void checkAlive() {
		if (destroyed) {
			throw new 未初始化实例错误();
		}

		if (Looper.myLooper() != Looper.getMainLooper()) {
			throw new IllegalArgumentException("请在主线程操作拉动刷新框。");
		}
	}
}
