package simple.library.wsd.jsoup;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.jsoup.Jsoup;

import simple.runtime.android.MainActivity;
import simple.runtime.errors.文件读写错误;
import simple.runtime.errors.文件未存在错误;
import simple.runtime.文件操作;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 从内存、普通文件或应用资产创建 HTML 文档的同步函数集。
 *
 * <p>不访问网络、不执行脚本。文件读取和较大文档解析应由调用者放到后台线程。
 * 每次解析返回独立文档；同一文档及其元素不保证并发读写安全。</p>
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class HTML解析器 {

	/**
	 * 静态解析入口，不创建实例。
	 */
	private HTML解析器() {
	}

	/* 文本及片段解析；基本 URL 只用于解析相对链接。 */

	/**
	 * 解析 HTML 文本，不设置基本 URL。
	 *
	 * @param html HTML 文本，不得为空引用；允许空文本。
	 * @return 独立 HTML 文档，空文本仍返回含头部和正文的文档。
	 * @throws IllegalArgumentException HTML 文本为空引用。
	 */
	@SimpleFunction
	public static HTML文档 解析文本(String html) {
		return 解析文本(html, "");
	}

	/**
	 * 解析 HTML 文本，按 HTML 规则补全省略或不完整的结构。
	 *
	 * @param html HTML 文本，不得为空引用；允许空文本。
	 * @param baseUri 用于补全相对链接的基本 URL，空文本表示未指定。
	 * @return 独立文档；不自动请求链接或执行 JavaScript。
	 * @throws IllegalArgumentException 参数为空引用。
	 */
	@SimpleFunction
	public static HTML文档 解析文本(String html, String baseUri) {
		return new HTML文档(Jsoup.parse(requireText(html, "HTML文本"), requireText(baseUri, "基本URL")));
	}

	/**
	 * 把 HTML 片段解析为新文档的正文内容。
	 *
	 * @param html HTML 文本，不得为空引用；允许空文本。
	 * @param baseUri 用于补全相对链接的基本 URL，空文本表示未指定。
	 * @return 正文中包含该片段的独立文档。
	 * @throws IllegalArgumentException 参数为空引用。
	 */
	@SimpleFunction
	public static HTML文档 解析片段(String html, String baseUri) {
		return new HTML文档(Jsoup.parseBodyFragment(requireText(html, "HTML文本"), requireText(baseUri, "基本URL")));
	}

	/* 字节与文件输入；字符集识别交给同版本 jsoup。 */

	/**
	 * 从原始字节解析 HTML，支持显式编码和自动编码识别。
	 *
	 * @param data 完整 HTML 字节，不得为空引用。
	 * @param encoding 字符集名称；空文本按 BOM 或 HTML 编码声明识别，未识别时使用 UTF-8。
	 * @param baseUri 用于补全相对链接的基本 URL，空文本表示未指定。
	 * @return 独立文档。
	 * @throws IllegalArgumentException 参数为空引用或字符集名称无效。
	 */
	@SimpleFunction
	public static HTML文档 解析字节集(byte[] data, String encoding, String baseUri) {
		if (data == null) {
			throw new IllegalArgumentException("HTML 字节集不得为空引用。");
		}

		String charsetName = charsetName(encoding);
		String uri = requireText(baseUri, "基本URL");

		try (ByteArrayInputStream input = new ByteArrayInputStream(data)) {
			return new HTML文档(Jsoup.parse(input, charsetName, uri));
		} catch (IOException error) {
			文件读写错误 failure = new 文件读写错误("读取 HTML 字节集失败：" + error.getMessage());
			failure.initCause(error);
			throw failure;
		}
	}

	/**
	 * 读取整个普通 HTML 文件并解析；沿用运行库文件访问规则。
	 *
	 * @param path 普通文件路径；相对路径以应用私有 files 目录为基准。
	 * @param encoding 字符集名称；空文本按 BOM 或 HTML 编码声明识别，未识别时使用 UTF-8。
	 * @param baseUri 用于补全相对链接的基本 URL，空文本表示未指定。
	 * @return 独立文档；不支持 content URI，文档不保留文件流。
	 * @throws IllegalArgumentException 路径、编码或基本 URL 参数无效。
	 * @throws 文件未存在错误 文件不存在。
	 * @throws 文件读写错误 文件无法读取。
	 */
	@SimpleFunction
	public static HTML文档 解析文件(String path, String encoding, String baseUri) {
		// 先检查参数，避免文件读取完成后才发现编码或基本 URL 无效。
		charsetName(encoding);
		requireText(baseUri, "基本URL");
		requireText(path, "路径");

		return 解析字节集(文件操作.读入字节文件(path), encoding, baseUri);
	}

	/**
	 * 解析随 APK 打包的 HTML 资产，不要求存储权限。
	 *
	 * @param name 资产目录内的相对文件名，例如 books.html。
	 * @param encoding 字符集名称；空文本表示自动识别。
	 * @param baseUri 用于补全链接的基本 URL，空文本表示未指定。
	 * @return 独立文档，方法结束前关闭资产输入流。
	 * @throws IllegalArgumentException 参数无效。
	 * @throws 文件读写错误 资产不存在或无法读取。
	 */
	@SimpleFunction
	public static HTML文档 解析资产(String name, String encoding, String baseUri) {
		String assetName = requireText(name, "资产名");
		String charsetName = charsetName(encoding);
		String uri = requireText(baseUri, "基本URL");

		try (InputStream input = MainActivity.getContext().getAssets().open(assetName)) {
			return new HTML文档(Jsoup.parse(input, charsetName, uri));
		} catch (IOException error) {
			文件读写错误 failure = new 文件读写错误("读取 HTML 资产失败：" + assetName);
			failure.initCause(error);
			throw failure;
		}
	}

	/* 参数检查供本库对象复用，不对 Simple 导出。 */

	/**
	 * 检查文本参数是否为空引用，保留空文本的正常含义。
	 *
	 * @param value 待检查文本。
	 * @param name 对外参数名，用于诊断。
	 * @return 原始文本。
	 * @throws IllegalArgumentException 参数为空引用。
	 */
	static String requireText(String value, String name) {
		if (value == null) {
			throw new IllegalArgumentException(name + "不得为空引用。");
		}

		return value;
	}

	/**
	 * 检查元素、属性等名称非空白，不改变合法名称本身。
	 *
	 * @param value 待检查名称。
	 * @param name 对外参数名，用于诊断。
	 * @return 原始名称文本。
	 * @throws IllegalArgumentException 名称为空引用或全为空白。
	 */
	static String requireName(String value, String name) {
		String text = requireText(value, name);
		if (text.trim().length() == 0) {
			throw new IllegalArgumentException(name + "不得为空文本或空白。");
		}

		return text;
	}

	/**
	 * 将空编码转换成 jsoup 的自动识别约定。
	 *
	 * @param encoding 编码文本，空文本表示自动识别。
	 * @return 规范字符集名称，自动识别时为空引用。
	 * @throws IllegalArgumentException 编码为空引用或不是已支持的字符集。
	 */
	private static String charsetName(String encoding) {
		String name = requireText(encoding, "编码");
		return name.length() == 0 ? null : Charset.forName(name).name();
	}
}
