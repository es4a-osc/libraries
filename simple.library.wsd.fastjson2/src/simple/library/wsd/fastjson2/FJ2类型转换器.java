package simple.library.wsd.fastjson2;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.variants.Variant;

/**
 * 普通业务对象的同步编解码接口，注册在独立读写配置中；不得在回调中重入相同类型的转换。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface FJ2类型转换器 {

	/**
	 * 将注册类型的普通对象转换为自定义协议值树。
	 *
	 * @param value 注册类型的普通对象，可赋给对应 Simple 类型。
	 * @return 按自定义协议构造的值树，空引用表示 JSON null。
	 */
	@SimpleFunction
	FJ2值 编码(Variant value);

	/**
	 * 按自定义协议创建注册类型的新对象。
	 *
	 * @param value 当前对象对应的 JSON 或 JSONB 值快照。
	 * @return 注册类型的新对象，允许返回空引用。
	 */
	@SimpleFunction
	Variant 解码(FJ2值 value);
}
