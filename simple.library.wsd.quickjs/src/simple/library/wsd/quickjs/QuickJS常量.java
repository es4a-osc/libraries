package simple.library.wsd.quickjs;

import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleDataElement;

/**
 * QuickJS 值类型、Promise 状态和类型数组编号。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class QuickJS常量 {
	/**
	 * 禁止创建常量容器。
	 */
	private QuickJS常量() {
	}

	/**
	 * undefined 的包装类型编号。
	 */
	@SimpleDataElement public static final int 类型_未定义 = 0;

	/**
	 * null 的包装类型编号。
	 */
	@SimpleDataElement public static final int 类型_空值 = 1;

	/**
	 * boolean 的包装类型编号。
	 */
	@SimpleDataElement public static final int 类型_逻辑 = 2;

	/**
	 * Number 的包装类型编号。
	 */
	@SimpleDataElement public static final int 类型_数值 = 3;

	/**
	 * String 的包装类型编号。
	 */
	@SimpleDataElement public static final int 类型_文本 = 4;

	/**
	 * BigInt 的包装类型编号。
	 */
	@SimpleDataElement public static final int 类型_大整数 = 5;

	/**
	 * Symbol 的包装类型编号。
	 */
	@SimpleDataElement public static final int 类型_符号 = 6;

	/**
	 * Object 的包装类型编号。
	 */
	@SimpleDataElement public static final int 类型_对象 = 7;

	/**
	 * Array 的包装类型编号。
	 */
	@SimpleDataElement public static final int 类型_数组 = 8;

	/**
	 * Function 的包装类型编号。
	 */
	@SimpleDataElement public static final int 类型_函数 = 9;

	/**
	 * Promise 的包装类型编号。
	 */
	@SimpleDataElement public static final int 类型_承诺 = 10;

	/**
	 * ArrayBuffer 或 SharedArrayBuffer 的包装类型编号。
	 */
	@SimpleDataElement public static final int 类型_缓冲区 = 11;

	/**
	 * TypedArray 的包装类型编号。
	 */
	@SimpleDataElement public static final int 类型_类型数组 = 12;

	/**
	 * BigFloat 的包装类型编号。
	 */
	@SimpleDataElement public static final int 类型_扩展大数 = 13;

	/**
	 * BigDecimal 的包装类型编号。
	 */
	@SimpleDataElement public static final int 类型_扩展十进制 = 14;

	/**
	 * 承诺尚未兑现或拒绝。
	 */
	@SimpleDataElement public static final int 承诺_等待 = 0;

	/**
	 * 承诺已成功兑现。
	 */
	@SimpleDataElement public static final int 承诺_兑现 = 1;

	/**
	 * 承诺已被拒绝。
	 */
	@SimpleDataElement public static final int 承诺_拒绝 = 2;

	/**
	 * Int8Array 的类型数组编号。
	 */
	@SimpleDataElement public static final int 数组_Int8 = 0;

	/**
	 * Uint8Array 的类型数组编号。
	 */
	@SimpleDataElement public static final int 数组_Uint8 = 1;

	/**
	 * Uint8ClampedArray 的类型数组编号。
	 */
	@SimpleDataElement public static final int 数组_Uint8Clamped = 2;

	/**
	 * Int16Array 的类型数组编号。
	 */
	@SimpleDataElement public static final int 数组_Int16 = 3;

	/**
	 * Uint16Array 的类型数组编号。
	 */
	@SimpleDataElement public static final int 数组_Uint16 = 4;

	/**
	 * Int32Array 的类型数组编号。
	 */
	@SimpleDataElement public static final int 数组_Int32 = 5;

	/**
	 * Uint32Array 的类型数组编号。
	 */
	@SimpleDataElement public static final int 数组_Uint32 = 6;

	/**
	 * Float32Array 的类型数组编号。
	 */
	@SimpleDataElement public static final int 数组_Float32 = 7;

	/**
	 * Float64Array 的类型数组编号。
	 */
	@SimpleDataElement public static final int 数组_Float64 = 8;

	/**
	 * BigInt64Array 的类型数组编号。
	 */
	@SimpleDataElement public static final int 数组_BigInt64 = 9;

	/**
	 * BigUint64Array 的类型数组编号。
	 */
	@SimpleDataElement public static final int 数组_BigUint64 = 10;
}
