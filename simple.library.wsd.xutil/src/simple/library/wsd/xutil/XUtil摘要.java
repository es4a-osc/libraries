package simple.library.wsd.xutil;

import java.nio.charset.StandardCharsets;

import com.xuexiang.xutil.security.EncryptUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 文本及文件摘要和 HMAC；MD5、SHA1 仅用于既有协议或内容校验。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUtil摘要 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil摘要() {
	}

	/**
	 * 计算 UTF-8 文本的 MD5 十六进制摘要。
	 *
	 * @param text 文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String MD5(String text) {
		return EncryptUtils.encryptMD5ToString(text.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * 计算 UTF-8 文本的 SHA1 十六进制摘要。
	 *
	 * @param text 文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String SHA1(String text) {
		return EncryptUtils.encryptSHA1ToString(text.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * 计算 UTF-8 文本的 SHA256 十六进制摘要。
	 *
	 * @param text 文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String SHA256(String text) {
		return EncryptUtils.encryptSHA256ToString(text.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * 计算 UTF-8 文本的 SHA512 十六进制摘要。
	 *
	 * @param text 文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String SHA512(String text) {
		return EncryptUtils.encryptSHA512ToString(text.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * 计算 UTF-8 文本的 HMAC-SHA256。
	 *
	 * @param text 文本。
	 * @param secret 密钥。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String HMACSHA256(String text, String secret) {
		return EncryptUtils.encryptHmacSHA256ToString(text.getBytes(StandardCharsets.UTF_8), secret.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * 计算 UTF-8 文本的 HMAC-SHA512。
	 *
	 * @param text 文本。
	 * @param secret 密钥。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String HMACSHA512(String text, String secret) {
		return EncryptUtils.encryptHmacSHA512ToString(text.getBytes(StandardCharsets.UTF_8), secret.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * 计算可读文件的 MD5，读取失败返回空文本。
	 *
	 * @param path 路径。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 文件MD5(String path) {
		return XUtilSupport.text(EncryptUtils.encryptMD5File2String(path));
	}
}
