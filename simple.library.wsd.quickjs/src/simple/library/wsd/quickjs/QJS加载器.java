package simple.library.wsd.quickjs;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 可继承的模块名称规范化和动态源码/字节码加载适配器。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public class QJS加载器 {
	/**
	 * 创建可继承的宿主适配对象。
	 */
	public QJS加载器() {
	}

	/**
	 * 相对名称按导入者目录解析，普通名称保持不变。
	 *
	 * @param base 导入者模块标识。
	 * @param name import 指定的模块名。
	 * @return 用于缓存及加载的规范模块标识。
	 */
	@SimpleFunction
	public String 规范名称(String base, String name) {
		if (name.startsWith("./") || name.startsWith("../")) {
			return java.net.URI.create(base).resolve(name).normalize().toString();
		}
		return name;
	}

	/**
	 * 返回模块源码或同版本可信字节码，未找到时抛出错误。
	 *
	 * @param name 规范化后的模块标识。
	 * @return 包含源码或字节码的模块对象。
	 */
	@SimpleFunction
	public QJS模块 加载模块(String name) {
		throw new QJS错误("未找到模块：" + name);
	}

	/**
	 * 配置模块的 import.meta；默认已设置 url 和 main，可按实际来源补充或覆盖。
	 *
	 * @param name 规范模块标识。
	 * @param information 本次回调的借用元信息对象，长期保存时须保留。
	 */
	@SimpleFunction
	public void 初始化元信息(String name, QJS对象 information) {
	}
}
