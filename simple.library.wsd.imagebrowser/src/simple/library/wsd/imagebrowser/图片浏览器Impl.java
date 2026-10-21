package simple.library.wsd.imagebrowser;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.LinearLayout;
import android.widget.TextView;

import simple.library.wsd.imagebrowser.widget.PhotoView;
import simple.runtime.android.MainActivity;
import simple.runtime.components.组件容器;
import simple.runtime.components.impl.组件Impl;
import simple.runtime.events.EventDispatcher;

/**
 * 管理图片浏览弹窗、页面选择和异步图片读取。
 *
 * <p>仅保留当前页位图，相邻页只创建视图；关闭或切页立即使旧请求失效。
 * 网络读取遵守系统 TLS 和明文访问策略，不修改应用的全局图片加载配置。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 图片浏览器Impl extends 组件Impl implements 图片浏览器 {

	private final MainActivity activity;
	private final Handler handler = new Handler(Looper.getMainLooper());
	private final ThreadPoolExecutor worker;

	private Browser browser;
	private String lastError = "";
	private boolean destroyed;

	private final MainActivity.OnDestroyListener destroyListener = new MainActivity.OnDestroyListener() {
		@Override
		public void onDestroy() {
			// Activity 正在遍历监听列表，不能在回调内移除监听器。
			dispose(false);
		}
	};

	/**
	 * 创建单线程读取队列，并监听宿主销毁。
	 *
	 * @param container 容纳非可视组件的窗口。
	 */
	public 图片浏览器Impl(组件容器 container) {
		super(container);
		activity = MainActivity.getContext();
		worker = new ThreadPoolExecutor(1, 1, 15, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(1), new ThreadFactory() {
				@Override
				public Thread newThread(Runnable action) {
					return new Thread(action, "Simple-ImageBrowser");
				}
			});
		worker.allowCoreThreadTimeOut(true);
		activity.addOnDestroyListener(destroyListener);
	}

	/* 组件事件 */

	/** {@inheritDoc} */
	@Override
	public void 图片改变(int index) {
		EventDispatcher.dispatchEvent(this, "图片改变", index);
	}

	/** {@inheritDoc} */
	@Override
	public void 载入成功(int index) {
		EventDispatcher.dispatchEvent(this, "载入成功", index);
	}

	/** {@inheritDoc} */
	@Override
	public void 载入失败(int index, String message) {
		EventDispatcher.dispatchEvent(this, "载入失败", index, message);
	}

	/** {@inheritDoc} */
	@Override
	public void 浏览关闭(int index) {
		EventDispatcher.dispatchEvent(this, "浏览关闭", index);
	}

	/* 组件属性 */

	/** {@inheritDoc} */
	@Override
	public boolean 正在浏览() {
		return browser != null && browser.dialog.isShowing();
	}

	/** {@inheritDoc} */
	@Override
	public int 当前索引() {
		return browser == null ? -1 : browser.index;
	}

	/** {@inheritDoc} */
	@Override
	public String 错误信息() {
		return lastError;
	}

	/* 组件方法 */

	/** {@inheritDoc} */
	@Override
	public void 浏览图片(String image) {
		浏览图片(new String[] {image}, 0);
	}

	/** {@inheritDoc} */
	@Override
	public void 浏览图片(String[] images, int startIndex) {
		checkThread();
		if (images == null || images.length == 0 || images.length > 1000) {
			fail(-1, "图片数量必须在 1 到 1000 之间。");
			return;
		}
		if (startIndex < 0 || startIndex >= images.length) {
			fail(startIndex, "起始图片索引越界。");
			return;
		}

		String[] sources = images.clone();
		for (int i = 0; i < sources.length; i++) {
			if (sources[i] == null || sources[i].trim().length() == 0) {
				fail(i, "图片来源不能为空。");
				return;
			}
		}

		lastError = "";
		close(false);
		try {
			Browser next = new Browser(sources);
			browser = next;
			next.show(startIndex);
		} catch (RuntimeException e) {
			close(false);
			fail(startIndex, "无法打开图片浏览界面：" + message(e));
		}
	}

	/** {@inheritDoc} */
	@Override
	public void 切换图片(int index) {
		checkThread();
		if (browser == null || index < 0 || index >= browser.sources.length) {
			fail(index, "浏览界面尚未打开或图片索引越界。");
			return;
		}
		browser.pager.setCurrentItem(index, false);
	}

	/** {@inheritDoc} */
	@Override
	public void 重新载入() {
		checkThread();
		if (browser == null) {
			fail(-1, "浏览界面尚未打开。");
			return;
		}
		browser.load();
	}

	/** {@inheritDoc} */
	@Override
	public void 关闭() {
		checkThread();
		close(true);
	}

	/**
	 * 关闭界面、终止读取队列并注销事件，不派发浏览关闭事件。
	 */
	@Override
	public void 销毁() {
		if (destroyed) {
			return;
		}
		checkThread();
		dispose(true);
	}

	/* 生命周期和错误边界 */

	private void checkThread() {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			throw new IllegalStateException("图片浏览器只能在界面线程调用。");
		}
		if (destroyed) {
			throw new IllegalStateException("图片浏览器已经销毁。");
		}
	}

	private void fail(int index, String error) {
		lastError = error;
		载入失败(index, error);
	}

	private static String message(Throwable error) {
		return error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
	}

	private void close(boolean notify) {
		Browser previous = browser;
		if (previous != null) {
			previous.notifyClose = notify;
			previous.dialog.dismiss();
			// OnDismiss 可能排队投递；主动关闭必须在返回前完成状态清理。
			previous.finish();
		}
	}

	private void dispose(boolean removeListener) {
		destroyed = true;
		close(false);
		worker.shutdownNow();
		if (removeListener) {
			activity.removeOnDestroyListener(destroyListener);
		}
		super.销毁();
	}

	/* 每次打开的界面持有独立状态，旧界面不能覆盖新界面。 */

	private final class Browser {
		private final String[] sources;
		private final Dialog dialog;
		private final ViewPager pager;
		private final TextView title;
		private final TextView status;
		private final LinearLayout feedback;
		private final TextView retry;
		private final ProgressBar progress;
		private final SparseArray<PhotoView> pages = new SparseArray<PhotoView>();

		private int index = -1;
		private int generation;
		private boolean ready;
		private boolean released;
		private boolean notifyClose = true;
		private Bitmap bitmap;
		private Future<?> request;

		Browser(String[] images) {
			sources = images;
			dialog = new Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
			FrameLayout root = new FrameLayout(activity);
			root.setBackgroundColor(Color.BLACK);

			pager = new ViewPager(activity) {
				@Override
				public boolean onInterceptTouchEvent(MotionEvent event) {
					// 老支持库在多指切换时可能收到已离开的指针索引。
					try {
						return super.onInterceptTouchEvent(event);
					} catch (IllegalArgumentException e) {
						return false;
					}
				}
			};
			root.addView(pager, new FrameLayout.LayoutParams(-1, -1));

			// 图片铺满可用区域；控件覆盖其上，不占用底部浏览空间。
			FrameLayout header = new FrameLayout(activity);
			FrameLayout.LayoutParams headerLayout = new FrameLayout.LayoutParams(-1, dp(64), Gravity.TOP);
			headerLayout.topMargin = dp(8);
			root.addView(header, headerLayout);
			title = label();
			title.setTextSize(14);
			setBackground(title, pill(0x99000000, 0));
			title.setPadding(dp(16), 0, dp(16), 0);
			header.addView(title, new FrameLayout.LayoutParams(-2, dp(36), Gravity.CENTER));
			ImageButton closeButton = new ImageButton(activity);
			closeButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
			closeButton.setColorFilter(Color.WHITE);
			closeButton.setContentDescription("关闭");
			closeButton.setPadding(dp(12), dp(12), dp(12), dp(12));
			setBackground(closeButton, actionBackground(0x99000000, 0));
			closeButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					dialog.dismiss();
				}
			});
			FrameLayout.LayoutParams closeLayout = new FrameLayout.LayoutParams(dp(48), dp(48), Gravity.LEFT | Gravity.CENTER_VERTICAL);
			closeLayout.leftMargin = dp(16);
			header.addView(closeButton, closeLayout);

			feedback = new LinearLayout(activity);
			feedback.setOrientation(LinearLayout.VERTICAL);
			feedback.setGravity(Gravity.CENTER);
			feedback.setPadding(dp(24), dp(20), dp(24), dp(20));
			progress = new ProgressBar(activity);
			feedback.addView(progress, new LinearLayout.LayoutParams(dp(32), dp(32)));
			status = label();
			status.setTextSize(15);
			status.setTextColor(0xFFB8BCC4);
			status.setMaxLines(4);
			status.setPadding(0, dp(16), 0, dp(20));
			feedback.addView(status, new LinearLayout.LayoutParams(-1, -2));
			retry = label();
			retry.setText("重新载入");
			retry.setTextSize(14);
			retry.setPadding(dp(24), 0, dp(24), 0);
			setBackground(retry, actionBackground(0xFF20242C, 0xFF555B66));
			retry.setFocusable(true);
			retry.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					load();
				}
			});
			feedback.addView(retry, new LinearLayout.LayoutParams(-2, dp(48)));
			root.addView(feedback, new FrameLayout.LayoutParams(-1, -2, Gravity.CENTER));
			dialog.setContentView(root);
			dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dismissed) {
					finish();
				}
			});
		}

		void show(int start) {
			dialog.show();
			dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.MATCH_PARENT);
			pager.setAdapter(new PagerAdapter() {
				@Override
				public int getCount() {
					return sources.length;
				}

				@Override
				public boolean isViewFromObject(View view, Object object) {
					return view == object;
				}

				@Override
				public Object instantiateItem(ViewGroup parent, int position) {
					PhotoView image = new PhotoView(activity);
					image.enable();
					image.setContentDescription("图片 " + (position + 1));
					pages.put(position, image);
					if (position == index && bitmap != null) {
						image.setImageBitmap(bitmap);
						image.setContentDescription("图片 " + (position + 1) + "，已载入");
					}
					parent.addView(image, new ViewGroup.LayoutParams(-1, -1));
					return image;
				}

				@Override
				public void destroyItem(ViewGroup parent, int position, Object object) {
					PhotoView image = (PhotoView) object;
					image.setImageDrawable(null);
					parent.removeView(image);
					pages.remove(position);
				}
			});
			pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
				@Override
				public void onPageSelected(int position) {
					if (ready) {
						select(position);
					}
				}
			});
			pager.setCurrentItem(start, false);
			ready = true;
			select(start);
		}

		void select(int position) {
			if (released || index == position) {
				return;
			}
			index = position;
			title.setText((index + 1) + " / " + sources.length);
			load();
			图片改变(index);
		}

		void cancel() {
			generation++;
			if (request != null) {
				request.cancel(true);
				request = null;
			}
			worker.getQueue().clear();
		}

		void load() {
			if (released || browser != this) {
				return;
			}
			cancel();
			bitmap = null;
			for (int i = 0; i < pages.size(); i++) {
				pages.valueAt(i).setImageDrawable(null);
				pages.valueAt(i).setContentDescription("图片 " + (pages.keyAt(i) + 1));
			}
			feedback.setVisibility(View.VISIBLE);
			progress.setVisibility(View.VISIBLE);
			retry.setVisibility(View.GONE);
			status.setText("正在载入…");
			lastError = "";
			final int token = generation;
			final int position = index;
			request = worker.submit(new Runnable() {
				@Override
				public void run() {
					Bitmap result = null;
					String error = null;
					try {
						result = ImageReader.read(activity, sources[position]);
					} catch (Exception e) {
						error = message(e);
					} catch (OutOfMemoryError e) {
						error = "可用内存不足，无法载入图片。";
					}
					final Bitmap decoded = result;
					final String failure = error;
					handler.post(new Runnable() {
						@Override
						public void run() {
							if (released || destroyed || browser != Browser.this || token != generation) {
								if (decoded != null) {
									decoded.recycle();
								}
								return;
							}
							request = null;
							if (decoded == null) {
								String reason = failure == null ? "无法解码图片。" : failure;
								progress.setVisibility(View.GONE);
								retry.setVisibility(View.VISIBLE);
								status.setText("图片载入失败\n" + reason);
								fail(position, reason);
							} else {
								bitmap = decoded;
								PhotoView page = pages.get(position);
								if (page != null) {
									page.setImageBitmap(decoded);
									page.setContentDescription("图片 " + (position + 1) + "，已载入");
								}
								feedback.setVisibility(View.GONE);
								lastError = "";
								载入成功(position);
							}
						}
					});
				}
			});
		}

		void finish() {
			if (released) {
				return;
			}
			released = true;
			cancel();
			for (int i = 0; i < pages.size(); i++) {
				pages.valueAt(i).setImageDrawable(null);
				pages.valueAt(i).setContentDescription("图片 " + (pages.keyAt(i) + 1));
			}
			pages.clear();
			bitmap = null;
			pager.setAdapter(null);
			if (browser == this) {
				browser = null;
				if (notifyClose && !destroyed) {
					浏览关闭(index);
				}
			}
		}

		private TextView label() {
			TextView view = new TextView(activity);
			view.setTextColor(Color.WHITE);
			view.setTextSize(16);
			view.setGravity(Gravity.CENTER);
			view.setPadding(dp(8), 0, dp(8), 0);
			return view;
		}

		private GradientDrawable pill(int color, int stroke) {
			GradientDrawable background = new GradientDrawable();
			background.setColor(color);
			background.setCornerRadius(dp(24));
			if (stroke != 0) {
				background.setStroke(dp(1), stroke);
			}
			return background;
		}

		private StateListDrawable actionBackground(int color, int stroke) {
			StateListDrawable states = new StateListDrawable();
			states.addState(new int[] {android.R.attr.state_pressed}, pill(0xFF3C424D, 0xFFABB5C5));
			states.addState(new int[] {android.R.attr.state_focused}, pill(0xFF252B35, 0xFFABB5C5));
			states.addState(new int[0], pill(color, stroke));
			return states;
		}

		@SuppressWarnings("deprecation")
		private void setBackground(View view, Drawable background) {
			// setBackground 从 API 16 才提供，保留 API 14 的等效调用。
			view.setBackgroundDrawable(background);
		}

		private int dp(int value) {
			return (int) (value * activity.getResources().getDisplayMetrics().density + 0.5f);
		}
	}
}
