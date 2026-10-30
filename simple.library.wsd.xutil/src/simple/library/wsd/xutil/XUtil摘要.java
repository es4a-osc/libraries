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
public final class XUtil摘要 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil摘要() {
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
	 * @param text 文本。
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
	 * @param text 文本。
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
	 */
	private static String digest(String text, String algorithm) {
		try {
			return hex(MessageDigest.getInstance(algorithm).digest(text.getBytes(StandardCharsets.UTF_8)));
		} catch (GeneralSecurityException failure) {
			throw XUtilSupport.failure("摘要算法不可用：" + algorithm, failure);
		}
	}

	/**
	 * 计算标准 HMAC；输入和密钥均按 UTF-8 编码，不将空消息当作缺失参数。
	 */
	private static String hmac(String text, String secret, String algorithm) {
		byte[] key = secret.getBytes(StandardCharsets.UTF_8);
		// RFC 2104 第2节先将短密钥尾部补零至块长度，空密钥与单个零字节补齐后完全相同。
		// SecretKeySpec 不接受零长度密钥；这里仅适配 JCA 入参，不改变 HMAC 的计算结果。
		if (key.length == 0) key = new byte[] { 0 };
		try {
			Mac mac = Mac.getInstance(algorithm);
			mac.init(new SecretKeySpec(key, algorithm));
			return hex(mac.doFinal(text.getBytes(StandardCharsets.UTF_8)));
		} catch (GeneralSecurityException failure) {
			throw XUtilSupport.failure("HMAC 计算失败：" + algorithm, failure);
		}
	}

	/**
	 * 固定输出大写十六进制，保留摘要的前导零，不依赖默认字符集或区域设置。
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
}
