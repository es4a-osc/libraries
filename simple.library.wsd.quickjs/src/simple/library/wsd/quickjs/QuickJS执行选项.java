package simple.library.wsd.quickjs;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 脚本及模块执行选项；调用时读取快照，不随后续修改改变已经开始的执行。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class QuickJS执行选项 {
	private boolean strict;
	private boolean async;
	private boolean barrier;
	private boolean main = true;
	private String url = "";

	/**
	 * 创建使用引擎默认行为的执行选项。
	 */
	public QuickJS执行选项() {
	}

	/**
	 * 设置是否强制严格模式。
	 *
	 * @param enabled 是否启用。
	 */
	@SimpleFunction
	public synchronized void 置严格模式(boolean enabled) {
		strict = enabled;
	}

	/**
	 * 设置普通脚本是否允许顶层 await；模块本身支持顶层 await。
	 *
	 * @param enabled 是否启用。
	 */
	@SimpleFunction
	public synchronized void 置允许等待(boolean enabled) {
		async = enabled;
	}

	/**
	 * 设置异常堆栈是否在本次执行边界截断。
	 *
	 * @param enabled 是否截断。
	 */
	@SimpleFunction
	public synchronized void 置截断堆栈(boolean enabled) {
		barrier = enabled;
	}

	/**
	 * 设置入口模块的 import.meta.main。
	 *
	 * @param enabled 是否为主模块。
	 */
	@SimpleFunction
	public synchronized void 置模块入口(boolean enabled) {
		main = enabled;
	}

	/**
	 * 设置入口模块的 import.meta.url；空文本使用模块标识。
	 *
	 * @param address 模块地址。
	 */
	@SimpleFunction
	public synchronized void 置模块地址(String address) {
		url = QuickJS实例.require(address);
	}

	/**
	 * 复制选项，保证一次调用使用同一份状态。
	 *
	 * @return 独立快照。
	 */
	synchronized QuickJS执行选项 snapshot() {
		QuickJS执行选项 copy = new QuickJS执行选项();
		copy.strict = strict;
		copy.async = async;
		copy.barrier = barrier;
		copy.main = main;
		copy.url = url;
		return copy;
	}

	/**
	 * 计算引擎执行标志，模块不使用普通脚本的异步标志。
	 *
	 * @param module 是否为模块。
	 * @return 引擎标志。
	 */
	int flags(boolean module) {
		return (strict ? 8 : 0) | (barrier ? 64 : 0) | (async && !module ? 128 : 0);
	}

	/**
	 * 建立入口模块的元信息选项。
	 *
	 * @param context 所属实例。
	 * @return 独立 JS 对象。
	 */
	QuickJS对象 metadata(QuickJS实例 context) {
		QuickJS对象 result = context.创建对象();
		try {
			result.置简单成员("url", simple.runtime.variants.StringVariant.getStringVariant(url));
			result.置简单成员("main", simple.runtime.variants.BooleanVariant.getBooleanVariant(main));
			return result;
		} catch (RuntimeException | Error error) {
			result.释放();
			throw error;
		}
	}
}
