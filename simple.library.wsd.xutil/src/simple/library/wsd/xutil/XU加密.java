package simple.library.wsd.xutil;

import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.GCMParameterSpec;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleFunction;

/**
 * 对称加解密，不隐藏算法、模式、填充及初始向量，也不将失败伪装为空数据。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XU加密 {
	/**
	 * 静态函数集不创建实例。
	 */
	private XU加密() {
	}

	/**
	 * 按明确的变换规则处理二进制数据。
	 *
	 * @param mode           加密或解密模式。
	 * @param data           待处理的数据。
	 * @param key            密钥字节。
	 * @param transformation 完整算法、模式和填充。
	 * @param iv             CBC 初始向量或 GCM nonce；ECB 必须为空。
	 * @return 处理后的数据。
	 */
	private static byte[] crypt(int mode, byte[] data, byte[] key, String transformation, byte[] iv) {
		String[] parts = transformation.split("/");
		if (parts.length != 3 || !(parts[0].equals("AES") || parts[0].equals("DES") || parts[0].equals("DESede"))) {
			throw new IllegalArgumentException("请使用完整的 AES、DES 或 DESede 变换名称");
		}
		try {
			Cipher cipher = Cipher.getInstance(transformation);
			SecretKeySpec secret = new SecretKeySpec(key, parts[0]);
			if (parts[1].equalsIgnoreCase("ECB")) {
				if (iv.length != 0) {
					throw new IllegalArgumentException("ECB 不使用初始向量");
				}
				cipher.init(mode, secret);
			} else if (parts[1].equalsIgnoreCase("GCM")) {
				if (iv.length != 12) {
					throw new IllegalArgumentException("GCM nonce 必须为12字节");
				}
				cipher.init(mode, secret, new GCMParameterSpec(128, iv));
			} else {
				if (iv.length == 0) {
					throw new IllegalArgumentException("此模式需要初始向量");
				}
				cipher.init(mode, secret, new IvParameterSpec(iv));
			}
			return cipher.doFinal(data);
		} catch (GeneralSecurityException failure) {
			throw XUtilSupport.failure("加解密失败：" + failure.getMessage(), failure);
		}
	}

	/**
	 * 加密二进制数据；GCM 返回密文及128位认证标签。同一密钥的 GCM nonce 不可重复。
	 *
	 * @param data           数据。
	 * @param key            AES为16/24/32字节；DES为8字节；DESede为24字节。
	 * @param transformation 如 AES/CBC/PKCS5Padding、AES/GCM/NoPadding；DES及DESede仅用于旧协议兼容。
	 * @param iv             CBC须为分组长度；GCM须为12字节nonce；ECB传空字节集。
	 * @return 加密二进制数据；GCM 返回密文及128位认证标签。同一密钥的 GCM nonce 不可重复。
	 */
	@SimpleFunction
	public static byte[] 加密(byte[] data, byte[] key, String transformation, byte[] iv) {
		return crypt(Cipher.ENCRYPT_MODE, data, key, transformation, iv);
	}

	/**
	 * 解密二进制数据；填充、密钥或 GCM 认证失败时抛出错误。
	 *
	 * @param data           数据。
	 * @param key            AES为16/24/32字节；DES为8字节；DESede为24字节。
	 * @param transformation 如 AES/CBC/PKCS5Padding、AES/GCM/NoPadding；DES及DESede仅用于旧协议兼容。
	 * @param iv             CBC须为分组长度；GCM须为12字节nonce；ECB传空字节集。
	 * @return 解密二进制数据；填充、密钥或 GCM 认证失败时抛出错误。
	 */
	@SimpleFunction
	public static byte[] 解密(byte[] data, byte[] key, String transformation, byte[] iv) {
		return crypt(Cipher.DECRYPT_MODE, data, key, transformation, iv);
	}
}
