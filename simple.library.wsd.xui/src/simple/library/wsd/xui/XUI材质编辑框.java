package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI材质材质编辑框组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI材质编辑框 extends XUI文本组件 {

	/**
	 * 设置图标左侧。
	 *
	 * @param res 图片资源 ID。
	 */
	@SimpleFunction
	void 置左侧图标资源(int res);

	/**
	 * 设置图标右侧。
	 *
	 * @param res 图片资源 ID。
	 */
	@SimpleFunction
	void 置右侧图标资源(int res);

	/**
	 * 取得当前底部行数。
	 *
	 * @return 当前底部行数。
	 */
	@SimpleFunction
	float 取当前底部行数();

	/**
	 * 是否浮动标签始终显示。
	 *
	 * @return 是否浮动标签始终显示。
	 */
	@SimpleFunction
	boolean 是否浮动标签始终显示();

	/**
	 * 设置浮动标签始终显示。
	 *
	 * @param floatingLabelAlwaysShown 浮动标签始终显示。
	 */
	@SimpleFunction
	void 置浮动标签始终显示(boolean floatingLabelAlwaysShown);

	/**
	 * 是否辅助文本始终显示。
	 *
	 * @return 是否辅助文本始终显示。
	 */
	@SimpleFunction
	boolean 是否辅助文本始终显示();

	/**
	 * 设置辅助文本始终显示。
	 *
	 * @param helperTextAlwaysShown 辅助文本始终显示。
	 */
	@SimpleFunction
	void 置辅助文本始终显示(boolean helperTextAlwaysShown);

	/**
	 * 是否隐藏下划线。
	 *
	 * @return 是否隐藏下划线。
	 */
	@SimpleFunction
	boolean 是否隐藏下划线();

	/**
	 * 设置隐藏下划线。
	 *
	 * @param hideUnderline 隐藏下划线。
	 */
	@SimpleFunction
	void 置隐藏下划线(boolean hideUnderline);

	/**
	 * 取得下划线颜色。
	 *
	 * @return 下划线颜色。
	 */
	@SimpleFunction
	int 取下划线颜色();

	/**
	 * 设置下划线颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置下划线颜色(int color);

	/**
	 * 取得浮动标签文本。
	 *
	 * @return 浮动标签文本。
	 */
	@SimpleFunction
	String 取浮动标签文本();

	/**
	 * 设置浮动标签文本。
	 *
	 * @param floatingLabelText 浮动标签文本。
	 */
	@SimpleFunction
	void 置浮动标签文本(String floatingLabelText);

	/**
	 * 取得浮动标签文本尺寸。
	 *
	 * @return 浮动标签文本尺寸。
	 */
	@SimpleFunction
	int 取浮动标签文本尺寸();

	/**
	 * 设置浮动标签文本尺寸。
	 *
	 * @param size 尺寸。
	 */
	@SimpleFunction
	void 置浮动标签文本尺寸(int size);

	/**
	 * 取得浮动标签文本颜色。
	 *
	 * @return 浮动标签文本颜色。
	 */
	@SimpleFunction
	int 取浮动标签文本颜色();

	/**
	 * 设置浮动标签文本颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置浮动标签文本颜色(int color);

	/**
	 * 取得底部文本尺寸。
	 *
	 * @return 底部文本尺寸。
	 */
	@SimpleFunction
	int 取底部文本尺寸();

	/**
	 * 设置底部文本尺寸。
	 *
	 * @param size 尺寸。
	 */
	@SimpleFunction
	void 置底部文本尺寸(int size);

	/**
	 * 是否校验开焦点离开。
	 *
	 * @return 是否校验开焦点离开。
	 */
	@SimpleFunction
	boolean 是否校验开焦点离开();

	/**
	 * 设置校验开焦点离开。
	 *
	 * @param validate 校验。
	 */
	@SimpleFunction
	void 置校验开焦点离开(boolean validate);

	/**
	 * 设置基础颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置基础颜色(int color);

	/**
	 * 设置主颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置主颜色(int color);

	/**
	 * 设置浮动标签。
	 *
	 * @param mode 模式。
	 */
	@SimpleFunction
	void 置浮动标签(int mode);

	/**
	 * 取得浮动标签内边距。
	 *
	 * @return 浮动标签内边距。
	 */
	@SimpleFunction
	int 取浮动标签内边距();

	/**
	 * 设置浮动标签内边距。
	 *
	 * @param padding 内边距。
	 */
	@SimpleFunction
	void 置浮动标签内边距(int padding);

	/**
	 * 是否浮动标签动画中。
	 *
	 * @return 是否浮动标签动画中。
	 */
	@SimpleFunction
	boolean 是否浮动标签动画中();

	/**
	 * 设置浮动标签动画中。
	 *
	 * @param animating 动画中。
	 */
	@SimpleFunction
	void 置浮动标签动画中(boolean animating);

	/**
	 * 设置单行省略。
	 */
	@SimpleFunction
	void 置单行省略();

	/**
	 * 设置单行省略。
	 *
	 * @param enabled 启用。
	 */
	@SimpleFunction
	void 置单行省略(boolean enabled);

	/**
	 * 取得最大字符数量。
	 *
	 * @return 最大字符数量。
	 */
	@SimpleFunction
	int 取最大字符数量();

	/**
	 * 设置最大字符数量。
	 *
	 * @param max 最大值。
	 */
	@SimpleFunction
	void 置最大字符数量(int max);

	/**
	 * 取得最小字符数量。
	 *
	 * @return 最小字符数量。
	 */
	@SimpleFunction
	int 取最小字符数量();

	/**
	 * 设置最小字符数量。
	 *
	 * @param min 最小值。
	 */
	@SimpleFunction
	void 置最小字符数量(int min);

	/**
	 * 取得最小底部文本行数。
	 *
	 * @return 最小底部文本行数。
	 */
	@SimpleFunction
	int 取最小底部文本行数();

	/**
	 * 设置最小底部文本行数。
	 *
	 * @param lines 行数。
	 */
	@SimpleFunction
	void 置最小底部文本行数(int lines);

	/**
	 * 是否自动校验。
	 *
	 * @return 是否自动校验。
	 */
	@SimpleFunction
	boolean 是否自动校验();

	/**
	 * 设置自动校验。
	 *
	 * @param autoValidate 自动校验。
	 */
	@SimpleFunction
	void 置自动校验(boolean autoValidate);

	/**
	 * 取得错误颜色。
	 *
	 * @return 错误颜色。
	 */
	@SimpleFunction
	int 取错误颜色();

	/**
	 * 设置错误颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置错误颜色(int color);

	/**
	 * 设置辅助文本。
	 *
	 * @param helperText 辅助文本。
	 */
	@SimpleFunction
	void 置辅助文本(String helperText);

	/**
	 * 取得辅助文本。
	 *
	 * @return 辅助文本。
	 */
	@SimpleFunction
	String 取辅助文本();

	/**
	 * 取得辅助文本颜色。
	 *
	 * @return 辅助文本颜色。
	 */
	@SimpleFunction
	int 取辅助文本颜色();

	/**
	 * 设置辅助文本颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置辅助文本颜色(int color);

	/**
	 * 设置输入框是否允许为空。
	 *
	 * @param allowEmpty 允许为空。
	 * @param errorEmpty 错误为空。
	 */
	@SimpleFunction
	void 置允许为空(boolean allowEmpty, String errorEmpty);

	/**
	 * 设置错误。
	 *
	 * @param errorText 错误文本。
	 */
	@SimpleFunction
	void 置错误(String errorText);

	/**
	 * 取得错误。
	 *
	 * @return 错误。
	 */
	@SimpleFunction
	String 取错误();

	/**
	 * 校验。
	 *
	 * @return 校验。
	 */
	@SimpleFunction
	boolean 校验();

	/**
	 * 清除内容。
	 */
	@SimpleFunction
	void 清空();

	/**
	 * 获取输入的内容。
	 *
	 * @return 输入的内容。
	 */
	@SimpleFunction
	String 取编辑值();

	/**
	 * 输入的内容是否为空。
	 *
	 * @return 输入的内容是否为空。
	 */
	@SimpleFunction
	boolean 是否为空();

	/**
	 * 输入的内容是否不为空。
	 *
	 * @return 输入的内容是否不为空。
	 */
	@SimpleFunction
	boolean 是否非空();

	/**
	 * 判断当前文本是否满足字数限制。
	 *
	 * @return 满足字数限制时返回真。
	 */
	@SimpleFunction
	boolean 是否字数有效();

	/**
	 * 设置输入提示文本。
	 *
	 * @param text 文本内容。
	 */
	@SimpleFunction
	void 置提示文本(String text);

	/**
	 * 取得当前输入内容。
	 *
	 * @return 当前输入内容。
	 */
	@SimpleFunction
	String 取输入文本();

	/**
	 * 设置输入类型。
	 *
	 * @param type Android InputType 标志组合。
	 */
	@SimpleFunction
	void 置输入类型(int type);

	/**
	 * 设置光标位置，允许位于文本末尾。
	 *
	 * @param index 从 0 开始的项目索引。
	 */
	@SimpleFunction
	void 置光标位置(int index);

	/**
	 * 设置图标左侧，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置左侧图标(String image);

	/**
	 * 设置图标右侧，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置右侧图标(String image);

	/**
	 * 设置材质编辑框文本颜色颜色。
	 *
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	void 置编辑框文本颜色(int color);

	/**
	 * 设置材质编辑框提示文本颜色颜色。
	 *
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	void 置编辑框提示文本颜色(int color);

	/**
	 * 设置是否显示输入内容清除按钮。
	 *
	 * @param show 是否显示清除按钮。
	 */
	@SimpleFunction
	void 置清除按钮(boolean show);

	/**
	 * 添加不允许空输入的校验规则。
	 *
	 * @param error 校验失败时显示的非空提示。
	 */
	@SimpleFunction
	void 添加必填规则(String error);

	/**
	 * 添加与完整输入匹配的正则校验规则。
	 *
	 * @param expression Java 正则表达式；无效表达式拒绝登记。
	 * @param error 校验失败时显示的非空提示。
	 */
	@SimpleFunction
	void 添加正则规则(String expression, String error);

	/**
	 * 移除已登记的校验规则并清除错误提示。
	 */
	@SimpleFunction
	void 清空校验规则();

}
