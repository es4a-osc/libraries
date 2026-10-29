package simple.library.wsd.xui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;

/**
 * 仅在设备回归辅助 APK 中提供可控识别结果，不执行录音或模拟真实识别准确率。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class VoiceResultActivity extends Activity {

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		String configure = getIntent().getStringExtra("configure");
		if (configure != null) {
			getPreferences(MODE_PRIVATE).edit().putString("mode", configure).commit();
			finish();
			return;
		}

		// 延迟返回以检验禁用、切窗和销毁与外部结果之间的竞争。
		final String mode = getPreferences(MODE_PRIVATE).getString("mode", "success");
		new Handler(getMainLooper()).postDelayed(new Runnable() {
			@Override
			public void run() {
				if ("cancel".equals(mode)) {
					setResult(RESULT_CANCELED);
				} else {
					ArrayList<String> results = new ArrayList<String>();
					if (!"empty".equals(mode)) {
						results.add("语音回填验证");
					}
					Intent data = new Intent().putStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS, results);
					if ("malformed".equals(mode)) {
						ArrayList<Integer> invalid = new ArrayList<Integer>();
						invalid.add(1);
						data.putIntegerArrayListExtra(RecognizerIntent.EXTRA_RESULTS, invalid);
					}
					setResult(RESULT_OK, data);
				}
				finish();
			}
		}, 500);
	}
}
