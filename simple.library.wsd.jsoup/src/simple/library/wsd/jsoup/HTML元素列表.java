package simple.library.wsd.jsoup;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * HTML 查询结果的有序成员快照，索引从 0 开始。
 *
 * <p>列表固定保存元素引用，文档树增删不会自动改变列表数量；通过成员修改元素
 * 仍会反映到其所属文档。每次查询可取得新的列表，不提供跨线程同步。</p>
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class HTML元素列表 {

	private final List<Element> elements;

	/**
	 * 复制结果的成员引用，不复制底层元素。
	 *
	 * @param elements 已有查询结果。
	 */
	HTML元素列表(List<Element> elements) {
		this.elements = new ArrayList<Element>(elements);
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
		return new Elements(elements).text();
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
		return new Elements(elements).attr(HTML解析器.requireName(name, "属性名"));
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
	 * @param name 属性名称，不得为空引用或空文本。
	 * @param value 新的属性值，允许空文本。
	 * @throws IllegalArgumentException 参数无效。
	 */
	@SimpleFunction
	public void 置属性值(String name, String value) {
		new Elements(elements).attr(HTML解析器.requireName(name, "属性名"), HTML解析器.requireText(value, "属性值"));
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
}
