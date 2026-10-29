package simple.library.wsd.xupdate;

import simple.runtime.components.组件容器;

/**
 * 核心更新器的唯一组件实现，默认使用 XUpdate 和普通流式下载。
 * 共享行为由未导出的抽象实现维护，不依赖 XUpdateAPI。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 更新器Impl extends UpdateController implements 更新器 {

	/**
	 * 创建绑定到所属窗口的更新器。
	 *
	 * @param container 所属 Simple 容器。
	 */
	public 更新器Impl(组件容器 container) {
		super(container);
	}
}
