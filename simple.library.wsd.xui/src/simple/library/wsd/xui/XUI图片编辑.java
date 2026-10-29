package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI图片编辑组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI图片编辑 extends XUI视图组件 {

	/**
	 * 设置待编辑的原图。
	 *
	 * @param image 图片资源标识、资产文件名或外部文件路径。
	 */
	@SimpleFunction
	void 置图片(String image);

	/**
	 * 切换画笔绘制与对象操作模式。
	 *
	 * @param enabled 是否启用画笔。
	 */
	@SimpleFunction
	void 置画笔模式(boolean enabled);

	/**
	 * 设置画笔颜色。
	 *
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	void 置画笔颜色(int color);

	/**
	 * 设置画笔粗细。
	 *
	 * @param size 画笔宽度，单位为物理像素。
	 */
	@SimpleFunction
	void 置画笔粗细(float size);

	/**
	 * 在图片上添加可移动文字。
	 *
	 * @param text 文本内容。
	 * @param color 文字 ARGB 颜色。
	 */
	@SimpleFunction
	void 添加文字(String text, int color);

	/**
	 * 在图片上添加可移动表情。
	 *
	 * @param text 文本内容。
	 */
	@SimpleFunction
	void 添加表情(String text);

	/**
	 * 撤销最后一次编辑，返回是否仍有可撤销的编辑对象。
	 *
	 * @return 撤销最后一次编辑，返回是否仍有可撤销的编辑对象。
	 */
	@SimpleFunction
	boolean 撤销();

	/**
	 * 重做一次编辑，返回是否仍有可重做的编辑对象。
	 *
	 * @return 重做一次编辑，返回是否仍有可重做的编辑对象。
	 */
	@SimpleFunction
	boolean 重做();

	/**
	 * 移除画笔和添加的对象，保留原图。
	 */
	@SimpleFunction
	void 清空编辑();

	/**
	 * 异步合成并保存已显示的编辑结果，通过保存完成或保存失败事件报告结果。
	 * 保存期间不要重复提交或改变编辑内容；切窗或销毁会取消未完成的保存请求。
	 *
	 * @param path PNG 输出路径；相对路径以应用内部文件目录为基准，目录须已存在。
	 */
	@SimpleFunction
	void 保存图片(String path);

	/**
	 * 图片编辑结果成功保存时触发。
	 *
	 * @param path 已保存的完整路径。
	 */
	@SimpleEvent
	void 保存完成(String path);

	/**
	 * 图片编辑结果保存失败时触发。
	 *
	 * @param message 失败原因。
	 */
	@SimpleEvent
	void 保存失败(String message);

	/**
	 * 添加一张可移动和缩放的本地图片。
	 *
	 * @param image 图片标识。
	 */
	@SimpleFunction
	void 添加贴图(String image);

	/**
	 * 设置画笔不透明度。
	 *
	 * @param opacity 0～100，零为完全透明。
	 */
	@SimpleFunction
	void 置画笔透明度(int opacity);

	/**
	 * 设置橡皮擦宽度。
	 *
	 * @param size 宽度，单位为物理像素，须大于零。
	 */
	@SimpleFunction
	void 置橡皮擦粗细(float size);

	/**
	 * 切换至橡皮擦模式。
	 */
	@SimpleFunction
	void 启用橡皮擦();

	/**
	 * 取得上游支持的全部滤镜名称。
	 *
	 * @return 滤镜名称数组，包含 NONE。
	 */
	@SimpleFunction
	String[] 取滤镜名称();

	/**
	 * 应用滤镜。
	 *
	 * @param name 取滤镜名称返回的名称；NONE 为原图。
	 */
	@SimpleFunction
	void 置滤镜(String name);

	/**
	 * 更新文字编辑请求对应的文字；没有待编辑文字时拒绝操作。
	 *
	 * @param text 新文本。
	 * @param color ARGB 颜色。
	 */
	@SimpleFunction
	void 更新选中文字(String text, int color);

	/**
	 * 用户长按已有文字时触发，可调用更新选中文字提交修改。
	 *
	 * @param text 原文字。
	 * @param color 原 ARGB 颜色。
	 */
	@SimpleEvent
	void 文字编辑请求(String text, int color);

	/**
	 * 添加或移除编辑对象时触发。
	 *
	 * @param type 上游对象类型名称。
	 * @param count 当前对象数量。
	 */
	@SimpleEvent
	void 编辑对象改变(String type, int count);

	/**
	 * 开始或结束拖动编辑对象时触发。
	 *
	 * @param type 上游对象类型名称。
	 * @param started 是否开始拖动。
	 */
	@SimpleEvent
	void 对象拖动改变(String type, boolean started);

	/**
	 * 设置后续保存的透明裁边与成功后清理选项，不改变内容本身的透明度。
	 *
	 * @param trimTransparentBorder 是否裁去外围完全透明的行列；全透明图保留原尺寸。
	 * @param clearAfterSave 是否在成功保存后清除编辑对象。
	 */
	@SimpleFunction
	void 置保存选项(boolean trimTransparentBorder, boolean clearAfterSave);

	/**
	 * 隐藏文字和贴图的辅助边框与删除手柄。
	 */
	@SimpleFunction
	void 隐藏编辑框();

	/**
	 * 查询是否含当前生效的编辑对象、笔画或滤镜。
	 *
	 * @return 查询是否含当前生效的编辑对象、笔画或滤镜。
	 */
	@SimpleFunction
	boolean 是否有编辑();

	/**
	 * 添加具有字号、资产字体、背景颜色及对齐方式的可编辑文字。
	 *
	 * @param text 待添加的文字。
	 * @param color ARGB 文字颜色。
	 * @param size 大于零的 SP 字号。
	 * @param font 字体资产路径，空文本使用系统默认字体。
	 * @param background ARGB 背景颜色，可使用透明色。
	 * @param gravity Android Gravity 对齐值。
	 */
	@SimpleFunction
	void 添加样式文字(String text, int color, float size, String font, int background, int gravity);

	/**
	 * 设置后续保存的编码格式与质量；PNG 为无损编码，不使用质量参数。
	 *
	 * @param format 输出格式常量。
	 * @param quality 0 至 100，仅用于 JPEG；JPEG 不保存透明通道。
	 */
	@SimpleFunction
	void 置输出格式(int format, int quality);

	/**
	 * PNG 无损图片，支持透明通道。
	 */
	@SimpleDataElement
	int 输出格式_PNG = 0;

	/**
	 * JPEG 有损图片，不支持透明通道。
	 */
	@SimpleDataElement
	int 输出格式_JPEG = 1;

}
