package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件;
import simple.runtime.parameters.BooleanReferenceParameter;

/**
 * 在当前窗口上显示提示、输入、选择和进度对话框；须在所属窗口显示后调用。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI对话框 extends 组件 {

	/**
	 * 显示提示或确认对话框；取消文本为空时隐藏取消按钮。
	 *
	 * @param title 标题。
	 * @param message 正文。
	 * @param positive 确认按钮文本。
	 * @param negative 取消按钮文本，可为空。
	 */
	@SimpleFunction
	void 显示提示(String title, String message, String positive, String negative);

	/**
	 * 显示输入对话框，确认后触发输入已确认事件。
	 *
	 * @param title 标题。
	 * @param hint 输入提示。
	 * @param value 初始文本。
	 * @param password 是否采用密码输入。
	 */
	@SimpleFunction
	void 显示输入(String title, String hint, String value, boolean password);

	/**
	 * 显示普通列表，点击项目后关闭。
	 *
	 * @param title 标题。
	 * @param items 项目文本数组。
	 */
	@SimpleFunction
	void 显示列表(String title, String[] items);

	/**
	 * 显示单选列表，确认后报告项目；选中索引从零开始。
	 *
	 * @param title 标题。
	 * @param items 项目文本数组。
	 * @param selected 初始索引；-1 表示未选。
	 */
	@SimpleFunction
	void 显示单选(String title, String[] items, int selected);

	/**
	 * 显示多选列表，确认后报告索引数组。
	 *
	 * @param title 标题。
	 * @param items 项目文本数组。
	 * @param selected 初始选中索引，可为空数组。
	 */
	@SimpleFunction
	void 显示多选(String title, String[] items, int[] selected);

	/**
	 * 显示确定或不确定进度对话框。
	 *
	 * @param title 标题。
	 * @param message 正文。
	 * @param maximum 最大进度，须大于零。
	 * @param indeterminate 是否采用不确定进度。
	 */
	@SimpleFunction
	void 显示进度(String title, String message, int maximum, boolean indeterminate);

	/**
	 * 更新已显示的确定进度对话框。
	 *
	 * @param value 从零到最大进度的值。
	 */
	@SimpleFunction
	void 置进度(int value);

	/**
	 * 提示对话框按钮被单击。
	 *
	 * @param confirmed 真表示确认，假表示取消。
	 */
	@SimpleEvent
	void 按钮被单击(boolean confirmed);

	/**
	 * 输入内容通过确认按钮提交。
	 *
	 * @param text 输入文本。
	 */
	@SimpleEvent
	void 输入已确认(String text);

	/**
	 * 普通列表或单选列表提交项目。
	 *
	 * @param index 项目索引。
	 * @param text 项目文本。
	 */
	@SimpleEvent
	void 项目已选择(int index, String text);

	/**
	 * 多选列表通过确认按钮提交。
	 *
	 * @param indices 选中索引数组。
	 */
	@SimpleEvent
	void 多选已确认(int[] indices);

	/**
	 * 对话框因按钮、返回键、外部点击或切窗而关闭。
	 */
	@SimpleEvent
	void 已关闭();

	/**
	 * 关闭当前显示的界面。
	 */
	@SimpleFunction
	void 关闭();

	/**
	 * 取得当前界面是否正在显示。
	 *
	 * @return 是否显示。
	 */
	@SimpleFunction
	boolean 是否显示();

	/**
	 * 设置下次显示时返回键和外部点击是否取消；取消按钮不受影响。
	 *
	 * @param back 是否允许返回键取消。
	 * @param outside 是否允许点击外部取消。
	 */
	@SimpleFunction
	void 置取消方式(boolean back, boolean outside);

	/**
	 * 设置下次输入对话框的 Android InputType；显示输入的密码参数为真时优先采用密码模式。
	 *
	 * @param type Android 输入类型标志。
	 */
	@SimpleFunction
	void 置输入类型(int type);

	/**
	 * 设置下次输入对话框的长度范围，按 UTF-16 字符单元计数；超出范围时禁用确认。
	 *
	 * @param minimum 最小长度，不小于零。
	 * @param maximum 最大长度，-1 表示不限制；其它值不得小于最小长度。
	 */
	@SimpleFunction
	void 置输入长度(int minimum, int maximum);

	/**
	 * 设置当前输入对话框的错误提示，适合在确认前校验事件中使用。
	 *
	 * @param error 错误提示；空文本清除错误。
	 */
	@SimpleFunction
	void 置输入错误(String error);

	/**
	 * 暂借当前窗口内的可视组件作为内容，内容可预先隐藏，关闭时恢复原父容器、顺序、布局参数和可见性；不可借出窗口根。
	 *
	 * @param title 标题。
	 * @param content 当前窗口内的可视组件，通常使用容器。
	 * @param positive 确认按钮文字。
	 * @param negative 取消按钮文字，空文本隐藏取消按钮。
	 */
	@SimpleFunction
	void 显示自定义(String title, 可视组件 content, String positive, String negative);

	/**
	 * 用户即将确认时同步校验；置取消为真可保留界面且不触发确认结果事件。
	 *
	 * @param text 输入模式的当前文字，其它模式为空。
	 * @param cancel 是否阻止确认，初始为假。
	 */
	@SimpleEvent
	void 确认前校验(String text, BooleanReferenceParameter cancel);

	/**
	 * 用户通过取消按钮、返回键或外部点击取消；程序关闭不会触发此事件。
	 */
	@SimpleEvent
	void 已取消();

	/**
	 * 设置后续对话框的中立按钮文本，空文本隐藏。
	 *
	 * @param text 按钮文本。
	 */
	@SimpleFunction
	void 置中立按钮(String text);

	/**
	 * 中立按钮被单击时触发，事件结束后关闭本次对话框。
	 */
	@SimpleEvent
	void 中立按钮被单击();

	/**
	 * 设置后续对话框的复选提示和初始选中状态，空文本隐藏。
	 *
	 * @param text 提示文本。
	 * @param checked 初始是否选中。
	 */
	@SimpleFunction
	void 置复选提示(String text, boolean checked);

	/**
	 * 取得当前对话框复选提示的选中状态，可在确认前校验中调用。
	 *
	 * @return 当前对话框复选提示的选中状态，可在确认前校验中调用。
	 */
	@SimpleFunction
	boolean 取复选提示选中();

	/**
	 * 设置后续列表对话框中不可选择的项目。
	 *
	 * @param indices 从零开始的项目位置数组，空数组全部启用。
	 */
	@SimpleFunction
	void 置禁用项目(int[] indices);

	/**
	 * 取得当前单选位置，未选中返回 -1，可在确认前校验中调用。
	 *
	 * @return 当前单选位置，未选中返回 -1，可在确认前校验中调用。
	 */
	@SimpleFunction
	int 取单选位置();

	/**
	 * 取得当前多选位置副本，可在确认前校验中调用。
	 *
	 * @return 当前多选位置副本，可在确认前校验中调用。
	 */
	@SimpleFunction
	int[] 取多选位置数组();

	/**
	 * 设置下次显示的标题图标，同时更新当前对话框；空文本清除图标。
	 *
	 * @param image 本地图片或资源标识。
	 */
	@SimpleFunction
	void 置图标(String image);

	/**
	 * 配置后续显示的标题、正文和列表文字颜色。
	 *
	 * @param title ARGB 标题文字色。
	 * @param content ARGB 正文文字色。
	 * @param items ARGB 列表文字色。
	 */
	@SimpleFunction
	void 置文字颜色(int title, int content, int items);

	/**
	 * 配置后续显示的标题、正文和列表文字对齐。
	 *
	 * @param title 对齐常量。
	 * @param content 对齐常量。
	 * @param items 对齐常量。
	 */
	@SimpleFunction
	void 置文字对齐(int title, int content, int items);

	/**
	 * 配置后续显示的确认、取消及中立按钮文字颜色。
	 *
	 * @param positive ARGB 颜色。
	 * @param negative ARGB 颜色。
	 * @param neutral ARGB 颜色。
	 */
	@SimpleFunction
	void 置按钮颜色(int positive, int negative, int neutral);

	/**
	 * 配置后续显示的对话框背景色。
	 *
	 * @param color ARGB 背景颜色。
	 */
	@SimpleFunction
	void 置背景颜色(int color);

	/**
	 * 配置后续显示的按钮是否纵向堆叠。
	 *
	 * @param mode 0 自动适应，1 始终堆叠，2 不堆叠。
	 */
	@SimpleFunction
	void 置按钮排列(int mode);

	/**
	 * 更新正在显示的对话框标题。
	 *
	 * @param title 新的标题。
	 */
	@SimpleFunction
	void 更新标题(String title);

	/**
	 * 更新正在显示的正文；当前对话框须具有正文区域。
	 *
	 * @param content 新的正文。
	 */
	@SimpleFunction
	void 更新内容(String content);

	/**
	 * 更新正在显示的输入对话框内容并将光标移到末尾。
	 *
	 * @param text 新的输入内容。
	 */
	@SimpleFunction
	void 更新输入(String text);

	/**
	 * 更新当前已有按钮的文字，空文本隐藏按钮；不创建没有行为的新按钮。
	 *
	 * @param action 按钮常量。
	 * @param text 新的按钮文字。
	 */
	@SimpleFunction
	void 更新按钮文本(int action, String text);

	/**
	 * 设置当前已有按钮的启用状态，不改变其它按钮或确认校验逻辑。
	 *
	 * @param action 按钮常量。
	 * @param enabled 是否允许点击。
	 */
	@SimpleFunction
	void 置按钮启用(int action, boolean enabled);

	/**
	 * 更新正在显示的单选对话框选择；-1 清除选择。
	 *
	 * @param index 从 0 开始的项目位置，或 -1。
	 */
	@SimpleFunction
	void 置单选位置(int index);

	/**
	 * 更新正在显示的多选对话框选择，空数组清除选择。
	 *
	 * @param indices 全部选中位置，均从 0 开始。
	 */
	@SimpleFunction
	void 置多选位置数组(int[] indices);

	/**
	 * 列表或选择对话框项目被长按时触发，不自动提交或关闭。
	 *
	 * @param index 从 0 开始的项目位置。
	 * @param text 当前项目文字。
	 */
	@SimpleEvent
	void 项目被长按(int index, String text);

	/**
	 * 显示带图标列表，保留禁用项目、确认前校验和长按事件。
	 *
	 * @param title 对话框标题。
	 * @param items 项目文字数组。
	 * @param images 与项目数量相同的图标数组；空文本隐藏图标。
	 */
	@SimpleFunction
	void 显示图标列表(String title, String[] items, String[] images);

	/**
	 * 按文字方向靠起点。
	 */
	@SimpleDataElement
	int 对齐_起点 = 0;

	/**
	 * 居中。
	 */
	@SimpleDataElement
	int 对齐_居中 = 1;

	/**
	 * 按文字方向靠终点。
	 */
	@SimpleDataElement
	int 对齐_终点 = 2;

	/**
	 * 确认按钮。
	 */
	@SimpleDataElement
	int 按钮_确认 = 0;

	/**
	 * 取消按钮。
	 */
	@SimpleDataElement
	int 按钮_取消 = 1;

	/**
	 * 中立按钮。
	 */
	@SimpleDataElement
	int 按钮_中立 = 2;

}
