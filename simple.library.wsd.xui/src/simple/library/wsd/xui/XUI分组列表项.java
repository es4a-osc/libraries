package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.可视组件;

/**
 * XUI分组列表项组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI分组列表项 extends XUI视图组件 {

	/**
	 * 设置红点位置。
	 *
	 * @param redDotPosition 红点位置。
	 */
	@SimpleFunction
	void 置红点位置(int redDotPosition);

	/**
	 * 取得详情文本。
	 *
	 * @return 详情文本。
	 */
	@SimpleFunction
	String 取详情文本();

	/**
	 * 设置详情文本。
	 *
	 * @param text 文本。
	 */
	@SimpleFunction
	void 置详情文本(String text);

	/**
	 * 取得附属类型。
	 *
	 * @return 附属类型。
	 */
	@SimpleFunction
	int 取附属类型();

	/**
	 * 设置右侧附属内容类型，0 无，1 箭头，2 开关，3 自定义。
	 *
	 * @param type 附属类型常量。
	 */
	@SimpleFunction
	void 置附属类型(int type);

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
	 * 设置左侧本地图标，空文本移除图标。
	 *
	 * @param image 资源标识、资产文件名或本地路径。
	 */
	@SimpleFunction
	void 置图标(String image);

	/**
	 * 设置红点是否显示。
	 *
	 * @param show 是否显示。
	 */
	@SimpleFunction
	void 显示红点(boolean show);

	/**
	 * 设置新标记是否显示。
	 *
	 * @param show 是否显示。
	 */
	@SimpleFunction
	void 显示新标记(boolean show);

	/**
	 * 设置附属开关状态；仅在附属类型为开关时可用。
	 *
	 * @param checked 是否选中。
	 * @param silent 是否不发送开关改变事件。
	 */
	@SimpleFunction
	void 置开关选中(boolean checked, boolean silent);

	/**
	 * 取得附属开关状态；仅在附属类型为开关时可用。
	 *
	 * @return 附属开关状态；仅在附属类型为开关时可用。
	 */
	@SimpleFunction
	boolean 取开关选中();

	/**
	 * 附属开关的选中状态改变时触发。
	 *
	 * @param checked 当前是否选中。
	 */
	@SimpleEvent
	void 开关改变(boolean checked);

	/**
	 * 暂借同一窗口内的组件作为附属内容，切换类型或销毁时归还。
	 *
	 * @param component 同一窗口内的可视组件，不得为自身或其祖先。
	 */
	@SimpleFunction
	void 置自定义附属(可视组件 component);

	/**
	 * 附属类型，无。
	 */
	@SimpleDataElement
	int 附属类型_无 = 0;

	/**
	 * 附属类型，箭头。
	 */
	@SimpleDataElement
	int 附属类型_箭头 = 1;

	/**
	 * 附属类型，开关。
	 */
	@SimpleDataElement
	int 附属类型_开关 = 2;

	/**
	 * 附属类型，自定义。
	 */
	@SimpleDataElement
	int 附属类型_自定义 = 3;

}
