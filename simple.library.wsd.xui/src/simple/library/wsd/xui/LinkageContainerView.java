package simple.library.wsd.xui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.xuexiang.xui.widget.layout.linkage.ILinkageScroll;
import com.xuexiang.xui.widget.layout.linkage.LinkageScrollLayout;

/**
 * 在动态添加 Simple 子组件后连接 XUI 联动处理器，并检查子项的必要协议。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class LinkageContainerView extends LinkageScrollLayout {

	/**
	 * 创建联动滚动容器。
	 *
	 * @param context 使用 XUI 主题的上下文。
	 */
	LinkageContainerView(Context context) {
		super(context);
	}

	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams parameters) {
		if (!(child instanceof ILinkageScroll)) {
			throw new IllegalArgumentException("联动滚动容器只接受联动组件，普通子组件应放入联动线性容器。");
		}
		super.addView(child, index, parameters);

		// Simple 在构造后动态添加子项，补齐原生控件在 XML 结束阶段建立的关联。
		super.onFinishInflate();
	}
}
