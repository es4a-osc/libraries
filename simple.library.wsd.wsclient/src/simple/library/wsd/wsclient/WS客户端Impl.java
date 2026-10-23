package simple.library.wsd.wsclient;

import simple.runtime.android.MainActivity;
import simple.runtime.components.组件容器;
import simple.runtime.components.impl.组件Impl;
import simple.runtime.events.EventDispatcher;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * 将 OkHttp WebSocket 回调转换为 Simple 主线程事件，并管理连接与组件生命周期。
 */
public final class WS客户端Impl extends 组件Impl implements WS客户端 {

	private final Object stateLock = new Object();
	private final MainActivity activity;
	private final OkHttpClient client = new OkHttpClient();
	private final Headers.Builder headers = new Headers.Builder();
	private Session current;
	private boolean destroyed;

	/**
	 * 每次打开连接拥有独立身份，重连后不能让旧回调修改新连接的状态。
	 */
	private static final class Session {
		WebSocket socket;
		boolean connected;
		boolean closing;
		boolean terminated;
	}

	private final MainActivity.OnDestroyListener destroyListener = new MainActivity.OnDestroyListener() {

		@Override
		public void onDestroy() {
			// Activity 正在遍历监听器，不能在这里修改同一监听器列表。
			dispose(false);
			EventDispatcher.unregisterEvent(WS客户端Impl.this);
		}
	};

	/**
	 * 创建客户端并登记 Activity 结束时的资源清理。
	 *
	 * @param container 所属组件容器。
	 */
	public WS客户端Impl(组件容器 container) {
		super(container);
		activity = MainActivity.getContext();
		activity.addOnDestroyListener(destroyListener);
	}

	/* 组件事件 */

	@Override
	public void 连接成功() {
		EventDispatcher.dispatchEvent(this, "连接成功");
	}

	@Override
	public void 出现错误(String message) {
		EventDispatcher.dispatchEvent(this, "出现错误", message);
	}

	@Override
	public void 收到数据(String message) {
		EventDispatcher.dispatchEvent(this, "收到数据", message);
	}

	@Override
	public void 连接断开() {
		EventDispatcher.dispatchEvent(this, "连接断开");
	}

	/* 组件方法 */

	@Override
	public void 添加协议头(String name, String value) {
		synchronized (stateLock) {
			if (!destroyed) headers.add(name, value);
		}
	}

	@Override
	public void 删除协议头(String name) {
		synchronized (stateLock) {
			if (!destroyed) headers.removeAll(name);
		}
	}

	@Override
	public void 打开连接(String url) {
		Session previous = null;

		try {
			synchronized (stateLock) {
				if (destroyed) return;
				if (url == null || !(url.startsWith("ws://") || url.startsWith("wss://"))) {
					throw new IllegalArgumentException("需要 ws:// 或 wss:// 地址");
				}

				Request request = new Request.Builder().url(url).headers(headers.build()).build();
				Session session = new Session();

				// 在持锁期间登记返回的连接，关闭操作也能处理尚未握手的连接。
				session.socket = client.newWebSocket(request, createListener(session));
				previous = current;
				current = session;
			}
		} catch (IllegalArgumentException exception) {
			notifyError(null, exception.toString());
		} finally {
			if (previous != null && previous.socket != null) previous.socket.cancel();
		}
	}

	@Override
	public void 发送数据(String message) {
		Session session;
		boolean accepted = false;

		synchronized (stateLock) {
			if (destroyed) return;

			session = current;
			if (session != null && session.connected && !session.closing && !session.terminated && message != null) {
				accepted = session.socket.send(message);
			}
		}

		if (!accepted) notifyError(session, "没有可发送的连接、消息为空或发送队列已拒绝");
	}

	@Override
	public void 关闭连接() {
		synchronized (stateLock) {
			if (destroyed || current == null || current.terminated || current.closing) return;

			current.closing = true;
			current.socket.close(1000, "close");
		}
	}

	/* 生命周期与内部辅助 */

	@Override
	public void 销毁() {
		dispose(true);
		super.销毁();
	}

	/**
	 * 清理网络任务；自动销毁和显式销毁可重复到达，资源只释放一次。
	 *
	 * @param unregister 是否注销生命周期监听器
	 */
	private void dispose(boolean unregister) {
		synchronized (stateLock) {
			if (!destroyed) {
				destroyed = true;
				if (current != null && current.socket != null) current.socket.cancel();
				current = null;
				client.dispatcher().cancelAll();
				client.dispatcher().executorService().shutdownNow();
				client.connectionPool().evictAll();
			}
		}

		if (unregister) activity.removeOnDestroyListener(destroyListener);
	}

	/**
	 * 在投递和执行之间再次确认连接身份，过滤重连或销毁后的迟到事件。
	 *
	 * @param session 当前连接会话
	 * @param callback 需要投递的回调
	 */
	private void post(final Session session, final Runnable callback) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				synchronized (stateLock) {
					if (destroyed || (session != null && current != session)) return;
				}

				callback.run();
			}
		});
	}

	/**
	 * 仅向仍有效的连接会话分发错误通知。
	 *
	 * @param session 当前连接会话
	 * @param message 实际失败原因
	 */
	private void notifyError(Session session, final String message) {
		post(session, new Runnable() {

			@Override
			public void run() {
				出现错误(message);
			}
		});
	}

	/**
	 * OkHttp 回调运行于网络线程，所有 Simple 事件统一切回主线程。
	 *
	 * @param session 当前连接会话
	 * @return 用于转发当前连接事件的监听器
	 */
	private WebSocketListener createListener(final Session session) {
		return new WebSocketListener() {

			@Override
			public void onOpen(WebSocket socket, Response response) {
				synchronized (stateLock) {
					if (destroyed || current != session) {
						socket.cancel();
						return;
					}

					if (session.closing) return;

					session.connected = true;
				}

				post(session, new Runnable() {

					@Override
					public void run() {
						连接成功();
					}
				});
			}

			@Override
			public void onMessage(WebSocket socket, final String message) {
				synchronized (stateLock) {
					if (destroyed || current != session || session.terminated || session.closing) return;
				}

				post(session, new Runnable() {

					@Override
					public void run() {
						收到数据(message);
					}
				});
			}

			@Override
			public void onClosing(WebSocket socket, int code, String reason) {
				synchronized (stateLock) {
					if (destroyed || current != session) {
						socket.cancel();
						return;
					}

					session.closing = true;
				}

				// 空关闭帧使用本地状态码 1005，不能将它写回网络。
				// 应答关闭帧，确保握手完成并收到 onClosed。
				socket.close(code == 1005 ? 1000 : code, reason);
			}

			@Override
			public void onClosed(WebSocket socket, int code, String reason) {
				synchronized (stateLock) {
					if (destroyed || current != session || session.terminated) return;

					session.connected = false;
					session.terminated = true;
				}

				post(session, new Runnable() {

					@Override
					public void run() {
						连接断开();
					}
				});
			}

			@Override
			public void onFailure(WebSocket socket, Throwable failure, Response response) {
				synchronized (stateLock) {
					if (destroyed || current != session || session.terminated) return;

					session.connected = false;
					session.terminated = true;
				}

				notifyError(session, failure.toString());
			}
		};
	}
}
