package simple.library.wsd.xui;

import android.content.res.ColorStateList;
import android.content.res.TypedArray;

import android.view.View;

import com.xuexiang.xui.widget.textview.LoggerTextView;

import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * XUI日志文本的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI日志文本Impl extends XUI文本组件基类 implements XUI日志文本 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private LoggerTextView view;

	/**
	 * 创建XUI日志文本。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI日志文本Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new LoggerTextView(ComponentSupport.createContext()) {
			@Override
			protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
				super.onLayout(changed, left, top, right, bottom);
				// 上游可能在首次布局前按旧高度滚动；布局后限制范围，避免首行被错误移出。
				int contentHeight = getLayout() == null ? 0 : getLayout().getHeight();
				int maximum = Math.max(0, contentHeight + getCompoundPaddingTop()
						+ getCompoundPaddingBottom() - getHeight());
				scrollTo(getScrollX(), Math.min(Math.max(0, getScrollY()), maximum));
			}
		};
		// 上游日志样式以深色控制台为背景；嵌入普通窗口时按当前组件主题使用主要文字色。
		TypedArray colors = view.getContext().obtainStyledAttributes(new int[] {android.R.attr.textColorPrimary});
		try {
			ColorStateList text = colors.getColorStateList(0);
			view.setTextColor(text == null ? ColorStateList.valueOf(0xFF333333) : text);
		} finally {
			colors.recycle();
		}

		return view;
	}

	@Override
	public void 清空日志() {
		view.clearLog();
	}

	@Override
	public void 追加日志(String text, int level) {
		if (text == null || level < 0 || level > 4) {
			throw new 非法参数错误();
		}
		switch (level) {
		case 0:
			view.logNormal(text);
			break;
		case 1:
			view.logSuccess(text);
			break;
		case 2:
			view.logWarning(text);
			break;
		case 3:
			view.logError(text);
			break;
		default:
			view.logCustom(text);
			break;
		}
	}

}
