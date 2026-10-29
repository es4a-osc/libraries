package simple.library.wsd.xui;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.SwitchCompat;

import com.xuexiang.xui.R;
import com.xuexiang.xui.widget.textview.supertextview.SuperTextView;

import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;
import simple.runtime.events.EventDispatcher;

/**
 * XUI超级文本的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI超级文本Impl extends XUI视图组件基类 implements XUI超级文本 {

	// 程序化构造不创建上游 XML 专用的编辑框及附属控件，在此维护对应实例。
	private CompoundButton accessory;
	private int accessoryType;
	private boolean silentAccessory;
	private EditText editor;

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private SuperTextView view;

	/**
	 * 创建XUI超级文本。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI超级文本Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new SuperTextView(ComponentSupport.createContext());

		view.setOnSuperTextViewClickListener(new SuperTextView.OnSuperTextViewClickListener() {

			@Override
			public void onClick(SuperTextView textView) {
				区域被单击("整行");
			}
		});

		// 各文本区和图标分别接收点击，保留整行空白处的点击事件。
		bindRegion(view.getLeftTopTextView(), "左侧顶部");
		bindRegion(view.getLeftTextView(), "左侧");
		bindRegion(view.getLeftBottomTextView(), "左侧底部");
		bindRegion(view.getCenterTopTextView(), "中间顶部");
		bindRegion(view.getCenterTextView(), "中间");
		bindRegion(view.getCenterBottomTextView(), "中间底部");
		bindRegion(view.getRightTopTextView(), "右侧顶部");
		bindRegion(view.getRightTextView(), "右侧");
		bindRegion(view.getRightBottomTextView(), "右侧底部");
		bindRegion(view.getLeftIconIV(), "左图标");
		bindRegion(view.getRightIconIV(), "右图标");

		return view;
	}

	@Override
	public void 置左侧顶部文本(String string) {
		view.setLeftTopString(string);
	}

	@Override
	public void 置左侧文本(String string) {
		view.setLeftString(string);
	}

	@Override
	public void 置左侧底部文本(String string) {
		view.setLeftBottomString(string);
	}

	@Override
	public void 置中间顶部文本(String string) {
		view.setCenterTopString(string);
	}

	@Override
	public void 置中间文本(String string) {
		view.setCenterString(string);
	}

	@Override
	public void 置中间编辑文本(String string) {
		置编辑模式(true);
		editor.setText(string);
	}

	@Override
	public void 置中间底部文本(String string) {
		view.setCenterBottomString(string);
	}

	@Override
	public void 置右侧顶部文本(String string) {
		view.setRightTopString(string);
	}

	@Override
	public void 置右侧文本(String string) {
		view.setRightString(string);
	}

	@Override
	public void 置右侧底部文本(String string) {
		view.setRightBottomString(string);
	}

	@Override
	public void 置左侧顶部文本颜色(int color) {
		view.setLeftTopTextColor(color);
	}

	@Override
	public void 置左侧文本颜色(int color) {
		view.setLeftTextColor(color);
	}

	@Override
	public void 置左侧底部文本颜色(int color) {
		view.setLeftBottomTextColor(color);
	}

	@Override
	public void 置中间顶部文本颜色(int color) {
		view.setCenterTopTextColor(color);
	}

	@Override
	public void 置中间文本颜色(int color) {
		view.setCenterTextColor(color);
	}

	@Override
	public void 置中间底部文本颜色(int color) {
		view.setCenterBottomTextColor(color);
	}

	@Override
	public void 置右侧顶部文本颜色(int color) {
		view.setRightTopTextColor(color);
	}

	@Override
	public void 置右侧文本颜色(int color) {
		view.setRightTextColor(color);
	}

	@Override
	public void 置右侧底部文本颜色(int color) {
		view.setRightBottomTextColor(color);
	}

	@Override
	public String 取左侧顶部文本() {
		return view.getLeftTopString();
	}

	@Override
	public String 取左侧文本() {
		return view.getLeftString();
	}

	@Override
	public String 取左侧底部文本() {
		return view.getLeftBottomString();
	}

	@Override
	public String 取中间顶部文本() {
		return view.getCenterTopString();
	}

	@Override
	public String 取中间文本() {
		return view.getCenterString();
	}

	@Override
	public String 取中间底部文本() {
		return view.getCenterBottomString();
	}

	@Override
	public String 取右侧顶部文本() {
		return view.getRightTopString();
	}

	@Override
	public String 取右侧文本() {
		return view.getRightString();
	}

	@Override
	public String 取右侧底部文本() {
		return view.getRightBottomString();
	}

	@Override
	public void 置开关是否选中(boolean isChecked) {
		置开关是否选中(isChecked, false);
	}

	@Override
	public void 置开关是否选中(boolean isChecked, boolean isSilent) {
		if (accessoryType != 附属类型_开关) {
			置附属类型(附属类型_开关);
		}
		silentAccessory = isSilent;
		try {
			accessory.setChecked(isChecked);
		} finally {
			silentAccessory = false;
		}
	}

	@Override
	public boolean 取开关是否选中() {
		return accessoryType == 附属类型_开关 && accessory.isChecked();
	}

	@Override
	public void 置左侧图标资源(int resId) {
		view.setLeftIcon(resId);
	}

	@Override
	public void 置右侧图标资源(int resId) {
		view.setRightIcon(resId);
	}

	@Override
	public String 取中间编辑值() {
		return editor == null ? "" : editor.getText().toString();
	}

	@Override
	public boolean 是否编辑为空() {
		return 取中间编辑值().isEmpty();
	}

	@Override
	public boolean 是否编辑非为空() {
		return !是否编辑为空();
	}

	@Override
	public void 置左侧文本对齐(int gravity) {
		view.setLeftTextGravity(gravity);
	}

	@Override
	public void 置中间文本对齐(int gravity) {
		view.setCenterTextGravity(gravity);
	}

	@Override
	public void 置右侧文本对齐(int gravity) {
		view.setRightTextGravity(gravity);
	}

	@Override
	public void 置顶部分隔线行可见性(int visibility) {
		view.setTopDividerLineVisibility(visibility);
	}

	@Override
	public void 置底部分隔线行可见性(int visibility) {
		view.setBottomDividerLineVisibility(visibility);
	}

	@Override
	public void 置形状按下颜色(int color) {
		view.setShapeSelectorPressedColor(color);
	}

	@Override
	public void 置形状正常颜色(int color) {
		view.setShapeSelectorNormalColor(color);
	}

	@Override
	public void 置形状填充颜色(int color) {
		view.setShapeSolidColor(color);
	}

	@Override
	public void 置形状描边宽度(int strokeWidth) {
		view.setShapeStrokeWidth(strokeWidth);
	}

	@Override
	public void 置形状描边颜色(int strokeColor) {
		view.setShapeStrokeColor(strokeColor);
	}

	@Override
	public void 置形状描边虚线宽度(float strokeDashWidth) {
		view.setShapeSrokeDashWidth(strokeDashWidth);
	}

	@Override
	public void 置形状描边虚线间隔(float strokeDashGap) {
		view.setShapeStrokeDashGap(strokeDashGap);
	}

	@Override
	public void 置形状圆角(float radius) {
		view.setShapeCornersRadius(radius);
	}

	@Override
	public void 置形状左上圆角(float radius) {
		view.setShapeCornersTopLeftRadius(radius);
	}

	@Override
	public void 置形状右上圆角(float radius) {
		view.setShapeCornersTopRightRadius(radius);
	}

	@Override
	public void 置形状左下圆角(float radius) {
		view.setShapeCornersBottomLeftRadius(radius);
	}

	@Override
	public void 置形状右下圆角(float radius) {
		view.setShapeCornersBottomRightRadius(radius);
	}

	@Override
	public void 置左侧文本框图像左侧(String image) {
		view.setLeftTvDrawableLeft(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置左侧文本框图像右侧(String image) {
		view.setLeftTvDrawableRight(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置中间文本框图像左侧(String image) {
		view.setCenterTvDrawableLeft(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置中间文本框图像右侧(String image) {
		view.setCenterTvDrawableRight(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置右侧文本框图像左侧(String image) {
		view.setRightTvDrawableLeft(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置右侧文本框图像右侧(String image) {
		view.setRightTvDrawableRight(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置左侧图标(String image) {
		view.setLeftIcon(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置右侧图标(String image) {
		view.setRightIcon(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 区域被单击(String region) {
		EventDispatcher.dispatchEvent(this, "区域被单击", region);
	}

	@Override
	public void 置附属类型(int type) {
		if (type < 0 || type > 2) {
			throw new 非法参数错误();
		}
		if (accessoryType == type) {
			return;
		}
		if (accessory != null) {
			view.removeView(accessory);
			accessory = null;
		}
		accessoryType = type;
		RelativeLayout.LayoutParams iconParams = (RelativeLayout.LayoutParams) view.getRightIconIV().getLayoutParams();
		iconParams.removeRule(RelativeLayout.START_OF);
		iconParams.addRule(RelativeLayout.ALIGN_PARENT_END);
		if (type != 0) {
			accessory = type == 1 ? new SwitchCompat(view.getContext()) : new AppCompatCheckBox(view.getContext());
			accessory.setId(View.generateViewId());
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_END);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			view.addView(accessory, params);
			iconParams.removeRule(RelativeLayout.ALIGN_PARENT_END);
			iconParams.addRule(RelativeLayout.START_OF, accessory.getId());
			accessory.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton button, boolean checked) {
					if (!silentAccessory) {
						附属选中改变(accessoryType, checked);
					}
				}
			});
		}
		view.getRightIconIV().setLayoutParams(iconParams);
	}

	@Override
	public int 取附属类型() {
		return accessoryType;
	}

	@Override
	public void 置复选框选中(boolean checked, boolean silent) {
		if (accessoryType != 附属类型_复选框) {
			置附属类型(附属类型_复选框);
		}
		silentAccessory = silent;
		try {
			accessory.setChecked(checked);
		} finally {
			silentAccessory = false;
		}
	}

	@Override
	public boolean 取复选框选中() {
		return accessoryType == 附属类型_复选框 && accessory.isChecked();
	}

	@Override
	public void 附属选中改变(int type, boolean checked) {
		EventDispatcher.dispatchEvent(this, "附属选中改变", type, checked);
	}

	@Override
	public void 置编辑模式(boolean enabled) {
		if (enabled && editor == null) {
			editor = new AppCompatEditText(view.getContext());
			editor.setSingleLine(true);
			editor.setTextSize(14);
			editor.setText(view.getCenterString());
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.END_OF, R.id.sLeftViewId);
			params.addRule(RelativeLayout.START_OF, R.id.sRightViewId);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			view.addView(editor, params);
			editor.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence text, int start, int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence text, int start, int before, int count) {
					编辑文本改变(text.toString());
				}

				@Override
				public void afterTextChanged(Editable text) {
				}
			});
			editor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				@Override
				public void onFocusChange(View focused, boolean hasFocus) {
					编辑焦点改变(hasFocus);
				}
			});
		}
		view.findViewById(R.id.sCenterViewId).setVisibility(enabled ? View.GONE : View.VISIBLE);
		if (editor != null) {
			editor.setVisibility(enabled ? View.VISIBLE : View.GONE);
		}
	}

	@Override
	public void 置编辑提示(String hint) {
		置编辑模式(true);
		editor.setHint(hint);
	}

	@Override
	public void 置编辑输入类型(int type) {
		置编辑模式(true);
		editor.setInputType(type);
	}

	@Override
	public void 编辑文本改变(String text) {
		EventDispatcher.dispatchEvent(this, "编辑文本改变", text);
	}

	@Override
	public void 编辑焦点改变(boolean focused) {
		EventDispatcher.dispatchEvent(this, "编辑焦点改变", focused);
	}

	/**
	 * 为已创建的文本区绑定独立事件，不改变上游区域布局。
	 *
	 * @param target 区域视图。
	 * @param region 对外区域名称。
	 */
	private void bindRegion(View target, final String region) {
		if (target != null) {
			target.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View clicked) {
					区域被单击(region);
				}
			});
		}
	}

	@Override
	public void 置区域字体(int region, String asset, float size) {
		TextView[] regions = {view.getLeftTopTextView(), view.getLeftTextView(), view.getLeftBottomTextView(), view.getCenterTopTextView(), view.getCenterTextView(), view.getCenterBottomTextView(), view.getRightTopTextView(), view.getRightTextView(), view.getRightBottomTextView()};
		ComponentSupport.validateIndex(region, regions.length);
		if (!Float.isFinite(size) || size <= 0) {
			throw new 非法参数错误();
		}
		TextView target = regions[region];
		if (target == null) {
			throw new ComponentStateException("该文字区域尚未创建，请先设置区域文字。");
		}
		target.setTypeface(ThemeSupport.loadFont(asset));
		target.setTextSize(size);
	}

}
