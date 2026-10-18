package simple.library.wsd.mssql;

import android.os.Handler;
import android.os.Looper;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import simple.runtime.android.MainActivity;
import simple.runtime.components.组件容器;
import simple.runtime.components.impl.组件Impl;
import simple.runtime.collections.集合;
import simple.runtime.events.EventDispatcher;
import simple.runtime.variants.ArrayVariant;

/**
 * 异步请求串行执行；每次任务捕获独立结果，销毁后抑制排队事件。
 */
public final class MsSQL数据库Impl extends 组件Impl implements MsSQL数据库 {

	private final JdbcSession session = new JdbcSession();
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final Handler handler = new Handler(Looper.getMainLooper());
	private final MainActivity activity;
	private volatile boolean destroyed;

	/**
	 * 交由组件串行执行的数据库操作。
	 */
	private interface Task {

		/**
		 * 执行串行数据库任务并返回结果。
		 *
		 * @return 串行数据库任务并返回结果
		 */
		Object run();
	}

	private final MainActivity.OnDestroyListener destroyListener = new MainActivity.OnDestroyListener() {

		@Override
		public void onDestroy() {
			dispose(false);
			EventDispatcher.unregisterEvent(MsSQL数据库Impl.this);
		}
	};

	public MsSQL数据库Impl(组件容器 container) {
		super(container);
		activity = MainActivity.getContext();
		activity.addOnDestroyListener(destroyListener);
	}

	/* 组件事件 */

	@Override
	public void 连接数据库完毕(final boolean succeeded) {
		EventDispatcher.dispatchEvent(this, "连接数据库完毕", succeeded);
	}

	@Override
	public void 查询记录完毕(final 集合 result) {
		EventDispatcher.dispatchEvent(this, "查询记录完毕", result);
	}

	@Override
	public void 修改记录完毕(final boolean succeeded) {
		EventDispatcher.dispatchEvent(this, "修改记录完毕", succeeded);
	}

	@Override
	public void 添加记录完毕(final boolean succeeded) {
		EventDispatcher.dispatchEvent(this, "添加记录完毕", succeeded);
	}

	@Override
	public void 删除记录完毕(final boolean succeeded) {
		EventDispatcher.dispatchEvent(this, "删除记录完毕", succeeded);
	}

	@Override
	public void 执行语句完毕(final boolean succeeded) {
		EventDispatcher.dispatchEvent(this, "执行语句完毕", succeeded);
	}

	@Override
	public void 查询语句完毕(final 集合 result) {
		EventDispatcher.dispatchEvent(this, "查询语句完毕", result);
	}

	/* 组件方法 */

	@Override
	public void 连接数据库(final String address, final String username, final String password, final String database) {
		submit("连接数据库完毕", new Task() {

			@Override
			public Object run() {
				return !destroyed && session.connect(address, username, password, database);
			}
		});
	}

	/**
	 * 查询指定表的全部记录及字段，不附加 WHERE 条件。
	 *
	 * <p>结果通过原有查询完成事件返回。</p>
	 *
	 * @param table 目标表名，须由调用方保证为合法 SQL 标识符。
	 */
	@Override
	public void 查询记录(String table) {
		查询记录(table, "*", "");
	}

	/**
	 * 按指定字段查询表内全部记录，不附加 WHERE 条件。
	 *
	 * <p>结果通过原有查询完成事件返回。</p>
	 *
	 * @param table 目标表名，须由调用方保证为合法 SQL 标识符。
	 * @param columns 查询或提取的字段列表，与完整方法的含义一致。
	 */
	@Override
	public void 查询记录(String table, String columns) {
		查询记录(table, columns, "");
	}

	@Override
	public void 查询记录(final String table, final String columns, final String condition) {
		submit("查询记录完毕", new Task() {

			@Override
			public Object run() {
				return 查询记录_同步(table, columns, condition);
			}
		});
	}

	/**
	 * 查询指定表的全部记录及字段，不附加 WHERE 条件。
	 *
	 * <p>阻塞调用线程，应在后台调用。</p>
	 *
	 * @param table 目标表名，须由调用方保证为合法 SQL 标识符。
	 * @return 查询结果集合；SQL NULL、空结果及失败行为与完整方法一致。
	 */
	@Override
	public 集合 查询记录_同步(String table) {
		return 查询记录_同步(table, "*", "");
	}

	/**
	 * 按指定字段查询表内全部记录，不附加 WHERE 条件。
	 *
	 * <p>阻塞调用线程，应在后台调用。</p>
	 *
	 * @param table 目标表名，须由调用方保证为合法 SQL 标识符。
	 * @param columns 查询或提取的字段列表，与完整方法的含义一致。
	 * @return 查询结果集合；SQL NULL、空结果及失败行为与完整方法一致。
	 */
	@Override
	public 集合 查询记录_同步(String table, String columns) {
		return 查询记录_同步(table, columns, "");
	}

	@Override
	public 集合 查询记录_同步(final String table, final String columns, final String condition) {
		String where = condition == null || condition.trim().isEmpty() ? "" : " WHERE " + condition;
		return 查询语句_同步("SELECT * FROM " + table + where, columns);
	}

