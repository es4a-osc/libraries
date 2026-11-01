package simple.library.wsd.calendarview;

import simple.runtime.annotations.*;
import simple.runtime.variants.*;
import simple.library.wsd.calendarview.vendor.Calendar;

/**
 * 一项日历标记，提交时复制值属性；附加数据中的对象保留引用语义，不随进程重建恢复。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 日历标记 {
	private int type;
	private int color = 0xffed5353;
	private String text = "记";
	private String extra = "";
	private Variant payload = UninitializedVariant.getUninitializedVariant();

	/**
	 * 创建具有默认颜色和文字的标记。
	 */
	public 日历标记() {
	}

	/**
	 * 读取由应用定义的标记类型编号。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public int 类型() {
		return type;
	}

	/**
	 * 设置由应用定义的标记类型编号。
	 *
	 * @param value 新的标记值。
	 */
	@SimpleProperty
	public void 类型(int value) {
		type = value;
	}

	/**
	 * 读取标记的 ARGB 颜色。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public int 颜色() {
		return color;
	}

	/**
	 * 设置标记的 ARGB 颜色。
	 *
	 * @param value 新的标记值。
	 */
	@SimpleProperty
	public void 颜色(int value) {
		color = value;
	}

	/**
	 * 读取标记文字。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public String 文字() {
		return text;
	}

	/**
	 * 设置标记文字。
	 *
	 * @param value 新的标记值。
	 */
	@SimpleProperty
	public void 文字(String value) {
		if (value == null) {
			throw new IllegalArgumentException("标记值不能是空引用");
		}
		text = value;
	}

	/**
	 * 读取附加说明文本。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public String 附加文本() {
		return extra;
	}

	/**
	 * 设置附加说明文本。
	 *
	 * @param value 新的标记值。
	 */
	@SimpleProperty
	public void 附加文本(String value) {
		if (value == null) {
			throw new IllegalArgumentException("标记值不能是空引用");
		}
		extra = value;
	}

	/**
	 * 读取应用附加数据；其中的对象保持引用语义。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public Variant 附加数据() {
		return payload;
	}

	/**
	 * 设置应用附加数据；其中的对象保持引用语义。
	 *
	 * @param value 新的标记值。
	 */
	@SimpleProperty
	public void 附加数据(Variant value) {
		if (value == null) {
			throw new IllegalArgumentException("标记值不能是空引用");
		}
		payload = value;
	}

	/**
	 * 转换成独立引擎标记。
	 *
	 * @return 原生标记副本。
	 */
	public Calendar.Scheme nativeValue() {
		Calendar.Scheme value = new Calendar.Scheme(type, color, text, extra);
		value.setObj(new Payload(payload));
		return value;
	}

	/**
	 * 复制引擎标记属性。
	 *
	 * @param source 原生标记。
	 * @return 本库的标记副本。
	 */
	public static 日历标记 from(Calendar.Scheme source) {
		日历标记 value = new 日历标记();
		value.type = source.getType();
		value.color = source.getShcemeColor();
		value.text = source.getScheme() == null ? "" : source.getScheme();
		value.extra = source.getOther() == null ? "" : source.getOther();
		if (source.getObj() instanceof Payload && ((Payload) source.getObj()).value != null) {
			value.payload = ((Payload) source.getObj()).value;
		}
		return value;
	}

	/**
	 * 引擎状态可序列化，附加对象只在本进程内保留引用。
	 *
	 * @author 树先生 xhwsd@qq.com
	 */
	private static final class Payload implements java.io.Serializable {
		private static final long serialVersionUID = 1L;
		private final transient Variant value;

		/**
		 * 保存进程内引用，不把应用对象写进 Android 状态包。
		 *
		 * @param payload 附加数据。
		 */
		Payload(Variant payload) {
			value = payload;
		}
	}
}
