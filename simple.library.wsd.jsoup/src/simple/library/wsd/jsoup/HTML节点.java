package simple.library.wsd.jsoup;

import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeFilter;
import org.jsoup.select.NodeFilter.FilterResult;
import org.jsoup.select.NodeTraversor;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * DOM 节点视图，包含元素、文本、注释和原始数据；修改反映到所属文档。同一文档串行访问。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public class HTML节点 {

	final Node node;

	/**
	 * 包装现有节点。
	 *
	 * @param node 非空的原生节点。
	 */
	HTML节点(Node node) {
		this.node = node;
	}

	/**
	 * 包装节点，保留元素或文档的实际类型。
	 *
	 * @param node 可空节点。
	 * @return 对应视图；空节点返回空引用。
	 */
	static HTML节点 wrap(Node node) {
		if (node == null) {
			return null;
		}
		return node instanceof Element ? HTML元素.wrap((Element) node) : new HTML节点(node);
	}

	/**
	 * 取得原生节点名称，例如 p、#text、#comment。
	 *
	 * @return 节点名称，不包含尖括号。
	 */
	@SimpleFunction
	public String 取节点名称() {
		return node.nodeName();
	}

	/**
	 * 判断节点是否为元素或文档。
	 *
	 * @return 符合上述条件时为真。
	 */
	@SimpleFunction
	public boolean 是否元素() {
		return node instanceof Element;
	}

	/**
	 * 取得元素视图；文本、注释等节点返回空引用。
	 *
	 * @return 对应元素视图，不存在时返回空引用。
	 */
	@SimpleFunction
	public HTML元素 到元素() {
		return node instanceof Element ? HTML元素.wrap((Element) node) : null;
	}

	/**
	 * 读取文本、注释或原始数据的完整内容；其他类型报告错误。
	 *
	 * @return 完整文本、注释或原始数据内容。
	 * @throws IllegalArgumentException 当前节点类型不支持内容读取。
	 */
	@SimpleFunction
	public String 取节点内容() {
		if (node instanceof TextNode) {
			return ((TextNode) node).getWholeText();
		}
		if (node instanceof Comment) {
			return ((Comment) node).getData();
		}
		if (node instanceof DataNode) {
			return ((DataNode) node).getWholeData();
		}
		throw new IllegalArgumentException("当前节点不是文本、注释或原始数据。");
	}

	/**
	 * 设置文本、注释或原始数据内容；不解析为 HTML。原始数据不要写入不可信脚本。
	 *
	 * @param text 完整新内容。
	 * @throws IllegalArgumentException 内容为空或类型不支持。
	 */
	@SimpleFunction
	public void 置节点内容(String text) {
		HTML解析器.requireText(text, "内容");
		if (node instanceof TextNode) {
			((TextNode) node).text(text);
		} else if (node instanceof Comment) {
			((Comment) node).setData(text);
		} else if (node instanceof DataNode) {
			((DataNode) node).setWholeData(text);
		} else {
			throw new IllegalArgumentException("当前节点不是文本、注释或原始数据。");
		}
	}

	/**
	 * 序列化此节点和后代；不会执行脚本。
	 *
	 * @return 当前节点的完整序列化文本。
	 */
	@SimpleFunction
	public String 取节点HTML() {
		return node.outerHtml();
	}

	/**
	 * 取得直接子节点数量，包含文本和注释。
	 *
	 * @return 按函数说明返回的数量、位置或指令。
	 */
	@SimpleFunction
	public int 取子节点数() {
		return node.childNodeSize();
	}

	/**
	 * 取得直接子节点，包含文本和注释。
	 *
	 * @param index 从零开始的子节点索引。
	 * @return 对应节点；越界报告索引超出界限错误。
	 */
	@SimpleFunction
	public HTML节点 取子节点(int index) {
		if (index < 0 || index >= node.childNodeSize()) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		return wrap(node.childNode(index));
	}

	/**
	 * 取得父节点，没有时返回空引用。
	 *
	 * @return 对应节点视图，不存在时返回空引用。
	 */
	@SimpleFunction
	public HTML节点 取父节点() {
		return wrap(node.parentNode());
	}

	/**
	 * 取得上一个同级节点，包含文本和注释，没有时返回空引用。
	 *
	 * @return 对应节点视图，不存在时返回空引用。
	 */
	@SimpleFunction
	public HTML节点 取前节点() {
		return wrap(node.previousSibling());
	}

	/**
	 * 取得下一个同级节点，包含文本和注释，没有时返回空引用。
	 *
	 * @return 对应节点视图，不存在时返回空引用。
	 */
	@SimpleFunction
	public HTML节点 取后节点() {
		return wrap(node.nextSibling());
	}

	/**
	 * 取得当前树的根节点。
	 *
	 * @return 对应节点视图，不存在时返回空引用。
	 */
	@SimpleFunction
	public HTML节点 取根节点() {
		return wrap(node.root());
	}

	/**
	 * 取得所属文档视图；独立节点没有所属文档时返回空引用。
	 *
	 * @return 独立文档。
	 */
	@SimpleFunction
	public HTML文档 取所属文档() {
		Document owner = node.ownerDocument();
		return owner == null ? null : new HTML文档(owner);
	}

	/**
	 * 取得节点在所有同级节点中的索引，文本和注释也计数。
	 *
	 * @return 按函数说明返回的数量、位置或指令。
	 */
	@SimpleFunction
	public int 取同级索引() {
		return node.siblingIndex();
	}

	/**
	 * 判断两个视图是否指向同一原生节点。
	 *
	 * @param other 待比较视图；允许空引用。
	 * @return 符合上述条件时为真。
	 */
	@SimpleFunction
	public boolean 是否同一节点(HTML节点 other) {
		return other != null && node == other.node;
	}

	/**
	 * 在统一 UTF-8、无美化排版配置下比较节点类型和序列化内容；不受所在位置或所属文档输出设置影响。
	 *
	 * @param other 待比较节点；允许空引用。
	 * @return 符合上述条件时为真。
	 */
	@SimpleFunction
	public boolean 是否同值(HTML节点 other) {
		return other != null && node.getClass() == other.node.getClass()
				&& canonical(node).equals(canonical(other.node));
	}

	/**
	 * 在独立副本上统一输出上下文，避免文本节点位置影响比较。
	 *
	 * @param value 原始节点。
	 * @return 用于比较的规范序列化内容。
	 */
	private static String canonical(Node value) {
		Node copy = value.clone();
		Document document;
		if (copy instanceof Document) {
			document = (Document) copy;
		} else {
			document = new Document("");
			document.appendChild(copy);
		}
		document.outputSettings(new Document.OutputSettings().prettyPrint(false));
		return copy.outerHtml();
	}

	/**
	 * 深复制节点和后代，副本修改不影响原节点。
	 *
	 * @return 对应节点视图，不存在时返回空引用。
	 */
	@SimpleFunction
	public HTML节点 复制节点() {
		return wrap(node.clone());
	}

	/**
	 * 复制当前节点及属性，不包含子节点。
	 *
	 * @return 对应节点视图，不存在时返回空引用。
	 */
	@SimpleFunction
	public HTML节点 浅复制节点() {
		return wrap(node.shallowClone());
	}

	/**
	 * 仅用于文本节点：保留前段，返回后段；有父节点时后段插在其后。
	 *
	 * @param offset UTF-16 字符索引，零到完整文本长度。
	 * @return 对应节点视图，不存在时返回空引用。
	 * @throws IllegalArgumentException 当前不是文本节点。
	 */
	@SimpleFunction
	public HTML节点 分割文本节点(int offset) {
		if (!(node instanceof TextNode)) {
			throw new IllegalArgumentException("当前不是文本节点。");
		}
		int length = ((TextNode) node).getWholeText().length();
		if (offset < 0 || offset > length) {
			throw new ArrayIndexOutOfBoundsException(offset);
		}
		return wrap(((TextNode) node).splitText(offset));
	}

	/**
	 * 从父节点移除此节点；已经独立时不处理。已有引用仍可使用。
	 */
	@SimpleFunction
	public void 移除() {
		if (node.parentNode() != null) {
			node.remove();
		}
	}

	/**
	 * 移除所有子节点，保留当前节点及其属性。
	 */
	@SimpleFunction
	public void 清空子节点() {
		node.empty();
	}

	/**
	 * 更新此节点及后代的基本 URL，不发起网络请求。
	 *
	 * @param uri 基本 URL，允许空文本。
	 * @throws IllegalArgumentException 参数为空引用。
	 */
	@SimpleFunction
	public void 置节点基本URL(String uri) {
		node.setBaseUri(HTML解析器.requireText(uri, "基本URL"));
	}

	/**
	 * 取得初次解析时节点的字符位置；未启用位置记录或节点为后来创建时返回 -1。DOM 修改不更新此位置。
	 *
	 * @return 按函数说明返回的数量、位置或指令。
	 */
	@SimpleFunction
	public int 取源码开始位置() {
		return node.sourceRange().start().pos();
	}

	/**
	 * 取得初次解析时节点的字符位置；未启用位置记录或节点为后来创建时返回 -1。DOM 修改不更新此位置。
	 *
	 * @return 按函数说明返回的数量、位置或指令。
	 */
	@SimpleFunction
	public int 取源码结束位置() {
		return node.sourceRange().end().pos();
	}

	/**
	 * 把已有节点移动到此节点相邻处或替换此节点；禁止构造循环树，当前节点须有父节点。
	 *
	 * @param value 待移动节点；先复制可保留原位置。
	 * @throws IllegalArgumentException 参数无效、当前无父节点或会形成循环。
	 */
	@SimpleFunction
	public void 在前面插入节点(HTML节点 value) {
		if (node.parentNode() == null) {
			throw new IllegalArgumentException("当前节点没有父节点。");
		}
		checkInsertion(node.parentNode(), value);
		if (value.node == node) {
			throw new IllegalArgumentException("不能把节点插入到自身相邻处或替换自身。");
		}
		node.before(value.node);
	}

	/**
	 * 把已有节点移动到此节点相邻处或替换此节点；禁止构造循环树，当前节点须有父节点。
	 *
	 * @param value 待移动节点；先复制可保留原位置。
	 * @throws IllegalArgumentException 参数无效、当前无父节点或会形成循环。
	 */
	@SimpleFunction
	public void 在后面插入节点(HTML节点 value) {
		if (node.parentNode() == null) {
			throw new IllegalArgumentException("当前节点没有父节点。");
		}
		checkInsertion(node.parentNode(), value);
		if (value.node == node) {
			throw new IllegalArgumentException("不能把节点插入到自身相邻处或替换自身。");
		}
		node.after(value.node);
	}

	/**
	 * 把已有节点移动到此节点相邻处或替换此节点；禁止构造循环树，当前节点须有父节点。
	 *
	 * @param value 待移动节点；先复制可保留原位置。
	 * @throws IllegalArgumentException 参数无效、当前无父节点或会形成循环。
	 */
	@SimpleFunction
	public void 替换为节点(HTML节点 value) {
		if (node.parentNode() == null) {
			throw new IllegalArgumentException("当前节点没有父节点。");
		}
		checkInsertion(node.parentNode(), value);
		if (value.node == node) {
			throw new IllegalArgumentException("不能把节点插入到自身相邻处或替换自身。");
		}
		node.replaceWith(value.node);
	}

	/**
	 * 在移动之前验证节点，禁止自包含、祖先回插和把文档插入子树。
	 *
	 * @param parent 目标父节点。
	 * @param value  待移动节点。
	 * @throws IllegalArgumentException 节点无效或插入会形成循环。
	 */
	static void checkInsertion(Node parent, HTML节点 value) {
		if (value == null || value.node instanceof Document) {
			throw new IllegalArgumentException("待插入节点不得为空或整个文档。");
		}
		for (Node current = parent; current != null; current = current.parentNode()) {
			if (current == value.node) {
				throw new IllegalArgumentException("不能把节点插入自身或其后代。");
			}
		}
	}

	/**
	 * 深度优先遍历；访问器可跳过、移除非根节点或停止。回调不要直接修改树结构或重入遍历。过滤增量生效，异常不会回滚已完成移除。
	 *
	 * @param visitor 进入和离开节点时回调。
	 * @return 完整遍历为真；访问器主动停止时为假。
	 * @throws IllegalArgumentException 访问器为空、返回未知指令或试图移除遍历根。
	 */
	@SimpleFunction
	public boolean 遍历(HTML节点访问器 visitor) {
		if (visitor == null) {
			throw new IllegalArgumentException("访问器不得为空。");
		}
		NodeFilter filter = new NodeFilter() {
			@Override
			public FilterResult head(Node current, int depth) {
				return decision(visitor.访问(wrap(current), depth, false), current == node);
			}

			@Override
			public FilterResult tail(Node current, int depth) {
				return decision(visitor.访问(wrap(current), depth, true), current == node);
			}
		};
		return NodeTraversor.filter(filter, node) != FilterResult.STOP;
	}

	/**
	 * 校验访问器指令并映射到上游枚举。
	 *
	 * @param value 访问器返回值。
	 * @param root  是否为本次遍历根。
	 * @return 上游遍历指令。
	 * @throws IllegalArgumentException 指令无效或试图移除根。
	 */
	private static FilterResult decision(int value, boolean root) {
		if (value < 0 || value > 4 || root && value == 3) {
			throw new IllegalArgumentException("遍历指令无效，或试图移除本次遍历根。");
		}
		return FilterResult.values()[value];
	}
}
