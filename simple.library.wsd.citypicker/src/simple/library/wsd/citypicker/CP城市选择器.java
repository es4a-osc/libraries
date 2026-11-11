package simple.library.wsd.citypicker;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.components.组件;

/**
 * 整页弹出的城市选择器，支持名称或拼音搜索、字母索引、热门城市和定位结果回填。 界面线程调用；配置用于下次显示，定位和自定义搜索结果可在当前显示期间回填。 参数、资源或状态不符合约定时抛出
 * CP城市错误。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface CP城市选择器 extends 组件 {

	/**
	 * 用户选择城市；在已关闭事件之后触发，可在此重新打开选择器。 代码原样取自当前数据源，不保证为行政区划代码。
	 *
	 * @param position 当前城市列表、搜索结果或热门数组中的位置，从零开始；定位为零，不计入分组标题。
	 * @param name     城市名称。
	 * @param province 所属省份。
	 * @param pinyin   城市拼音。
	 * @param code     数据源中的城市代码。
	 */
	@SimpleEvent
	void 城市已选择(int position, String name, String province, String pinyin, String code);

	/**
	 * 用户通过取消按钮或系统返回取消选择，在已关闭事件之后触发；程序关闭和窗口切换不触发此事件。
	 */
	@SimpleEvent
	void 选择已取消();

	/**
	 * 本次选择界面已关闭，包括选择、取消、程序关闭及所属窗口离开。 本次未完成请求已失效；选择或取消时先触发此事件，再触发对应结果事件。
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
	 * 自定义搜索开启后，非空搜索文本变化或点击失败区域重试时触发；空文本恢复全部城市，不发出请求。
	 *
	 * @param request 本次搜索标记；仅最新且尚未完成的请求可以回填。
	 * @param keyword 去除首尾空白后的搜索文本，保留大小写。
	 */
	@SimpleEvent
	void 搜索已请求(long request, String keyword);

	/**
	 * 设置下次显示时是否由应用提供搜索结果，默认关闭；不影响全部城市、定位和热门数据。
	 *
	 * @param enabled 真表示通过搜索已请求事件提供结果，假表示使用名称子串或拼音前缀的本地搜索。
	 */
	@SimpleFunction
	void 启用自定义搜索(boolean enabled);

	/**
	 * 提交当前自定义搜索的结果，保留传入顺序；四个数组不得为 null 且长度相同，空数组表示没有匹配城市。
	 *
	 * @param request   搜索已请求事件提供的标记。
	 * @param names     非空的城市名称。
	 * @param provinces 省份。
	 * @param pinyins   城市拼音。
	 * @param codes     保留数据源含义的城市代码。
	 * @return 是否接受；旧关键字、重复、已清空搜索、已关闭或已销毁的请求返回假。
	 */
	@SimpleFunction
	boolean 回填搜索(long request, String[] names, String[] provinces, String[] pinyins, String[] codes);

	/**
	 * 提交当前搜索的失败原因，清除旧结果；用户点击失败区域可重试相同关键字。
	 *
	 * @param request 当前搜索请求标记。
	 * @param reason  失败原因；空文本使用默认说明，不允许为 null。
	 * @return 是否接受；已失效或已完成的请求返回假。
	 */
	@SimpleFunction
	boolean 搜索失败(long request, String reason);

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
	 * @param names     非空的城市名称。
	 * @param provinces 省份。
	 * @param pinyins   用于搜索及字母分组的完整拼音。
	 * @param codes     城市代码，保留调用方定义。
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
	 * @param names     非空的城市名称。
	 * @param provinces 省份。
	 * @param codes     城市代码。
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
	 * @param name     非空城市名称。
	 * @param province 省份。
	 * @param code     城市代码。
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
	 * @param request  定位已请求事件提供的标记。
	 * @param name     非空城市名称。
	 * @param province 省份。
	 * @param code     城市代码。
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
	 * 关闭当前界面并使未完成的定位和搜索请求失效；重复关闭不重复触发事件，销毁后可安全调用。
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
