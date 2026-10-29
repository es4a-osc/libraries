package simple.library.wsd.xui;

import java.util.Collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.view.View;

import com.xuexiang.xui.widget.textview.MarqueeTextView;
import com.xuexiang.xui.widget.textview.marqueen.DisplayEntity;

import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;
import simple.runtime.events.EventDispatcher;

/**
 * XUI跑马灯文本的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI跑马灯文本Impl extends XUI文本组件基类 implements XUI跑马灯文本 {

	private long messageGeneration;
	private Runnable startNotification;
	private Runnable finishNotification;


	private Runnable expiryTask;
	private boolean disposed;

	private boolean repeating;

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private MarqueeTextView view;

	/**
	 * 创建XUI跑马灯文本。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI跑马灯文本Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		expiryTask = new Runnable() {
			@Override
			public void run() {
				expireMessages();
			}
		};
		view = new MarqueeTextView(ComponentSupport.createContext()) {
			@Override
			protected void onAttachedToWindow() {
				super.onAttachedToWindow();
				scheduleExpiry();
			}

			@Override
			protected void onDetachedFromWindow() {
				cancelNotifications();
				removeCallbacks(expiryTask);
				super.onDetachedFromWindow();
			}
		};
		repeating = true;

		view.setOnMarqueeListener(new MarqueeTextView.OnMarqueeListener() {
			@Override
			public DisplayEntity onStartMarquee(DisplayEntity entity, int index) {
				if (entity instanceof TimedMessage && ((TimedMessage) entity).isExpired()) {
					// 让统一过期边界移除并通知，避免原生自行删项而遗漏过期事件。
					view.removeCallbacks(expiryTask);
					view.post(expiryTask);
					return entity;
				}
				postStarted(entity, index);
				return entity;
			}

			@Override
			public List<DisplayEntity> onMarqueeFinished(List<DisplayEntity> previous) {
				postFinished();
				return repeating && view.getDisplaySize() > 0 ? new ArrayList<DisplayEntity>(currentMessages()) : null;
			}
		});
		return view;
	}

	@Override
	public void 开始滚动() {
		view.startRoll();
	}

	@Override
	public void 置视图宽度(int viewWidth) {
		view.setViewWidth(viewWidth);
	}

	@Override
	public float 取当前位置() {
		return view.getCurrentPosition();
	}

	@Override
	public void 置当前位置(float coordinateX) {
		view.setCurrentPosition(coordinateX);
	}

	@Override
	public int 取滚动宽度() {
		return view.getScrollWidth();
	}

	@Override
	public void 置滚动宽度(int scrollWidth) {
		view.setScrollWidth(scrollWidth);
	}

	@Override
	public int 取速度() {
		return view.getSpeed();
	}

	@Override
	public void 置速度(int speed) {
		view.setSpeed(speed);
	}

	@Override
	public boolean 是否滚动中() {
		return view.isRolling();
	}

	@Override
	public int 取当前索引() {
		return view.getCurrentIndex();
	}

	@Override
	public boolean 是否含有显示消息() {
		return view.hasDisplayMessage();
	}

	@Override
	public void 清空() {
		cancelNotifications();
		view.removeCallbacks(expiryTask);
		view.clear();
	}

	@Override
	public void 置公告数组(String[] items) {
		String[] data = ComponentSupport.copyTextArray(items);
		cancelNotifications();
		view.clear();
		view.setDisplaySimpleList(Arrays.asList(data));
		scheduleExpiry();
	}

	@Override
	public void 置循环(boolean enabled) {
		repeating = enabled;
	}

	@Override
	public int 取公告数量() {
		return view.getDisplaySize();
	}

	@Override
	public void 插入公告(int index, String text) {
		ComponentSupport.validateIndex(index, view.getDisplaySize() + 1);
		if (text == null || text.isEmpty()) {
			throw new 非法参数错误();
		}
		List<DisplayEntity> data = new ArrayList<DisplayEntity>(currentMessages());
		data.add(index, new DisplayEntity(text));
		replaceMessages(data);
	}

	@Override
	public void 移除公告(int index) {
		ComponentSupport.validateIndex(index, view.getDisplaySize());
		List<DisplayEntity> data = new ArrayList<DisplayEntity>(currentMessages());
		data.remove(index);
		replaceMessages(data);
	}

	/**
	 * 先停止旧一轮再替换消息，避免当前位置引用已移除的项目。
	 *
	 * @param data 新消息副本。
	 */
	private void replaceMessages(List<DisplayEntity> data) {
		cancelNotifications();
		view.clear();
		view.setDisplayList(data);
		if (!data.isEmpty()) {
			view.startRoll();
		}
		scheduleExpiry();
	}

	@Override
	public void 滚动开始(int index, String text) {
		EventDispatcher.dispatchEvent(this, "滚动开始", index, text);
	}

	@Override
	public void 滚动结束() {
		EventDispatcher.dispatchEvent(this, "滚动结束");
	}

	@Override
	public void 追加定时公告(String identifier, String text, long expiresAt) {
		if (identifier == null || identifier.isEmpty() || text == null || text.isEmpty()
			|| expiresAt < 0 || (expiresAt != 0 && expiresAt <= System.currentTimeMillis())) {
			throw new 非法参数错误();
		}
		List<DisplayEntity> data = new ArrayList<DisplayEntity>(currentMessages());
		int existing = -1;
		for (int index = 0; index < data.size(); index++) {
			if (identifier.equals(data.get(index).getID())) {
				existing = index;
				break;
			}
		}
		DisplayEntity message = new TimedMessage(identifier, text, expiresAt);
		if (existing < 0) {
			data.add(message);
		} else {
			data.set(existing, message);
		}
		replaceMessages(data);
	}

	@Override
	public boolean 按标识移除公告(String identifier) {
		if (identifier == null || identifier.isEmpty()) {
			throw new 非法参数错误();
		}
		List<DisplayEntity> data = new ArrayList<DisplayEntity>(currentMessages());
		for (int index = 0; index < data.size(); index++) {
			if (identifier.equals(data.get(index).getID())) {
				data.remove(index);
				replaceMessages(data);
				return true;
			}
		}
		return false;
	}

	@Override
	public String 取公告标识(int index) {
		ComponentSupport.validateIndex(index, view.getDisplaySize());
		String identifier = view.getDisplayItem(index).getID();
		return identifier == null ? "" : identifier;
	}

	@Override
	public void 公告开始(String identifier, int index, String text) {
		EventDispatcher.dispatchEvent(this, "公告开始", identifier, index, text);
	}

	@Override
	public void 公告已过期(String identifier) {
		EventDispatcher.dispatchEvent(this, "公告已过期", identifier);
	}

	/**
	 * 为原生消息增加真实截止时间，保留其文字与滚动实现。
	 */
	private static final class TimedMessage extends DisplayEntity {
		final long expiresAt;

		TimedMessage(String identifier, String text, long expiresAt) {
			super(identifier, text, System.currentTimeMillis(), 0);
			this.expiresAt = expiresAt;
		}

		boolean isExpired() {
			return expiresAt > 0 && System.currentTimeMillis() >= expiresAt;
		}
	}

	/**
	 * 一个组件只保留最近截止时间的任务，离开窗口和销毁时取消。
	 */
	private void scheduleExpiry() {
		if (view == null || expiryTask == null) {
			return;
		}
		view.removeCallbacks(expiryTask);
		if (disposed || !view.isAttachedToWindow()) {
			return;
		}
		long earliest = Long.MAX_VALUE;
		for (DisplayEntity message : currentMessages()) {
			if (message instanceof TimedMessage && ((TimedMessage) message).expiresAt > 0) {
				earliest = Math.min(earliest, ((TimedMessage) message).expiresAt);
			}
		}
		if (earliest != Long.MAX_VALUE) {
			view.postDelayed(expiryTask, Math.max(1, earliest - System.currentTimeMillis()));
		}
	}

	/**
	 * 先替换数据再通知过期，事件中的新操作不会被旧任务覆盖。
	 */
	private void expireMessages() {
		if (disposed) {
			return;
		}
		List<DisplayEntity> valid = new ArrayList<DisplayEntity>();
		List<String> expired = new ArrayList<String>();
		for (DisplayEntity message : new ArrayList<DisplayEntity>(currentMessages())) {
			if (message instanceof TimedMessage && ((TimedMessage) message).isExpired()) {
				expired.add(message.getID());
			} else {
				valid.add(message);
			}
		}
		if (!expired.isEmpty()) {
			replaceMessages(valid);
		} else {
			scheduleExpiry();
		}
		for (String identifier : expired) {
			公告已过期(identifier);
		}
	}

	@Override
	public void 销毁() {
		disposed = true;
		cancelNotifications();
		view.removeCallbacks(expiryTask);
		super.销毁();
	}

	/**
	 * 上游在从未配置消息时返回空引用，业务侧统一按空列表处理。
	 *
	 * @return 当前有效的数据列表，不在此处修改。
	 */
	private List<DisplayEntity> currentMessages() {
		List<DisplayEntity> data = view.getDisplayList();
		return data == null ? Collections.<DisplayEntity>emptyList() : data;
	}


	/**
	 * 上游在回调返回后仍按旧索引写列表；将用户事件移到该写入完成后分派。
	 * 这样事件中清空、替换或销毁不会使原生继续操作已经失效的索引。
	 *
	 * @param entity 本轮原生实际开始的消息。
	 * @param index 消息位置。
	 */
	private void postStarted(final DisplayEntity entity, final int index) {
		if (startNotification != null) {
			view.removeCallbacks(startNotification);
		}
		final long generation = messageGeneration;
		startNotification = new Runnable() {
			@Override
			public void run() {
				startNotification = null;
				if (disposed || generation != messageGeneration || !view.isAttachedToWindow()
					|| index >= currentMessages().size() || currentMessages().get(index) != entity) {
					return;
				}
				滚动开始(index, entity.toString());
				if (!disposed && generation == messageGeneration) {
					公告开始(entity.getID() == null ? "" : entity.getID(), index, entity.toString());
				}
			}
		};
		view.post(startNotification);
	}

	/**
	 * 原生完成换轮后再通知，事件内配置的新消息不能被旧轮返回值覆盖。
	 */
	private void postFinished() {
		if (finishNotification != null) {
			view.removeCallbacks(finishNotification);
		}
		final long generation = messageGeneration;
		finishNotification = new Runnable() {
			@Override
			public void run() {
				finishNotification = null;
				if (!disposed && generation == messageGeneration && view.isAttachedToWindow()) {
					滚动结束();
				}
			}
		};
		view.post(finishNotification);
	}

	/**
	 * 替换数据或离开视图树时注销尚未分派的旧轮通知，不留下迟到事件。
	 */
	private void cancelNotifications() {
		messageGeneration++;
		if (startNotification != null) {
			view.removeCallbacks(startNotification);
			startNotification = null;
		}
		if (finishNotification != null) {
			view.removeCallbacks(finishNotification);
			finishNotification = null;
		}
	}

}
