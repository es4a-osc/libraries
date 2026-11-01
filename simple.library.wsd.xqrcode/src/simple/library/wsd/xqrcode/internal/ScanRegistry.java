package simple.library.wsd.xqrcode.internal;

import java.lang.ref.WeakReference;
import java.util.UUID;
import simple.library.wsd.xqrcode.*;

/**
 * 仅在本进程传递配置与结果，不通过可伪造的广播分发扫码回调。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class ScanRegistry {
	private static Session current;

	/**
	 * 禁止创建注册表实例。
	 */
	private ScanRegistry() {
	}

	/**
	 * 创建唯一独立扫码页请求。
	 *
	 * @param id 调用方请求序号。
	 * @param options 已复制的配置。
	 * @param owner 组件接收者。
	 * @return 随机会话标识。
	 */
	public static String create(int id, 扫码配置 options, Listener owner) {
		if (current != null && current.owner.get() != null) {
			throw new IllegalStateException("独立扫码页已有未结束的请求");
		}
		current = new Session(id, options, owner);
		return current.token;
	}

	/**
	 * 查找仍有效的本应用请求。
	 *
	 * @param token 会话标识。
	 * @return 存在且接收者存活时返回会话，否则为空。
	 */
	static Session get(String token) {
		return current != null && current.token.equals(token) && current.owner.get() != null ? current : null;
	}

	/**
	 * 停止指定会话，先失效再关闭界面以避免重复取消。
	 *
	 * @param token 会话标识。
	 * @param notify 是否报告取消。
	 * @return 是否找到本次会话。
	 */
	public static boolean stop(String token, boolean notify) {
		Session session = get(token);
		if (session == null) {
			return false;
		}
		current = null;
		if (session.page != null) {
			session.page.finish();
		}
		Listener owner = session.owner.get();
		if (notify && owner != null) {
			owner.scanCancelled(session.id);
		}
		return true;
	}

	/**
	 * 控制前台扫码页。
	 *
	 * @param token 会话标识。
	 * @param operation 0 暂停，1 继续，2 关闭补光，3 开启补光。
	 * @return 操作实际被接受时为真。
	 */
	public static boolean control(String token, int operation) {
		Session session = get(token);
		if (session == null) {
			return false;
		}
		if (operation < 2) {
			session.paused = operation == 0;
			return session.page == null || session.page.control(operation);
		}
		return session.page != null && session.page.control(operation);
	}

	/**
	 * 一次独立扫码页的进程内状态。
	 */
	static final class Session {
		final String token = UUID.randomUUID().toString();
		final int id;
		final 扫码配置 options;
		final WeakReference<Listener> owner;
		ScanActivity page;
		boolean paused;

		/**
		 * 复制请求及弱接收者。
		 *
		 * @param requestId 请求编号。
		 * @param config 配置。
		 * @param listener 接收者。
		 */
		Session(int requestId, 扫码配置 config, Listener listener) {
			id = requestId;
			options = config.copy();
			owner = new WeakReference<>(listener);
		}
	}

	/**
	 * 独立扫码页向所属组件报告事件。
	 */
	public interface Listener {
		/**
		 * 相机启动通知。
		 *
		 * @param id 请求编号。
		 */
		void scanReady(int id);

		/**
		 * 识别完成通知。
		 *
		 * @param id 请求编号。
		 * @param result 识别结果。
		 * @param finished 是否为单次扫描并已结束。
		 */
		void scanResult(int id, 识别结果 result, boolean finished);

		/**
		 * 错误通知。
		 *
		 * @param id 请求编号。
		 * @param reason 原因。
		 * @param finished 是否因终止错误结束会话。
		 */
		void scanError(int id, String reason, boolean finished);

		/**
		 * 取消通知。
		 *
		 * @param id 请求编号。
		 */
		void scanCancelled(int id);
	}
}
