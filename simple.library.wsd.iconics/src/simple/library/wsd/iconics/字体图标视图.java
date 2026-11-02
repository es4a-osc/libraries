package simple.library.wsd.iconics;

import android.graphics.*;
import android.view.*;
import android.widget.*;
import com.mikepenz.iconics.*;
import com.mikepenz.iconics.typeface.*;
import java.util.*;
import simple.runtime.annotations.*;
import simple.runtime.components.*;

/**
 * 图标视图公共接口。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface 字体图标视图 extends 可视组件 {
	/**
	 * 设置主图标；文本控件设置左附图。
	 *
	 * @param image 图像。
	 */
	@SimpleFunction
	public void 设置图标(图标图像 image);
	/**
	 * 设置图标混排文字；仅文本类控件有效。
	 *
	 * @param text 文本。
	 */
	@SimpleFunction
	public void 设置文字(String text);
	/**
	 * 取得文本；仅文本类控件有效。
	 *
	 * @return 取得文本；仅文本类控件有效。
	 */
	@SimpleFunction
	public String 取文字();
	/**
	 * 设置文字 SP 大小；仅文本类控件有效。
	 *
	 * @param size 字号。
	 */
	@SimpleFunction
	public void 设置字号(float size);
	/**
	 * 设置文字颜色；仅文本类控件有效。
	 *
	 * @param color 颜色。
	 */
	@SimpleFunction
	public void 设置文字颜色(int color);
	/**
	 * 设置是否接受操作。
	 *
	 * @param enabled 启用。
	 */
	@SimpleFunction
	public void 设置启用(boolean enabled);
	/**
	 * 设置无障碍描述。
	 *
	 * @param text 描述。
	 */
	@SimpleFunction
	public void 设置内容描述(String text);
	/**
	 * 用户点击控件。
	 */
	@SimpleEvent
	void 被单击();
	/**
	 * 用户长按控件。
	 */
	@SimpleEvent
	void 被长按();
	/**
	 * 焦点状态改变。
	 */
	@SimpleEvent
	void 焦点改变(boolean focused);

	/**
	 * 设置文本四侧附图，0左1上2右3下4起始5结束。
	 *
	 * @param image 图像。
	 * @param position 位置。
	 */
	@SimpleFunction
	public void 设置附图(图标图像 image, int position);

	/**
	 * 读取图片或左侧附图快照，不存在时为空引用。
	 *
	 * @return 读取图片或左侧附图快照，不存在时为空引用。
	 */
	@SimpleFunction
	public 图标图像 取图标();

	/**
	 * 清除图片；文字视图只清除左附图。
	 */
	@SimpleFunction
	public void 清除图标();

	/**
	 * 清除组件背景图像。
	 */
	@SimpleFunction
	public void 清除背景();

	/**
	 * 读取背景字体图标快照，没有时为空引用。
	 *
	 * @return 读取背景字体图标快照，没有时为空引用。
	 */
	@SimpleFunction
	public 图标图像 取背景图标();

	/**
	 * 读取指定位置常态附图快照；位置0至5。
	 *
	 * @param position 位置。
	 * @return 读取指定位置常态附图快照；位置0至5。
	 */
	@SimpleFunction
	public 图标图像 取附图(int position);

	/**
	 * 清除指定位置常态附图；保留选中态。
	 *
	 * @param position 位置。
	 */
	@SimpleFunction
	public void 清除附图(int position);

	/**
	 * 设置全部四侧常态附图；空引用清除。
	 *
	 * @param image 图像。
	 */
	@SimpleFunction
	public void 设置四侧附图(图标图像 image);

	/**
	 * 设置图标与文本之间间距，单位DIP。
	 *
	 * @param spacing 间距。
	 */
	@SimpleFunction
	public void 设置图文间距(int spacing);

	/**
	 * 读取图文间距，单位DIP。
	 *
	 * @return 读取图文间距，单位DIP。
	 */
	@SimpleFunction
	public int 取图文间距();

	/**
	 * 设置布局方向：0左至右，1右至左，2继承，3随文字。
	 *
	 * @param direction 方向。
	 */
	@SimpleFunction
	public void 设置布局方向(int direction);

	/**
	 * 读取实际布局方向，0左至右，1右至左。
	 *
	 * @return 读取实际布局方向，0左至右，1右至左。
	 */
	@SimpleFunction
	public int 取布局方向();

	/**
	 * 读取是否接受操作。
	 *
	 * @return 读取是否接受操作。
	 */
	@SimpleFunction
	public boolean 是否启用();

	/**
	 * 读取无障碍描述。
	 *
	 * @return 读取无障碍描述。
	 */
	@SimpleFunction
	public String 取内容描述();

	/**
	 * 读取SP字号。
	 *
	 * @return 读取SP字号。
	 */
	@SimpleFunction
	public float 取字号();

	/**
	 * 读取当前状态下文字颜色。
	 *
	 * @return 读取当前状态下文字颜色。
	 */
	@SimpleFunction
	public int 取文字颜色();

	/**
	 * 设置图片缩放：0矩阵1拉伸2适应起点3适应居中4适应终点5居中6裁剪7居中收缩。
	 *
	 * @param mode 方式。
	 */
	@SimpleFunction
	public void 设置图片缩放方式(int mode);

	/**
	 * 读取图片缩放方式。
	 *
	 * @return 读取图片缩放方式。
	 */
	@SimpleFunction
	public int 取图片缩放方式();

	/**
	 * 设置图片是否按宽高比调整边界。
	 *
	 * @param enabled 保持。
	 */
	@SimpleFunction
	public void 设置保持宽高比(boolean enabled);

	/**
	 * 读取是否按宽高比调整边界。
	 *
	 * @return 读取是否按宽高比调整边界。
	 */
	@SimpleFunction
	public boolean 是否保持宽高比();
}
