package simple.library.wsd.quickjs;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.variants.Variant;

/**
 * 保留实际类型的 JS 值；显式释放引用或销毁实例后不可继续使用。 值形参的空引用表示 undefined，JS null 须显式创建。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public class QJS值 {

	final QJS实例 context;
	volatile long handle;

	/**
	 * 持有所属实例和拥有引用。
	 *
	 * @param context 所属实例。
	 * @param handle  原生引用。
	 */
	QJS值(QJS实例 context, long handle) {
		this.context = context;
		this.handle = handle;
	}

	/**
	 * 在运行时锁内回收引用。
	 */
	void closeInternal() {
		if (handle != 0) {
			Native.release(handle);
			handle = 0;
			context.values.remove(this);
		}
	}

	/**
	 * 读取已经转换成字符串的值。
	 *
	 * @return UTF-16 文本。
	 */
	private String nativeText() {
		return context.runtime.perform(context, () -> {
			return Native.string(context.handle, context.pointer(QJS值.this));
		});
	}

	/**
	 * 取得 QJS常量中的类型编号。
	 *
	 * @return QJS常量中对应的类型编号。
	 */
	@SimpleFunction
	public int 取类型() {
		return context.runtime.perform(context, () -> {
			return Native.kind(context.handle, context.pointer(QJS值.this));
		});
	}

	/**
	 * 取得包装层类型名；JS typeof 可用一元计算读取。
	 *
	 * @return 包装层类型名，不等同于 JS typeof 结果。
	 */
	@SimpleFunction
	public String 取类型名称() {
		String[] names = { "undefined", "null", "boolean", "number", "string", "bigint", "symbol", "object", "array",
				"function", "promise", "arraybuffer", "typedarray", "bigfloat", "bigdecimal" };
		return names[取类型()];
	}

	/**
	 * 判断是否为 undefined。
	 *
	 * @return undefined 时为真，否则为假。
	 */
	@SimpleFunction
	public boolean 是否未定义() {
		return 取类型() == 0;
	}

	/**
	 * 判断是否为 null。
	 *
	 * @return null 时为真，否则为假。
	 */
	@SimpleFunction
	public boolean 是否空值() {
		return 取类型() == 1;
	}

	/**
	 * 判断是否为 Number。
	 *
	 * @return Number 时为真，否则为假。
	 */
	@SimpleFunction
	public boolean 是否数值() {
		return 取类型() == 3;
	}

	/**
	 * 判断是否为 BigInt。
	 *
	 * @return BigInt 时为真，否则为假。
	 */
	@SimpleFunction
	public boolean 是否大整数() {
		return 取类型() == 5;
	}

	/**
	 * 判断是否为 Symbol。
	 *
	 * @return Symbol 时为真，否则为假。
	 */
	@SimpleFunction
	public boolean 是否符号() {
		return 取类型() == 6;
	}

	/**
	 * 判断是否为 Array。
	 *
	 * @return Array 时为真，否则为假。
	 */
	@SimpleFunction
	public boolean 是否数组() {
		return 取类型() == 8;
	}

	/**
	 * 判断是否为可调用函数。
	 *
	 * @return 可调用函数时为真，否则为假。
	 */
	@SimpleFunction
	public boolean 是否函数() {
		return 取类型() == 9;
	}

	/**
	 * 判断是否为真实 Promise。
	 *
	 * @return 真实 Promise 时为真，否则为假。
	 */
	@SimpleFunction
	public boolean 是否承诺() {
		return 取类型() == 10;
	}

	/**
	 * 判断引用或实例是否已释放。
	 *
	 * @return 当前引用或所属实例已释放时为真，否则为假。
	 */
	@SimpleFunction
	public boolean 是否已释放() {
		return handle == 0 || context.closed;
	}

	/**
	 * 按 JS 真值规则转换，空对象与空数组仍为真。
	 *
	 * @return 按 JS 真值规则计算的逻辑值。
	 */
	@SimpleFunction
	public boolean 到逻辑() {
		return context.runtime.perform(context, () -> {
			return Native.bool(context.handle, context.pointer(QJS值.this));
		});
	}

	/**
	 * 按 String 规则转换，支持 Symbol、零字符和完整 Unicode。
	 *
	 * @return 完整字符串内容。
	 */
	@SimpleFunction
	public String 到文本() {
		QJS值 text = context.apply("string", this);
		try {
			return text.nativeText();
		} finally {
			text.释放();
		}
	}

	/**
	 * 按 Number 规则转换，BigInt 可能丢失精度。
	 *
	 * @return 转换后的 Number，可能为 NaN 或无穷大。
	 */
	@SimpleFunction
	public double 到小数() {
		final QJS值 value = context.apply("number", this);
		try {
			return context.runtime.perform(context, () -> {
				return Native.number(context.handle, context.pointer(value));
			});
		} finally {
			value.释放();
		}
	}

	/**
	 * 截断小数并检查整数型范围；非法或溢出时抛出错误。
	 *
	 * @return 向零截断后的整数。
	 */
	@SimpleFunction
	public int 到整数() {
		double value = 到小数();
		if (Double.isNaN(value) || value <= -2147483649.0 || value >= 2147483648.0) {
			throw new QJS错误("数值超出整数型范围");
		}
		return (int) value;
	}

	/**
	 * 读取长整数；BigInt 必须在 64 位范围内，Number 不能恢复已丢失精度。
	 *
	 * @return 在 64 位范围内的长整数。
	 */
	@SimpleFunction
	public long 到长整数() {
		if (取类型() == 5) {
			try {
				java.math.BigInteger value = new java.math.BigInteger(到文本());
				if (value.compareTo(java.math.BigInteger.valueOf(Long.MIN_VALUE)) < 0
						|| value.compareTo(java.math.BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
					throw new ArithmeticException("long overflow");
				}
				return value.longValue();
			} catch (ArithmeticException error) {
				throw new QJS错误("BigInt 超出长整数型范围");
			}
		}
		double value = 到小数();
		if (Double.isNaN(value) || value < -0x1p63 || value >= 0x1p63) {
			throw new QJS错误("数值超出长整数型范围");
		}
		return (long) value;
	}

	/**
	 * 显式转换基本类型；null 与 undefined 转为空，BigInt 转为十进制文本，其它值保留包装。
	 *
	 * @return Simple 基本值或持有独立引用的 JS 包装对象。
	 */
	@SimpleFunction
	public Variant 到简单值() {
		int type = 取类型();
		if (type == 0 || type == 1) {
			return simple.runtime.variants.ObjectVariant.getObjectVariant(null);
		}
		if (type == 2) {
			return simple.runtime.variants.BooleanVariant.getBooleanVariant(到逻辑());
		}
		if (type == 3) {
			return simple.runtime.variants.DoubleVariant.getDoubleVariant(到小数());
		}
		if (type == 4 || type == 5) {
			return simple.runtime.variants.StringVariant.getStringVariant(到文本());
		}
		return simple.runtime.variants.ObjectVariant.getObjectVariant(保留());
	}

	/**
	 * 递归转换为 Simple 集合及哈希表，保留重复引用和循环关系。 null、undefined 和空洞转为空，BigInt 转文本；特殊对象保留独立 JS 引用，使用后须释放。
	 *
	 * @return Simple 数据图。
	 */
	@SimpleFunction
	public Variant 深度转换() {
		return ValueConversion.toSimple(this);
	}

	/**
	 * 将 JS 数组递归复制为 Simple 集合。
	 *
	 * @return 集合；特殊 JS 对象的引用由调用方释放。
	 */
	@SimpleFunction
	public simple.runtime.collections.集合 到集合() {
		if (!是否数组()) {
			throw new QJS错误("到集合需要 JS 数组");
		}
		return (simple.runtime.collections.集合) 深度转换().getObject();
	}

	/**
	 * 将普通 JS 对象的自有可枚举字符串成员递归复制为哈希表。
	 *
	 * @return 哈希表；特殊 JS 对象的引用由调用方释放。
	 */
	@SimpleFunction
	public simple.runtime.collections.哈希表 到哈希表() {
		QJS值 test = context.apply("plain", this);
		try {
			if (!test.到逻辑()) {
				throw new QJS错误("到哈希表需要普通 JS 对象");
			}
		} finally {
			test.释放();
		}
		return (simple.runtime.collections.哈希表) 深度转换().getObject();
	}

	/**
	 * 检查类型并返回当前包装，不增加引用计数。
	 *
	 * @return 当前对象包装，不另建引用。
	 */
	@SimpleFunction
	public QJS对象 到对象() {
		取类型();
		if (!(this instanceof QJS对象)) {
			throw new QJS错误("类型不匹配：需要 QJS对象");
		}
		return (QJS对象) this;
	}

	/**
	 * 检查类型并返回当前包装，不增加引用计数。
	 *
	 * @return 当前数组包装，不另建引用。
	 */
	@SimpleFunction
	public QJS数组 到数组() {
		取类型();
		if (!(this instanceof QJS数组)) {
			throw new QJS错误("类型不匹配：需要 QJS数组");
		}
		return (QJS数组) this;
	}

	/**
	 * 检查类型并返回当前包装，不增加引用计数。
	 *
	 * @return 当前函数包装，不另建引用。
	 */
	@SimpleFunction
	public QJS函数 到函数() {
		取类型();
		if (!(this instanceof QJS函数)) {
			throw new QJS错误("类型不匹配：需要 QJS函数");
		}
		return (QJS函数) this;
	}

	/**
	 * 检查类型并返回当前包装，不增加引用计数。
	 *
	 * @return 当前承诺包装，不另建引用。
	 */
	@SimpleFunction
	public QJS承诺 到承诺() {
		取类型();
		if (!(this instanceof QJS承诺)) {
			throw new QJS错误("类型不匹配：需要 QJS承诺");
		}
		return (QJS承诺) this;
	}

	/**
	 * 取得独立拥有引用，可用于保留回调中的借用值。
	 *
	 * @return 同一 JS 值的独立拥有引用。
	 */
	@SimpleFunction
	public QJS值 保留() {
		return context.op(Native.DUPLICATE, this, null, null, null, 0, 0, null, null);
	}

	/**
	 * 显式复制到同一运行时的其它实例，不允许跨运行时。
	 *
	 * @param target 同一运行时的目标实例。
	 * @return 目标实例持有的独立引用；底层 JS 数据仍共享。
	 */
	@SimpleFunction
	public QJS值 复制到(QJS实例 target) {
		if (target == null || target.runtime != context.runtime) {
			throw new QJS错误("目标必须属于同一运行时");
		}
		context.runtime.checkAccess();
		synchronized (context.runtime.lock) {
			context.pointer(this);
			return target.runtime.perform(target, () -> {
				return target
						.wrap(Native.value(target.handle, Native.DUPLICATE, handle, 0, null, null, 0, 0, null, null));
			});
		}
	}

	/**
	 * 执行 JS 算术、位、比较或关系运算。
	 *
	 * @param operator JS 二元运算符，例如 +、===、instanceof。
	 * @param other    同一实例的右操作数。
	 * @return 二元运算结果的独立引用。
	 */
	@SimpleFunction
	public QJS值 计算(String operator, QJS值 other) {
		return context.apply("binary", QJS实例.require(operator), this, other);
	}

	/**
	 * 执行 JS 一元运算。
	 *
	 * @param operator 支持 +、-、~、!、typeof、void。
	 * @return 一元运算结果的独立引用。
	 */
	@SimpleFunction
	public QJS值 一元计算(String operator) {
		return context.apply("unary", QJS实例.require(operator), this);
	}

	/**
	 * 按 Object.is 比较，区分正负零且认为 NaN 相同。
	 *
	 * @param other 同一实例的值。
	 * @return 符合 Object.is 规则时为真，否则为假。
	 */
	@SimpleFunction
	public boolean 是否相同(QJS值 other) {
		QJS值 result = context.apply("same", this, other);
		try {
			return result.到逻辑();
		} finally {
			result.释放();
		}
	}

	/**
	 * 按 JSON.stringify 返回字符串值或 undefined。
	 *
	 * @return JSON 字符串值；无法表示时为 undefined。
	 */
	@SimpleFunction
	public QJS值 转JSON() {
		return context.apply("json", this, null, null);
	}

	/**
	 * 复制 ArrayBuffer、SharedArrayBuffer、TypedArray 或 DataView 的当前字节范围。
	 *
	 * @return 当前可见范围的字节副本。
	 */
	@SimpleFunction
	public byte[] 到字节集() {
		final QJS值 view = context.apply("bytes", this);
		try {
			return context.runtime.perform(context, () -> {
				return Native.bytes(context.handle, context.pointer(view));
			});
		} finally {
			view.释放();
		}
	}

	/**
	 * 读取 Symbol.for 注册键，独立 Symbol 返回 undefined。
	 *
	 * @return 注册键字符串；独立 Symbol 返回 undefined。
	 */
	@SimpleFunction
	public QJS值 取符号注册键() {
		return context.apply("symbolKey", this);
	}

	/**
	 * 释放当前拥有引用，可重复调用；其它引用中的 JS 数据继续有效。
	 */
	@SimpleFunction
	public void 释放() {
		context.runtime.checkAccess();
		synchronized (context.runtime.lock) {
			if (context.closed) {
				handle = 0;
				return;
			}
			closeInternal();
		}
	}
}
