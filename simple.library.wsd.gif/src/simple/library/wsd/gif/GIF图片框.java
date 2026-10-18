package simple.library.wsd.gif;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;
import simple.runtime.components.可视组件;

/**
 * 显示并控制 GIF 动画的可视组件。
 *
 * <p>在界面线程调用。读取与解码异步进行，最新请求生效，失败保留原图。
 * 不直接下载网址，不申请外部存储权限。输入限制为 8 MiB、宽高各 2048 像素、
 * 512 帧及画布面积乘帧数不超过 16777216；实际内存消耗仍取决于平台。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface GIF图片框 extends 可视组件 {

	/* 组件常量 */

	/**
	 * 保持宽高比，在组件内部完整显示。
	 */
	@SimpleDataElement
	int 缩放_等比适应 = 0;

	/**
	 * 拉伸至组件可用尺寸。
	 */
	@SimpleDataElement
	int 缩放_拉伸填满 = 1;

	/**
	 * 使用原始像素尺寸居中，超出部分裁剪。
	 */
	@SimpleDataElement
	int 缩放_原始居中 = 2;

	/* 组件事件 */

	/**
	 * 用户单击图片框时触发。
	 */
	@SimpleEvent
	void 被单击();

	/**
	 * 最新请求载入成功后在界面线程触发。
	 *
	 * @param width 原始宽度，单位像素。
	 * @param height 原始高度，单位像素。
	 * @param frames GIF 图像帧数量。
	 */
	@SimpleEvent
	void 载入成功(int width, int height, int frames);

	/**
	 * 最新请求载入失败后在界面线程触发，保留原来的图片和播放状态。
	 *
	 * @param message 读取、格式、大小限制或平台解码失败的说明。
	 */
	@SimpleEvent
	void 载入失败(String message);

	/**
	 * 非循环播放自然结束时触发一次，停留在最后一帧。
	 */
	@SimpleEvent
	void 播放完毕();

	/* 组件属性 */

	/**
	 * 读取图片。
	 *
	 * @return 已成功载入的资产名、资源标识或绝对路径；字节载入及清空后为空文本。
	 */
	@SimpleProperty
	String 图片();

	/**
	 * 设置图片。
	 *
	 * @param image 图片来源；空文本清空。支持资产名、@raw/名称、@drawable/名称和绝对文件路径。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_ASSET,
		initializer = "\"\""
	)
	void 图片(String image);

	/**
	 * 读取自动播放。
	 *
	 * @return 新图片载入成功后是否从头播放；不改变当前播放状态。
	 */
	@SimpleProperty
	boolean 自动播放();

	/**
	 * 设置自动播放。
	 *
	 * @param enabled 是否在新图片成功载入后自动播放。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 自动播放(boolean enabled);

	/**
	 * 读取循环播放。
	 *
	 * @return 是否持续循环；假为只播放一次，忽略 GIF 文件内的循环次数。
	 */
	@SimpleProperty
	boolean 循环播放();

	/**
	 * 设置循环播放。
	 *
	 * @param enabled 是否持续循环播放。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	void 循环播放(boolean enabled);

	/**
	 * 读取显示方式。
	 *
	 * @return 0 等比适应、1 拉伸填满、2 原始像素居中。
	 */
	@SimpleProperty
	int 显示方式();

	/**
	 * 设置显示方式。
	 *
	 * @param mode 使用缩放_等比适应、缩放_拉伸填满或缩放_原始居中。
	 * @throws IllegalArgumentException 显示方式无效，原值保持不变。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0"
	)
	void 显示方式(int mode);

	/**
	 * 读取正在载入。
	 *
	 * @return 当前是否正在读取、检查或解码最新请求。
	 */
	@SimpleProperty
	boolean 正在载入();

	/**
	 * 读取正在播放。
	 *
	 * @return 当前已开始播放并且组件和应用可见。
	 */
	@SimpleProperty
	boolean 正在播放();

	/**
	 * 读取图片宽度。
	 *
	 * @return 当前 GIF 的原始像素宽度；没有图片时为 0。
	 */
	@SimpleProperty
	int 图片宽度();

	/**
	 * 读取图片高度。
	 *
	 * @return 当前 GIF 的原始像素高度；没有图片时为 0。
	 */
	@SimpleProperty
	int 图片高度();

	/**
	 * 读取帧数。
	 *
	 * @return 当前 GIF 图像帧数量；没有图片时为 0。
	 */
	@SimpleProperty
	int 帧数();

	/**
	 * 读取动画时长。
	 *
	 * @return 当前 GIF 一轮播放时长，单位毫秒；没有图片时为 0。
	 */
	@SimpleProperty
	int 动画时长();

	/**
	 * 读取播放位置。
	 *
	 * @return 最近一次刷新后的播放时间，单位毫秒；停止和清空后为 0。
	 */
	@SimpleProperty
	int 播放位置();

	/**
	 * 读取最后错误。
	 *
	 * @return 最近一次载入失败的说明；新请求、成功或清空后为空文本。
	 */
	@SimpleProperty
	String 最后错误();

	/* 组件方法 */

	/**
	 * 从字节集异步载入 GIF，成功后图片属性为空文本。
	 *
	 * @param data 完整 GIF 文件字节集，复制后异步载入；空数据按失败事件反馈。
	 */
	@SimpleFunction
	void 载入字节图片(byte[] data);

	/**
	 * 从第一帧开始播放；尚未载入图片时不执行。
	 */
	@SimpleFunction
	void 开始播放();

	/**
	 * 暂停并保留当前播放位置。
	 */
	@SimpleFunction
	void 暂停播放();

	/**
	 * 从暂停位置继续；一次播放结束后从头继续。
	 */
	@SimpleFunction
	void 继续播放();

	/**
	 * 停止并回到第一帧，保留已载入的图片。
	 */
	@SimpleFunction
	void 停止播放();

	/**
	 * 取消待处理请求，停止刷新并释放组件持有的图片引用。
	 */
	@SimpleFunction
	void 清空图片();
}
