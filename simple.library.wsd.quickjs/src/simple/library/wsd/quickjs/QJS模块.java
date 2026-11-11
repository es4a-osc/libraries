package simple.library.wsd.quickjs;

import java.util.LinkedHashMap;
import java.util.Map;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 模块源码、可信字节码或宿主导出快照；宿主导出持有 JS 引用，使用后释放或销毁所属实例。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class QJS模块 {

	private byte[] payload = new byte[0];
	private boolean bytecode;
	private volatile QJS实例 owner;
	private Map<String, QJS值> exports;

	/**
	 * 创建空源码模块。
	 */
	public QJS模块() {
	}

	/**
	 * 生成 JNI 约定的编码标识及内容副本。
	 *
	 * @return 模块内容。
	 */
	synchronized byte[] nativeContent() {
		if (exports != null) {
			return new byte[] { 2 };
		}
		byte[] result = new byte[payload.length + 1];
		result[0] = (byte) (bytecode ? 1 : 0);
		System.arraycopy(payload, 0, result, 1, payload.length);
		return result;
	}

	/**
	 * 替换模块源码。
	 *
	 * @param source 完整 ES 模块源码。
	 */
	@SimpleFunction
	public void 置源码(String source) {
		replace(QJS实例.require(source).getBytes(java.nio.charset.StandardCharsets.UTF_8), false);
	}

	/**
	 * 核对并保存模块字节码副本。
	 *
	 * @param code 本库同版本生成、来源可信的模块字节码。
	 */
	@SimpleFunction
	public void 置字节码(byte[] code) {
		byte[] data = QJS实例.unpack(code, 3);
		replace(data, true);
	}

	/**
	 * 取得源码文本，字节码模块不提供反编译。
	 *
	 * @return 模块保存的源码文本。
	 */
	@SimpleFunction
	public String 取源码() {
		synchronized (this) {
			if (bytecode || exports != null) {
				throw new QJS错误("当前模块没有源码文本");
			}
			return new String(payload, java.nio.charset.StandardCharsets.UTF_8);
		}
	}

	/**
	 * 读取字节码副本；源码模块尚未编译时报告错误。
	 *
	 * @return 模块字节码的独立副本。
	 */
	@SimpleFunction
	public byte[] 取字节码() {
		synchronized (this) {
			if (!bytecode || exports != null) {
				throw new QJS错误("模块尚未编译为字节码");
			}
			return QJS实例.pack(payload, 3);
		}
	}

	/**
	 * 判断当前模块是否保存字节码。
	 *
	 * @return 模块保存字节码时为真，否则为假。
	 */
	@SimpleFunction
	public boolean 是否字节码() {
		synchronized (this) {
			return exports == null && bytecode;
		}
	}

	/**
	 * 设置宿主模块导出，复制自有可枚举字符串成员的引用；default 作为默认导出。 后续替换标量成员不影响快照，已导出对象和函数仍保持同一 JS 身份。
	 *
	 * @param object 导出对象，来自同一运行时；不可在成员名中使用零字符。
	 */
	@SimpleFunction
	public void 置导出(QJS对象 object) {
		if (object == null) {
			throw new QJS错误("模块导出对象不能为空");
		}
		QJS实例 context = object.context;
		context.runtime.checkAccess();
		synchronized (context.runtime.lock) {
			if (owner != null && owner.runtime != context.runtime) {
				throw new QJS错误("宿主模块不能跨运行时使用");
			}
			Map<String, QJS值> copy = new LinkedHashMap<String, QJS值>();
			QJS数组 keys = object.取可枚举键();
			try {
				for (long i = 0; i < keys.取长度(); i++) {
					QJS值 key = keys.取项目(i);
					try {
						String name = key.到文本();
						if (name.indexOf(0) >= 0) {
							throw new QJS错误("模块导出名不能包含零字符");
						}
						copy.put(name, object.取成员(name));
					} finally {
						key.释放();
					}
				}
				synchronized (this) {
					clearExports();
					owner = context;
					exports = copy;
					copy = null;
				}
			} finally {
				keys.释放();
				if (copy != null) {
					for (QJS值 value : copy.values()) {
						value.释放();
					}
				}
			}
		}
	}

	/**
	 * 判断模块是否使用宿主导出快照。
	 *
	 * @return 使用宿主导出时为真。
	 */
	@SimpleFunction
	public synchronized boolean 是否宿主模块() {
		return exports != null;
	}

	/**
	 * 释放持有的宿主导出快照；已经加载的模块不受影响。
	 */
	@SimpleFunction
	public void 释放() {
		replace(new byte[0], false);
	}

	/**
	 * 在固定的运行时锁、模块锁顺序下替换内容。
	 *
	 * @param data     新内容。
	 * @param compiled 是否为字节码。
	 */
	private void replace(byte[] data, boolean compiled) {
		QJS实例 context = owner;
		if (context != null) {
			context.runtime.checkAccess();
			synchronized (context.runtime.lock) {
				synchronized (this) {
					clearExports();
					payload = data;
					bytecode = compiled;
				}
			}
		} else {
			synchronized (this) {
				if (owner != null) {
					throw new QJS错误("模块内容正在改变，请串行设置");
				}
				payload = data;
				bytecode = compiled;
			}
		}
	}

	/**
	 * 释放旧快照，调用方已取得所属运行时和模块锁。
	 */
	private void clearExports() {
		if (exports != null) {
			for (QJS值 value : exports.values()) {
				value.释放();
			}
			exports = null;
		}
	}

	/**
	 * 为模块加载创建独立快照，只有同一运行时的实例可以复制引用。
	 *
	 * @param context 目标实例，调用方已取得运行时锁。
	 * @return 独立拥有引用的导出表。
	 */
	synchronized Map<String, QJS值> snapshot(QJS实例 context) {
		if (exports == null || owner.runtime != context.runtime) {
			throw new QJS错误("宿主模块不存在或属于其它运行时");
		}
		Map<String, QJS值> result = new LinkedHashMap<String, QJS值>();
		try {
			for (Map.Entry<String, QJS值> entry : exports.entrySet()) {
				result.put(entry.getKey(), entry.getValue().复制到(context));
			}
			return result;
		} catch (RuntimeException | Error error) {
			for (QJS值 value : result.values()) {
				value.释放();
			}
			throw error;
		}
	}
}
