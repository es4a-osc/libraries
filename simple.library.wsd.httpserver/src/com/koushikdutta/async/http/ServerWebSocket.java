package com.koushikdutta.async.http;

import android.util.Base64;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.AsyncSocket;
import com.koushikdutta.async.BufferedDataSink;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.Util;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.WritableCallback;
import com.koushikdutta.async.future.Cancellable;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * 复用 AndroidAsync 帧解析器的服务端 WebSocket 连接，补齐关闭握手。
 *
 * <p>回调与帧收发沿用 AndroidAsync 3.1.0 WebSocketImpl 的适配方式；原实现收到
 * Close 后直接关闭 TCP，本类先回送 Close 并等待发送缓冲清空，最多等待三秒。
 * HybiParser 仅包内可见，本类以同包桥接方式访问固定 3.1.0 版本的解析器，
 * 不替换第三方原包；相关上游许可保留在 libs/androidasync-LICENSE.txt。</p>
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class ServerWebSocket implements WebSocket {

	private static final String ACCEPT_MAGIC = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	private static final long CLOSE_TIMEOUT_MS = 3000;

	private final AsyncSocket socket;
	private final BufferedDataSink sink;
	private final HybiParser parser;
	private CompletedCallback closedCallback;
	private CompletedCallback endCallback;
	private WritableCallback writableCallback;
	private StringCallback stringCallback;
	private DataCallback dataCallback;
	private PingCallback pingCallback;
	private PongCallback pongCallback;
	private Cancellable closeTimeout;
	private Exception failure;
	private boolean closing;
	private boolean closeReceived;
	private boolean closeSent;
	private boolean closed;

	/**
	 * 安装帧解析与连接清理；调用方安装业务回调后再恢复接收。
	 *
	 * @param socket 已完成 HTTP 升级的底层连接。
	 */
	private ServerWebSocket(AsyncSocket socket) {
		this.socket = socket;
		sink = new BufferedDataSink(socket);
		sink.setWriteableCallback(new WritableCallback() {
			@Override
			public void onWriteable() {
				if (closing && closeReceived) {
					finishCloseIfDrained();
				} else if (!closing && writableCallback != null) {
					writableCallback.onWriteable();
				}
			}
		});
		socket.setClosedCallback(new CompletedCallback() {
			@Override
			public void onCompleted(Exception exception) {
				closed = true;
				cancelCloseTimeout();
				Exception result = failure != null ? failure : exception;
				if (result == null && !closeReceived) {
					result = new IOException("WebSocket 连接中断，未完成关闭握手");
				}
				if (closedCallback != null) {
					closedCallback.onCompleted(result);
				}
			}
		});
		parser = new HybiParser(socket) {
			@Override
			protected void report(Exception exception) {
				failure = exception;
				try {
					if (endCallback != null) {
						endCallback.onCompleted(exception);
					}
				} finally {
					socket.close();
				}
			}

			@Override
			protected void onMessage(byte[] payload) {
				// 当前 Simple 封装没有二进制接收事件；无人接收时消费该帧，继续处理控制帧。
				if (!closing && dataCallback != null) {
					ByteBufferList data = new ByteBufferList(payload);
					try {
						Util.emitAllData(ServerWebSocket.this, data);
					} finally {
						data.recycle();
					}
				}
			}

			@Override
			protected void onMessage(String payload) {
				if (!closing && stringCallback != null) {
					stringCallback.onStringAvailable(payload);
				}
			}

			@Override
			protected void onDisconnect(int code, String reason) {
				receiveClose(code, reason);
			}

			@Override
			protected void sendFrame(byte[] frame) {
				if (frame != null) {
					sink.write(new ByteBufferList(frame));
				}
			}

			@Override
			protected void onPing(String payload) {
				if (pingCallback != null) {
					pingCallback.onPingReceived(payload);
				}
			}

			@Override
			protected void onPong(String payload) {
				if (pongCallback != null) {
					pongCallback.onPongReceived(payload);
				}
			}
		};
		parser.setMasking(false);
		parser.setDeflate(false);
	}

	/**
	 * 校验无子协议的服务端升级请求并发送握手响应。
	 *
	 * @param request 收到的 GET 请求。
	 * @param response 对应的 HTTP 响应。
	 * @return 升级后的暂停连接；请求无效时结束响应并返回空。
	 */
	public static ServerWebSocket upgrade(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
		Headers headers = request.getHeaders();
		if (!"websocket".equalsIgnoreCase(headers.get("Upgrade"))
				|| !hasToken(headers.get("Connection"), "Upgrade")
				|| headers.get("Sec-WebSocket-Protocol") != null) {
			response.code(404);
			response.end();
			return null;
		}
		if (!"13".equals(headers.get("Sec-WebSocket-Version"))) {
			response.code(426);
			response.getHeaders().set("Sec-WebSocket-Version", "13");
			response.end();
			return null;
		}
		String key = headers.get("Sec-WebSocket-Key");
		try {
			if (key == null || Base64.decode(key, Base64.DEFAULT).length != 16) {
				throw new IllegalArgumentException("无效的 WebSocket 握手密钥");
			}
		} catch (IllegalArgumentException exception) {
			response.code(400);
			response.end();
			return null;
		}
		String accept;
		try {
			byte[] digest = MessageDigest.getInstance("SHA-1")
				.digest((key + ACCEPT_MAGIC).getBytes(StandardCharsets.US_ASCII));
			accept = Base64.encodeToString(digest, Base64.NO_WRAP);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("系统缺少 WebSocket 握手所需的 SHA-1", exception);
		}
		AsyncSocket socket = request.getSocket();
		socket.pause();
		response.code(101);
		response.getHeaders().set("Upgrade", "websocket");
		response.getHeaders().set("Connection", "Upgrade");
		response.getHeaders().set("Sec-WebSocket-Accept", accept);
		// 101 响应没有 HTTP 消息体；空值是 AndroidAsync 禁用分块编码的约定。
		response.getHeaders().set("Transfer-Encoding", "");
		response.writeHead();
		return new ServerWebSocket(socket);
	}

	/**
	 * 按 HTTP 头的逗号分隔规则查找不区分大小写的令牌。
	 *
	 * @param value 协议头值，可为空。
	 * @param token 要查找的令牌。
	 * @return 包含令牌时返回真。
	 */
	private static boolean hasToken(String value, String token) {
		if (value != null) {
			for (String item : value.split(",")) {
				if (token.equalsIgnoreCase(item.trim())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 回送对端关闭码与原因；无状态码的 Close 以空负载回复。
	 *
	 * @param code 对端状态码；0 表示未携带状态码。
	 * @param reason 对端关闭原因，可为空。
	 */
	private void receiveClose(int code, String reason) {
		closeReceived = true;
		closing = true;
		scheduleCloseTimeout();
		if (!closeSent) {
			closeSent = true;
			parser.close(code, reason == null ? "" : reason);
		}
		finishCloseIfDrained();
	}

	/**
	 * 发送缓冲排空后再关闭 TCP，保证 Close 帧先进入底层连接。
	 */
	private void finishCloseIfDrained() {
		if (!closed && closeReceived && closeSent && !sink.isBuffering()) {
			socket.close();
		}
	}

	/**
	 * 为写缓冲阻塞或对端不响应的关闭过程设置一次有界等待。
	 */
	private void scheduleCloseTimeout() {
		if (!closed && closeTimeout == null) {
			closeTimeout = socket.getServer().postDelayed(new Runnable() {
				@Override
				public void run() {
					failure = new IOException("WebSocket 关闭握手超时");
					socket.close();
				}
			}, CLOSE_TIMEOUT_MS);
		}
	}

	/**
	 * 连接关闭后取消超时任务，避免任务继续持有已释放的连接。
	 */
	private void cancelCloseTimeout() {
		if (closeTimeout != null) {
			closeTimeout.cancel();
			closeTimeout = null;
		}
	}

	/**
	 * 拒绝在关闭过程中继续提交应用数据。
	 */
	private void requireOpen() {
		if (closing || !socket.isOpen()) {
			throw new IllegalStateException("WebSocket 连接已开始关闭");
		}
	}

	@Override
	public void close() {
		if (!closing && !closed) {
			closing = true;
			closeSent = true;
			scheduleCloseTimeout();
			parser.close(1000, "");
		}
	}

	@Override
	public void end() {
		close();
	}

	@Override
	public void send(String value) {
		requireOpen();
		sink.write(new ByteBufferList(parser.frame(value)));
	}

	@Override
	public void send(byte[] value) {
		requireOpen();
		sink.write(new ByteBufferList(parser.frame(value)));
	}

	@Override
	public void send(byte[] value, int offset, int length) {
		if (offset < 0 || length < 0 || offset > value.length - length) {
			throw new IndexOutOfBoundsException("WebSocket 字节范围越界");
		}
		send(Arrays.copyOfRange(value, offset, offset + length));
	}

	@Override
	public void ping(String value) {
		requireOpen();
		sink.write(new ByteBufferList(parser.pingFrame(value)));
	}

	@Override
	public void pong(String value) {
		requireOpen();
		sink.write(new ByteBufferList(parser.pongFrame(value)));
	}

	@Override
	public void write(ByteBufferList value) {
		send(value.getAllByteArray());
	}

	@Override
	public String getProtocol() {
		return null;
	}

	@Override
	public void setClosedCallback(CompletedCallback callback) {
		closedCallback = callback;
	}

	@Override
	public CompletedCallback getClosedCallback() {
		return closedCallback;
	}

	@Override
	public void setEndCallback(CompletedCallback callback) {
		endCallback = callback;
	}

	@Override
	public CompletedCallback getEndCallback() {
		return endCallback;
	}

	@Override
	public void setWriteableCallback(WritableCallback callback) {
		writableCallback = callback;
	}

	@Override
	public WritableCallback getWriteableCallback() {
		return writableCallback;
	}

	@Override
	public void setStringCallback(StringCallback callback) {
		stringCallback = callback;
	}

	@Override
	public StringCallback getStringCallback() {
		return stringCallback;
	}

	@Override
	public void setDataCallback(DataCallback callback) {
		dataCallback = callback;
	}

	@Override
	public DataCallback getDataCallback() {
		return dataCallback;
	}

	@Override
	public void setPingCallback(PingCallback callback) {
		pingCallback = callback;
	}

	@Override
	public void setPongCallback(PongCallback callback) {
		pongCallback = callback;
	}

	@Override
	public PongCallback getPongCallback() {
		return pongCallback;
	}

	@Override
	public AsyncSocket getSocket() {
		return socket;
	}

	@Override
	public AsyncServer getServer() {
		return socket.getServer();
	}

	@Override
	public boolean isOpen() {
		return !closing && socket.isOpen();
	}

	@Override
	public boolean isBuffering() {
		return sink.isBuffering();
	}

	@Override
	public boolean isChunked() {
		return false;
	}

	@Override
	public void pause() {
		socket.pause();
	}

	@Override
	public void resume() {
		socket.resume();
	}

	@Override
	public boolean isPaused() {
		return socket.isPaused();
	}

	@Override
	public String charset() {
		return null;
	}
}
