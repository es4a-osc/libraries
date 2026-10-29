package simple.library.wsd.xupdate;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 接管更新提示界面；调用方可使用自己的组件展示信息并决定何时下载。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface 提示器 {

	/**
	 * 显示更新提示；后续下载可调用更新器的下载更新。
	 *
	 * @param info 与活动流程隔离的更新信息。
	 */
	@SimpleFunction
	void 显示(更新信息 info);
}
