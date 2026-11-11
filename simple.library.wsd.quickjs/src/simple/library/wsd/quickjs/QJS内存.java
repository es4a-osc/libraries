package simple.library.wsd.quickjs;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 原生运行时内存统计快照，不随后续引擎操作改变；不包含 Java 内存。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class QJS内存 {

	private final long[] values;

	/**
	 * 复制原生统计。
	 *
	 * @param values 原生统计字段。
	 */
	QJS内存(long[] values) {
		this.values = values.clone();
	}

	/**
	 * 取得分配字节，按原生引擎统计。
	 *
	 * @return 分配字节，单位为字节。
	 */
	@SimpleFunction
	public long 取分配字节() {
		return values[0];
	}

	/**
	 * 取得分配上限，按原生引擎统计。
	 *
	 * @return 分配上限，单位为字节。
	 */
	@SimpleFunction
	public long 取分配上限() {
		return values[1];
	}

	/**
	 * 取得使用字节，按原生引擎统计。
	 *
	 * @return 使用字节，单位为字节。
	 */
	@SimpleFunction
	public long 取使用字节() {
		return values[2];
	}

	/**
	 * 取得分配块数，按原生引擎统计。
	 *
	 * @return 分配块数。
	 */
	@SimpleFunction
	public long 取分配块数() {
		return values[3];
	}

	/**
	 * 取得使用块数，按原生引擎统计。
	 *
	 * @return 使用块数。
	 */
	@SimpleFunction
	public long 取使用块数() {
		return values[4];
	}

	/**
	 * 取得原子数量，按原生引擎统计。
	 *
	 * @return 原子数量。
	 */
	@SimpleFunction
	public long 取原子数量() {
		return values[5];
	}

	/**
	 * 取得原子字节，按原生引擎统计。
	 *
	 * @return 原子字节，单位为字节。
	 */
	@SimpleFunction
	public long 取原子字节() {
		return values[6];
	}

	/**
	 * 取得字符串数量，按原生引擎统计。
	 *
	 * @return 字符串数量。
	 */
	@SimpleFunction
	public long 取字符串数量() {
		return values[7];
	}

	/**
	 * 取得字符串字节，按原生引擎统计。
	 *
	 * @return 字符串字节，单位为字节。
	 */
	@SimpleFunction
	public long 取字符串字节() {
		return values[8];
	}

	/**
	 * 取得对象数量，按原生引擎统计。
	 *
	 * @return 对象数量。
	 */
	@SimpleFunction
	public long 取对象数量() {
		return values[9];
	}

	/**
	 * 取得对象字节，按原生引擎统计。
	 *
	 * @return 对象字节，单位为字节。
	 */
	@SimpleFunction
	public long 取对象字节() {
		return values[10];
	}

	/**
	 * 取得属性数量，按原生引擎统计。
	 *
	 * @return 属性数量。
	 */
	@SimpleFunction
	public long 取属性数量() {
		return values[11];
	}

	/**
	 * 取得属性字节，按原生引擎统计。
	 *
	 * @return 属性字节，单位为字节。
	 */
	@SimpleFunction
	public long 取属性字节() {
		return values[12];
	}

	/**
	 * 取得形状数量，按原生引擎统计。
	 *
	 * @return 形状数量。
	 */
	@SimpleFunction
	public long 取形状数量() {
		return values[13];
	}

	/**
	 * 取得形状字节，按原生引擎统计。
	 *
	 * @return 形状字节，单位为字节。
	 */
	@SimpleFunction
	public long 取形状字节() {
		return values[14];
	}

	/**
	 * 取得JS函数数量，按原生引擎统计。
	 *
	 * @return JS函数数量。
	 */
	@SimpleFunction
	public long 取JS函数数量() {
		return values[15];
	}

	/**
	 * 取得JS函数字节，按原生引擎统计。
	 *
	 * @return JS函数字节，单位为字节。
	 */
	@SimpleFunction
	public long 取JS函数字节() {
		return values[16];
	}

	/**
	 * 取得JS字节码字节，按原生引擎统计。
	 *
	 * @return JS字节码字节，单位为字节。
	 */
	@SimpleFunction
	public long 取JS字节码字节() {
		return values[17];
	}

	/**
	 * 取得行号表数量，按原生引擎统计。
	 *
	 * @return 行号表数量。
	 */
	@SimpleFunction
	public long 取行号表数量() {
		return values[18];
	}

	/**
	 * 取得行号表字节，按原生引擎统计。
	 *
	 * @return 行号表字节，单位为字节。
	 */
	@SimpleFunction
	public long 取行号表字节() {
		return values[19];
	}

	/**
	 * 取得C函数数量，按原生引擎统计。
	 *
	 * @return C函数数量。
	 */
	@SimpleFunction
	public long 取C函数数量() {
		return values[20];
	}

	/**
	 * 取得数组数量，按原生引擎统计。
	 *
	 * @return 数组数量。
	 */
	@SimpleFunction
	public long 取数组数量() {
		return values[21];
	}

	/**
	 * 取得快速数组数量，按原生引擎统计。
	 *
	 * @return 快速数组数量。
	 */
	@SimpleFunction
	public long 取快速数组数量() {
		return values[22];
	}

	/**
	 * 取得快速数组元素数，按原生引擎统计。
	 *
	 * @return 快速数组元素数。
	 */
	@SimpleFunction
	public long 取快速数组元素数() {
		return values[23];
	}

	/**
	 * 取得二进制对象数量，按原生引擎统计。
	 *
	 * @return 二进制对象数量。
	 */
	@SimpleFunction
	public long 取二进制对象数量() {
		return values[24];
	}

	/**
	 * 取得二进制对象字节，按原生引擎统计。
	 *
	 * @return 二进制对象字节，单位为字节。
	 */
	@SimpleFunction
	public long 取二进制对象字节() {
		return values[25];
	}
}
