package simple.library.wsd.jsoup;

import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.Entities.EscapeMode;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 控制 HTML 序列化的排版、编码、实体和 HTML/XML 语法；应用到文档时复制。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class HTML输出配置 {

	final OutputSettings settings;

	/**
	 * 创建默认 UTF-8、HTML 和美化排版配置。
	 */
	public HTML输出配置() {
		this(new OutputSettings());
	}

	/**
	 * 复制已有输出配置。
	 *
	 * @param settings 当前设置。
	 */
	HTML输出配置(OutputSettings settings) {
		this.settings = settings.clone();
	}

	/**
	 * 输出 HTML 语法。
	 */
	@SimpleDataElement
	public static final int 语法_HTML = 0;

	/**
	 * 输出 XML 语法。
	 */
	@SimpleDataElement
	public static final int 语法_XML = 1;

	/**
	 * 使用常见 HTML 实体。
	 */
	@SimpleDataElement
	public static final int 实体_基本 = 0;

	/**
	 * 使用扩展 HTML 实体。
	 */
	@SimpleDataElement
	public static final int 实体_扩展 = 1;

	/**
	 * 使用 XHTML 支持的实体。
	 */
	@SimpleDataElement
	public static final int 实体_XHTML = 2;

	/**
	 * 读取是否插入排版换行和缩进。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 美化排版() {
		return settings.prettyPrint();
	}

	/**
	 * 设置是否插入排版换行和缩进。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty
	public void 美化排版(boolean value) {
		settings.prettyPrint(value);
	}

	/**
	 * 读取是否把各标签按块排版。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 大纲排版() {
		return settings.outline();
	}

	/**
	 * 设置是否把各标签按块排版。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty
	public void 大纲排版(boolean value) {
		settings.outline(value);
	}

	/**
	 * 读取每层缩进空格数，必须非负。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 缩进宽度() {
		return settings.indentAmount();
	}

	/**
	 * 设置每层缩进空格数，必须非负。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 配置值无效。
	 */
	@SimpleProperty
	public void 缩进宽度(int value) {
		settings.indentAmount(value);
	}

	/**
	 * 读取缩进总宽度上限，-1 表示不限。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 最大缩进宽度() {
		return settings.maxPaddingWidth();
	}

	/**
	 * 设置缩进总宽度上限，-1 表示不限。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 配置值无效。
	 */
	@SimpleProperty
	public void 最大缩进宽度(int value) {
		if (value < -1) {
			throw new IllegalArgumentException("最大缩进宽度必须为 -1 或非负整数。");
		}
		settings.maxPaddingWidth(value);
	}

	/**
	 * 读取输出字符集名称；不修改已解析的文本。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 编码() {
		return settings.charset().name();
	}

	/**
	 * 设置输出字符集名称；不修改已解析的文本。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 配置值无效。
	 */
	@SimpleProperty
	public void 编码(String value) {
		settings.charset(HTML解析器.requireName(value, "编码"));
	}

	/**
	 * 读取HTML 或 XML 输出语法，使用本类语法常量。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 语法() {
		return settings.syntax() == Syntax.html ? 0 : 1;
	}

	/**
	 * 设置HTML 或 XML 输出语法，使用本类语法常量。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 配置值无效。
	 */
	@SimpleProperty
	public void 语法(int value) {
		if (value < 0 || value > 1) {
			throw new IllegalArgumentException("未知输出语法。");
		}
		settings.syntax(value == 0 ? Syntax.html : Syntax.xml);
	}

	/**
	 * 读取实体转义模式，使用本类实体常量。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 实体模式() {
		return settings.escapeMode() == EscapeMode.base ? 0 : settings.escapeMode() == EscapeMode.extended ? 1 : 2;
	}

	/**
	 * 设置实体转义模式，使用本类实体常量。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 配置值无效。
	 */
	@SimpleProperty
	public void 实体模式(int value) {
		if (value < 0 || value > 2) {
			throw new IllegalArgumentException("未知实体模式。");
		}
		settings.escapeMode(value == 0 ? EscapeMode.base : value == 1 ? EscapeMode.extended : EscapeMode.xhtml);
	}

	/**
	 * 复制当前输出配置。
	 *
	 * @return 独立输出配置。
	 */
	@SimpleFunction
	public HTML输出配置 复制() {
		return new HTML输出配置(settings);
	}

}
