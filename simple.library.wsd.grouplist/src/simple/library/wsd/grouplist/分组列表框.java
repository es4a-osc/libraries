package simple.library.wsd.grouplist;

import simple.runtime.components.组件;
import simple.runtime.components.列表组件;
import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 分组列表框组件。
 * 分组索引和组内子项索引均从 0 开始，删除后后续索引会改变；稳定业务标识使用标记保存。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface 分组列表框 extends 列表组件 {

	/* 事件 */

	/**
	 * 用户单击子项时触发。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @param childId 组内子项索引，从 0 开始。
	 */
	@SimpleEvent
	void 子项被单击(int groupId, int childId);

	/**
	 * 用户长按子项时触发。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @param childId 组内子项索引，从 0 开始。
	 */
	@SimpleEvent
	void 子项被长按(int groupId, int childId);

	/**
	 * 用户单击子项内的按钮时触发。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @param childId 组内子项索引，从 0 开始。
	 */
	@SimpleEvent
	void 子项按钮被单击(int groupId, int childId);

	/**
	 * 分组展开后触发。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 */
	@SimpleEvent
	void 分组被展开(int groupId);

	/**
	 * 分组收起后触发。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 */
	@SimpleEvent
	void 分组被收起(int groupId);

	/**
	 * 用户单击分组标题时触发。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 */
	@SimpleEvent
	void 分组被单击(int groupId);

	/* 属性 */

	/**
	 * 读取分组标题的字体大小，单位 SP。
	 *
	 * @return 分组标题的字体大小，单位 SP。
	 */
	@SimpleProperty
	int 分组标题字体大小();

	/**
	 * 设置分组标题的字体大小，单位 SP。
	 *
	 * @param size 字体大小，单位 SP。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "14"
	)
	void 分组标题字体大小(int size);

	/**
	 * 读取分组标题的字体颜色，使用 ARGB 整数值。
	 *
	 * @return 分组标题的字体颜色，使用 ARGB 整数值。
	 */
	@SimpleProperty
	int 分组标题字体颜色();

	/**
	 * 设置分组标题的字体颜色，使用 ARGB 整数值。
	 *
	 * @param argb 字体颜色的 ARGB 整数值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_COLOR,
		initializer = 组件.颜色_黑 + ""
	)
	void 分组标题字体颜色(int argb);

	/**
	 * 读取分组信息的字体大小，单位 SP。
	 *
	 * @return 分组信息的字体大小，单位 SP。
	 */
	@SimpleProperty
	int 分组信息字体大小();

	/**
	 * 设置分组信息的字体大小，单位 SP。
	 *
	 * @param size 字体大小，单位 SP。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "12"
	)
	void 分组信息字体大小(int size);

	/**
	 * 读取分组信息的字体颜色，使用 ARGB 整数值。
	 *
	 * @return 分组信息的字体颜色，使用 ARGB 整数值。
	 */
	@SimpleProperty
	int 分组信息字体颜色();

	/**
	 * 设置分组信息的字体颜色，使用 ARGB 整数值。
	 *
	 * @param argb 字体颜色的 ARGB 整数值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_COLOR,
		initializer = 组件.颜色_灰 + ""
	)
	void 分组信息字体颜色(int argb);

	/**
	 * 读取子项标题的字体大小，单位 SP。
	 *
	 * @return 子项标题的字体大小，单位 SP。
	 */
	@SimpleProperty
	int 子项标题字体大小();

	/**
	 * 设置子项标题的字体大小，单位 SP。
	 *
	 * @param size 字体大小，单位 SP。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "14"
	)
	void 子项标题字体大小(int size);

	/**
	 * 读取子项标题的字体颜色，使用 ARGB 整数值。
	 *
	 * @return 子项标题的字体颜色，使用 ARGB 整数值。
	 */
	@SimpleProperty
	int 子项标题字体颜色();

	/**
	 * 设置子项标题的字体颜色，使用 ARGB 整数值。
	 *
	 * @param argb 字体颜色的 ARGB 整数值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_COLOR,
		initializer = 组件.颜色_黑 + ""
	)
	void 子项标题字体颜色(int argb);

	/**
	 * 读取子项信息的字体大小，单位 SP。
	 *
	 * @return 子项信息的字体大小，单位 SP。
	 */
	@SimpleProperty
	int 子项信息字体大小();

	/**
	 * 设置子项信息的字体大小，单位 SP。
	 *
	 * @param size 字体大小，单位 SP。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "12"
	)
	void 子项信息字体大小(int size);

	/**
	 * 读取子项信息的字体颜色，使用 ARGB 整数值。
	 *
	 * @return 子项信息的字体颜色，使用 ARGB 整数值。
	 */
	@SimpleProperty
	int 子项信息字体颜色();

	/**
	 * 设置子项信息的字体颜色，使用 ARGB 整数值。
	 *
	 * @param argb 字体颜色的 ARGB 整数值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_COLOR,
		initializer = 组件.颜色_灰 + ""
	)
	void 子项信息字体颜色(int argb);

	/**
	 * 读取子项按钮的字体大小，单位 SP。
	 *
	 * @return 子项按钮的字体大小，单位 SP。
	 */
	@SimpleProperty
	int 子项按钮字体大小();

	/**
	 * 设置子项按钮的字体大小，单位 SP。
	 *
	 * @param size 字体大小，单位 SP。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "12"
	)
	void 子项按钮字体大小(int size);

	/**
	 * 读取子项按钮的字体颜色，使用 ARGB 整数值。
	 *
	 * @return 子项按钮的字体颜色，使用 ARGB 整数值。
	 */
	@SimpleProperty
	int 子项按钮字体颜色();

	/**
	 * 设置子项按钮的字体颜色，使用 ARGB 整数值。
	 *
	 * @param argb 字体颜色的 ARGB 整数值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_COLOR,
		initializer = 组件.颜色_黑 + ""
	)
	void 子项按钮字体颜色(int argb);

	/**
	 * 读取列表背景图片路径。
	 *
	 * @return 最近设置的图片路径。
	 */
	@SimpleProperty
	String 背景图片();

	/**
	 * 设置列表背景图片路径。
	 *
	 * @param imagePath 资产路径或绝对文件路径；空文本不加载新图片。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_ASSET,
		initializer = "\"\""
	)
	void 背景图片(String imagePath);

	/**
	 * 读取列表背景图片的 Android 资源 ID。
	 *
	 * @return 最近设置的资源 ID；初始值为 -1。
	 */
	@SimpleProperty
	int 背景图片2();

	/**
	 * 设置列表背景图片的 Android 资源 ID。
	 *
	 * @param id Android 可绘制资源 ID。
	 */
	@SimpleProperty
	void 背景图片2(int id);

	/**
	 * 读取是否显示子项内的按钮。
	 *
	 * @return 显示子项按钮时返回真。
	 */
	@SimpleProperty
	boolean 显示子项按钮();

	/**
	 * 设置是否显示子项内的按钮。
	 *
	 * @param value 是否显示子项按钮。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 显示子项按钮(boolean value);

	/* 方法 */

	/**
	 * 请求让列表获得输入焦点。
	 *
	 * @return 成功获得焦点时返回真。
	 */
	@SimpleFunction
	boolean 获取焦点();

	/**
	 * 清除列表的输入焦点。
	 */
	@SimpleFunction
	void 清除焦点();

	/**
	 * 在列表末尾添加分组并返回其索引；新分组的业务标记为空文本。
	 *
	 * @param title 要显示的标题。
	 * @param info 附加信息文本；空文本表示不显示信息。
	 * @return 新增分组的索引，从 0 开始。
	 */
	@SimpleFunction
	int 添加分组(String title, String info);

	/**
	 * 在列表末尾添加仅含标题的分组，信息和业务标记均为空文本。
	 *
	 * @param title 要显示的标题。
	 * @return 新增分组的索引，从 0 开始。
	 */
	@SimpleFunction
	int 添加分组(String title);

	/**
	 * 读取分组的业务标记。
	 *
	 * @param position 分组索引，从 0 开始。
	 * @return 业务标记文本；未设置时为空文本。
	 */
	@SimpleFunction
	String 取分组标记(int position);

	/**
	 * 设置分组的业务标记，不改变列表显示内容。
	 *
	 * @param position 分组索引，从 0 开始。
	 * @param tag 业务标记文本，不作为列表索引。
	 */
	@SimpleFunction
	void 置分组标记(int position, String tag);

	/**
	 * 读取分组标题。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @return 分组标题文本。
	 */
	@SimpleFunction
	String 取分组标题(int groupId);

	/**
	 * 更新分组标题并刷新列表。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @param title 要显示的标题。
	 */
	@SimpleFunction
	void 置分组标题(int groupId, String title);

	/**
	 * 读取分组的附加信息。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @return 分组附加信息文本。
	 */
	@SimpleFunction
	String 取分组信息(int groupId);

	/**
	 * 更新分组附加信息并刷新列表。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @param info 附加信息文本；空文本表示不显示信息。
	 */
	@SimpleFunction
	void 置分组信息(int groupId, String info);

	/**
	 * 取得当前分组数量。
	 *
	 * @return 分组数量；没有分组时为 0。
	 */
	@SimpleFunction
	int 取分组总数();

	/**
	 * 展开指定分组，显示其中的子项。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 */
	@SimpleFunction
	void 展开分组(int groupId);

	/**
	 * 收起指定分组，隐藏其中的子项。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 */
	@SimpleFunction
	void 收起分组(int groupId);

	/**
	 * 判断指定分组是否已展开。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @return 分组已展开时返回真。
	 */
	@SimpleFunction
	boolean 取分组状态(int groupId);

	/**
	 * 将列表选择定位到指定分组。
	 *
	 * @param groupPosition 分组索引，从 0 开始。
	 */
	@SimpleFunction
	void 选中分组(int groupPosition);

	/**
	 * 删除指定分组及其全部子项，后续分组索引随之改变。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 */
	@SimpleFunction
	void 删除分组(int groupId);

	/**
	 * 删除全部分组及子项。
	 */
	@SimpleFunction
	void 清空所有数据();

	/**
	 * 添加含标题和信息的子项，图片、按钮内容和业务标记均为空文本。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @param title 子项标题。
	 * @param info 附加信息；空文本不显示信息。
	 */
	@SimpleFunction
	void 添加子项(int groupId, String title, String info);

	/**
	 * 添加含图片、标题和信息的子项，按钮内容和业务标记均为空文本。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @param image 子项图片的资产路径或绝对路径；空文本不显示图片。
	 * @param title 子项标题。
	 * @param info 附加信息；空文本不显示信息。
	 */
	@SimpleFunction
	void 添加子项(int groupId, String image, String title, String info);

	/**
	 * 在指定分组末尾添加子项，新子项的业务标记为空文本。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @param image 子项图片的资产路径或绝对文件路径；空文本表示不显示图片。
	 * @param title 要显示的标题。
	 * @param info 附加信息文本；空文本表示不显示信息。
	 * @param buttonImage 按钮背景图片的资产路径或绝对文件路径；空文本不设置图片背景。
	 * @param buttonTitle 子项按钮显示的文字。
	 */
	@SimpleFunction
	void 添加子项(int groupId, String image, String title, String info, String buttonImage, String buttonTitle);

	/**
	 * 在指定分组末尾添加仅含标题的子项，其余文字、图片和业务标记均为空文本。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @param title 要显示的标题。
	 */
	@SimpleFunction
	void 添加子项(int groupId, String title);

	/**
	 * 读取子项的业务标记。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @param childId 组内子项索引，从 0 开始。
	 * @return 业务标记文本；未设置时为空文本。
	 */
	@SimpleFunction
	String 取子项标记(int groupId, int childId);

	/**
	 * 设置子项的业务标记，不改变列表显示内容。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @param childId 组内子项索引，从 0 开始。
	 * @param tag 业务标记文本，不作为列表索引。
	 */
	@SimpleFunction
	void 置子项标记(int groupId, int childId, String tag);

	/**
	 * 读取子项的图片。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @param childId 组内子项索引，从 0 开始。
	 * @return 子项图片路径。
	 */
	@SimpleFunction
	String 取子项图片(int groupId, int childId);

	/**
	 * 更新子项图片并刷新列表。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @param childId 组内子项索引，从 0 开始。
	 * @param image 子项图片的资产路径或绝对文件路径；空文本表示不显示图片。
	 */
	@SimpleFunction
	void 置子项图片(int groupId, int childId, String image);

	/**
	 * 读取子项的标题。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @param childId 组内子项索引，从 0 开始。
	 * @return 子项标题文本。
	 */
	@SimpleFunction
	String 取子项标题(int groupId, int childId);

	/**
	 * 更新子项标题并刷新列表。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @param childId 组内子项索引，从 0 开始。
	 * @param title 要显示的标题。
	 */
	@SimpleFunction
	void 置子项标题(int groupId, int childId, String title);

	/**
	 * 读取子项的信息。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @param childId 组内子项索引，从 0 开始。
	 * @return 子项附加信息文本。
	 */
	@SimpleFunction
	String 取子项信息(int groupId, int childId);

	/**
	 * 更新子项信息并刷新列表。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @param childId 组内子项索引，从 0 开始。
	 * @param info 附加信息文本；空文本表示不显示信息。
	 */
	@SimpleFunction
	void 置子项信息(int groupId, int childId, String info);

	/**
	 * 读取子项的按钮图片。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @param childId 组内子项索引，从 0 开始。
	 * @return 子项按钮背景图片路径。
	 */
	@SimpleFunction
	String 取子项按钮图片(int groupId, int childId);

	/**
	 * 更新子项按钮图片并刷新列表。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @param childId 组内子项索引，从 0 开始。
	 * @param buttonImage 按钮背景图片的资产路径或绝对文件路径；空文本不设置图片背景。
	 */
	@SimpleFunction
	void 置子项按钮图片(int groupId, int childId, String buttonImage);

	/**
	 * 读取子项的按钮标题。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @param childId 组内子项索引，从 0 开始。
	 * @return 子项按钮文字。
	 */
	@SimpleFunction
	String 取子项按钮标题(int groupId, int childId);

	/**
	 * 更新子项按钮标题并刷新列表。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @param childId 组内子项索引，从 0 开始。
	 * @param buttonTitle 子项按钮显示的文字。
	 */
	@SimpleFunction
	void 置子项按钮标题(int groupId, int childId, String buttonTitle);

	/**
	 * 取得指定分组中的子项数量。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @return 该分组中的子项数量。
	 */
	@SimpleFunction
	int 取子项总数(int groupId);

	/**
	 * 删除指定子项，组内后续子项索引随之改变。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 * @param childId 组内子项索引，从 0 开始。
	 */
	@SimpleFunction
	void 删除子项(int groupId, int childId);

	/**
	 * 删除指定分组的全部子项，保留分组。
	 *
	 * @param groupId 分组索引，从 0 开始。
	 */
	@SimpleFunction
	void 清空子项(int groupId);

	/**
	 * 选中子项，必要时自动展开所在分组。
	 *
	 * @param groupPosition 分组索引，从 0 开始。
	 * @param childPosition 组内子项索引，从 0 开始。
	 */
	@SimpleFunction
	void 选中子项(int groupPosition, int childPosition);

	/**
	 * 将列表选择定位到指定子项，可按需展开目标分组。
	 *
	 * @param groupPosition 分组索引，从 0 开始。
	 * @param childPosition 组内子项索引，从 0 开始。
	 * @param shouldExpandGroup 目标分组收起时，是否先将其展开。
	 */
	@SimpleFunction
	void 选中子项(int groupPosition, int childPosition, boolean shouldExpandGroup);
}
