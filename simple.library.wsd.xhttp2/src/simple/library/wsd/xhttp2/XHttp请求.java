package simple.library.wsd.xhttp2;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 可复用的请求配置；发送前复制独立快照，后续修改不影响已经发出的请求。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XHttp请求 {

	/**
	 * 使用客户端默认缓存策略。
	 */
	@SimpleDataElement
	public static final int 缓存_默认 = -1;

	/**
	 * 不读写应用响应缓存。
	 */
	@SimpleDataElement
	public static final int 缓存_不缓存 = 0;

	/**
	 * 遵循 HTTP 缓存头；带凭据的请求不复用默认 HTTP 缓存。
	 */
	@SimpleDataElement
	public static final int 缓存_HTTP = 1;

	/**
	 * 网络失败时读取有效缓存。
	 */
	@SimpleDataElement
	public static final int 缓存_网络优先 = 2;

	/**
	 * 有效缓存命中时不请求网络。
	 */
	@SimpleDataElement
	public static final int 缓存_缓存优先 = 3;

	/**
	 * 仅从网络获取，但保存成功的 HTTP 响应。
	 */
	@SimpleDataElement
	public static final int 缓存_仅网络 = 4;

	/**
	 * 只读取有效缓存，未命中作为缓存错误。
	 */
	@SimpleDataElement
	public static final int 缓存_仅缓存 = 5;

	/**
	 * 先返回缓存，再返回网络响应。
	 */
	@SimpleDataElement
	public static final int 缓存_缓存及网络 = 6;

	/**
	 * 先缓存再网络，完整响应内容相同则只返回一次。
	 */
	@SimpleDataElement
	public static final int 缓存_缓存及网络去重 = 7;

	/**
	 * 保留原始响应，不解析业务字段。
	 */
	@SimpleDataElement
	public static final int 解析_原始 = 0;

	/**
	 * 按业务状态、消息和数据路径解析 JSON。
	 */
	@SimpleDataElement
	public static final int 解析_标准 = 1;

	/**
	 * 通过解析响应事件填写业务结果。
	 */
	@SimpleDataElement
	public static final int 解析_自定义 = 2;

	/**
	 * 使用 GET 方法。
	 */
	@SimpleDataElement
	public static final String 方法_GET = "GET";

	/**
	 * 使用 POST 方法。
	 */
	@SimpleDataElement
	public static final String 方法_POST = "POST";

	/**
	 * 使用 PUT 方法。
	 */
	@SimpleDataElement
	public static final String 方法_PUT = "PUT";

	/**
	 * 使用 PATCH 方法。
	 */
	@SimpleDataElement
	public static final String 方法_PATCH = "PATCH";

	/**
	 * 使用 DELETE 方法。
	 */
	@SimpleDataElement
	public static final String 方法_DELETE = "DELETE";

	/**
	 * 使用 HEAD 方法。
	 */
	@SimpleDataElement
	public static final String 方法_HEAD = "HEAD";

	/**
	 * 使用 OPTIONS 方法。
	 */
	@SimpleDataElement
	public static final String 方法_OPTIONS = "OPTIONS";

	String address = "";
	String method = "GET";
	String id = "";
	int timeout = -1;
	int connectTimeout = -1;
	int readTimeout = -1;
	int writeTimeout = -1;
	int callTimeout = -1;
	int retries = -1;
	long retryDelay = -1;
	long retryIncrease = -1;
	int cacheMode = -1;
	long cacheSeconds = -2;
	String cacheKey = "";
	String download = "";
	String sha256 = "";
	int parser = 0;
	int successCode = 0;
	String codePath = "code";
	String messagePath = "msg";
	String dataPath = "data";
	String mediaType = "";
	byte[] body;
	final List<String[]> headers = new ArrayList<String[]>();
	final List<String[]> query = new ArrayList<String[]>();
	final List<String[]> form = new ArrayList<String[]>();
	final List<UploadPart> files = new ArrayList<UploadPart>();

	/**
	 * 取得请求地址；允许由客户端基础地址解析的相对地址。
	 *
	 * @return 请求地址；允许由客户端基础地址解析的相对地址。
	 */
	@SimpleProperty
	public synchronized String 地址() {
		return address;
	}

	/**
	 * 设置请求地址；允许由客户端基础地址解析的相对地址。
	 *
	 * @param value 请求地址；允许由客户端基础地址解析的相对地址。
	 */
	@SimpleProperty
	public synchronized void 地址(String value) {
		value = HttpSupport.text(value);
		address = value;
	}

	/**
	 * 取得HTTP 方法，支持 GET、POST、PUT、PATCH、DELETE、HEAD 和 OPTIONS。
	 *
	 * @return HTTP 方法，支持 GET、POST、PUT、PATCH、DELETE、HEAD 和 OPTIONS。
	 */
	@SimpleProperty
	public synchronized String 方法() {
		return method;
	}

	/**
	 * 设置HTTP 方法，支持 GET、POST、PUT、PATCH、DELETE、HEAD 和 OPTIONS。
	 *
	 * @param value HTTP 方法，支持 GET、POST、PUT、PATCH、DELETE、HEAD 和 OPTIONS。
	 */
	@SimpleProperty
	public synchronized void 方法(String value) {
		value = HttpSupport.method(value);
		method = value;
	}

	/**
	 * 取得请求标识；留空时生成，同一客户端运行期间不得重复。
	 *
	 * @return 请求标识；留空时生成，同一客户端运行期间不得重复。
	 */
	@SimpleProperty
	public synchronized String 标识() {
		return id;
	}

	/**
	 * 设置请求标识；留空时生成，同一客户端运行期间不得重复。
	 *
	 * @param value 请求标识；留空时生成，同一客户端运行期间不得重复。
	 */
	@SimpleProperty
	public synchronized void 标识(String value) {
		value = HttpSupport.text(value);
		id = value;
	}

	/**
	 * 取得连接、读取及写入超时，单位毫秒；-1 采用客户端配置，0 不限。
	 *
	 * @return 连接、读取及写入超时，单位毫秒；-1 采用客户端配置，0 不限。
	 */
	@SimpleProperty
	public synchronized int 超时() {
		return timeout;
	}

	/**
	 * 设置连接、读取及写入超时，单位毫秒；-1 采用客户端配置，0 不限。
	 *
	 * @param value 连接、读取及写入超时，单位毫秒；-1 采用客户端配置，0 不限。
	 */
	@SimpleProperty
	public synchronized void 超时(int value) {
		HttpSupport.minimum(value, -1);
		timeout = value;
	}

	/**
	 * 取得连接或超时失败后的重试次数；-1 采用客户端配置。重试可能重复提交非幂等操作。
	 *
	 * @return 连接或超时失败后的重试次数；-1 采用客户端配置。重试可能重复提交非幂等操作。
	 */
	@SimpleProperty
	public synchronized int 重试次数() {
		return retries;
	}

	/**
	 * 设置连接或超时失败后的重试次数；-1 采用客户端配置。重试可能重复提交非幂等操作。
	 *
	 * @param value 连接或超时失败后的重试次数；-1 采用客户端配置。重试可能重复提交非幂等操作。
	 */
	@SimpleProperty
	public synchronized void 重试次数(int value) {
		HttpSupport.minimum(value, -1);
		retries = value;
	}

	/**
	 * 取得重试等待毫秒数；-1 采用客户端配置。
	 *
	 * @return 重试等待毫秒数；-1 采用客户端配置。
	 */
	@SimpleProperty
	public synchronized long 重试间隔() {
		return retryDelay;
	}

	/**
	 * 设置重试等待毫秒数；-1 采用客户端配置。
	 *
	 * @param value 重试等待毫秒数；-1 采用客户端配置。
	 */
	@SimpleProperty
	public synchronized void 重试间隔(long value) {
		HttpSupport.minimum(value, -1);
		HttpSupport.maximum(value, Integer.MAX_VALUE);
		retryDelay = value;
	}

	/**
	 * 取得每次后续重试额外增加的等待毫秒数，-1 采用客户端配置。
	 *
	 * @return 后续重试等待的递增毫秒数。
	 */
	@SimpleProperty
	public synchronized long 重试递增间隔() {
		return retryIncrease;
	}

	/**
	 * 设置后续重试的等待增量；第一次等待重试间隔，之后每次再增加该值，-1 采用客户端配置。
	 *
	 * @param value 递增毫秒数，零不递增。
	 */
	@SimpleProperty
	public synchronized void 重试递增间隔(long value) {
		HttpSupport.minimum(value, -1);
		HttpSupport.maximum(value, Integer.MAX_VALUE);
		retryIncrease = value;
	}

	/**
	 * 取得客户端的缓存策略常量；-1 采用客户端配置。下载仅允许不缓存。
	 *
	 * @return 客户端的缓存策略常量；-1 采用客户端配置。下载仅允许不缓存。
	 */
	@SimpleProperty
	public synchronized int 缓存策略() {
		return cacheMode;
	}

	/**
	 * 设置客户端的缓存策略常量；-1 采用客户端配置。下载仅允许不缓存。
	 *
	 * @param value 客户端的缓存策略常量；-1 采用客户端配置。下载仅允许不缓存。
	 */
	@SimpleProperty
	public synchronized void 缓存策略(int value) {
		HttpSupport.range(value, -1, 7);
		cacheMode = value;
	}

	/**
	 * 取得缓存有效秒数；-2 采用客户端配置，-1 不过期，0 不复用旧缓存。
	 *
	 * @return 缓存有效秒数；-2 采用客户端配置，-1 不过期，0 不复用旧缓存。
	 */
	@SimpleProperty
	public synchronized long 缓存秒数() {
		return cacheSeconds;
	}

	/**
	 * 设置缓存有效秒数；-2 采用客户端配置，-1 不过期，0 不复用旧缓存。
	 *
	 * @param value 缓存有效秒数；-2 采用客户端配置，-1 不过期，0 不复用旧缓存。
	 */
	@SimpleProperty
	public synchronized void 缓存秒数(long value) {
		HttpSupport.minimum(value, -2);
		cacheSeconds = value;
	}

	/**
	 * 取得自定义缓存名称；仍与地址、方法、请求内容、请求头及 Cookie 隔离。
	 *
	 * @return 自定义缓存名称；仍与地址、方法、请求内容、请求头及 Cookie 隔离。
	 */
	@SimpleProperty
	public synchronized String 缓存标识() {
		return cacheKey;
	}

	/**
	 * 设置自定义缓存名称；仍与地址、方法、请求内容、请求头及 Cookie 隔离。
	 *
	 * @param value 自定义缓存名称；仍与地址、方法、请求内容、请求头及 Cookie 隔离。
	 */
	@SimpleProperty
	public synchronized void 缓存标识(String value) {
		value = HttpSupport.text(value);
		cacheKey = value;
	}

	/**
	 * 取得下载目标的绝对文件路径；留空时读取响应。成功校验后原子替换，失败保留原文件。
	 *
	 * @return 下载目标的绝对文件路径；留空时读取响应。成功校验后原子替换，失败保留原文件。
	 */
	@SimpleProperty
	public synchronized String 下载路径() {
		return download;
	}

	/**
	 * 设置下载目标的绝对文件路径；留空时读取响应。成功校验后原子替换，失败保留原文件。
	 *
	 * @param value 下载目标的绝对文件路径；留空时读取响应。成功校验后原子替换，失败保留原文件。
	 */
	@SimpleProperty
	public synchronized void 下载路径(String value) {
		value = HttpSupport.text(value);
		download = value;
	}

	/**
	 * 取得下载文件预期 SHA-256 十六进制摘要；留空时不核对摘要。
	 *
	 * @return 下载文件预期 SHA-256 十六进制摘要；留空时不核对摘要。
	 */
	@SimpleProperty
	public synchronized String 预期SHA256() {
		return sha256;
	}

	/**
	 * 设置下载文件预期 SHA-256 十六进制摘要；留空时不核对摘要。
	 *
	 * @param value 下载文件预期 SHA-256 十六进制摘要；留空时不核对摘要。
	 */
	@SimpleProperty
	public synchronized void 预期SHA256(String value) {
		value = HttpSupport.sha256Text(value);
		sha256 = value;
	}

	/**
	 * 取得0 原始响应，1 标准 JSON，2 自定义解析事件。文件下载只支持原始响应。
	 *
	 * @return 0 原始响应，1 标准 JSON，2 自定义解析事件。文件下载只支持原始响应。
	 */
	@SimpleProperty
	public synchronized int 解析方式() {
		return parser;
	}

	/**
	 * 设置0 原始响应，1 标准 JSON，2 自定义解析事件。文件下载只支持原始响应。
	 *
	 * @param value 0 原始响应，1 标准 JSON，2 自定义解析事件。文件下载只支持原始响应。
	 */
	@SimpleProperty
	public synchronized void 解析方式(int value) {
		HttpSupport.range(value, 0, 2);
		parser = value;
	}

	/**
	 * 取得标准 JSON 解析中代表业务成功的整数。
	 *
	 * @return 标准 JSON 解析中代表业务成功的整数。
	 */
	@SimpleProperty
	public synchronized int 成功码() {
		return successCode;
	}

	/**
	 * 设置标准 JSON 解析中代表业务成功的整数。
	 *
	 * @param value 标准 JSON 解析中代表业务成功的整数。
	 */
	@SimpleProperty
	public synchronized void 成功码(int value) {
		successCode = value;
	}

	/**
	 * 取得标准 JSON 中业务状态字段的路径，支持点分隔及数组下标。
	 *
	 * @return 标准 JSON 中业务状态字段的路径，支持点分隔及数组下标。
	 */
	@SimpleProperty
	public synchronized String 状态路径() {
		return codePath;
	}

	/**
	 * 设置标准 JSON 中业务状态字段的路径，支持点分隔及数组下标。
	 *
	 * @param value 标准 JSON 中业务状态字段的路径，支持点分隔及数组下标。
	 */
	@SimpleProperty
	public synchronized void 状态路径(String value) {
		value = HttpSupport.nonempty(value);
		codePath = value;
	}

	/**
	 * 取得标准 JSON 中消息字段的路径；留空表示不读取。
	 *
	 * @return 标准 JSON 中消息字段的路径；留空表示不读取。
	 */
	@SimpleProperty
	public synchronized String 消息路径() {
		return messagePath;
	}

	/**
	 * 设置标准 JSON 中消息字段的路径；留空表示不读取。
	 *
	 * @param value 标准 JSON 中消息字段的路径；留空表示不读取。
	 */
	@SimpleProperty
	public synchronized void 消息路径(String value) {
		value = HttpSupport.text(value);
		messagePath = value;
	}

	/**
	 * 取得标准 JSON 中数据字段的路径；留空表示完整 JSON。
	 *
	 * @return 标准 JSON 中数据字段的路径；留空表示完整 JSON。
	 */
	@SimpleProperty
	public synchronized String 数据路径() {
		return dataPath;
	}

	/**
	 * 设置标准 JSON 中数据字段的路径；留空表示完整 JSON。
	 *
	 * @param value 标准 JSON 中数据字段的路径；留空表示完整 JSON。
	 */
	@SimpleProperty
	public synchronized void 数据路径(String value) {
		value = HttpSupport.text(value);
		dataPath = value;
	}

	/**
	 * 追加 URL 查询参数，保留同名参数。
	 *
	 * @param name 字段名称。
	 * @param value 字段内容。
	 */
	@SimpleFunction
	public synchronized void 添加查询参数(String name, String value) {
		HttpSupport.add(query, name, value, false);
	}

	/**
	 * 追加表单字段，保留同名字段；有文件时使用 multipart。
	 *
	 * @param name 字段名称。
	 * @param value 字段内容。
	 */
	@SimpleFunction
	public synchronized void 添加表单参数(String name, String value) {
		HttpSupport.add(form, name, value, false);
	}

	/**
	 * 追加请求头，保留同名字段。
	 *
	 * @param name 字段名称。
	 * @param value 字段内容。
	 */
	@SimpleFunction
	public synchronized void 添加请求头(String name, String value) {
		HttpSupport.add(headers, name, value, true);
	}

	/**
	 * 替换全部同名请求头，名称不区分大小写。
	 *
	 * @param name 字段名称。
	 * @param value 字段内容。
	 */
	@SimpleFunction
	public synchronized void 置请求头(String name, String value) {
		HttpSupport.setHeader(headers, name, value);
	}

	/**
	 * 删除全部同名请求头。
	 *
	 * @param name 不区分大小写的字段名称。
	 */
	@SimpleFunction
	public synchronized void 删除请求头(String name) {
		HttpSupport.remove(headers, name, true);
	}

	/**
	 * 取得首个同名请求头；不存在时返回空文本。
	 *
	 * @param name 不区分大小写的字段名称。
	 * @return 操作结果。
	 */
	@SimpleFunction
	public synchronized String 取请求头(String name) {
		return HttpSupport.first(headers, name);
	}

	/**
	 * 设置文本请求体，以指定媒体类型的字符集编码，未指定字符集时采用 UTF-8；清除表单及文件。
	 *
	 * @param text 请求文本。
	 * @param type 例如 application/json; charset=utf-8。
	 */
	@SimpleFunction
	public synchronized void 置文本请求体(String text, String type) {
		okhttp3.MediaType parsed = HttpSupport.media(type);
		body = HttpSupport.text(text).getBytes(parsed.charset(StandardCharsets.UTF_8));
		mediaType = type;
		form.clear();
		files.clear();
	}

	/**
	 * 设置二进制请求体并复制字节数组；清除表单及文件。
	 *
	 * @param bytes 请求数据。
	 * @param type 例如 application/octet-stream。
	 */
	@SimpleFunction
	public synchronized void 置字节请求体(byte[] bytes, String type) {
		HttpSupport.media(type);
		body = HttpSupport.bytes(bytes);
		mediaType = type;
		form.clear();
		files.clear();
	}

	/**
	 * 追加文件或 content URI 上传项；每次重试重新打开来源，不将整个文件读入内存。
	 *
	 * @param name 表单字段名。
	 * @param path 绝对文件路径或已获读取授权的 content URI。
	 * @param filename 发送给服务端的文件名，不包含目录。
	 * @param type 文件媒体类型。
	 */
	@SimpleFunction
	public synchronized void 添加文件(String name, String path, String filename, String type) {
		files.add(UploadPart.file(name, path, filename, type));
		body = null;
	}

	/**
	 * 追加内存文件，保留文件名和媒体类型。
	 *
	 * @param name 表单字段名。
	 * @param bytes 文件内容，将复制保存。
	 * @param filename 发送给服务端的文件名。
	 * @param type 文件媒体类型。
	 */
	@SimpleFunction
	public synchronized void 添加字节文件(String name, byte[] bytes, String filename, String type) {
		files.add(UploadPart.bytes(name, bytes, filename, type));
		body = null;
	}

	/**
	 * 清空 URL 查询参数。
	 */
	@SimpleFunction
	public synchronized void 清空查询参数() {
		query.clear();
	}

	/**
	 * 清空请求头。
	 */
	@SimpleFunction
	public synchronized void 清空请求头() {
		headers.clear();
	}

	/**
	 * 清空文本、字节、表单和文件请求体。
	 */
	@SimpleFunction
	public synchronized void 清空请求体() {
		body = null;
		mediaType = "";
		form.clear();
		files.clear();
	}

	/**
	 * 复制配置、请求字段和字节文件；文件路径仍引用同一来源。
	 *
	 * @return 独立的可修改请求。
	 */
	@SimpleFunction
	public synchronized XHttp请求 复制() {
		return copy();
	}

	/**
	 * 在同一锁内复制全部字段，防止请求发送时读到一半配置。
	 */
	synchronized XHttp请求 copy() {
		XHttp请求 result = new XHttp请求();
		result.address = address;
		result.method = method;
		result.id = id;
		result.timeout = timeout;
		result.connectTimeout = connectTimeout;
		result.readTimeout = readTimeout;
		result.writeTimeout = writeTimeout;
		result.callTimeout = callTimeout;
		result.retries = retries;
		result.retryDelay = retryDelay;
		result.retryIncrease = retryIncrease;
		result.cacheMode = cacheMode;
		result.cacheSeconds = cacheSeconds;
		result.cacheKey = cacheKey;
		result.download = download;
		result.sha256 = sha256;
		result.parser = parser;
		result.successCode = successCode;
		result.codePath = codePath;
		result.messagePath = messagePath;
		result.dataPath = dataPath;
		result.mediaType = mediaType;
		result.body = body == null ? null : body.clone();
		HttpSupport.copy(headers, result.headers);
		HttpSupport.copy(query, result.query);
		HttpSupport.copy(form, result.form);
		for (UploadPart file : files) {
			result.files.add(file.copy());
		}
		return result;
	}
	/**
	 * 取得连接超时毫秒数，-1 采用客户端默认配置，0 不限。单项配置优先于统一超时。
	 *
	 * @return 连接超时毫秒数，-1 采用客户端默认配置，0 不限。单项配置优先于统一超时。
	 */
	@SimpleProperty
	public synchronized int 连接超时() {
		return connectTimeout;
	}

	/**
	 * 设置连接超时毫秒数，-1 采用客户端默认配置，0 不限。单项配置优先于统一超时。
	 *
	 * @param value 连接超时毫秒数，-1 采用客户端默认配置，0 不限。单项配置优先于统一超时。
	 */
	@SimpleProperty
	public synchronized void 连接超时(int value) {
		HttpSupport.minimum(value, -1);
		connectTimeout = value;
	}

	/**
	 * 取得读取超时毫秒数，-1 采用客户端默认配置，0 不限。单项配置优先于统一超时。
	 *
	 * @return 读取超时毫秒数，-1 采用客户端默认配置，0 不限。单项配置优先于统一超时。
	 */
	@SimpleProperty
	public synchronized int 读取超时() {
		return readTimeout;
	}

	/**
	 * 设置读取超时毫秒数，-1 采用客户端默认配置，0 不限。单项配置优先于统一超时。
	 *
	 * @param value 读取超时毫秒数，-1 采用客户端默认配置，0 不限。单项配置优先于统一超时。
	 */
	@SimpleProperty
	public synchronized void 读取超时(int value) {
		HttpSupport.minimum(value, -1);
		readTimeout = value;
	}

	/**
	 * 取得写入超时毫秒数，-1 采用客户端默认配置，0 不限。单项配置优先于统一超时。
	 *
	 * @return 写入超时毫秒数，-1 采用客户端默认配置，0 不限。单项配置优先于统一超时。
	 */
	@SimpleProperty
	public synchronized int 写入超时() {
		return writeTimeout;
	}

	/**
	 * 设置写入超时毫秒数，-1 采用客户端默认配置，0 不限。单项配置优先于统一超时。
	 *
	 * @param value 写入超时毫秒数，-1 采用客户端默认配置，0 不限。单项配置优先于统一超时。
	 */
	@SimpleProperty
	public synchronized void 写入超时(int value) {
		HttpSupport.minimum(value, -1);
		writeTimeout = value;
	}

	/**
	 * 取得单次总超时毫秒数，-1 采用客户端默认配置，0 不限。每次重试重新计算该尝试的总超时。
	 *
	 * @return 单次总超时毫秒数，-1 采用客户端默认配置，0 不限。每次重试重新计算该尝试的总超时。
	 */
	@SimpleProperty
	public synchronized int 单次总超时() {
		return callTimeout;
	}

	/**
	 * 设置单次总超时毫秒数，-1 采用客户端默认配置，0 不限。每次重试重新计算该尝试的总超时。
	 *
	 * @param value 单次总超时毫秒数，-1 采用客户端默认配置，0 不限。每次重试重新计算该尝试的总超时。
	 */
	@SimpleProperty
	public synchronized void 单次总超时(int value) {
		HttpSupport.minimum(value, -1);
		callTimeout = value;
	}

	/**
	 * 删除全部同名查询参数。
	 *
	 * @param name 区分大小写的参数名。
	 */
	@SimpleFunction
	public synchronized void 删除查询参数(String name) {
		HttpSupport.remove(query, name, false);
	}

	/**
	 * 删除全部同名表单参数，不移除同名文件项。
	 *
	 * @param name 区分大小写的字段名。
	 */
	@SimpleFunction
	public synchronized void 删除表单参数(String name) {
		HttpSupport.remove(form, name, false);
	}

}
