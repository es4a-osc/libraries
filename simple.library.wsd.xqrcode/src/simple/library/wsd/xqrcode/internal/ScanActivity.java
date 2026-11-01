package simple.library.wsd.xqrcode.internal;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
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
		super.onCreate(state);
		session = ScanRegistry.get(getIntent().getStringExtra("session"));
		if (session == null) {
			finish();
			return;
		}
		session.page = this;
		LinearLayout root = new LinearLayout(this);
		root.setOrientation(LinearLayout.VERTICAL);
		root.setBackgroundColor(Color.BLACK);
		status = new TextView(this);
		status.setTextColor(Color.WHITE);
		status.setText("将二维码或条码置于框内");
		status.setPadding(16, 16, 16, 16);
		root.addView(status);
		scanner = new ScanView(this);
		root.addView(scanner, new LinearLayout.LayoutParams(-1, 0, 1));
		LinearLayout buttons = new LinearLayout(this);
		button(buttons, "返回", 0);
		button(buttons, "暂停 / 继续", 1);
		button(buttons, "补光", 2);
		root.addView(buttons);
		setContentView(root);
		scanner.listener(new ScanView.Listener() {
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
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (operation == 0) {
					ScanRegistry.stop(session.token, true);
				} else if (operation == 1) {
					session.paused = !session.paused;
					scanner.pause(session.paused);
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
	 * @param operation 暂停、继续或补光类型。
	 * @return 真实操作返回值。
	 */
	boolean control(int operation) {
		return scanner != null && (operation < 2 ? scanner.pause(operation == 0) : scanner.torch(operation == 3));
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
}