	@Override
	public void 修改记录(final String table, final String assignments, final String condition) {
		submit("修改记录完毕", new Task() {

			@Override
			public Object run() {
				return 修改记录_同步(table, assignments, condition);
			}
		});
	}

	@Override
	public boolean 修改记录_同步(final String table, final String assignments, final String condition) {
		return 执行语句_同步("UPDATE " + table + " SET " + assignments + " WHERE " + condition);
	}

	@Override
	public void 添加记录(final String table, final String columns, final String values) {
		submit("添加记录完毕", new Task() {

			@Override
			public Object run() {
				return 添加记录_同步(table, columns, values);
			}
		});
	}

	@Override
	public boolean 添加记录_同步(final String table, final String columns, final String values) {
		return 执行语句_同步("INSERT INTO " + table + " (" + columns + ") VALUES (" + values + ")");
	}

	@Override
	public void 删除记录(final String table, final String condition) {
		submit("删除记录完毕", new Task() {

			@Override
			public Object run() {
				return 删除记录_同步(table, condition);
			}
		});
	}

	@Override
	public boolean 删除记录_同步(final String table, final String condition) {
		return 执行语句_同步("DELETE FROM " + table + " WHERE " + condition);
	}

	@Override
	public void 执行语句(final String sql) {
		submit("执行语句完毕", new Task() {

			@Override
			public Object run() {
				return 执行语句_同步(sql);
			}
		});
	}

	@Override
	public boolean 执行语句_同步(final String sql) {
		return !destroyed && session.execute(sql);
	}

	/**
	 * 执行 SQL 查询并返回全部结果字段。
	 *
	 * <p>结果通过原有查询语句完成事件返回。</p>
	 *
	 * @param sql SQL 查询语句，由调用方确保内容合法。
	 */
	@Override
	public void 查询语句(String sql) {
		查询语句(sql, "*");
	}

	@Override
	public void 查询语句(final String sql, final String columns) {
		submit("查询语句完毕", new Task() {

			@Override
			public Object run() {
				return 查询语句_同步(sql, columns);
			}
		});
	}

	/**
	 * 执行 SQL 查询并返回全部结果字段。
	 *
	 * <p>阻塞调用线程，应在后台调用。</p>
	 *
	 * @param sql SQL 查询语句，由调用方确保内容合法。
	 * @return 结果集合，保留完整方法的空结果及失败约定。
	 */
	@Override
	public 集合 查询语句_同步(String sql) {
		return 查询语句_同步(sql, "*");
	}

	@Override
	public 集合 查询语句_同步(final String sql, final String columns) {
		String[] labels = columns == null || columns.trim().isEmpty() || "*".equals(columns.trim()) ? null : columns.split(",");
		if (labels != null) for (int index = 0; index < labels.length; index++) labels[index] = labels[index].trim();
		return destroyed ? new 集合() : rows(session.query(sql, labels));
	}

	@Override
	public boolean 断开连接() {
		return session.disconnect();
	}

	/* 生命周期与内部辅助 */

	/**
	 * 提交串行数据库任务并安排结果通知。
	 *
	 * @param event 本次事件对象
	 * @param task 要执行的后台任务
	 */
	private void submit(final String event, final Task task) {
		if (destroyed) return;

		try {

			// 串行执行数据库请求，防止同一连接上的语句互相覆盖结果。
			executor.execute(new Runnable() {

				@Override
				public void run() {
					if (destroyed) return;

					final Object result = task.run();

					// 结果在主线程分派；销毁后丢弃已排队的回调。
					handler.post(new Runnable() {

						@Override
						public void run() {
							if (!destroyed) EventDispatcher.dispatchEvent(MsSQL数据库Impl.this, event, result);
						}
					});
				}
			});
		} catch (RejectedExecutionException exception) {
			/* 销毁竞态时丢弃请求。 */
		}
	}

	/**
	 * 将查询结果逐行包装为 Simple 集合。
	 *
	 * @param values 输入值数组
	 * @return 各成员为一行文本数组的 Simple 集合。
	 */
	private 集合 rows(List<String[]> values) {
		集合 result = new 集合();

		for (String[] value : values) result.加入(ArrayVariant.getArrayVariant(value));

		return result;
	}

	/**
	 * 释放连接、线程及按要求移除生命周期监听器。
	 *
	 * @param unregister 是否注销生命周期监听器
	 */
	private void dispose(boolean unregister) {
		if (!destroyed) {
			destroyed = true;
			executor.shutdownNow();
			handler.removeCallbacksAndMessages(null);

			// 等待现有网络操作仅发生在清理线程，不阻塞 Activity 销毁。
			new Thread(new Runnable() {

				@Override
				public void run() {
					session.dispose();
				}
			}, "Simple-JDBC-close").start();
		}

		if (unregister) activity.removeOnDestroyListener(destroyListener);
	}

	@Override
	public void 销毁() {
		dispose(true);
		super.销毁();
	}
}
