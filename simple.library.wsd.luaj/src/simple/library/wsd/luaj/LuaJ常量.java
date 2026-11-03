package simple.library.wsd.luaj;

import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleObject;

/**
 * Lua 5.1 类型编号；整数仍属于数值类型。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class LuaJ常量 {
	/**
	 * 常量容器不可创建实例。
	 */
	private LuaJ常量() {}

	/**
	 * 空值类型编号。
	 */
	@SimpleDataElement public static final int 类型_空值 = 0;

	/**
	 * 逻辑类型编号。
	 */
	@SimpleDataElement public static final int 类型_逻辑 = 1;

	/**
	 * 数值类型编号。
	 */
	@SimpleDataElement public static final int 类型_数值 = 3;

	/**
	 * 文本类型编号。
	 */
	@SimpleDataElement public static final int 类型_文本 = 4;

	/**
	 * 表类型编号。
	 */
	@SimpleDataElement public static final int 类型_表 = 5;

	/**
	 * 函数类型编号。
	 */
	@SimpleDataElement public static final int 类型_函数 = 6;

	/**
	 * 用户数据类型编号。
	 */
	@SimpleDataElement public static final int 类型_用户数据 = 7;

	/**
	 * 协程类型编号。
	 */
	@SimpleDataElement public static final int 类型_协程 = 8;
}
