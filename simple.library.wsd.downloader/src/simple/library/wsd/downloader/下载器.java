package simple.library.wsd.downloader;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.UsesPermissions;
import simple.runtime.collections.集合;
import simple.runtime.components.组件;

/**
 * HTTP/HTTPS 文件下载器，提供异步下载、暂停、继续、取消及应用私有断点记录。
 * 同一应用中的组件实例共享任务和事件；最后一个实例销毁时保存并暂停活动任务。
 * 进程被强制结束后，下次创建组件时将记录中的活动任务恢复为暂停状态。
 * 不提供进程退出后的后台驻留下载。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
@SimpleComponent
@UsesPermissions(
	permissionNames = "android.permission.INTERNET"
)
public interface 下载器 extends 组件 {

	/**
	 * 在主线程通知下载进度；传输开始时通知一次，传输中约每 200 毫秒检查并通知。
	 * 下载完成时额外通知最终进度，不受上述间隔限制。
	 *
	 * @param taskId 稳定任务编号，从 1 开始。
	 * @param total 文件总字节数；服务器未提供长度时为 -1。
	 * @param downloaded 已写入临时文件的字节数。
	 * @param percent 整数百分比；总长度未知时为 -1，完成时为 100。
	 */
	@SimpleEvent
	void 进度改变(int taskId, long total, long downloaded, int percent);

	/**
	 * 下载成功或失败时在主线程通知；暂停和取消不触发完成事件。
	 *
	 * @param taskId 稳定任务编号。
	 * @param success 是否已完整下载并发布到目标路径；失败原因见取任务信息。
	 */
	@SimpleEvent
	void 下载完毕(int taskId, boolean success);

	/**
	 * 开始异步下载，文件以分块流式写入；目标已经存在时拒绝覆盖。
	 * 相同网址和目标路径的未完成任务会继续原任务；服务器忽略续传时从头下载。
	 *
	 * @param address HTTP/HTTPS 完整网址，中文网址按 URL 规则编码。
	 * @param path 保存文件的绝对路径，或相对于应用内部文件目录的路径；相对路径不能越出该目录。
	 * @return 稳定任务编号，从 1 开始。
	 * @throws IllegalStateException 下载器已销毁、网址或路径无效、目标冲突，或任务记录保存失败。
	 */
	@SimpleFunction
	int 开始下载(String address, String path);

	/**
	 * 暂停任务并保留临时文件；等待或下载中的任务切换为暂停，其它状态保持不变。
	 * 调用返回后停止写入；阻塞的网络请求在返回或连接、读取超时后收尾。
	 *
	 * @param taskId 已存在的稳定任务编号。
	 * @throws IllegalArgumentException 任务编号不存在。
	 * @throws IllegalStateException 下载器已销毁或任务记录保存失败。
	 */
	@SimpleFunction
	void 暂停下载(int taskId);

	/**
	 * 继续暂停或失败的任务，恢复点以临时文件实际长度为准。
	 * 没有可验证的服务器文件标识时重新下载；已完成或取消的任务拒绝继续。
	 *
	 * @param taskId 已存在的稳定任务编号。
	 * @throws IllegalArgumentException 任务编号不存在。
	 * @throws IllegalStateException 下载器已销毁、任务已完成或取消、保存目录不可用，或任务记录保存失败。
	 */
	@SimpleFunction
	void 继续下载(int taskId);

	/**
	 * 取消未完成任务并清理其临时文件；传输尚未收尾时延后清理。
	 * 已完成或取消的任务保持不变，已完成的目标文件不会被删除。
	 *
	 * @param taskId 已存在的稳定任务编号。
	 * @throws IllegalArgumentException 任务编号不存在。
	 * @throws IllegalStateException 下载器已销毁、临时文件删除失败，或任务记录保存失败。
	 */
	@SimpleFunction
	void 取消下载(int taskId);

	/**
	 * 返回等待、下载、暂停或失败任务的网址，顺序按稳定编号；不包含已完成或取消任务。
	 * 同网址不同目标路径可能重复，继续时提供原网址和原目标路径调用开始下载。
	 *
	 * @return 元素为文本型网址的集合；没有未完成任务时为空集合。
	 * @throws IllegalStateException 下载器已销毁。
	 */
	@SimpleFunction
	集合 取未完成记录();

	/**
	 * 读取任务状态和错误，不执行网络请求。
	 *
	 * @param taskId 已存在的稳定任务编号。
	 * @return 六项文本数组，索引 0 至 5 依次为网址、保存绝对路径、状态、总字节数、已下载字节数、错误说明。
	 * 状态为等待、下载、暂停、完成、失败或取消；未知总字节数为 -1，没有错误时错误说明为空文本。
	 * @throws IllegalArgumentException 任务编号不存在。
	 * @throws IllegalStateException 下载器已销毁。
	 */
	@SimpleFunction
	String[] 取任务信息(int taskId);
}
