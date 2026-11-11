package simple.library.wsd.jsoup;

import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Parser;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 配置 HTML 或 XML 解析、大小写、错误记录和源码位置；解析时复制配置，不共享解析器状态。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class HTML解析配置 {

	private final Parser parser;
	private int errorLimit;

	/**
	 * 创建默认 HTML 解析配置。
	 */
	public HTML解析配置() {
		this(Parser.htmlParser());
	}

	/**
	 * 保存独立解析器。
	 *
	 * @param parser 新的解析器。
	 */
	private HTML解析配置(Parser parser) {
		this.parser = parser;
	}

	/**
	 * 创建 XML 配置，默认保留标签和属性大小写。
	 *
	 * @return 独立解析配置。
	 */
	@SimpleFunction
	public static HTML解析配置 创建XML配置() {
		return new HTML解析配置(Parser.xmlParser());
	}

	/**
	 * 读取标签大小写保留状态。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 保留标签大小写() {
		return parser.settings().preserveTagCase();
	}

	/**
	 * 设置标签大小写保留状态。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty
	public void 保留标签大小写(boolean value) {
		parser.settings(new ParseSettings(value, parser.settings().preserveAttributeCase()));
	}

	/**
	 * 读取属性大小写保留状态。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 保留属性大小写() {
		return parser.settings().preserveAttributeCase();
	}

	/**
	 * 设置属性大小写保留状态。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty
	public void 保留属性大小写(boolean value) {
		parser.settings(new ParseSettings(parser.settings().preserveTagCase(), value));
	}

	/**
	 * 读取源码位置记录状态；后续 DOM 修改不会更新原始位置。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 记录源码位置() {
		return parser.isTrackPosition();
	}

	/**
	 * 设置源码位置记录状态；后续 DOM 修改不会更新原始位置。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty
	public void 记录源码位置(boolean value) {
		parser.setTrackPosition(value);
	}

	/**
	 * 读取解析错误记录上限；零表示不记录，必须非负。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 错误记录上限() {
		return errorLimit;
	}

	/**
	 * 设置解析错误记录上限；零表示不记录，必须非负。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 配置值无效。
	 */
	@SimpleProperty
	public void 错误记录上限(int value) {
		if (value < 0) {
			throw new IllegalArgumentException("错误记录上限不得小于零。");
		}
		errorLimit = value;
		parser.setTrackErrors(value);
	}

	/**
	 * 复制当前配置，之后修改互不影响。
	 *
	 * @return 独立解析配置。
	 */
	@SimpleFunction
	public HTML解析配置 复制() {
		HTML解析配置 result = new HTML解析配置(parser.newInstance());
		result.errorLimit = errorLimit;
		return result;
	}

	/**
	 * 为一次解析创建独立解析器。
	 *
	 * @return 无共享解析进度的解析器。
	 */
	Parser snapshot() {
		return parser.newInstance();
	}
}
