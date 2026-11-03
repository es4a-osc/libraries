package simple.library.wsd.xqrcode;

import simple.runtime.Objects;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 扫描会话的独立配置，尺寸按预览区域约束，不影响其它实例。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XQRC扫码配置 implements Cloneable {
	private boolean continuous;
	private long interval;
	private long focusInterval;
	private boolean beep;
	private boolean vibrate;
	private int vibrationDuration;
	private boolean keepAwake;
	private boolean torch;
	private int cameraId;
	private int frameWidth;
	private int frameHeight;
	private int top;
	private int cornerColor;
	private float cornerLength;
	private float cornerWidth;
	private int maskColor;
	private int resultColor;
	private int pointColor;
	private String scanImage;
	private int scanTint;
	private float velocity;
	private int animationInterval;
	private boolean showPoints;
	private boolean returnImage;
	private boolean onlyFrame;
	private String charset;
	private int themeResource;
	private String prompt;
	private boolean showPossiblePoints;
	private String[] formats = new String[0];

	/**
	 * 创建具有声明默认值的独立配置。
	 */
	public XQRC扫码配置() {
		Objects.initializeProperties(this);
	}

	/**
	 * 读取是否连续输出扫描结果。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 连续扫描() {
		return continuous;
	}

	/**
	 * 设置是否连续输出扫描结果。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	public void 连续扫描(boolean value) {
		continuous = value;
	}

	/**
	 * 读取连续扫描两次识别之间的间隔，100 至 60000 毫秒。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public long 连续间隔() {
		return interval;
	}

	/**
	 * 设置连续扫描两次识别之间的间隔，100 至 60000 毫秒。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 输入为空、不是有限数值或超出允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_LONG,
		initializer = "1000"
	)
	public void 连续间隔(long value) {
		if (value < 100 || value > 60000) {
			throw new IllegalArgumentException("连续间隔的值不合法。");
		}
		interval = value;
	}

	/**
	 * 读取自动对焦间隔，200 至 10000 毫秒。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public long 对焦间隔() {
		return focusInterval;
	}

	/**
	 * 设置自动对焦间隔，200 至 10000 毫秒。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 输入为空、不是有限数值或超出允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_LONG,
		initializer = "1500"
	)
	public void 对焦间隔(long value) {
		if (value < 200 || value > 10000) {
			throw new IllegalArgumentException("对焦间隔的值不合法。");
		}
		focusInterval = value;
	}

	/**
	 * 读取成功识别后是否播放提示音，静音模式下不播放。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 提示音() {
		return beep;
	}

	/**
	 * 设置成功识别后是否播放提示音，静音模式下不播放。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	public void 提示音(boolean value) {
		beep = value;
	}

	/**
	 * 读取成功识别后是否振动，需要设备具备振动器。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 振动() {
		return vibrate;
	}

	/**
	 * 设置成功识别后是否振动，需要设备具备振动器。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	public void 振动(boolean value) {
		vibrate = value;
	}

	/**
	 * 读取成功提示的振动时长，0 至 3000 毫秒。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 振动时长() {
		return vibrationDuration;
	}

	/**
	 * 设置成功提示的振动时长，0 至 3000 毫秒。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 输入为空、不是有限数值或超出允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "200"
	)
	public void 振动时长(int value) {
		if (value < 0 || value > 3000) {
			throw new IllegalArgumentException("振动时长的值不合法。");
		}
		vibrationDuration = value;
	}

	/**
	 * 读取扫描期间是否保持屏幕点亮。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 保持亮屏() {
		return keepAwake;
	}

	/**
	 * 设置扫描期间是否保持屏幕点亮。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	public void 保持亮屏(boolean value) {
		keepAwake = value;
	}

	/**
	 * 读取开始扫描时是否请求打开相机补光灯，需要相机支持。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 初始补光() {
		return torch;
	}

	/**
	 * 设置开始扫描时是否请求打开相机补光灯，需要相机支持。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	public void 初始补光(boolean value) {
		torch = value;
	}

	/**
	 * 读取相机编号；-1 优先使用后置相机。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 相机编号() {
		return cameraId;
	}

	/**
	 * 设置相机编号；-1 优先使用后置相机。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 输入为空、不是有限数值或超出允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "-1"
	)
	public void 相机编号(int value) {
		if (value < -1 || value > 32) {
			throw new IllegalArgumentException("相机编号的值不合法。");
		}
		cameraId = value;
	}

	/**
	 * 读取扫描框宽度，单位 DIP；0 自动，最大 4096。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 扫描框宽度() {
		return frameWidth;
	}

	/**
	 * 设置扫描框宽度，单位 DIP；0 自动，最大 4096。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 输入为空、不是有限数值或超出允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0"
	)
	public void 扫描框宽度(int value) {
		if (value < 0 || value > 4096) {
			throw new IllegalArgumentException("扫描框宽度的值不合法。");
		}
		frameWidth = value;
	}

	/**
	 * 读取扫描框高度，单位 DIP；0 自动，最大 4096。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 扫描框高度() {
		return frameHeight;
	}

	/**
	 * 设置扫描框高度，单位 DIP；0 自动，最大 4096。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 输入为空、不是有限数值或超出允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0"
	)
	public void 扫描框高度(int value) {
		if (value < 0 || value > 4096) {
			throw new IllegalArgumentException("扫描框高度的值不合法。");
		}
		frameHeight = value;
	}

	/**
	 * 读取扫描框顶部偏移，单位 DIP；-1 居中，最大 4096。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 顶部偏移() {
		return top;
	}

	/**
	 * 设置扫描框顶部偏移，单位 DIP；-1 居中，最大 4096。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 输入为空、不是有限数值或超出允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "-1"
	)
	public void 顶部偏移(int value) {
		if (value < -1 || value > 4096) {
			throw new IllegalArgumentException("顶部偏移的值不合法。");
		}
		top = value;
	}

	/**
	 * 读取扫描框边角的 ARGB 颜色。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 边角颜色() {
		return cornerColor;
	}

	/**
	 * 设置扫描框边角的 ARGB 颜色。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_COLOR,
		initializer = "-15940866"
	)
	public void 边角颜色(int value) {
		cornerColor = value;
	}

	/**
	 * 读取边角长度，单位 DIP，0 至 256。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public float 边角长度() {
		return cornerLength;
	}

	/**
	 * 设置边角长度，单位 DIP，0 至 256。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 输入为空、不是有限数值或超出允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "32"
	)
	public void 边角长度(float value) {
		if (!Float.isFinite(value) || value < 0 || value > 256) {
			throw new IllegalArgumentException("边角长度的值不合法。");
		}
		cornerLength = value;
	}

	/**
	 * 读取边角线宽，单位 DIP，大于 0 且不大于 64。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public float 边角线宽() {
		return cornerWidth;
	}

	/**
	 * 设置边角线宽，单位 DIP，大于 0 且不大于 64。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 输入为空、不是有限数值或超出允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "3"
	)
	public void 边角线宽(float value) {
		if (!Float.isFinite(value) || value <= 0 || value > 64) {
			throw new IllegalArgumentException("边角线宽的值不合法。");
		}
		cornerWidth = value;
	}

	/**
	 * 读取扫描框外遮罩的 ARGB 颜色。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 遮罩颜色() {
		return maskColor;
	}

	/**
	 * 设置扫描框外遮罩的 ARGB 颜色。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_COLOR,
		initializer = "1610612736"
	)
	public void 遮罩颜色(int value) {
		maskColor = value;
	}

	/**
	 * 读取单次识别成功后的遮罩颜色。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 结果遮罩颜色() {
		return resultColor;
	}

	/**
	 * 设置单次识别成功后的遮罩颜色。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_COLOR,
		initializer = "-1342177280"
	)
	public void 结果遮罩颜色(int value) {
		resultColor = value;
	}

	/**
	 * 读取识别结果点的 ARGB 颜色。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 识别点颜色() {
		return pointColor;
	}

	/**
	 * 设置识别结果点的 ARGB 颜色。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_COLOR,
		initializer = "-16128"
	)
	public void 识别点颜色(int value) {
		pointColor = value;
	}

	/**
	 * 读取自定义扫描线位图来源；空文本使用默认扫描线。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 扫描图像() {
		return scanImage;
	}

	/**
	 * 设置自定义扫描线位图来源；空文本使用默认扫描线。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 输入为空、不是有限数值或超出允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"\""
	)
	public void 扫描图像(String value) {
		if (value == null) {
			throw new IllegalArgumentException("扫描图像的值不合法。");
		}
		scanImage = value;
	}

	/**
	 * 读取自定义扫描线的 ARGB 着色；-1 保留原色。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 扫描图着色() {
		return scanTint;
	}

	/**
	 * 设置自定义扫描线的 ARGB 着色；-1 保留原色。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_COLOR,
		initializer = "-1"
	)
	public void 扫描图着色(int value) {
		scanTint = value;
	}

	/**
	 * 读取每帧扫描线移动距离，单位 DIP，大于 0 且不大于 100。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public float 扫描速度() {
		return velocity;
	}

	/**
	 * 设置每帧扫描线移动距离，单位 DIP，大于 0 且不大于 100。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 输入为空、不是有限数值或超出允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "5"
	)
	public void 扫描速度(float value) {
		if (!Float.isFinite(value) || value <= 0 || value > 100) {
			throw new IllegalArgumentException("扫描速度的值不合法。");
		}
		velocity = value;
	}

	/**
	 * 读取扫描线绘制间隔，10 至 250 毫秒。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 动画间隔() {
		return animationInterval;
	}

	/**
	 * 设置扫描线绘制间隔，10 至 250 毫秒。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 输入为空、不是有限数值或超出允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "25"
	)
	public void 动画间隔(int value) {
		if (value < 10 || value > 250) {
			throw new IllegalArgumentException("动画间隔的值不合法。");
		}
		animationInterval = value;
	}

	/**
	 * 读取是否绘制扫描结果点。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 显示结果点() {
		return showPoints;
	}

	/**
	 * 设置是否绘制扫描结果点。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	public void 显示结果点(boolean value) {
		showPoints = value;
	}

	/**
	 * 读取是否在结果对象中返回识别区域的 PNG 图像。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 返回识别图像() {
		return returnImage;
	}

	/**
	 * 设置是否在结果对象中返回识别区域的 PNG 图像。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	public void 返回识别图像(boolean value) {
		returnImage = value;
	}

	/**
	 * 读取是否只识别扫描框内的预览内容。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 仅识别框内() {
		return onlyFrame;
	}

	/**
	 * 设置是否只识别扫描框内的预览内容。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	public void 仅识别框内(boolean value) {
		onlyFrame = value;
	}

	/**
	 * 读取指定识别字符集；空文本由引擎判断。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 字符集() {
		return charset;
	}

	/**
	 * 设置指定识别字符集；空文本由引擎判断。
	 *
	 * @param value 新的配置值。
	 * @throws IllegalArgumentException 输入为空、不是有限数值或超出允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"\""
	)
	public void 字符集(String value) {
		if (value == null || (!value.isEmpty() && !java.nio.charset.Charset.isSupported(value))) {
			throw new IllegalArgumentException("字符集的值不合法。");
		}
		charset = value;
	}

	/**
	 * 设置识别格式；空数组使用二维码、Data Matrix 和常用一维码。
	 *
	 * @param values ZXing 格式名称数组。
	 * @throws IllegalArgumentException 数组无效或格式名称不受支持。
	 */
	@SimpleFunction
	public void 置识别格式(String[] values) {
		if (values == null || values.length > 32) {
			throw new IllegalArgumentException("识别格式数组无效。");
		}
		String[] copy = values.clone();
		for (int index = 0; index < copy.length; index++) {
			if (copy[index] == null) {
				throw new IllegalArgumentException("格式名称不能为空引用。");
			}
			copy[index] = copy[index].trim().toUpperCase(java.util.Locale.ROOT);
			simple.library.wsd.xqrcode.internal.zxing.BarcodeFormat.valueOf(copy[index]);
		}
		formats = copy;
	}

	/**
	 * 读取识别格式的独立副本。
	 *
	 * @return 空数组表示使用默认格式集合。
	 */
	@SimpleFunction
	public String[] 取识别格式() {
		return formats.clone();
	}

	/**
	 * 复制配置与数组，供一次调用独立持有。
	 *
	 * @return 独立配置快照。
	 */
	public XQRC扫码配置 copy() {
		try {
			XQRC扫码配置 value = (XQRC扫码配置) clone();
			value.formats = formats.clone();
			return value;
		} catch (CloneNotSupportedException exception) {
			throw new AssertionError(exception);
		}
	}

	/**
	 * 返回包含独立格式数组的配置副本。
	 *
	 * @return 可独立修改的配置副本。
	 */
	@SimpleFunction
	public XQRC扫码配置 复制() {
		return copy();
	}

	/**
	 * 独立扫码页的原生Activity主题资源；0使用默认主题，内嵌框忽略此项。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 主题资源() {
		return themeResource;
	}

	/**
	 * 独立扫码页的原生Activity主题资源；0使用默认主题，内嵌框忽略此项。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0"
	)
	public void 主题资源(int value) {
		if (value < 0) {
			throw new XQRC二维码错误("主题资源不能为负");
		}
		themeResource = value;
	}

	/**
	 * 独立扫码页提示文字。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 提示文字() {
		return prompt;
	}

	/**
	 * 独立扫码页提示文字。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"将二维码或条码置于框内\""
	)
	public void 提示文字(String value) {
		if (value == null) {
			throw new XQRC二维码错误("文本不能为引用空");
		}
		prompt = value;
	}

	/**
	 * 显示解码器当前发现的候选定位点，不代表识别成功。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 显示实时识别点() {
		return showPossiblePoints;
	}

	/**
	 * 显示解码器当前发现的候选定位点，不代表识别成功。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	public void 显示实时识别点(boolean value) {
		showPossiblePoints = value;
	}
}
