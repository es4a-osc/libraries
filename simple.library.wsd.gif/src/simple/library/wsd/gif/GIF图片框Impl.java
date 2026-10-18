package simple.library.wsd.gif;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;

import simple.runtime.android.MainActivity;
import simple.runtime.components.组件容器;
import simple.runtime.components.impl.android.视图组件;
import simple.runtime.events.EventDispatcher;

/**
 * 使用平台 GIF 解码和软件画布播放动画，单工作线程处理最新载入请求。
 *
 * <p>所有界面和播放状态均由主线程维护。取消或销毁后不会派发过期回调；
 * 已进入平台解码的调用可能继续至返回，其结果将被丢弃。清空移除组件持有的
 * Movie 引用，平台原生内存由系统回收，不承诺立即释放。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class GIF图片框Impl extends 视图组件 implements GIF图片框,
	MainActivity.OnPauseListener, MainActivity.OnResumeListener, MainActivity.OnDestroyListener {

	private final Handler handler = new Handler(Looper.getMainLooper());
	private final PlaybackClock clock = new PlaybackClock();
	private final MainActivity activity;
	private final ThreadPoolExecutor worker;
	private Future<?> request;
	private volatile int generation;
	private volatile boolean destroyed;

	private Movie movie;
	private GifInfo info;
	private String image = "";
	private String lastError = "";
	private boolean loading;
	private boolean autoPlay = true;
	private boolean loop = true;
	private boolean active = true;
	private int scaleMode;

	// 每轮刷新最多登记一个回调，隐藏、暂停和销毁都立即取消。
	private final Runnable ticker = new Runnable() {

		@Override
		public void run() {
			if (destroyed || movie == null || !clock.isAdvancing()) {
				return;
			}

			boolean finished = clock.advance(SystemClock.uptimeMillis());
			getView().invalidate();
			if (clock.isAdvancing()) {
				handler.postDelayed(this, 16);
			}
			if (finished) {
				播放完毕();
			}
		}
	};

	/**
	 * 建立有界解码队列并注册活动生命周期监听。
	 *
	 * @param container 容纳图片框的组件容器。
	 */
	public GIF图片框Impl(组件容器 container) {
		super(container);
		activity = MainActivity.getContext();
		worker = new ThreadPoolExecutor(1, 1, 15, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(1), new ThreadFactory() {

				@Override
				public Thread newThread(Runnable action) {
					return new Thread(action, "Simple-GIF");
				}
			});
		worker.allowCoreThreadTimeOut(true);
		activity.addOnPauseListener(this);
		activity.addOnResumeListener(this);
		activity.addOnDestroyListener(this);
	}

	@Override
	protected View createView() {
		// 父类构造时调用；此处不读取尚未初始化的子类字段。
		View view = new GifCanvas(MainActivity.getContext());
		view.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View clicked) {
				被单击();
			}
		});
		return view;
	}

	/* 组件事件 */

	/**
	 * 用户单击图片框时触发。
	 */
	@Override
	public void 被单击() {
		EventDispatcher.dispatchEvent(this, "被单击");
	}

	/**
	 * 最新请求载入成功后在界面线程触发。
	 *
	 * @param width 原始宽度，单位像素。
	 * @param height 原始高度，单位像素。
	 * @param frames GIF 图像帧数量。
	 */
	@Override
	public void 载入成功(int width, int height, int frames) {
		EventDispatcher.dispatchEvent(this, "载入成功", width, height, frames);
	}

	/**
	 * 最新请求载入失败后在界面线程触发，保留原来的图片和播放状态。
	 *
	 * @param message 读取、格式、大小限制或平台解码失败的说明。
	 */
	@Override
	public void 载入失败(String message) {
		EventDispatcher.dispatchEvent(this, "载入失败", message);
	}

	/**
	 * 非循环播放自然结束时触发一次，停留在最后一帧。
	 */
	@Override
	public void 播放完毕() {
		EventDispatcher.dispatchEvent(this, "播放完毕");
	}

	/* 组件属性 */

	/**
	 * 读取图片。
	 *
	 * @return 已成功载入的资产名、资源标识或绝对路径；字节载入及清空后为空文本。
	 */
	@Override
	public String 图片() {
		return image;
	}

	/**
	 * 设置图片。
	 *
	 * @param image 图片来源；空文本清空。支持资产名、@raw/名称、@drawable/名称和绝对文件路径。
	 */
	@Override
	public void 图片(String image) {
		ensureActive();
		if (image == null || image.length() == 0) {
			清空图片();
		} else {
			load(image, null);
		}
	}

	/**
	 * 读取自动播放。
	 *
	 * @return 新图片载入成功后是否从头播放；不改变当前播放状态。
	 */
	@Override
	public boolean 自动播放() {
		return autoPlay;
	}

	/**
	 * 设置自动播放。
	 *
	 * @param enabled 是否在新图片成功载入后自动播放。
	 */
	@Override
	public void 自动播放(boolean enabled) {
		ensureActive();
		autoPlay = enabled;
	}

	/**
	 * 读取循环播放。
	 *
	 * @return 是否持续循环；假为只播放一次，忽略 GIF 文件内的循环次数。
	 */
	@Override
	public boolean 循环播放() {
		return loop;
	}

	/**
	 * 设置循环播放。
	 *
	 * @param enabled 是否持续循环播放。
	 */
	@Override
	public void 循环播放(boolean enabled) {
		ensureActive();
		loop = enabled;
		clock.setLoop(enabled);
	}

	/**
	 * 读取显示方式。
	 *
	 * @return 0 等比适应、1 拉伸填满、2 原始像素居中。
	 */
	@Override
	public int 显示方式() {
		return scaleMode;
	}

	/**
	 * 设置显示方式。
	 *
	 * @param mode 使用缩放_等比适应、缩放_拉伸填满或缩放_原始居中。
	 */
	@Override
	public void 显示方式(int mode) {
		ensureActive();
		if (mode < 缩放_等比适应 || mode > 缩放_原始居中) {
			throw new IllegalArgumentException("未知 GIF 显示方式");
		}

		scaleMode = mode;
		getView().invalidate();
	}

	/**
	 * 读取正在载入。
	 *
	 * @return 当前是否正在读取、检查或解码最新请求。
	 */
	@Override
	public boolean 正在载入() {
		return loading;
	}

	/**
	 * 读取正在播放。
	 *
	 * @return 当前已开始播放并且组件和应用可见。
	 */
	@Override
	public boolean 正在播放() {
		return clock.isAdvancing();
	}

	/**
	 * 读取图片宽度。
	 *
	 * @return 当前 GIF 的原始像素宽度；没有图片时为 0。
	 */
	@Override
	public int 图片宽度() {
		return info == null ? 0 : info.width;
	}

	/**
	 * 读取图片高度。
	 *
	 * @return 当前 GIF 的原始像素高度；没有图片时为 0。
	 */
	@Override
	public int 图片高度() {
		return info == null ? 0 : info.height;
	}

	/**
	 * 读取帧数。
	 *
	 * @return 当前 GIF 图像帧数量；没有图片时为 0。
	 */
	@Override
	public int 帧数() {
		return info == null ? 0 : info.frames;
	}

	/**
	 * 读取动画时长。
	 *
	 * @return 当前 GIF 一轮播放时长，单位毫秒；没有图片时为 0。
	 */
	@Override
	public int 动画时长() {
		return movie == null ? 0 : Math.max(1, movie.duration());
	}

	/**
	 * 读取播放位置。
	 *
	 * @return 最近一次刷新后的播放时间，单位毫秒；停止和清空后为 0。
	 */
	@Override
	public int 播放位置() {
		return clock.position();
	}

	/**
	 * 读取最后错误。
	 *
	 * @return 最近一次载入失败的说明；新请求、成功或清空后为空文本。
	 */
	@Override
	public String 最后错误() {
		return lastError;
	}

	/* 组件方法 */

	/**
	 * 从字节集异步载入 GIF，成功后图片属性为空文本。
	 *
	 * @param data 完整 GIF 文件字节集，复制后异步载入；空数据按失败事件反馈。
	 */
	@Override
	public void 载入字节图片(byte[] data) {
		ensureActive();
		load("", data);
	}

	/**
	 * 从第一帧开始播放；尚未载入图片时不执行。
	 */
	@Override
	public void 开始播放() {
		ensureActive();
		clock.start(SystemClock.uptimeMillis());
		refreshVisibility();
	}

	/**
	 * 暂停并保留当前播放位置。
	 */
	@Override
	public void 暂停播放() {
		ensureActive();
		clock.pause(SystemClock.uptimeMillis());
		refreshVisibility();
	}

	/**
	 * 从暂停位置继续；一次播放结束后从头继续。
	 */
	@Override
	public void 继续播放() {
		ensureActive();
		clock.resume(SystemClock.uptimeMillis());
		refreshVisibility();
	}

	/**
	 * 停止并回到第一帧，保留已载入的图片。
	 */
	@Override
	public void 停止播放() {
		ensureActive();
		clock.stop(SystemClock.uptimeMillis());
		refreshVisibility();
	}

	/**
	 * 取消待处理请求，停止刷新并释放组件持有的图片引用。
	 */
	@Override
	public void 清空图片() {
		ensureActive();
		cancelLoad();
		loading = false;
		image = "";
		lastError = "";
		movie = null;
		info = null;
		clock.clear(SystemClock.uptimeMillis());
		handler.removeCallbacksAndMessages(null);
		getView().requestLayout();
		getView().invalidate();
	}

	/* 生命周期 */

	/**
	 * 应用进入后台时冻结动画时间，保留用户的播放意图。
	 */
	@Override
	public void onPause() {
		active = false;
		refreshVisibility();
	}

	/**
	 * 应用恢复前台时，仅恢复原本处于播放状态的动画。
	 */
	@Override
	public void onResume() {
		active = true;
		refreshVisibility();
	}

	/**
	 * 活动销毁时停止工作，不在正在迭代的销毁监听列表中移除自身。
	 */
	@Override
	public void onDestroy() {
		release(false);
	}

	/**
	 * 显式销毁时取消全部任务、移除监听及视图；重复调用不产生副作用。
	 */
	@Override
	public void 销毁() {
		if (destroyed) {
			return;
		}

		release(true);
		super.销毁();
	}

	/**
	 * 取消载入并释放图像与生命周期资源。
	 *
	 * @param removeDestroyListener 是否移除销毁监听器
	 */
	private void release(boolean removeDestroyListener) {
		if (destroyed) {
			return;
		}

		cancelLoad();
		destroyed = true;
		worker.shutdownNow();
		handler.removeCallbacksAndMessages(null);
		movie = null;
		info = null;
		image = "";
		lastError = "";
		loading = false;
		clock.clear(SystemClock.uptimeMillis());
		activity.removeOnPauseListener(this);
		activity.removeOnResumeListener(this);
		if (removeDestroyListener) {
			activity.removeOnDestroyListener(this);
		}
	}

	/**
	 * 确保组件未被释放。
	 */
	private void ensureActive() {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			throw new IllegalStateException("GIF 图片框须在界面线程操作");
		}
		if (destroyed) {
			throw new IllegalStateException("GIF 图片框已经销毁");
		}
	}

	/* 异步载入与请求隔离 */

	/**
	 * 使当前载入任务失效并取消后台工作。
	 */
	private void cancelLoad() {
		generation++;
		if (request != null) {
			request.cancel(true);
			request = null;
		}
		worker.getQueue().clear();
	}

	/**
	 * 启动图片载入并用任务标识隔离过期结果。
	 *
	 * @param source 输入数据或来源
	 * @param bytes 输入字节数据
	 */
	private void load(final String source, byte[] bytes) {
		cancelLoad();
		loading = true;
		lastError = "";
		final int token = generation;

		// 字节输入在调用边界复制，之后修改原数组不影响后台请求。
		if (bytes != null && bytes.length <= GifInfo.MAX_BYTES) {
			bytes = bytes.clone();
		}
		final byte[] snapshot = bytes;
		request = worker.submit(new Runnable() {

			@Override
			public void run() {
				try {
					byte[] data = source.length() == 0 ? snapshot : read(source);
					final GifInfo parsed = GifInfo.read(data);
					if (Thread.currentThread().isInterrupted() || generation != token || destroyed) {
						return;
					}

					final Movie decoded = Movie.decodeByteArray(data, 0, data.length);
					if (decoded == null || decoded.width() != parsed.width || decoded.height() != parsed.height) {
						throw new IOException("平台无法解码此 GIF");
					}

					// Movie 在后台创建，后续 setTime 和绘制仅由界面线程调用。
					if (!destroyed && generation == token) {
						handler.post(new Runnable() {

							@Override
							public void run() {
								if (destroyed || token != generation) {
									return;
								}

								movie = decoded;
								info = parsed;
								image = source;
								loading = false;
								request = null;
								clock.load(Math.max(1, decoded.duration()), parsed.frames > 1,
									autoPlay, SystemClock.uptimeMillis());
								clock.setLoop(loop);
								getView().requestLayout();
								refreshVisibility();
								载入成功(parsed.width, parsed.height, parsed.frames);
							}
						});
					}
				} catch (IOException exception) {
					fail(token, exception.getMessage());
				} catch (RuntimeException exception) {
					fail(token, "GIF 解码失败：" + exception.getClass().getSimpleName());
				} catch (OutOfMemoryError error) {
					fail(token, "GIF 解码内存不足，请减小图片");
				}
			}
		});
	}

	/**
	 * 仅在任务仍有效时报告载入失败。
	 *
	 * @param token 当前载入任务标识
	 * @param message 消息文本
	 */
	private void fail(final int token, String message) {
		final String detail = message == null ? "GIF 载入失败" : message;
		if (destroyed || token != generation) {
			return;
		}

		handler.post(new Runnable() {

			@Override
			public void run() {
				if (!destroyed && token == generation) {
					loading = false;
					request = null;
					lastError = detail;
					载入失败(detail);
				}
			}
		});
	}

	/**
	 * 读取本地文件、资产或 Android 资源中的 GIF 数据，不直接访问网址。
	 *
	 * @param source 输入数据或来源
	 * @return 完整的 GIF 原始字节数据。
	 * @throws IOException 访问文件、流或网络资源失败时
	 */
	private byte[] read(String source) throws IOException {
		if (source.contains("://")) {
			throw new IOException("请先取得 GIF 字节或文件，不直接载入网址及 content URI");
		}

		InputStream input;
		if (source.startsWith("/")) {
			input = new FileInputStream(source);
		} else if (source.startsWith("@raw/") || source.startsWith("@drawable/")) {
			int id = activity.getResources().getIdentifier(source.substring(1), null, activity.getPackageName());
			if (id == 0) {
				throw new IOException("GIF 资源不存在：" + source);
			}

			input = activity.getResources().openRawResource(id);
		} else {
			input = activity.getAssets().open(source);
		}

		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			byte[] buffer = new byte[8192];
			int count;
			while ((count = input.read(buffer)) != -1) {
				if (Thread.currentThread().isInterrupted()) {
					throw new IOException("GIF 载入已取消");
				}
				if (count > GifInfo.MAX_BYTES - output.size()) {
					throw new IOException("GIF 文件超过 8 MiB");
				}

				output.write(buffer, 0, count);
			}

			return output.toByteArray();
		} finally {
			input.close();
		}
	}

	/* 绘制与可见性 */

	/**
	 * 按视图可见性更新播放计时状态。
	 */
	private void refreshVisibility() {
		// Android 可能在父类构造期间派发视图回调，此时状态字段尚未初始化。
		if (handler == null || destroyed) {
			return;
		}

		View view = getView();
		boolean finished = clock.setVisible(active && view.getWindowToken() != null
			&& view.getWindowVisibility() == View.VISIBLE && view.isShown(), SystemClock.uptimeMillis());
		handler.removeCallbacks(ticker);
		if (movie != null && clock.isAdvancing()) {
			handler.postDelayed(ticker, 16);
		}
		view.invalidate();
		if (finished) {
			postCompletion();
		}
	}

	/**
	 * 将播放完成通知投递到主线程。
	 * <p>可见性回调可能发生在 Activity 正在遍历监听器时，完成事件延后派发。
	 */
	private void postCompletion() {
		final int token = generation;
		handler.post(new Runnable() {

			@Override
			public void run() {
				if (!destroyed && generation == token) {
					播放完毕();
				}
			}
		});
	}

	/**
	 * 根据播放时钟绘制 GIF，并随视图可见性调整播放。
	 */
	private final class GifCanvas extends View {

		/**
		 * 创建用于 GIF 绘制和播放可见性跟踪的视图。
		 *
		 * @param context Android 上下文
		 */
		GifCanvas(Context context) {
			super(context);
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}

		@Override
		protected void onAttachedToWindow() {
			super.onAttachedToWindow();
			refreshVisibility();
		}

		@Override
		protected void onDetachedFromWindow() {
			if (handler != null && !destroyed) {
				if (clock.setVisible(false, SystemClock.uptimeMillis())) {
					postCompletion();
				}
				handler.removeCallbacks(ticker);
			}
			super.onDetachedFromWindow();
		}

		@Override
		protected void onWindowVisibilityChanged(int visibility) {
			super.onWindowVisibilityChanged(visibility);
			refreshVisibility();
		}

		@Override
		protected void onVisibilityChanged(View changed, int visibility) {
			super.onVisibilityChanged(changed, visibility);
			refreshVisibility();
		}

		@Override
		protected void onMeasure(int widthSpec, int heightSpec) {
			int width = movie == null ? 160 : movie.width();
			int height = movie == null ? 100 : movie.height();
			setMeasuredDimension(resolveSize(width + getPaddingLeft() + getPaddingRight(), widthSpec),
				resolveSize(height + getPaddingTop() + getPaddingBottom(), heightSpec));
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			Movie current = movie;
			if (current == null) {
				return;
			}

			int width = getWidth() - getPaddingLeft() - getPaddingRight();
			int height = getHeight() - getPaddingTop() - getPaddingBottom();
			if (width <= 0 || height <= 0) {
				return;
			}

			float scaleX = width / (float) current.width();
			float scaleY = height / (float) current.height();
			if (scaleMode == 缩放_等比适应) {
				scaleX = scaleY = Math.min(scaleX, scaleY);
			} else if (scaleMode == 缩放_原始居中) {
				scaleX = scaleY = 1;
			}
			float left = getPaddingLeft() + (width - current.width() * scaleX) / 2;
			float top = getPaddingTop() + (height - current.height() * scaleY) / 2;
			int saved = canvas.save();
			canvas.clipRect(getPaddingLeft(), getPaddingTop(),
				getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
			canvas.translate(left, top);
			canvas.scale(scaleX, scaleY);
			current.setTime(clock.position());
			current.draw(canvas, 0, 0);
			canvas.restoreToCount(saved);
		}
	}
}
