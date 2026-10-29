package simple.library.wsd.xui;

import simple.runtime.annotations.ManifestNodes;
import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI搜索框组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
@ManifestNodes(rootXml =
	"<queries>" +
		// Android 11 包可见性：只查询实际调用的系统语音识别界面。
		"<intent><action android:name=\"android.speech.action.RECOGNIZE_SPEECH\" /></intent>" +
	"</queries>"
)
public interface XUI搜索框 extends XUI视图组件 {

	/**
	 * 设置文字颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置文字颜色(int color);

	/**
	 * 设置提示文本颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置提示文本颜色(int color);

	/**
	 * 设置提示文本。
	 *
	 * @param hint 提示文本。
	 */
	@SimpleFunction
	void 置提示文本(String hint);

	/**
	 * 设置输入类型。
	 *
	 * @param inputType 输入类型。
	 */
	@SimpleFunction
	void 置输入类型(int inputType);

	/**
	 * 设置是否允许语音搜索；调用系统识别应用，不直接录音。
	 *
	 * @param voiceSearch 语音搜索。
	 */
	@SimpleFunction
	void 置语音搜索(boolean voiceSearch);

	/**
	 * 查询设备是否提供可调用的系统语音识别界面。
	 *
	 * @return 是否支持语音搜索。
	 */
	@SimpleFunction
	boolean 是否支持语音搜索();

	/**
	 * 从当前可见组件启动语音识别；须先启用语音搜索，同一组件不可重复启动。
	 * 成功后填入识别文字并触发搜索提交；切窗或销毁后丢弃尚未返回的结果。
	 *
	 * @return 真表示已启动识别界面；假表示未启动，并触发语音搜索失败。
	 */
	@SimpleFunction
	boolean 开始语音搜索();

	/**
	 * 用户取消系统语音识别，保留原搜索词。
	 */
	@SimpleEvent
	void 语音搜索已取消();

	/**
	 * 识别界面无法启动或没有返回有效文字。
	 *
	 * @param message 失败原因。
	 */
	@SimpleEvent
	void 语音搜索失败(String message);

	/**
	 * 显示候选。
	 */
	@SimpleFunction
	void 显示候选();

	/**
	 * 设置是否点击条目后直接进行查询 Submit the query as soon as the user clicks the item。
	 *
	 * @param submit 提交。
	 */
	@SimpleFunction
	void 置提交开单击(boolean submit);

	/**
	 * 设置搜索提示信息集合 Set Adapter for suggestions list with the given suggestion array。
	 *
	 * @param suggestions 候选。
	 */
	@SimpleFunction
	void 置候选(String[] suggestions);

	/**
	 * 隐藏候选。
	 */
	@SimpleFunction
	void 隐藏候选();

	/**
	 * 是否搜索打开。
	 *
	 * @return 是否搜索打开。
	 */
	@SimpleFunction
	boolean 是否搜索打开();

	/**
	 * 设置动画毫秒。
	 *
	 * @param duration 时长。
	 */
	@SimpleFunction
	void 置动画毫秒(int duration);

	/**
	 * 显示搜索框 Open Search View. This will animate the showing of the view。
	 */
	@SimpleFunction
	void 展开搜索();

	/**
	 * 展开搜索。
	 *
	 * @param animate 动画。
	 */
	@SimpleFunction
	void 展开搜索(boolean animate);

	/**
	 * 关闭搜索框 Close search view。
	 */
	@SimpleFunction
	void 关闭搜索();

	/**
	 * 设置省略。
	 *
	 * @param ellipsize 省略。
	 */
	@SimpleFunction
	void 置省略(boolean ellipsize);

	/**
	 * 更新搜索词，可同时提交搜索。
	 *
	 * @param text 文本内容。
	 * @param submit 是否立即提交搜索。
	 */
	@SimpleFunction
	void 置搜索词(String text, boolean submit);

	/**
	 * 设置语音图标，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置语音图标(String image);

	/**
	 * 设置返回图标，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置返回图标(String image);

	/**
	 * 设置候选图标，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置候选图标(String image);

	/**
	 * 设置候选背景，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置候选背景(String image);

	/**
	 * 用户提交搜索词时触发。
	 *
	 * @param text 文本内容。
	 */
	@SimpleEvent
	void 搜索提交(String text);

	/**
	 * 搜索输入文本改变时触发。
	 *
	 * @param text 文本内容。
	 */
	@SimpleEvent
	void 搜索词改变(String text);

	/**
	 * 搜索框展开时触发。
	 */
	@SimpleEvent
	void 已展开();

	/**
	 * 搜索框收起时触发。
	 */
	@SimpleEvent
	void 已收起();

	/**
	 * 搜索候选被单击时触发。
	 *
	 * @param index 当前候选列表中从零开始的位置。
	 * @param text 候选文字。
	 */
	@SimpleEvent
	void 候选被单击(int index, String text);

}
