package simple.library.wsd.calendarview;

import simple.runtime.annotations.*;

/**
 * 一次性应用的日历配置；应用时复制，后续修改不影响现有日历。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 日历配置 implements Cloneable {
	private int selectMode = 0;
	private int weekStart = 2;
	private int monthMode = 0;
	private int autoSelect = 0;
	private int multiMax = 5;
	private int rangeMin = 1;
	private int rangeMax = -1;
	private int itemHeight = 56;
	private int weekHeight = 36;
	private float daySize = 16f;
	private float lunarSize = 10f;
	private float weekSize = 13f;
	private int leftPadding = 0;
	private int rightPadding = 0;
	private int lineMargin = 0;
	private int style = 0;
	private boolean showLunar = true;
	private boolean customDraw = false;
	private boolean preventLongSelect = false;
	private boolean monthScroll = true;
	private boolean weekScroll = true;
	private boolean yearScroll = true;
	private float yearMonthSize = 16f;
	private float yearDaySize = 10f;
	private float yearWeekSize = 9f;
	private int yearMonthHeight = 40;
	private int yearWeekHeight = 24;
	private int yearLeft = 0;
	private int yearRight = 0;
	private int yearMonthLeft = 4;
	private int yearMonthRight = 4;
	private int yearMonthTop = 2;
	private int yearMonthBottom = 2;
	private int todayText = 0xffed5353;
	private int todayLunar = 0xffed5353;
	private int monthText = 0xff263238;
	private int otherText = 0xffb0bec5;
	private int monthLunar = 0xff78909c;
	private int otherLunar = 0xffcfd8dc;
	private int selected = 0xff167d79;
	private int selectedText = 0xffffffff;
	private int selectedLunar = 0xffffffff;
	private int scheme = 0xffed5353;
	private int schemeText = 0xff263238;
	private int schemeLunar = 0xff78909c;
	private int weekBackground = 0xffffffff;
	private int weekText = 0xff455a64;
	private int lineColor = 0xffeceff1;
	private int yearBackground = 0xffffffff;
	private int yearMonthColor = 0xff263238;
	private int yearDayColor = 0xff455a64;
	private int yearSchemeColor = 0xffed5353;
	private int yearTodayColor = 0xffed5353;
	private int yearSelectedColor = 0xff167d79;
	private int yearWeekColor = 0xff78909c;
	private boolean fillHeight;
	private String defaultScheme = "记";
	private String[] weekTitles = {"日", "一", "二", "三", "四", "五", "六"};

	/**
	 * 创建具有声明默认值的独立配置。
	 */
	public 日历配置() {
	}

	/**
	 * 读取选择模式：0 默认自动选择，1 单选，2 范围，3 多选。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 选择模式() {
		return selectMode;
	}

	/**
	 * 设置选择模式：0 默认自动选择，1 单选，2 范围，3 多选。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0"
	)
	public void 选择模式(int value) {
		if (value < 0 || value > 3) {
			throw new IllegalArgumentException("选择模式无效");
		}
		selectMode = value;
	}

	/**
	 * 读取每周起始：1 周日，2 周一，7 周六。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 周起始() {
		return weekStart;
	}

	/**
	 * 设置每周起始：1 周日，2 周一，7 周六。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "2"
	)
	public void 周起始(int value) {
		if (value != 1 && value != 2 && value != 7) {
			throw new IllegalArgumentException("周起始无效");
		}
		weekStart = value;
	}

	/**
	 * 读取月份填充：0 固定六行，1 只显示本月，2 按实际周数填充。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 月份填充() {
		return monthMode;
	}

	/**
	 * 设置月份填充：0 固定六行，1 只显示本月，2 按实际周数填充。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0"
	)
	public void 月份填充(int value) {
		if (value < 0 || value > 2) {
			throw new IllegalArgumentException("月份填充无效");
		}
		monthMode = value;
	}

	/**
	 * 读取默认模式翻页时：0 首日，1 跟随上次日期并优先今天，2 只跟随上次日期。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 自动选择日() {
		return autoSelect;
	}

	/**
	 * 设置默认模式翻页时：0 首日，1 跟随上次日期并优先今天，2 只跟随上次日期。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0"
	)
	public void 自动选择日(int value) {
		if (value < 0 || value > 2) {
			throw new IllegalArgumentException("自动选择日无效");
		}
		autoSelect = value;
	}

	/**
	 * 读取多选数量上限，范围 1 至 10000。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 最大多选数() {
		return multiMax;
	}

	/**
	 * 设置多选数量上限，范围 1 至 10000。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "5"
	)
	public void 最大多选数(int value) {
		if (value < 1 || value > 10000) {
			throw new IllegalArgumentException("最大多选数无效");
		}
		multiMax = value;
	}

	/**
	 * 读取范围选择包含首尾的最小天数。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 最小选择跨度() {
		return rangeMin;
	}

	/**
	 * 设置范围选择包含首尾的最小天数。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "1"
	)
	public void 最小选择跨度(int value) {
		if (value < 1 || value > 73049) {
			throw new IllegalArgumentException("最小选择跨度无效");
		}
		rangeMin = value;
	}

	/**
	 * 读取范围选择包含首尾的最大天数，-1 不限制。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 最大选择跨度() {
		return rangeMax;
	}

	/**
	 * 设置范围选择包含首尾的最大天数，-1 不限制。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "-1"
	)
	public void 最大选择跨度(int value) {
		if (value != -1 && (value < 1 || value > 73049)) {
			throw new IllegalArgumentException("最大选择跨度无效");
		}
		rangeMax = value;
	}

	/**
	 * 读取日项高度，单位 DIP，范围 16 至 512。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 日项高度() {
		return itemHeight;
	}

	/**
	 * 设置日项高度，单位 DIP，范围 16 至 512。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "56"
	)
	public void 日项高度(int value) {
		if (value < 16 || value > 512) {
			throw new IllegalArgumentException("日项高度无效");
		}
		itemHeight = value;
	}

	/**
	 * 读取星期栏高度，单位 DIP；0 隐藏星期栏。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 周栏高度() {
		return weekHeight;
	}

	/**
	 * 设置星期栏高度，单位 DIP；0 隐藏星期栏。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "36"
	)
	public void 周栏高度(int value) {
		if (value < 0 || value > 256) {
			throw new IllegalArgumentException("周栏高度无效");
		}
		weekHeight = value;
	}

	/**
	 * 读取日期文字大小，单位 SP。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public float 日期字号() {
		return daySize;
	}

	/**
	 * 设置日期文字大小，单位 SP。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "16"
	)
	public void 日期字号(float value) {
		if (!Float.isFinite(value) || value <= 0 || value > 96) {
			throw new IllegalArgumentException("日期字号无效");
		}
		daySize = value;
	}

	/**
	 * 读取农历文字大小，单位 SP。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public float 农历字号() {
		return lunarSize;
	}

	/**
	 * 设置农历文字大小，单位 SP。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "10"
	)
	public void 农历字号(float value) {
		if (!Float.isFinite(value) || value <= 0 || value > 96) {
			throw new IllegalArgumentException("农历字号无效");
		}
		lunarSize = value;
	}

	/**
	 * 读取星期栏文字大小，单位 SP。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public float 周栏字号() {
		return weekSize;
	}

	/**
	 * 设置星期栏文字大小，单位 SP。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "13"
	)
	public void 周栏字号(float value) {
		if (!Float.isFinite(value) || value <= 0 || value > 96) {
			throw new IllegalArgumentException("周栏字号无效");
		}
		weekSize = value;
	}

	/**
	 * 读取日历左内边距，单位 DIP。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 左内边距() {
		return leftPadding;
	}

	/**
	 * 设置日历左内边距，单位 DIP。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0"
	)
	public void 左内边距(int value) {
		if (value < 0 || value > 256) {
			throw new IllegalArgumentException("左内边距无效");
		}
		leftPadding = value;
	}

	/**
	 * 读取日历右内边距，单位 DIP。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 右内边距() {
		return rightPadding;
	}

	/**
	 * 设置日历右内边距，单位 DIP。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0"
	)
	public void 右内边距(int value) {
		if (value < 0 || value > 256) {
			throw new IllegalArgumentException("右内边距无效");
		}
		rightPadding = value;
	}

	/**
	 * 读取星期栏分隔线的左右边距，单位 DIP。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 分隔线边距() {
		return lineMargin;
	}

	/**
	 * 设置星期栏分隔线的左右边距，单位 DIP。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0"
	)
	public void 分隔线边距(int value) {
		if (value < 0 || value > 256) {
			throw new IllegalArgumentException("分隔线边距无效");
		}
		lineMargin = value;
	}

	/**
	 * 读取内置样式：0 圆形，1 方形，2 圆角，3 进度圆环，4 多点标记；范围模式绘制连续选区。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 内置样式() {
		return style;
	}

	/**
	 * 设置内置样式：0 圆形，1 方形，2 圆角，3 进度圆环，4 多点标记；范围模式绘制连续选区。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0"
	)
	public void 内置样式(int value) {
		if (value < 0 || value > 4) {
			throw new IllegalArgumentException("内置样式无效");
		}
		style = value;
	}

	/**
	 * 读取是否绘制农历、节气及节日文字。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 显示农历() {
		return showLunar;
	}

	/**
	 * 设置是否绘制农历、节气及节日文字。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	public void 显示农历(boolean value) {
		showLunar = value;
	}

	/**
	 * 读取是否同步触发 Simple 绘制事件；默认关闭以减少逐单元事件开销。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 启用绘制事件() {
		return customDraw;
	}

	/**
	 * 设置是否同步触发 Simple 绘制事件；默认关闭以减少逐单元事件开销。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	public void 启用绘制事件(boolean value) {
		customDraw = value;
	}

	/**
	 * 读取长按时是否仅通知长按而不改变所选日期。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 长按阻止选择() {
		return preventLongSelect;
	}

	/**
	 * 设置长按时是否仅通知长按而不改变所选日期。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	public void 长按阻止选择(boolean value) {
		preventLongSelect = value;
	}

	/**
	 * 读取是否允许手势翻动月视图。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 月视图可滑动() {
		return monthScroll;
	}

	/**
	 * 设置是否允许手势翻动月视图。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	public void 月视图可滑动(boolean value) {
		monthScroll = value;
	}

	/**
	 * 读取是否允许手势翻动周视图。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 周视图可滑动() {
		return weekScroll;
	}

	/**
	 * 设置是否允许手势翻动周视图。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	public void 周视图可滑动(boolean value) {
		weekScroll = value;
	}

	/**
	 * 读取是否允许手势翻动年视图。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public boolean 年视图可滑动() {
		return yearScroll;
	}

	/**
	 * 设置是否允许手势翻动年视图。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "True"
	)
	public void 年视图可滑动(boolean value) {
		yearScroll = value;
	}

	/**
	 * 读取年视图月份标题字号，单位 SP。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public float 年月份字号() {
		return yearMonthSize;
	}

	/**
	 * 设置年视图月份标题字号，单位 SP。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "16"
	)
	public void 年月份字号(float value) {
		if (!Float.isFinite(value) || value <= 0 || value > 96) {
			throw new IllegalArgumentException("年月份字号无效");
		}
		yearMonthSize = value;
	}

	/**
	 * 读取年视图日期字号，单位 SP。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public float 年日期字号() {
		return yearDaySize;
	}

	/**
	 * 设置年视图日期字号，单位 SP。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "10"
	)
	public void 年日期字号(float value) {
		if (!Float.isFinite(value) || value <= 0 || value > 96) {
			throw new IllegalArgumentException("年日期字号无效");
		}
		yearDaySize = value;
	}

	/**
	 * 读取年视图星期字号，单位 SP。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public float 年星期字号() {
		return yearWeekSize;
	}

	/**
	 * 设置年视图星期字号，单位 SP。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_SINGLE,
		initializer = "9"
	)
	public void 年星期字号(float value) {
		if (!Float.isFinite(value) || value <= 0 || value > 96) {
			throw new IllegalArgumentException("年星期字号无效");
		}
		yearWeekSize = value;
	}

	/**
	 * 读取年视图月份标题高度，单位 DIP。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 年月份标题高度() {
		return yearMonthHeight;
	}

	/**
	 * 设置年视图月份标题高度，单位 DIP。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "40"
	)
	public void 年月份标题高度(int value) {
		if (value < 8 || value > 256) {
			throw new IllegalArgumentException("年月份标题高度无效");
		}
		yearMonthHeight = value;
	}

	/**
	 * 读取年视图星期栏高度，单位 DIP。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 年星期高度() {
		return yearWeekHeight;
	}

	/**
	 * 设置年视图星期栏高度，单位 DIP。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "24"
	)
	public void 年星期高度(int value) {
		if (value < 0 || value > 256) {
			throw new IllegalArgumentException("年星期高度无效");
		}
		yearWeekHeight = value;
	}

	/**
	 * 读取年视图左内边距，单位 DIP。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 年左内边距() {
		return yearLeft;
	}

	/**
	 * 设置年视图左内边距，单位 DIP。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0"
	)
	public void 年左内边距(int value) {
		if (value < 0 || value > 256) {
			throw new IllegalArgumentException("年左内边距无效");
		}
		yearLeft = value;
	}

	/**
	 * 读取年视图右内边距，单位 DIP。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 年右内边距() {
		return yearRight;
	}

	/**
	 * 设置年视图右内边距，单位 DIP。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0"
	)
	public void 年右内边距(int value) {
		if (value < 0 || value > 256) {
			throw new IllegalArgumentException("年右内边距无效");
		}
		yearRight = value;
	}

	/**
	 * 读取年视图单月左边距，单位 DIP。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 年月份左边距() {
		return yearMonthLeft;
	}

	/**
	 * 设置年视图单月左边距，单位 DIP。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "4"
	)
	public void 年月份左边距(int value) {
		if (value < 0 || value > 256) {
			throw new IllegalArgumentException("年月份左边距无效");
		}
		yearMonthLeft = value;
	}

	/**
	 * 读取年视图单月右边距，单位 DIP。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 年月份右边距() {
		return yearMonthRight;
	}

	/**
	 * 设置年视图单月右边距，单位 DIP。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "4"
	)
	public void 年月份右边距(int value) {
		if (value < 0 || value > 256) {
			throw new IllegalArgumentException("年月份右边距无效");
		}
		yearMonthRight = value;
	}

	/**
	 * 读取年视图单月上边距，单位 DIP。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 年月份上边距() {
		return yearMonthTop;
	}

	/**
	 * 设置年视图单月上边距，单位 DIP。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "2"
	)
	public void 年月份上边距(int value) {
		if (value < 0 || value > 256) {
			throw new IllegalArgumentException("年月份上边距无效");
		}
		yearMonthTop = value;
	}

	/**
	 * 读取年视图单月下边距，单位 DIP。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 年月份下边距() {
		return yearMonthBottom;
	}

	/**
	 * 设置年视图单月下边距，单位 DIP。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "2"
	)
	public void 年月份下边距(int value) {
		if (value < 0 || value > 256) {
			throw new IllegalArgumentException("年月份下边距无效");
		}
		yearMonthBottom = value;
	}

	/**
	 * 读取今日文字颜色，ARGB 整数。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 今日文字颜色() {
		return todayText;
	}

	/**
	 * 设置今日文字颜色，ARGB 整数。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "-1223853"
	)
	public void 今日文字颜色(int value) {
		todayText = value;
	}

	/**
	 * 读取今日农历颜色，ARGB 整数。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 今日农历颜色() {
		return todayLunar;
	}

	/**
	 * 设置今日农历颜色，ARGB 整数。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "-1223853"
	)
	public void 今日农历颜色(int value) {
		todayLunar = value;
	}

	/**
	 * 读取本月文字颜色，ARGB 整数。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 本月文字颜色() {
		return monthText;
	}

	/**
	 * 设置本月文字颜色，ARGB 整数。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "-14273992"
	)
	public void 本月文字颜色(int value) {
		monthText = value;
	}

	/**
	 * 读取其他月文字颜色，ARGB 整数。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 其他月文字颜色() {
		return otherText;
	}

	/**
	 * 设置其他月文字颜色，ARGB 整数。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "-5194043"
	)
	public void 其他月文字颜色(int value) {
		otherText = value;
	}

	/**
	 * 读取本月农历颜色，ARGB 整数。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 本月农历颜色() {
		return monthLunar;
	}

	/**
	 * 设置本月农历颜色，ARGB 整数。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "-8875876"
	)
	public void 本月农历颜色(int value) {
		monthLunar = value;
	}

	/**
	 * 读取其他月农历颜色，ARGB 整数。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 其他月农历颜色() {
		return otherLunar;
	}

	/**
	 * 设置其他月农历颜色，ARGB 整数。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "-3155748"
	)
	public void 其他月农历颜色(int value) {
		otherLunar = value;
	}

	/**
	 * 读取选中背景颜色，ARGB 整数。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 选中背景颜色() {
		return selected;
	}

	/**
	 * 设置选中背景颜色，ARGB 整数。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "-15303303"
	)
	public void 选中背景颜色(int value) {
		selected = value;
	}

	/**
	 * 读取选中文字颜色，ARGB 整数。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 选中文字颜色() {
		return selectedText;
	}

	/**
	 * 设置选中文字颜色，ARGB 整数。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0xfffffff"
	)
	public void 选中文字颜色(int value) {
		selectedText = value;
	}

	/**
	 * 读取选中农历颜色，ARGB 整数。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 选中农历颜色() {
		return selectedLunar;
	}

	/**
	 * 设置选中农历颜色，ARGB 整数。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0xfffffff"
	)
	public void 选中农历颜色(int value) {
		selectedLunar = value;
	}

	/**
	 * 读取标记颜色，ARGB 整数。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 标记颜色() {
		return scheme;
	}

	/**
	 * 设置标记颜色，ARGB 整数。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "-1223853"
	)
	public void 标记颜色(int value) {
		scheme = value;
	}

	/**
	 * 读取标记文字颜色，ARGB 整数。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 标记文字颜色() {
		return schemeText;
	}

	/**
	 * 设置标记文字颜色，ARGB 整数。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "-14273992"
	)
	public void 标记文字颜色(int value) {
		schemeText = value;
	}

	/**
	 * 读取标记农历颜色，ARGB 整数。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 标记农历颜色() {
		return schemeLunar;
	}

	/**
	 * 设置标记农历颜色，ARGB 整数。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "-8875876"
	)
	public void 标记农历颜色(int value) {
		schemeLunar = value;
	}

	/**
	 * 读取周栏背景颜色，ARGB 整数。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 周栏背景颜色() {
		return weekBackground;
	}

	/**
	 * 设置周栏背景颜色，ARGB 整数。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0xfffffff"
	)
	public void 周栏背景颜色(int value) {
		weekBackground = value;
	}

	/**
	 * 读取周栏文字颜色，ARGB 整数。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 周栏文字颜色() {
		return weekText;
	}

	/**
	 * 设置周栏文字颜色，ARGB 整数。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "-12232092"
	)
	public void 周栏文字颜色(int value) {
		weekText = value;
	}

	/**
	 * 读取分隔线颜色，ARGB 整数。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 分隔线颜色() {
		return lineColor;
	}

	/**
	 * 设置分隔线颜色，ARGB 整数。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "-1249295"
	)
	public void 分隔线颜色(int value) {
		lineColor = value;
	}

	/**
	 * 读取年背景颜色，ARGB 整数。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 年背景颜色() {
		return yearBackground;
	}

	/**
	 * 设置年背景颜色，ARGB 整数。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "0xfffffff"
	)
	public void 年背景颜色(int value) {
		yearBackground = value;
	}

	/**
	 * 读取年月份颜色，ARGB 整数。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 年月份颜色() {
		return yearMonthColor;
	}

	/**
	 * 设置年月份颜色，ARGB 整数。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "-14273992"
	)
	public void 年月份颜色(int value) {
		yearMonthColor = value;
	}

	/**
	 * 读取年日期颜色，ARGB 整数。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 年日期颜色() {
		return yearDayColor;
	}

	/**
	 * 设置年日期颜色，ARGB 整数。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "-12232092"
	)
	public void 年日期颜色(int value) {
		yearDayColor = value;
	}

	/**
	 * 读取年标记颜色，ARGB 整数。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 年标记颜色() {
		return yearSchemeColor;
	}

	/**
	 * 设置年标记颜色，ARGB 整数。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "-1223853"
	)
	public void 年标记颜色(int value) {
		yearSchemeColor = value;
	}

	/**
	 * 读取年今日颜色，ARGB 整数。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 年今日颜色() {
		return yearTodayColor;
	}

	/**
	 * 设置年今日颜色，ARGB 整数。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "-1223853"
	)
	public void 年今日颜色(int value) {
		yearTodayColor = value;
	}

	/**
	 * 读取年选中颜色，ARGB 整数。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 年选中颜色() {
		return yearSelectedColor;
	}

	/**
	 * 设置年选中颜色，ARGB 整数。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "-15303303"
	)
	public void 年选中颜色(int value) {
		yearSelectedColor = value;
	}

	/**
	 * 读取年星期颜色，ARGB 整数。
	 *
	 * @return 当前配置值。
	 */
	@SimpleProperty
	public int 年星期颜色() {
		return yearWeekColor;
	}

	/**
	 * 设置年星期颜色，ARGB 整数。
	 *
	 * @param value 新的配置值。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_INTEGER,
		initializer = "-8875876"
	)
	public void 年星期颜色(int value) {
		yearWeekColor = value;
	}

	/**
	 * 设置从周日到周六的七个星期文字；输入数组会复制。
	 *
	 * @param values 星期文字。
	 */
	@SimpleFunction
	public void 置星期文字(String[] values) {
		if (values == null || values.length != 7) {
			throw new IllegalArgumentException("必须提供从周日到周六的七项文字");
		}
		for (String value : values) {
			if (value == null || value.length() > 32) {
				throw new IllegalArgumentException("星期文字为空引用或过长");
			}
		}
		weekTitles = values.clone();
	}

	/**
	 * 取得从周日到周六排列的独立文字数组。
	 *
	 * @return 操作结果。
	 */
	@SimpleFunction
	public String[] 取星期文字() {
		return weekTitles.clone();
	}

	/**
	 * 复制全部值及数组。
	 *
	 * @return 可独立修改的配置副本。
	 */
	public 日历配置 copy() {
		try {
			日历配置 value = (日历配置) clone();
			value.weekTitles = weekTitles.clone();
			return value;
		} catch (CloneNotSupportedException error) {
			throw new AssertionError(error);
		}
	}

	/**
	 * 读取是否让月视图填满可用高度。
	 *
	 * @return 当前设置。
	 */
	@SimpleProperty
	public boolean 填满高度() {
		return fillHeight;
	}

	/**
	 * 设置是否让月视图填满可用高度，只支持独立月日历，不能配合收缩布局。
	 *
	 * @param value 是否填满。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_BOOLEAN,
		initializer = "False"
	)
	public void 填满高度(boolean value) {
		fillHeight = value;
	}

	/**
	 * 读取没有指定文字时的默认标记文字。
	 *
	 * @return 默认标记文字。
	 */
	@SimpleProperty
	public String 默认标记文字() {
		return defaultScheme;
	}

	/**
	 * 设置没有指定文字时的默认标记文字。
	 *
	 * @param value 新的默认文字。
	 */
	@SimpleProperty(
		type = SimpleProperty.PROPERTY_TYPE_STRING,
		initializer = "\"记\""
	)
	public void 默认标记文字(String value) {
		if (value == null || value.length() > 32) {
			throw new IllegalArgumentException("默认标记文字为空或过长");
		}
		defaultScheme = value;
	}

}
