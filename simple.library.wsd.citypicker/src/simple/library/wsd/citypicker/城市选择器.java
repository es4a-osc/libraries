package simple.library.wsd.citypicker;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.组件;

/**
 * 整页弹出的城市选择器，支持名称或拼音搜索、字母索引、热门城市和定位结果回填。
 * 界面线程调用；配置用于下次显示，定位结果可在当前显示期间回填。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface 城市选择器 extends 组件 {

	/**
	 * 用户选择城市；界面已关闭，代码原样取自当前数据源，不保证为行政区划代码。
	 *
	 * @param name 城市名称。
	 * @param province 所属省份。
	 * @param pinyin 城市拼音。
	 * @param code 数据源中的城市代码。
	 */
	@SimpleEvent
	void 城市已选择(String name, String province, String pinyin, String code);

	/**
	 * 用户通过取消按钮或系统返回取消选择；程序关闭和窗口切换不触发此事件。
	 */
	@SimpleEvent
	void 选择已取消();

	/**
	 * 本次选择界面已关闭，包括选择、取消、程序关闭及所属窗口离开。
	 */
	@SimpleEvent
	void 已关闭();

	/**
	 * 请求应用提供定位结果；本库不申请权限，也不包含定位 SDK。
	 *
	 * @param request 当前请求标记；回填时传回此值，已关闭或旧请求的结果会被拒绝。
	 */
	@SimpleEvent
	void 定位已请求(long request);

	/**
	 * 设置整页界面主题，保留上游全部 cp* 样式属性。
	 *
	 * @param style 继承 CityPickerDialogTheme 的资源标识，如 @style/MyCityTheme；空文本恢复默认。
	 */
	@SimpleFunction
	void 置主题(String style);

	/**
	 * 设置窗口动画。
	 *
	 * @param style 动画样式资源标识，如 @style/CityPickerAnimation；空文本关闭动画。
	 */
	@SimpleFunction
	void 置动画(String style);

	/**
	 * 替换全部城市数据，允许空数组；名称、省份、拼音和代码数组长度必须相等。
	 *
	 * @param names 非空的城市名称。
	 * @param provinces 省份。
	 * @param pinyins 用于搜索及字母分组的完整拼音。
	 * @param codes 城市代码，保留调用方定义。
	 */
	@SimpleFunction
	void 置城市数据(String[] names, String[] provinces, String[] pinyins, String[] codes);

	/**
	 * 恢复随 CityPicker 1.0.2 携带的城市列表，不保证行政区划及时更新。
	 */
	@SimpleFunction
	void 使用内置城市();

	/**
	 * 替换热门城市，允许空数组；三个数组长度必须相等，顺序保持不变。
	 *
	 * @param names 非空的城市名称。
	 * @param provinces 省份。
	 * @param codes 城市代码。
	 */
	@SimpleFunction
	void 置热门城市(String[] names, String[] provinces, String[] codes);

	/**
	 * 恢复上游默认的九个热门城市。
	 */
	@SimpleFunction
	void 使用默认热门城市();

	/**
	 * 设置预先获得的定位城市；下次显示时直接提供该城市，不自动发起定位请求。
	 *
	 * @param name 非空城市名称。
	 * @param province 省份。
	 * @param code 城市代码。
	 */
	@SimpleFunction
	void 置定位城市(String name, String province, String code);

	/**
	 * 清除预设定位城市；下次显示时触发定位已请求事件。
	 */
	@SimpleFunction
	void 清除定位城市();

	/**
	 * 回填当前定位请求的成功结果；必须在界面线程调用。
	 *
	 * @param request 定位已请求事件提供的标记。
	 * @param name 非空城市名称。
	 * @param province 省份。
	 * @param code 城市代码。
	 * @return 是否接受；过期、重复或已关闭请求返回假。
	 */
	@SimpleFunction
	boolean 回填定位(long request, String name, String province, String code);

	/**
	 * 结束一次定位请求并显示失败状态；用户可点击定位区域重试。
	 *
	 * @param request 当前定位请求标记。
	 * @return 是否接受；过期、重复或已关闭请求返回假。
	 */
	@SimpleFunction
	boolean 定位失败(long request);

	/**
	 * 在所属 Simple 窗口上显示选择器；重复调用保持当前界面与输入。
	 */
	@SimpleFunction
	void 显示();

	/**
	 * 关闭当前界面并使未完成的定位请求失效；重复关闭不重复触发事件。
	 */
	@SimpleFunction
	void 关闭();

	/**
	 * 查询当前选择界面的显示状态。
	 *
	 * @return 是否正在显示。
	 */
	@SimpleFunction
	boolean 是否显示();
}
