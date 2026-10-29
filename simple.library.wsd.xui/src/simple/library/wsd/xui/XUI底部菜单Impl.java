package simple.library.wsd.xui;

import java.util.Arrays;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;

import com.xuexiang.xui.R;
import com.xuexiang.xui.widget.dialog.bottomsheet.BottomSheet;
import com.xuexiang.xui.widget.dialog.bottomsheet.BottomSheetItemView;

import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * 底部列表和网格菜单的会话、取消方式及 Simple 生命周期适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI底部菜单Impl extends XUI弹层基类 implements XUI底部菜单 {

	private Boolean listCentered;
	private String gridButtonText;

	private Session session;
	private boolean backCancelable = true;
	private boolean outsideCancelable = true;

	/**
	 * 记录单次显示，旧菜单的关闭回调不得清除后来显示的菜单。
	 */
	private static final class Session {
		BottomSheet sheet;
		boolean closed;
		boolean selected;
		boolean canceled;
	}

	/**
	 * 创建底部菜单。
	 *
	 * @param container 所属 Simple 窗口。
	 */
	public XUI底部菜单Impl(组件容器 container) {
		super(container);
	}

	@Override
	public void 置取消方式(boolean back, boolean outside) {
		backCancelable = back;
		outsideCancelable = outside;
	}

	@Override
	public void 显示列表(String title, String[] items, int checked) {
		String[] data = ComponentSupport.copyTextArray(items);
		String[] images = new String[data.length];
		boolean[] enabled = new boolean[data.length];
		for (int i = 0; i < data.length; i++) {
			images[i] = "";
			enabled[i] = true;
		}
		显示列表(title, data, images, new boolean[data.length], enabled, checked);
	}

	@Override
	public void 显示列表(String title, String[] items, String[] images, boolean[] redPoints, boolean[] enabled, int checked) {
		requireVisibleOwner();
		final String[] data = ComponentSupport.copyTextArray(items);
		String[] icons = ComponentSupport.copyTextArray(images);
		if (title == null || data.length == 0 || icons.length != data.length || redPoints == null
			|| enabled == null || redPoints.length != data.length || enabled.length != data.length) {
			throw new 非法参数错误();
		}
		if (checked != -1) {
			ComponentSupport.validateIndex(checked, data.length);
		}
		Drawable[] drawables = new Drawable[data.length];
		for (int i = 0; i < data.length; i++) {
			if (!icons[i].isEmpty()) {
				drawables[i] = ComponentSupport.getDrawable(icons[i]);
			}
		}
		final Session current = prepareSession();
		BottomSheet.BottomListSheetBuilder builder = new BottomSheet.BottomListSheetBuilder(ComponentSupport.createContext(), checked >= 0);
		builder.setTitle(title).setCheckedIndex(checked);
		if (listCentered != null) {
			builder.setIsCenter(listCentered);
		}
		for (String text : data) {
			builder.addItem(text);
		}
		current.sheet = builder.build();
		ListView list = current.sheet.findViewById(R.id.listview);
		list.setAdapter(new BottomMenuAdapter(data, drawables, redPoints.clone(), enabled.clone(), checked,
			new BottomMenuAdapter.SelectionListener() {
				@Override
				public void onSelected(int position) {
					select(current, position, data[position]);
				}
			}));
		showSession(current);
	}

	@Override
	public void 显示网格(String[] items, String[] images, int firstLineCount) {
		String[] data = ComponentSupport.copyTextArray(items);
		boolean[] enabled = new boolean[data.length];
		Arrays.fill(enabled, true);
		显示网格(data, images, new boolean[data.length], enabled, firstLineCount);
	}

	/**
	 * 参数校验完成后才替换旧界面，关闭事件若已打开新界面则拒绝覆盖。
	 *
	 * @return 待显示会话。
	 */
	private Session prepareSession() {
		关闭();
		requireVisibleOwner();
		if (session != null) {
			throw new ComponentStateException("关闭事件已显示新的菜单，请勿重复覆盖。");
		}
		return new Session();
	}

	/**
	 * 在每个界面上单独配置取消与关闭通知。
	 *
	 * @param current 完成构建的会话。
	 */
	private void showSession(final Session current) {
		ThemeSupport.applyFont(current.sheet.getWindow().getDecorView());
		final boolean allowBack = backCancelable;
		current.sheet.setCancelable(backCancelable || outsideCancelable);
		current.sheet.setCanceledOnTouchOutside(outsideCancelable);
		current.sheet.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int code, KeyEvent event) {
				return !allowBack && code == KeyEvent.KEYCODE_BACK;
			}
		});
		current.sheet.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if (!current.closed && !current.selected && !current.canceled) {
					current.canceled = true;
					已取消();
				}
			}
		});
		current.sheet.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (!current.closed) {
					current.closed = true;
					if (session == current) {
						session = null;
					}
					已关闭();
				}
			}
		});
		session = current;
		try {
			current.sheet.show();
		} catch (RuntimeException failure) {
			current.closed = true;
			if (session == current) {
				session = null;
			}
			current.sheet.dismiss();
			throw failure;
		}
	}

	/**
	 * 一次显示只接受一次有效选择，回调中重新打开菜单不会被旧实例关闭。
	 *
	 * @param current 产生点击的会话。
	 * @param index 项目索引。
	 * @param text 项目文字。
	 */
	private void select(Session current, int index, String text) {
		if (session != current || current.closed || current.selected || !current.sheet.isShowing()) {
			return;
		}
		current.selected = true;
		try {
			项目被单击(index, text);
		} finally {
			current.sheet.dismiss();
		}
	}

	@Override
	public boolean 是否显示() {
		return session != null && session.sheet.isShowing();
	}

	@Override
	public void 关闭() {
		Session previous = session;
		session = null;
		if (previous != null) {
			previous.sheet.dismiss();
		}
	}

	@Override
	public void 项目被单击(int index, String text) {
		dispatchEvent("项目被单击", index, text);
	}

	@Override
	public void 已取消() {
		dispatchEvent("已取消");
	}

	@Override
	public void 已关闭() {
		dispatchEvent("已关闭");
	}

	@Override
	public void 显示网格(String[] items, String[] images, boolean[] redPoints, boolean[] enabled, int firstLineCount) {
		requireVisibleOwner();
		final String[] data = ComponentSupport.copyTextArray(items);
		String[] icons = ComponentSupport.copyTextArray(images);
		if (icons.length != data.length || firstLineCount < 1 || firstLineCount > data.length
			|| redPoints == null || enabled == null || redPoints.length != data.length || enabled.length != data.length) {
			throw new 非法参数错误();
		}
		Drawable[] drawables = new Drawable[data.length];
		for (int i = 0; i < data.length; i++) {
			drawables[i] = ComponentSupport.getDrawable(icons[i]);
		}
		final boolean[] active = enabled.clone();
		final Session current = prepareSession();
		BottomSheet.BottomGridSheetBuilder builder = new BottomSheet.BottomGridSheetBuilder(ComponentSupport.createContext());
		if (gridButtonText != null) {
			builder.setIsShowButton(!gridButtonText.isEmpty()).setButtonText(gridButtonText);
		}
		builder.setButtonClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// 原生默认按钮只调用 dismiss，显式取消才能与返回、外部取消采用同一语义。
				current.sheet.cancel();
			}
		});
		for (int i = 0; i < data.length; i++) {
			BottomSheetItemView item = builder.createItemView(drawables[i], data[i], i, 0);
			item.setEnabled(active[i]);
			item.setAlpha(active[i] ? 1.0f : 0.4f);
			if (redPoints[i]) {
				View point = item.getSubScript().inflate();
				point.setBackgroundResource(R.drawable.xui_tips_point);
				int size = item.getResources().getDimensionPixelSize(R.dimen.xui_tips_point_size);
				point.getLayoutParams().width = size;
				point.getLayoutParams().height = size;
			}
			builder.addItem(item, i < firstLineCount ? BottomSheet.BottomGridSheetBuilder.FIRST_LINE : BottomSheet.BottomGridSheetBuilder.SECOND_LINE);
		}
		builder.setOnSheetItemClickListener(new BottomSheet.BottomGridSheetBuilder.OnSheetItemClickListener() {
			@Override
			public void onClick(BottomSheet dialog, BottomSheetItemView item) {
				int index = (Integer) item.getTag();
				if (active[index]) {
					select(current, index, data[index]);
				}
			}
		});
		current.sheet = builder.build();
		showSession(current);
	}

	@Override
	public void 置列表居中(boolean center) {
		listCentered = center;
	}

	@Override
	public void 置底部按钮文本(String text) {
		if (text == null) {
			throw new 非法参数错误();
		}
		gridButtonText = text;
	}

}
