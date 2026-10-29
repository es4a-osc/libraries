package simple.library.wsd.agentweb;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.UsesPermissions;
import simple.runtime.components.可视组件;
import simple.runtime.parameters.BooleanReferenceParameter;

/**
 * 基于系统 WebView 与 AgentWeb 的浏览器，提供导航、脚本、文件交互和视频全屏。
 * 界面线程调用；所属窗口离开时暂停浏览、退出全屏并取消未完成的文件交互及下载。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public interface 浏览器 extends 可视组件 {

	/**
	 * 网页发起主框架导航时触发，可同步屏蔽；主动加载和部分 POST 导航不会保证触发。
	 *
	 * @param url 即将跳转的网址。
	 * @param block 是否屏蔽，初始为假。
	 */
	@SimpleEvent
	void 即将跳转(String url, BooleanReferenceParameter block);

	/**
	 * 主页面开始载入。
	 *
	 * @param url 载入网址。
	 */
	@SimpleEvent
	void 开始载入(String url);

	/**
	 * 主页面完成一轮载入，不保证所有子资源成功。
	 *
	 * @param url 页面网址。
	 */
	@SimpleEvent
	void 载入完毕(String url);

	/**
	 * 主页面发生网络或证书错误。
	 *
	 * @param code WebView 错误码，证书错误使用 -11。
	 * @param message 错误说明。
	 * @param url 失败网址。
	 */
	@SimpleEvent
	void 载入失败(int code, String message, String url);

	/**
	 * 主页面返回 HTTP 错误响应。
	 *
	 * @param status HTTP 状态码。
	 * @param url 请求网址。
	 */
	@SimpleEvent
	void HTTP错误(int status, String url);

	/**
	 * 页面标题更新。
	 *
	 * @param title 新标题。
	 */
	@SimpleEvent
	void 标题改变(String title);

	/**
	 * 页面载入进度更新。
	 *
	 * @param progress 0 至 100。
	 */
	@SimpleEvent
	void 进度改变(int progress);

	/**
	 * JavaScript 执行结果返回。
	 *
	 * @param mark 调用时的自定义标记。
	 * @param result WebView 返回的 JSON 文本。
	 */
	@SimpleEvent
	void 脚本返回(String mark, String result);

	/**
	 * 网页向已注册的消息接口发送文本，事件在界面线程触发。
	 *
	 * @param name 消息接口名称。
	 * @param message 网页传来的文本。
	 */
	@SimpleEvent
	void 接口调用(String name, String message);

	/**
	 * 网页文件选择结束，取消时数组为空；内容 URI 只表示用户授予的访问，不等于真实文件路径。
	 *
	 * @param uris 选中的 URI 字符串。
	 */
	@SimpleEvent
	void 文件选择完成(String[] uris);

	/**
	 * 网页请求下载文件；应用可在事件中调用下载文件以确定目标路径。
	 *
	 * @param url 下载网址。
	 * @param userAgent 网页 User-Agent。
	 * @param disposition Content-Disposition。
	 * @param mime MIME 类型。
	 * @param length 已知字节长度，未知时可能为 -1。
	 */
	@SimpleEvent
	void 下载已请求(String url, String userAgent, String disposition, String mime, long length);

	/**
	 * 本组件的下载进度更新，事件在界面线程触发。
	 *
	 * @param url 下载网址。
	 * @param received 已下载字节数。
	 * @param total 总字节数，未知时为 -1。
	 */
	@SimpleEvent
	void 下载进度(String url, long received, long total);

	/**
	 * 文件下载成功，已关闭写入。
	 *
	 * @param url 下载网址。
	 * @param path 实际目标文件完整路径。
	 */
	@SimpleEvent
	void 下载完成(String url, String path);

	/**
	 * 本组件下载失败，不把部分文件当作成功。
	 *
	 * @param url 下载网址。
	 * @param message 错误说明。
	 */
	@SimpleEvent
	void 下载失败(String url, String message);

	/**
	 * 本组件任务已接受取消请求，不保证本轮之前写入的部分文件已删除。
	 *
	 * @param url 下载网址。
	 */
	@SimpleEvent
	void 下载已取消(String url);

	/**
	 * 视频进入或退出全屏。
	 *
	 * @param fullscreen 是否正在全屏。
	 */
	@SimpleEvent
	void 全屏改变(boolean fullscreen);

	/**
	 * 异步 Cookie 写入完成。
	 *
	 * @param url Cookie 所属网址。
	 * @param success 是否成功写入。
	 */
	@SimpleEvent
	void Cookie已写入(String url, boolean success);

	/**
	 * 应用 WebView 的 Cookie 已完成清除。
	 */
	@SimpleEvent
	void Cookie已清除();

	/**
	 * 加载 HTTP、HTTPS、资产或应用可访问的本地页面；不打开外部应用协议。
	 *
	 * @param url 完整网址；本地资产使用 file:///android_asset/文件名。
	 */
	@SimpleFunction
	void 加载网址(String url);

	/**
	 * 以给定基准网址加载 UTF-8 HTML；基准网址决定相对链接与页面源。
	 *
	 * @param html HTML 内容。
	 * @param baseUrl 基准网址，空文本使用 about:blank。
	 */
	@SimpleFunction
	void 加载HTML(String html, String baseUrl);

	/**
	 * 优先退出视频全屏，否则返回上一网页；返回真表示已处理。
	 *
	 * @return 是否处理了返回操作。
	 */
	@SimpleFunction
	boolean 后退();

	/**
	 * 前进到下一网页，没有下一页时返回假。
	 *
	 * @return 是否执行前进。
	 */
	@SimpleFunction
	boolean 前进();

	/**
	 * 重新加载当前页面。
	 */
	@SimpleFunction
	void 刷新();

	/**
	 * 停止当前页面继续载入。
	 */
	@SimpleFunction
	void 停止载入();

	/**
	 * 查询当前网页是否存在上一条历史记录。
	 *
	 * @return 是否存在上一页。
	 */
	@SimpleFunction
	boolean 可否后退();

	/**
	 * 查询当前网页是否存在下一条历史记录。
	 *
	 * @return 是否存在下一页。
	 */
	@SimpleFunction
	boolean 可否前进();

	/**
	 * 取得当前页面地址，尚未载入时返回空文本。
	 *
	 * @return 当前网址。
	 */
	@SimpleFunction
	String 取地址();

	/**
	 * 取得当前页面标题，未知时返回空文本。
	 *
	 * @return 当前标题。
	 */
	@SimpleFunction
	String 取标题();

	/**
	 * 启用或关闭网页 JavaScript。默认启用；注册的接口只应向可信页面提供。
	 *
	 * @param enabled 是否启用。
	 */
	@SimpleFunction
	void 置JavaScript(boolean enabled);

	/**
	 * 设置当前浏览框的 User-Agent。
	 *
	 * @param value 完整 User-Agent。
	 */
	@SimpleFunction
	void 置UserAgent(String value);

	/**
	 * 读取当前浏览框的 User-Agent。
	 *
	 * @return 当前 User-Agent。
	 */
	@SimpleFunction
	String 取UserAgent();

	/**
	 * 配置手势缩放与屏幕缩放按钮。
	 *
	 * @param enabled 是否支持缩放。
	 * @param buttons 是否显示缩放按钮。
	 */
	@SimpleFunction
	void 置缩放(boolean enabled, boolean buttons);

	/**
	 * 设置网页缓存策略。
	 *
	 * @param mode -1 默认；1 优先缓存；2 仅网络；3 仅缓存。
	 */
	@SimpleFunction
	void 置缓存模式(int mode);

	/**
	 * 清除当前应用的 WebView 资源缓存，不清除 Cookie；资源缓存由应用内浏览框共享。
	 *
	 * @param disk 是否同时清除磁盘缓存。
	 */
	@SimpleFunction
	void 清除缓存(boolean disk);

	/**
	 * 清除当前浏览框的导航历史。
	 */
	@SimpleFunction
	void 清除历史();

	/**
	 * 为网址写入一条 Cookie，完成后触发 Cookie已写入。Cookie 由同应用的 WebView 共享。
	 *
	 * @param url Cookie 所属 HTTP 或 HTTPS 网址。
	 * @param value Set-Cookie 格式内容。
	 */
	@SimpleFunction
	void 置Cookie(String url, String value);

	/**
	 * 取得网址的 Cookie 字符串；没有时返回空文本。
	 *
	 * @param url HTTP 或 HTTPS 网址。
	 * @return Cookie 请求头内容。
	 */
	@SimpleFunction
	String 取Cookie(String url);

	/**
	 * 异步清除应用所有 WebView 的 Cookie，完成后触发 Cookie已清除。
	 */
	@SimpleFunction
	void 清除Cookie();

	/**
	 * 配置当前浏览框是否接受第三方 Cookie，默认关闭。
	 *
	 * @param enabled 是否接受。
	 */
	@SimpleFunction
	void 置第三方Cookie(boolean enabled);

	/**
	 * 执行 JavaScript 并异步返回 JSON 格式结果；页面变化或组件离开后不派发旧结果。
	 *
	 * @param script JavaScript 表达式或语句。
	 * @param mark 用于对应脚本返回事件的自定义标记。
	 */
	@SimpleFunction
	void 执行脚本(String script, String mark);

	/**
	 * 注册固定消息接口，网页通过 window.名称.postMessage(文本) 触发接口调用。页面及其子框架均可访问，仅用于可信内容。
	 *
	 * @param name 合法 JavaScript 标识符。
	 */
	@SimpleFunction
	void 注册接口(String name);

	/**
	 * 移除此前注册的消息接口。
	 *
	 * @param name 注册名称。
	 */
	@SimpleFunction
	void 移除接口(String name);

	/**
	 * 下载到应用可写文件；同网址已有任务或目标已存在时拒绝，不自动打开文件。离开所属窗口时取消本组件的未完成任务。
	 *
	 * @param url HTTP 或 HTTPS 下载地址。
	 * @param path 完整可写路径；相对路径以应用内部文件目录为基准，父目录必须存在。
	 */
	@SimpleFunction
	void 下载文件(String url, String path);

	/**
	 * 取消本组件指定网址的任务，不取消其它浏览框的任务。
	 *
	 * @param url 本组件发起的下载网址。
	 * @return 是否找到并取消了任务。
	 */
	@SimpleFunction
	boolean 取消下载(String url);

	/**
	 * 设置 AgentWeb 顶部进度指示条的 ARGB 颜色。
	 *
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	void 置进度颜色(int color);

	/**
	 * 查询是否正在显示本浏览框的视频全屏视图。
	 *
	 * @return 是否全屏。
	 */
	@SimpleFunction
	boolean 是否全屏();

	/**
	 * 退出当前视频全屏并恢复原有系统栏状态。
	 */
	@SimpleFunction
	void 退出全屏();

	/**
	 * 查询浏览框是否已释放；此查询在销毁后仍可调用。
	 *
	 * @return 是否已销毁。
	 */
	@SimpleFunction
	boolean 是否已销毁();
}
