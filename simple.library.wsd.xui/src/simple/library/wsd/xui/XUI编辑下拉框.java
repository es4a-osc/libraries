package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI编辑下拉框组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI编辑下拉框 extends XUI视图组件 {

	/**
	 * 设置输入框字体的颜色。
	 *
	 * @param color 输入框字体的颜色。
	 */
	@SimpleFunction
	void 置文字颜色(int color);

	/**
	 * 设置是否显示key为醒目的颜色。
	 *
	 * @param isFilterKey 是否显示key为醒目的颜色。
	 */
	@SimpleFunction
	void 置是否筛选关键字(boolean isFilterKey);

	/**
	 * 设置显示key的醒目颜色。
	 *
	 * @param filterColor 显示key的醒目颜色。
	 */
	@SimpleFunction
	void 置筛选颜色(String filterColor);

	/**
	 * 设置输入框的字体大小。
	 *
	 * @param textSize 输入框的字体大小。
	 */
	@SimpleFunction
	void 置文字大小(float textSize);

	/**
	 * 设置输入框的提示信息。
	 *
	 * @param hint 提示文本。
	 */
	@SimpleFunction
	void 置提示文本(String hint);

	/**
	 * 设置输入框最大的行数。
	 *
	 * @param maxLine 最大行。
	 */
	@SimpleFunction
	void 置最大行(int maxLine);

	/**
	 * 设置输入框的高度。
	 *
	 * @param dp 尺寸，单位 DIP。
	 */
	@SimpleFunction
	void 置编辑文本高度(int dp);

	/**
	 * 设置输入框的宽度。
	 *
	 * @param dp 尺寸，单位 DIP。
	 */
	@SimpleFunction
	void 置编辑文本宽度(int dp);

	/**
	 * 设置输入框的最大字符长度。
	 *
	 * @param maxLength 最大长度。
	 */
	@SimpleFunction
	void 置最大长度(int maxLength);

	/**
	 * 设置输入框的最大字符宽度。
	 *
	 * @param maxEms 最大字宽。
	 */
	@SimpleFunction
	void 置最大字宽(int maxEms);

	/**
	 * 设置输入的类型。
	 *
	 * @param type 类型。
	 */
	@SimpleFunction
	void 置输入类型(int type);

	/**
	 * 替换项目数据，按传入顺序显示。
	 *
	 * @param itemsValue 完整项目数组；空数组清空项目。
	 */
	@SimpleFunction
	void 置项目数组(String[] itemsValue);

	/**
	 * 取得项目总数。
	 *
	 * @return 项目总数。
	 */
	@SimpleFunction
	int 取项目数();

	/**
	 * 设置文本颜色颜色。
	 *
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	void 置文本颜色(int color);

	/**
	 * 设置箭头图片图像，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置箭头图片图像(String image);

	/**
	 * 设置组件显示或输入的文本。
	 *
	 * @param text 文本内容。
	 */
	@SimpleFunction
	void 置文本(String text);

	/**
	 * 取得组件显示或输入的文本。
	 *
	 * @return 组件显示或输入的文本。
	 */
	@SimpleFunction
	String 取文本();

	/**
	 * 项目被选择时触发。
	 *
	 * @param index 从 0 开始的项目索引。
	 * @param text 文本内容。
	 */
	@SimpleEvent
	void 项目被选择(int index, String text);

	/**
	 * 内部输入内容改变时触发。
	 *
	 * @param text 文本内容。
	 */
	@SimpleEvent
	void 文本改变(String text);
}
