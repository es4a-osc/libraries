package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件;

/**
 * 构建带样式和图片的文本，应用到文本类组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI富文本工具 extends 组件 {

	/**
	 * 添加新的文本片段，后续样式设置作用于该片段。
	 *
	 * @param text 文本。
	 */
	@SimpleFunction
	void 添加文本(String text);

	/**
	 * 添加换行。
	 */
	@SimpleFunction
	void 添加换行();

	/**
	 * 设置当前文本片段颜色。
	 *
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	void 置文字颜色(int color);

	/**
	 * 设置当前文本片段背景。
	 *
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	void 置背景颜色(int color);

	/**
	 * 设置当前片段的绝对字号。
	 *
	 * @param size 字号，单位为 dp，须大于零。
	 */
	@SimpleFunction
	void 置文字大小(int size);

	/**
	 * 把当前片段设置为粗体。
	 */
	@SimpleFunction
	void 置粗体();

	/**
	 * 把当前片段设置为斜体。
	 */
	@SimpleFunction
	void 置斜体();

	/**
	 * 给当前片段添加下划线。
	 */
	@SimpleFunction
	void 置下划线();

	/**
	 * 给当前片段添加删除线。
	 */
	@SimpleFunction
	void 置删除线();

	/**
	 * 把当前片段设为上标。
	 */
	@SimpleFunction
	void 置上标();

	/**
	 * 把当前片段设为下标。
	 */
	@SimpleFunction
	void 置下标();

	/**
	 * 添加本地图片片段。
	 *
	 * @param image 图片标识。
	 */
	@SimpleFunction
	void 添加图片(String image);

	/**
	 * 将当前文本片段设为可点击内容，点击后回传标识。
	 *
	 * @param tag 调用方用于区分片段的标识。
	 */
	@SimpleFunction
	void 置点击标识(String tag);

	/**
	 * 将当前文本片段设为链接；点击回传网址，由调用方决定如何打开。
	 *
	 * @param url 非空网址，不自动跳转外部应用。
	 */
	@SimpleFunction
	void 置链接(String url);

	/**
	 * 已应用的文本片段被单击时触发。
	 *
	 * @param tag 片段标识；链接片段为网址。
	 * @param text 被点击的片段文字。
	 */
	@SimpleEvent
	void 片段被单击(String tag, String text);

	/**
	 * 把富文本内容复制到文本类组件。
	 *
	 * @param component 原生视图为 TextView 的组件。
	 */
	@SimpleFunction
	void 应用到(可视组件 component);

	/**
	 * 取得构建内容的纯文本。
	 *
	 * @return 纯文本。
	 */
	@SimpleFunction
	String 取纯文本();

	/**
	 * 清空已构建内容。
	 */
	@SimpleFunction
	void 清空();

	/**
	 * 释放已构建内容。
	 */
	@SimpleFunction
	void 关闭();

	/**
	 * 设置当前文本片段的段落对齐；片段应覆盖完整段落。
	 *
	 * @param alignment 段落对齐常量。
	 */
	@SimpleFunction
	void 置段落对齐(int alignment);

	/**
	 * 设置当前文本片段的段落行高。
	 *
	 * @param height 大于零的像素行高。
	 */
	@SimpleFunction
	void 置段落行高(int height);

	/**
	 * 设置当前文本片段首行及其余行的像素缩进。
	 *
	 * @param first 非负像素。
	 * @param rest 非负像素。
	 */
	@SimpleFunction
	void 置段落缩进(int first, int rest);

	/**
	 * 为当前文本片段添加引用竖线。
	 *
	 * @param color ARGB 竖线颜色。
	 * @param width 大于零的像素线宽。
	 * @param gap 非负像素间隔。
	 */
	@SimpleFunction
	void 置引用样式(int color, int width, int gap);

	/**
	 * 为当前文本片段添加圆形项目符号。
	 *
	 * @param color ARGB 圆点颜色。
	 * @param radius 大于零的像素半径。
	 * @param gap 非负像素间隔。
	 */
	@SimpleFunction
	void 置项目符号(int color, int radius, int gap);

	/**
	 * 添加指定像素大小与垂直对齐的图片片段。
	 *
	 * @param image Simple 图片标识。
	 * @param width 大于零的像素宽度。
	 * @param height 大于零的像素高度。
	 * @param alignment 图片对齐常量。
	 */
	@SimpleFunction
	void 添加图片(String image, int width, int height, int alignment);

	/**
	 * 起点对齐段落。
	 */
	@SimpleDataElement
	int 段落对齐_起点 = 0;

	/**
	 * 居中对齐段落。
	 */
	@SimpleDataElement
	int 段落对齐_居中 = 1;

	/**
	 * 末尾对齐段落。
	 */
	@SimpleDataElement
	int 段落对齐_末尾 = 2;

	/**
	 * 底部对齐图片。
	 */
	@SimpleDataElement
	int 图片对齐_底部 = 0;

	/**
	 * 基线对齐图片。
	 */
	@SimpleDataElement
	int 图片对齐_基线 = 1;

	/**
	 * 居中对齐图片。
	 */
	@SimpleDataElement
	int 图片对齐_居中 = 2;

	/**
	 * 顶部对齐图片。
	 */
	@SimpleDataElement
	int 图片对齐_顶部 = 3;

	/**
	 * 为当前文字片段设置资产字体，空文本恢复系统默认字体。
	 *
	 * @param asset 字体资产路径。
	 */
	@SimpleFunction
	void 置字体(String asset);

	/**
	 * 设置当前图片片段左右的透明间距。
	 *
	 * @param left 非负物理像素数。
	 * @param right 非负物理像素数。
	 */
	@SimpleFunction
	void 置图片间距(int left, int right);

}
