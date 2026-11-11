package simple.library.wsd.fastjson2;

import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Deque;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONArray;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 文本或 JSONB 的顺序写入器，自动处理分隔符并检查根值、对象名值及定长数组。输出保存在内存中，回调期间不得重入，同一实例串行使用。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class FJ2写入器 {

	/**
	 * 上游写入器；关闭后置为空引用。
	 */
	private JSONWriter writer;

	/**
	 * 尚未闭合的写入容器，由栈顶表示当前层。
	 */
	private final Deque<FJ2层> levels = new ArrayDeque<FJ2层>();

	/**
	 * 是否已经写入或开始写入根值。
	 */
	private boolean rootWritten;

	/**
	 * 是否正在调用写入或过滤逻辑，用于拒绝回调重入。
	 */
	private boolean busy;

	/**
	 * 打开时复制的写入配置，关闭后释放其回调引用。
	 */
	private FJ2写配置 valueConfig;

	/**
	 * 保存独立写入器。
	 *
	 * @param writer 上游写入器。
	 */
	private FJ2写入器(JSONWriter writer) {
		this.writer = writer;
	}

	/**
	 * 创建文本写入器；手动写名称不应用属性过滤，完整值中的属性仍使用过滤器。
	 *
	 * @param config 打开时复制配置；回调实例共享。
	 * @return 使用独立配置的新写入器。
	 * @throws IllegalArgumentException 写入配置为空或不能创建指定格式的写入器。
	 */
	@SimpleFunction
	public static FJ2写入器 创建(FJ2写配置 config) {
		try {
			FJ2写配置 copy = FJ2支持.require(config).复制();
			FJ2写配置 plain = copy.复制();
			plain.filter = null;
			FJ2写入器 result = new FJ2写入器(JSONWriter.of(FJ2支持.writeContext(plain)));
			result.valueConfig = copy;
			return result;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 创建 JSONB 写入器；数组必须显式提供元素数量，完整值应用本库过滤规则。
	 *
	 * @param config  打开时复制配置；回调实例共享。
	 * @param symbols 可选外部符号表。
	 * @return 使用独立配置的新写入器。
	 * @throws IllegalArgumentException 写入配置为空或不能创建指定格式的写入器。
	 */
	@SimpleFunction
	public static FJ2写入器 创建JSONB(FJ2写配置 config, FJ2符号表 symbols) {
		try {
			FJ2写配置 copy = FJ2支持.require(config).复制();
			FJ2写配置 plain = copy.复制();
			plain.filter = null;
			FJ2写入器 result = new FJ2写入器(
					JSONWriter.ofJSONB(FJ2支持.writeContext(plain), symbols == null ? null : symbols.table));
			result.valueConfig = copy;
			return result;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 判断当前输出是否为 JSONB。
	 *
	 * @return 当前读写格式为 JSONB 时为真。
	 * @throws IllegalArgumentException 写入器已关闭、发生重入，或当前位置及数据不满足操作要求。
	 */
	@SimpleFunction
	public boolean 是否JSONB() {
		try {
			return check().jsonb;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 判断写入器是否已关闭。
	 *
	 * @return 读写器已经关闭时为真。
	 */
	@SimpleFunction
	public boolean 是否关闭() {
		try {
			return writer == null;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 开始对象，必须以结束对象关闭。
	 *
	 * @throws IllegalArgumentException 写入器已关闭、发生重入，或当前位置及数据不满足操作要求。
	 */
	@SimpleFunction
	public void 开始对象() {
		try {
			FJ2层 level = new FJ2层(1, -1);
			level.token = new JSONObject();
			beforeValue(level.token);
			writer.startObject();
			levels.push(level);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 开始未知长度的文本数组；JSONB 请调用带数量参数的同名方法。
	 *
	 * @throws IllegalArgumentException 写入器已关闭、发生重入，或当前位置及数据不满足操作要求。
	 */
	@SimpleFunction
	public void 开始数组() {
		try {
			check();
			if (writer.jsonb) {
				throw new IllegalArgumentException("JSONB 数组必须提供元素数量。");
			}
			开始数组(-1);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 开始指定长度的数组；结束时检查元素数量，嵌套容器算一个元素。
	 *
	 * @param size JSONB 为非负元素数量；文本为非负定长或 -1 表示不限长度。
	 * @throws IllegalArgumentException 写入器已关闭、发生重入，或当前位置及数据不满足操作要求。
	 */
	@SimpleFunction
	public void 开始数组(int size) {
		try {
			check();
			if (size < -1 || (writer.jsonb && size < 0)) {
				throw new IllegalArgumentException("数组数量无效。");
			}
			FJ2层 level = new FJ2层(2, size);
			level.token = new JSONArray();
			beforeValue(level.token);
			if (writer.jsonb) {
				writer.startArray(size);
			} else {
				writer.startArray();
			}
			levels.push(level);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 关闭当前容器；拒绝类型不匹配、缺少属性值或定长数组数量不符。
	 *
	 * @throws IllegalArgumentException 写入器已关闭、发生重入，或当前位置及数据不满足操作要求。
	 */
	@SimpleFunction
	public void 结束对象() {
		try {
			check();
			if (levels.isEmpty() || levels.peek().kind != 1 || levels.peek().pending) {
				throw new IllegalArgumentException("容器结束不匹配或属性缺值。");
			}
			FJ2层 level = levels.peek();
			if (level.expected >= 0 && level.count != level.expected) {
				throw new IllegalArgumentException("数组元素数量不符。");
			}
			writer.endObject();
			writer.popPath(level.token);
			levels.pop();
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 关闭当前容器；拒绝类型不匹配、缺少属性值或定长数组数量不符。
	 *
	 * @throws IllegalArgumentException 写入器已关闭、发生重入，或当前位置及数据不满足操作要求。
	 */
	@SimpleFunction
	public void 结束数组() {
		try {
			check();
			if (levels.isEmpty() || levels.peek().kind != 2 || levels.peek().pending) {
				throw new IllegalArgumentException("容器结束不匹配或属性缺值。");
			}
			FJ2层 level = levels.peek();
			if (level.expected >= 0 && level.count != level.expected) {
				throw new IllegalArgumentException("数组元素数量不符。");
			}
			writer.endArray();
			writer.popPath(level.token);
			levels.pop();
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 手动写对象属性名，下一步必须写值；名称按原样使用，不应用属性过滤器。
	 *
	 * @param name 非空引用的对象属性名。
	 * @throws IllegalArgumentException 写入器已关闭、发生重入，或当前位置及数据不满足操作要求。
	 */
	@SimpleFunction
	public void 写名称(String name) {
		try {
			FJ2支持.text(name);
			check();
			if (levels.isEmpty() || levels.peek().kind != 1 || levels.peek().pending) {
				throw new IllegalArgumentException("当前位置不能写名称。");
			}
			FJ2层 level = levels.peek();
			if (!writer.jsonb && level.count > 0) {
				writer.writeComma();
			}
			writer.writeString(name);
			if (!writer.jsonb) {
				writer.writeColon();
			}
			level.pending = true;
			level.name = name;
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 写完整值；过滤或上游写入失败会使此写入器关闭，避免输出半份数据。
	 *
	 * @param value 空引用表示 JSON null。
	 * @throws IllegalArgumentException 写入器已关闭、发生重入，或当前位置及数据不满足操作要求。
	 */
	@SimpleFunction
	public void 写值(FJ2值 value) {
		try {
			check();
			Object data = FJ2支持.copy(FJ2支持.raw(value));
			if (valueConfig.filter != null) {
				busy = true;
				try {
					data = FJ2支持.filterForWrite(data, valueConfig);
				} catch (RuntimeException error) {
					writer.close();
					writer = null;
					valueConfig = null;
					levels.clear();
					throw error;
				} finally {
					busy = false;
				}
			}
			FJ2支持.ensureSafe(data, valueConfig);
			beforeValue(data);
			busy = true;
			try {
				writer.writeAny(data);
				writer.popPath(data);
			} catch (RuntimeException error) {
				writer.close();
				writer = null;
				valueConfig = null;
				levels.clear();
				throw error;
			} finally {
				busy = false;
			}
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 解析并验证完整子值后再写入，失败不消费值位置。
	 *
	 * @param text 完整 JSON 子值。
	 * @throws IllegalArgumentException 写入器已关闭、发生重入，或当前位置及数据不满足操作要求。
	 */
	@SimpleFunction
	public void 写JSON文本(String text) {
		try {
			FJ2值 value = FJ2操作.解析文本(text);
			写值(value);
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回完整文本输出；JSONB 模式请读取 JSONB 字节。
	 *
	 * @return 完整的文本输出。
	 * @throws IllegalArgumentException 写入器已关闭、发生重入，或当前位置及数据不满足操作要求。
	 */
	@SimpleFunction
	public String 取文本() {
		try {
			complete();
			if (writer.jsonb) {
				throw new IllegalArgumentException("JSONB 模式请读取 JSONB 字节。");
			}
			return writer.toString();
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回完整文本的 UTF-8 字节；不适用于 JSONB 模式。
	 *
	 * @return 完整文本输出的 UTF-8 字节。
	 * @throws IllegalArgumentException 写入器已关闭、发生重入，或当前位置及数据不满足操作要求。
	 */
	@SimpleFunction
	public byte[] 取UTF8字节() {
		try {
			return 取文本().getBytes(Charset.forName("UTF-8"));
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 返回完整 JSONB 的独立字节副本；不适用于文本模式。
	 *
	 * @return 完整 JSONB 输出的独立字节副本。
	 * @throws IllegalArgumentException 写入器已关闭、发生重入，或当前位置及数据不满足操作要求。
	 */
	@SimpleFunction
	public byte[] 取JSONB字节() {
		try {
			complete();
			if (!writer.jsonb) {
				throw new IllegalArgumentException("当前不是 JSONB 模式。");
			}
			return writer.getBytes().clone();
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 释放写入器，可重复关闭；回调期间不能关闭或读取同一个写入器。
	 *
	 * @throws IllegalArgumentException 回调期间尝试关闭同一个写入器。
	 */
	@SimpleFunction
	public void 关闭() {
		try {
			if (busy) {
				throw new IllegalArgumentException("回调期间不能操作同一写入器。");
			}
			if (writer != null) {
				writer.close();
				writer = null;
				valueConfig = null;
				levels.clear();
			}
		} catch (RuntimeException error) {
			throw FJ2支持.failure(error);
		}
	}

	/**
	 * 检查写入器有效且没有回调重入。
	 *
	 * @return 检查写入器有效且没有回调重入。
	 */
	JSONWriter check() {
		if (writer == null || busy) {
			throw new IllegalArgumentException("写入器已关闭或正处于回调中。");
		}
		return writer;
	}

	/**
	 * 检查根值和容器全部写入完成。
	 */
	void complete() {
		check();
		if (!rootWritten || !levels.isEmpty()) {
			throw new IllegalArgumentException("文档尚未完整写入。");
		}
	}

	/**
	 * 校验值位置并处理数组分隔符。
	 *
	 * @param token 当前值或手动容器的身份，用于引用路径。
	 */
	void beforeValue(Object token) {
		check();
		if (levels.size() >= 512) {
			throw new IllegalArgumentException("嵌套超过 512 层。");
		}
		if (levels.isEmpty()) {
			if (rootWritten) {
				throw new IllegalArgumentException("只能写一个根值。");
			}
			rootWritten = true;
			writer.setRootObject(token);
			return;
		}
		FJ2层 level = levels.peek();
		if (level.kind == 1 && !level.pending) {
			throw new IllegalArgumentException("写对象值前必须写名称。");
		}
		if (level.expected >= 0 && level.count >= level.expected) {
			throw new IllegalArgumentException("数组元素超出声明数量。");
		}
		if (level.kind == 2 && level.count > 0 && !writer.jsonb) {
			writer.writeComma();
		}
		// 在增加数量前登记当前值的位置，使完整子值中的引用仍相对于整个文档。
		if (level.kind == 1) {
			writer.setPath(level.name, token);
		} else {
			writer.setPath(level.count, token);
		}
		level.count++;
		level.pending = false;
	}
}
