package simple.library.wsd.quickjs;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * console 同步日志接收器，默认写入 Android Logcat，可继承并覆写输出。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public class QuickJS日志 {
	/**
	 * 创建可继承的宿主适配对象。
	 */
	public QuickJS日志() {
	}

	/**
	 * 接收一条 console 日志。
	 *
	 * @param level 0 log、1 info、2 warn、3 error、4 debug。
	 * @param message 日志文本。
	 */
	@SimpleFunction
	public void 输出(int level, String message) {
		int[] priorities = {android.util.Log.INFO, android.util.Log.INFO, android.util.Log.WARN, android.util.Log.ERROR,
				android.util.Log.DEBUG};
		if (level < 0 || level >= priorities.length) {
			throw new QuickJS错误("无效日志等级");
		}
		android.util.Log.println(priorities[level], "QuickJS", message);
	}
}
