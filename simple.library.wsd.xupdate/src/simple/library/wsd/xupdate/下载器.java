package simple.library.wsd.xupdate;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 替换 APK 下载实现；进度、结果及错误必须通过本次下载回调返回。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface 下载器 {

	/**
	 * 开始下载，不执行版本检查或安装。
	 *
	 * @param info 地址、大小和校验信息。
	 * @param callback 本次下载的结果回调。
	 */
	@SimpleFunction
	void 下载(更新信息 info, 下载回调 callback);

	/**
	 * 取消当前下载。
	 */
	@SimpleFunction
	void 取消();

	/**
	 * 将当前下载转入此下载器支持的后台执行方式。
	 */
	@SimpleFunction
	void 转入后台();
}
