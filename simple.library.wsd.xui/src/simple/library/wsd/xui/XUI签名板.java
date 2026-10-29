package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI签名板组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI签名板 extends XUI视图组件 {

	/**
	 * 清除画板。
	 */
	@SimpleFunction
	void 清空();

	/**
	 * 设置画笔宽度 默认宽度为10px。
	 *
	 * @param penSize 单位：px。
	 */
	@SimpleFunction
	void 置画笔尺寸(float penSize);

	/**
	 * 设置是否画边框。
	 *
	 * @param hasBorder 是否画边框。
	 */
	@SimpleFunction
	void 启用边框(boolean hasBorder);

	/**
	 * 设置签名板的背景颜色。
	 *
	 * @param backColor 签名板的背景颜色。
	 */
	@SimpleFunction
	void 置画板背景颜色(int backColor);

	/**
	 * 设置画笔颜色。
	 *
	 * @param penColor 画笔颜色。
	 */
	@SimpleFunction
	void 置画笔颜色(int penColor);

	/**
	 * 设置签名板的尺寸。
	 *
	 * @param width 宽。
	 * @param height 高。
	 */
	@SimpleFunction
	void 置视图尺寸(int width, int height);

	/**
	 * 将签名保存为 PNG；写入失败引发文件未存在错误。
	 *
	 * @param path PNG 输出路径；相对路径以应用内部文件目录为基准，目录须已存在。
	 */
	@SimpleFunction
	void 保存签名(String path);

	/**
	 * 取得画板是否已有手写内容。
	 *
	 * @return 画板是否已有手写内容。
	 */
	@SimpleFunction
	boolean 是否已绘制();
}
