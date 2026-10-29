package simple.library.wsd.xui;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.widget.TextView;

import com.xuexiang.xui.widget.actionbar.TitleBar;

import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;
import simple.runtime.events.EventDispatcher;

/**
 * XUI标题栏的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI标题栏Impl extends XUI视图组件基类 implements XUI标题栏 {

	private List<View> actions;
	private BorrowedContent customTitle;

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private TitleBar view;

	/**
	 * 创建XUI标题栏。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI标题栏Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new TitleBar(ComponentSupport.createContext());
		actions = new ArrayList<View>();

		view.setLeftClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View clicked) {
				左侧被单击();
			}
		});

		view.setCenterClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View clicked) {
				标题被单击();
			}
		});

		return view;
	}

	@Override
	public void 置沉浸(boolean immersive) {
		view.setImmersive(immersive);
	}

	@Override
	public void 置左侧文本粗体(boolean isBold) {
		view.setLeftTextBold(isBold);
	}

	@Override
	public void 置中间文本粗体(boolean isBold) {
		view.setCenterTextBold(isBold);
	}

	@Override
	public void 置左侧文本(String title) {
		view.setLeftText(title);
	}

	@Override
	public void 置左侧文本资源(int resId) {
		view.setLeftText(resId);
	}

	@Override
	public void 置左侧文本尺寸(float size) {
		view.setLeftTextSize(size);
	}

	@Override
	public void 置左侧文本最大字宽(int maxEms) {
		view.setLeftTextMaxEms(maxEms);
	}

	@Override
	public void 置左侧文本最大宽度(int maxPixels) {
		view.setLeftTextMaxWidth(maxPixels);
	}

	@Override
	public void 置左侧文本内边距(int paddingStart, int paddingEnd) {
		view.setLeftTextPadding(paddingStart, paddingEnd);
	}

	@Override
	public void 置左侧文本颜色(int color) {
		view.setLeftTextColor(color);
	}

	@Override
	public void 置左侧可见(boolean visible) {
		view.setLeftVisible(visible);
	}

	@Override
	public void 置标题(String title) {
		view.setTitle(title);
	}

	@Override
	public void 置标题(String title, String subTitle, int orientation) {
		view.setTitle(title, subTitle, orientation);
	}

	@Override
	public void 置副标题(String subTitle, int orientation) {
		view.setSubTitle(subTitle, orientation);
	}

	@Override
	public void 置副标题(String subTitle) {
		view.setSubTitle(subTitle);
	}

	@Override
	public void 置中间对齐(int gravity) {
		view.setCenterGravity(gravity);
	}

	@Override
	public void 置标题资源(int resId) {
		view.setTitle(resId);
	}

	@Override
	public void 置标题颜色(int resId) {
		view.setTitleColor(resId);
	}

	@Override
	public void 置标题尺寸(float size) {
		view.setTitleSize(size);
	}

	@Override
	public void 置标题背景(int resId) {
		view.setTitleBackground(resId);
	}

	@Override
	public void 置副标题颜色(int resId) {
		view.setSubTitleColor(resId);
	}

	@Override
	public void 置副标题尺寸(float size) {
		view.setSubTitleSize(size);
	}

	@Override
	public void 置分隔线颜色(int color) {
		view.setDividerColor(color);
	}

	@Override
	public void 置分隔线高度(int dividerHeight) {
		view.setDividerHeight(dividerHeight);
	}

	@Override
	public void 置操作文本颜色(int colorResId) {
		view.setActionTextColor(colorResId);
	}

	@Override
	public void 清空操作项() {
		view.removeAllActions();
		actions.clear();
	}

	@Override
	public int 取操作数量() {
		return view.getActionCount();
	}

	@Override
	public int 添加操作(String text) {
		final int index = view.getActionCount();
		if (text == null || text.isEmpty()) {
			throw new 非法参数错误();
		}
		View action = view.addAction(new TitleBar.TextAction(text) {
			@Override
			public void performAction(View clicked) {
				int current = actions.indexOf(clicked);
				if (current >= 0) {
					操作被单击(current);
				}
			}
		});
		ThemeSupport.applyFont(action);
		actions.add(action);
		return index;
	}

	@Override
	public void 置左侧图片图像(String image) {
		view.setLeftImageDrawable(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置分隔线(String image) {
		view.setDivider(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 左侧被单击() {
		EventDispatcher.dispatchEvent(this, "左侧被单击");
	}

	@Override
	public void 标题被单击() {
		EventDispatcher.dispatchEvent(this, "标题被单击");
	}

	@Override
	public void 操作被单击(int index) {
		EventDispatcher.dispatchEvent(this, "操作被单击", index);
	}

	@Override
	public void 移除操作(int index) {
		ComponentSupport.validateIndex(index, actions.size());
		view.removeActionAt(index);
		actions.remove(index);
	}

	@Override
	public void 置操作文本(int index, String text) {
		ComponentSupport.validateIndex(index, actions.size());
		if (text == null || text.isEmpty()) {
			throw new 非法参数错误();
		}
		((TextView) actions.get(index)).setText(text);
	}

	@Override
	public void 置自定义标题(可视组件 component) {
		View target = ComponentOwnership.requireSibling(view, component);
		清除自定义标题();
		BorrowedContent borrowed = new BorrowedContent(target, view.getRootView());
		try {
			view.setCustomTitle(target);
			customTitle = borrowed;
		} catch (RuntimeException failure) {
			borrowed.restore();
			throw failure;
		}
	}

	@Override
	public void 清除自定义标题() {
		if (customTitle != null) {
			view.setCustomTitle(null);
			BorrowedContent borrowed = customTitle;
			customTitle = null;
			borrowed.restore();
		}
	}

	@Override
	public void 销毁() {
		清除自定义标题();
		super.销毁();
	}

}
