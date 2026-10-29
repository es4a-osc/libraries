package simple.library.wsd.xhttp2;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;
import simple.runtime.annotations.UsesPermissions;
import simple.runtime.components.组件;

/**
 * 基于 XHttp2 的独立网络客户端；普通操作在界面线程使用，同步发送仅允许工作线程调用。
 * 所有事件在界面线程分发，离开所属窗口取消请求，销毁后不可复用。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
@UsesPermissions(permissionNames =
	// 建立 HTTP/HTTPS 连接及上传、下载内容。
	"android.permission.INTERNET"
)
public interface XHttp客户端 extends 组件 {

	/**
	 * 不读写应用响应缓存。
	 */
	@SimpleDataElement
	int 缓存_不缓存 = 0;

	/**
	 * 遵循 HTTP 缓存头；带凭据的请求不复用默认 HTTP 缓存。
	 */
	@SimpleDataElement
	int 缓存_HTTP = 1;

	/**
	 * 网络失败时读取有效缓存。
	 */
	@SimpleDataElement
	int 缓存_网络优先 = 2;

	/**
	 * 有效缓存命中时不请求网络。
	 */
	@SimpleDataElement
	int 缓存_缓存优先 = 3;

	/**
	 * 仅从网络获取，但保存成功的 HTTP 响应。
	 */
	@SimpleDataElement
	int 缓存_仅网络 = 4;

	/**
	 * 只读取有效缓存，未命中作为缓存错误。
	 */
	@SimpleDataElement
	int 缓存_仅缓存 = 5;

	/**
	 * 先返回缓存，再返回网络响应。
	 */
	@SimpleDataElement
	int 缓存_缓存及网络 = 6;

	/**
	 * 先缓存再网络，完整响应内容相同则只返回一次。
	 */
	@SimpleDataElement
	int 缓存_缓存及网络去重 = 7;

	/**
	 * 取得基础地址；相对请求地址按标准 URL 规则解析，完整地址不受影响。
	 *
	 * @return 基础地址；相对请求地址按标准 URL 规则解析，完整地址不受影响。
	 */
	@SimpleProperty
	String 基础地址();

	/**
	 * 设置基础地址；相对请求地址按标准 URL 规则解析，完整地址不受影响。
	 *
	 * @param value 基础地址；相对请求地址按标准 URL 规则解析，完整地址不受影响。
	 */
	@SimpleProperty
	void 基础地址(String value);

	/**
	 * 取得默认连接、读取和写入超时毫秒数，默认 15000，0 不限。
	 *
	 * @return 默认连接、读取和写入超时毫秒数，默认 15000，0 不限。
	 */
	@SimpleProperty
	int 默认超时();

	/**
	 * 设置默认连接、读取和写入超时毫秒数，默认 15000，0 不限。
	 *
	 * @param value 默认连接、读取和写入超时毫秒数，默认 15000，0 不限。
	 */
	@SimpleProperty
	void 默认超时(int value);

	/**
	 * 取得连接或超时失败的默认重试次数，默认零；非幂等操作须谨慎启用。
	 *
	 * @return 连接或超时失败的默认重试次数，默认零；非幂等操作须谨慎启用。
	 */
	@SimpleProperty
	int 默认重试次数();

	/**
	 * 设置连接或超时失败的默认重试次数，默认零；非幂等操作须谨慎启用。
	 *
	 * @param value 连接或超时失败的默认重试次数，默认零；非幂等操作须谨慎启用。
	 */
	@SimpleProperty
	void 默认重试次数(int value);

	/**
	 * 取得默认重试间隔毫秒数，默认 1000。
	 *
	 * @return 默认重试间隔毫秒数，默认 1000。
	 */
	@SimpleProperty
	long 默认重试间隔();

	/**
	 * 设置默认重试间隔毫秒数，默认 1000。
	 *
	 * @param value 默认重试间隔毫秒数，默认 1000。
	 */
	@SimpleProperty
	void 默认重试间隔(long value);

	/**
	 * 取得后续重试的默认等待增量毫秒数，初始零。
	 *
	 * @return 每次后续重试增加的等待毫秒数。
	 */
	@SimpleProperty
	long 默认重试递增间隔();

	/**
	 * 设置后续重试的默认等待增量；第一次使用默认重试间隔，之后每次增加该值。
	 *
	 * @param value 递增毫秒数，零不递增。
	 */
	@SimpleProperty
	void 默认重试递增间隔(long value);

	/**
	 * 取得默认响应缓存策略，初始不缓存。
	 *
	 * @return 默认响应缓存策略，初始不缓存。
	 */
	@SimpleProperty
	int 默认缓存策略();

	/**
	 * 设置默认响应缓存策略，初始不缓存。
	 *
	 * @param value 默认响应缓存策略，初始不缓存。
	 */
	@SimpleProperty
	void 默认缓存策略(int value);

	/**
	 * 取得默认应用缓存有效秒数，初始 3600；-1 不过期，0 不复用旧缓存。
	 *
	 * @return 默认应用缓存有效秒数，初始 3600；-1 不过期，0 不复用旧缓存。
	 */
	@SimpleProperty
	long 默认缓存秒数();

	/**
	 * 设置默认应用缓存有效秒数，初始 3600；-1 不过期，0 不复用旧缓存。
	 *
	 * @param value 默认应用缓存有效秒数，初始 3600；-1 不过期，0 不复用旧缓存。
	 */
	@SimpleProperty
	void 默认缓存秒数(long value);

	/**
	 * 取得内存响应字节上限，默认 8 MiB；大文件应使用下载路径，不限制流式文件大小。
	 *
	 * @return 内存响应字节上限，默认 8 MiB；大文件应使用下载路径，不限制流式文件大小。
	 */
	@SimpleProperty
	long 最大响应字节();

	/**
	 * 设置内存响应字节上限，默认 8 MiB；大文件应使用下载路径，不限制流式文件大小。
	 *
	 * @param value 内存响应字节上限，默认 8 MiB；大文件应使用下载路径，不限制流式文件大小。
	 */
	@SimpleProperty
	void 最大响应字节(long value);

	/**
	 * 取得是否跟随 HTTP 重定向，默认真；跨主机遵循 OkHttp 的敏感头处理。
	 *
	 * @return 是否跟随 HTTP 重定向，默认真；跨主机遵循 OkHttp 的敏感头处理。
	 */
	@SimpleProperty
	boolean 自动重定向();

	/**
	 * 设置是否跟随 HTTP 重定向，默认真；跨主机遵循 OkHttp 的敏感头处理。
	 *
	 * @param value 是否跟随 HTTP 重定向，默认真；跨主机遵循 OkHttp 的敏感头处理。
	 */
	@SimpleProperty
	void 自动重定向(boolean value);

	/**
	 * 发送请求快照并返回实际标识；同一请求可重复提交，非空标识在运行期间不得重复。
	 *
	 * @param request 将复制保存的请求。
	 * @return 操作结果。
	 */
	@SimpleFunction
	String 发送(XHttp请求 request);

	/**
	 * 在工作线程发送并返回最后一份响应；仍在界面线程触发请求前处理及自定义解析，不派发普通响应和结束事件。
	 *
	 * @param request 请求配置。
	 * @return 操作结果。
	 */
	@SimpleFunction
	XHttp响应 同步发送(XHttp请求 request);

	/**
	 * 按输入顺序返回各请求最终结果；可串行或并行，失败即停会取消余下项目。
	 *
	 * @param requests 非空请求数组。
	 * @param serial 真为依次执行，假为并发执行。
	 * @param stopOnFailure 任一请求失败后取消剩余项目。
	 * @return 操作结果。
	 */
	@SimpleFunction
	String 批量发送(XHttp请求[] requests, boolean serial, boolean stopOnFailure);

	/**
	 * 取消运行中的请求或批次；下载已经完成原子提交或任务已结束时返回假。
	 *
	 * @param id 请求或批次标识。
	 * @return 操作结果。
	 */
	@SimpleFunction
	boolean 取消(String id);

	/**
	 * 取消当前客户端全部请求和批次。
	 */
	@SimpleFunction
	void 取消全部();

	/**
	 * 查询当前标识是否仍有任务或批次。
	 *
	 * @param id 请求或批次标识。
	 * @return 操作结果。
	 */
	@SimpleFunction
	boolean 是否运行(String id);

	/**
	 * 取得当前请求数量，不重复计算批次容器。
	 *
	 * @return 操作结果。
	 */
	@SimpleFunction
	int 取运行数量();

	/**
	 * 替换默认同名请求头；单次请求可以覆盖默认值。
	 *
	 * @param name 字段名称。
	 * @param value 字段值。
	 */
	@SimpleFunction
	void 置默认请求头(String name, String value);

	/**
	 * 删除默认同名请求头。
	 *
	 * @param name 不区分大小写的名称。
	 */
	@SimpleFunction
	void 删除默认请求头(String name);

	/**
	 * 清空默认请求头。
	 */
	@SimpleFunction
	void 清空默认请求头();

	/**
	 * 追加默认 URL 查询参数，单次参数追加在其后。
	 *
	 * @param name 字段名。
	 * @param value 参数值。
	 */
	@SimpleFunction
	void 添加默认参数(String name, String value);

	/**
	 * 清空默认 URL 查询参数。
	 */
	@SimpleFunction
	void 清空默认参数();

	/**
	 * 设置当前客户端 HTTP 代理及可选 Basic 认证；主机留空关闭代理。
	 *
	 * @param host 代理主机或 IP。
	 * @param port 代理端口。
	 * @param username 可空。
	 * @param password 可空。
	 */
	@SimpleFunction
	void 置代理(String host, int port, String username, String password);

	/**
	 * 设置 PEM/DER CA 证书，支持绝对路径、asset:// 或已获授权的 content URI；空数组且保留系统信任表示恢复默认。
	 *
	 * @param paths 可含多份证书。
	 * @param includeSystem 假表示只接受这些自定义 CA，仍验证主机名。
	 */
	@SimpleFunction
	void 置信任证书(String[] paths, boolean includeSystem);

	/**
	 * 设置双向认证的 PKCS12 证书；来源留空清除。
	 *
	 * @param path 绝对路径、asset:// 或已获授权的 content URI。
	 * @param password PKCS12 密码。
	 */
	@SimpleFunction
	void 置客户端证书(String path, String password);

	/**
	 * 切换命名 Cookie/缓存会话，仅允许无任务时调用；同名客户端明确共享状态，默认使用独立随机会话。
	 *
	 * @param name 不能为空；同名会话在应用范围内共享。
	 * @param persistent 是否将有有效期的 Cookie 保存至应用私有目录。
	 */
	@SimpleFunction
	void 置会话(String name, boolean persistent);

	/**
	 * 按指定地址解析完整 Set-Cookie 字段并保存，遵循域、路径、Secure 和过期规则。
	 *
	 * @param address Cookie 所属 HTTP/HTTPS 地址。
	 * @param cookie Set-Cookie 字段内容。
	 */
	@SimpleFunction
	void 设置Cookie(String address, String cookie);

	/**
	 * 取得当前地址适用的 Cookie 字段数组，长路径优先，不包含过期记录。
	 *
	 * @param address HTTP/HTTPS 地址。
	 * @return 操作结果。
	 */
	@SimpleFunction
	String[] 取Cookie(String address);

	/**
	 * 按名称、域、路径删除一项 Cookie，返回是否存在。
	 *
	 * @param name Cookie 名称。
	 * @param domain 不含前导点的规范域名。
	 * @param path Cookie 路径。
	 * @return 操作结果。
	 */
	@SimpleFunction
	boolean 删除Cookie(String name, String domain, String path);

	/**
	 * 清空当前会话 Cookie，持久化失败时报告错误。
	 */
	@SimpleFunction
	void 清空Cookie();

	/**
	 * 异步删除响应中给出的实际缓存键，结果通过缓存操作完成事件返回。
	 *
	 * @param key 响应.缓存键。
	 */
	@SimpleFunction
	void 删除缓存(String key);

	/**
	 * 异步清空当前会话应用响应缓存和 HTTP 缓存，结果通过缓存操作完成事件返回。
	 */
	@SimpleFunction
	void 清空缓存();

	/**
	 * 取消任务并解除窗口监听，销毁后不得再次发送。
	 */
	@SimpleFunction
	void 销毁();

	/**
	 * 发送前修改本次请求副本，可动态提供令牌、参数或签名；每次提交触发一次，重试复用该快照。
	 *
	 * @param request 可修改的本次请求副本；请求标识不可在此改变。
	 */
	@SimpleEvent
	void 请求前处理(XHttp请求 request);

	/**
	 * 本次请求完成前处理后开始执行。
	 *
	 * @param id 实际请求标识。
	 */
	@SimpleEvent
	void 请求开始(String id);

	/**
	 * 收到一份响应，缓存双发策略可能触发两次；HTTP 或解析失败也保留响应供检查。
	 *
	 * @param id 请求标识。
	 * @param response 只读响应。
	 */
	@SimpleEvent
	void 收到响应(String id, XHttp响应 response);

	/**
	 * 一份 HTTP 和业务均成功的响应已到达。
	 *
	 * @param id 请求标识。
	 * @param response 只读响应。
	 */
	@SimpleEvent
	void 请求成功(String id, XHttp响应 response);

	/**
	 * 请求失败，仍可检查状态码、错误类型、响应头和内容。
	 *
	 * @param id 请求标识。
	 * @param response 失败响应。
	 */
	@SimpleEvent
	void 请求失败(String id, XHttp响应 response);

	/**
	 * 本次请求被主动取消，随后只触发一次请求结束。
	 *
	 * @param id 请求标识。
	 */
	@SimpleEvent
	void 请求取消(String id);

	/**
	 * 本次请求不再产生响应；每次提交最多一次。
	 *
	 * @param id 请求标识。
	 */
	@SimpleEvent
	void 请求结束(String id);

	/**
	 * 上传或下载字节进度，未知总量为 -1；不能把未知总量换算为百分比。
	 *
	 * @param id 请求标识。
	 * @param upload 真为上传，假为下载。
	 * @param transferred 实际字节数。
	 * @param total 总字节数，未知为 -1。
	 */
	@SimpleEvent
	void 传输进度(String id, boolean upload, long transferred, long total);

	/**
	 * 自定义解析模式的同步事件，填写结果后返回；未处理视为解析失败。
	 *
	 * @param id 请求标识。
	 * @param response 原始 HTTP 响应。
	 * @param result 供事件填写的业务结果。
	 */
	@SimpleEvent
	void 解析响应(String id, XHttp响应 response, XHttp解析结果 result);

	/**
	 * 全部请求已结束，按输入顺序提供最终结果。
	 *
	 * @param result 包含失败、取消及提前停止的结果。
	 */
	@SimpleEvent
	void 批次完成(XHttp批结果 result);

	/**
	 * 异步删除或清空缓存后触发；空缓存键表示清空全部。
	 *
	 * @param key 实际缓存键或空文本。
	 * @param success 是否成功执行。
	 * @param error 失败原因，成功时为空。
	 */
	@SimpleEvent
	void 缓存操作完成(String key, boolean success, String error);

	/**
	 * 分别配置默认连接、读取、写入和单次尝试总超时，单位毫秒，零不限；请求单项配置可以覆盖。
	 *
	 * @param connect 连接毫秒数。
	 * @param read 读取毫秒数。
	 * @param write 写入毫秒数。
	 * @param total 单次尝试的总超时毫秒数。
	 */
	@SimpleFunction
	void 置超时(int connect, int read, int write, int total);

}
