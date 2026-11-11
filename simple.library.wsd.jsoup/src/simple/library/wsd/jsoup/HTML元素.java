package simple.library.wsd.jsoup;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.Connection.KeyVal;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.select.Selector.SelectorParseException;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * HTML 元素的查询、遍历与修改视图，由文档或查询结果取得。
 *
 * <p>
 * 多个包装对象可能指向同一底层元素，修改会反映到所属文档。 查询结果列表固定成员引用，不随 DOM 增删自动更新；同一文档应串行访问。 文本读取按 HTML 规则合并空白，输出 HTML
 * 不保证保留原始排版。
 * </p>
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public class HTML元素 extends HTML节点 {

	final Element element;

	/**
	 * 包装已有元素，不复制所属文档。
	 *
	 * @param element 已存在且非空的 jsoup 元素。
	 */
	HTML元素(Element element) {
		super(element);
		this.element = element;
	}

	/**
	 * 包装可空元素，并保留文档的实际类型。
	 *
	 * @param element 待包装元素。
	 * @return 元素视图；输入为空时返回空引用。
	 */
	static HTML元素 wrap(Element element) {
		if (element == null) {
			return null;
		}

		return element instanceof Document ? new HTML文档((Document) element) : new HTML元素(element);
	}

	/* CSS 查询及按名称定位。 */

	/**
	 * 以当前元素为查询上下文，按 CSS 选择器查找元素。
	 *
	 * @param selector jsoup 1.15.4 支持的 CSS 选择器，不得为空引用或空文本。
	 * @return 匹配元素的成员快照；无匹配时数量为 0。
	 * @throws IllegalArgumentException 选择器无效。
	 */
	@SimpleFunction
	public HTML元素列表 查询元素(String selector) {
		HTML解析器.requireName(selector, "选择器");

		try {
			return new HTML元素列表(element.select(selector));
		} catch (SelectorParseException error) {
			throw new IllegalArgumentException("CSS 选择器无效：" + selector, error);
		}
	}

	/**
	 * 以当前元素为查询上下文，按 CSS 选择器查找元素。
	 *
	 * @param selector jsoup 1.15.4 支持的 CSS 选择器，不得为空引用或空文本。
	 * @return 首个匹配元素；无匹配时返回空引用。
	 * @throws IllegalArgumentException 选择器无效。
	 */
	@SimpleFunction
	public HTML元素 查询首个(String selector) {
		HTML解析器.requireName(selector, "选择器");

		try {
			return wrap(element.selectFirst(selector));
		} catch (SelectorParseException error) {
			throw new IllegalArgumentException("CSS 选择器无效：" + selector, error);
		}
	}

	/**
	 * 以当前元素为查询上下文，按 CSS 选择器判断当前元素。
	 *
	 * @param selector jsoup 1.15.4 支持的 CSS 选择器，不得为空引用或空文本。
	 * @return 当前元素符合选择器时为真。
	 * @throws IllegalArgumentException 选择器无效。
	 */
	@SimpleFunction
	public boolean 是否匹配(String selector) {
		HTML解析器.requireName(selector, "选择器");

		try {
			return element.is(selector);
		} catch (SelectorParseException error) {
			throw new IllegalArgumentException("CSS 选择器无效：" + selector, error);
		}
	}

	/**
	 * 查找当前元素及后代中具有指定 ID 的首个元素。
	 *
	 * @param id 要查找的 ID。
	 * @return 匹配元素，不存在时返回空引用。
	 * @throws IllegalArgumentException 参数为空引用或名称无效。
	 */
	@SimpleFunction
	public HTML元素 取节点自ID(String id) {
		return wrap(element.getElementById(HTML解析器.requireText(id, "ID")));
	}

	/**
	 * 查找当前元素及后代中具有指定标签的元素。
	 *
	 * @param tag 标签名称，例如 a、li。
	 * @return 匹配列表，无匹配时为空列表。
	 * @throws IllegalArgumentException 参数为空引用或名称无效。
	 */
	@SimpleFunction
	public HTML元素列表 取节点自TAG(String tag) {
		return new HTML元素列表(element.getElementsByTag(HTML解析器.requireName(tag, "标签")));
	}

	/**
	 * 查找当前元素及后代中具有指定类名的元素。
	 *
	 * @param className 单个 CSS 类名，不含前导句点。
	 * @return 匹配列表，无匹配时为空列表。
	 * @throws IllegalArgumentException 参数为空引用或名称无效。
	 */
	@SimpleFunction
	public HTML元素列表 取元素自类名(String className) {
		return new HTML元素列表(element.getElementsByClass(HTML解析器.requireName(className, "类名")));
	}

	/* 文本与标记；置文本会转义内容，置内部 HTML 会解析标记。 */

	/**
	 * 取得当前元素的标签名。
	 *
	 * @return 标签名。
	 */
	@SimpleFunction
	public String 取标签名称() {
		return element.tagName();
	}

	/**
	 * 取得当前元素的 id 属性。
	 *
	 * @return ID 文本，未设置时为空文本。
	 */
	@SimpleFunction
	public String 取ID() {
		return element.id();
	}

	/**
	 * 取得当前元素直接包含的文本，不包含子元素文本。
	 *
	 * @return 已解码且合并空白的直接文本。
	 */
	@SimpleFunction
	public String 取内容() {
		return element.ownText();
	}

	/**
	 * 取得当前元素及后代的可见文本，合并多余空白。
	 *
	 * @return 文本内容，不包含脚本和样式数据。
	 */
	@SimpleFunction
	public String 取所有文本() {
		return element.text();
	}

	/**
	 * 取得当前元素及后代文本，保留文本节点中的空白。
	 *
	 * @return 文本节点内容；不是原始 HTML 源码。
	 */
	@SimpleFunction
	public String 取原始文本() {
		return element.wholeText();
	}

	/**
	 * 序列化当前元素内部的 HTML，不包含外层标签。
	 *
	 * @return 内部 HTML 文本。
	 */
	@SimpleFunction
	public String 取内部HTML() {
		return element.html();
	}

	/**
	 * 序列化当前元素及后代，包括外层标签。
	 *
	 * @return 完整元素 HTML 文本。
	 */
	@SimpleFunction
	public String 取HTML文本() {
		return element.outerHtml();
	}

	/**
	 * 取得当前元素用于补全相对链接的基本 URL。
	 *
	 * @return 基本 URL，未指定时为空文本。
	 */
	@SimpleFunction
	public String 取基本URL() {
		return element.baseUri();
	}

	/**
	 * 修改当前元素的标签名。
	 *
	 * @param tag 新的标签名称。
	 * @throws IllegalArgumentException 参数为空引用或名称无效。
	 */
	@SimpleFunction
	public void 置标签名称(String tag) {
		element.tagName(HTML解析器.requireName(tag, "标签"));
	}

	/**
	 * 设置当前元素的 id 属性。
	 *
	 * @param id 新的 id 属性，空文本允许。
	 * @throws IllegalArgumentException 参数为空引用或名称无效。
	 */
	@SimpleFunction
	public void 置ID(String id) {
		element.id(HTML解析器.requireText(id, "ID"));
	}

	/**
	 * 用纯文本替换当前元素的全部子节点。
	 *
	 * @param text 新的纯文本，HTML 特殊字符会被转义。
	 * @throws IllegalArgumentException 参数为空引用或名称无效。
	 */
	@SimpleFunction
	public void 置文本(String text) {
		element.text(HTML解析器.requireText(text, "文本"));
	}

	/**
	 * 解析 HTML 并替换当前元素的全部子节点。
	 *
	 * @param html HTML 文本，不得为空引用；允许空文本。
	 * @throws IllegalArgumentException 参数为空引用或名称无效。
	 */
	@SimpleFunction
	public void 置内部HTML(String html) {
		element.html(HTML解析器.requireText(html, "HTML文本"));
	}

	/* 属性及链接；读取属性不会发起网络请求。 */

	/**
	 * 读取当前元素的属性值。
	 *
	 * @param name 属性名称，不得为空引用或空文本。
	 * @return 属性值；不存在时返回空文本，需区分时先调用是否有属性。
	 * @throws IllegalArgumentException 属性名无效。
	 */
	@SimpleFunction
	public String 取属性值(String name) {
		return element.attr(HTML解析器.requireName(name, "属性名"));
	}

	/**
	 * 判断属性是否存在，包括值为空文本的属性。
	 *
	 * @param name 属性名称，不得为空引用或空文本。
	 * @return 属性存在时为真。
	 * @throws IllegalArgumentException 属性名无效。
	 */
	@SimpleFunction
	public boolean 是否有属性(String name) {
		return element.hasAttr(HTML解析器.requireName(name, "属性名"));
	}

	/**
	 * 将指定链接属性解析为绝对 URL，不读取目标资源。
	 *
	 * @param name 属性名称，不得为空引用或空文本。
	 * @return 绝对 URL；属性不存在或无法解析时为空文本。
	 * @throws IllegalArgumentException 属性名无效。
	 */
	@SimpleFunction
	public String 取URL(String name) {
		return element.absUrl(HTML解析器.requireName(name, "属性名"));
	}

	/**
	 * 新增或覆盖当前元素属性，不进行网络操作。
	 *
	 * @param name  属性名称，不得为空引用或空文本。
	 * @param value 新的属性值，允许空文本。
	 * @throws IllegalArgumentException 参数无效。
	 */
	@SimpleFunction
	public void 置属性值(String name, String value) {
		element.attr(HTML解析器.requireName(name, "属性名"), HTML解析器.requireText(value, "属性值"));
	}

	/**
	 * 移除指定属性，不存在时不处理。
	 *
	 * @param name 属性名称，不得为空引用或空文本。
	 * @throws IllegalArgumentException 属性名无效。
	 */
	@SimpleFunction
	public void 移除属性(String name) {
		element.removeAttr(HTML解析器.requireName(name, "属性名"));
	}

	/**
	 * 按当前属性顺序取得名称快照。
	 *
	 * @return 属性名称数组，索引从 0 开始。
	 */
	@SimpleFunction
	public String[] 取属性名列表() {
		List<String> names = new ArrayList<String>();
		for (Attribute attribute : element.attributes()) {
			names.add(attribute.getKey());
		}

		return names.toArray(new String[names.size()]);
	}

	/* 父子与相邻元素；这里的子元素不包含独立文本节点或注释。 */

	/**
	 * 取得父元素。
	 *
	 * @return 父元素，文档根没有父元素时为空引用。
	 */
	@SimpleFunction
	public HTML元素 取父元素() {
		return wrap(element.parent());
	}

	/**
	 * 取得直接子元素的成员快照。
	 *
	 * @return 子元素列表，索引从 0 开始。
	 */
	@SimpleFunction
	public HTML元素列表 取子元素() {
		return new HTML元素列表(element.children());
	}

	/**
	 * 取得同级上一个元素，跳过文本和注释节点。
	 *
	 * @return 相邻元素，不存在时为空引用。
	 */
	@SimpleFunction
	public HTML元素 取上一元素() {
		return wrap(element.previousElementSibling());
	}

	/**
	 * 取得同级下一个元素，跳过文本和注释节点。
	 *
	 * @return 相邻元素，不存在时为空引用。
	 */
	@SimpleFunction
	public HTML元素 取下一元素() {
		return wrap(element.nextElementSibling());
	}

	/**
	 * 按索引取得直接子元素。
	 *
	 * @param index 从 0 开始的直接子元素索引。
	 * @return 对应元素。
	 * @throws ArrayIndexOutOfBoundsException 索引小于 0 或不小于子元素数量。
	 */
	@SimpleFunction
	public HTML元素 取指定子元素(int index) {
		return 取子元素().取成员(index);
	}

	/* 文档树修改；已有引用仍指向被移除或替换下来的元素。 */

	/**
	 * 把 HTML 片段解析后追加到当前元素内部末尾。
	 *
	 * @param html HTML 文本，不得为空引用；允许空文本。
	 * @throws IllegalArgumentException HTML 文本为空引用。
	 */
	@SimpleFunction
	public void 追加HTML(String html) {
		element.append(HTML解析器.requireText(html, "HTML文本"));
	}

	/**
	 * 把 HTML 片段解析后插入当前元素内部首部。
	 *
	 * @param html HTML 文本，不得为空引用；允许空文本。
	 * @throws IllegalArgumentException HTML 文本为空引用。
	 */
	@SimpleFunction
	public void 插入HTML到首部(String html) {
		element.prepend(HTML解析器.requireText(html, "HTML文本"));
	}

	/**
	 * 在当前元素内部末尾添加一个空元素。
	 *
	 * @param tag 新子元素的标签名称。
	 * @return 已加入当前文档树的新元素。
	 * @throws IllegalArgumentException 标签名称无效。
	 */
	@SimpleFunction
	public HTML元素 创建子元素(String tag) {
		return wrap(element.appendElement(HTML解析器.requireName(tag, "标签")));
	}

	/**
	 * 深复制当前元素及后代，修改副本不会改变原元素。
	 *
	 * @return 独立元素副本。
	 */
	@SimpleFunction
	public HTML元素 复制() {
		return wrap(element.clone());
	}

	/**
	 * 按 XPath 查询元素，结果是成员快照；文本节点请调用查询XPath节点。
	 *
	 * @param xpath XPath 表达式，返回元素节点。
	 * @return 按查询顺序排列的成员快照。
	 * @throws IllegalArgumentException 表达式无效。
	 */
	@SimpleFunction
	public HTML元素列表 查询XPath(String xpath) {
		try {
			return new HTML元素列表(element.selectXpath(HTML解析器.requireName(xpath, "XPath")));
		} catch (SelectorParseException error) {
			throw new IllegalArgumentException("选择器无效。", error);
		}
	}

	/**
	 * 按 XPath 取得节点快照数组；仅返回 DOM 节点，不返回数值或文本标量。
	 *
	 * @param xpath 选择元素、文本或注释节点的 XPath。
	 * @return 按匹配顺序排列的节点视图数组，无匹配时为空数组。
	 * @throws IllegalArgumentException 表达式无效或结果不是节点集合。
	 */
	@SimpleFunction
	public HTML节点[] 查询XPath节点(String xpath) {
		try {
			List<Node> selected = element.selectXpath(HTML解析器.requireName(xpath, "XPath"), Node.class);
			HTML节点[] result = new HTML节点[selected.size()];
			for (int i = 0; i < result.length; i++) {
				result[i] = HTML节点.wrap(selected.get(i));
			}
			return result;
		} catch (SelectorParseException error) {
			throw new IllegalArgumentException("选择器无效。", error);
		}
	}

	/**
	 * 从自身向父级查找首个符合选择器的元素，找不到返回空引用。
	 *
	 * @param selector CSS 选择器。
	 * @return 对应元素视图，不存在时返回空引用。
	 * @throws IllegalArgumentException 选择器无效。
	 */
	@SimpleFunction
	public HTML元素 取最近匹配(String selector) {
		try {
			return wrap(element.closest(HTML解析器.requireName(selector, "选择器")));
		} catch (SelectorParseException error) {
			throw new IllegalArgumentException("选择器无效。", error);
		}
	}

	/**
	 * 取得所有祖先元素，近到远排列。
	 *
	 * @return 按查询顺序排列的成员快照。
	 */
	@SimpleFunction
	public HTML元素列表 取祖先元素() {
		return new HTML元素列表(element.parents());
	}

	/**
	 * 取得同级元素，不包含自身。
	 *
	 * @return 按查询顺序排列的成员快照。
	 */
	@SimpleFunction
	public HTML元素列表 取同级元素() {
		return new HTML元素列表(element.siblingElements());
	}

	/**
	 * 取得后续同级元素。
	 *
	 * @return 按查询顺序排列的成员快照。
	 */
	@SimpleFunction
	public HTML元素列表 取后续元素() {
		return new HTML元素列表(element.nextElementSiblings());
	}

	/**
	 * 取得先前同级元素。
	 *
	 * @return 按查询顺序排列的成员快照。
	 */
	@SimpleFunction
	public HTML元素列表 取先前元素() {
		return new HTML元素列表(element.previousElementSiblings());
	}

	/**
	 * 取得当前元素的CSS路径。
	 *
	 * @return 用于定位当前元素的 CSS 选择器。
	 */
	@SimpleFunction
	public String 取CSS路径() {
		return element.cssSelector();
	}

	/**
	 * 取得当前元素的类名。
	 *
	 * @return class 属性文本。
	 */
	@SimpleFunction
	public String 取类名() {
		return element.className();
	}

	/**
	 * 取得当前元素的原始直接文本。
	 *
	 * @return 保留原始空白的直接文本。
	 */
	@SimpleFunction
	public String 取原始直接文本() {
		return element.wholeOwnText();
	}

	/**
	 * 取得当前元素的原始数据；原始数据可能包含脚本和样式，读取不会执行。
	 *
	 * @return 脚本、样式等数据文本。
	 */
	@SimpleFunction
	public String 取原始数据() {
		return element.data();
	}

	/**
	 * 取得当前元素的表单值。
	 *
	 * @return 表单控件的当前值。
	 */
	@SimpleFunction
	public String 取表单值() {
		return element.val();
	}

	/**
	 * 设置元素值，不触发表单提交。
	 *
	 * @param value input 值或 textarea 文本。
	 * @throws IllegalArgumentException 值为空引用。
	 */
	@SimpleFunction
	public void 置表单值(String value) {
		element.val(HTML解析器.requireText(value, "值"));
	}

	/**
	 * 按逻辑状态设置 HTML 属性。
	 *
	 * @param name  逻辑属性名，例如 disabled、checked。
	 * @param value 真表示保留空值属性，假表示移除属性。
	 * @throws IllegalArgumentException 属性名无效。
	 */
	@SimpleFunction
	public void 置逻辑属性(String name, boolean value) {
		element.attr(HTML解析器.requireName(name, "属性名"), value);
	}

	/**
	 * 移除当前元素全部属性，保留元素与子节点。
	 */
	@SimpleFunction
	public void 清空属性() {
		element.clearAttributes();
	}

	/**
	 * 按当前元素上下文解析为独立节点数组，不自动插入；表格片段和所属 XML 模式会影响解析。
	 *
	 * @param html HTML 或 XML 片段。
	 * @return 按片段顺序排列的新节点数组，不自动加入当前树。
	 * @throws IllegalArgumentException 片段为空引用。
	 */
	@SimpleFunction
	public HTML节点[] 按上下文解析片段(String html) {
		Document owner = element.ownerDocument();
		Parser parser = owner == null ? Parser.htmlParser() : owner.parser().newInstance();
		List<Node> nodes = parser.parseFragmentInput(HTML解析器.requireText(html, "片段"), element, element.baseUri());
		HTML节点[] result = new HTML节点[nodes.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = HTML节点.wrap(nodes.get(i));
		}
		return result;
	}

	/**
	 * 修改此元素的单个 CSS 类名。
	 *
	 * @param className 单个类名，不含空白。
	 * @throws IllegalArgumentException 类名无效。
	 */
	@SimpleFunction
	public void 添加类名(String className) {
		element.addClass(HTML解析器.requireClass(className));
	}

	/**
	 * 修改此元素的单个 CSS 类名。
	 *
	 * @param className 单个类名，不含空白。
	 * @throws IllegalArgumentException 类名无效。
	 */
	@SimpleFunction
	public void 移除类名(String className) {
		element.removeClass(HTML解析器.requireClass(className));
	}

	/**
	 * 修改此元素的单个 CSS 类名。
	 *
	 * @param className 单个类名，不含空白。
	 * @throws IllegalArgumentException 类名无效。
	 */
	@SimpleFunction
	public void 切换类名(String className) {
		element.toggleClass(HTML解析器.requireClass(className));
	}

	/**
	 * 判断元素是否具有指定类名。
	 *
	 * @param className 单个类名。
	 * @return 符合上述条件时为真。
	 * @throws IllegalArgumentException 类名无效。
	 */
	@SimpleFunction
	public boolean 是否有类名(String className) {
		return element.hasClass(HTML解析器.requireClass(className));
	}

	/**
	 * 取得所有 data- 属性的后缀名称快照，不含 data- 前缀。
	 *
	 * @return 按说明顺序排列的文本数组。
	 */
	@SimpleFunction
	public String[] 取数据属性名() {
		return element.dataset().keySet().toArray(new String[0]);
	}

	/**
	 * 读取 data- 属性；不存在时返回空文本。
	 *
	 * @param name 不含 data- 前缀的属性后缀。
	 * @return 对应数据属性值，不存在时为空文本。
	 * @throws IllegalArgumentException 属性名无效。
	 */
	@SimpleFunction
	public String 取数据属性(String name) {
		String value = element.dataset().get(HTML解析器.requireName(name, "数据属性名"));
		return value == null ? "" : value;
	}

	/**
	 * 设置 data- 属性。
	 *
	 * @param name  不含 data- 前缀的属性后缀。
	 * @param value 属性值。
	 * @throws IllegalArgumentException 参数无效。
	 */
	@SimpleFunction
	public void 置数据属性(String name, String value) {
		element.dataset().put(HTML解析器.requireName(name, "数据属性名"), HTML解析器.requireText(value, "数据属性值"));
	}

	/**
	 * 把独立或已有节点移入当前元素，禁止形成循环树。
	 *
	 * @param value 待移动节点，先复制可保留原位置。
	 * @throws IllegalArgumentException 节点无效或会形成循环。
	 */
	@SimpleFunction
	public void 追加节点(HTML节点 value) {
		checkInsertion(element, value);
		element.appendChild(value.node);
	}

	/**
	 * 把独立或已有节点移入当前元素，禁止形成循环树。
	 *
	 * @param value 待移动节点，先复制可保留原位置。
	 * @throws IllegalArgumentException 节点无效或会形成循环。
	 */
	@SimpleFunction
	public void 首部插入节点(HTML节点 value) {
		checkInsertion(element, value);
		element.prependChild(value.node);
	}

	/**
	 * 把节点移入指定位置；同父节点移动时索引表示移动前子节点序列的插入边界。
	 *
	 * @param index 插入位置，零到子节点数量，包含文本和注释。
	 * @param value 待移动节点，先复制可保留原位置。
	 * @throws IllegalArgumentException 节点无效或会形成循环。
	 */
	@SimpleFunction
	public void 按位置插入节点(int index, HTML节点 value) {
		if (index < 0 || index > element.childNodeSize()) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		checkInsertion(element, value);
		element.insertChildren(index, value.node);
	}

	/**
	 * 在当前元素的相邻位置解析插入 HTML；当前元素必须有父节点。
	 *
	 * @param html HTML 片段。
	 * @throws IllegalArgumentException 参数无效或无父节点。
	 */
	@SimpleFunction
	public void 前面插入HTML(String html) {
		if (element.parent() == null) {
			throw new IllegalArgumentException("当前元素没有父节点。");
		}
		element.before(HTML解析器.requireText(html, "HTML文本"));
	}

	/**
	 * 在当前元素的相邻位置解析插入 HTML；当前元素必须有父节点。
	 *
	 * @param html HTML 片段。
	 * @throws IllegalArgumentException 参数无效或无父节点。
	 */
	@SimpleFunction
	public void 后面插入HTML(String html) {
		if (element.parent() == null) {
			throw new IllegalArgumentException("当前元素没有父节点。");
		}
		element.after(HTML解析器.requireText(html, "HTML文本"));
	}

	/**
	 * 把纯文本插入当前元素内部，不解析为标签。
	 *
	 * @param text 纯文本，特殊字符自动转义。
	 * @throws IllegalArgumentException 文本为空引用。
	 */
	@SimpleFunction
	public void 追加文本(String text) {
		element.appendText(HTML解析器.requireText(text, "文本"));
	}

	/**
	 * 把纯文本插入当前元素内部，不解析为标签。
	 *
	 * @param text 纯文本，特殊字符自动转义。
	 * @throws IllegalArgumentException 文本为空引用。
	 */
	@SimpleFunction
	public void 首部插入文本(String text) {
		element.prependText(HTML解析器.requireText(text, "文本"));
	}

	/**
	 * 用 HTML 外层结构包裹此元素，元素移动到最内层首个元素中。
	 *
	 * @param html 具有外层元素的非空片段。
	 * @throws IllegalArgumentException 片段为空或不能构成包裹元素。
	 */
	@SimpleFunction
	public void 包裹HTML(String html) {
		element.wrap(HTML解析器.requireName(html, "包裹HTML"));
	}

	/**
	 * 移除此元素并把子节点提升到原父节点；返回提升后的首节点，无子节点则返回空。
	 *
	 * @return 对应节点视图，不存在时返回空引用。
	 * @throws IllegalArgumentException 当前无父节点。
	 */
	@SimpleFunction
	public HTML节点 解开包裹() {
		if (element.parent() == null) {
			throw new IllegalArgumentException("当前元素没有父节点。");
		}
		return HTML节点.wrap(element.unwrap());
	}

	/**
	 * 取得 form 关联控件的成员快照，仅用于 form 元素，不提交表单。
	 *
	 * @return 按查询顺序排列的成员快照。
	 * @throws IllegalArgumentException 当前元素不是 form。
	 */
	@SimpleFunction
	public HTML元素列表 取表单控件() {
		if (!(element instanceof FormElement)) {
			throw new IllegalArgumentException("当前元素不是表单。");
		}
		return new HTML元素列表(((FormElement) element).elements());
	}

	/**
	 * 把手动创建的控件关联到当前 form；不移动 DOM 位置，同一控件不会重复关联。
	 *
	 * @param control 已创建的控件元素。
	 * @throws IllegalArgumentException 当前不是 form 或控件为空。
	 */
	@SimpleFunction
	public void 关联表单控件(HTML元素 control) {
		if (!(element instanceof FormElement) || control == null) {
			throw new IllegalArgumentException("需要表单和非空控件。");
		}
		FormElement form = (FormElement) element;
		if (!form.elements().contains(control.element)) {
			form.addElement(control.element);
		}
	}

	/**
	 * 取得可提交表单字段的名称数组，按控件顺序排列并保留重名；与另一个字段数组按索引对应。不发起请求。
	 *
	 * @return 按说明顺序排列的文本数组。
	 * @throws IllegalArgumentException 当前元素不是 form。
	 */
	@SimpleFunction
	public String[] 取表单字段名() {
		if (!(element instanceof FormElement)) {
			throw new IllegalArgumentException("当前元素不是表单。");
		}
		List<KeyVal> data = ((FormElement) element).formData();
		String[] result = new String[data.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = data.get(i).key();
		}
		return result;
	}

	/**
	 * 取得可提交表单字段的值数组，按控件顺序排列并保留重名；与另一个字段数组按索引对应。不发起请求。
	 *
	 * @return 按说明顺序排列的文本数组。
	 * @throws IllegalArgumentException 当前元素不是 form。
	 */
	@SimpleFunction
	public String[] 取表单字段值() {
		if (!(element instanceof FormElement)) {
			throw new IllegalArgumentException("当前元素不是表单。");
		}
		List<KeyVal> data = ((FormElement) element).formData();
		String[] result = new String[data.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = data.get(i).value();
		}
		return result;
	}

}
