package simple.library.wsd.fastjson2;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 同步属性过滤接口；回调接收独立快照，不得重入同一过滤器。保留的属性名不能重复，被忽略的属性不参与重名判断。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface FJ2过滤器 {

	/**
	 * 决定是否输出当前属性；数组元素不调用此回调。
	 *
	 * @param name  已完成名称转换的属性名。
	 * @param value 属性值的独立副本。
	 * @return 保留该属性时为真，忽略该属性时为假。
	 */
	@SimpleFunction
	boolean 保留属性(String name, FJ2值 value);

	/**
	 * 将原属性名转换为输出属性名；仅保留的属性参与重名判断。
	 *
	 * @param name 原属性名。
	 * @return 输出使用的新属性名，不得为空引用。
	 */
	@SimpleFunction
	String 转换名称(String name);

	/**
	 * 提供当前属性的输出值，不直接修改源文档。
	 *
	 * @param name  已完成名称转换的属性名。
	 * @param value 属性值的独立副本。
	 * @return 要输出的值，空引用表示 JSON null。
	 */
	@SimpleFunction
	FJ2值 转换值(String name, FJ2值 value);
}
