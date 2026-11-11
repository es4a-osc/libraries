package simple.library.wsd.xutil;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.xuexiang.xutil.security.EncryptUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 文本及文件摘要和 HMAC；MD5、SHA1 仅用于既有协议或内容校验。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XU摘要 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XU摘要() {
	}

	/**
	 * 计算 UTF-8 文本的大写十六进制 MD5 摘要，支持空文本。
	 *
	 * @param text 文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String MD5(String text) {
		return digest(text, "MD5");
	}

	/**
	 * 计算 UTF-8 文本的大写十六进制 SHA1 摘要，支持空文本。
	 *
	 * @param text 文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String SHA1(String text) {
		return digest(text, "SHA-1");
	}

	/**
	 * 计算 UTF-8 文本的大写十六进制 SHA256 摘要，支持空文本。
	 *
	 * @param text 文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String SHA256(String text) {
		return digest(text, "SHA-256");
	}

	/**
	 * 计算 UTF-8 文本的大写十六进制 SHA512 摘要，支持空文本。
	 *
	 * @param text 文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String SHA512(String text) {
		return digest(text, "SHA-512");
	}

	/**
	 * 计算 UTF-8 文本的 HMAC-SHA256 大写十六进制结果，支持空文本。
	 *
	 * @param text   文本。
	 * @param secret UTF-8 密钥，允许空密钥并按 HMAC 定义计算。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String HMACSHA256(String text, String secret) {
		return hmac(text, secret, "HmacSHA256");
	}

	/**
	 * 计算 UTF-8 文本的 HMAC-SHA512 大写十六进制结果，支持空文本。
	 *
	 * @param text   文本。
	 * @param secret UTF-8 密钥，允许空密钥并按 HMAC 定义计算。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String HMACSHA512(String text, String secret) {
		return hmac(text, secret, "HmacSHA512");
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

	/**
	 * 使用标准摘要实现，包含长度为零的有效输入，不沿用上游对空字节集返回 null 的规则。
	 *
	 * @param text      UTF-8 原文。
	 * @param algorithm JCA 摘要算法。
	 * @return 大写十六进制摘要。
	 */
	private static String digest(String text, String algorithm) {
		return 字节集摘要(text.getBytes(StandardCharsets.UTF_8), algorithm);
	}

	/**
	 * 计算标准 HMAC；输入和密钥均按 UTF-8 编码，不将空消息当作缺失参数。
	 *
	 * @param text      UTF-8 原文。
	 * @param secret    UTF-8 密钥。
	 * @param algorithm JCA HMAC 算法。
	 * @return 大写十六进制认证码。
	 */
	private static String hmac(String text, String secret, String algorithm) {
		return 字节集HMAC(text.getBytes(StandardCharsets.UTF_8), secret.getBytes(StandardCharsets.UTF_8), algorithm);
	}

	/**
	 * 固定输出大写十六进制，保留摘要的前导零，不依赖默认字符集或区域设置。
	 *
	 * @param bytes 摘要或认证码。
	 * @return 完整大写十六进制。
	 */
	private static String hex(byte[] bytes) {
		char[] alphabet = "0123456789ABCDEF".toCharArray();
		char[] result = new char[bytes.length * 2];
		for (int index = 0; index < bytes.length; index++) {
			int value = bytes[index] & 255;
			result[index * 2] = alphabet[value >>> 4];
			result[index * 2 + 1] = alphabet[value & 15];
		}
		return new String(result);
	}

	/**
	 * 计算 UTF-8 文本的 SHA-224 大写十六进制摘要。
	 *
	 * @param text 文本。
	 * @return 计算 UTF-8 文本的 SHA-224 大写十六进制摘要。
	 */
	@SimpleFunction
	public static String SHA224(String text) {
		return digest(text, "SHA-224");
	}

	/**
	 * 计算 UTF-8 文本的 SHA-384 大写十六进制摘要。
	 *
	 * @param text 文本。
	 * @return 计算 UTF-8 文本的 SHA-384 大写十六进制摘要。
	 */
	@SimpleFunction
	public static String SHA384(String text) {
		return digest(text, "SHA-384");
	}

	/**
	 * 计算字节集的大写十六进制摘要；空数据有效，算法不可用时抛出错误。
	 *
	 * @param data      数据。
	 * @param algorithm 如 MD5、SHA-1、SHA-224、SHA-256、SHA-384、SHA-512。
	 * @return 计算字节集的大写十六进制摘要；空数据有效，算法不可用时抛出错误。
	 */
	@SimpleFunction
	public static String 字节集摘要(byte[] data, String algorithm) {
		try {
			return hex(MessageDigest.getInstance(algorithm).digest(data));
		} catch (GeneralSecurityException failure) {
			throw XUtilSupport.failure("摘要算法不可用：" + algorithm, failure);
		}
	}

	/**
	 * 计算任意二进制消息的 HMAC；返回大写十六进制，失败抛出错误。
	 *
	 * @param data      数据。
	 * @param key       可为空字节集。
	 * @param algorithm 如 HmacMD5、HmacSHA1、HmacSHA224、HmacSHA256、HmacSHA384、HmacSHA512。
	 * @return 计算任意二进制消息的 HMAC；返回大写十六进制，失败抛出错误。
	 */
	@SimpleFunction
	public static String 字节集HMAC(byte[] data, byte[] key, String algorithm) {
		try {
			Mac mac = Mac.getInstance(algorithm);
			mac.init(new SecretKeySpec(key.length == 0 ? new byte[] { 0 } : key, algorithm));
			return hex(mac.doFinal(data));
		} catch (GeneralSecurityException failure) {
			throw XUtilSupport.failure("HMAC 计算失败：" + algorithm, failure);
		}
	}
}
