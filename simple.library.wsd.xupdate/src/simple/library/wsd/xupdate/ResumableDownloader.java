package simple.library.wsd.xupdate;

import java.io.File;
import java.util.Map;

/**
 * 可暂停下载器的 Java 适配契约；核心只依赖此能力，不识别具体下载引擎。
 * 此接口不导出到 Simple，Simple 调用方使用更新器的暂停、继续和事件。
 *
 * @author 树先生 xhwsd@qq.com
 */
public interface ResumableDownloader extends 下载器 {

	/**
	 * 固定本次任务的私有文件和请求配置；在首次下载前调用。
	 *
	 * @param target 本次任务的完整目标文件。
	 * @param headers 请求头快照，不得修改调用方集合。
	 * @param timeout 最长无进展等待毫秒数。
	 */
	void configure(File target, Map<String, String> headers, int timeout);

	/**
	 * 请求停止实际传输，确认停止后才能恢复。
	 *
	 * @param complete 停止完成回调，在界面线程执行。
	 */
	void pause(Runnable complete);
}
