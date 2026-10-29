package simple.library.wsd.xutil;

import java.nio.charset.StandardCharsets;

import com.xuexiang.xutil.security.EncodeUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * UTF-8 文本、Base64、URL 与 HTML 编解码。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUtil编码 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil编码() {
	}

	/**
	 * 使用 UTF-8 编码文本。
	 *
	 * @param text 文本。
	 * @return 返回的字节集。
	 */
	@SimpleFunction
	public static byte[] 文本到字节集(String text) {
		return text.getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * 使用 UTF-8 解码字节集。
	 *
	 * @param data 数据。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 字节集到文本(byte[] data) {
		return new String(data, StandardCharsets.UTF_8);
	}

	/**
	 * 编码字节集为无换行的标准 Base64。
	 *
	 * @param data 数据。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String Base64编码(byte[] data) {
		return EncodeUtils.base64Encode2String(data);
	}

	/**
	 * 解码标准 Base64，非法输入抛出异常。
	 *
	 * @param text 文本。
	 * @return 返回的字节集。
	 */
	@SimpleFunction
	public static byte[] Base64解码(String text) {
		return EncodeUtils.base64Decode(text);
	}

	/**
	 * 使用上游规则进行URL编码；URL 文本使用 UTF-8。
	 *
	 * @param text 文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String URL编码(String text) {
		return EncodeUtils.urlEncode(text);
	}

	/**
	 * 使用上游规则进行URL解码；URL 文本使用 UTF-8。
	 *
	 * @param text 文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String URL解码(String text) {
		return EncodeUtils.urlDecode(text);
	}

	/**
	 * 使用上游规则进行HTML编码；URL 文本使用 UTF-8。
	 *
	 * @param text 文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String HTML编码(String text) {
		return EncodeUtils.htmlEncode(text);
	}

	/**
	 * 将 HTML 实体及标记转换为纯文本。
	 *
	 * @param text 文本。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String HTML解码(String text) {
		return EncodeUtils.htmlDecode(text).toString();
	}
}
