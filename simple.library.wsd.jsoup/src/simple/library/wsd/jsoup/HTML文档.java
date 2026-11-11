package simple.library.wsd.jsoup;

import java.nio.charset.Charset;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.parser.ParseError;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 解析后的 HTML 文档，继承 HTML元素 的查询、遍历和修改能力。
 *
 * <p>
 * 通过 HTML解析器 获得，不直接创建空包装实例。文档不执行网页脚本， 不会自动加载样式、图片或链接资源。
 * </p>
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
	 * @return 正文元素，缺失时按 HTML 文档规则创建。
	 */
	@SimpleFunction
	public HTML元素 取主体元素() {
		return wrap(((Document) element).body());
	}

	/**
	 * 取得 HTML 头部元素。
	 *
	 * @return 头部元素，缺失时按 HTML 文档规则创建。
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

	/**
	 * 取得输出配置副本；修改副本需重新应用。
	 *
	 * @return 独立输出配置。
	 */
	@SimpleFunction
	public HTML输出配置 取输出配置() {
		return new HTML输出配置(((Document) element).outputSettings());
	}

	/**
	 * 复制并应用输出配置，不共享配置对象。
	 *
	 * @param settings 新的输出配置。
	 * @throws IllegalArgumentException 配置为空引用。
	 */
	@SimpleFunction
	public void 置输出配置(HTML输出配置 settings) {
		if (settings == null) {
			throw new IllegalArgumentException("输出配置不得为空。");
		}
		((Document) element).outputSettings(settings.settings.clone());
	}

	/**
	 * 取得所属解析器最近一次解析的错误信息快照，含位置；片段写入也可能更新记录。
	 *
	 * @return 错误信息数组；未启用记录时为空。
	 */
	@SimpleFunction
	public String[] 取解析错误() {
		List<ParseError> errors = ((Document) element).parser().getErrors();
		String[] result = new String[errors.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = errors.get(i).toString();
		}
		return result;
	}

	/**
	 * 按名单清洗为独立正文文档，保留当前输出配置，不改变原文档。
	 *
	 * @param safelist 允许名单。
	 * @return 独立文档。
	 * @throws IllegalArgumentException 名单为空引用。
	 */
	@SimpleFunction
	public HTML文档 清洗副本(HTML安全名单 safelist) {
		if (safelist == null) {
			throw new IllegalArgumentException("名单不得为空。");
		}
		return new HTML文档(new Cleaner(new Safelist(safelist.safelist)).clean(((Document) element).clone()));
	}

	/**
	 * 按 HTML 文档规则规范头部和正文；XML 文档不应调用此方法。
	 */
	@SimpleFunction
	public void 规范结构() {
		// 手动修改树结构后仍可能需要归一化，保留本依赖版本的真实修复行为。
		((Document) element).normalise();
	}

	/**
	 * 设置输出字符集，并同步 HTML meta 或 XML 编码声明；不重新解码已解析的文本。
	 *
	 * @param encoding 有效的输出字符集名称。
	 * @throws IllegalArgumentException 字符集名称无效。
	 */
	@SimpleFunction
	public void 置文档编码(String encoding) {
		((Document) element).charset(Charset.forName(HTML解析器.requireName(encoding, "编码")));
	}

}
