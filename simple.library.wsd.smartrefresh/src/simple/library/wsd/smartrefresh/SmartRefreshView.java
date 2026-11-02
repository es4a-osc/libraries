package simple.library.wsd.smartrefresh;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.RefreshState;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;
import com.scwang.smart.refresh.layout.simple.SimpleMultiListener;

/**
 * 保留原生刷新与回弹实现，把附着状态和回调交给 Simple 组件协调。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class SmartRefreshView extends SmartRefreshLayout {

	private Listener listener;
	private boolean available;
	private boolean attachedBefore;
	private final Handler stateCallbacks = new Handler(Looper.getMainLooper());

	/**
	 * 创建本实例独立的经典头尾，不改变上游全局默认配置。
	 *
	 * @param context 当前 Simple 活动上下文。
	 */
	SmartRefreshView(Context context) {
		super(context);
		setRefreshHeader(new ClassicsHeader(context).setEnableLastTime(false));
		setRefreshFooter(new ClassicsFooter(context));
		setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
			@Override
			public void onRefresh(RefreshLayout layout) {
				if (available && listener != null) {
					listener.started(true);
				}
			}

			@Override
			public void onLoadMore(RefreshLayout layout) {
				if (available && listener != null) {
					listener.started(false);
				}
			}
		});
		setOnMultiListener(new SimpleMultiListener() {
			@Override
			public void onHeaderMoving(com.scwang.smart.refresh.layout.api.RefreshHeader header, boolean dragging,
					float percent, int offset, int height, int maxHeight) {
				if (available && listener != null) {
					listener.moving(true, dragging, percent, offset, height, maxHeight);
				}
			}

			@Override
			public void onFooterMoving(com.scwang.smart.refresh.layout.api.RefreshFooter footer, boolean dragging,
					float percent, int offset, int height, int maxHeight) {
				if (available && listener != null) {
					listener.moving(false, dragging, percent, offset, height, maxHeight);
				}
			}

			@Override
			public void onHeaderReleased(com.scwang.smart.refresh.layout.api.RefreshHeader header, int height, int maxHeight) {
				notifyPhase(true, 0, height, maxHeight, false);
			}

			@Override
			public void onFooterReleased(com.scwang.smart.refresh.layout.api.RefreshFooter footer, int height, int maxHeight) {
				notifyPhase(false, 0, height, maxHeight, false);
			}

			@Override
			public void onHeaderStartAnimator(com.scwang.smart.refresh.layout.api.RefreshHeader header, int height, int maxHeight) {
				notifyPhase(true, 1, height, maxHeight, false);
			}

			@Override
			public void onFooterStartAnimator(com.scwang.smart.refresh.layout.api.RefreshFooter footer, int height, int maxHeight) {
				notifyPhase(false, 1, height, maxHeight, false);
			}

			@Override
			public void onHeaderFinish(com.scwang.smart.refresh.layout.api.RefreshHeader header, boolean success) {
				notifyPhase(true, 2, 0, 0, success);
			}

			@Override
			public void onFooterFinish(com.scwang.smart.refresh.layout.api.RefreshFooter footer, boolean success) {
				notifyPhase(false, 2, 0, 0, success);
			}

			@Override
			public void onStateChanged(RefreshLayout layout, RefreshState oldState, RefreshState newState) {
				if (available && listener != null) {
					// 上游在空闲通知之后才设置分页结束标志，等本次变更完成再通知应用。
					stateCallbacks.post(new Runnable() {
						@Override
						public void run() {
							if (available && listener != null) {
								listener.changed(oldState, newState);
							}
						}
					});
				}
			}
		});
	}

	/**
	 * 连接或释放 Simple 组件的回调，不替换原生内部监听。
	 *
	 * @param value 回调；销毁时传空引用。
	 */
	void setListener(Listener value) {
		listener = value;
		if (value == null) {
			stateCallbacks.removeCallbacksAndMessages(null);
		}
	}

	/**
	 * 查询视图是否仍属于当前界面。
	 *
	 * @return 已附着时为真。
	 */
	boolean isAvailable() {
		return available;
	}

	/**
	 * 读取上游实际的分页结束标志。
	 *
	 * @return 没有更多数据时为真。
	 */
	boolean hasNoMoreData() {
		return mFooterNoMoreData;
	}

	/**
	 * 查询固定内容设置是否已越过首次初始化边界。
	 *
	 * @return 曾经附着过时为真。
	 */
	boolean wasAttached() {
		return attachedBefore;
	}

	/**
	 * 查询固定内容包装是否已参与初始化。
	 *
	 * @return 设置过固定头尾标识时为真。
	 */
	boolean hasFixedContent() {
		return mFixedHeaderViewId != android.view.View.NO_ID || mFixedFooterViewId != android.view.View.NO_ID;
	}

	/**
	 * 读取当前原生布局的标准高度。
	 *
	 * @param header 是否读取刷新头。
	 * @return 像素高度。
	 */
	int nativeHeight(boolean header) {
		return header ? mHeaderHeight : mFooterHeight;
	}

	/**
	 * 撤销尚未开始的延时自动触发；收回已有拖动由原生关闭方法处理。
	 */
	void cancelScheduled() {
		if (mState == RefreshState.None) {
			mHandler.removeCallbacksAndMessages(null);
			mViceState = RefreshState.None;
		}
	}

	/**
	 * 只在附着期间报告原生阶段。
	 *
	 * @param header 是否为刷新头。
	 * @param phase 0 释放、1 动画开始、2 结束。
	 * @param height 像素高度。
	 * @param maxHeight 最大像素高度。
	 * @param success 完成结果。
	 */
	private void notifyPhase(boolean header, int phase, int height, int maxHeight, boolean success) {
		if (available && listener != null) {
			listener.phase(header, phase, height, maxHeight, success);
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		available = true;
		attachedBefore = true;
		if (listener != null) {
			listener.attached();
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		// 原生销毁动画会发出状态变化，离开界面后不再转发这些过渡回调。
		available = false;
		stateCallbacks.removeCallbacksAndMessages(null);
		super.onDetachedFromWindow();
		if (listener != null) {
			listener.detached();
		}
	}

	/**
	 * 原生视图与 Simple 请求管理之间的单实例回调。
	 */
	interface Listener {

		/**
		 * 附着完成后恢复独立头尾主题。
		 */
		void attached();

		/**
		 * 报告头尾移动，距离和高度均为像素。
		 *
		 * @param header 是否为刷新头。
		 * @param dragging 是否由手指拖动。
		 * @param percent 相对头尾高度比例。
		 * @param offset 像素偏移。
		 * @param height 像素高度。
		 * @param maxHeight 最大像素拖动高度。
		 */
		void moving(boolean header, boolean dragging, float percent, int offset, int height, int maxHeight);

		/**
		 * 报告释放、启动动画和结束阶段。
		 *
		 * @param header 是否为刷新头。
		 * @param phase 阶段编号。
		 * @param height 像素高度。
		 * @param maxHeight 最大像素高度。
		 * @param success 完成结果。
		 */
		void phase(boolean header, int phase, int height, int maxHeight, boolean success);

		/**
		 * 原生状态已进入刷新或加载，通知应用开始业务。
		 *
		 * @param refresh 下拉刷新为真，上拉加载为假。
		 */
		void started(boolean refresh);

		/**
		 * 报告仍附着在界面的原生状态变化。
		 *
		 * @param oldState 变化前状态。
		 * @param newState 变化后状态。
		 */
		void changed(RefreshState oldState, RefreshState newState);

		/**
		 * 原生动画和排队任务已清理，通知组件使旧请求失效。
		 */
		void detached();
	}

	/**
	 * 按当前内容尺寸重新测量适应内容的头尾。
	 *
	 * @param header 是否为刷新头。
	 * @return 仅适应内容高度且已有头尾时返回真。
	 */
	boolean remeasure(boolean header) {
		com.scwang.smart.refresh.layout.api.RefreshComponent component = header ? mRefreshHeader : mRefreshFooter;
		if (component == null || component.getView().getLayoutParams().height != android.view.ViewGroup.LayoutParams.WRAP_CONTENT) {
			return false;
		}
		mKernel.requestRemeasureHeightFor(component);
		// 释放代码固定高度的优先级，下一次布局才能根据 WRAP_CONTENT 更新实际高度。
		if (header) {
			mHeaderHeightStatus = com.scwang.smart.refresh.layout.constant.DimensionStatus.DefaultUnNotify;
		} else {
			mFooterHeightStatus = com.scwang.smart.refresh.layout.constant.DimensionStatus.DefaultUnNotify;
		}
		component.getView().requestLayout();
		requestLayout();
		return true;
	}

}
