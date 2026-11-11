package simple.library.wsd.jsoup;

import org.jsoup.safety.Safelist;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * HTML 正文片段的标签、属性和协议允许名单；不适用于脚本、样式或 URL 字符串上下文。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class HTML安全名单 {

	final Safelist safelist;
	private boolean relativeLinks;

	/**
	 * 创建不允许任何标签的名单，仅保留安全文本。
	 */
	public HTML安全名单() {
		this(Safelist.none());
	}

	/**
	 * 复制允许名单。
	 *
	 * @param safelist 现有名单。
	 */
	private HTML安全名单(Safelist safelist) {
		this.safelist = new Safelist(safelist);
	}

	/**
	 * 不允许任何标签。
	 *
	 * @return 独立允许名单。
	 */
	@SimpleFunction
	public static HTML安全名单 创建纯文本() {
		return new HTML安全名单(Safelist.none());
	}

	/**
	 * 允许 b、em、i、strong、u。
	 *
	 * @return 独立允许名单。
	 */
	@SimpleFunction
	public static HTML安全名单 创建简单文本() {
		return new HTML安全名单(Safelist.simpleText());
	}

	/**
	 * 允许常见正文和链接，不允许图片。
	 *
	 * @return 独立允许名单。
	 */
	@SimpleFunction
	public static HTML安全名单 创建基本() {
		return new HTML安全名单(Safelist.basic());
	}

	/**
	 * 在基本名单上允许图片。
	 *
	 * @return 独立允许名单。
	 */
	@SimpleFunction
	public static HTML安全名单 创建带图片基本() {
		return new HTML安全名单(Safelist.basicWithImages());
	}

	/**
	 * 允许较丰富正文、图片和表格；仍不允许脚本。
	 *
	 * @return 独立允许名单。
	 */
	@SimpleFunction
	public static HTML安全名单 创建宽松() {
		return new HTML安全名单(Safelist.relaxed());
	}

	/**
	 * 调整名单中的一个标签。
	 *
	 * @param tag 单个标签名称。
	 * @throws IllegalArgumentException 标签为空引用或空白。
	 */
	@SimpleFunction
	public void 添加标签(String tag) {
		safelist.addTags(HTML解析器.requireName(tag, "标签"));
	}

	/**
	 * 调整名单中的一个标签。
	 *
	 * @param tag 单个标签名称。
	 * @throws IllegalArgumentException 标签为空引用或空白。
	 */
	@SimpleFunction
	public void 移除标签(String tag) {
		safelist.removeTags(HTML解析器.requireName(tag, "标签"));
	}

	/**
	 * 调整某类标签允许的属性。
	 *
	 * @param tag       标签名；:all 表示所有允许标签。
	 * @param attribute 单个属性名。
	 * @throws IllegalArgumentException 参数无效。
	 */
	@SimpleFunction
	public void 添加属性(String tag, String attribute) {
		safelist.addAttributes(HTML解析器.requireName(tag, "标签"), HTML解析器.requireName(attribute, "属性名"));
	}

	/**
	 * 调整某类标签允许的属性。
	 *
	 * @param tag       标签名；:all 表示所有允许标签。
	 * @param attribute 单个属性名。
	 * @throws IllegalArgumentException 参数无效。
	 */
	@SimpleFunction
	public void 移除属性(String tag, String attribute) {
		safelist.removeAttributes(HTML解析器.requireName(tag, "标签"), HTML解析器.requireName(attribute, "属性名"));
	}

	/**
	 * 为清洗后的指定标签强制设置属性。
	 *
	 * @param tag       标签名。
	 * @param attribute 属性名。
	 * @param value     固定值，允许空文本。
	 * @throws IllegalArgumentException 参数无效。
	 */
	@SimpleFunction
	public void 强制属性(String tag, String attribute, String value) {
		safelist.addEnforcedAttribute(HTML解析器.requireName(tag, "标签"), HTML解析器.requireName(attribute, "属性名"),
				HTML解析器.requireText(value, "属性值"));
	}

	/**
	 * 取消某标签的强制属性。
	 *
	 * @param tag       标签名。
	 * @param attribute 属性名。
	 * @throws IllegalArgumentException 参数无效。
	 */
	@SimpleFunction
	public void 移除强制属性(String tag, String attribute) {
		safelist.removeEnforcedAttribute(HTML解析器.requireName(tag, "标签"), HTML解析器.requireName(attribute, "属性名"));
	}

	/**
	 * 调整链接属性允许的协议；放宽名单前应核对内容来源。
	 *
	 * @param tag       标签名。
	 * @param attribute 链接属性名。
	 * @param protocol  单个协议名，不含冒号；# 表示锚点。
	 * @throws IllegalArgumentException 参数无效。
	 */
	@SimpleFunction
	public void 添加协议(String tag, String attribute, String protocol) {
		safelist.addProtocols(HTML解析器.requireName(tag, "标签"), HTML解析器.requireName(attribute, "属性名"),
				HTML解析器.requireName(protocol, "协议"));
	}

	/**
	 * 调整链接属性允许的协议；放宽名单前应核对内容来源。
	 *
	 * @param tag       标签名。
	 * @param attribute 链接属性名。
	 * @param protocol  单个协议名，不含冒号；# 表示锚点。
	 * @throws IllegalArgumentException 参数无效。
	 */
	@SimpleFunction
	public void 移除协议(String tag, String attribute, String protocol) {
		safelist.removeProtocols(HTML解析器.requireName(tag, "标签"), HTML解析器.requireName(attribute, "属性名"),
				HTML解析器.requireName(protocol, "协议"));
	}

	/**
	 * 读取是否保留相对链接文本；协议校验仍需要有效基本 URL。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 保留相对链接() {
		return relativeLinks;
	}

	/**
	 * 设置是否保留相对链接文本；协议校验仍需要有效基本 URL。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty
	public void 保留相对链接(boolean value) {
		safelist.preserveRelativeLinks(value);
		relativeLinks = value;
	}

	/**
	 * 复制名单，之后修改互不影响。
	 *
	 * @return 独立允许名单。
	 */
	@SimpleFunction
	public HTML安全名单 复制() {
		HTML安全名单 result = new HTML安全名单(safelist);
		result.relativeLinks = relativeLinks;
		return result;
	}

}
