package simple.library.wsd.quickjs;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.variants.Variant;

/**
 * 真实 JS 对象，支持字符串与 Symbol 键、描述符、原型和集合成员。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public class QJS对象 extends QJS值 {

	/**
	 * 持有所属实例和拥有引用。
	 *
	 * @param context 所属实例。
	 * @param handle  原生引用。
	 */
	QJS对象(QJS实例 context, long handle) {
		super(context, handle);
	}

	/**
	 * 读取属性及原型链，不存在时返回 undefined。
	 *
	 * @param name 属性名。
	 * @return 成员值；不存在时为 undefined。
	 */
	@SimpleFunction
	public QJS值 取成员(String name) {
		return context.op(Native.GET_PROPERTY, this, null, QJS实例.require(name), null, 0, 0, null, null);
	}

	/**
	 * 使用任意 JS 属性键读取值。
	 *
	 * @param key 字符串、Symbol 或可转换的属性键。
	 * @return 属性键对应的值；不存在时为 undefined。
	 */
	@SimpleFunction
	public QJS值 取键值(QJS值 key) {
		return context.op(Native.GET_PROPERTY, this, null, null, null, 0, 0, new QJS值[] { key }, null);
	}

	/**
	 * 设置属性，不可写或 setter 失败时报告错误。
	 *
	 * @param name  属性名。
	 * @param value 同一实例的值。
	 */
	@SimpleFunction
	public void 置成员(String name, QJS值 value) {
		context.op(Native.SET_PROPERTY, this, value, QJS实例.require(name), null, 0, 0, null, null).释放();
	}

	/**
	 * 设置任意属性键对应的值。
	 *
	 * @param key   JS 属性键。
	 * @param value 同一实例的值。
	 */
	@SimpleFunction
	public void 置键值(QJS值 key, QJS值 value) {
		context.op(Native.SET_PROPERTY, this, value, null, null, 0, 0, new QJS值[] { key }, null).释放();
	}

	/**
	 * 转换并设置成员，自动释放临时引用。
	 *
	 * @param name  属性名。
	 * @param value Simple 值。
	 */
	@SimpleFunction
	public void 置简单成员(String name, Variant value) {
		QJS值 converted = context.生成值(value);
		try {
			置成员(name, converted);
		} finally {
			converted.释放();
		}
	}

	/**
	 * 判断对象或原型链是否包含属性。
	 *
	 * @param name 属性名。
	 * @return 当前对象或原型链包含该成员时为真。
	 */
	@SimpleFunction
	public boolean 是否有成员(String name) {
		QJS值 result = context.op(Native.HAS_PROPERTY, this, null, QJS实例.require(name), null, 0, 0, null, null);
		try {
			return result.到逻辑();
		} finally {
			result.释放();
		}
	}

	/**
	 * 删除属性；不可配置属性返回假，代理异常照常报告。
	 *
	 * @param name 属性名。
	 * @return 已删除或成员不存在时为真；不可删除时为假。
	 */
	@SimpleFunction
	public boolean 删除成员(String name) {
		QJS值 result = context.op(Native.DELETE_PROPERTY, this, null, QJS实例.require(name), null, 0, 0, null, null);
		try {
			return result.到逻辑();
		} finally {
			result.释放();
		}
	}

	/**
	 * 列出自有字符串和 Symbol 键，包含不可枚举属性。
	 *
	 * @return 含字符串键及 Symbol 键的数组。
	 */
	@SimpleFunction
	public QJS数组 取自有键() {
		return (QJS数组) context.apply("ownKeys", this);
	}

	/**
	 * 列出自有可枚举字符串键。
	 *
	 * @return 包含自有可枚举字符串键的数组。
	 */
	@SimpleFunction
	public QJS数组 取可枚举键() {
		return (QJS数组) context.apply("keys", this);
	}

	/**
	 * 读取对象原型；根原型可以为 null。
	 *
	 * @return 原型对象；没有原型时为 null。
	 */
	@SimpleFunction
	public QJS值 取原型() {
		return (QJS值) context.apply("prototype", this);
	}

	/**
	 * 读取 Object.prototype.toString 结果，遵循 Symbol.toStringTag。
	 *
	 * @return Object.prototype.toString 的结果值。
	 */
	@SimpleFunction
	public QJS值 取对象种类() {
		return (QJS值) context.apply("brand", this);
	}

	/**
	 * 取得同步迭代器；不能迭代时报告错误。
	 *
	 * @return 同步迭代器对象。
	 */
	@SimpleFunction
	public QJS对象 取迭代器() {
		QJS值 result = context.apply("iterator", this);
		try {
			return result.到对象();
		} catch (QJS错误 error) {
			result.释放();
			throw error;
		}
	}

	/**
	 * 判断是否为自有属性。
	 *
	 * @param key 字符串或 Symbol 键。
	 * @return 对象自身定义了该键时为真。
	 */
	@SimpleFunction
	public boolean 是否自有键(QJS值 key) {
		QJS值 result = context.apply("hasOwn", this, key);
		try {
			return result.到逻辑();
		} finally {
			result.释放();
		}
	}

	/**
	 * 读取自有属性描述，不存在时返回 undefined。
	 *
	 * @param key 属性键。
	 * @return 属性描述对象；不存在时为 undefined。
	 */
	@SimpleFunction
	public QJS值 取属性描述(QJS值 key) {
		return (QJS值) context.apply("descriptor", this, key);
	}

	/**
	 * 按 Reflect.defineProperty 定义属性，非法描述抛出错误。
	 *
	 * @param key        属性键。
	 * @param descriptor 含 value、get、set、writable 等字段的描述。
	 * @return 定义成功时为真；目标不允许定义时为假。
	 */
	@SimpleFunction
	public boolean 定义属性(QJS值 key, QJS对象 descriptor) {
		QJS值 result = context.apply("define", this, key, descriptor);
		try {
			return result.到逻辑();
		} finally {
			result.释放();
		}
	}

	/**
	 * 设置对象原型。
	 *
	 * @param prototype 同一实例的对象值或 JS null 值；不能传空引用。
	 */
	@SimpleFunction
	public void 置原型(QJS值 prototype) {
		context.apply("setPrototype", this, prototype).释放();
	}

	/**
	 * 冻结对象。
	 */
	@SimpleFunction
	public void 冻结() {
		context.apply("freeze", this).释放();
	}

	/**
	 * 密封对象。
	 */
	@SimpleFunction
	public void 密封() {
		context.apply("seal", this).释放();
	}

	/**
	 * 禁止增加属性。
	 */
	@SimpleFunction
	public void 禁止扩展() {
		context.apply("prevent", this).释放();
	}

	/**
	 * 判断是否可扩展。
	 *
	 * @return 允许新增属性时为真。
	 */
	@SimpleFunction
	public boolean 是否可扩展() {
		QJS值 result = context.apply("extensible", this);
		try {
			return result.到逻辑();
		} finally {
			result.释放();
		}
	}

	/**
	 * 判断是否已冻结。
	 *
	 * @return 对象已冻结时为真。
	 */
	@SimpleFunction
	public boolean 是否已冻结() {
		QJS值 result = context.apply("frozen", this);
		try {
			return result.到逻辑();
		} finally {
			result.释放();
		}
	}

	/**
	 * 判断是否已密封。
	 *
	 * @return 对象已密封时为真。
	 */
	@SimpleFunction
	public boolean 是否已密封() {
		QJS值 result = context.apply("sealed", this);
		try {
			return result.到逻辑();
		} finally {
			result.释放();
		}
	}

	/**
	 * 以当前对象作为 this 调用属性函数。
	 *
	 * @param name      可调用属性名。
	 * @param arguments Simple 参数数组。
	 * @return 成员函数的实际返回值。
	 */
	@SimpleFunction
	public QJS值 调用成员(String name, Variant[] arguments) {
		QJS值 function = 取成员(name);
		try {
			return context.call(function, this, arguments, false);
		} finally {
			function.释放();
		}
	}

	/**
	 * 调用迭代器 next，返回含 value 和 done 的对象。
	 *
	 * @param value 传给 next 的值；空引用表示 undefined。
	 * @return 含 value 和 done 成员的迭代结果。
	 */
	@SimpleFunction
	public QJS对象 下一项(QJS值 value) {
		QJS值 result = context.apply("next", this, value);
		try {
			return result.到对象();
		} catch (QJS错误 error) {
			result.释放();
			throw error;
		}
	}
}
