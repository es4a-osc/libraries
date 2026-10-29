package simple.library.wsd.xui;

import simple.runtime.errors.非法参数错误;

import com.xuexiang.xui.widget.edittext.materialedittext.validation.NotAllowEmptyValidator;

import com.xuexiang.xui.widget.edittext.materialedittext.validation.METValidator;

import java.util.regex.Pattern;

import android.content.res.ColorStateList;
import android.view.View;

import com.xuexiang.xui.widget.edittext.materialedittext.MaterialEditText;

import simple.runtime.components.组件容器;

/**
 * XUI材质材质编辑框的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI材质编辑框Impl extends XUI文本组件基类 implements XUI材质编辑框 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private MaterialEditText view;

	/**
	 * 创建XUI材质材质编辑框。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI材质编辑框Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new MaterialEditText(ComponentSupport.createContext());

		return view;
	}

	@Override
	public void 置左侧图标资源(int res) {
		view.setIconLeft(res);
	}

	@Override
	public void 置右侧图标资源(int res) {
		view.setIconRight(res);
	}

	@Override
	public float 取当前底部行数() {
		return view.getCurrentBottomLines();
	}

	@Override
	public boolean 是否浮动标签始终显示() {
		return view.isFloatingLabelAlwaysShown();
	}

	@Override
	public void 置浮动标签始终显示(boolean floatingLabelAlwaysShown) {
		view.setFloatingLabelAlwaysShown(floatingLabelAlwaysShown);
	}

	@Override
	public boolean 是否辅助文本始终显示() {
		return view.isHelperTextAlwaysShown();
	}

	@Override
	public void 置辅助文本始终显示(boolean helperTextAlwaysShown) {
		view.setHelperTextAlwaysShown(helperTextAlwaysShown);
	}

	@Override
	public boolean 是否隐藏下划线() {
		return view.isHideUnderline();
	}

	@Override
	public void 置隐藏下划线(boolean hideUnderline) {
		view.setHideUnderline(hideUnderline);
	}

	@Override
	public int 取下划线颜色() {
		return view.getUnderlineColor();
	}

	@Override
	public void 置下划线颜色(int color) {
		view.setUnderlineColor(color);
	}

	@Override
	public String 取浮动标签文本() {
		return String.valueOf(view.getFloatingLabelText());
	}

	@Override
	public void 置浮动标签文本(String floatingLabelText) {
		view.setFloatingLabelText(floatingLabelText);
	}

	@Override
	public int 取浮动标签文本尺寸() {
		return view.getFloatingLabelTextSize();
	}

	@Override
	public void 置浮动标签文本尺寸(int size) {
		view.setFloatingLabelTextSize(size);
	}

	@Override
	public int 取浮动标签文本颜色() {
		return view.getFloatingLabelTextColor();
	}

	@Override
	public void 置浮动标签文本颜色(int color) {
		view.setFloatingLabelTextColor(color);
	}

	@Override
	public int 取底部文本尺寸() {
		return view.getBottomTextSize();
	}

	@Override
	public void 置底部文本尺寸(int size) {
		view.setBottomTextSize(size);
	}

	@Override
	public boolean 是否校验开焦点离开() {
		return view.isValidateOnFocusLost();
	}

	@Override
	public void 置校验开焦点离开(boolean validate) {
		view.setValidateOnFocusLost(validate);
	}

	@Override
	public void 置基础颜色(int color) {
		view.setBaseColor(color);
	}

	@Override
	public void 置主颜色(int color) {
		view.setPrimaryColor(color);
	}

	@Override
	public void 置浮动标签(int mode) {
		view.setFloatingLabel(mode);
	}

	@Override
	public int 取浮动标签内边距() {
		return view.getFloatingLabelPadding();
	}

	@Override
	public void 置浮动标签内边距(int padding) {
		view.setFloatingLabelPadding(padding);
	}

	@Override
	public boolean 是否浮动标签动画中() {
		return view.isFloatingLabelAnimating();
	}

	@Override
	public void 置浮动标签动画中(boolean animating) {
		view.setFloatingLabelAnimating(animating);
	}

	@Override
	public void 置单行省略() {
		view.setSingleLineEllipsis();
	}

	@Override
	public void 置单行省略(boolean enabled) {
		view.setSingleLineEllipsis(enabled);
	}

	@Override
	public int 取最大字符数量() {
		return view.getMaxCharacters();
	}

	@Override
	public void 置最大字符数量(int max) {
		view.setMaxCharacters(max);
	}

	@Override
	public int 取最小字符数量() {
		return view.getMinCharacters();
	}

	@Override
	public void 置最小字符数量(int min) {
		view.setMinCharacters(min);
	}

	@Override
	public int 取最小底部文本行数() {
		return view.getMinBottomTextLines();
	}

	@Override
	public void 置最小底部文本行数(int lines) {
		view.setMinBottomTextLines(lines);
	}

	@Override
	public boolean 是否自动校验() {
		return view.isAutoValidate();
	}

	@Override
	public void 置自动校验(boolean autoValidate) {
		view.setAutoValidate(autoValidate);
	}

	@Override
	public int 取错误颜色() {
		return view.getErrorColor();
	}

	@Override
	public void 置错误颜色(int color) {
		view.setErrorColor(color);
	}

	@Override
	public void 置辅助文本(String helperText) {
		view.setHelperText(helperText);
	}

	@Override
	public String 取辅助文本() {
		return view.getHelperText();
	}

	@Override
	public int 取辅助文本颜色() {
		return view.getHelperTextColor();
	}

	@Override
	public void 置辅助文本颜色(int color) {
		view.setHelperTextColor(color);
	}

	@Override
	public void 置允许为空(boolean allowEmpty, String errorEmpty) {
		view.setAllowEmpty(allowEmpty, errorEmpty);
	}

	@Override
	public void 置错误(String errorText) {
		view.setError(errorText);
	}

	@Override
	public String 取错误() {
		return String.valueOf(view.getError());
	}

	@Override
	public boolean 校验() {
		return view.validate();
	}

	@Override
	public void 清空() {
		view.clear();
	}

	@Override
	public String 取编辑值() {
		return view.getEditValue();
	}

	@Override
	public boolean 是否为空() {
		return view.isEmpty();
	}

	@Override
	public boolean 是否非空() {
		return view.isNotEmpty();
	}

	@Override
	public boolean 是否字数有效() {
		return view.isCharactersCountValid();
	}

	@Override
	public void 置提示文本(String text) {
		view.setHint(text);
	}

	@Override
	public String 取输入文本() {
		return view.getText().toString();
	}

	@Override
	public void 置输入类型(int type) {
		view.setInputType(type);
	}

	@Override
	public void 置光标位置(int index) {
		ComponentSupport.validateIndex(index, view.length() + 1);
		view.setSelection(index);
	}

	@Override
	public void 置左侧图标(String image) {
		view.setIconLeft(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置右侧图标(String image) {
		view.setIconRight(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置编辑框文本颜色(int color) {
		view.setMetTextColor(ColorStateList.valueOf(color));
	}

	@Override
	public void 置编辑框提示文本颜色(int color) {
		view.setMetHintTextColor(ColorStateList.valueOf(color));
	}

	@Override
	public void 置清除按钮(boolean show) {
		view.setShowClearButton(show);
	}

	@Override
	public void 添加必填规则(String error) {
		validateRuleMessage(error);
		view.addValidator(new NotAllowEmptyValidator(error));
	}

	@Override
	public void 添加正则规则(String expression, String error) {
		validateRuleMessage(error);
		if (expression == null) {
			throw new 非法参数错误();
		}
		final Pattern pattern = Pattern.compile(expression);
		view.addValidator(new METValidator(error) {
			@Override
			public boolean isValid(CharSequence text, boolean empty) {
				return pattern.matcher(text).matches();
			}
		});
	}

	@Override
	public void 清空校验规则() {
		view.clearValidators();
		view.setError(null);
	}

	/**
	 * 校验规则必须携带可解释的错误提示，拒绝半次配置。
	 *
	 * @param error 校验失败提示。
	 */
	private static void validateRuleMessage(String error) {
		if (error == null || error.trim().isEmpty()) {
			throw new 非法参数错误();
		}
	}

}
