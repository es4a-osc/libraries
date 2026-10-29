package simple.library.wsd.xui;

import java.util.Arrays;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.xuexiang.xui.widget.dialog.materialdialog.DialogAction;
import com.xuexiang.xui.widget.dialog.materialdialog.GravityEnum;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xui.widget.dialog.materialdialog.StackingBehavior;

import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;
import simple.runtime.parameters.BooleanReferenceParameter;

/**
 * 材质对话框的 Simple 适配，集中处理确认校验、借用内容和每次显示的独立状态。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI对话框Impl extends XUI弹层基类 implements XUI对话框 {

	private Drawable icon;
	private Integer titleTextColor;
	private Integer contentTextColor;
	private Integer itemTextColor;
	private Integer dialogBackground;
	private int titleGravity;
	private int contentGravity;
	private int itemGravity;
	private int buttonStacking;
	private int[] buttonColors;

	private String neutralText = "";
	private String checkPrompt = "";
	private boolean checkInitially;
	private int[] disabledItems = new int[0];

	private Session session;
	private boolean backCancelable = true;
	private boolean outsideCancelable = true;
	private Integer inputType;
	private int inputMinimum;
	private int inputMaximum = -1;

	/**
	 * 单次对话框的数据与归属，不通过全局模式字段读取旧实例的回调。
	 */
	private static final class Session {
		MaterialDialog.Builder builder;
		MaterialDialog dialog;
		BorrowedContent content;
		int progressMaximum;
		int selectionMode;
		int itemCount;
		boolean[] actions;
		boolean closed;
	}

	/**
	 * 创建对话框组件。
	 *
	 * @param container 所属 Simple 容器。
	 */
	public XUI对话框Impl(组件容器 container) {
		super(container);
	}

	@Override
	public void 置取消方式(boolean back, boolean outside) {
		backCancelable = back;
		outsideCancelable = outside;
	}

	@Override
	public void 置输入类型(int type) {
		inputType = type;
	}

	@Override
	public void 置输入长度(int minimum, int maximum) {
		if (minimum < 0 || maximum < -1 || (maximum != -1 && maximum < minimum)) {
			throw new 非法参数错误();
		}
		inputMinimum = minimum;
		inputMaximum = maximum;
	}

	@Override
	public void 置输入错误(String error) {
		if (session == null || session.dialog.getInputEditText() == null) {
			throw new ComponentStateException("当前没有输入对话框。");
		}
		session.dialog.getInputEditText().setError(error == null || error.isEmpty() ? null : error);
	}

	/**
	 * 替换旧实例后准备本次构建器。关闭事件若已显示新界面，保留该界面并拒绝覆盖。
	 *
	 * @param title 本次标题。
	 * @return 本次独立会话。
	 */
	private Session createSession(String title) {
		requireVisibleOwner();
		关闭();
		requireVisibleOwner();
		if (session != null) {
			throw new ComponentStateException("关闭事件已显示新的对话框，请勿重复覆盖。");
		}
		final Session current = new Session();
		final boolean allowBack = backCancelable;
		current.builder = new MaterialDialog.Builder(ComponentSupport.createContext()).title(title)
			.cancelable(backCancelable || outsideCancelable).canceledOnTouchOutside(outsideCancelable).autoDismiss(false)
			.keyListener(new DialogInterface.OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface source, int keyCode, KeyEvent event) {
					// Android 开启外部点击取消会同时启用返回键，必须单独限制该按键。
					return !allowBack && keyCode == KeyEvent.KEYCODE_BACK;
				}
			})
			.onNegative(new MaterialDialog.SingleButtonCallback() {
				@Override
				public void onClick(MaterialDialog source, DialogAction which) {
					if (!current.closed) {
						restoreContent(current);
						source.cancel();
					}
				}
			}).cancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface source) {
					if (!current.closed) {
						restoreContent(current);
						已取消();
					}
				}
			}).dismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface source) {
					if (current.closed) {
						return;
					}
					current.closed = true;
					if (session == current) {
						session = null;
					}
					// 先恢复内容，再允许用户在关闭事件中操作或重新显示它。
					restoreContent(current);
					已关闭();
				}
			});
		if (!neutralText.isEmpty()) {
			current.builder.neutralText(neutralText).onNeutral(new MaterialDialog.SingleButtonCallback() {
				@Override
				public void onClick(MaterialDialog source, DialogAction which) {
					if (session == current && !current.closed) {
						try {
							中立按钮被单击();
						} finally {
							restoreContent(current);
							source.dismiss();
						}
					}
				}
			});
		}
		if (!checkPrompt.isEmpty()) {
			current.builder.checkBoxPrompt(checkPrompt, checkInitially, null);
		}
		Integer[] disabled = new Integer[disabledItems.length];
		for (int index = 0; index < disabled.length; index++) {
			disabled[index] = disabledItems[index];
		}
		current.builder.itemsDisabledIndices(disabled);
		current.builder.icon(icon).titleGravity(toGravity(titleGravity)).contentGravity(toGravity(contentGravity))
			.itemsGravity(toGravity(itemGravity)).stackingBehavior(StackingBehavior.values()[buttonStacking]);
		if (titleTextColor != null) {
			current.builder.titleColor(titleTextColor).contentColor(contentTextColor).itemsColor(itemTextColor);
		}
		if (dialogBackground != null) {
			current.builder.backgroundColor(dialogBackground);
		}
		if (buttonColors != null) {
			current.builder.positiveColor(buttonColors[0]).negativeColor(buttonColors[1]).neutralColor(buttonColors[2]);
		}
		current.builder.itemsLongCallback(new MaterialDialog.ListLongCallback() {
			@Override
			public boolean onLongSelection(MaterialDialog source, View row, int index, CharSequence text) {
				if (session == current && !current.closed) {
					项目被长按(index, text.toString());
				}
				return true;
			}
		});
		return current;
	}

	/**
	 * 先释放本次内容引用，再恢复原页面，允许后续关闭事件立即再次借用它。
	 *
	 * @param current 正在退出的会话。
	 */
	private void restoreContent(Session current) {
		BorrowedContent previous = current.content;
		current.content = null;
		if (previous != null) {
			previous.restore();
		}
	}

	/**
	 * 构建和显示失败时回滚借用内容，不能留下移出原页面的组件。
	 *
	 * @param current 完成配置的会话。
	 */
	private void showSession(Session current) {
		try {
			current.dialog = current.builder.build();
			ThemeSupport.applyFont(current.dialog.getView());
			// 上游构建末尾会重新显示全部按钮，按实际文字恢复空按钮的隐藏状态。
			current.actions = new boolean[DialogAction.values().length];
			for (DialogAction action : DialogAction.values()) {
				current.actions[action.ordinal()] = current.dialog.getActionButton(action).getText().length() > 0;
				if (current.dialog.getActionButton(action).getText().length() == 0) {
					current.dialog.getActionButton(action).setVisibility(View.GONE);
				}
			}
			session = current;
			current.dialog.show();
		} catch (RuntimeException failure) {
			if (session == current) {
				session = null;
			}
			current.closed = true;
			if (current.dialog != null) {
				current.dialog.dismiss();
			}
			if (current.content != null) {
				current.content.restore();
				current.content = null;
			}
			throw failure;
		}
	}

	/**
	 * 确认前向 Simple 同步询问；回调关闭或替换界面后不再提交旧会话。
	 *
	 * @param current 事件所属会话。
	 * @param text 输入文字，其它模式为空。
	 * @return 是否允许提交。
	 */
	private boolean canConfirm(Session current, String text) {
		if (session != current || current.closed || !current.dialog.isShowing()) {
			return false;
		}
		BooleanReferenceParameter cancel = new BooleanReferenceParameter(false);
		确认前校验(text, cancel);
		return !cancel.get() && session == current && !current.closed;
	}

	@Override
	public void 显示提示(String title, String message, String positive, String negative) {
		if (title == null || message == null || positive == null || negative == null) {
			throw new 非法参数错误();
		}
		final Session current = createSession(title);
		if (!negative.isEmpty()) {
			current.builder.negativeText(negative);
		}
		current.builder.content(message).positiveText(positive)
			.onPositive(new MaterialDialog.SingleButtonCallback() {
				@Override
				public void onClick(MaterialDialog source, DialogAction which) {
					if (canConfirm(current, "")) {
						try {
							按钮被单击(true);
						} finally {
							restoreContent(current);
							source.dismiss();
						}
					}
				}
			}).onNegative(new MaterialDialog.SingleButtonCallback() {
				@Override
				public void onClick(MaterialDialog source, DialogAction which) {
					try {
						按钮被单击(false);
					} finally {
						source.cancel();
					}
				}
			});
		showSession(current);
	}

	@Override
	public void 显示输入(String title, String hint, String value, boolean password) {
		final Session current = createSession(title);
		int type = inputType == null ? InputType.TYPE_CLASS_TEXT : inputType;
		if (password) {
			type = (type & InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_NUMBER
				? InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD
				: InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
		}
		current.builder.positiveText("确定").negativeText("取消").inputType(type)
			.inputRange(inputMinimum, inputMaximum).input(hint, value, new MaterialDialog.InputCallback() {
				@Override
				public void onInput(MaterialDialog source, CharSequence text) {
					// 原生输入回调在按钮后触发；提交由下方确认处理统一校验和分发。
				}
			}).onPositive(new MaterialDialog.SingleButtonCallback() {
				@Override
				public void onClick(MaterialDialog source, DialogAction which) {
					String text = source.getInputEditText().getText().toString();
					if (canConfirm(current, text)) {
						try {
							输入已确认(text);
						} finally {
							restoreContent(current);
							source.dismiss();
						}
					}
				}
			});
		showSession(current);
	}

	@Override
	public void 显示自定义(String title, 可视组件 content, String positive, String negative) {
		if (title == null || positive == null || negative == null) {
			throw new 非法参数错误();
		}
		View target = getNativeView(content);
		if (session == null || session.content == null || session.content.view != target) {
			requireContent(content);
		}
		final Session current = createSession(title);
		target = requireContent(content);
		current.content = new BorrowedContent(target, requireVisibleOwner());
		if (!negative.isEmpty()) {
			current.builder.negativeText(negative);
		}
		current.builder.customView(target, false).positiveText(positive)
			.onPositive(new MaterialDialog.SingleButtonCallback() {
				@Override
				public void onClick(MaterialDialog source, DialogAction which) {
					if (canConfirm(current, "")) {
						try {
							按钮被单击(true);
						} finally {
							restoreContent(current);
							source.dismiss();
						}
					}
				}
			});
		showSession(current);
	}

	/**
	 * 自定义内容可以预先隐藏，但必须仍属于当前窗口，不能借出窗口本身。
	 *
	 * @param component 借用的 Simple 组件。
	 * @return 原生内容视图。
	 */
	private View requireContent(可视组件 component) {
		View owner = requireVisibleOwner();
		View target = getNativeView(component);
		View parent = target;
		while (parent != null && parent != owner) {
			parent = parent.getParent() instanceof View ? (View) parent.getParent() : null;
		}
		if (target == owner || parent != owner) {
			throw new 非法参数错误();
		}
		return target;
	}

	@Override
	public void 显示列表(String title, String[] items) {
		final String[] data = ComponentSupport.copyTextArray(items);
		for (int index : disabledItems) {
			ComponentSupport.validateIndex(index, data.length);
		}
		final Session current = createSession(title);
		current.builder.items(data).itemsCallback(new MaterialDialog.ListCallback() {
			@Override
			public void onSelection(MaterialDialog source, View item, int index, CharSequence text) {
				if (canConfirm(current, "")) {
					try {
						项目已选择(index, data[index]);
					} finally {
						source.dismiss();
					}
				}
			}
		});
		showSession(current);
	}

	@Override
	public void 显示单选(String title, String[] items, int selected) {
		final String[] data = ComponentSupport.copyTextArray(items);
		for (int index : disabledItems) {
			ComponentSupport.validateIndex(index, data.length);
		}
		if (selected != -1) {
			ComponentSupport.validateIndex(selected, data.length);
		}
		final Session current = createSession(title);
		current.selectionMode = 1;
		current.itemCount = data.length;
		current.builder.items(data).positiveText("确定").negativeText("取消")
			.itemsCallbackSingleChoice(selected, new MaterialDialog.ListCallbackSingleChoice() {
				@Override
				public boolean onSelection(MaterialDialog source, View item, int index, CharSequence text) {
					if (index < 0 || !canConfirm(current, "")) {
						return false;
					}
					try {
						项目已选择(index, data[index]);
					} finally {
						source.dismiss();
					}
					return true;
				}
			});
		showSession(current);
	}

	@Override
	public void 显示多选(String title, String[] items, int[] selected) {
		final String[] data = ComponentSupport.copyTextArray(items);
		for (int index : disabledItems) {
			ComponentSupport.validateIndex(index, data.length);
		}
		if (selected == null) {
			throw new 非法参数错误();
		}
		Integer[] indices = new Integer[selected.length];
		for (int i = 0; i < selected.length; i++) {
			ComponentSupport.validateIndex(selected[i], data.length);
			indices[i] = selected[i];
		}
		final Session current = createSession(title);
		current.selectionMode = 2;
		current.itemCount = data.length;
		current.builder.items(data).positiveText("确定").negativeText("取消")
			.itemsCallbackMultiChoice(indices, new MaterialDialog.ListCallbackMultiChoice() {
				@Override
				public boolean onSelection(MaterialDialog source, Integer[] which, CharSequence[] texts) {
					if (!canConfirm(current, "")) {
						return false;
					}
					int[] values = new int[which.length];
					for (int i = 0; i < which.length; i++) {
						values[i] = which[i];
					}
					try {
						多选已确认(values);
					} finally {
						source.dismiss();
					}
					return true;
				}
			});
		showSession(current);
	}

	@Override
	public void 显示进度(String title, String message, int maximum, boolean indeterminate) {
		if (maximum <= 0) {
			throw new 非法参数错误();
		}
		Session current = createSession(title);
		current.builder.content(message).progress(indeterminate, maximum);
		current.progressMaximum = indeterminate ? 0 : maximum;
		showSession(current);
	}

	@Override
	public void 置进度(int value) {
		if (!是否显示() || session.progressMaximum <= 0 || value < 0 || value > session.progressMaximum) {
			throw new 非法参数错误();
		}
		session.dialog.setProgress(value);
	}

	@Override
	public boolean 是否显示() {
		return session != null && session.dialog.isShowing();
	}

	@Override
	public void 关闭() {
		Session previous = session;
		session = null;
		if (previous != null && !previous.closed) {
			restoreContent(previous);
			previous.dialog.dismiss();
		}
	}

	@Override
	public void 确认前校验(String text, BooleanReferenceParameter cancel) {
		dispatchEvent("确认前校验", text, cancel);
	}

	@Override
	public void 已取消() {
		dispatchEvent("已取消");
	}

	@Override
	public void 按钮被单击(boolean confirmed) {
		dispatchEvent("按钮被单击", confirmed);
	}

	@Override
	public void 输入已确认(String text) {
		dispatchEvent("输入已确认", text);
	}

	@Override
	public void 项目已选择(int index, String text) {
		dispatchEvent("项目已选择", index, text);
	}

	@Override
	public void 多选已确认(int[] indices) {
		dispatchEvent("多选已确认", indices);
	}

	@Override
	public void 已关闭() {
		dispatchEvent("已关闭");
	}

	@Override
	public void 置中立按钮(String text) {
		if (text == null) {
			throw new 非法参数错误();
		}
		neutralText = text;
	}

	@Override
	public void 中立按钮被单击() {
		dispatchEvent("中立按钮被单击");
	}

	@Override
	public void 置复选提示(String text, boolean checked) {
		if (text == null) {
			throw new 非法参数错误();
		}
		checkPrompt = text;
		checkInitially = checked;
	}

	@Override
	public boolean 取复选提示选中() {
		return requireDialog().isPromptCheckBoxChecked();
	}

	@Override
	public void 置禁用项目(int[] indices) {
		if (indices == null) {
			throw new 非法参数错误();
		}
		int[] copy = indices.clone();
		for (int index : copy) {
			if (index < 0) {
				throw new 非法参数错误();
			}
		}
		disabledItems = copy;
	}

	@Override
	public int 取单选位置() {
		return requireDialog().getSelectedIndex();
	}

	@Override
	public int[] 取多选位置数组() {
		Integer[] selected = requireDialog().getSelectedIndices();
		if (selected == null) {
			return new int[0];
		}
		int[] result = new int[selected.length];
		for (int index = 0; index < result.length; index++) {
			result[index] = selected[index];
		}
		return result;
	}

	/**
	 * 只允许读取仍在显示的会话，拒绝把关闭后的旧状态当作当前选择。
	 *
	 * @return 正在显示的原生对话框。
	 */
	private MaterialDialog requireDialog() {
		if (session == null || session.closed || !session.dialog.isShowing()) {
			throw new ComponentStateException("当前没有显示的对话框。");
		}
		return session.dialog;
	}

	@Override
	public void 置图标(String image) {
		Drawable replacement = ComponentSupport.getDrawable(image);
		icon = replacement;
		if (是否显示()) {
			session.dialog.setIcon(replacement);
			session.dialog.getIconView().setVisibility(replacement == null ? View.GONE : View.VISIBLE);
		}
	}

	@Override
	public void 置文字颜色(int title, int content, int items) {
		titleTextColor = title;
		contentTextColor = content;
		itemTextColor = items;
	}

	@Override
	public void 置文字对齐(int title, int content, int items) {
		toGravity(title);
		toGravity(content);
		toGravity(items);
		titleGravity = title;
		contentGravity = content;
		itemGravity = items;
	}

	@Override
	public void 置按钮颜色(int positive, int negative, int neutral) {
		buttonColors = new int[] {positive, negative, neutral};
	}

	@Override
	public void 置背景颜色(int color) {
		dialogBackground = color;
	}

	@Override
	public void 置按钮排列(int mode) {
		if (mode < 0 || mode > 2) {
			throw new 非法参数错误();
		}
		buttonStacking = mode;
	}

	@Override
	public void 更新标题(String title) {
		requireDialog().setTitle(requireText(title));
	}

	@Override
	public void 更新内容(String content) {
		MaterialDialog current = requireDialog();
		if (current.getContentView() == null) {
			throw new ComponentStateException("当前对话框没有正文区域。");
		}
		current.setContent(requireText(content));
	}

	@Override
	public void 更新输入(String text) {
		MaterialDialog current = requireDialog();
		if (current.getInputEditText() == null) {
			throw new ComponentStateException("当前不是输入对话框。");
		}
		current.getInputEditText().setText(requireText(text));
		current.getInputEditText().setSelection(text.length());
	}

	@Override
	public void 更新按钮文本(int action, String text) {
		MaterialDialog current = requireDialog();
		DialogAction which = toAction(action);
		if (!session.actions[which.ordinal()]) {
			throw new ComponentStateException("本次对话框没有配置该按钮。");
		}
		current.setActionButton(which, requireText(text));
		current.getActionButton(which).setVisibility(text.isEmpty() ? View.GONE : View.VISIBLE);
	}

	@Override
	public void 置按钮启用(int action, boolean enabled) {
		MaterialDialog current = requireDialog();
		DialogAction which = toAction(action);
		if (!session.actions[which.ordinal()]) {
			throw new ComponentStateException("本次对话框没有配置该按钮。");
		}
		current.getActionButton(which).setEnabled(enabled);
	}

	@Override
	public void 置单选位置(int index) {
		MaterialDialog current = requireDialog();
		if (session.selectionMode != 1) {
			throw new ComponentStateException("当前不是单选对话框。");
		}
		if (index != -1) {
			ComponentSupport.validateIndex(index, session.itemCount);
		}
		current.setSelectedIndex(index);
	}

	@Override
	public void 置多选位置数组(int[] indices) {
		MaterialDialog current = requireDialog();
		if (session.selectionMode != 2 || indices == null) {
			throw new ComponentStateException("请在多选对话框中传入有效数组。");
		}
		Integer[] copy = new Integer[indices.length];
		for (int index = 0; index < indices.length; index++) {
			ComponentSupport.validateIndex(indices[index], session.itemCount);
			copy[index] = indices[index];
		}
		current.setSelectedIndices(copy);
	}

	@Override
	public void 项目被长按(int index, String text) {
		dispatchEvent("项目被长按", index, text);
	}

	@Override
	public void 显示图标列表(String title, String[] items, String[] images) {
		String[] texts = ComponentSupport.copyTextArray(items);
		String[] icons = ComponentSupport.copyTextArray(images);
		if (texts.length != icons.length) {
			throw new 非法参数错误();
		}
		Drawable[] pictures = new Drawable[icons.length];
		boolean[] enabled = new boolean[texts.length];
		Arrays.fill(enabled, true);
		for (int index = 0; index < icons.length; index++) {
			pictures[index] = ComponentSupport.getDrawable(icons[index]);
		}
		for (int index : disabledItems) {
			ComponentSupport.validateIndex(index, texts.length);
			enabled[index] = false;
		}
		Session current = createSession(title);
		current.builder.adapter(new IconListAdapter(current, texts, pictures, enabled), null);
		showSession(current);
	}

	/**
	 * 将公共稳定常量映射到原生对齐，不泄漏枚举对象。
	 */
	private static GravityEnum toGravity(int value) {
		switch (value) {
		case 0:
			return GravityEnum.START;
		case 1:
			return GravityEnum.CENTER;
		case 2:
			return GravityEnum.END;
		default:
			throw new 非法参数错误();
		}
	}

	/**
	 * 公共按钮编号不依赖上游枚举的声明顺序。
	 */
	private static DialogAction toAction(int value) {
		switch (value) {
		case 0:
			return DialogAction.POSITIVE;
		case 1:
			return DialogAction.NEGATIVE;
		case 2:
			return DialogAction.NEUTRAL;
		default:
			throw new 非法参数错误();
		}
	}

	/**
	 * 接口允许空文本，但不得向原生文字视图传入空引用。
	 */
	private static String requireText(String text) {
		if (text == null) {
			throw new 非法参数错误();
		}
		return text;
	}

	/**
	 * 复用上游图标列表布局，保留本库的先校验后关闭约定。
	 * 上游 SimpleListAdapter 在回调前直接关闭，不适合确认前校验。
	 */
	private final class IconListAdapter extends RecyclerView.Adapter<IconHolder> {
		private final Session owner;
		private final String[] texts;
		private final Drawable[] icons;
		private final boolean[] enabled;
		private final int gravity;

		IconListAdapter(Session owner, String[] texts, Drawable[] icons, boolean[] enabled) {
			this.owner = owner;
			this.texts = texts;
			this.icons = icons;
			this.enabled = enabled;
			gravity = toGravity(itemGravity).getGravityInt();
		}

		@Override
		public IconHolder onCreateViewHolder(ViewGroup parent, int type) {
			return new IconHolder(LayoutInflater.from(parent.getContext())
				.inflate(com.xuexiang.xui.R.layout.xmd_layout_simplelist_item, parent, false));
		}

		@Override
		public void onBindViewHolder(IconHolder holder, final int index) {
			holder.title.setText(texts[index]);
			holder.title.setTextColor(owner.builder.getItemColor());
			holder.title.setGravity(gravity);
			holder.image.setImageDrawable(icons[index]);
			holder.image.setVisibility(icons[index] == null ? View.GONE : View.VISIBLE);
			holder.itemView.setEnabled(enabled[index]);
			holder.itemView.setAlpha(enabled[index] ? 1f : 0.4f);
			holder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View clicked) {
					if (enabled[index] && canConfirm(owner, "")) {
						try {
							项目已选择(index, texts[index]);
						} finally {
							owner.dialog.dismiss();
						}
					}
				}
			});
			holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View clicked) {
					if (enabled[index] && session == owner && !owner.closed) {
						项目被长按(index, texts[index]);
					}
					return true;
				}
			});
		}

		@Override
		public int getItemCount() {
			return texts.length;
		}
	}

	/**
	 * 原生图标与文字视图的稳定引用，循环绑定时显式恢复图标可见性。
	 */
	private static final class IconHolder extends RecyclerView.ViewHolder {
		final ImageView image;
		final TextView title;

		IconHolder(View item) {
			super(item);
			image = item.findViewById(android.R.id.icon);
			title = item.findViewById(android.R.id.title);
		}
	}

}
