package simple.library.wsd.xui;

import android.view.View;
import android.widget.CompoundButton;

import com.xuexiang.xui.utils.ThemeUtils;
import com.xuexiang.xui.widget.grouplist.XUICommonListItemView;

import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;
import simple.runtime.events.EventDispatcher;

/**
 * XUI分组列表项的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI分组列表项Impl extends XUI视图组件基类 implements XUI分组列表项 {

	// 同一列表项只能被一个分组暂借，销毁时先解除该归属。
	XUI分组列表Impl groupOwner;

	private BorrowedContent accessoryContent;
	private boolean silentSwitch;

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private XUICommonListItemView view;

	/**
	 * 创建XUI分组列表项。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI分组列表项Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new XUICommonListItemView(ComponentSupport.createContext());
		// 原生分组工厂会指定标准行高；独立配置行以最小高度保留同样的触摸区域。
		view.setMinimumHeight(ThemeUtils.resolveDimension(view.getContext(), com.xuexiang.xui.R.attr.xui_list_item_height));

		return view;
	}

	@Override
	public void 置红点位置(int redDotPosition) {
		view.setRedDotPosition(redDotPosition);
	}

	@Override
	public String 取详情文本() {
		return String.valueOf(view.getDetailText());
	}

	@Override
	public void 置详情文本(String text) {
		view.setDetailText(text);
	}

	@Override
	public int 取附属类型() {
		return view.getAccessoryType();
	}

	@Override
	public void 置附属类型(int type) {
		if (type < 0 || type > 3) {
			throw new 非法参数错误();
		}
		restoreAccessory();
		view.setAccessoryType(type);
		if (type == 2) {
			view.getSwitch().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton button, boolean checked) {
					if (!silentSwitch) {
						开关改变(checked);
					}
				}
			});
		}
	}

	@Override
	public void 置文本(String text) {
		view.setText(text);
	}

	@Override
	public String 取文本() {
		return String.valueOf(view.getText());
	}

	/**
	 * 归还自定义附属内容，切换类型和销毁共用此边界。
	 */
	private void restoreAccessory() {
		if (accessoryContent != null) {
			accessoryContent.restore();
			accessoryContent = null;
		}
	}

	@Override
	public void 销毁() {
		if (groupOwner != null) {
			groupOwner.releaseGroupedItem(this);
		}
		restoreAccessory();
		super.销毁();
	}

	@Override
	public void 置图标(String image) {
		view.setImageDrawable(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 显示红点(boolean show) {
		view.showRedDot(show);
	}

	@Override
	public void 显示新标记(boolean show) {
		view.showNewTip(show);
	}

	@Override
	public void 置开关选中(boolean checked, boolean silent) {
		if (view.getAccessoryType() != 2) {
			throw new ComponentStateException("请先将附属类型设为开关。");
		}
		silentSwitch = silent;
		try {
			view.getSwitch().setChecked(checked);
		} finally {
			silentSwitch = false;
		}
	}

	@Override
	public boolean 取开关选中() {
		if (view.getAccessoryType() != 2) {
			throw new ComponentStateException("请先将附属类型设为开关。");
		}
		return view.getSwitch().isChecked();
	}

	@Override
	public void 开关改变(boolean checked) {
		EventDispatcher.dispatchEvent(this, "开关改变", checked);
	}

	@Override
	public void 置自定义附属(可视组件 component) {
		View target = ComponentOwnership.requireSibling(getView(), component);
		restoreAccessory();
		BorrowedContent borrowed = new BorrowedContent(target, getView().getRootView());
		try {
			view.setAccessoryType(3);
			view.addAccessoryCustomView(target);
			accessoryContent = borrowed;
		} catch (RuntimeException failure) {
			borrowed.restore();
			throw failure;
		}
	}

}
