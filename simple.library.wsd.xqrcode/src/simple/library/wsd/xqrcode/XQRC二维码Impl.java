package simple.library.wsd.xqrcode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import simple.library.wsd.xqrcode.internal.*;
import simple.runtime.android.MainActivity;
import simple.runtime.components.impl.组件Impl;
import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * 管理生成、图片识别和独立扫码页的请求序号及取消边界。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XQRC二维码Impl extends 组件Impl implements XQRC二维码, MainActivity.OnDestroyListener,
		MainActivity.OnActivityResultListener, ScanRegistry.Listener {
	private static final AtomicInteger PICKERS = new AtomicInteger(52000);
	private final MainActivity host;
	private final Context context;
	private final Handler handler = new Handler(Looper.getMainLooper());
	private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(4));
	private final Map<Integer, Job> jobs = new LinkedHashMap<>();
	private final int pickerCode = PICKERS.getAndIncrement();
	private int sequence;
	private int picking;
	private int scanId;
	private String scanToken = "";
	private boolean destroyed;

	/**
	 * 创建工作队列并监听所属活动的选择结果和销毁。
	 *
	 * @param container 所属窗口。
	 */
	public XQRC二维码Impl(组件容器 container) {
		super(container);
		host = MainActivity.getContext();
		context = host.getApplicationContext();
		host.addOnDestroyListener(this);
		host.addOnActivityResultListener(this);
	}

	/**
	 * 拒绝销毁后及非主线程调用。
	 */
	private void check() {
		if (destroyed || Looper.myLooper() != Looper.getMainLooper()) {
			throw new XQRC二维码错误("二维码组件仅可在主线程且销毁前使用");
		}
	}

	/**
	 * 分配不复用的请求编号。
	 *
	 * @return 正整数请求编号。
	 */
	private int nextId() {
		sequence = Math.incrementExact(sequence);
		return sequence;
	}

	@Override
	public int 当前扫描序号() {
		check();
		return scanId;
	}

	@Override
	public int 异步任务数() {
		check();
		return jobs.size();
	}

	@Override
	public byte[] 生成二维码(String content, XQRC二维码配置 options) {
		check();
		try {
			return Images.generate(context, content, options.copy());
		} catch (Exception error) {
			throw new IllegalArgumentException("二维码生成失败：" + message(error), error);
		}
	}

	@Override
	public XQRC识别结果 识别图片(String path, XQRC扫码配置 options) {
		check();
		try {
			return Images.recognize(Images.read(context, path), options.copy());
		} catch (Exception error) {
			throw new IllegalArgumentException("图片识别失败：" + message(error), error);
		}
	}

	@Override
	public XQRC识别结果 识别字节图片(byte[] data, XQRC扫码配置 options) {
		check();
		try {
			return Images.recognize(data, options.copy());
		} catch (Exception error) {
			throw new IllegalArgumentException("图片识别失败：" + message(error), error);
		}
	}

	@Override
	public int 异步生成(final String content, XQRC二维码配置 options) {
		check();
		final XQRC二维码配置 snapshot = options.copy();
		return submit("生成", new Work() {
			@Override
			public Object execute() throws Exception {
				return Images.generate(context, content, snapshot);
			}
		});
	}

	@Override
	public int 异步识别图片(final String path, XQRC扫码配置 options) {
		check();
		final XQRC扫码配置 snapshot = options.copy();
		return submit("识别", new Work() {
			@Override
			public Object execute() throws Exception {
				return Images.recognize(Images.read(context, path), snapshot);
			}
		});
	}

	@Override
	public int 异步识别字节图片(byte[] data, XQRC扫码配置 options) {
		check();
		if (data == null || data.length > 32 * 1024 * 1024) {
			throw new IllegalArgumentException("图片数据为空引用或超过 32 MiB");
		}
		final byte[] snapshot = data.clone();
		final XQRC扫码配置 config = options.copy();
		return submit("识别", new Work() {
			@Override
			public Object execute() throws Exception {
				return Images.recognize(snapshot, config);
			}
		});
	}

	/**
	 * 新建任务并将实际执行交给后台队列。
	 *
	 * @param operation 操作名称。
	 * @param work 后台工作。
	 * @return 请求编号。
	 */
	private int submit(String operation, Work work) {
		Job job = new Job(nextId(), operation, null);
		jobs.put(job.id, job);
		schedule(job, work);
		return job.id;
	}

	/**
	 * 调度已有请求，主线程完成时先移除请求再触发事件。
	 *
	 * @param job 本次任务。
	 * @param work 后台工作。
	 */
	private void schedule(final Job job, final Work work) {
		try {
			job.future = executor.submit(new Runnable() {
				@Override
				public void run() {
					Object value = null;
					Exception failure = null;
					try {
						value = work.execute();
					} catch (Exception error) {
						failure = error;
					} catch (OutOfMemoryError error) {
						failure = new IllegalStateException("可用内存不足，请减小图片尺寸后重试", error);
					}
					complete(job, value, failure);
				}
			});
		} catch (RejectedExecutionException error) {
			complete(job, null, new IllegalStateException("后台任务队列已满，请等待或取消后重试"));
		}
	}

	/**
	 * 将后台结果送回主线程，取消或销毁后的回调直接丢弃。
	 *
	 * @param job 本次任务。
	 * @param value 成功结果。
	 * @param error 失败原因。
	 */
	private void complete(final Job job, final Object value, final Exception error) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (destroyed || jobs.get(job.id) != job) {
					return;
				}
				jobs.remove(job.id);
				if (error != null) {
					任务失败(job.id, job.operation, message(error));
				} else if (value instanceof byte[]) {
					生成完成(job.id, (byte[]) value);
				} else {
					识别完成(job.id, (XQRC识别结果) value);
				}
			}
		});
	}

	@Override
	public int 选择图片识别(XQRC扫码配置 options) {
		check();
		if (picking != 0) {
			return 0;
		}
		Job job = new Job(nextId(), "选图识别", options.copy());
		jobs.put(job.id, job);
		picking = job.id;
		try {
			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("image/*");
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			host.startActivityForResult(intent, pickerCode);
		} catch (RuntimeException error) {
			picking = 0;
			complete(job, null, error);
		}
		return job.id;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != pickerCode || destroyed || picking == 0) {
			return;
		}
		final Job job = jobs.get(picking);
		picking = 0;
		if (job == null) {
			return;
		}
		if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
			jobs.remove(job.id);
			任务取消(job.id, job.operation);
			return;
		}
		final String uri = data.getData().toString();
		schedule(job, new Work() {
			@Override
			public Object execute() throws Exception {
				return Images.recognize(Images.read(context, uri), job.options);
			}
		});
	}

	@Override
	public boolean 取消任务(int requestId) {
		check();
		Job job = jobs.remove(requestId);
		if (job == null) {
			return false;
		}
		if (job.future != null) {
			job.future.cancel(true);
			executor.purge();
		}
		// 系统选择器可能仍显示；保留 picking 到结果返回，避免旧结果误投给新请求。
		任务取消(job.id, job.operation);
		return true;
	}

	@Override
	public int 开始扫描(XQRC扫码配置 options) {
		check();
		if (scanId != 0) {
			return 0;
		}
		final int id = nextId();
		try {
			if (options == null) {
				throw new XQRC二维码错误("扫码配置为空");
			}
			int theme = options.主题资源();
			try {
				if (theme != 0
						&& !"style".equals(context.getResources().getResourceTypeName(theme))) {
					throw new XQRC二维码错误("主题必须为style资源");
				}
			} catch (android.content.res.Resources.NotFoundException error) {
				throw new XQRC二维码错误("主题资源不存在：" + theme);
			}
			scanToken = ScanRegistry.create(id, options.copy(), this);
			scanId = id;
			Intent intent = new Intent(host, ScanActivity.class);
			intent.putExtra("session", scanToken);
			host.startActivity(intent);
		} catch (final RuntimeException error) {
			if (scanId == id) {
				ScanRegistry.stop(scanToken, false);
				scanId = 0;
				scanToken = "";
			}
			handler.post(
					new Runnable() {
						@Override
						public void run() {
							if (!destroyed) {
								扫描失败(id, message(error));
							}
						}
					});
		}
		return id;
	}

	@Override
	public boolean 停止扫描(int requestId) {
		check();
		return requestId != 0 && requestId == scanId && ScanRegistry.stop(scanToken, true);
	}

	@Override
	public boolean 暂停扫描(int requestId) {
		check();
		return requestId != 0 && requestId == scanId && ScanRegistry.control(scanToken, 0);
	}

	@Override
	public boolean 继续扫描(int requestId) {
		check();
		return requestId != 0 && requestId == scanId && ScanRegistry.control(scanToken, 1);
	}

	@Override
	public boolean 设置扫描补光(int requestId, boolean enabled) {
		check();
		return requestId != 0 && requestId == scanId && ScanRegistry.control(scanToken, enabled ? 3 : 2);
	}

	@Override
	public void scanReady(int id) {
		if (scanId == id) {
			扫描准备就绪(id);
		}
	}

	@Override
	public void scanResult(int id, XQRC识别结果 result, boolean finished) {
		if (scanId == id) {
			if (finished) {
				scanId = 0;
				scanToken = "";
			}
			扫描得到结果(id, result);
		}
	}

	@Override
	public void scanError(int id, String reason, boolean finished) {
		if (scanId == id) {
			if (finished) {
				scanId = 0;
				scanToken = "";
			}
			扫描失败(id, reason);
		}
	}

	@Override
	public void scanCancelled(int id) {
		if (scanId == id) {
			scanId = 0;
			scanToken = "";
			扫描取消(id);
		}
	}

	@Override
	public void onDestroy() {
		销毁();
	}

	@Override
	public void 销毁() {
		if (!destroyed) {
			destroyed = true;
			host.removeOnDestroyListener(this);
			host.removeOnActivityResultListener(this);
			ScanRegistry.stop(scanToken, false);
			scanToken = "";
			scanId = 0;
			for (Job job : jobs.values()) {
				if (job.future != null) {
					job.future.cancel(true);
				}
			}
			jobs.clear();
			executor.shutdownNow();
			handler.removeCallbacksAndMessages(null);
			super.销毁();
		}
	}

	/**
	 * 为异常生成非空说明。
	 *
	 * @param error 失败异常。
	 * @return 可显示的原因。
	 */
	private static String message(Exception error) {
		return error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
	}

	/**
	 * 单个后台操作。
	 */
	private interface Work {
		/**
		 * 执行实际工作。
		 *
		 * @return 图片或识别结果。
		 * @throws Exception 输入或编码错误。
		 */
		Object execute() throws Exception;
	}

	/**
	 * 独立请求的执行句柄及配置。
	 */
	private static final class Job {
		final int id;
		final String operation;
		final XQRC扫码配置 options;
		Future<?> future;

		/**
		 * 创建请求记录。
		 *
		 * @param requestId 编号。
		 * @param name 操作名。
		 * @param config 选图配置，普通任务可为空。
		 */
		Job(int requestId, String name, XQRC扫码配置 config) {
			id = requestId;
			operation = name;
			options = config;
		}
	}
	@Override
	public void 生成完成(int requestId, byte[] image) {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "生成完成" , requestId, image);
		}
	}

	@Override
	public void 识别完成(int requestId, XQRC识别结果 result) {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "识别完成" , requestId, result);
		}
	}

	@Override
	public void 任务失败(int requestId, String operation, String reason) {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "任务失败" , requestId, operation, reason);
		}
	}

	@Override
	public void 任务取消(int requestId, String operation) {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "任务取消" , requestId, operation);
		}
	}

	@Override
	public void 扫描准备就绪(int requestId) {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "扫描准备就绪" , requestId);
		}
	}

	@Override
	public void 扫描得到结果(int requestId, XQRC识别结果 result) {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "扫描得到结果" , requestId, result);
		}
	}

	@Override
	public void 扫描失败(int requestId, String reason) {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "扫描失败" , requestId, reason);
		}
	}

	@Override
	public void 扫描取消(int requestId) {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "扫描取消" , requestId);
		}
	}

	@Override
	public void 扫描对焦完成(int requestId, boolean success) {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "扫描对焦完成", requestId, success);
		}
	}

	@Override
	public void 扫描发现识别点(int requestId, float[] points) {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "扫描发现识别点", requestId, points.clone());
		}
	}

	@Override
	public boolean 触发扫描对焦(int requestId) {
		check();
		return requestId != 0 && requestId == scanId && ScanRegistry.control(scanToken, 4);
	}

	@Override
	public String 取扫描状态(int requestId) {
		check();
		return requestId != 0 && requestId == scanId ? ScanRegistry.state(scanToken) : "未开始";
	}

	@Override
	public boolean 取扫描补光状态(int requestId) {
		check();
		return requestId != 0 && requestId == scanId && ScanRegistry.light(scanToken);
	}

	@Override
	public XQRC扫码配置 取得扫描配置(int requestId) {
		check();
		return requestId != 0 && requestId == scanId ? ScanRegistry.configuration(scanToken) : null;
	}

	@Override
	public int[] 取扫描区域(int requestId) {
		check();
		return requestId != 0 && requestId == scanId
				? ScanRegistry.bounds(scanToken, false)
				: new int[0];
	}

	@Override
	public int[] 取预览识别区域(int requestId) {
		check();
		return requestId != 0 && requestId == scanId
				? ScanRegistry.bounds(scanToken, true)
				: new int[0];
	}

	@Override
	public void scanFocused(int id, boolean success) {
		if (scanId == id) {
			扫描对焦完成(id, success);
		}
	}

	@Override
	public void scanPoints(int id, float[] points) {
		if (scanId == id) {
			扫描发现识别点(id, points);
		}
	}
}
