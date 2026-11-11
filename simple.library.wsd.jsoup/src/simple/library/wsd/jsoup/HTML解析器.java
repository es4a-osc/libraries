package simple.library.wsd.jsoup;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Entities;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import simple.runtime.android.MainActivity;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.errors.文件未存在错误;
import simple.runtime.errors.文件读写错误;
import simple.runtime.文件操作;

/**
 * 从内存、普通文件或应用资产创建 HTML 文档的同步函数集。
 *
 * <p>
 * 不访问网络、不执行脚本。文件读取和较大文档解析应由调用者放到后台线程。 每次解析返回独立文档；同一文档及其元素不保证并发读写安全。
 * </p>
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
	 * @param html    HTML 文本，不得为空引用；允许空文本。
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
	 * @param html    HTML 文本，不得为空引用；允许空文本。
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
	 * @param data     完整 HTML 字节，不得为空引用。
	 * @param encoding 字符集名称；空文本按 BOM 或 HTML 编码声明识别，未识别时使用 UTF-8。
	 * @param baseUri  用于补全相对链接的基本 URL，空文本表示未指定。
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
	 * @param path     普通文件路径；相对路径以应用私有 files 目录为基准。
	 * @param encoding 字符集名称；空文本按 BOM 或 HTML 编码声明识别，未识别时使用 UTF-8。
	 * @param baseUri  用于补全相对链接的基本 URL，空文本表示未指定。
	 * @return 独立文档；不支持 content URI，文档不保留文件流。
	 * @throws IllegalArgumentException 路径、编码或基本 URL 参数无效。
	 * @throws 文件未存在错误                  文件不存在。
	 * @throws 文件读写错误                   文件无法读取。
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
	 * @param name     资产目录内的相对文件名，例如 books.html。
	 * @param encoding 字符集名称；空文本表示自动识别。
	 * @param baseUri  用于补全链接的基本 URL，空文本表示未指定。
	 * @return 独立文档，方法结束前关闭资产输入流。
	 * @throws IllegalArgumentException 参数无效。
	 * @throws 文件读写错误                   资产不存在或无法读取。
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
	 * @param name  对外参数名，用于诊断。
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
	 * @param name  对外参数名，用于诊断。
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

	/**
	 * 按独立配置解析 HTML 或 XML，不联网或执行脚本。
	 *
	 * @param html    完整文本。
	 * @param baseUri 基本 URL，允许空文本。
	 * @param config  解析配置，不得为空。
	 * @return 独立文档。
	 * @throws IllegalArgumentException 参数无效。
	 */
	@SimpleFunction
	public static HTML文档 按配置解析(String html, String baseUri, HTML解析配置 config) {
		if (config == null) {
			throw new IllegalArgumentException("解析配置不得为空。");
		}
		return new HTML文档(Jsoup.parse(requireText(html, "文本"), requireText(baseUri, "基本URL"), config.snapshot()));
	}

	/**
	 * 按独立配置从字节解析 HTML 或 XML，支持 BOM 和编码声明识别。
	 *
	 * @param data     完整原始字节。
	 * @param encoding 字符集名，空文本自动识别。
	 * @param baseUri  基本 URL，允许空文本。
	 * @param config   HTML 或 XML 解析配置。
	 * @return 独立文档。
	 * @throws IllegalArgumentException 参数或字符集无效；输入读取错误报告文件读写错误。
	 */
	@SimpleFunction
	public static HTML文档 按配置解析字节集(byte[] data, String encoding, String baseUri, HTML解析配置 config) {
		if (data == null || config == null) {
			throw new IllegalArgumentException("字节集和解析配置不得为空。");
		}
		String charset = charsetName(encoding);
		String uri = requireText(baseUri, "基本URL");
		try (ByteArrayInputStream input = new ByteArrayInputStream(data)) {
			return new HTML文档(Jsoup.parse(input, charset, uri, config.snapshot()));
		} catch (IOException error) {
			文件读写错误 failure = new 文件读写错误("读取字节集失败。");
			failure.initCause(error);
			throw failure;
		}
	}

	/**
	 * 读取普通文件后按配置解析 HTML 或 XML；不支持 content URI，文件错误沿用运行库错误类型。
	 *
	 * @param path     普通文件路径；相对路径使用运行库文件规则。
	 * @param encoding 字符集名，空文本自动识别。
	 * @param baseUri  基本 URL。
	 * @param config   解析配置。
	 * @return 独立文档。
	 * @throws IllegalArgumentException 参数无效；文件不存在或读写失败使用对应运行库错误。
	 */
	@SimpleFunction
	public static HTML文档 按配置解析文件(String path, String encoding, String baseUri, HTML解析配置 config) {
		if (config == null) {
			throw new IllegalArgumentException("解析配置不得为空。");
		}
		charsetName(encoding);
		requireText(baseUri, "基本URL");
		return 按配置解析字节集(文件操作.读入字节文件(requireText(path, "路径")), encoding, baseUri, config);
	}

	/**
	 * 读取随 APK 打包的资产后按配置解析 HTML 或 XML；结束前关闭输入流。
	 *
	 * @param name     资产相对路径。
	 * @param encoding 字符集名，空文本自动识别。
	 * @param baseUri  基本 URL。
	 * @param config   解析配置。
	 * @return 独立文档。
	 * @throws IllegalArgumentException 参数无效；资产读取失败报告文件读写错误。
	 */
	@SimpleFunction
	public static HTML文档 按配置解析资产(String name, String encoding, String baseUri, HTML解析配置 config) {
		if (config == null) {
			throw new IllegalArgumentException("解析配置不得为空。");
		}
		String asset = requireText(name, "资产名");
		String charset = charsetName(encoding);
		String uri = requireText(baseUri, "基本URL");
		try (InputStream input = MainActivity.getContext().getAssets().open(asset)) {
			return new HTML文档(Jsoup.parse(input, charset, uri, config.snapshot()));
		} catch (IOException error) {
			文件读写错误 failure = new 文件读写错误("读取资产失败：" + asset);
			failure.initCause(error);
			throw failure;
		}
	}

	/**
	 * 按允许名单清洗正文片段；只适用于 HTML 正文，不适用于脚本、样式或 URL 字符串。
	 *
	 * @param html     HTML 正文片段。
	 * @param baseUri  补全并校验相对链接的基本 URL。
	 * @param safelist 允许名单。
	 * @param settings 输出配置。
	 * @return 清洗后的 HTML 正文片段。
	 * @throws IllegalArgumentException 参数无效。
	 */
	@SimpleFunction
	public static String 清洗片段(String html, String baseUri, HTML安全名单 safelist, HTML输出配置 settings) {
		if (safelist == null || settings == null) {
			throw new IllegalArgumentException("名单和输出配置不得为空。");
		}
		return Jsoup.clean(requireText(html, "HTML文本"), requireText(baseUri, "基本URL"), new Safelist(safelist.safelist),
				settings.settings.clone());
	}

	/**
	 * 检查正文片段是否符合允许名单，不返回清洗后的文本。
	 *
	 * @param html     待检查的正文片段。
	 * @param safelist 允许名单。
	 * @return 符合上述条件时为真。
	 * @throws IllegalArgumentException 参数无效。
	 */
	@SimpleFunction
	public static boolean 片段是否安全(String html, HTML安全名单 safelist) {
		if (safelist == null) {
			throw new IllegalArgumentException("名单不得为空。");
		}
		return Jsoup.isValid(requireText(html, "HTML文本"), new Safelist(safelist.safelist));
	}

	/**
	 * 将纯文本转义为 HTML 文本内容；不作为脚本、样式或 URL 编码器。
	 *
	 * @param text     纯文本。
	 * @param settings 字符集和实体设置。
	 * @return 适用于 HTML 文本内容的转义结果。
	 * @throws IllegalArgumentException 参数无效。
	 */
	@SimpleFunction
	public static String 转义实体(String text, HTML输出配置 settings) {
		if (settings == null) {
			throw new IllegalArgumentException("输出配置不得为空。");
		}
		return Entities.escape(requireText(text, "文本"), settings.settings.clone());
	}

	/**
	 * 把命名和数字字符实体解码为文本，不解析 HTML 标签。
	 *
	 * @param text      包含字符实体的文本。
	 * @param attribute 是否按属性值上下文解码。
	 * @return 解码后的纯文本。
	 * @throws IllegalArgumentException 文本为空引用。
	 */
	@SimpleFunction
	public static String 解码实体(String text, boolean attribute) {
		return Parser.unescapeEntities(requireText(text, "文本"), attribute);
	}

	/**
	 * 创建独立节点，不自动加入文档。
	 *
	 * @param text 节点原始内容，不得为空。
	 * @return 对应节点视图，不存在时返回空引用。
	 * @throws IllegalArgumentException 内容为空引用。
	 */
	@SimpleFunction
	public static HTML节点 创建文本节点(String text) {
		return HTML节点.wrap(new TextNode(requireText(text, "内容")));
	}

	/**
	 * 创建独立节点，不自动加入文档。
	 *
	 * @param text 节点原始内容，不得为空。
	 * @return 对应节点视图，不存在时返回空引用。
	 * @throws IllegalArgumentException 内容为空引用。
	 */
	@SimpleFunction
	public static HTML节点 创建注释节点(String text) {
		return HTML节点.wrap(new Comment(requireText(text, "内容")));
	}

	/**
	 * 创建独立节点，不自动加入文档。
	 *
	 * @param text 节点原始内容，不得为空。
	 * @return 对应节点视图，不存在时返回空引用。
	 * @throws IllegalArgumentException 内容为空引用。
	 */
	@SimpleFunction
	public static HTML节点 创建数据节点(String text) {
		return HTML节点.wrap(new DataNode(requireText(text, "内容")));
	}

	/**
	 * 校验单个 CSS 类名，避免空白导致添加多个类名的歧义。
	 *
	 * @param value 类名。
	 * @return 有效类名。
	 * @throws IllegalArgumentException 为空或包含空白。
	 */
	static String requireClass(String value) {
		requireName(value, "类名");
		for (int i = 0; i < value.length(); i++) {
			if (Character.isWhitespace(value.charAt(i))) {
				throw new IllegalArgumentException("这里只接受单个、不含空白的类名。");
			}
		}
		return value;
	}
}
