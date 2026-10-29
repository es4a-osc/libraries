package simple.library.wsd.xui;

import android.content.res.ColorStateList;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;

import com.xuexiang.xui.widget.spinner.editspinner.EditSpinner;

import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * XUI编辑下拉框的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI编辑下拉框Impl extends XUI视图组件基类 implements XUI编辑下拉框 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private EditSpinner view;

	// 当前数据用于校验索引及事件文本。
	private String[] items;

	/**
	 * 创建XUI编辑下拉框。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI编辑下拉框Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new EditSpinner(ComponentSupport.createContext());

		items = new String[0];

		view.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View child, int index, long id) {
				项目被选择(index, view.getText());
			}
		});

		view.getEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence text, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence text, int start, int before, int count) {
				文本改变(text.toString());
			}

			@Override
			public void afterTextChanged(Editable text) {
			}
		});

		return view;
	}

	@Override
	public void 置文字颜色(int color) {
		view.setTextColor(color);
	}

	@Override
	public void 置是否筛选关键字(boolean isFilterKey) {
		view.setIsFilterKey(isFilterKey);
	}

	@Override
	public void 置筛选颜色(String filterColor) {
		view.setFilterColor(filterColor);
	}

	@Override
	public void 置文字大小(float textSize) {
		view.setTextSize(textSize);
	}

	@Override
	public void 置提示文本(String hint) {
		view.setHint(hint);
	}

	@Override
	public void 置最大行(int maxLine) {
		view.setMaxLine(maxLine);
	}

	@Override
	public void 置编辑文本高度(int dp) {
		view.setEditTextHeight(dp);
	}

	@Override
	public void 置编辑文本宽度(int dp) {
		view.setEditTextWidth(dp);
	}

	@Override
	public void 置最大长度(int maxLength) {
		view.setMaxLength(maxLength);
	}

	@Override
	public void 置最大字宽(int maxEms) {
		view.setMaxEms(maxEms);
	}

	@Override
	public void 置输入类型(int type) {
		view.setInputType(type);
	}

	@Override
	public void 置项目数组(String[] itemsValue) {
		items = ComponentSupport.copyTextArray(itemsValue);
		view.setItems(items);
	}

	@Override
	public int 取项目数() {
		return items.length;
	}

	@Override
	public void 置文本颜色(int color) {
		view.setTextColors(ColorStateList.valueOf(color));
	}

	@Override
	public void 置箭头图片图像(String image) {
		view.setArrowImageDrawable(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置文本(String text) {
		view.setText(text);
	}

	@Override
	public String 取文本() {
		return String.valueOf(view.getText());
	}

	@Override
	public void 项目被选择(int index, String text) {
		EventDispatcher.dispatchEvent(this, "项目被选择", index, text);
	}

	@Override
	public void 文本改变(String text) {
		EventDispatcher.dispatchEvent(this, "文本改变", text);
	}

	@Override
	public boolean 启用() {
		return view.getEditText().isEnabled();
	}
}
