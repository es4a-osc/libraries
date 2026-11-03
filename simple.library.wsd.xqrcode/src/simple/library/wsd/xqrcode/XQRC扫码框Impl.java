package simple.library.wsd.xqrcode;

import android.os.Looper;
import android.view.View;
import simple.library.wsd.xqrcode.internal.ScanView;
import simple.runtime.android.MainActivity;
import simple.runtime.components.impl.android.视图组件;
import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * 将独立预览的会话及生命周期接入 Simple 视图组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XQRC扫码框Impl extends 视图组件 implements XQRC扫码框,
		MainActivity.OnResumeListener, MainActivity.OnPauseListener,
		MainActivity.OnDestroyListener, MainActivity.OnRequestPermissionsResultListener {
	private final MainActivity host;
	private boolean destroyed;

	/**
	 * 创建扫码框并注册本组件的活动监听。
	 *
	 * @param container 所属窗口或面板。
	 */
	public XQRC扫码框Impl(组件容器 container) {
		super(container);
		host = MainActivity.getContext();
		host.addOnResumeListener(this);
		host.addOnPauseListener(this);
		host.addOnDestroyListener(this);
		host.addOnRequestPermissionsResultListener(this);
		view().listener(
						new ScanView.Listener() {
							@Override
							public void focused(boolean success) {
								对焦完成(success);
							}

							@Override
							public void points(float[] points) {
								发现识别点(points);
							}

							@Override
							public void ready() {
								扫描就绪();
							}

							@Override
							public void result(XQRC识别结果 result) {
								识别完成(result);
							}

							@Override
							public void problem(String reason) {
								扫描失败(reason);
							}

							@Override
							public void cancelled() {
								扫描取消();
							}

							@Override
							public void changed(String oldState, String newState) {
								状态改变(oldState, newState);
							}
						});
	}

	@Override
	protected View createView() {
		return new ScanView(MainActivity.getContext());
	}

	/**
	 * 取得本组件独有的预览。
	 *
	 * @return 原生预览。
	 */
	private ScanView view() {
		return (ScanView) getView();
	}

	/**
	 * 拒绝非主线程或销毁后的操作。
	 */
	private void check() {
		if (destroyed || Looper.myLooper() != Looper.getMainLooper()) {
			throw new XQRC二维码错误("扫码框仅可在主线程且销毁前使用");
		}
	}

	@Override
	public boolean 开始扫描(XQRC扫码配置 options) {
		check();
		return view().start(options);
	}

	@Override
	public boolean 暂停扫描() {
		check();
		return view().pause(true);
	}

	@Override
	public boolean 继续扫描() {
		check();
		return view().pause(false);
	}

	@Override
	public boolean 停止扫描() {
		check();
		return view().stop(true);
	}

	@Override
	public boolean 设置补光(boolean enabled) {
		check();
		return view().torch(enabled);
	}

	@Override
	public XQRC扫码配置 取得配置() {
		check();
		return view().configuration();
	}

	@Override
	public String 当前状态() {
		check();
		return view().state();
	}

	@Override
	public boolean 扫描中() {
		check();
		return view().active();
	}

	@Override
	public boolean 补光开启() {
		check();
		return view().light();
	}

	@Override
	public void onResume() {
		if (!destroyed) {
			view().resumed(true);
		}
	}

	@Override
	public void onPause() {
		if (!destroyed) {
			view().resumed(false);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
		if (!destroyed) {
			view().permission(requestCode);
		}
	}

	@Override
	public void onDestroy() {
		销毁();
	}

	@Override
	public void 销毁() {
		if (!destroyed) {
			destroyed = true;
			host.removeOnResumeListener(this);
			host.removeOnPauseListener(this);
			host.removeOnDestroyListener(this);
			host.removeOnRequestPermissionsResultListener(this);
			view().close();
			super.销毁();
		}
	}
	@Override
	public void 扫描就绪() {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "扫描就绪");
		}
	}

	@Override
	public void 识别完成(XQRC识别结果 result) {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "识别完成" , result);
		}
	}

	@Override
	public void 扫描失败(String reason) {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "扫描失败" , reason);
		}
	}

	@Override
	public void 扫描取消() {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "扫描取消");
		}
	}

	@Override
	public void 状态改变(String oldState, String newState) {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "状态改变" , oldState, newState);
		}
	}

	@Override
	public void 对焦完成(boolean success) {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "对焦完成", success);
		}
	}

	@Override
	public void 发现识别点(float[] points) {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "发现识别点", points.clone());
		}
	}

	@Override
	public boolean 触发对焦() {
		check();
		return view().focusOnce();
	}

	@Override
	public int[] 取扫描区域() {
		check();
		return view().frameBounds();
	}

	@Override
	public int[] 取预览识别区域() {
		check();
		return view().previewBounds();
	}
}
