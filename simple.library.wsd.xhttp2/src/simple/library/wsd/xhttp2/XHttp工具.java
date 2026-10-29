package simple.library.wsd.xhttp2;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

import com.google.gson.JsonParser;

import okhttp3.Credentials;
import okhttp3.HttpUrl;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 网络数据的编码、摘要、签名及 JSON 路径函数集，不访问网络、不声明网络权限。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XHttp工具 {

	/**
	 * 函数集仅提供静态方法，不创建实例。
	 */
	private XHttp工具() {
	}
	/**
	 * 使用 UTF-8 编码 URL 查询或表单参数值，空格编码为加号。
	 *
	 * @param text 待编码的字段值。
	 * @return 操作结果。
	 */
	@SimpleFunction
	public static String 网址编码(String text) {
		try {
			return URLEncoder.encode(HttpSupport.text(text), "UTF-8");
		} catch (UnsupportedEncodingException impossible) {
			throw new IllegalStateException(impossible);
		}
	}

	/**
	 * 使用 UTF-8 解码 URL 查询或表单参数值，加号还原为空格。
	 *
	 * @param text 已编码字段值。
	 * @return 操作结果。
	 */
	@SimpleFunction
	public static String 网址解码(String text) {
		try {
			return URLDecoder.decode(HttpSupport.text(text), "UTF-8");
		} catch (UnsupportedEncodingException impossible) {
			throw new IllegalStateException(impossible);
		}
	}

	/**
	 * 编码为无换行的标准 Base64。
	 *
	 * @param data 原始数据。
	 * @return 操作结果。
	 */
	@SimpleFunction
	public static String Base64编码(byte[] data) {
		return Base64.encodeToString(HttpSupport.bytes(data), Base64.NO_WRAP);
	}

	/**
	 * 解码标准 Base64，非法格式抛出参数错误。
	 *
	 * @param text Base64 文本。
	 * @return 操作结果。
	 */
	@SimpleFunction
	public static byte[] Base64解码(String text) {
		String normalized = HttpSupport.text(text).replaceAll("\\s+", "");
		if (!normalized.matches("(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?")) {
			throw new IllegalArgumentException("Base64 格式无效。");
		}
		byte[] result = Base64.decode(normalized, Base64.DEFAULT);
		if (!Base64.encodeToString(result, Base64.NO_WRAP).equals(normalized)) {
			throw new IllegalArgumentException("Base64 填充位无效。");
		}
		return result;
	}

	/**
	 * 计算数据的 SHA-256，返回小写十六进制文本。
	 *
	 * @param data 原始数据。
	 * @return 操作结果。
	 */
	@SimpleFunction
	public static String SHA256(byte[] data) {
		return HttpSupport.digest(HttpSupport.bytes(data));
	}

	/**
	 * 计算数据的 MD5，仅用于兼容既有协议，不用于安全签名。
	 *
	 * @param data 原始数据。
	 * @return 操作结果。
	 */
	@SimpleFunction
	public static String MD5(byte[] data) {
		try {
			return HttpSupport.hex(MessageDigest.getInstance("MD5").digest(HttpSupport.bytes(data)));
		} catch (GeneralSecurityException impossible) {
			throw new IllegalStateException(impossible);
		}
	}

	/**
	 * 计算 UTF-8 文本的 HMAC-SHA256 签名，返回小写十六进制文本。
	 *
	 * @param text 待签名文本。
	 * @param key UTF-8 密钥，不能为空。
	 * @return 操作结果。
	 */
	@SimpleFunction
	public static String HMAC_SHA256(String text, String key) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(HttpSupport.nonempty(key).getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			return HttpSupport.hex(mac.doFinal(HttpSupport.text(text).getBytes(StandardCharsets.UTF_8)));
		} catch (GeneralSecurityException failure) {
			throw new IllegalStateException("HMAC-SHA256 不可用。", failure);
		}
	}

	/**
	 * 生成使用 UTF-8 用户名及密码的 Basic 认证字段值。
	 *
	 * @param username 用户名。
	 * @param password 密码。
	 * @return 操作结果。
	 */
	@SimpleFunction
	public static String Basic认证(String username, String password) {
		return Credentials.basic(HttpSupport.text(username), HttpSupport.text(password), StandardCharsets.UTF_8);
	}

	/**
	 * 按点分隔和数组下标读取 JSON；对象和数组返回 JSON，字符串返回文本，缺失或 null 返回空文本。
	 *
	 * @param json 有效 JSON 文本。
	 * @param path 如 data.items[0]；空路径取完整内容。
	 * @return 操作结果。
	 */
	@SimpleFunction
	public static String 取JSON值(String json, String path) {
		return HttpSupport.jsonText(HttpSupport.select(new JsonParser().parse(HttpSupport.nonempty(json)), HttpSupport.text(path)));
	}

	/**
	 * 追加并编码一个查询参数，保留同名参数和原地址片段。
	 *
	 * @param address 完整 HTTP/HTTPS 地址。
	 * @param name 参数名。
	 * @param value 参数值。
	 * @return 操作结果。
	 */
	@SimpleFunction
	public static String 追加查询参数(String address, String name, String value) {
		HttpUrl url = HttpUrl.parse(HttpSupport.nonempty(address));
		if (url == null) { throw new IllegalArgumentException("地址无效。"); }
		return url.newBuilder().addQueryParameter(HttpSupport.nonempty(name), HttpSupport.text(value)).build().toString();
	}

}
