package simple.library.wsd.xupdate;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;
import simple.runtime.annotations.UsesPermissions;
import simple.runtime.components.组件;

/**
 * 独立的应用版本检查、更新提示、APK 下载校验与系统安装请求。
 * 组件调用和事件均在界面线程；默认离开窗口取消，显式转入后台的文件任务由服务继续。
 * 可通过下载器接口接入其它传输实现，默认使用普通流式下载。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
@UsesPermissions(permissionNames =
	// 允许检查版本和下载更新 APK。
	"android.permission.INTERNET," +
	// 允许核对“仅 Wi-Fi”配置所需的当前网络状态。
	"android.permission.ACCESS_NETWORK_STATE," +
	// 允许向系统请求安装更新，仍需用户授权和确认。
	"android.permission.REQUEST_INSTALL_PACKAGES," +
	// 允许用户选择后台下载后维持带通知的前台服务。
	"android.permission.FOREGROUND_SERVICE"
)
public interface 更新器 extends 组件 {

	/**
	 * 状态：空闲。
	 */
	@SimpleDataElement
	int 状态_空闲 = 0;

	/**
	 * 状态：检查。
	 */
	@SimpleDataElement
	int 状态_检查 = 1;

	/**
	 * 状态：提示。
	 */
	@SimpleDataElement
	int 状态_提示 = 2;

	/**
	 * 状态：下载。
	 */
	@SimpleDataElement
	int 状态_下载 = 3;

	/**
	 * 状态：就绪。
	 */
	@SimpleDataElement
	int 状态_就绪 = 4;

	/**
	 * 状态：授权。
	 */
	@SimpleDataElement
	int 状态_授权 = 5;

	/**
	 * 状态：安装。
	 */
	@SimpleDataElement
	int 状态_安装 = 6;

	/**
	 * 状态：暂停；等待已暂停事件后可继续下载。
	 */
	@SimpleDataElement
	int 状态_暂停 = 7;

	/**
	 * 阶段：检查。
	 */
	@SimpleDataElement
	int 阶段_检查 = 1;

	/**
	 * 阶段：下载。
	 */
	@SimpleDataElement
	int 阶段_下载 = 2;

	/**
	 * 阶段：安装。
	 */
	@SimpleDataElement
	int 阶段_安装 = 3;

	/**
	 * 异步检查版本；已有任务时拒绝启动第二个任务。
	 *
	 * @param address 版本检查接口的完整 HTTP 或 HTTPS 地址。
	 */
	@SimpleFunction
	void 检查更新(String address);

	/**
	 * 解析已有响应；默认解析器比较本机版本，自定义解析器自行决定是否更新。
	 *
	 * @param response 默认 XUpdate JSON，或由解析响应事件处理的自定义内容。
	 */
	@SimpleFunction
	void 解析更新(String response);

	/**
	 * 显示 XUpdate 原生更新提示，使用传入信息的快照。
	 *
	 * @param info 包含版本名称、下载地址和更新内容的信息。
	 */
	@SimpleFunction
	void 显示更新(更新信息 info);

	/**
	 * 直接下载，不要求存在更高版本；进度和已校验文件通过事件返回。
	 *
	 * @param info 具有下载地址和可选校验信息的版本信息。
	 */
	@SimpleFunction
	void 下载更新(更新信息 info);

	/**
	 * 请求系统安装；需要时先打开安装来源授权页，恢复后继续。
	 *
	 * @param path 可读取的本地 APK 绝对路径。
	 */
	@SimpleFunction
	void 安装更新(String path);

	/**
	 * 取消请求、下载或待授权安装并关闭提示，移除未完成续传记录；显式后台下载同样取消。
	 */
	@SimpleFunction
	void 取消();

	/**
	 * 暂停已接入可暂停下载器的任务，保留当前文件及请求配置；停止后触发已暂停。
	 * 尚未下载或下载器不支持暂停时拒绝调用。
	 */
	@SimpleFunction
	void 暂停下载();

	/**
	 * 恢复已暂停或网络失败的续传任务；沿用开始下载时的地址、请求头和安装配置。
	 * 须等待已暂停事件或可恢复的失败事件，不得在停止尚未完成时调用。
	 */
	@SimpleFunction
	void 继续下载();

	/**
	 * 移除本更新器的已下载文件和续传记录，不影响其它实例。
	 * 下载或安装进行中拒绝调用，请先暂停或取消。
	 */
	@SimpleFunction
	void 清理缓存();

	/**
	 * 新增或替换版本检查参数；GET 使用查询串，POST 使用表单。
	 *
	 * @param name 非空参数名。
	 * @param value 参数文本，使用 UTF-8 编码。
	 */
	@SimpleFunction
	void 置请求参数(String name, String value);

	/**
	 * 新增或替换检查与下载请求头；配置在任务开始时固定。
	 *
	 * @param name 合法 HTTP 请求头名称。
	 * @param value 不含换行的请求头值。
	 */
	@SimpleFunction
	void 置请求头(String name, String value);

	/**
	 * 清空请求参数与请求头，不改变其它配置。
	 */
	@SimpleFunction
	void 清空请求配置();

	/**
	 * 设置下次更新框的颜色、顶部图片及尺寸。
	 *
	 * @param themeColor ARGB 颜色；-1 使用上游默认。
	 * @param textColor 按钮文字颜色；0 使用上游默认。
	 * @param topImage drawable 资源标识；空文本使用上游默认。
	 * @param widthRatio 屏幕宽度比例，0 至 1；0 使用默认。
	 * @param heightRatio 屏幕高度比例，0 至 1；0 适应内容。
	 */
	@SimpleFunction
	void 置提示样式(int themeColor, int textColor, String topImage, float widthRatio, float heightRatio);

	/**
	 * 清除 XUpdate 为当前应用记录的忽略版本。
	 */
	@SimpleFunction
	void 清除忽略版本();

	/**
	 * 替换本实例的检查器；仅可在空闲时设置，空对象恢复默认实现。
	 *
	 * @param value 自定义检查器。
	 */
	@SimpleFunction
	void 置检查器(检查器 value);

	/**
	 * 替换本实例的解析器；仅可在空闲时设置，空对象恢复默认实现。
	 *
	 * @param value 自定义解析器。
	 */
	@SimpleFunction
	void 置解析器(解析器 value);

	/**
	 * 替换本实例的提示器；仅可在空闲时设置，空对象恢复默认实现。
	 *
	 * @param value 自定义提示器。
	 */
	@SimpleFunction
	void 置提示器(提示器 value);

	/**
	 * 替换本实例的下载器；仅可在空闲时设置，空对象恢复默认实现。
	 *
	 * @param value 自定义下载器。
	 */
	@SimpleFunction
	void 置下载器(下载器 value);

	/**
	 * 替换本实例的网络服务；仅可在空闲时设置，空对象恢复默认实现。
	 *
	 * @param value 自定义网络服务。
	 */
	@SimpleFunction
	void 置网络服务(网络服务 value);

	/**
	 * 替换本实例的校验器；仅可在空闲时设置，空对象恢复默认实现。
	 *
	 * @param value 自定义校验器。
	 */
	@SimpleFunction
	void 置校验器(校验器 value);

	/**
	 * 替换本实例的安装器；仅可在空闲时设置，空对象恢复默认实现。
	 *
	 * @param value 自定义安装器。
	 */
	@SimpleFunction
	void 置安装器(安装器 value);

	/**
	 * 取得发现更新后是否跳过提示直接下载，默认关闭。
	 *
	 * @return 是否自动下载。
	 */
	@SimpleProperty
	boolean 自动下载();

	/**
	 * 设置是否自动下载；是否请求安装仍由下载后安装独立控制。
	 *
	 * @param value 是否自动下载。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	void 自动下载(boolean value);

	/** 检查请求已经结束；之后仍可能解析、提示或下载，不代表更新完成。 */
	@SimpleEvent
	void 检查结束();

	/** 下载中关闭提示并转入后台通知；不表示允许静默安装。 */
	@SimpleFunction
	void 转入后台();

	/**
	 * 取得是否用 POST 表单检查；默认为 GET。
	 *
	 * @return 是否用 POST 表单检查；默认为 GET。
	 */
	@SimpleProperty
	boolean 使用POST();

	/**
	 * 设置是否用 POST 表单检查；默认为 GET。
	 *
	 * @param value 是否用 POST 表单检查；默认为 GET。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	void 使用POST(boolean value);

	/**
	 * 取得 POST 是否发送 JSON 对象；默认为假，只在使用 POST 时生效。
	 *
	 * @return 是否使用 JSON 请求体。
	 */
	@SimpleProperty
	boolean POST使用JSON();

	/**
	 * 设置 POST 是否发送 UTF-8 JSON 对象，参数值保持文本；假表示表单。
	 *
	 * @param value 是否使用 JSON 请求体。
	 */
	@SimpleProperty(type = SimpleProperty.PROPERTY_TYPE_BOOLEAN, initializer = "False")
	void POST使用JSON(boolean value);

	/**
	 * 取得是否通过解析响应事件填写更新信息。
	 *
	 * @return 是否通过解析响应事件填写更新信息。
	 */
	@SimpleProperty
	boolean 自定义解析();

	/**
	 * 设置是否通过解析响应事件填写更新信息。
	 *
	 * @param value 是否通过解析响应事件填写更新信息。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	void 自定义解析(boolean value);

	/**
	 * 取得检查发现更新后是否自动显示更新框。
	 *
	 * @return 检查发现更新后是否自动显示更新框。
	 */
	@SimpleProperty
	boolean 自动提示();

	/**
	 * 设置检查发现更新后是否自动显示更新框。
	 *
	 * @param value 检查发现更新后是否自动显示更新框。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 自动提示(boolean value);

	/**
	 * 取得下载完成后是否请求系统安装；设置为假可先取得 APK。
	 *
	 * @return 下载完成后是否请求系统安装；设置为假可先取得 APK。
	 */
	@SimpleProperty
	boolean 下载后安装();

	/**
	 * 设置下载完成后是否请求系统安装；设置为假可先取得 APK。
	 *
	 * @param value 下载完成后是否请求系统安装；设置为假可先取得 APK。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 下载后安装(boolean value);

	/**
	 * 取得开始检查和下载时是否要求当前网络为 Wi-Fi；不自动切换网络。
	 *
	 * @return 开始检查和下载时是否要求当前网络为 Wi-Fi；不自动切换网络。
	 */
	@SimpleProperty
	boolean 仅WiFi();

	/**
	 * 设置开始检查和下载时是否要求当前网络为 Wi-Fi；不自动切换网络。
	 *
	 * @param value 开始检查和下载时是否要求当前网络为 Wi-Fi；不自动切换网络。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	void 仅WiFi(boolean value);

	/**
	 * 取得连接及连续读取的超时毫秒数，范围 1000 至 120000。
	 *
	 * @return 连接及连续读取的超时毫秒数，范围 1000 至 120000。
	 */
	@SimpleProperty
	int 请求超时();

	/**
	 * 设置连接及连续读取的超时毫秒数，范围 1000 至 120000。
	 *
	 * @param value 连接及连续读取的超时毫秒数，范围 1000 至 120000。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "15000"
	)
	void 请求超时(int value);

	/**
	 * 当前状态，使用状态_* 常量比较。
	 *
	 * @return 当前状态，使用状态_* 常量比较。
	 */
	@SimpleProperty
	int 状态();

	/**
	 * 最后一次下载校验通过的私有 APK 路径，尚无文件时为空文本。
	 *
	 * @return 最后一次下载校验通过的私有 APK 路径，尚无文件时为空文本。
	 */
	@SimpleProperty
	String 已下载文件();

	/**
	 * 版本请求即将发出，事件在界面线程触发。
	 */
	@SimpleEvent
	void 检查开始();

	/**
	 * 启用自定义解析后触发；默认有更新为假，必须明确填写有效信息。
	 *
	 * @param response 原始响应内容。
	 * @param info 在本事件内填写；事件返回后读取其快照。
	 */
	@SimpleEvent
	void 解析响应(String response, 更新信息 info);

	/**
	 * 存在高于本机的未忽略版本，之后按自动提示配置决定是否显示更新框。
	 *
	 * @param info 独立的版本信息，修改不影响当前提示或下载。
	 */
	@SimpleEvent
	void 发现更新(更新信息 info);

	/**
	 * 服务端无更新、本机版本已足够或此版本被用户忽略。
	 */
	@SimpleEvent
	void 无需更新();

	/**
	 * 下载任务已启动，进度事件在界面线程返回。
	 */
	@SimpleEvent
	void 下载开始();

	/**
	 * 下载期间的进度；只有写入完成且校验通过后才报告 100%。
	 *
	 * @param received 已接收的字节数。
	 * @param total 预期字节数；未知时为零。
	 * @param percent 0 至 100；总大小未知时为 -1。
	 */
	@SimpleEvent
	void 下载进度(long received, long total, int percent);

	/**
	 * APK 下载和可选大小、MD5 校验完成；不表示已经安装。
	 *
	 * @param path 已写入完成且校验通过的私有缓存 APK 绝对路径。
	 */
	@SimpleEvent
	void 下载完成(String path);

	/**
	 * 已打开允许此来源安装应用的系统设置；拒绝后报告失败，可重新请求。
	 *
	 * @param path 等待本应用安装来源授权的已校验 APK。
	 */
	@SimpleEvent
	void 安装授权(String path);

	/**
	 * 系统安装界面已打开；用户仍可拒绝或取消，不表示安装成功。
	 *
	 * @param path 交给系统安装器的 APK 路径。
	 */
	@SimpleEvent
	void 已请求安装(String path);

	/**
	 * 下载已经停止且续传记录已保留，可以调用继续下载；不表示下载完成。
	 */
	@SimpleEvent
	void 已暂停();

	/**
	 * 活动任务被用户或程序取消；切换窗口时静默释放，不回调旧窗口。
	 */
	@SimpleEvent
	void 已取消();

	/**
	 * 请求、解析、下载、校验或安装请求失败。
	 *
	 * @param stage 阶段_检查、阶段_下载或阶段_安装。
	 * @param message 失败原因，任务已停止；可修正配置后重试。
	 */
	@SimpleEvent
	void 失败(int stage, String message);
}
