package simple.library.wsd.jsoup;

import org.jsoup.nodes.Document;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 解析后的 HTML 文档，继承 HTML元素 的查询、遍历和修改能力。
 *
 * <p>通过 HTML解析器 获得，不直接创建空包装实例。文档不执行网页脚本，
 * 不会自动加载样式、图片或链接资源。</p>
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class HTML文档 extends HTML元素 {

	/**
	 * 包装已解析的文档，不再进行二次解析。
	 *
	 * @param document 已存在的 jsoup 文档。
	 */
	HTML文档(Document document) {
		super(document);
	}

	/* 文档结构与标题。 */

	/**
	 * 取得文档标题，合并其中的多余空白。
	 *
	 * @return 标题文本，没有标题时为空文本。
	 */
	@SimpleFunction
	public String 取标题() {
		return ((Document) element).title();
	}

	/**
	 * 取得 HTML 正文元素。
	 *
	 * @return 正文元素；文档被手动清空等情况下可能为空引用。
	 */
	@SimpleFunction
	public HTML元素 取主体元素() {
		return wrap(((Document) element).body());
	}

	/**
	 * 取得 HTML 头部元素。
	 *
	 * @return 头部元素；文档被手动清空等情况下可能为空引用。
	 */
	@SimpleFunction
	public HTML元素 取头元素() {
		return wrap(((Document) element).head());
	}

	/**
	 * 取得文档的位置标识，不请求网络。
	 *
	 * @return 传给解析器的基本 URL；未指定时通常为空文本。
	 */
	@SimpleFunction
	public String 取文档URL() {
		return ((Document) element).location();
	}

	/**
	 * 设置文档标题，需要时创建 title 元素。
	 *
	 * @param title 新标题文本，不得为空引用。
	 * @throws IllegalArgumentException 标题为空引用。
	 */
	@SimpleFunction
	public void 置标题(String title) {
		((Document) element).title(HTML解析器.requireText(title, "标题"));
	}

	/**
	 * 创建独立空元素，不自动插入文档树。
	 *
	 * @param tag 新元素的标签名称。
	 * @return 尚未挂接到文档树的元素。
	 * @throws IllegalArgumentException 标签名称无效。
	 */
	@SimpleFunction
	public HTML元素 创建元素(String tag) {
		return wrap(((Document) element).createElement(HTML解析器.requireName(tag, "标签")));
	}
}
