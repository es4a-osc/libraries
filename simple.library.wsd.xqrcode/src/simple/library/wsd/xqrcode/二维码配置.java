package simple.library.wsd.xqrcode;

import simple.runtime.Objects;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;
import simple.runtime.annotations.SimpleFunction;

/**
 * 二维码生成的独立配置，提交时按快照复制。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 二维码配置 implements Cloneable {
	private int size;
	private int width;
	private int height;
	private int margin;
	private float dotScale;
	private int dark;
	private int light;
	private String background;
	private boolean whiteMargin;
	private boolean autoColor;
	private boolean binarize;
	private int threshold;
	private String logo;
	private float logoScale;
	private byte[] logoBytes = new byte[0];
	private byte[] backgroundBytes = new byte[0];

	/**
	 * 创建具有声明默认值的独立配置。
	 */
	public 二维码配置() {
		Objects.initializeProperties(this);
	}

	/**
	 * 读取二维码正方形边长，单位像素，范围 32 至 2048。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 尺寸() {
		return size;
	}

	/**
	 * 设置二维码正方形边长，单位像素，范围 32 至 2048。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 输入为空、不是有限数值或超出允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "400"
	)
	public void 尺寸(int value) {
		if (value < 32 || value > 2048) {
			throw new IllegalArgumentException("尺寸的值不合法。");
		}
		size = value;
	}

	/**
	 * 读取输出宽度，0 使用尺寸；非零范围 32 至 2048。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 图像宽度() {
		return width;
	}

	/**
	 * 设置输出宽度，0 使用尺寸；非零范围 32 至 2048。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 输入为空、不是有限数值或超出允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0"
	)
	public void 图像宽度(int value) {
		if (value != 0 && (value < 32 || value > 2048)) {
			throw new IllegalArgumentException("图像宽度的值不合法。");
		}
		width = value;
	}

	/**
	 * 读取输出高度，0 使用尺寸；非零范围 32 至 2048。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 图像高度() {
		return height;
	}

	/**
	 * 设置输出高度，0 使用尺寸；非零范围 32 至 2048。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 输入为空、不是有限数值或超出允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0"
	)
	public void 图像高度(int value) {
		if (value != 0 && (value < 32 || value > 2048)) {
			throw new IllegalArgumentException("图像高度的值不合法。");
		}
		height = value;
	}

	/**
	 * 读取二维码边缘宽度，范围 0 至 256。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 边距() {
		return margin;
	}

	/**
	 * 设置二维码边缘宽度，范围 0 至 256。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 输入为空、不是有限数值或超出允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "12"
	)
	public void 边距(int value) {
		if (value < 0 || value > 256) {
			throw new IllegalArgumentException("边距的值不合法。");
		}
		margin = value;
	}

	/**
	 * 读取艺术二维码的数据点比例，大于 0 且不大于 1。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public float 数据点比例() {
		return dotScale;
	}

	/**
	 * 设置艺术二维码的数据点比例，大于 0 且不大于 1。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 输入为空、不是有限数值或超出允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "0.5"
	)
	public void 数据点比例(float value) {
		if (!Float.isFinite(value) || value <= 0 || value > 1) {
			throw new IllegalArgumentException("数据点比例的值不合法。");
		}
		dotScale = value;
	}

	/**
	 * 读取二维码深色点的 ARGB 颜色。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 深色() {
		return dark;
	}

	/**
	 * 设置二维码深色点的 ARGB 颜色。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_COLOR,
		initializer = "-16777216"
	)
	public void 深色(int value) {
		dark = value;
	}

	/**
	 * 读取二维码浅色点的 ARGB 颜色。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 浅色() {
		return light;
	}

	/**
	 * 设置二维码浅色点的 ARGB 颜色。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_COLOR,
		initializer = "-1"
	)
	public void 浅色(int value) {
		light = value;
	}

	/**
	 * 读取背景位图的资产、文件或内容 URI；空文本不使用背景。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 背景图片() {
		return background;
	}

	/**
	 * 设置背景位图的资产、文件或内容 URI；空文本不使用背景。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 输入为空、不是有限数值或超出允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"\""
	)
	public void 背景图片(String value) {
		if (value == null) {
			throw new IllegalArgumentException("背景图片的值不合法。");
		}
		background = value;
	}

	/**
	 * 读取是否保留艺术二维码的白色边缘。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 白色边缘() {
		return whiteMargin;
	}

	/**
	 * 设置是否保留艺术二维码的白色边缘。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	public void 白色边缘(boolean value) {
		whiteMargin = value;
	}

	/**
	 * 读取是否从背景自动选择艺术二维码的深色。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 自动取色() {
		return autoColor;
	}

	/**
	 * 设置是否从背景自动选择艺术二维码的深色。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	public void 自动取色(boolean value) {
		autoColor = value;
	}

	/**
	 * 读取是否对艺术二维码背景进行二值化。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 二值化() {
		return binarize;
	}

	/**
	 * 设置是否对艺术二维码背景进行二值化。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	public void 二值化(boolean value) {
		binarize = value;
	}

	/**
	 * 读取二值化阈值，范围 0 至 255。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 二值化阈值() {
		return threshold;
	}

	/**
	 * 设置二值化阈值，范围 0 至 255。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 输入为空、不是有限数值或超出允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "128"
	)
	public void 二值化阈值(int value) {
		if (value < 0 || value > 255) {
			throw new IllegalArgumentException("二值化阈值的值不合法。");
		}
		threshold = value;
	}

	/**
	 * 读取中心图标的资产、文件或内容 URI；空文本不使用图标。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 图标图片() {
		return logo;
	}

	/**
	 * 设置中心图标的资产、文件或内容 URI；空文本不使用图标。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 输入为空、不是有限数值或超出允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"\""
	)
	public void 图标图片(String value) {
		if (value == null) {
			throw new IllegalArgumentException("图标图片的值不合法。");
		}
		logo = value;
	}

	/**
	 * 读取图标相对二维码的比例，大于 0 且不大于 0.4。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public float 图标比例() {
		return logoScale;
	}

	/**
	 * 设置图标相对二维码的比例，大于 0 且不大于 0.4。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 输入为空、不是有限数值或超出允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "0.2"
	)
	public void 图标比例(float value) {
		if (!Float.isFinite(value) || value <= 0 || value > 0.4f) {
			throw new IllegalArgumentException("图标比例的值不合法。");
		}
		logoScale = value;
	}

	/**
	 * 设置图标图片字节，非空时优先于对应图片路径。
	 *
	 * @param values 图片数据；空数组恢复路径输入。
	 * @throws IllegalArgumentException 数组为空引用或超过 32 MiB。
	 */
	@SimpleFunction
	public void 置图标字节(byte[] values) {
		if (values == null || values.length > 32 * 1024 * 1024) {
			throw new IllegalArgumentException("图片字节无效或过大。");
		}
		logoBytes = values.clone();
	}

	/**
	 * 读取图标图片字节副本。
	 *
	 * @return 独立图片字节数组。
	 */
	@SimpleFunction
	public byte[] 取图标字节() {
		return logoBytes.clone();
	}

	/**
	 * 设置背景图片字节，非空时优先于对应图片路径。
	 *
	 * @param values 图片数据；空数组恢复路径输入。
	 * @throws IllegalArgumentException 数组为空引用或超过 32 MiB。
	 */
	@SimpleFunction
	public void 置背景字节(byte[] values) {
		if (values == null || values.length > 32 * 1024 * 1024) {
			throw new IllegalArgumentException("图片字节无效或过大。");
		}
		backgroundBytes = values.clone();
	}

	/**
	 * 读取背景图片字节副本。
	 *
	 * @return 独立图片字节数组。
	 */
	@SimpleFunction
	public byte[] 取背景字节() {
		return backgroundBytes.clone();
	}

	/**
	 * 复制配置与数组，供一次调用独立持有。
	 *
	 * @return 独立配置快照。
	 */
	public 二维码配置 copy() {
		try {
			二维码配置 value = (二维码配置) clone();
			value.logoBytes = logoBytes.clone();
			value.backgroundBytes = backgroundBytes.clone();
			return value;
		} catch (CloneNotSupportedException exception) {
			throw new AssertionError(exception);
		}
	}
}
