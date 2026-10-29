package simple.library.wsd.alerter;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;
import simple.runtime.annotations.UsesPermissions;
import simple.runtime.components.组件;

/**
 * 在当前窗口内显示标题、正文、图标、进度和操作按钮的提示条。
 *
 * <p>所有成员在界面线程调用。配置用于下次显示，更新标题和更新内容可同时修改
 * 当前提示条。同一窗口同时保留一个提示条，新显示替换旧显示并通知旧实例隐藏。
 * 显示立即返回；进入动画结束后触发被显示，移除后触发被隐藏。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
@SimpleComponent
@UsesPermissions(permissionNames = "android.permission.VIBRATE")
public interface 提示条 extends 组件 {

	/* 组件事件 */

	/**
	 * 本次提示条完成进入动画后触发。
	 */
	@SimpleEvent
	void 被显示();

	/**
	 * 本次提示条自动关闭、程序关闭、滑动关闭或被另一提示条替换后触发一次；销毁不派发。
	 */
	@SimpleEvent
	void 被隐藏();

	/**
	 * 用户点击提示条主体时触发；是否随后关闭由本次显示采用的单击关闭设置决定。
	 */
	@SimpleEvent
	void 被单击();

	/**
	 * 用户点击操作按钮时触发；不会自动关闭，调用关闭方法可结束显示。
	 *
	 * @param id 添加按钮时指定的标记。
	 */
	@SimpleEvent
	void 按钮被单击(int id);

	/* 下次显示采用的配置 */

	/**
	 * 读取标题配置。
	 *
	 * @return 标题文本，空文本不显示标题。
	 */
	@SimpleProperty
	String 标题();

	/**
	 * 设置下次显示使用的标题。
	 *
	 * @param value 标题文本，空文本不显示标题。
	 * @throws IllegalArgumentException 参数不符合约定。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"\""
	)
	void 标题(String value);

	/**
	 * 读取内容配置。
	 *
	 * @return 正文文本，空文本不显示正文。
	 */
	@SimpleProperty
	String 内容();

	/**
	 * 设置下次显示使用的内容。
	 *
	 * @param value 正文文本，空文本不显示正文。
	 * @throws IllegalArgumentException 参数不符合约定。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"\""
	)
	void 内容(String value);

	/**
	 * 读取显示时间配置。
	 *
	 * @return 自动关闭前的显示时间，单位毫秒，不得小于零；始终显示开启时不自动关闭。
	 */
	@SimpleProperty
	long 显示时间();

	/**
	 * 设置下次显示使用的显示时间。
	 *
	 * @param value 自动关闭前的显示时间，单位毫秒，不得小于零；始终显示开启时不自动关闭。
	 * @throws IllegalArgumentException 参数不符合约定。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_LONG,
		initializer = "3000"
	)
	void 显示时间(long value);

	/**
	 * 读取背景颜色配置。
	 *
	 * @return 背景 ARGB 颜色；背景图片非空时优先使用图片。
	 */
	@SimpleProperty
	int 背景颜色();

	/**
	 * 设置下次显示使用的背景颜色。
	 *
	 * @param value 背景 ARGB 颜色；背景图片非空时优先使用图片。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_COLOR,
		initializer = "-12434878"
	)
	void 背景颜色(int value);

	/**
	 * 读取背景图片配置。
	 *
	 * @return 背景图片，支持资源编号文本、@drawable/名称、资产文件名及有读取权限的绝对文件路径；空文本使用背景颜色。
	 */
	@SimpleProperty
	String 背景图片();

	/**
	 * 设置下次显示使用的背景图片。
	 *
	 * @param value 背景图片，支持资源编号文本、@drawable/名称、资产文件名及有读取权限的绝对文件路径；空文本使用背景颜色。
	 * @throws IllegalArgumentException 参数不符合约定。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_ASSET,
		initializer = "\"\""
	)
	void 背景图片(String value);

	/**
	 * 读取图标配置。
	 *
	 * @return 左侧图片，支持资源编号文本、@drawable/名称、资产文件名及有读取权限的绝对文件路径；空文本沿用内置图标。
	 */
	@SimpleProperty
	String 图标();

	/**
	 * 设置下次显示使用的图标。
	 *
	 * @param value 左侧图片，支持资源编号文本、@drawable/名称、资产文件名及有读取权限的绝对文件路径；空文本沿用内置图标。
	 * @throws IllegalArgumentException 参数不符合约定。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_ASSET,
		initializer = "\"\""
	)
	void 图标(String value);

	/**
	 * 读取右侧图标配置。
	 *
	 * @return 右侧图片，图片标识规则与图标相同；是否显示由显示右侧图标决定。
	 */
	@SimpleProperty
	String 右侧图标();

	/**
	 * 设置下次显示使用的右侧图标。
	 *
	 * @param value 右侧图片，图片标识规则与图标相同；是否显示由显示右侧图标决定。
	 * @throws IllegalArgumentException 参数不符合约定。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_ASSET,
		initializer = "\"\""
	)
	void 右侧图标(String value);

	/**
	 * 读取图标颜色配置。
	 *
	 * @return 左侧图标的 ARGB 着色。
	 */
	@SimpleProperty
	int 图标颜色();

	/**
	 * 设置下次显示使用的图标颜色。
	 *
	 * @param value 左侧图标的 ARGB 着色。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_COLOR,
		initializer = "-1"
	)
	void 图标颜色(int value);

	/**
	 * 读取右侧图标颜色配置。
	 *
	 * @return 右侧图标的 ARGB 着色。
	 */
	@SimpleProperty
	int 右侧图标颜色();

	/**
	 * 设置下次显示使用的右侧图标颜色。
	 *
	 * @param value 右侧图标的 ARGB 着色。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_COLOR,
		initializer = "-1"
	)
	void 右侧图标颜色(int value);

	/**
	 * 读取图标尺寸配置。
	 *
	 * @return 左侧图标宽高，单位 DIP；-1 沿用内置尺寸，设置后取消像素尺寸设置。
	 */
	@SimpleProperty
	int 图标尺寸();

	/**
	 * 设置下次显示使用的图标尺寸。
	 *
	 * @param value 左侧图标宽高，单位 DIP；-1 沿用内置尺寸，设置后取消像素尺寸设置。
	 * @throws IllegalArgumentException 参数不符合约定。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "-1"
	)
	void 图标尺寸(int value);

	/**
	 * 读取右侧图标尺寸配置。
	 *
	 * @return 右侧图标宽高，单位 DIP；-1 沿用内置尺寸，设置后取消像素尺寸设置。
	 */
	@SimpleProperty
	int 右侧图标尺寸();

	/**
	 * 设置下次显示使用的右侧图标尺寸。
	 *
	 * @param value 右侧图标宽高，单位 DIP；-1 沿用内置尺寸，设置后取消像素尺寸设置。
	 * @throws IllegalArgumentException 参数不符合约定。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "-1"
	)
	void 右侧图标尺寸(int value);

	/**
	 * 读取显示图标配置。
	 *
	 * @return 是否显示左侧图标；显示进度开启时由进度指示替代。
	 */
	@SimpleProperty
	boolean 显示图标();

	/**
	 * 设置下次显示使用的显示图标。
	 *
	 * @param value 是否显示左侧图标；显示进度开启时由进度指示替代。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 显示图标(boolean value);

	/**
	 * 读取显示右侧图标配置。
	 *
	 * @return 是否显示右侧图标。
	 */
	@SimpleProperty
	boolean 显示右侧图标();

	/**
	 * 设置下次显示使用的显示右侧图标。
	 *
	 * @param value 是否显示右侧图标。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	void 显示右侧图标(boolean value);

	/**
	 * 读取图标动画配置。
	 *
	 * @return 是否播放左侧图标的脉冲动画。
	 */
	@SimpleProperty
	boolean 图标动画();

	/**
	 * 设置下次显示使用的图标动画。
	 *
	 * @param value 是否播放左侧图标的脉冲动画。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 图标动画(boolean value);

	/**
	 * 读取右侧图标动画配置。
	 *
	 * @return 是否播放右侧图标的脉冲动画。
	 */
	@SimpleProperty
	boolean 右侧图标动画();

	/**
	 * 设置下次显示使用的右侧图标动画。
	 *
	 * @param value 是否播放右侧图标的脉冲动画。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 右侧图标动画(boolean value);

	/**
	 * 读取单击动画配置。
	 *
	 * @return 是否显示提示条按下时的反馈动画。
	 */
	@SimpleProperty
	boolean 单击动画();

	/**
	 * 设置下次显示使用的单击动画。
	 *
	 * @param value 是否显示提示条按下时的反馈动画。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 单击动画(boolean value);

	/**
	 * 读取单击关闭配置。
	 *
	 * @return 点击提示条主体后是否关闭；先派发被单击事件，再关闭仍属于本次显示的提示条。
	 */
	@SimpleProperty
	boolean 单击关闭();

	/**
	 * 设置下次显示使用的单击关闭。
	 *
	 * @param value 点击提示条主体后是否关闭；先派发被单击事件，再关闭仍属于本次显示的提示条。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 单击关闭(boolean value);

	/**
	 * 读取始终显示配置。
	 *
	 * @return 是否持续显示，直至关闭、滑动关闭或被新的提示条替换。
	 */
	@SimpleProperty
	boolean 始终显示();

	/**
	 * 设置下次显示使用的始终显示。
	 *
	 * @param value 是否持续显示，直至关闭、滑动关闭或被新的提示条替换。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	void 始终显示(boolean value);

	/**
	 * 读取滑动关闭配置。
	 *
	 * @return 是否允许使用滑动手势关闭提示条。
	 */
	@SimpleProperty
	boolean 滑动关闭();

	/**
	 * 设置下次显示使用的滑动关闭。
	 *
	 * @param value 是否允许使用滑动手势关闭提示条。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	void 滑动关闭(boolean value);

	/**
	 * 读取启用震动配置。
	 *
	 * @return 显示时是否请求震动反馈；实际效果受设备硬件和系统设置影响。
	 */
	@SimpleProperty
	boolean 启用震动();

	/**
	 * 设置下次显示使用的启用震动。
	 *
	 * @param value 显示时是否请求震动反馈；实际效果受设备硬件和系统设置影响。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	void 启用震动(boolean value);

	/**
	 * 读取显示进度配置。
	 *
	 * @return 是否在图标区域显示进度指示。
	 */
	@SimpleProperty
	boolean 显示进度();

	/**
	 * 设置下次显示使用的显示进度。
	 *
	 * @param value 是否在图标区域显示进度指示。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	void 显示进度(boolean value);

	/**
	 * 读取进度颜色配置。
	 *
	 * @return 进度指示的 ARGB 颜色。
	 */
	@SimpleProperty
	int 进度颜色();

	/**
	 * 设置下次显示使用的进度颜色。
	 *
	 * @param value 进度指示的 ARGB 颜色。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_COLOR,
		initializer = "-1"
	)
	void 进度颜色(int value);

	/**
	 * 读取禁用外部触摸配置。
	 *
	 * @return 显示时是否阻止提示条以外区域的触摸。
	 */
	@SimpleProperty
	boolean 禁用外部触摸();

	/**
	 * 设置下次显示使用的禁用外部触摸。
	 *
	 * @param value 显示时是否阻止提示条以外区域的触摸。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	void 禁用外部触摸(boolean value);

	/**
	 * 读取位置配置。
	 *
	 * @return 提示条在窗口中的 Android Gravity 值，48 表示顶部，80 表示底部。
	 */
	@SimpleProperty
	int 位置();

	/**
	 * 设置下次显示使用的位置。
	 *
	 * @param value 提示条在窗口中的 Android Gravity 值，48 表示顶部，80 表示底部。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_GRAVITY,
		initializer = "48"
	)
	void 位置(int value);

	/**
	 * 读取内容对齐配置。
	 *
	 * @return 提示条内容区域的 Android Gravity 值，16 表示垂直居中。
	 */
	@SimpleProperty
	int 内容对齐();

	/**
	 * 设置下次显示使用的内容对齐。
	 *
	 * @param value 提示条内容区域的 Android Gravity 值，16 表示垂直居中。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_GRAVITY,
		initializer = "16"
	)
	void 内容对齐(int value);

	/**
	 * 读取右侧图标对齐配置。
	 *
	 * @return 右侧图标的 Android Gravity 值，8388613 表示跟随文字方向的末端。
	 */
	@SimpleProperty
	int 右侧图标对齐();

	/**
	 * 设置下次显示使用的右侧图标对齐。
	 *
	 * @param value 右侧图标的 Android Gravity 值，8388613 表示跟随文字方向的末端。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_GRAVITY,
		initializer = "8388613"
	)
	void 右侧图标对齐(int value);

	/**
	 * 读取进入动画资源配置。
	 *
	 * @return 进入动画资源编号，0 沿用内置动画。
	 */
	@SimpleProperty
	int 进入动画资源();

	/**
	 * 设置下次显示使用的进入动画资源。
	 *
	 * @param value 进入动画资源编号，0 沿用内置动画。
	 * @throws IllegalArgumentException 参数不符合约定。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0"
	)
	void 进入动画资源(int value);

	/**
	 * 读取退出动画资源配置。
	 *
	 * @return 退出动画资源编号，0 沿用内置动画。
	 */
	@SimpleProperty
	int 退出动画资源();

	/**
	 * 设置下次显示使用的退出动画资源。
	 *
	 * @param value 退出动画资源编号，0 沿用内置动画。
	 * @throws IllegalArgumentException 参数不符合约定。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0"
	)
	void 退出动画资源(int value);

	/**
	 * 读取阴影高度配置。
	 *
	 * @return 背景的抬升高度，单位 DIP；-1 沿用内置值，不改变提示条布局高度。
	 */
	@SimpleProperty
	float 阴影高度();

	/**
	 * 设置下次显示使用的阴影高度。
	 *
	 * @param value 背景的抬升高度，单位 DIP；-1 沿用内置值，不改变提示条布局高度。
	 * @throws IllegalArgumentException 参数不符合约定。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "-1"
	)
	void 阴影高度(float value);

	/**
	 * 读取提示声音配置。
	 *
	 * @return 提示音 URI，空文本不播放；内容 URI 需具备读取权限，可使用 android.resource://包名/raw/资源名。
	 */
	@SimpleProperty
	String 提示声音();

	/**
	 * 设置下次显示使用的提示声音。
	 *
	 * @param value 提示音 URI，空文本不播放；内容 URI 需具备读取权限，可使用 android.resource://包名/raw/资源名。
	 * @throws IllegalArgumentException 参数不符合约定。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"\""
	)
	void 提示声音(String value);

	/**
	 * 查询本实例是否有正在等待显示、正在显示或正在退出的提示条。
	 *
	 * @return 本次显示尚未结束时为真，否则为假。
	 */
	@SimpleProperty
	boolean 正在显示();

	/* 显示、交互及扩展配置 */

	/**
	 * 使用当前配置显示提示条，立即返回；同一窗口的新显示替换旧显示。
	 *
	 * @throws IllegalStateException 不在界面线程调用、组件已销毁或宿主不能显示。
	 * @throws IllegalArgumentException 图片或资源不可读取。
	 */
	@SimpleFunction
	void 显示();

	/**
	 * 使用退出动画关闭本实例当前提示条；重复调用不重复触发被隐藏。
	 *
	 * @throws IllegalStateException 不在界面线程调用、组件已销毁或宿主不能显示。
	 */
	@SimpleFunction
	void 关闭();

	/**
	 * 为下次显示添加按钮；相同标记替换已有配置，按钮顺序保留首次添加顺序。
	 *
	 * @param id 用于按钮被单击事件的标记。
	 * @param caption 按钮显示文字。
	 * @param style 按钮样式资源编号，-1 使用内置 AlertButton，0 不指定额外样式。
	 * @throws IllegalStateException 不在界面线程调用、组件已销毁或宿主不能显示。
	 * @throws IllegalArgumentException 参数不符合约定。
	 */
	@SimpleFunction
	void 添加按钮(int id, String caption, int style);

	/**
	 * 清除下次显示的全部按钮，不修改当前界面。
	 *
	 * @throws IllegalStateException 不在界面线程调用、组件已销毁或宿主不能显示。
	 */
	@SimpleFunction
	void 清空按钮();

	/**
	 * 更新标题配置，并立即修改本实例当前提示条的标题。
	 *
	 * @param value 新的标题文本。
	 * @throws IllegalStateException 不在界面线程调用、组件已销毁或宿主不能显示。
	 * @throws IllegalArgumentException 参数不符合约定。
	 */
	@SimpleFunction
	void 更新标题(String value);

	/**
	 * 更新正文配置，并立即修改本实例当前提示条的正文。
	 *
	 * @param value 新的正文文本。
	 * @throws IllegalStateException 不在界面线程调用、组件已销毁或宿主不能显示。
	 * @throws IllegalArgumentException 参数不符合约定。
	 */
	@SimpleFunction
	void 更新内容(String value);

	/**
	 * 读取字符串资源作为下次显示的标题。
	 *
	 * @param resourceId 字符串资源编号。
	 * @throws IllegalStateException 不在界面线程调用、组件已销毁或宿主不能显示。
	 * @throws IllegalArgumentException 参数不符合约定。
	 */
	@SimpleFunction
	void 置标题资源(int resourceId);

	/**
	 * 读取字符串资源作为下次显示的正文。
	 *
	 * @param resourceId 字符串资源编号。
	 * @throws IllegalStateException 不在界面线程调用、组件已销毁或宿主不能显示。
	 * @throws IllegalArgumentException 参数不符合约定。
	 */
	@SimpleFunction
	void 置内容资源(int resourceId);

	/**
	 * 读取颜色资源作为下次显示的背景颜色。
	 *
	 * @param resourceId 颜色资源编号。
	 * @throws IllegalStateException 不在界面线程调用、组件已销毁或宿主不能显示。
	 * @throws IllegalArgumentException 参数不符合约定。
	 */
	@SimpleFunction
	void 置背景颜色资源(int resourceId);

	/**
	 * 读取颜色资源作为下次显示的进度颜色。
	 *
	 * @param resourceId 颜色资源编号。
	 * @throws IllegalStateException 不在界面线程调用、组件已销毁或宿主不能显示。
	 * @throws IllegalArgumentException 参数不符合约定。
	 */
	@SimpleFunction
	void 置进度颜色资源(int resourceId);

	/**
	 * 设置下次显示的左侧图标宽高，优先于此前的 DIP 尺寸设置。
	 *
	 * @param pixels 像素尺寸，不得小于零。
	 * @throws IllegalStateException 不在界面线程调用、组件已销毁或宿主不能显示。
	 * @throws IllegalArgumentException 参数不符合约定。
	 */
	@SimpleFunction
	void 置图标像素尺寸(int pixels);

	/**
	 * 设置下次显示的右侧图标宽高，优先于此前的 DIP 尺寸设置。
	 *
	 * @param pixels 像素尺寸，不得小于零。
	 * @throws IllegalStateException 不在界面线程调用、组件已销毁或宿主不能显示。
	 * @throws IllegalArgumentException 参数不符合约定。
	 */
	@SimpleFunction
	void 置右侧图标像素尺寸(int pixels);

	/**
	 * 设置下次显示的背景图片资源；0 恢复使用背景颜色。
	 *
	 * @param resourceId 图片资源编号，不得小于零。
	 * @throws IllegalArgumentException 资源编号小于零。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleFunction
	void 置背景图片资源(int resourceId);

	/**
	 * 设置下次显示的左侧图标资源；0 沿用内置图标。
	 *
	 * @param resourceId 图片资源编号，不得小于零。
	 * @throws IllegalArgumentException 资源编号小于零。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleFunction
	void 置图标资源(int resourceId);

	/**
	 * 设置下次显示的右侧图标资源；0 沿用内置图标。
	 *
	 * @param resourceId 图片资源编号，不得小于零。
	 * @throws IllegalArgumentException 资源编号小于零。
	 * @throws IllegalStateException 不在界面线程调用或组件已销毁。
	 */
	@SimpleFunction
	void 置右侧图标资源(int resourceId);

	/**
	 * 设置下次显示使用的标题字体。
	 *
	 * @param family 系统字体族名称、@assets/字体文件名或有读取权限的绝对文件路径；空文本使用默认字体。
	 * @param style 0 普通、1 粗体、2 斜体、3 粗斜体。
	 * @throws IllegalStateException 不在界面线程调用、组件已销毁或宿主不能显示。
	 * @throws IllegalArgumentException 参数不符合约定。
	 */
	@SimpleFunction
	void 置标题字体(String family, int style);

	/**
	 * 设置下次显示使用的内容字体。
	 *
	 * @param family 系统字体族名称、@assets/字体文件名或有读取权限的绝对文件路径；空文本使用默认字体。
	 * @param style 0 普通、1 粗体、2 斜体、3 粗斜体。
	 * @throws IllegalStateException 不在界面线程调用、组件已销毁或宿主不能显示。
	 * @throws IllegalArgumentException 参数不符合约定。
	 */
	@SimpleFunction
	void 置内容字体(String family, int style);

	/**
	 * 设置下次显示使用的按钮字体。
	 *
	 * @param family 系统字体族名称、@assets/字体文件名或有读取权限的绝对文件路径；空文本使用默认字体。
	 * @param style 0 普通、1 粗体、2 斜体、3 粗斜体。
	 * @throws IllegalStateException 不在界面线程调用、组件已销毁或宿主不能显示。
	 * @throws IllegalArgumentException 参数不符合约定。
	 */
	@SimpleFunction
	void 置按钮字体(String family, int style);

}
