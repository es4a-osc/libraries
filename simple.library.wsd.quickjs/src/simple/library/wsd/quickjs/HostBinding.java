package simple.library.wsd.quickjs;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleProperty;
import simple.runtime.collections.哈希表;
import simple.runtime.collections.集合;
import simple.runtime.variants.Variant;

/**
 * 显式宿主成员绑定；默认仅公开 Simple 注解成员，按签名处理重载、属性和字段。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class HostBinding {
	/**
	 * 禁止创建工具对象。
	 */
	private HostBinding() {
	}

	/**
	 * 建立绑定对象，未指定成员时只选择 Simple 导出成员。
	 *
	 * @param context  所属实例。
	 * @param target   宿主对象。
	 * @param selected 指定成员名；空表示使用 Simple 注解。
	 * @return 新的 JS 对象引用。
	 */
	static QJS对象 create(final QJS实例 context, final Object target, String[] selected) {
		if (target == null) {
			throw new QJS错误("宿主对象不能为空");
		}
		Set<String> requested = selected == null ? null : new HashSet<String>(Arrays.asList(selected));
		Map<String, List<Method>> functions = new LinkedHashMap<String, List<Method>>();
		Map<String, Method[]> properties = new LinkedHashMap<String, Method[]>();
		List<Field> fields = new ArrayList<Field>();
		Set<String> found = new HashSet<String>();

		for (Method method : target.getClass().getMethods()) {
			if (method.isBridge() || method.isSynthetic() || method.getDeclaringClass() == Object.class) {
				continue;
			}
			boolean property = method.isAnnotationPresent(SimpleProperty.class);
			if (requested != null ? !requested.contains(method.getName())
					: !property && !method.isAnnotationPresent(SimpleFunction.class)) {
				continue;
			}
			found.add(method.getName());
			if (property) {
				Method[] pair = properties.get(method.getName());
				if (pair == null) {
					pair = new Method[2];
					properties.put(method.getName(), pair);
				}
				if (method.getParameterTypes().length == 0 && method.getReturnType() != Void.TYPE) {
					pair[0] = method;
				} else if (method.getParameterTypes().length == 1 && method.getReturnType() == Void.TYPE) {
					pair[1] = method;
				} else {
					throw new QJS错误("宿主属性签名无效：" + method.getName());
				}
			} else {
				List<Method> overloads = functions.get(method.getName());
				if (overloads == null) {
					overloads = new ArrayList<Method>();
					functions.put(method.getName(), overloads);
				}
				overloads.add(method);
			}
		}

		for (Field field : target.getClass().getFields()) {
			if (field.isSynthetic() || (requested != null ? !requested.contains(field.getName())
					: !field.isAnnotationPresent(SimpleDataElement.class))) {
				continue;
			}
			found.add(field.getName());
			fields.add(field);
		}
		if (requested != null && !found.containsAll(requested)) {
			requested.removeAll(found);
			throw new QJS错误("未找到可绑定的公开成员：" + requested);
		}
		if (found.isEmpty()) {
			throw new QJS错误("宿主对象没有 Simple 导出成员，可用成员列表明确选择公开成员");
		}

		QJS对象 object = context.创建对象();
		try {
			for (Map.Entry<String, List<Method>> entry : functions.entrySet()) {
				final List<Method> methods = entry.getValue();
				QJS函数 function = context.创建函数(entry.getKey(), 0, new ManagedCallback() {
					@Override
					public QJS值 处理调用(QJS值 receiver, QJS参数 arguments) {
						return invoke(context, target, methods, arguments);
					}
				});
				try {
					context.apply("dataProperty", object, entry.getKey(), function).释放();
				} finally {
					function.释放();
				}
			}

			for (Map.Entry<String, Method[]> entry : properties.entrySet()) {
				if (functions.containsKey(entry.getKey())) {
					throw new QJS错误("宿主方法与属性重名：" + entry.getKey());
				}
				final Method getter = entry.getValue()[0], setter = entry.getValue()[1];
				defineAccessor(context, object, entry.getKey(), target, getter, setter, null);
			}
			for (final Field field : fields) {
				if (functions.containsKey(field.getName()) || properties.containsKey(field.getName())) {
					throw new QJS错误("宿主成员重名：" + field.getName());
				}
				defineAccessor(context, object, field.getName(), target, null, null, field);
			}
			return object;
		} catch (RuntimeException | Error error) {
			object.释放();
			throw error;
		}
	}

	/**
	 * 绑定实时属性或字段，不把宿主值做成一次性快照。
	 *
	 * @param context 所属实例。
	 * @param object  目标对象。
	 * @param name    属性名。
	 * @param target  宿主对象。
	 * @param getter  可选读取方法。
	 * @param setter  可选设置方法。
	 * @param field   可选公开字段。
	 */
	private static void defineAccessor(final QJS实例 context, QJS对象 object, String name, final Object target,
			final Method getter, final Method setter, final Field field) {
		QJS函数 read = null, write = null;
		try {
			if (getter != null || field != null) {
				read = context.创建函数(name, 0, new ManagedCallback() {
					@Override
					public QJS值 处理调用(QJS值 receiver, QJS参数 args) {
						try {
							return context.fromJava(field == null ? getter.invoke(target) : field.get(target));
						} catch (ReflectiveOperationException error) {
							throw failure(error);
						}
					}
				});
			}
			if (setter != null || field != null && !Modifier.isFinal(field.getModifiers())) {
				write = context.创建函数(name, 1, new ManagedCallback() {
					@Override
					public QJS值 处理调用(QJS值 receiver, QJS参数 args) {
						List<QJS值> temporary = new ArrayList<QJS值>();
						try {
							Class<?> type = field == null ? setter.getParameterTypes()[0] : field.getType();
							Object value = argument(args.取值(0), type, temporary, 0);
							if (field == null) {
								setter.invoke(target, value);
							} else {
								field.set(target, value);
							}
							return null;
						} catch (ReflectiveOperationException error) {
							throw failure(error);
						} finally {
							for (QJS值 value : temporary) {
								value.释放();
							}
						}
					}
				});
			}
			context.apply("accessor", object, name, read, write).释放();
		} finally {
			if (read != null) {
				read.释放();
			}
			if (write != null) {
				write.释放();
			}
		}
	}

	/**
	 * 按参数类型选择唯一重载，失败时明确报告，不依赖反射枚举顺序。
	 *
	 * @param context   所属实例。
	 * @param target    宿主对象。
	 * @param methods   同名候选方法。
	 * @param arguments 本次借用参数。
	 * @return 拥有引用的返回值。
	 */
	private static QJS值 invoke(QJS实例 context, Object target, List<Method> methods, QJS参数 arguments) {
		Method selected = null;
		int best = Integer.MAX_VALUE;
		boolean ambiguous = false;
		for (Method method : methods) {
			Class<?>[] types = method.getParameterTypes();
			if (method.isVarArgs() ? arguments.取数量() < types.length - 1 : arguments.取数量() != types.length) {
				continue;
			}
			int score = method.isVarArgs() ? 8 : 0;
			for (int i = 0; i < arguments.取数量(); i++) {
				Class<?> type = method.isVarArgs() && i >= types.length - 1 ? types[types.length - 1].getComponentType()
						: types[i];
				int part = score(arguments.取值(i), type);
				if (part < 0) {
					score = Integer.MAX_VALUE;
					break;
				}
				score += part;
			}
			if (score < best) {
				selected = method;
				best = score;
				ambiguous = false;
			} else if (selected != null && score == best) {
				ambiguous = true;
			}
		}
		if (selected == null || ambiguous) {
			throw new QJS错误(ambiguous ? "宿主方法重载不明确" : "没有匹配参数类型及数量的宿主方法");
		}

		List<QJS值> temporary = new ArrayList<QJS值>();
		try {
			Class<?>[] types = selected.getParameterTypes();
			Object[] actual = new Object[types.length];
			for (int i = 0; i < types.length; i++) {
				if (selected.isVarArgs() && i == types.length - 1) {
					int count = arguments.取数量() - i;
					Object array = Array.newInstance(types[i].getComponentType(), count);
					for (int j = 0; j < count; j++) {
						Array.set(array, j, argument(arguments.取值(i + j), types[i].getComponentType(), temporary, 0));
					}
					actual[i] = array;
				} else {
					actual[i] = argument(arguments.取值(i), types[i], temporary, 0);
				}
			}
			Object result = selected.invoke(target, actual);
			return selected.getReturnType() == Void.TYPE ? null : context.fromJava(result);
		} catch (ReflectiveOperationException error) {
			throw failure(error);
		} finally {
			for (QJS值 value : temporary) {
				value.释放();
			}
		}
	}

	/**
	 * 计算实参与声明类型的匹配程度，数字重载优先保留原精度。
	 *
	 * @param value JS 值。
	 * @param type  声明类型。
	 * @return 非负匹配分数，负数表示不支持。
	 */
	private static int score(QJS值 value, Class<?> type) {
		int kind = value.取类型();
		if (type.isInstance(value)) {
			return type == Object.class ? 20 : 0;
		}
		if (type == Variant.class || type == Object.class) {
			return 20;
		}
		if (kind == 0 || kind == 1) {
			return type.isPrimitive() ? -1 : 4;
		}
		if (type == String.class || type == Character.class || type == Character.TYPE) {
			return kind == 4 ? 0 : -1;
		}
		if (type == Boolean.class || type == Boolean.TYPE) {
			return kind == 2 ? 0 : -1;
		}
		if (kind == 8 && (type.isArray() || type == 集合.class) || kind == 7 && type == 哈希表.class) {
			return 1;
		}
		if (type == byte[].class && (kind == 11 || kind == 12)) {
			return 0;
		}
		if (kind == 5) {
			return type == Long.TYPE || type == Long.class ? 0 : -1;
		}
		if (kind == 3 && (type.isPrimitive() || Number.class.isAssignableFrom(type))) {
			double number = value.到小数();
			boolean integer = !Double.isNaN(number) && !Double.isInfinite(number) && number == Math.rint(number);
			if (type == Double.TYPE || type == Double.class) {
				return integer ? 3 : 0;
			}
			if (type == Float.TYPE || type == Float.class) {
				return integer ? 4 : 1;
			}
			if (!integer) {
				return -1;
			}
			if (type == Integer.TYPE || type == Integer.class) {
				return number >= Integer.MIN_VALUE && number <= Integer.MAX_VALUE ? 0 : -1;
			}
			if (type == Long.TYPE || type == Long.class) {
				return Math.abs(number) <= 9007199254740991.0 ? 1 : -1;
			}
			if (type == Short.TYPE || type == Short.class) {
				return number >= Short.MIN_VALUE && number <= Short.MAX_VALUE ? 2 : -1;
			}
			if (type == Byte.TYPE || type == Byte.class) {
				return number >= Byte.MIN_VALUE && number <= Byte.MAX_VALUE ? 2 : -1;
			}
		}
		return -1;
	}

	/**
	 * 将已匹配参数转换为 Java 声明类型，特殊 JS 值仍作为借用值使用。
	 *
	 * @param value     JS 值。
	 * @param type      目标类型。
	 * @param temporary 转换产生的特殊值引用。
	 * @param depth     数组嵌套深度。
	 * @return Java 参数。
	 */
	private static Object argument(QJS值 value, Class<?> type, List<QJS值> temporary, int depth) {
		if (depth > 128 || score(value, type) < 0) {
			throw new QJS错误("宿主参数类型不匹配：" + type.getName());
		}
		if (QJS值.class.isAssignableFrom(type)) {
			return value;
		}
		int kind = value.取类型();
		if ((kind == 0 || kind == 1) && type != Variant.class) {
			return null;
		}
		if (type == Variant.class || type == Object.class || type == 集合.class || type == 哈希表.class) {
			Variant converted = ValueConversion.toSimple(value, temporary);
			Object result = value.context.unbox(converted);
			if (type == Variant.class) {
				return converted;
			}
			if (result != null && type != Object.class && !type.isInstance(result)) {
				throw new QJS错误("宿主参数不能转换为：" + type.getName());
			}
			return result;
		}
		if (type == String.class) {
			return value.到文本();
		}
		if (type == Character.class || type == Character.TYPE) {
			String text = value.到文本();
			if (text.length() != 1) {
				throw new QJS错误("字符参数须恰好包含一个 UTF-16 字符");
			}
			return text.charAt(0);
		}
		if (type == Boolean.class || type == Boolean.TYPE) {
			return value.到逻辑();
		}
		if (type == byte[].class && (kind == 11 || kind == 12)) {
			return value.到字节集();
		}
		if (type.isArray()) {
			QJS数组 array = value.到数组();
			long length = array.取长度();
			if (length > 100000) {
				throw new QJS错误("宿主数组参数不能超过 100000 项");
			}
			Object result = Array.newInstance(type.getComponentType(), (int) length);
			for (int i = 0; i < length; i++) {
				QJS值 item = array.取项目(i);
				try {
					Array.set(result, i, argument(item, type.getComponentType(), temporary, depth + 1));
				} finally {
					item.释放();
				}
			}
			return result;
		}
		if (type == Double.TYPE || type == Double.class) {
			return value.到小数();
		}
		if (type == Float.TYPE || type == Float.class) {
			return (float) value.到小数();
		}
		if (type == Long.TYPE || type == Long.class) {
			return value.到长整数();
		}
		if (type == Integer.TYPE || type == Integer.class) {
			return value.到整数();
		}
		if (type == Short.TYPE || type == Short.class) {
			return (short) value.到整数();
		}
		if (type == Byte.TYPE || type == Byte.class) {
			return (byte) value.到整数();
		}
		throw new QJS错误("不支持的宿主参数类型：" + type.getName());
	}

	/**
	 * 恢复宿主实际异常，避免吞掉异常后返回伪造结果。
	 *
	 * @param error 反射异常。
	 * @return 可传播的运行错误。
	 */
	private static RuntimeException failure(ReflectiveOperationException error) {
		Throwable cause = error instanceof InvocationTargetException ? error.getCause() : error;
		if (cause instanceof RuntimeException) {
			return (RuntimeException) cause;
		}
		if (cause instanceof Error) {
			throw (Error) cause;
		}
		return new QJS错误("宿主调用失败：" + cause);
	}
}
