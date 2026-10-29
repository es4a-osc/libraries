package simple.library.wsd.xui;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.UsesPermissions;

/**
 * XUI自适应列表组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public interface XUI自适应列表 extends XUI列表 {

	/**
	 * 限制内容自适应后的最大高度。
	 *
	 * @param height 允许的最大高度，单位为物理像素，不得为负。
	 */
	@SimpleFunction
	void 置最大高度(int height);

	/**
	 * 设置带说明和可选图片的列表条目；三个数组长度须相同。
	 *
	 * @param titles 项目标题。
	 * @param details 项目说明。
	 * @param images 本地或网络图片标识；空文本不显示图片。
	 */
	@SimpleFunction
	void 置图文项目(String[] titles, String[] details, String[] images);

	/**
	 * 更新一项完整图文内容，保留选择状态。
	 *
	 * @param index 项目索引。
	 * @param title 主文本。
	 * @param detail 说明文本。
	 * @param image 本地图片标识或网络地址，空文本不显示图片。
	 */
	@SimpleFunction
	void 置图文项目内容(int index, String title, String detail, String image);

	/**
	 * 在末尾添加图文项目。
	 *
	 * @param title 主文本。
	 * @param detail 说明文本。
	 * @param image 本地图片标识或网络地址，空文本不显示图片。
	 */
	@SimpleFunction
	void 添加图文项目(String title, String detail, String image);
}
