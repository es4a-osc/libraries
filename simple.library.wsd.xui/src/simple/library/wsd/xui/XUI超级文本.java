package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI超级文本组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI超级文本 extends XUI视图组件 {

	/**
	 * 设置左上字符串。
	 *
	 * @param string 字符串。
	 */
	@SimpleFunction
	void 置左侧顶部文本(String string);

	/**
	 * 设置左中字符串。
	 *
	 * @param string 字符串。
	 */
	@SimpleFunction
	void 置左侧文本(String string);

	/**
	 * 设置左下字符串。
	 *
	 * @param string 字符串。
	 */
	@SimpleFunction
	void 置左侧底部文本(String string);

	/**
	 * 设置中上字符串。
	 *
	 * @param string 字符串。
	 */
	@SimpleFunction
	void 置中间顶部文本(String string);

	/**
	 * 设置中间字符串。
	 *
	 * @param string 字符串。
	 */
	@SimpleFunction
	void 置中间文本(String string);

	/**
	 * 设置输入框字符串。
	 *
	 * @param string 字符串。
	 */
	@SimpleFunction
	void 置中间编辑文本(String string);

	/**
	 * 设置中下字符串。
	 *
	 * @param string 字符串。
	 */
	@SimpleFunction
	void 置中间底部文本(String string);

	/**
	 * 设置右上字符串。
	 *
	 * @param string 字符串。
	 */
	@SimpleFunction
	void 置右侧顶部文本(String string);

	/**
	 * 设置右中字符串。
	 *
	 * @param string 字符串。
	 */
	@SimpleFunction
	void 置右侧文本(String string);

	/**
	 * 设置右下字符串。
	 *
	 * @param string 字符串。
	 */
	@SimpleFunction
	void 置右侧底部文本(String string);

	/**
	 * 设置左上文字颜色。
	 *
	 * @param color 颜色值。
	 */
	@SimpleFunction
	void 置左侧顶部文本颜色(int color);

	/**
	 * 设置左中文字颜色。
	 *
	 * @param color 颜色值。
	 */
	@SimpleFunction
	void 置左侧文本颜色(int color);

	/**
	 * 设置左下文字颜色。
	 *
	 * @param color 颜色值。
	 */
	@SimpleFunction
	void 置左侧底部文本颜色(int color);

	/**
	 * 设置中上文字颜色。
	 *
	 * @param color 颜色值。
	 */
	@SimpleFunction
	void 置中间顶部文本颜色(int color);

	/**
	 * 设置中间文字颜色。
	 *
	 * @param color 颜色值。
	 */
	@SimpleFunction
	void 置中间文本颜色(int color);

	/**
	 * 设置中下文字颜色。
	 *
	 * @param color 颜色值。
	 */
	@SimpleFunction
	void 置中间底部文本颜色(int color);

	/**
	 * 设置右上文字颜色。
	 *
	 * @param color 颜色值。
	 */
	@SimpleFunction
	void 置右侧顶部文本颜色(int color);

	/**
	 * 设置右中文字颜色。
	 *
	 * @param color 颜色值。
	 */
	@SimpleFunction
	void 置右侧文本颜色(int color);

	/**
	 * 设置右下文字颜色。
	 *
	 * @param color 颜色值。
	 */
	@SimpleFunction
	void 置右侧底部文本颜色(int color);

	/**
	 * 获取左上字符串。
	 *
	 * @return 左上字符串。
	 */
	@SimpleFunction
	String 取左侧顶部文本();

	/**
	 * 获取左中字符串。
	 *
	 * @return 左中字符串。
	 */
	@SimpleFunction
	String 取左侧文本();

	/**
	 * 获取左下字符串。
	 *
	 * @return 左下字符串。
	 */
	@SimpleFunction
	String 取左侧底部文本();

	/**
	 * 获取中上字符串。
	 *
	 * @return 中上字符串。
	 */
	@SimpleFunction
	String 取中间顶部文本();

	/**
	 * 获取中间字符串。
	 *
	 * @return 中间字符串。
	 */
	@SimpleFunction
	String 取中间文本();

	/**
	 * 获取中下字符串。
	 *
	 * @return 中下字符串。
	 */
	@SimpleFunction
	String 取中间底部文本();

	/**
	 * 获取右上字符串。
	 *
	 * @return 右上字符串。
	 */
	@SimpleFunction
	String 取右侧顶部文本();

	/**
	 * 获取右中字符串。
	 *
	 * @return 右中字符串。
	 */
	@SimpleFunction
	String 取右侧文本();

	/**
	 * 获取右下字符串。
	 *
	 * @return 右下字符串。
	 */
	@SimpleFunction
	String 取右侧底部文本();

	/**
	 * 设置Switch是否选中。
	 *
	 * @param isChecked Switch是否选中。
	 */
	@SimpleFunction
	void 置开关是否选中(boolean isChecked);

	/**
	 * 设置Switch是否选中。
	 *
	 * @param isChecked Switch是否选中。
	 * @param isSilent 是否不触发回调。
	 */
	@SimpleFunction
	void 置开关是否选中(boolean isChecked, boolean isSilent);

	/**
	 * 获取switch状态。
	 *
	 * @return switch状态。
	 */
	@SimpleFunction
	boolean 取开关是否选中();

	/**
	 * 设置左边图标。
	 *
	 * @param resId 左边图标资源id。
	 */
	@SimpleFunction
	void 置左侧图标资源(int resId);

	/**
	 * 设置右边图标资源Id。
	 *
	 * @param resId 右边图标。
	 */
	@SimpleFunction
	void 置右侧图标资源(int resId);

	/**
	 * 获取输入框。
	 *
	 * @return 输入框。
	 */
	@SimpleFunction
	String 取中间编辑值();

	/**
	 * 获取输入框内容是否为空。
	 *
	 * @return 输入框内容是否为空。
	 */
	@SimpleFunction
	boolean 是否编辑为空();

	/**
	 * 获取输入框内容是否不为空。
	 *
	 * @return 输入框内容是否不为空。
	 */
	@SimpleFunction
	boolean 是否编辑非为空();

	/**
	 * 设置左边textView文字对齐方式。
	 *
	 * @param gravity 对齐方式。
	 */
	@SimpleFunction
	void 置左侧文本对齐(int gravity);

	/**
	 * 设置中间textView文字对齐方式。
	 *
	 * @param gravity 对齐方式。
	 */
	@SimpleFunction
	void 置中间文本对齐(int gravity);

	/**
	 * 设置右边textView文字对齐方式。
	 *
	 * @param gravity 对齐方式。
	 */
	@SimpleFunction
	void 置右侧文本对齐(int gravity);

	/**
	 * 设置上边分割线显示状态。
	 *
	 * @param visibility 可见性。
	 */
	@SimpleFunction
	void 置顶部分隔线行可见性(int visibility);

	/**
	 * 设置下边分割线显示状态。
	 *
	 * @param visibility 可见性。
	 */
	@SimpleFunction
	void 置底部分隔线行可见性(int visibility);

	/**
	 * 设置按下的颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置形状按下颜色(int color);

	/**
	 * 设置正常的颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置形状正常颜色(int color);

	/**
	 * 设置填充的颜色。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	void 置形状填充颜色(int color);

	/**
	 * 设置边框宽度。
	 *
	 * @param strokeWidth 边框宽度值。
	 */
	@SimpleFunction
	void 置形状描边宽度(int strokeWidth);

	/**
	 * 设置边框颜色。
	 *
	 * @param strokeColor 边框颜色。
	 */
	@SimpleFunction
	void 置形状描边颜色(int strokeColor);

	/**
	 * 设置边框虚线宽度。
	 *
	 * @param strokeDashWidth 边框虚线宽度。
	 */
	@SimpleFunction
	void 置形状描边虚线宽度(float strokeDashWidth);

	/**
	 * 设置边框虚线间隙。
	 *
	 * @param strokeDashGap 边框虚线间隙值。
	 */
	@SimpleFunction
	void 置形状描边虚线间隔(float strokeDashGap);

	/**
	 * 设置圆角半径。
	 *
	 * @param radius 半径。
	 */
	@SimpleFunction
	void 置形状圆角(float radius);

	/**
	 * 设置左上圆角半径。
	 *
	 * @param radius 半径。
	 */
	@SimpleFunction
	void 置形状左上圆角(float radius);

	/**
	 * 设置右上圆角半径。
	 *
	 * @param radius 半径。
	 */
	@SimpleFunction
	void 置形状右上圆角(float radius);

	/**
	 * 设置左下圆角半径。
	 *
	 * @param radius 半径。
	 */
	@SimpleFunction
	void 置形状左下圆角(float radius);

	/**
	 * 设置右下圆角半径。
	 *
	 * @param radius 半径。
	 */
	@SimpleFunction
	void 置形状右下圆角(float radius);

	/**
	 * 设置左侧文本框图像左侧，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置左侧文本框图像左侧(String image);

	/**
	 * 设置左侧文本框图像右侧，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置左侧文本框图像右侧(String image);

	/**
	 * 设置中间文本框图像左侧，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置中间文本框图像左侧(String image);

	/**
	 * 设置中间文本框图像右侧，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置中间文本框图像右侧(String image);

	/**
	 * 设置右侧文本框图像左侧，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置右侧文本框图像左侧(String image);

	/**
	 * 设置右侧文本框图像右侧，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置右侧文本框图像右侧(String image);

	/**
	 * 设置左侧图标，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置左侧图标(String image);

	/**
	 * 设置右侧图标，使用 Simple 图片标识。
	 *
	 * @param image 图片资源标识、资产文件名或外部路径。
	 */
	@SimpleFunction
	void 置右侧图标(String image);

	/**
	 * 组合文本中的可操作区域被单击时触发。
	 *
	 * @param region 被单击的区域名称。
	 */
	@SimpleEvent
	void 区域被单击(String region);

	/**
	 * 设置右侧附属控件；切换类型时原选中状态清空。
	 *
	 * @param type 附属类型常量。
	 */
	@SimpleFunction
	void 置附属类型(int type);

	/**
	 * 取得当前右侧附属类型。
	 *
	 * @return 当前右侧附属类型。
	 */
	@SimpleFunction
	int 取附属类型();

	/**
	 * 设置右侧复选框选中状态；尚未显示复选框时自动创建。
	 *
	 * @param checked 是否选中。
	 * @param silent 是否不触发事件。
	 */
	@SimpleFunction
	void 置复选框选中(boolean checked, boolean silent);

	/**
	 * 取得右侧复选框的选中状态。
	 *
	 * @return 右侧复选框的选中状态。
	 */
	@SimpleFunction
	boolean 取复选框选中();

	/**
	 * 右侧开关或复选框状态改变时触发。
	 *
	 * @param type 附属类型常量。
	 * @param checked 当前是否选中。
	 */
	@SimpleEvent
	void 附属选中改变(int type, boolean checked);

	/**
	 * 设置中间区域是否显示输入框，保留已输入文本。
	 *
	 * @param enabled 是否启用编辑模式。
	 */
	@SimpleFunction
	void 置编辑模式(boolean enabled);

	/**
	 * 设置中间输入框的提示文本。
	 *
	 * @param hint 输入提示文本。
	 */
	@SimpleFunction
	void 置编辑提示(String hint);

	/**
	 * 设置中间输入框的 Android 输入类型。
	 *
	 * @param type Android 输入类型及组合标志。
	 */
	@SimpleFunction
	void 置编辑输入类型(int type);

	/**
	 * 输入框文本改变时触发。
	 *
	 * @param text 文本。
	 */
	@SimpleEvent
	void 编辑文本改变(String text);

	/**
	 * 输入框焦点改变时触发。
	 *
	 * @param focused 获得焦点。
	 */
	@SimpleEvent
	void 编辑焦点改变(boolean focused);

	/**
	 * 右侧无控件。
	 */
	@SimpleDataElement
	int 附属类型_无 = 0;

	/**
	 * 右侧开关控件。
	 */
	@SimpleDataElement
	int 附属类型_开关 = 1;

	/**
	 * 右侧复选框控件。
	 */
	@SimpleDataElement
	int 附属类型_复选框 = 2;

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
