package simple.library.wsd.fastjson2;

import com.alibaba.fastjson2.SymbolTable;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * JSONB 外部符号表。名称去重后按文本自然顺序排列，最多 32768 项；编码与解码必须使用内容相同的表，创建后不可修改。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class FJ2符号表 {

	/**
	 * 创建后保持不变的外部符号表。
	 */
	final SymbolTable table;

	/**
	 * 保存不可变符号表。
	 *
	 * @param table 上游符号表。
	 */
	private FJ2符号表(SymbolTable table) {
		this.table = table;
	}

	/**
	 * 复制名称集并创建外部符号表。
	 *
	 * @param names 不含空引用的属性名数组，去重后最多 32768 项。
	 * @return 不可修改的外部符号表。
	 * @throws IllegalArgumentException 名称数组为空引用、含有空引用，或去重后超过 32768 项。
	 */
	@SimpleFunction
	public static FJ2符号表 创建(String[] names) {
		try {
			if (names == null) {
				throw new IllegalArgumentException("名称集不得为空。");
			}
			String[] copy = names.clone();
			for (String name : copy) {
				FJ2支持.text(name);
			}
			SymbolTable table = new SymbolTable(copy);
			if (table.size() > 32768) {
				throw new IllegalArgumentException("符号表去重后不能超过 32768 项。");
			}
			return new FJ2符号表(table);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回符号数量。
	 *
	 * @return 符号表中的名称数量。
	 */
	@SimpleFunction
	public int 取数量() {
		try {
			return table.size();
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回符号名称；名称去重后按文本自然顺序排列。
	 *
	 * @param ordinal 从 1 开始的符号序号。
	 * @return 指定序号对应的属性名称。
	 * @throws IllegalArgumentException 序号不在符号表范围内。
	 */
	@SimpleFunction
	public String 取名称(int ordinal) {
		try {
			checkOrdinal(ordinal);
			return table.getName(ordinal);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回名称对应的序号；不存在返回 -1。
	 *
	 * @param name 符号名称。
	 * @return 从 1 开始的符号序号，不存在时为 -1。
	 * @throws IllegalArgumentException 名称为空引用。
	 */
	@SimpleFunction
	public int 取序号(String name) {
		try {
			return table.getOrdinal(FJ2支持.text(name));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回整个符号表的 64 位哈希，与名称输入顺序及重复项无关。
	 *
	 * @return 当前表的结构哈希。
	 */
	@SimpleFunction
	public long 取表哈希() {
		return table.hashCode64();
	}

	/**
	 * 返回指定符号名称的 64 位哈希。
	 *
	 * @param ordinal 从 1 开始的符号序号。
	 * @return 该名称的上游哈希。
	 * @throws IllegalArgumentException 序号不在符号表范围内。
	 */
	@SimpleFunction
	public long 取名称哈希(int ordinal) {
		checkOrdinal(ordinal);
		return table.getHashCode(ordinal);
	}

	/**
	 * 根据名称哈希查询符号名称。
	 *
	 * @param hash 名称的 64 位哈希。
	 * @return 对应名称；未找到时返回空文本，可通过按哈希取序号区分空名称与未找到。
	 */
	@SimpleFunction
	public String 按哈希取名称(long hash) {
		String result = table.getNameByHashCode(hash);
		return result == null ? "" : result;
	}

	/**
	 * 根据名称哈希查询符号序号。
	 *
	 * @param hash 名称的 64 位哈希。
	 * @return 从 1 开始的序号，未找到时为 -1。
	 */
	@SimpleFunction
	public int 按哈希取序号(long hash) {
		return table.getOrdinalByHashCode(hash);
	}

	/**
	 * 检查外部符号序号，避免直接暴露上游的数组越界异常。
	 *
	 * @param ordinal 从 1 开始的序号。
	 */
	private void checkOrdinal(int ordinal) {
		if (ordinal < 1 || ordinal > table.size()) {
			throw new IllegalArgumentException("符号序号越界。");
		}
	}
}
