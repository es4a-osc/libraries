package simple.library.wsd.ftpclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 网络线程等待用户确认，主线程只显示对话框；超时及组件销毁均释放等待者。
 */
final class SftpHostConfirmation implements SftpHostTrust.Confirmation {

	private static final class Request {
		final CountDownLatch signal = new CountDownLatch(1);
		final AtomicBoolean completed = new AtomicBoolean();
		volatile boolean accepted;
		volatile AlertDialog dialog;

		void finish(boolean value) {
			if (completed.compareAndSet(false, true)) {
				accepted = value;
				signal.countDown();
			}
		}
	}

	private final Activity activity;
	private final Handler handler = new Handler(Looper.getMainLooper());
	private volatile boolean closed;
	private Request pending;

	SftpHostConfirmation(Activity activity) { this.activity = activity; }

	@Override
	public boolean confirm(final String host, final int port, final String algorithm,
			final String fingerprint, final String previous) {
		if (Looper.myLooper() == Looper.getMainLooper()) return false;

		final Request request = new Request();

		synchronized (this) {
			if (closed || pending != null) return false;

			pending = request;
		}

		try {
			if (!handler.post(new Runnable() {
				@Override public void run() {
					if (closed || request.completed.get() || activity.isFinishing()) {
						request.finish(false);
						return;
					}

					boolean changed = previous.length() > 0;
					String message = "服务器：" + host + ":" + port + "\n公钥类型：" + algorithm
						+ "\n公钥指纹：\n" + fingerprint;
					if (changed) {
						message += "\n\n原指纹：\n" + previous + "\n\n服务器身份与已保存记录不同，请向管理员核实后再更新信任。";
					} else {
						message += "\n\n首次连接，请核对服务器身份。确认后保存在本应用中，后续自动校验。";
					}

					try {
						request.dialog = new AlertDialog.Builder(activity)
							.setTitle(changed ? "服务器公钥已变化" : "首次连接 SFTP 服务器")
							.setMessage(message)
							.setPositiveButton(changed ? "更新信任并连接" : "信任并连接", new DialogInterface.OnClickListener() {
								@Override public void onClick(DialogInterface dialog, int which) { request.finish(true); }
							})
							.setNegativeButton("取消", new DialogInterface.OnClickListener() {
								@Override public void onClick(DialogInterface dialog, int which) { request.finish(false); }
							})
							.setOnCancelListener(new DialogInterface.OnCancelListener() {
								@Override public void onCancel(DialogInterface dialog) { request.finish(false); }
							})
							.create();
						request.dialog.setCanceledOnTouchOutside(false);
						request.dialog.show();
					} catch (RuntimeException ex) {
						request.finish(false);
					}
				}
			})) request.finish(false);
			if (!request.signal.await(60, TimeUnit.SECONDS)) request.finish(false);
			return request.accepted && !closed;
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			request.finish(false);
			return false;
		} finally {
			dismiss(request);

			synchronized (this) { if (pending == request) pending = null; }
		}
	}

	private void dismiss(final Request request) {
		handler.post(new Runnable() {
			@Override public void run() {
				if (request.dialog != null) request.dialog.dismiss();
			}
		});
	}

	synchronized void close() {
		closed = true;
		if (pending != null) {
			pending.finish(false);
			dismiss(pending);
		}
	}
}
