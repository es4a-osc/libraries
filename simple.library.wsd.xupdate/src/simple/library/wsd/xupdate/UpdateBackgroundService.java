package simple.library.wsd.xupdate;

import java.io.File;
import java.util.IdentityHashMap;
import java.util.ArrayList;
import java.util.Map;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

/**
 * 只承接文件任务和通知，不持有窗口或更新器；进程被结束后不承诺自动恢复。
 * 后台完成只提供用户点击安装的通知，不在后台强行启动系统安装界面。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class UpdateBackgroundService extends Service {

	private static final String CHANNEL = "simple_xupdate_downloads";
	private static final int FOREGROUND = 7301;
	private static final Map<UpdateFileTask, Integer> TASKS = new IdentityHashMap<UpdateFileTask, Integer>();
	private static int nextId = 7400;
	private static UpdateBackgroundService instance;

	/** 用应用上下文移交任务；服务启动失败不得悄悄丢失前台任务。 */
	static void attach(Context context, UpdateFileTask task) {
		if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
			throw new IllegalArgumentException("请先在系统设置中允许本应用显示通知，再转入后台下载。");
		}
		if (!TASKS.containsKey(task)) {
			TASKS.put(task, nextId++);
			try {
				ContextCompat.startForegroundService(context, new Intent(context, UpdateBackgroundService.class));
			} catch (RuntimeException exception) {
				TASKS.remove(task);
				throw exception;
			}
		}
	}

	/** 任务进度或暂停状态变化时刷新低打扰通知。 */
	static void changed(UpdateFileTask task) {
		if (instance != null && TASKS.containsKey(task)) {
			instance.refresh();
		}
	}

	/** 结束任务；只有校验完成的实际文件才能生成安装入口。 */
	static void finished(UpdateFileTask task, File file, String error) {
		Integer id = TASKS.remove(task);
		if (instance != null && id != null) {
			NotificationManager manager = (NotificationManager) instance.getSystemService(NOTIFICATION_SERVICE);
			if (file != null || error != null) {
				NotificationCompat.Builder builder = instance.builder()
						.setContentTitle(file == null ? "更新下载失败" : "更新下载完成")
						.setContentText(file == null ? error : "点击安装已校验的更新包")
						.setOngoing(false).setAutoCancel(true);
				if (file != null) {
					Uri uri = FileProvider.getUriForFile(instance, instance.getPackageName() + ".updateFileProvider", file);
					Intent intent = new Intent(Intent.ACTION_VIEW)
							.setDataAndType(uri, "application/vnd.android.package-archive")
							.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
					int flags = PendingIntent.FLAG_UPDATE_CURRENT;
					if (Build.VERSION.SDK_INT >= 23) {
						flags |= PendingIntent.FLAG_IMMUTABLE;
					}
					builder.setContentIntent(PendingIntent.getActivity(instance, id, intent, flags));
				}
				manager.notify(id, builder.build());
			}
			instance.refresh();
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		if (Build.VERSION.SDK_INT >= 26) {
			NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			manager.createNotificationChannel(new NotificationChannel(CHANNEL, "应用更新下载", NotificationManager.IMPORTANCE_LOW));
		}
		// 启动后立即进入前台，满足 Android 对前台服务的时限要求。
		startForeground(FOREGROUND, builder().setContentTitle("正在下载更新").build());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && intent.hasExtra("task")) {
			int id = intent.getIntExtra("task", -1);
			for (UpdateFileTask task : new ArrayList<UpdateFileTask>(TASKS.keySet())) {
				if (TASKS.get(task) == id) {
					if ("cancel".equals(intent.getAction())) {
						task.cancel();
					} else if ("resume".equals(intent.getAction())) {
						try {
							task.resume();
						} catch (RuntimeException exception) {
							finished(task, null, exception.getMessage());
						}
					}
					break;
				}
			}
		}
		refresh();
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		instance = null;
		// 系统终止服务时一并取消任务，不让下载在失去前台通知后继续运行。
		for (UpdateFileTask task : new ArrayList<UpdateFileTask>(TASKS.keySet())) {
			task.cancel();
		}
		TASKS.clear();
		super.onDestroy();
	}

	/** 多实例共用一个前台服务，最后一个任务结束才退出。 */
	private void refresh() {
		if (TASKS.isEmpty()) {
			stopForeground(true);
			stopSelf();
			return;
		}
		UpdateFileTask task = TASKS.keySet().iterator().next();
		long total = task.total();
		int percent = total > 0 ? (int) Math.min(100, task.received() * 100.0 / total) : 0;
		NotificationCompat.Builder progress = builder().setContentTitle("正在下载更新")
				.setContentText(task.state() == 更新器.状态_暂停 ? "下载已暂停" : "后台任务：" + TASKS.size())
				.setOngoing(true).setProgress(100, percent, total <= 0);
		int id = TASKS.get(task);
		progress.addAction(0, "取消", action(id, "cancel"));
		if (task.state() == 更新器.状态_暂停) {
			progress.addAction(0, "继续", action(id, "resume"));
		}
		((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(FOREGROUND, progress.build());
	}

	/** 通知操作只引用内部任务编号，不把窗口或第三方对象放入 Intent。 */
	private PendingIntent action(int id, String command) {
		Intent intent = new Intent(this, UpdateBackgroundService.class).setAction(command).putExtra("task", id);
		int flags = PendingIntent.FLAG_UPDATE_CURRENT;
		if (Build.VERSION.SDK_INT >= 23) {
			flags |= PendingIntent.FLAG_IMMUTABLE;
		}
		return PendingIntent.getService(this, id, intent, flags);
	}

	/** 统一使用库内现有更新图标和低打扰通道。 */
	private NotificationCompat.Builder builder() {
		return new NotificationCompat.Builder(this, CHANNEL)
				.setSmallIcon(com.xuexiang.xupdate.R.drawable.xupdate_icon_app_update)
				.setOnlyAlertOnce(true);
	}
}
