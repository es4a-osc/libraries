package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.UsesPermissions;

/**
 * XUI图片轮播组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public interface XUI图片轮播 extends XUI视图组件 {

	/**
	 * 设置容器缩放。
	 *
	 * @param containerScale 容器缩放。
	 */
	@SimpleFunction
	void 置容器缩放(float containerScale);

	/**
	 * 取得缩放。
	 *
	 * @return 缩放。
	 */
	@SimpleFunction
	double 取缩放();

	/**
	 * 设置缩放。
	 *
	 * @param scale 缩放。
	 */
	@SimpleFunction
	void 置缩放(double scale);

	/**
	 * 设置显示样式,STYLE_DRAWABLE_RESOURCE or STYLE_CORNER_RECTANGLE。
	 *
	 * @param indicatorStyle 指示样式。
	 */
	@SimpleFunction
	void 置指示样式(int indicatorStyle);

	/**
	 * 设置显示宽度,单位dp,默认6dp。
	 *
	 * @param indicatorWidth 指示宽度。
	 */
	@SimpleFunction
	void 置指示宽度(float indicatorWidth);

	/**
	 * 设置显示器高度,单位dp,默认6dp。
	 *
	 * @param indicatorHeight 指示高度。
	 */
	@SimpleFunction
	void 置指示高度(float indicatorHeight);

	/**
	 * 设置两个显示器间距,单位dp,默认6dp。
	 *
	 * @param indicatorGap 指示间隔。
	 */
	@SimpleFunction
	void 置指示间隔(float indicatorGap);

	/**
	 * 设置显示器选中颜色(for STYLE_CORNER_RECTANGLE),默认"#ffffff"。
	 *
	 * @param selectColor 选择颜色。
	 */
	@SimpleFunction
	void 置指示选择颜色(int selectColor);

	/**
	 * 设置显示器圆角弧度(for STYLE_CORNER_RECTANGLE),单位dp,默认3dp。
	 *
	 * @param indicatorCornerRadius 指示圆角。
	 */
	@SimpleFunction
	void 置指示圆角(float indicatorCornerRadius);

	/**
	 * 取得项目高度。
	 *
	 * @return 项目高度。
	 */
	@SimpleFunction
	int 取项目高度();

	/**
	 * 取得项目宽度。
	 *
	 * @return 项目宽度。
	 */
	@SimpleFunction
	int 取项目宽度();

	/**
	 * 取得容器缩放。
	 *
	 * @return 容器缩放。
	 */
	@SimpleFunction
	float 取容器缩放();

	/**
	 * 滚动延时,默认5秒。
	 *
	 * @param delay 首次切页延迟，单位为秒，须为非负数。
	 */
	@SimpleFunction
	void 置延迟(long delay);

	/**
	 * 滚动间隔,默认5秒。
	 *
	 * @param period 自动切页周期，单位为秒，须大于零。
	 */
	@SimpleFunction
	void 置周期(long period);

	/**
	 * 设置是否支持自动滚动,默认true.仅对LoopViewPager有效。
	 *
	 * @param isAutoScrollEnable 是否自动滚动启用。
	 */
	@SimpleFunction
	void 置自动滚动启用(boolean isAutoScrollEnable);

	/**
	 * 设置底部背景条颜色,默认透明。
	 *
	 * @param barColor 栏颜色。
	 */
	@SimpleFunction
	void 置栏颜色(int barColor);

	/**
	 * 设置标题文字颜色,默认"#ffffff"。
	 *
	 * @param textColor 文字颜色。
	 */
	@SimpleFunction
	void 置文字颜色(int textColor);

	/**
	 * 设置标题文字大小,单位sp,默认14sp。
	 *
	 * @param textSize 文字大小。
	 */
	@SimpleFunction
	void 置文字大小(float textSize);

	/**
	 * 设置是否显示标题,默认true。
	 *
	 * @param isTitleShow 是否标题显示。
	 */
	@SimpleFunction
	void 置标题显示(boolean isTitleShow);

	/**
	 * 设置是否显示显示器,默认true。
	 *
	 * @param isIndicatorShow 是否指示显示。
	 */
	@SimpleFunction
	void 置指示显示(boolean isIndicatorShow);

	/**
	 * 设置当页面只有一条时，是否轮播。
	 *
	 * @param isOnePageLoop 是否单个页面循环。
	 */
	@SimpleFunction
	void 置是否单个页面循环(boolean isOnePageLoop);

	/**
	 * 替换轮播项目。
	 *
	 * @param values 文本轮播传文本，其余轮播传图片标识或网络地址。
	 */
	@SimpleFunction
	void 置项目数组(String[] values);

	/**
	 * 取得轮播项目数量。
	 *
	 * @return 轮播项目数量。
	 */
	@SimpleFunction
	int 取项目数();

	/**
	 * 准备并显示已设置的数据，按自动滚动开关开始播放；至少须有一项数据。
	 */
	@SimpleFunction
	void 开始轮播();

	/**
	 * 暂停自动轮播并保留当前页，手动滑动不会恢复自动播放。
	 */
	@SimpleFunction
	void 暂停轮播();

	/**
	 * 轮播项目被单击时触发。
	 *
	 * @param index 从 0 开始的项目索引。
	 * @param text 文本内容。
	 */
	@SimpleEvent
	void 项目被单击(int index, String text);

	/**
	 * 轮播选中页面改变时触发。
	 *
	 * @param index 从 0 开始的项目索引。
	 */
	@SimpleEvent
	void 页面被选择(int index);

	/**
	 * 设置页面切换效果。
	 *
	 * @param effect 本组件的轮播效果_* 常量，例如轮播效果_深度。
	 */
	@SimpleFunction
	void 置切换效果(String effect);

	/**
	 * 设置图片及对应标题，长度须相同。
	 *
	 * @param images 图片标识数组，支持本地及网络地址。
	 * @param titles 标题数组。
	 */
	@SimpleFunction
	void 置图文项目(String[] images, String[] titles);

	/**
	 * 取得从零开始的当前页，无数据时返回 -1。
	 *
	 * @return 从零开始的当前页，无数据时返回 -1。
	 */
	@SimpleFunction
	int 取当前页();

	/**
	 * 切换到指定页面，保留自动播放设置。
	 *
	 * @param index 从零开始的页码。
	 * @param smooth 是否平滑切换。
	 */
	@SimpleFunction
	void 置当前页(int index, boolean smooth);

	/**
	 * 设置后续加载图片时显示的占位图和失败图。
	 *
	 * @param loading Simple 图片标识，空文本清除。
	 * @param error Simple 图片标识，空文本清除。
	 */
	@SimpleFunction
	void 置占位图片(String loading, String error);

	/**
	 * 某页图片加载失败时触发。
	 *
	 * @param image 加载的图片地址。
	 */
	@SimpleEvent
	void 图片加载失败(String image);

	/**
	 * 使用图片资源绘制指示器。
	 */
	@SimpleDataElement
	int 指示样式_图片 = 0;

	/**
	 * 使用圆角矩形绘制指示器。
	 */
	@SimpleDataElement
	int 指示样式_圆角矩形 = 1;

	/**
	 * 设置底部标题及指示栏的内容边距。
	 *
	 * @param left 非负 DP 数。
	 * @param top 非负 DP 数。
	 * @param right 非负 DP 数。
	 * @param bottom 非负 DP 数。
	 */
	@SimpleFunction
	void 置栏边距(float left, float top, float right, float bottom);

	/**
	 * 设置轮播最后一页是否保留底部背景栏。
	 *
	 * @param show 是否在最后一页显示底部栏。
	 */
	@SimpleFunction
	void 置末页栏显示(boolean show);

	/**
	 * 设置后续切页的指示器选中动画。
	 *
	 * @param effect 指示动画常量。
	 */
	@SimpleFunction
	void 置指示动画(int effect);

	/**
	 * 不播放指示器选中动画。
	 */
	@SimpleDataElement
	int 指示动画_无 = 0;

	/**
	 * 选中时缩放。
	 */
	@SimpleDataElement
	int 指示动画_缩放 = 1;

	/**
	 * 选中时旋转。
	 */
	@SimpleDataElement
	int 指示动画_旋转 = 2;

}
