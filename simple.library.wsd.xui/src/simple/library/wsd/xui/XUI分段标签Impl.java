package simple.library.wsd.xui;

import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.viewpager.widget.ViewPager;

import com.xuexiang.xui.widget.tabbar.TabSegment;

import simple.runtime.components.impl.android.视图组件;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.索引超出界限错误;
import simple.runtime.errors.非法参数错误;
import simple.runtime.events.EventDispatcher;

/**
 * XUI分段标签的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI分段标签Impl extends XUI视图组件基类 implements XUI分段标签 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private TabSegment view;

	// 调用方提供的标题与适配器自动生成的标题分开处理，重绑时保留显式配置。
	private boolean titlesConfigured;

	/**
	 * 创建XUI分段标签。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI分段标签Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new TabSegment(ComponentSupport.createContext());
		titlesConfigured = false;

		view.addOnTabSelectedListener(new TabSegment.OnTabSelectedListener() {

			@Override
			public void onTabSelected(int index) {
				标签被选择(index);
			}

			@Override
			public void onTabUnselected(int index) {
				标签取消选择(index);
			}

			@Override
			public void onTabReselected(int index) {
				标签被重复选择(index);
			}

			@Override
			public void onDoubleTap(int index) {
				标签被双击(index);
			}
		});

		return view;
	}

	@Override
	public void 置标签文本尺寸(int tabTextSize) {
		view.setTabTextSize(tabTextSize);
	}

	@Override
	public void 重置() {
		view.reset();
		titlesConfigured = false;
	}

	@Override
	public void 置项目间距入场滚动模式(int itemSpaceInScrollMode) {
		view.setItemSpaceInScrollMode(itemSpaceInScrollMode);
	}

	@Override
	public void 置指示宽度调整内容(boolean indicatorWidthFollowContent) {
		view.setIndicatorWidthAdjustContent(indicatorWidthFollowContent);
	}

	@Override
	public void 置指示位置(boolean isIndicatorTop) {
		view.setIndicatorPosition(isIndicatorTop);
	}

	@Override
	public void 置含有指示(boolean hasIndicator) {
		view.setHasIndicator(hasIndicator);
	}

	@Override
	public int 取模式() {
		return view.getMode();
	}

	@Override
	public void 置模式(int mode) {
		view.setMode(mode);
	}

	@Override
	public void 置默认正常颜色(int defaultNormalColor) {
		view.setDefaultNormalColor(defaultNormalColor);
	}

	@Override
	public void 置默认选中颜色(int defaultSelectedColor) {
		view.setDefaultSelectedColor(defaultSelectedColor);
	}

	@Override
	public void 置默认标签图标位置(int defaultTabIconPosition) {
		view.setDefaultTabIconPosition(defaultTabIconPosition);
	}

	@Override
	public int 取选中索引() {
		return view.getSelectedIndex();
	}

	@Override
	public int 取标记数量(int index) {
		return view.getSignCount(index);
	}

	@Override
	public void 置标签数组(String[] items) {
		String[] data = ComponentSupport.copyTextArray(items);
		view.reset();
		for (String title : data) {
			view.addTab(new TabSegment.Tab(title));
		}
		titlesConfigured = data.length > 0;
		view.notifyDataChanged();
		ThemeSupport.applyFont(view);
	}

	@Override
	public void 选择标签(int index) {
		if (index < 0) {
			throw new 索引超出界限错误();
		}
		try {
			if (view.getTab(index) == null) {
				throw new 索引超出界限错误();
			}
		} catch (IndexOutOfBoundsException exception) {
			throw new 索引超出界限错误();
		}
		view.selectTab(index);
	}

	@Override
	public void 绑定页面框(可视组件 pager) {
		View target = ((视图组件) pager).getView();
		if (!(target instanceof ViewPager)) {
			throw new 非法参数错误();
		}
		// 已配置标签时沿用调用方的标题；空标签才从页面适配器读取标题。
		view.setupWithViewPager((ViewPager) target, !titlesConfigured);
	}

	@Override
	public void 置指示图像(String image) {
		view.setIndicatorDrawable(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 标签被选择(int index) {
		EventDispatcher.dispatchEvent(this, "标签被选择", index);
	}

	/**
	 * 将上游的越界结果统一转换为 Simple 索引错误。
	 *
	 * @param index 从零开始的标签位置。
	 * @return 已存在的标签模型。
	 */
	private TabSegment.Tab requireTab(int index) {
		if (index < 0) {
			throw new 索引超出界限错误();
		}
		try {
			TabSegment.Tab tab = view.getTab(index);
			if (tab == null) {
				throw new 索引超出界限错误();
			}
			return tab;
		} catch (IndexOutOfBoundsException failure) {
			throw new 索引超出界限错误();
		}
	}

	@Override
	public void 置标签文本(int index, String text) {
		requireTab(index);
		if (text == null) {
			throw new 非法参数错误();
		}
		view.updateTabText(index, text);
	}

	@Override
	public void 置标签图标(int index, String normal, String selected) {
		TabSegment.Tab previous = requireTab(index);
		Drawable normalIcon = ComponentSupport.getDrawable(normal);
		Drawable selectedIcon = ComponentSupport.getDrawable(selected);
		TabSegment.Tab replacement = new TabSegment.Tab(normalIcon, selectedIcon, previous.getText(), false);
		replacement.setTextColor(previous.getNormalColor(), previous.getSelectedColor());
		replacement.setIconPosition(previous.getIconPosition());
		replacement.setTextSize(previous.getTextSize());
		replacement.setGravity(previous.getGravity());
		int count = previous.getSignCount();
		view.replaceTab(index, replacement);
		if (count > 0) {
			view.showSignCountView(view.getContext(), index, count);
		}
	}

	@Override
	public void 置标签徽标(int index, int count) {
		requireTab(index);
		if (count < 0) {
			throw new 非法参数错误();
		}
		if (count == 0) {
			view.hideSignCountView(index);
		} else {
			view.showSignCountView(view.getContext(), index, count);
		}
	}

	@Override
	public void 标签取消选择(int index) {
		EventDispatcher.dispatchEvent(this, "标签取消选择", index);
	}

	@Override
	public void 标签被重复选择(int index) {
		EventDispatcher.dispatchEvent(this, "标签被重复选择", index);
	}

	@Override
	public void 标签被双击(int index) {
		EventDispatcher.dispatchEvent(this, "标签被双击", index);
	}

}
