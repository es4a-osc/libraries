package simple.library.wsd.imagebrowser;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;
import simple.runtime.annotations.UsesPermissions;
import simple.runtime.components.组件;

/**
 * 在独立浏览界面中查看静态图片，支持滑动翻页、双击和双指缩放。
 *
 * <p>所有成员在界面线程调用。每个实例同时打开一个浏览界面，索引从 0 开始。
 * 图片异步读取；支持资产名、@资源类型/名称、绝对路径、file URI、已授权的
 * content URI 和 HTTP/HTTPS 网址，不申请存储权限。单图输入上限 20 MiB，
 * 按尺寸采样到宽高不超过 2048 像素；不是原图无损查看或 GIF 播放器。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
@UsesPermissions(permissionNames =
	// 允许通过 HTTP/HTTPS 载入网络图片。
	"android.permission.INTERNET"
)
public interface 图片浏览器 extends 组件 {

	/* 组件事件 */

	/**
	 * 当前页改变时触发，此时图片可能仍在载入；首次打开也会触发。
	 *
	 * @param index 当前图片索引，从 0 开始。
	 */
	@SimpleEvent
	void 图片改变(int index);

	/**
	 * 当前页图片载入成功时触发，过期请求不触发。
	 *
	 * @param index 图片索引，从 0 开始。
	 */
	@SimpleEvent
	void 载入成功(int index);

	/**
	 * 参数、界面创建或当前页图片载入失败时触发，不关闭已经打开的浏览界面。
	 *
	 * @param index 图片索引；无法确定图片时为 -1。
	 * @param message 失败原因。
	 */
	@SimpleEvent
	void 载入失败(int index, String message);

	/**
	 * 通过关闭按钮、系统返回或关闭方法结束浏览时触发；销毁组件不派发此事件。
	 *
	 * @param index 关闭前的图片索引，从 0 开始。
	 */
	@SimpleEvent
	void 浏览关闭(int index);

	/* 组件属性 */

	/**
	 * 查询浏览界面是否已打开。
	 *
	 * @return 已打开为真。
	 */
	@SimpleProperty
	boolean 正在浏览();

	/**
	 * 读取当前页索引。
	 *
	 * @return 从 0 开始的索引，没有打开浏览界面时为 -1。
	 */
	@SimpleProperty
	int 当前索引();

	/**
	 * 读取最近一次失败说明；有效请求开始或当前页成功后清空。
	 *
	 * @return 失败说明，没有错误时为空文本。
	 */
	@SimpleProperty
	String 错误信息();

	/* 组件方法 */

	/**
	 * 打开单张图片；已经打开时替换原图片列表。
	 *
	 * @param image 图片来源。
	 */
	@SimpleFunction
	void 浏览图片(String image);

	/**
	 * 打开图片列表并定位到指定图片，复制数组后使用。
	 *
	 * @param images 图片来源数组，最多 1000 项，不能为空且每项不能为空文本。
	 * @param startIndex 起始图片索引，从 0 开始；越界通过失败事件反馈。
	 */
	@SimpleFunction
	void 浏览图片(String[] images, int startIndex);

	/**
	 * 切换到指定图片；尚未打开或索引越界时通过失败事件反馈。
	 *
	 * @param index 图片索引，从 0 开始。
	 */
	@SimpleFunction
	void 切换图片(int index);

	/**
	 * 重新读取当前页，适用于网络恢复或文件内容发生变化之后。
	 */
	@SimpleFunction
	void 重新载入();

	/**
	 * 关闭浏览界面并取消载入；未打开时不执行。
	 */
	@SimpleFunction
	void 关闭();
}
