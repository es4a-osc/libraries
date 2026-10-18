package simple.library.wsd.excel;

import java.io.File;
import simple.runtime.android.MainActivity;
import simple.runtime.components.组件容器;
import simple.runtime.components.impl.组件Impl;
import simple.runtime.events.EventDispatcher;

/**
 * 将 XLS 文件会话接入 Simple 组件；销毁时释放尚未关闭的读写资源。
 */
public final class EXCEL操作Impl extends 组件Impl implements EXCEL操作 {

	private final WorkbookSession session = new WorkbookSession();
	private final MainActivity activity;
	private boolean destroyed;

	private final MainActivity.OnDestroyListener destroyListener = new MainActivity.OnDestroyListener() {

		@Override
		public void onDestroy() {
			dispose(false);
			EventDispatcher.unregisterEvent(EXCEL操作Impl.this);
		}
	};

	public EXCEL操作Impl(组件容器 container) {
		super(container);
		activity = MainActivity.getContext();
		activity.addOnDestroyListener(destroyListener);
	}

	/* 组件方法 */

	@Override
	public boolean 添加工作表(String table, String fileName) {
		return !destroyed && session.create(table, resolve(fileName));
	}

	@Override
	public boolean 添加单元格(int row, int column, String title) {
		return !destroyed && session.add(row, column, title);
	}

	@Override
	public boolean 合并单元格(int startRow, int startColumn, int endRow, int endColumn) {
		return !destroyed && session.merge(startRow, startColumn, endRow, endColumn);
	}

	@Override
	public boolean 写出工作表() {
		return !destroyed && session.write();
	}

	@Override
	public boolean 读取工作表(String fileName) {
		return !destroyed && session.open(resolve(fileName));
	}

	@Override
	public int 读取总行数() {
		return destroyed ? 0 : session.rows();
	}

	@Override
	public int 读取总列数() {
		return destroyed ? 0 : session.columns();
	}

	@Override
	public String 读取单元格(int row, int column) {
		return destroyed ? "" : session.read(row, column);
	}

	/* 生命周期与内部辅助 */

	/**
	 * 相对路径使用应用私有目录，示例无需申请共享存储权限。
	 *
	 * @param path 文件或目录路径
	 * @return 解析后的文件对象
	 */
	private String resolve(String path) {
		if (path == null) return null;

		File file = new File(path);
		return file.isAbsolute() ? path : new File(activity.getFilesDir(), path).getPath();
	}

	/**
	 * 释放连接、线程及按要求移除生命周期监听器。
	 *
	 * @param unregister 是否注销生命周期监听器
	 */
	private void dispose(boolean unregister) {
		destroyed = true;
		session.close();
		if (unregister) activity.removeOnDestroyListener(destroyListener);
	}

	@Override
	public void 销毁() {
		dispose(true);
		super.销毁();
	}
}
