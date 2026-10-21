package simple.library.wsd.jsoup;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Selector.SelectorParseException;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * HTML 元素的查询、遍历与修改视图，由文档或查询结果取得。
 *
 * <p>多个包装对象可能指向同一底层元素，修改会反映到所属文档。
 * 查询结果列表固定成员引用，不随 DOM 增删自动更新；同一文档应串行访问。
 * 文本读取按 HTML 规则合并空白，输出 HTML 不保证保留原始排版。</p>
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public class HTML元素 {

	final Element element;

	/**
	 * 包装已有元素，不复制所属文档。
	 *
	 * @param element 已存在且非空的 jsoup 元素。
	 */
	HTML元素(Element element) {
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
	 * @param name 属性名称，不得为空引用或空文本。
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
	 * 将当前元素从父节点移除；已有包装引用仍可使用。
	 *
	 */
	@SimpleFunction
	public void 移除() {
		if (element.parent() != null) {
			element.remove();
		}
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
}
