package simple.library.wsd.xqrcode.internal;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import simple.library.wsd.xqrcode.识别结果;

/**
 * 不要求 Simple 宿主继承 FragmentActivity 的普通扫码页。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class ScanActivity extends Activity {
	private ScanView scanner;
	private ScanRegistry.Session session;
	private TextView status;

	/**
	 * 创建系统可实例化的独立活动。
	 */
	public ScanActivity() {
	}

	@Override
	protected void onCreate(Bundle state) {
		session = ScanRegistry.get(getIntent().getStringExtra("session"));
		if (session != null && session.options.主题资源() != 0) {
			setTheme(session.options.主题资源());
		}
		super.onCreate(state);
		if (session == null) {
			finish();
			return;
		}
		session.page = this;
		LinearLayout root = new LinearLayout(this);
		root.setOrientation(LinearLayout.VERTICAL);
		root.setBackgroundColor(Color.BLACK);
		TextView prompt = new TextView(this);
		prompt.setTextColor(Color.WHITE);
		prompt.setText(session.options.提示文字());
		prompt.setPadding(16, 16, 16, 8);
		root.addView(prompt);
		status = new TextView(this);
		status.setTextColor(Color.WHITE);
		status.setPadding(16, 0, 16, 8);
		root.addView(status);
		scanner = new ScanView(this);
		root.addView(scanner, new LinearLayout.LayoutParams(-1, 0, 1));
		LinearLayout buttons = new LinearLayout(this);
		button(buttons, "返回", 0);
		button(buttons, "暂停 / 继续", 1);
		button(buttons, "补光", 2);
		button(buttons, "对焦", 3);
		root.addView(buttons);
		setContentView(root);
		scanner.listener(
				new ScanView.Listener() {
					@Override
					public void focused(boolean success) {
				status.setText(success ? "手动对焦成功" : "手动对焦未成功");
						ScanRegistry.Listener owner = owner();
						if (owner != null) {
							owner.scanFocused(session.id, success);
						}
					}

					@Override
					public void points(float[] points) {
						ScanRegistry.Listener owner = owner();
						if (owner != null) {
							owner.scanPoints(session.id, points);
						}
					}

					@Override
					public void ready() {
						ScanRegistry.Listener owner = owner();
						if (owner != null) {
							owner.scanReady(session.id);
						}
					}

					@Override
					public void result(识别结果 result) {
						ScanRegistry.Listener owner = owner();
						if (owner != null) {
							boolean finished = !session.options.连续扫描();
							if (finished) {
								ScanRegistry.stop(session.token, false);
							}
							status.setText(result.格式() + "：" + result.内容());
							owner.scanResult(session.id, result, finished);
						}
					}

					@Override
					public void problem(String reason) {
						ScanRegistry.Listener owner = owner();
						if (owner != null) {
							boolean finished = "失败".equals(scanner.state());
							if (finished) {
								ScanRegistry.stop(session.token, false);
							}
							status.setText(reason);
							owner.scanError(session.id, reason, finished);
						}
					}

					@Override
					public void cancelled() {
						if (session != null) {
							ScanRegistry.stop(session.token, true);
						}
					}

					@Override
					public void changed(String oldState, String newState) {
						status.setText(newState);
					}
				});
		scanner.start(session.options);
		if (session.paused) {
			scanner.pause(true);
		}
	}

	/**
	 * 创建底部操作按钮。
	 *
	 * @param container 按钮容器。
	 * @param title 按钮文本。
	 * @param operation 操作类型。
	 */
	private void button(LinearLayout container, String title, final int operation) {
		Button button = new Button(this);
		button.setText(title);
		button.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (operation == 0) {
							ScanRegistry.stop(session.token, true);
						} else if (operation == 1) {
							session.paused = !session.paused;
							scanner.pause(session.paused);
						} else if (operation == 3) {
					status.setText(scanner.focusOnce() ? "对焦请求已受理" : "对焦请求未受理：相机未开启或不支持");
						} else {
							scanner.torch(!scanner.light());
						}
					}
				});
		container.addView(button, new LinearLayout.LayoutParams(0, -2, 1));
	}

	/**
	 * 读取仍有效的组件接收者。
	 *
	 * @return 接收者或空引用。
	 */
	private ScanRegistry.Listener owner() {
		return session != null && ScanRegistry.get(session.token) == session ? session.owner.get() : null;
	}

	/**
	 * 接受组件的前台控制请求。
	 *
	 * @param operation 0 暂停、1 继续、2 关闭补光、3 开启补光、4 手动对焦。
	 * @return 真实操作返回值。
	 */
	boolean control(int operation) {
		return scanner != null
				&& (operation == 4
						? scanner.focusOnce()
						: operation < 2
								? scanner.pause(operation == 0)
								: scanner.torch(operation == 3));
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (scanner != null) {
			scanner.resumed(true);
		}
	}

	@Override
	protected void onPause() {
		if (scanner != null) {
			scanner.resumed(false);
		}
		super.onPause();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
		super.onRequestPermissionsResult(requestCode, permissions, results);
		if (scanner != null) {
			scanner.permission(requestCode);
		}
	}

	@Override
	protected void onDestroy() {
		if (scanner != null) {
			scanner.close();
		}
		if (session != null && session.page == this) {
			session.page = null;
			if (!isChangingConfigurations()) {
				ScanRegistry.stop(session.token, true);
			}
		}
		super.onDestroy();
	}

	/**
	 * 获取本页预览，供进程内受控查询。
	 *
	 * @return 预览视图或空。
	 */
	ScanView scanner() {
		return scanner;
	}
}
