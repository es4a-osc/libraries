package simple.library.wsd.fastjson2;

import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Deque;

import com.alibaba.fastjson2.JSONReader;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 文本或 JSONB 的顺序读取器，自动检查根值、名称和值、分隔符及容器闭合。输入在内存中保存，使用完必须关闭，同一实例串行使用。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class FJ2读取器 {

	/**
	 * 上游读取器；关闭后置为空引用。
	 */
	private JSONReader reader;

	/**
	 * 尚未闭合的手动读取容器，由栈顶表示当前层。
	 */
	private final Deque<FJ2层> levels = new ArrayDeque<FJ2层>();

	/**
	 * 是否已经开始消费根值。
	 */
	private boolean started;

	/**
	 * 打开读取器时捕获的最大嵌套层数。
	 */
	private final int maxLevel;

	/**
	 * 保存独立读取器。
	 *
	 * @param reader 已打开的上游读取器。
	 */
	private FJ2读取器(JSONReader reader) {
		this.reader = reader;
		maxLevel = reader.getContext().getMaxLevel();
	}

	/**
	 * 打开 JSON 文本，逐项读取时检查结构，不预先构建完整值树。
	 *
	 * @param text   非空白 JSON 文本。
	 * @param config 打开时捕获配置。
	 * @return 保存独立读取状态的新读取器。
	 * @throws IllegalArgumentException 输入、字符集或读取配置无效。
	 */
	@SimpleFunction
	public static FJ2读取器 打开文本(String text, FJ2读配置 config) {
		try {
			FJ2支持.nonempty(text);
			return new FJ2读取器(JSONReader.of(text, FJ2支持.readContext(config)));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 复制字节后按字符集打开文本读取器。
	 *
	 * @param bytes   非空 JSON 文本字节。
	 * @param charset 有效字符集名称。
	 * @param config  打开时捕获配置。
	 * @return 保存独立读取状态的新读取器。
	 * @throws IllegalArgumentException 输入、字符集或读取配置无效。
	 */
	@SimpleFunction
	public static FJ2读取器 打开字节(byte[] bytes, String charset, FJ2读配置 config) {
		try {
			byte[] copy = FJ2支持.nonemptyBytes(bytes);
			return new FJ2读取器(JSONReader.of(copy, 0, copy.length, Charset.forName(FJ2支持.text(charset)),
					FJ2支持.readContext(config)));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 复制输入后打开 JSONB 顺序读取器。
	 *
	 * @param bytes   非空 JSONB 数据。
	 * @param config  打开时捕获配置。
	 * @param symbols 编码时的同一外部符号表；未使用则为空。
	 * @return 保存独立读取状态的新读取器。
	 * @throws IllegalArgumentException 输入、字符集或读取配置无效。
	 */
	@SimpleFunction
	public static FJ2读取器 打开JSONB(byte[] bytes, FJ2读配置 config, FJ2符号表 symbols) {
		try {
			return new FJ2读取器(JSONReader.ofJSONB(FJ2支持.nonemptyBytes(bytes), FJ2支持.readContext(config, symbols)));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 判断当前输入是否为 JSONB。
	 *
	 * @return 当前读写格式为 JSONB 时为真。
	 * @throws IllegalArgumentException 读取器已经关闭。
	 */
	@SimpleFunction
	public boolean 是否JSONB() {
		try {
			return check().isJSONB();
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 判断读取器是否已关闭。
	 *
	 * @return 读写器已经关闭时为真。
	 */
	@SimpleFunction
	public boolean 是否关闭() {
		try {
			return reader == null;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回上游当前读取偏移；文本字符集不同，偏移单位遵循上游实现。
	 *
	 * @return 上游读取器的当前偏移量。
	 * @throws IllegalArgumentException 读取器已经关闭。
	 */
	@SimpleFunction
	public int 取位置() {
		try {
			return check().getOffset();
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回当前手动打开的 JSONB 数组长度；文本数组长度未知返回 -1，当前不是数组时报错。
	 *
	 * @return JSONB 数组的声明长度；文本数组为 -1。
	 * @throws IllegalArgumentException 读取器已关闭或当前未手动打开数组。
	 */
	@SimpleFunction
	public int 取当前数组长度() {
		try {
			check();
			if (levels.isEmpty() || levels.peek().kind != 2) {
				throw new IllegalArgumentException("当前不是手动打开的数组。");
			}
			return levels.peek().expected;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 当前位置为对象开始时打开一层并返回真，否则不消费输入。
	 *
	 * @return 已经消费对象开始标记并打开一层时为真。
	 * @throws IllegalArgumentException 读取器已关闭、操作位置错误或输入结构不完整。
	 */
	@SimpleFunction
	public boolean 尝试对象开始() {
		try {
			check();
			if (reader.isEnd() || !reader.isObject()) {
				return false;
			}
			beforeValue();
			if (!reader.nextIfObjectStart()) {
				return false;
			}
			levels.push(new FJ2层(1, -1));
			return true;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 当前位置为数组开始时打开一层并返回真；JSONB 数组长度可随后查询。
	 *
	 * @return 已经消费数组开始标记并打开一层时为真。
	 * @throws IllegalArgumentException 读取器已关闭、操作位置错误或输入结构不完整。
	 */
	@SimpleFunction
	public boolean 尝试数组开始() {
		try {
			check();
			if (reader.isEnd() || !reader.isArray()) {
				return false;
			}
			beforeValue();
			int length = -1;
			if (reader.isJSONB()) {
				length = reader.startArray();
			} else if (!reader.nextIfArrayStart()) {
				return false;
			}
			levels.push(new FJ2层(2, length));
			return true;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 当前对象结束且没有待读取的属性值时关闭一层并返回真，否则返回假。
	 *
	 * @return 已经消费对象结束标记并关闭一层时为真。
	 * @throws IllegalArgumentException 读取器已关闭、操作位置错误或输入结构不完整。
	 */
	@SimpleFunction
	public boolean 尝试对象结束() {
		try {
			check();
			if (levels.isEmpty() || levels.peek().kind != 1 || levels.peek().pending || reader.isEnd()) {
				return false;
			}
			FJ2层 level = levels.peek();
			if (!reader.nextIfObjectEnd()) {
				return false;
			}
			if (!reader.isJSONB() && level.count > 0 && level.comma) {
				throw new IllegalArgumentException("对象结束前存在多余逗号。");
			}
			levels.pop();
			afterValue();
			return true;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 数组内容已读完时关闭一层并返回真；JSONB 按声明长度判断，文本按结束标记判断。
	 *
	 * @return 当前数组读完并关闭一层时为真。
	 * @throws IllegalArgumentException 读取器已关闭、操作位置错误或输入结构不完整。
	 */
	@SimpleFunction
	public boolean 尝试数组结束() {
		try {
			check();
			if (levels.isEmpty() || levels.peek().kind != 2) {
				return false;
			}
			FJ2层 level = levels.peek();
			if (reader.isJSONB()) {
				if (level.count != level.expected) {
					return false;
				}
			} else {
				if (reader.isEnd() || !reader.nextIfArrayEnd()) {
					return false;
				}
				if (level.count > 0 && level.comma) {
					throw new IllegalArgumentException("数组结束前存在多余逗号。");
				}
			}
			levels.pop();
			afterValue();
			return true;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 读取当前对象的属性名及冒号；随后必须读取、跳过或打开对应值。
	 *
	 * @return 对象当前属性的名称。
	 * @throws IllegalArgumentException 读取器已关闭、操作位置错误或输入结构不完整。
	 */
	@SimpleFunction
	public String 读取名称() {
		try {
			check();
			if (levels.isEmpty() || levels.peek().kind != 1 || levels.peek().pending) {
				throw new IllegalArgumentException("当前位置不能读取属性名。");
			}
			FJ2层 level = levels.peek();
			if (!reader.isJSONB() && level.count > 0 && !level.comma) {
				throw new IllegalArgumentException("对象属性之间缺少逗号。");
			}
			if (reader.isEnd()) {
				throw new IllegalArgumentException("对象尚未结束。");
			}
			String name = reader.readFieldName();
			if (name == null) {
				throw new IllegalArgumentException("属性名无效。");
			}
			level.pending = true;
			return name;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 读取当前位置的独立子值；引用只能指向该次读取的子树，不能跨读取调用。
	 *
	 * @return 当前位置完整子值的独立副本。
	 * @throws IllegalArgumentException 读取器已关闭、操作位置错误或输入结构不完整。
	 */
	@SimpleFunction
	public FJ2值 读取值() {
		try {
			beforeValue();
			Object value = reader.readAny();
			reader.handleResolveTasks(value);
			afterValue();
			return new FJ2值(value);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 跳过当前位置的完整子值，检查根值及容器内的位置。
	 *
	 * @throws IllegalArgumentException 读取器已关闭、操作位置错误或输入结构不完整。
	 */
	@SimpleFunction
	public void 跳过值() {
		try {
			beforeValue();
			Object skipped = reader.readAny();
			reader.handleResolveTasks(skipped);
			afterValue();
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 判断一个根值是否已经完整读完且全部手动容器已闭合。
	 *
	 * @return 一个根值已读完且全部手动容器闭合时为真。
	 * @throws IllegalArgumentException 读取器已关闭、操作位置错误或输入结构不完整。
	 */
	@SimpleFunction
	public boolean 是否结束() {
		try {
			check();
			return started && levels.isEmpty() && reader.isEnd() && (reader.isJSONB() || !reader.hasComma());
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 要求恰好一个根值已读完，拒绝空输入、未闭合容器、尾随逗号及剩余数据。
	 *
	 * @throws IllegalArgumentException 读取器已关闭、操作位置错误或输入结构不完整。
	 */
	@SimpleFunction
	public void 要求结束() {
		try {
			if (!是否结束()) {
				throw new IllegalArgumentException("根值未读完、容器未闭合或存在尾随数据。");
			}
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 释放读取器并清空结构状态，可重复关闭。
	 */
	@SimpleFunction
	public void 关闭() {
		try {
			if (reader != null) {
				reader.close();
				reader = null;
				levels.clear();
			}
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 取得仍有效的读取器。
	 *
	 * @return 取得仍有效的读取器。
	 */
	JSONReader check() {
		if (reader == null) {
			throw new IllegalArgumentException("读取器已关闭。");
		}
		return reader;
	}

	/**
	 * 在消费输入前校验根及当前容器的值位置。
	 */
	void beforeValue() {
		check();
		if (levels.size() >= maxLevel) {
			throw new IllegalArgumentException("嵌套超过配置最大层数。");
		}
		reader.getContext().setMaxLevel(maxLevel - levels.size());
		if (reader.isEnd()) {
			throw new IllegalArgumentException("没有可读取的值。");
		}
		if (levels.isEmpty()) {
			if (started) {
				throw new IllegalArgumentException("只能读取一个根值。");
			}
			started = true;
			return;
		}
		FJ2层 level = levels.peek();
		if (level.kind == 1 && !level.pending) {
			throw new IllegalArgumentException("读取对象值前必须读取名称。");
		}
		if (level.kind == 2) {
			if (level.expected >= 0 && level.count >= level.expected) {
				throw new IllegalArgumentException("JSONB 数组元素已读完。");
			}
			if (!reader.isJSONB() && level.count > 0 && !level.comma) {
				throw new IllegalArgumentException("数组元素之间缺少逗号。");
			}
		}
	}

	/**
	 * 记录完整子值已读取，并保存文本分隔符状态。
	 */
	void afterValue() {
		if (!levels.isEmpty()) {
			FJ2层 level = levels.peek();
			level.count++;
			level.pending = false;
			level.comma = !reader.isJSONB() && reader.hasComma();
		}
	}
}
