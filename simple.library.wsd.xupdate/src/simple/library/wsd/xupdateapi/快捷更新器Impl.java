package simple.library.wsd.xupdateapi;

import android.content.Context;

import com.xuexiang.xupdate.UpdateManager;
import com.xuexiang.xupdate.easy.EasyUpdate;

import simple.library.wsd.xupdate.下载器;
import simple.library.wsd.xupdate.UpdateController;
import simple.runtime.components.组件容器;

/**
 * 将 EasyUpdate 的入口和可选 Aria 策略接入核心实现，不复制更新流程或界面状态。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 快捷更新器Impl extends UpdateController implements 快捷更新器 {

	private boolean resumable;

	/**
	 * 创建绑定到所属窗口的快捷更新器。
	 *
	 * @param container 所属 Simple 容器。
	 */
	public 快捷更新器Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected UpdateManager.Builder createUpdateBuilder(Context context, String address) {
		return EasyUpdate.create(context, address);
	}

	@Override
	public boolean 断点续传() {
		return resumable;
	}

	@Override
	public void 断点续传(boolean value) {
		super.置下载器(value ? new Aria下载器() : null);
		resumable = value;
	}

	@Override
	public void 置下载器(下载器 value) {
		super.置下载器(value);
		resumable = value instanceof Aria下载器;
	}
}
