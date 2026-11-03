package simple.library.wsd.quickjs;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.variants.Variant;

/**
 * 保留空洞、undefined 和元素真实类型的数组，索引从 0 开始。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class QuickJS数组 extends QuickJS对象 {

	/**
	 * 持有所属实例和拥有引用。
	 *
	 * @param context 所属实例。
	 * @param handle 原生引用。
	 */
	QuickJS数组(QuickJS实例 context, long handle) {
		super(context, handle);
	}

	/**
	 * 检查有效数组索引。
	 *
	 * @param index 索引。
	 */
	private static void checkIndex(long index) {
		if (index < 0 || index > 4294967294L) {
			throw new QuickJS错误("数组索引超出范围");
		}
	}

	/**
	 * 读取数组长度，可能包含空洞。
	 *
	 * @return 数组长度，包含空洞位置。
	 */
	@SimpleFunction
	public long 取长度() {
		QuickJS值 value = 取成员("length");
		try {
			return value.到长整数();
		} finally {
			value.释放();
		}
	}

	/**
	 * 设置数组长度，扩大时产生空洞。
	 *
	 * @param length 0 至 4294967295；缩短会删除尾部项目。
	 */
	@SimpleFunction
	public void 置长度(long length) {
		if (length < 0 || length > 4294967295L) {
			throw new QuickJS错误("数组长度超出范围");
		}
		QuickJS值 value = context.创建小数(length);
		try {
			置成员("length", value);
		} finally {
			value.释放();
		}
	}

	/**
	 * 读取数组项目，空洞返回 undefined。
	 *
	 * @param index 0 至 4294967294。
	 * @return 项目值；空洞或不存在时为 undefined。
	 */
	@SimpleFunction
	public QuickJS值 取项目(long index) {
		checkIndex(index);
		return context.op(Native.GET_INDEX, this, null, null, null, index, 0, null, null);
	}

	/**
	 * 设置项目，必要时扩大数组长度。
	 *
	 * @param index 项目索引。
	 * @param value 同一实例的值。
	 */
	@SimpleFunction
	public void 置项目(long index, QuickJS值 value) {
		checkIndex(index);
		context.op(Native.SET_INDEX, this, value, null, null, index, 0, null, null).释放();
	}

	/**
	 * 转换并设置项目，释放临时引用。
	 *
	 * @param index 项目索引。
	 * @param value Simple 值。
	 */
	@SimpleFunction
	public void 置简单项目(long index, Variant value) {
		QuickJS值 converted = context.生成值(value);
		try {
			置项目(index, converted);
		} finally {
			converted.释放();
		}
	}

	/**
	 * 删除项目并留下空洞，不移动后续项目。
	 *
	 * @param index 项目索引。
	 * @return 已删除或项目不存在时为真；不可删除时为假。
	 */
	@SimpleFunction
	public boolean 删除项目(long index) {
		checkIndex(index);
		return 删除成员(Long.toString(index));
	}

	/**
	 * 向数组末尾追加值。
	 *
	 * @param value 待追加值。
	 */
	@SimpleFunction
	public void 追加(QuickJS值 value) {
		context.apply("splice", this, 取长度(), 0, value).释放();
	}

	/**
	 * 插入项目并移动后续项目。
	 *
	 * @param index 插入位置，支持 splice 负索引。
	 * @param value 待插入值。
	 */
	@SimpleFunction
	public void 插入(int index, QuickJS值 value) {
		context.apply("splice", this, index, 0, value).释放();
	}

	/**
	 * 移除连续项目，返回被移除数组。
	 *
	 * @param index 开始位置，可为负数。
	 * @param count 删除数量，按 splice 规则处理。
	 * @return 包含被删除项目的新数组。
	 */
	@SimpleFunction
	public QuickJS数组 移除(int index, int count) {
		return (QuickJS数组)context.apply("splice", this, index, count);
	}

	/**
	 * 按 join 规则连接为文本。
	 *
	 * @param separator 分隔文本。
	 * @return 按分隔符连接的文本。
	 */
	@SimpleFunction
	public String 连接文本(String separator) {
		QuickJS值 text = context.apply("join", this, QuickJS实例.require(separator));
		try {
			return text.到文本();
		} finally {
			text.释放();
		}
	}

	/**
	 * 取得全部项目的独立引用；空洞返回 undefined，最多 100000 项。
	 *
	 * @return 引用数组，各项使用后分别释放。
	 */
	@SimpleFunction
	public QuickJS值[] 取全部项目() {
		context.runtime.checkAccess();
		synchronized (context.runtime.lock) {
			long count = 取长度();
			if (count > 100000) {
				throw new QuickJS错误("批量读取不能超过 100000 项");
			}
			QuickJS值[] values = new QuickJS值[(int)count];
			try {
				for (int i = 0; i < count; i++) {
					values[i] = 取项目(i);
				}
				return values;
			} catch (RuntimeException | Error error) {
				for (QuickJS值 value : values) {
					if (value != null) {
						value.释放();
					}
				}
				throw error;
			}
		}
	}
}
