package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI通用文本组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI通用文本 extends XUI视图组件 {

	/**
	 * 设置左边文字字符串。
	 *
	 * @param string 字符串。
	 */
	@SimpleFunction
	void 置左侧文本(String string);

	/**
	 * 设置左上文字字符串。
	 *
	 * @param string 字符串。
	 */
	@SimpleFunction
	void 置左侧顶部文本(String string);

	/**
	 * 设置左下文字字符串。
	 *
	 * @param string 字符串。
	 */
	@SimpleFunction
	void 置左侧底部文本(String string);

	/**
	 * 设置左边文字字体大小。
	 *
	 * @param size 字体大小。
	 */
	@SimpleFunction
	void 置左侧文本尺寸(float size);

	/**
	 * 设置左侧文本颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置左侧文本颜色(int color);

	/**
	 * 设置中间文字字符串。
	 *
	 * @param string 字符串。
	 */
	@SimpleFunction
	void 置中间文本(String string);

	/**
	 * 设置中上文字字符串。
	 *
	 * @param string 字符串。
	 */
	@SimpleFunction
	void 置中间顶部文本(String string);

	/**
	 * 设置中下文字字符串。
	 *
	 * @param string 字符串。
	 */
	@SimpleFunction
	void 置中间底部文本(String string);

	/**
	 * 设置中间文字字体大小。
	 *
	 * @param size 字体大小。
	 */
	@SimpleFunction
	void 置中间文本尺寸(float size);

	/**
	 * 设置中间文字颜色值。
	 *
	 * @param color 颜色值。
	 */
	@SimpleFunction
	void 置中间文本颜色(int color);

	/**
	 * 设置右边文字字符串。
	 *
	 * @param string 字符串。
	 */
	@SimpleFunction
	void 置右侧文本(String string);

	/**
	 * 设置右上文字字符串。
	 *
	 * @param string 字符串。
	 */
	@SimpleFunction
	void 置右侧顶部文本(String string);

	/**
	 * 设置右下文字字符串。
	 *
	 * @param string 字符串。
	 */
	@SimpleFunction
	void 置右侧底部文本(String string);

	/**
	 * 设置右边文字的字体大小。
	 *
	 * @param size 字体大小。
	 */
	@SimpleFunction
	void 置右侧文本尺寸(float size);

	/**
	 * 设置右边文字的颜色。
	 *
	 * @param color 颜色值。
	 */
	@SimpleFunction
	void 置右侧文本颜色(int color);

	/**
	 * 获取左边textView的值。
	 *
	 * @return 左边textView的值。
	 */
	@SimpleFunction
	String 取左侧文本();

	/**
	 * 获取左上textView的值。
	 *
	 * @return 左上textView的值。
	 */
	@SimpleFunction
	String 取左侧顶部文本();

	/**
	 * 获取左下textView的值。
	 *
	 * @return 左下textView的值。
	 */
	@SimpleFunction
	String 取左侧底部文本();

	/**
	 * 获取中间textView的值。
	 *
	 * @return 中间textView的值。
	 */
	@SimpleFunction
	String 取中间文本();

	/**
	 * 获取中上textView的值。
	 *
	 * @return 中上textView的值。
	 */
	@SimpleFunction
	String 取中间顶部文本();

	/**
	 * 获取中下textView的值。
	 *
	 * @return 中下textView的值。
	 */
	@SimpleFunction
	String 取中间底部文本();

	/**
	 * 获取右边textView的值。
	 *
	 * @return 右边textView的值。
	 */
	@SimpleFunction
	String 取右侧文本();

	/**
	 * 获取右上textView的值。
	 *
	 * @return 右上textView的值。
	 */
	@SimpleFunction
	String 取右侧顶部文本();

	/**
	 * 获取右下textView的值。
	 *
	 * @return 右下textView的值。
	 */
	@SimpleFunction
	String 取右侧底部文本();

	/**
	 * 设置左边view可点击。
	 *
	 * @param isClickable boolean类型。
	 */
	@SimpleFunction
	void 置左侧视图是否可单击(boolean isClickable);

	/**
	 * 设置中间view可点击。
	 *
	 * @param isClickable boolean类型。
	 */
	@SimpleFunction
	void 置中间视图是否可单击(boolean isClickable);

	/**
	 * 设置右边view可点击。
	 *
	 * @param isClickable boolean类型。
	 */
	@SimpleFunction
	void 置右侧视图是否可单击(boolean isClickable);

	/**
	 * 设置左侧图像左侧，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置左侧图像左侧(String image);

	/**
	 * 设置左侧图像顶部，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置左侧图像顶部(String image);

	/**
	 * 设置左侧图像右侧，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置左侧图像右侧(String image);

	/**
	 * 设置左侧图像底部，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置左侧图像底部(String image);

	/**
	 * 设置中间图像左侧，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置中间图像左侧(String image);

	/**
	 * 设置中间图像顶部，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置中间图像顶部(String image);

	/**
	 * 设置中间图像右侧，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置中间图像右侧(String image);

	/**
	 * 设置中间图像底部，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置中间图像底部(String image);

	/**
	 * 设置右侧图像左侧，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置右侧图像左侧(String image);

	/**
	 * 设置右侧图像顶部，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置右侧图像顶部(String image);

	/**
	 * 设置右侧图像右侧，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置右侧图像右侧(String image);

	/**
	 * 设置右侧图像底部，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置右侧图像底部(String image);

	/**
	 * 组合文本中的可操作区域被单击时触发。
	 *
	 * @param region 被单击的区域名称。
	 */
	@SimpleEvent
	void 区域被单击(String region);

	/**
	 * 设置指定文字区域的资产字体与 SP 字号。
	 *
	 * @param region 区域常量。
	 * @param asset 字体资产路径，空文本使用系统字体。
	 * @param size 大于零的 SP 字号。
	 */
	@SimpleFunction
	void 置区域字体(int region, String asset, float size);

	/**
	 * 左上文字区域。
	 */
	@SimpleDataElement
	int 区域_左上 = 0;

	/**
	 * 左中文字区域。
	 */
	@SimpleDataElement
	int 区域_左中 = 1;

	/**
	 * 左下文字区域。
	 */
	@SimpleDataElement
	int 区域_左下 = 2;

	/**
	 * 中上文字区域。
	 */
	@SimpleDataElement
	int 区域_中上 = 3;

	/**
	 * 中间文字区域。
	 */
	@SimpleDataElement
	int 区域_中间 = 4;

	/**
	 * 中下文字区域。
	 */
	@SimpleDataElement
	int 区域_中下 = 5;

	/**
	 * 右上文字区域。
	 */
	@SimpleDataElement
	int 区域_右上 = 6;

	/**
	 * 右中文字区域。
	 */
	@SimpleDataElement
	int 区域_右中 = 7;

	/**
	 * 右下文字区域。
	 */
	@SimpleDataElement
	int 区域_右下 = 8;

}
