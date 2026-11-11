package simple.library.wsd.iconics;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import simple.runtime.components.impl.android.视图组件;
import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * 字体图标公共视图适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
abstract class WidgetBase extends 视图组件 implements 字体图标视图, simple.runtime.android.MainActivity.OnDestroyListener {
	boolean destroyed;

	/**
	 * 初始化通用交互。
	 *
	 * @param container 所属容器。
	 */
	protected WidgetBase(组件容器 container) {
		super(container);
		simple.runtime.android.MainActivity.getContext().addOnDestroyListener(this);
		getView().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				check();
				被单击();
			}
		});
		getView().setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				check();
				被长按();
				return true;
			}
		});
		getView().setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean focused) {
				check();
				焦点改变(focused);
			}
		});
	}

	/**
	 * 取得文本目标。
	 *
	 * @return 文本控件。
	 */
	TextView text() {
		if (!(getView() instanceof TextView)) {
			throw new 图标错误("此操作需要文本控件");
		}
		return (TextView) getView();
	}

	@Override
	public void 设置图标(图标图像 image) {
		check();
		if (image == null) {
			throw new 图标错误("图像为空");
		}
		if (getView() instanceof ImageView) {
			image.应用图片(this);
		} else {
			image.应用文字附图(this, 0);
		}
	}

	@Override
	public void 设置文字(String text) {
		check();
		text();
		图标工具.应用文字图标(this, text);
	}

	@Override
	public String 取文字() {
		check();
		return text().getText().toString();
	}

	@Override
	public void 设置字号(float size) {
		check();
		if (!Float.isFinite(size) || size <= 0) {
			throw new 图标错误("字号必须大于零");
		}
		text().setTextSize(size);
	}

	@Override
	public void 设置文字颜色(int color) {
		check();
		text().setTextColor(color);
	}

	@Override
	public void 设置启用(boolean enabled) {
		check();
		getView().setEnabled(enabled);
	}

	@Override
	public void 设置内容描述(String text) {
		check();
		getView().setContentDescription(text);
	}

	@Override
	public void 被单击() {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "被单击");
		}
	}

	@Override
	public void 被长按() {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "被长按");
		}
	}

	@Override
	public void 焦点改变(boolean focused) {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "焦点改变", focused);
		}
	}

	@Override
	public void 设置附图(图标图像 image, int position) {
		check();
		if (image == null) {
			throw new 图标错误("图像为空");
		}
		image.应用文字附图(this, position);
	}

	/**
	 * 验证组件仍可用。
	 */
	void check() {
		Support.main();
		if (destroyed) {
			throw new 图标错误("图标控件已销毁");
		}
	}

	@Override
	public void onDestroy() {
		销毁();
	}

	@Override
	public void 销毁() {
		if (destroyed) {
			return;
		}
		destroyed = true;
		getView().setOnClickListener(null);
		getView().setOnLongClickListener(null);
		getView().setOnFocusChangeListener(null);
		if (getView() instanceof CompoundButton) {
			((CompoundButton) getView()).setOnCheckedChangeListener(null);
		}
		if (getView() instanceof com.mikepenz.iconics.view.IconicsCheckableTextView) {
			((com.mikepenz.iconics.view.IconicsCheckableTextView) getView()).setOnCheckedChangeListener(null);
		}
		simple.runtime.android.MainActivity.getContext().removeOnDestroyListener(this);
		super.销毁();
	}

	@Override
	public 图标图像 取图标() {
		check();
		if (getView() instanceof ImageView) {
			return 图标图像.wrap(((ImageView) getView()).getDrawable());
		}
		return ViewIcons.get(text(), 0, false);
	}

	@Override
	public void 清除图标() {
		check();
		if (getView() instanceof ImageView) {
			((ImageView) getView()).setImageDrawable(null);
		} else {
			ViewIcons.set(text(), 0, null, false);
		}
	}

	@Override
	public void 清除背景() {
		check();
		getView().setBackground(null);
	}

	@Override
	public 图标图像 取背景图标() {
		check();
		return 图标图像.wrap(getView().getBackground());
	}

	@Override
	public 图标图像 取附图(int position) {
		check();
		return ViewIcons.get(text(), position, false);
	}

	@Override
	public void 清除附图(int position) {
		check();
		ViewIcons.set(text(), position, null, false);
	}

	@Override
	public void 设置四侧附图(图标图像 image) {
		check();
		ViewIcons.all(text(), image, false);
	}

	@Override
	public void 设置图文间距(int spacing) {
		check();
		if (spacing < 0) {
			throw new 图标错误("间距不能为负");
		}
		text().setCompoundDrawablePadding(Support.dp(spacing));
	}

	@Override
	public int 取图文间距() {
		check();
		return Math.round(text().getCompoundDrawablePadding() / Support.dpf(1));
	}

	@Override
	public void 设置布局方向(int direction) {
		check();
		if (direction < 0 || direction > 3) {
			throw new 图标错误("方向为0左至右1右至左2继承3随文字");
		}
		getView().setLayoutDirection(direction);
	}

	@Override
	public int 取布局方向() {
		check();
		return getView().getLayoutDirection();
	}

	@Override
	public boolean 是否启用() {
		check();
		return getView().isEnabled();
	}

	@Override
	public String 取内容描述() {
		check();
		CharSequence s = getView().getContentDescription();
		return s == null ? "" : s.toString();
	}

	@Override
	public float 取字号() {
		check();
		return text().getTextSize()
				/ simple.runtime.android.MainActivity.getContext().getResources().getDisplayMetrics().scaledDensity;
	}

	@Override
	public int 取文字颜色() {
		check();
		return text().getCurrentTextColor();
	}

	@Override
	public void 设置图片缩放方式(int mode) {
		check();
		if (!(getView() instanceof ImageView) || mode < 0 || mode > 7) {
			throw new 图标错误("需要图片视图，方式0至7");
		}
		((ImageView) getView()).setScaleType(ImageView.ScaleType.values()[mode]);
	}

	@Override
	public int 取图片缩放方式() {
		check();
		if (!(getView() instanceof ImageView)) {
			throw new 图标错误("需要图片视图");
		}
		return ((ImageView) getView()).getScaleType().ordinal();
	}

	@Override
	public void 设置保持宽高比(boolean enabled) {
		check();
		if (!(getView() instanceof ImageView)) {
			throw new 图标错误("需要图片视图");
		}
		((ImageView) getView()).setAdjustViewBounds(enabled);
	}

	@Override
	public boolean 是否保持宽高比() {
		check();
		if (!(getView() instanceof ImageView)) {
			throw new 图标错误("需要图片视图");
		}
		return ((ImageView) getView()).getAdjustViewBounds();
	}
}
