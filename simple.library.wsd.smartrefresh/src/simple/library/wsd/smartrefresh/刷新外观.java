package simple.library.wsd.smartrefresh;

import java.text.SimpleDateFormat;
import java.util.Locale;
import simple.runtime.Objects;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;
import simple.runtime.annotations.SimpleFunction;

/**
 * 可复用的刷新头尾外观快照；修改后通过智能刷新框的应用外观方法生效。
 *
 * <p>经典、雷达、Material、球脉冲、仓库和游戏选项只作用于相应样式。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 刷新外观 implements Cloneable {
	private int spinnerStyle;
	private int primaryColor;
	private int accentColor;
	private float titleSize;
	private int finishDuration;
	private String arrowImage;
	private String progressImage;
	private float arrowSize;
	private float progressSize;
	private float drawableMargin;
	private String pullText;
	private String releaseText;
	private String workingText;
	private String finishText;
	private String failureText;
	private String oppositeText;
	private String noMoreText;
	private String twoLevelText;
	private boolean showTime;
	private long updatedAt;
	private String timeFormat;
	private String updateText;
	private float timeSize;
	private float timeMargin;
	private boolean radarHorizontal;
	private boolean materialLarge;
	private boolean materialWave;
	private boolean materialScrollable;
	private int ballNormalColor;
	private int ballAnimatingColor;
	private String storeText;
	private int storeFont;
	private float storeScale;
	private float storeLineWidth;
	private float storeDropHeight;
	private int storeDuration;
	private String gamePullText;
	private String gameReleaseText;
	private String gameControlText;
	private String gameOverText;
	private String gameLoadingText;
	private String gameFinishText;
	private String gameFailureText;
	private boolean customHorizontal;
	private int[] themeColors = new int[0];
	private int[] colors = new int[0];
	private float[] points = new float[0];

	/**
	 * 创建拥有注解默认值的独立配置，不访问 Android 界面。
	 */
	public 刷新外观() {
		Objects.initializeProperties(this);
	}

	/**
	 * 返回-1 使用原生样式；0 平移，1 拉伸，2 固定背后，3 固定前面，4 填满布局。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 变换样式() {
		return spinnerStyle;
	}

	/**
	 * 设置-1 使用原生样式；0 平移，1 拉伸，2 固定背后，3 固定前面，4 填满布局。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "-1"
	)
	public void 变换样式(int value) {
		if (value < -1 || value > 4) {
			throw new IllegalArgumentException("外观数值超出允许范围。");
		}
		spinnerStyle = value;
	}

	/**
	 * 返回头尾背景的 ARGB 颜色。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 主颜色() {
		return primaryColor;
	}

	/**
	 * 设置头尾背景的 ARGB 颜色。
	 *
	 * @param value 新配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_COLOR,
		initializer = "-1118482"
	)
	public void 主颜色(int value) {
		primaryColor = value;
	}

	/**
	 * 返回文字与图形的 ARGB 颜色。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 强调颜色() {
		return accentColor;
	}

	/**
	 * 设置文字与图形的 ARGB 颜色。
	 *
	 * @param value 新配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_COLOR,
		initializer = "-12303292"
	)
	public void 强调颜色(int value) {
		accentColor = value;
	}

	/**
	 * 返回经典头尾标题字号，单位 SP，范围 1 至 200。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public float 标题字号() {
		return titleSize;
	}

	/**
	 * 设置经典头尾标题字号，单位 SP，范围 1 至 200。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "16"
	)
	public void 标题字号(float value) {
		if (!Float.isFinite(value) || value < 1 || value > 200) {
			throw new IllegalArgumentException("外观数值超出允许范围。");
		}
		titleSize = value;
	}

	/**
	 * 返回完成后停留的毫秒数，范围 0 至 10000；同样用于自定义头尾。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 完成停留时间() {
		return finishDuration;
	}

	/**
	 * 设置完成后停留的毫秒数，范围 0 至 10000；同样用于自定义头尾。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "500"
	)
	public void 完成停留时间(int value) {
		if (value < 0 || value > 10000) {
			throw new IllegalArgumentException("外观数值超出允许范围。");
		}
		finishDuration = value;
	}

	/**
	 * 返回经典头尾箭头的资源标识、资产文件名或绝对路径；空文本使用原生箭头。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 箭头图片() {
		return arrowImage;
	}

	/**
	 * 设置经典头尾箭头的资源标识、资产文件名或绝对路径；空文本使用原生箭头。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"\""
	)
	public void 箭头图片(String value) {
		if (value == null) {
			throw new IllegalArgumentException("文字不能为空引用。");
		}
		arrowImage = value;
	}

	/**
	 * 返回经典头尾进度图形的资源标识、资产文件名或绝对路径；空文本使用原生动画图形。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 进度图片() {
		return progressImage;
	}

	/**
	 * 设置经典头尾进度图形的资源标识、资产文件名或绝对路径；空文本使用原生动画图形。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"\""
	)
	public void 进度图片(String value) {
		if (value == null) {
			throw new IllegalArgumentException("文字不能为空引用。");
		}
		progressImage = value;
	}

	/**
	 * 返回经典箭头宽高，单位 DIP，范围 1 至 1000。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public float 箭头尺寸() {
		return arrowSize;
	}

	/**
	 * 设置经典箭头宽高，单位 DIP，范围 1 至 1000。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "20"
	)
	public void 箭头尺寸(float value) {
		if (!Float.isFinite(value) || value < 1 || value > 1000) {
			throw new IllegalArgumentException("外观数值超出允许范围。");
		}
		arrowSize = value;
	}

	/**
	 * 返回经典进度图形宽高，单位 DIP，范围 1 至 1000。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public float 进度尺寸() {
		return progressSize;
	}

	/**
	 * 设置经典进度图形宽高，单位 DIP，范围 1 至 1000。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "20"
	)
	public void 进度尺寸(float value) {
		if (!Float.isFinite(value) || value < 1 || value > 1000) {
			throw new IllegalArgumentException("外观数值超出允许范围。");
		}
		progressSize = value;
	}

	/**
	 * 返回经典图形与文字的间距，单位 DIP，范围 0 至 1000。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public float 图形右边距() {
		return drawableMargin;
	}

	/**
	 * 设置经典图形与文字的间距，单位 DIP，范围 0 至 1000。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "20"
	)
	public void 图形右边距(float value) {
		if (!Float.isFinite(value) || value < 0 || value > 1000) {
			throw new IllegalArgumentException("外观数值超出允许范围。");
		}
		drawableMargin = value;
	}

	/**
	 * 返回未达到触发阈值时的经典提示。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 拉动提示() {
		return pullText;
	}

	/**
	 * 设置未达到触发阈值时的经典提示。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"下拉可以刷新\""
	)
	public void 拉动提示(String value) {
		if (value == null) {
			throw new IllegalArgumentException("文字不能为空引用。");
		}
		pullText = value;
	}

	/**
	 * 返回达到阈值等待松手时的经典提示。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 释放提示() {
		return releaseText;
	}

	/**
	 * 设置达到阈值等待松手时的经典提示。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"松开立即刷新\""
	)
	public void 释放提示(String value) {
		if (value == null) {
			throw new IllegalArgumentException("文字不能为空引用。");
		}
		releaseText = value;
	}

	/**
	 * 返回本方向正在处理时的经典提示。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 处理中提示() {
		return workingText;
	}

	/**
	 * 设置本方向正在处理时的经典提示。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"正在刷新…\""
	)
	public void 处理中提示(String value) {
		if (value == null) {
			throw new IllegalArgumentException("文字不能为空引用。");
		}
		workingText = value;
	}

	/**
	 * 返回成功完成时的经典提示。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 完成提示() {
		return finishText;
	}

	/**
	 * 设置成功完成时的经典提示。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"操作完成\""
	)
	public void 完成提示(String value) {
		if (value == null) {
			throw new IllegalArgumentException("文字不能为空引用。");
		}
		finishText = value;
	}

	/**
	 * 返回失败时的经典提示。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 失败提示() {
		return failureText;
	}

	/**
	 * 设置失败时的经典提示。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"操作失败\""
	)
	public void 失败提示(String value) {
		if (value == null) {
			throw new IllegalArgumentException("文字不能为空引用。");
		}
		failureText = value;
	}

	/**
	 * 返回另一方向正在处理时的经典提示。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 另一方向提示() {
		return oppositeText;
	}

	/**
	 * 设置另一方向正在处理时的经典提示。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"正在加载…\""
	)
	public void 另一方向提示(String value) {
		if (value == null) {
			throw new IllegalArgumentException("文字不能为空引用。");
		}
		oppositeText = value;
	}

	/**
	 * 返回经典加载尾的分页结束提示。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 无更多提示() {
		return noMoreText;
	}

	/**
	 * 设置经典加载尾的分页结束提示。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"没有更多数据了\""
	)
	public void 无更多提示(String value) {
		if (value == null) {
			throw new IllegalArgumentException("文字不能为空引用。");
		}
		noMoreText = value;
	}

	/**
	 * 返回经典刷新头达到二楼阈值时的提示。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 二楼提示() {
		return twoLevelText;
	}

	/**
	 * 设置经典刷新头达到二楼阈值时的提示。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"松开进入二楼\""
	)
	public void 二楼提示(String value) {
		if (value == null) {
			throw new IllegalArgumentException("文字不能为空引用。");
		}
		twoLevelText = value;
	}

	/**
	 * 返回经典刷新头是否显示更新时间。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 显示更新时间() {
		return showTime;
	}

	/**
	 * 设置经典刷新头是否显示更新时间。
	 *
	 * @param value 新配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	public void 显示更新时间(boolean value) {
		showTime = value;
	}

	/**
	 * 返回自 Unix 纪元起的毫秒数；-1 保留原生时间。成功刷新由原生组件更新时间。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public long 更新时间() {
		return updatedAt;
	}

	/**
	 * 设置自 Unix 纪元起的毫秒数；-1 保留原生时间。成功刷新由原生组件更新时间。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_LONG,
		initializer = "-1"
	)
	public void 更新时间(long value) {
		if (value < -1) {
			throw new IllegalArgumentException("时间不能小于 -1。");
		}
		updatedAt = value;
	}

	/**
	 * 返回经典刷新头使用的 SimpleDateFormat 格式。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 时间格式() {
		return timeFormat;
	}

	/**
	 * 设置经典刷新头使用的 SimpleDateFormat 格式。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"上次更新 M-d HH:mm\""
	)
	public void 时间格式(String value) {
		if (value == null) {
			throw new IllegalArgumentException("文字不能为空引用。");
		}
		new SimpleDateFormat(value, Locale.getDefault());
		timeFormat = value;
	}

	/**
	 * 返回非空时用自定义文字替代时间；空文本恢复日期格式。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 更新时间文字() {
		return updateText;
	}

	/**
	 * 设置非空时用自定义文字替代时间；空文本恢复日期格式。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"\""
	)
	public void 更新时间文字(String value) {
		if (value == null) {
			throw new IllegalArgumentException("文字不能为空引用。");
		}
		updateText = value;
	}

	/**
	 * 返回更新时间字号，单位 SP，范围 1 至 200。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public float 时间字号() {
		return timeSize;
	}

	/**
	 * 设置更新时间字号，单位 SP，范围 1 至 200。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "12"
	)
	public void 时间字号(float value) {
		if (!Float.isFinite(value) || value < 1 || value > 200) {
			throw new IllegalArgumentException("外观数值超出允许范围。");
		}
		timeSize = value;
	}

	/**
	 * 返回更新时间文字的上边距，单位 DIP，范围 0 至 1000。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public float 时间上边距() {
		return timeMargin;
	}

	/**
	 * 设置更新时间文字的上边距，单位 DIP，范围 0 至 1000。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "0"
	)
	public void 时间上边距(float value) {
		if (!Float.isFinite(value) || value < 0 || value > 1000) {
			throw new IllegalArgumentException("外观数值超出允许范围。");
		}
		timeMargin = value;
	}

	/**
	 * 返回雷达刷新头是否响应水平拖动。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 雷达横向拖动() {
		return radarHorizontal;
	}

	/**
	 * 设置雷达刷新头是否响应水平拖动。
	 *
	 * @param value 新配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	public void 雷达横向拖动(boolean value) {
		radarHorizontal = value;
	}

	/**
	 * 返回Material 刷新头是否使用大号圆圈。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean Material大号圆圈() {
		return materialLarge;
	}

	/**
	 * 设置Material 刷新头是否使用大号圆圈。
	 *
	 * @param value 新配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	public void Material大号圆圈(boolean value) {
		materialLarge = value;
	}

	/**
	 * 返回Material 刷新头是否显示贝塞尔波浪。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean Material波浪() {
		return materialWave;
	}

	/**
	 * 设置Material 刷新头是否显示贝塞尔波浪。
	 *
	 * @param value 新配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	public void Material波浪(boolean value) {
		materialWave = value;
	}

	/**
	 * 返回Material 刷新期间是否允许内容滚动。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean Material刷新中滚动() {
		return materialScrollable;
	}

	/**
	 * 设置Material 刷新期间是否允许内容滚动。
	 *
	 * @param value 新配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	public void Material刷新中滚动(boolean value) {
		materialScrollable = value;
	}

	/**
	 * 返回球脉冲加载尾的空闲颜色。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 球普通颜色() {
		return ballNormalColor;
	}

	/**
	 * 设置球脉冲加载尾的空闲颜色。
	 *
	 * @param value 新配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_COLOR,
		initializer = "-1118482"
	)
	public void 球普通颜色(int value) {
		ballNormalColor = value;
	}

	/**
	 * 返回球脉冲加载尾的动画颜色。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 球动画颜色() {
		return ballAnimatingColor;
	}

	/**
	 * 设置球脉冲加载尾的动画颜色。
	 *
	 * @param value 新配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_COLOR,
		initializer = "-12303292"
	)
	public void 球动画颜色(int value) {
		ballAnimatingColor = value;
	}

	/**
	 * 返回仓库动画的文字路径；使用上游支持的英文字母、数字及符号。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 仓库文字() {
		return storeText;
	}

	/**
	 * 设置仓库动画的文字路径；使用上游支持的英文字母、数字及符号。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"ES4A\""
	)
	public void 仓库文字(String value) {
		if (value == null) {
			throw new IllegalArgumentException("文字不能为空引用。");
		}
		storeText = value;
	}

	/**
	 * 返回仓库文字的路径字号，范围 1 至 200。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 仓库字号() {
		return storeFont;
	}

	/**
	 * 设置仓库文字的路径字号，范围 1 至 200。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "25"
	)
	public void 仓库字号(int value) {
		if (value < 1 || value > 200) {
			throw new IllegalArgumentException("外观数值超出允许范围。");
		}
		storeFont = value;
	}

	/**
	 * 返回仓库动画的缩放比例，范围大于 0 且不大于 20。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public float 仓库缩放() {
		return storeScale;
	}

	/**
	 * 设置仓库动画的缩放比例，范围大于 0 且不大于 20。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "1"
	)
	public void 仓库缩放(float value) {
		if (!Float.isFinite(value) || value <= 0 || value > 20) {
			throw new IllegalArgumentException("外观数值超出允许范围。");
		}
		storeScale = value;
	}

	/**
	 * 返回仓库动画线宽，单位 DIP，范围大于 0 且不大于 100。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public float 仓库线宽() {
		return storeLineWidth;
	}

	/**
	 * 设置仓库动画线宽，单位 DIP，范围大于 0 且不大于 100。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "1"
	)
	public void 仓库线宽(float value) {
		if (!Float.isFinite(value) || value <= 0 || value > 100) {
			throw new IllegalArgumentException("外观数值超出允许范围。");
		}
		storeLineWidth = value;
	}

	/**
	 * 返回仓库动画线段的下落高度，单位 DIP，范围 0 至 10000。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public float 仓库下落高度() {
		return storeDropHeight;
	}

	/**
	 * 设置仓库动画线段的下落高度，单位 DIP，范围 0 至 10000。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "40"
	)
	public void 仓库下落高度(float value) {
		if (!Float.isFinite(value) || value < 0 || value > 10000) {
			throw new IllegalArgumentException("外观数值超出允许范围。");
		}
		storeDropHeight = value;
	}

	/**
	 * 返回仓库循环动画的毫秒数，范围 1 至 10000。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 仓库动画时长() {
		return storeDuration;
	}

	/**
	 * 设置仓库循环动画的毫秒数，范围 1 至 10000。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "1000"
	)
	public void 仓库动画时长(int value) {
		if (value < 1 || value > 10000) {
			throw new IllegalArgumentException("外观数值超出允许范围。");
		}
		storeDuration = value;
	}

	/**
	 * 返回游戏刷新头的拉动提示。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 游戏拉动提示() {
		return gamePullText;
	}

	/**
	 * 设置游戏刷新头的拉动提示。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"下拉展开游戏\""
	)
	public void 游戏拉动提示(String value) {
		if (value == null) {
			throw new IllegalArgumentException("文字不能为空引用。");
		}
		gamePullText = value;
	}

	/**
	 * 返回游戏刷新头的释放提示。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 游戏释放提示() {
		return gameReleaseText;
	}

	/**
	 * 设置游戏刷新头的释放提示。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"松开开始游戏\""
	)
	public void 游戏释放提示(String value) {
		if (value == null) {
			throw new IllegalArgumentException("文字不能为空引用。");
		}
		gameReleaseText = value;
	}

	/**
	 * 返回游戏刷新头的操作提示。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 游戏操作提示() {
		return gameControlText;
	}

	/**
	 * 设置游戏刷新头的操作提示。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"拖动控制游戏\""
	)
	public void 游戏操作提示(String value) {
		if (value == null) {
			throw new IllegalArgumentException("文字不能为空引用。");
		}
		gameControlText = value;
	}

	/**
	 * 返回游戏结束提示。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 游戏结束提示() {
		return gameOverText;
	}

	/**
	 * 设置游戏结束提示。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"游戏结束\""
	)
	public void 游戏结束提示(String value) {
		if (value == null) {
			throw new IllegalArgumentException("文字不能为空引用。");
		}
		gameOverText = value;
	}

	/**
	 * 返回游戏内显示的业务等待提示。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 游戏加载提示() {
		return gameLoadingText;
	}

	/**
	 * 设置游戏内显示的业务等待提示。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"正在处理…\""
	)
	public void 游戏加载提示(String value) {
		if (value == null) {
			throw new IllegalArgumentException("文字不能为空引用。");
		}
		gameLoadingText = value;
	}

	/**
	 * 返回游戏内显示的业务成功提示。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 游戏完成提示() {
		return gameFinishText;
	}

	/**
	 * 设置游戏内显示的业务成功提示。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"处理完成\""
	)
	public void 游戏完成提示(String value) {
		if (value == null) {
			throw new IllegalArgumentException("文字不能为空引用。");
		}
		gameFinishText = value;
	}

	/**
	 * 返回游戏内显示的业务失败提示。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public String 游戏失败提示() {
		return gameFailureText;
	}

	/**
	 * 设置游戏内显示的业务失败提示。
	 *
	 * @param value 新配置值。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"处理失败\""
	)
	public void 游戏失败提示(String value) {
		if (value == null) {
			throw new IllegalArgumentException("文字不能为空引用。");
		}
		gameFailureText = value;
	}

	/**
	 * 返回自定义头尾是否报告水平拖动。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 自定义横向拖动() {
		return customHorizontal;
	}

	/**
	 * 设置自定义头尾是否报告水平拖动。
	 *
	 * @param value 新配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	public void 自定义横向拖动(boolean value) {
		customHorizontal = value;
	}

	/**
	 * 设置 Material 与波浪刷新头的颜色序列；空数组恢复主色和强调色。
	 *
	 * @param values 独立复制的 ARGB 颜色数组。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleFunction
	public void 置动画颜色(int[] values) {
		if (values == null) {
			throw new IllegalArgumentException("颜色数组不能为空引用。");
		}
		colors = values.clone();
	}

	/**
	 * 返回动画颜色序列的副本。
	 *
	 * @return 独立颜色数组。
	 */
	@SimpleFunction
	public int[] 取动画颜色() {
		return colors.clone();
	}

	/**
	 * 设置仓库动画线段，每四个数依次为起点 X、起点 Y、终点 X、终点 Y。
	 *
	 * @param values 有限坐标数组；空数组使用仓库文字。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleFunction
	public void 置仓库线段(float[] values) {
		if (values == null || values.length % 4 != 0) {
			throw new IllegalArgumentException("线段坐标必须按四个数分组。");
		}
		for (float value : values) {
			if (!Float.isFinite(value)) {
				throw new IllegalArgumentException("线段坐标必须是有限数值。");
			}
		}
		points = values.clone();
	}

	/**
	 * 返回仓库动画坐标的副本。
	 *
	 * @return 每四个数表示一条线段的坐标数组。
	 */
	@SimpleFunction
	public float[] 取仓库线段() {
		return points.clone();
	}

	/**
	 * 复制配置及数组，避免应用后调用方继续修改同一份状态。
	 *
	 * @return 独立外观快照。
	 */
	刷新外观 copy() {
		try {
			刷新外观 result = (刷新外观) clone();
			result.colors = colors.clone();
			result.themeColors = themeColors.clone();
			result.points = points.clone();
			return result;
		} catch (CloneNotSupportedException exception) {
			throw new AssertionError(exception);
		}
	}

	/**
	 * 设置完整头尾主题颜色序列；游戏等样式可使用多于两个颜色。
	 *
	 * @param values ARGB 数组，空数组恢复主颜色及强调颜色。
	 * @throws IllegalArgumentException 输入为空、非有限值或超出本选项的允许范围。
	 */
	@SimpleFunction
	public void 置主题颜色(int[] values) {
		if (values == null || values.length > 32) {
			throw new IllegalArgumentException("主题颜色必须为不超过 32 项的数组。");
		}
		themeColors = values.clone();
	}

	/**
	 * 返回完整主题颜色序列副本。
	 *
	 * @return 独立颜色数组。
	 */
	@SimpleFunction
	public int[] 取主题颜色() {
		return themeColors.clone();
	}
}
