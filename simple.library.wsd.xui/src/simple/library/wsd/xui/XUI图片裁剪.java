package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * XUI图片裁剪组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface XUI图片裁剪 extends XUI视图组件 {

	/**
	 * 从文件路径加载待裁剪图片。
	 *
	 * @param imagePath 图片的资源路径。
	 */
	@SimpleFunction
	void 置图片路径(String imagePath);

	/**
	 * 设置裁剪覆盖层视图可见性。
	 *
	 * @param visibility 可见性。
	 */
	@SimpleFunction
	void 置裁剪覆盖层视图可见性(int visibility);

	/**
	 * 设置剪切类型 false: 自由剪切 true : 固定大小比例剪切。
	 *
	 * @param fixAspectRatio 是否固定裁剪宽高比例。
	 */
	@SimpleFunction
	void 置固定宽高比例(boolean fixAspectRatio);

	/**
	 * 设置辅助线。
	 *
	 * @param guidelines 辅助线。
	 */
	@SimpleFunction
	void 置辅助线(int guidelines);

	/**
	 * 设置固定裁剪宽高比例；须先启用固定宽高比例。
	 *
	 * @param aspectRatioX 宽度比例。
	 * @param aspectRatioY 高度比例。
	 */
	@SimpleFunction
	void 置宽高比例(int aspectRatioX, int aspectRatioY);

	/**
	 * 将当前裁剪区域保存为 PNG。
	 *
	 * @param path PNG 输出路径；相对路径以应用内部文件目录为基准，目录须已存在。
	 */
	@SimpleFunction
	void 保存裁剪(String path);

	/**
	 * 设置待裁剪图片；不自动下载网络图片。
	 *
	 * @param image 图片资源标识、资产文件名或外部文件路径。
	 */
	@SimpleFunction
	void 置图片(String image);

	/**
	 * 旋转待裁剪图片。
	 *
	 * @param degrees 旋转角度。
	 */
	@SimpleFunction
	void 旋转图片(int degrees);

	/**
	 * 沿指定方向镜像当前图片。
	 *
	 * @param horizontal 真为左右镜像，假为上下镜像。
	 */
	@SimpleFunction
	void 镜像图片(boolean horizontal);

	/**
	 * 取得原图像素坐标的左、上、右、下四项裁剪边界，须在图片完成布局后读取。
	 *
	 * @return 原图像素坐标的左、上、右、下四项裁剪边界，须在图片完成布局后读取。
	 */
	@SimpleFunction
	float[] 取裁剪区域();

	/**
	 * 隐藏裁剪辅助线。
	 */
	@SimpleDataElement
	int 辅助线_隐藏 = 0;

	/**
	 * 触摸显示裁剪辅助线。
	 */
	@SimpleDataElement
	int 辅助线_触摸显示 = 1;

	/**
	 * 始终显示裁剪辅助线。
	 */
	@SimpleDataElement
	int 辅助线_始终显示 = 2;

}
