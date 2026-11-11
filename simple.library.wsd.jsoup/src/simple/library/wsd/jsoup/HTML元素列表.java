package simple.library.wsd.jsoup;

import java.util.List;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector.SelectorParseException;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * HTML 查询结果的有序成员快照，索引从 0 开始。
 *
 * <p>
 * 列表固定保存元素引用，文档树增删不会自动改变列表数量；通过成员修改元素 仍会反映到其所属文档。每次查询可取得新的列表，不提供跨线程同步。
 * </p>
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class HTML元素列表 {

	private final Elements elements;

	/**
	 * 复制结果的成员引用，不复制底层元素。
	 *
	 * @param elements 已有查询结果。
	 */
	HTML元素列表(List<Element> elements) {
		this.elements = new Elements(elements);
	}

	/* 成员访问与数组转换。 */

	/**
	 * 取得列表固定保存的成员数量。
	 *
	 * @return 非负成员数量。
	 */
	@SimpleFunction
	public int 取成员数() {
		return elements.size();
	}

	/**
	 * 按索引取得元素，修改该元素会影响所属文档。
	 *
	 * @param index 成员索引，从 0 开始。
	 * @return 对应元素。
	 * @throws ArrayIndexOutOfBoundsException 索引不在有效范围内。
	 */
	@SimpleFunction
	public HTML元素 取成员(int index) {
		if (index < 0 || index >= elements.size()) {
			throw new ArrayIndexOutOfBoundsException(index);
		}

		return HTML元素.wrap(elements.get(index));
	}

	/**
	 * 取得首个成员，空列表返回空引用。
	 *
	 * @return 对应元素，空列表返回空引用。
	 */
	@SimpleFunction
	public HTML元素 首元素() {
		return elements.isEmpty() ? null : 取成员(0);
	}

	/**
	 * 取得最后一个成员，空列表返回空引用。
	 *
	 * @return 对应元素，空列表返回空引用。
	 */
	@SimpleFunction
	public HTML元素 尾元素() {
		return elements.isEmpty() ? null : 取成员(elements.size() - 1);
	}

	/**
	 * 按成员顺序合并文本，不同成员之间用空格分隔。
	 *
	 * @return 合并并规范空白后的文本，空列表返回空文本。
	 */
	@SimpleFunction
	public String 取所有文本() {
		return elements.text();
	}

	/**
	 * 逐个提取成员文本，保留成员顺序。
	 *
	 * @return 与成员数量相同的文本数组。
	 */
	@SimpleFunction
	public String[] 取文本列表() {
		String[] result = new String[elements.size()];
		for (int i = 0; i < elements.size(); i++) {
			result[i] = elements.get(i).text();
		}

		return result;
	}

	/**
	 * 读取首个具有指定属性的成员值。
	 *
	 * @param name 属性名称，不得为空引用或空文本。
	 * @return 属性值，所有成员均无此属性时返回空文本。
	 * @throws IllegalArgumentException 属性名无效。
	 */
	@SimpleFunction
	public String 取属性值(String name) {
		return elements.attr(HTML解析器.requireName(name, "属性名"));
	}

	/**
	 * 逐个读取指定属性，缺少属性的成员保留为空文本。
	 *
	 * @param name 属性名称，不得为空引用或空文本。
	 * @return 与成员数量相同的属性值数组。
	 * @throws IllegalArgumentException 属性名无效。
	 */
	@SimpleFunction
	public String[] 取属性值列表(String name) {
		HTML解析器.requireName(name, "属性名");
		String[] result = new String[elements.size()];
		for (int i = 0; i < elements.size(); i++) {
			result[i] = elements.get(i).attr(name);
		}

		return result;
	}

	/* 批量修改；列表成员数量保持不变。 */

	/**
	 * 为所有成员新增或覆盖属性。
	 *
	 * @param name  属性名称，不得为空引用或空文本。
	 * @param value 新的属性值，允许空文本。
	 * @throws IllegalArgumentException 参数无效。
	 */
	@SimpleFunction
	public void 置属性值(String name, String value) {
		elements.attr(HTML解析器.requireName(name, "属性名"), HTML解析器.requireText(value, "属性值"));
	}

	/**
	 * 从各自父节点中移除所有成员，不清空此快照列表。
	 *
	 */
	@SimpleFunction
	public void 移除() {
		for (Element element : elements) {
			if (element.parent() != null) {
				element.remove();
			}
		}
	}

	/**
	 * 在每个成员范围内查询 CSS 并合并去重，返回新成员快照。
	 *
	 * @param selector CSS 选择器。
	 * @return 按查询顺序排列的成员快照。
	 * @throws IllegalArgumentException 选择器无效。
	 */
	@SimpleFunction
	public HTML元素列表 查询元素(String selector) {
		try {
			return new HTML元素列表(elements.select(HTML解析器.requireName(selector, "选择器")));
		} catch (SelectorParseException error) {
			throw new IllegalArgumentException("选择器无效。", error);
		}
	}

	/**
	 * 从当前成员中排除符合 CSS 的元素，返回新快照。
	 *
	 * @param selector CSS 选择器。
	 * @return 按查询顺序排列的成员快照。
	 * @throws IllegalArgumentException 选择器无效。
	 */
	@SimpleFunction
	public HTML元素列表 排除匹配(String selector) {
		try {
			return new HTML元素列表(elements.not(HTML解析器.requireName(selector, "选择器")));
		} catch (SelectorParseException error) {
			throw new IllegalArgumentException("选择器无效。", error);
		}
	}

	/**
	 * 判断至少一个成员是否符合 CSS 选择器。
	 *
	 * @param selector CSS 选择器。
	 * @return 符合上述条件时为真。
	 * @throws IllegalArgumentException 选择器无效。
	 */
	@SimpleFunction
	public boolean 是否有匹配(String selector) {
		try {
			return elements.is(HTML解析器.requireName(selector, "选择器"));
		} catch (SelectorParseException error) {
			throw new IllegalArgumentException("选择器无效。", error);
		}
	}

	/**
	 * 分别深复制所有成员；副本列表的修改不影响原成员文档。
	 *
	 * @return 按查询顺序排列的成员快照。
	 */
	@SimpleFunction
	public HTML元素列表 复制() {
		return new HTML元素列表(elements.clone());
	}

	/**
	 * 合并各成员内部 HTML。
	 *
	 * @return 各成员内部 HTML 的合并文本。
	 */
	@SimpleFunction
	public String 取内部HTML() {
		return elements.html();
	}

	/**
	 * 合并各成员完整 HTML。
	 *
	 * @return 各成员完整 HTML 的合并文本。
	 */
	@SimpleFunction
	public String 取HTML文本() {
		return elements.outerHtml();
	}

	/**
	 * 批量修改固定列表中的成员；同时选择祖先和后代时按成员顺序增量执行。
	 *
	 * @param value 纯文本内容。
	 * @throws IllegalArgumentException 内容为空引用。
	 */
	@SimpleFunction
	public void 置文本(String value) {
		HTML解析器.requireText(value, "内容");
		for (Element element : elements) {
			element.text(value);
		}
	}

	/**
	 * 批量修改固定列表中的成员；同时选择祖先和后代时按成员顺序增量执行。
	 *
	 * @param value HTML 片段。
	 * @throws IllegalArgumentException 内容为空引用。
	 */
	@SimpleFunction
	public void 置内部HTML(String value) {
		elements.html(HTML解析器.requireText(value, "内容"));
	}

	/**
	 * 批量修改固定列表中的成员；同时选择祖先和后代时按成员顺序增量执行。
	 *
	 * @param value HTML 片段。
	 * @throws IllegalArgumentException 内容为空引用。
	 */
	@SimpleFunction
	public void 追加HTML(String value) {
		elements.append(HTML解析器.requireText(value, "内容"));
	}

	/**
	 * 批量修改固定列表中的成员；同时选择祖先和后代时按成员顺序增量执行。
	 *
	 * @param value HTML 片段。
	 * @throws IllegalArgumentException 内容为空引用。
	 */
	@SimpleFunction
	public void 首部插入HTML(String value) {
		elements.prepend(HTML解析器.requireText(value, "内容"));
	}

	/**
	 * 从所有成员移除指定属性。
	 *
	 * @param name 属性名。
	 * @throws IllegalArgumentException 属性名无效。
	 */
	@SimpleFunction
	public void 移除属性(String name) {
		elements.removeAttr(HTML解析器.requireName(name, "属性名"));
	}

	/**
	 * 批量修改成员的单个 CSS 类名。
	 *
	 * @param value 单个类名。
	 * @throws IllegalArgumentException 类名无效。
	 */
	@SimpleFunction
	public void 添加类名(String value) {
		elements.addClass(HTML解析器.requireClass(value));
	}

	/**
	 * 批量修改成员的单个 CSS 类名。
	 *
	 * @param value 单个类名。
	 * @throws IllegalArgumentException 类名无效。
	 */
	@SimpleFunction
	public void 移除类名(String value) {
		elements.removeClass(HTML解析器.requireClass(value));
	}

	/**
	 * 批量修改成员的单个 CSS 类名。
	 *
	 * @param value 单个类名。
	 * @throws IllegalArgumentException 类名无效。
	 */
	@SimpleFunction
	public void 切换类名(String value) {
		elements.toggleClass(HTML解析器.requireClass(value));
	}

	/**
	 * 移除每个成员的子节点，保留成员和属性。
	 */
	@SimpleFunction
	public void 清空子节点() {
		elements.empty();
	}

}
