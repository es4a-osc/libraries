package simple.library.wsd.smartrefresh;

import android.os.Looper;
import android.view.animation.*;
import simple.runtime.parameters.BooleanReferenceParameter;
import simple.runtime.parameters.SingleReferenceParameter;
import android.view.View;
import android.view.ViewGroup;

import com.scwang.smart.refresh.layout.constant.RefreshState;

import simple.runtime.android.MainActivity;
import simple.runtime.components.布局;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件;
import simple.runtime.components.组件容器;
import simple.runtime.components.impl.android.视图组件;
import simple.runtime.components.impl.android.视图组件容器;
import simple.runtime.errors.未初始化实例错误;
import simple.runtime.events.EventDispatcher;

/**
 * 把 SmartRefreshLayout 接入 Simple 单内容容器，并拒绝过期业务结果。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 智能刷新框Impl extends 视图组件 implements 智能刷新框, 视图组件容器,
		MainActivity.OnDestroyListener {

	private 视图组件 content;
	private 视图组件 initialContent;
	private BorrowedView replacementContent;
	private RefreshFeatures features;
	private boolean scheduledAuto;
	private boolean twoLevelOpen;
	private Interpolator curve = new PathInterpolator(0.2f, 0f, 0.2f, 1f);
	private int requestSequence;
	private int activeRequest;
	private boolean activeRefresh;
	private boolean destroyed;
	private boolean enableRefresh;
	private boolean enableLoadMore;
	private boolean autoLoadMore;
	private boolean loadWhenNotFull;
	private boolean overScrollBounce;
	private boolean overScrollDrag;
	private boolean nestedScroll;
	private boolean disableWhenRefresh;
	private boolean disableWhenLoading;
	private float dragRate;
	private float headerTriggerRate;
	private float footerTriggerRate;
	private float headerHeight;
	private float footerHeight;
	private int reboundDuration;
	private int primaryColor;
	private int accentColor;

	/**
	 * 创建本实例独立的原生容器和活动销毁监听。
	 *
	 * @param container 所属 Simple 组件容器。
	 */
	public 智能刷新框Impl(组件容器 container) {
		super(container);
		features = new RefreshFeatures(this, view());
		view().setListener(new SmartRefreshView.Listener() {
			@Override
			public void attached() {
				features.reapplyColors();
			}

			@Override
			public void moving(boolean header, boolean dragging, float percent, int offset, int height, int maxHeight) {
				if (!destroyed) {
					if (header) {
						头部移动(dragging, percent, offset, height, maxHeight);
					} else {
						尾部移动(dragging, percent, offset, height, maxHeight);
					}
				}
			}

			@Override
			public void phase(boolean header, int phase, int height, int maxHeight, boolean success) {
				if (!destroyed) {
					if (phase == 0 && header) {
						头部释放(height, maxHeight);
					} else if (phase == 0) {
						尾部释放(height, maxHeight);
					} else if (phase == 1 && header) {
						头部动画开始(height, maxHeight);
					} else if (phase == 1) {
						尾部动画开始(height, maxHeight);
					} else if (header) {
						头部结束(success);
					} else {
						尾部结束(success);
					}
				}
			}

			@Override
			public void started(boolean refresh) {
				if (!destroyed) {
					scheduledAuto = false;
					activeRequest = Math.incrementExact(requestSequence);
					requestSequence = activeRequest;
					activeRefresh = refresh;
					if (refresh) {
						开始刷新(activeRequest);
					} else {
						开始加载(activeRequest);
					}
				}
			}

			@Override
			public void changed(RefreshState oldState, RefreshState newState) {
				if (!destroyed) {
					if (newState == RefreshState.None) {
						scheduledAuto = false;
					}
					if (newState == RefreshState.TwoLevel) {
						twoLevelOpen = true;
						二楼已打开();
					} else if (newState == RefreshState.None && twoLevelOpen) {
						twoLevelOpen = false;
						二楼已关闭();
					}
					状态改变(stateText(oldState), stateText(newState));
				}
			}

			@Override
			public void detached() {
				// 在任何应用回调之前清除序号，旧结果不能命中新一次操作。
				scheduledAuto = false;
				if (!destroyed && twoLevelOpen) {
					twoLevelOpen = false;
					二楼已关闭();
				}
				int canceled = activeRequest;
				activeRequest = 0;
				if (!destroyed && canceled != 0) {
					操作取消(canceled);
				}
			}
		});
		view().setScrollBoundaryDecider(new com.scwang.smart.refresh.layout.simple.SimpleBoundaryDecider() {
			@Override
			public boolean canRefresh(View target) {
				BooleanReferenceParameter allowed = new BooleanReferenceParameter(super.canRefresh(target));
				if (customBoundary && isUsable()) {
					判断下拉边界(allowed);
				}
				return allowed.get();
			}

			@Override
			public boolean canLoadMore(View target) {
				BooleanReferenceParameter allowed = new BooleanReferenceParameter(super.canLoadMore(target));
				if (customBoundary && isUsable()) {
					判断上拉边界(allowed);
				}
				return allowed.get();
			}
		});
		MainActivity.getContext().addOnDestroyListener(this);
	}

	@Override
	protected View createView() {
		return new SmartRefreshView(MainActivity.getContext());
	}

	@Override
	public void 开始刷新(int requestId) {
		EventDispatcher.dispatchEvent(this, "开始刷新", requestId);
	}

	@Override
	public void 开始加载(int requestId) {
		EventDispatcher.dispatchEvent(this, "开始加载", requestId);
	}

	@Override
	public void 状态改变(String oldState, String newState) {
		EventDispatcher.dispatchEvent(this, "状态改变", oldState, newState);
	}

	@Override
	public void 操作取消(int requestId) {
		EventDispatcher.dispatchEvent(this, "操作取消", requestId);
	}

	@Override
	public boolean 允许下拉() {
		checkAlive();
		return enableRefresh;
	}

	@Override
	public void 允许下拉(boolean value) {
		checkAlive();
		enableRefresh = value;
		view().setEnableRefresh(value);
	}

	@Override
	public boolean 允许上拉() {
		checkAlive();
		return enableLoadMore;
	}

	@Override
	public void 允许上拉(boolean value) {
		checkAlive();
		enableLoadMore = value;
		view().setEnableLoadMore(value);
	}

	@Override
	public boolean 自动加载更多() {
		checkAlive();
		return autoLoadMore;
	}

	@Override
	public void 自动加载更多(boolean value) {
		checkAlive();
		autoLoadMore = value;
		view().setEnableAutoLoadMore(value);
	}

	@Override
	public boolean 内容不足时允许加载() {
		checkAlive();
		return loadWhenNotFull;
	}

	@Override
	public void 内容不足时允许加载(boolean value) {
		checkAlive();
		loadWhenNotFull = value;
		view().setEnableLoadMoreWhenContentNotFull(value);
	}

	@Override
	public boolean 越界回弹() {
		checkAlive();
		return overScrollBounce;
	}

	@Override
	public void 越界回弹(boolean value) {
		checkAlive();
		overScrollBounce = value;
		view().setEnableOverScrollBounce(value);
	}

	@Override
	public boolean 允许越界拖动() {
		checkAlive();
		return overScrollDrag;
	}

	@Override
	public void 允许越界拖动(boolean value) {
		checkAlive();
		overScrollDrag = value;
		view().setEnableOverScrollDrag(value);
	}

	@Override
	public boolean 启用嵌套滚动() {
		checkAlive();
		return nestedScroll;
	}

	@Override
	public void 启用嵌套滚动(boolean value) {
		checkAlive();
		nestedScroll = value;
		view().setEnableNestedScroll(value);
	}

	@Override
	public boolean 刷新时禁止操作内容() {
		checkAlive();
		return disableWhenRefresh;
	}

	@Override
	public void 刷新时禁止操作内容(boolean value) {
		checkAlive();
		disableWhenRefresh = value;
		view().setDisableContentWhenRefresh(value);
	}

	@Override
	public boolean 加载时禁止操作内容() {
		checkAlive();
		return disableWhenLoading;
	}

	@Override
	public void 加载时禁止操作内容(boolean value) {
		checkAlive();
		disableWhenLoading = value;
		view().setDisableContentWhenLoading(value);
	}

	@Override
	public float 拖动阻尼() {
		checkAlive();
		return dragRate;
	}

	@Override
	public void 拖动阻尼(float value) {
		checkAlive();
		if (!Float.isFinite(value) || value <= 0 || value > 1) {
			throw new IllegalArgumentException("比例必须大于 0 且不大于 1。");
		}
		dragRate = value;
		view().setDragRate(value);
	}

	@Override
	public float 下拉触发比例() {
		checkAlive();
		return headerTriggerRate;
	}

	@Override
	public void 下拉触发比例(float value) {
		checkAlive();
		if (!Float.isFinite(value) || value <= 0 || value > 1) {
			throw new IllegalArgumentException("比例必须大于 0 且不大于 1。");
		}
		headerTriggerRate = value;
		view().setHeaderTriggerRate(value);
	}

	@Override
	public float 上拉触发比例() {
		checkAlive();
		return footerTriggerRate;
	}

	@Override
	public void 上拉触发比例(float value) {
		checkAlive();
		if (!Float.isFinite(value) || value <= 0 || value > 1) {
			throw new IllegalArgumentException("比例必须大于 0 且不大于 1。");
		}
		footerTriggerRate = value;
		view().setFooterTriggerRate(value);
	}

	@Override
	public float 刷新头高度() {
		checkAlive();
		return headerHeight;
	}

	@Override
	public void 刷新头高度(float value) {
		checkAlive();
		if (!Float.isFinite(value) || value < 1 || value > 10000) {
			throw new IllegalArgumentException("数值超出允许范围。");
		}
		headerHeight = value;
		view().setHeaderHeight(value);
	}

	@Override
	public float 加载尾高度() {
		checkAlive();
		return footerHeight;
	}

	@Override
	public void 加载尾高度(float value) {
		checkAlive();
		if (!Float.isFinite(value) || value < 1 || value > 10000) {
			throw new IllegalArgumentException("数值超出允许范围。");
		}
		footerHeight = value;
		view().setFooterHeight(value);
	}

	@Override
	public int 回弹时长() {
		checkAlive();
		return reboundDuration;
	}

	@Override
	public void 回弹时长(int value) {
		checkAlive();
		if (value < 0 || value > 10000) {
			throw new IllegalArgumentException("数值超出允许范围。");
		}
		reboundDuration = value;
		view().setReboundDuration(value);
	}

	@Override
	public int 主色() {
		checkAlive();
		return primaryColor;
	}

	@Override
	public void 主色(int value) {
		checkAlive();
		primaryColor = value;
		view().setPrimaryColors(primaryColor, accentColor);
		features.colors(primaryColor, accentColor);
	}

	@Override
	public int 文字颜色() {
		checkAlive();
		return accentColor;
	}

	@Override
	public void 文字颜色(int value) {
		checkAlive();
		accentColor = value;
		view().setPrimaryColors(primaryColor, accentColor);
		features.colors(primaryColor, accentColor);
	}

	@Override
	public boolean 正在刷新() {
		checkAlive();
		return view().isRefreshing();
	}

	@Override
	public boolean 正在加载() {
		checkAlive();
		return view().isLoading();
	}

	@Override
	public boolean 没有更多数据() {
		checkAlive();
		return view().hasNoMoreData();
	}

	@Override
	public int 当前请求序号() {
		checkAlive();
		return activeRequest;
	}

	@Override
	public String 当前状态() {
		checkAlive();
		return stateText(view().getState());
	}

	@Override
	public boolean 触发刷新() {
		checkAlive();
		return 动画刷新(0, 300, 1f, false);
	}

	@Override
	public boolean 触发加载() {
		checkAlive();
		return 动画加载(0, 300, 1f, false);
	}

	@Override
	public boolean 完成刷新(int requestId, boolean success) {
		checkAlive();
		if (!accepts(requestId, true)) {
			return false;
		}
		activeRequest = 0;
		view().finishRefresh(success);
		return true;
	}

	@Override
	public boolean 完成加载(int requestId, boolean success, boolean noMoreData) {
		checkAlive();
		if (!accepts(requestId, false)) {
			return false;
		}
		activeRequest = 0;
		view().finishLoadMore(0, success, success && noMoreData);
		return true;
	}

	@Override
	public boolean 取消当前操作() {
		checkAlive();
		if (scheduledAuto && activeRequest == 0) {
			scheduledAuto = false;
			view().cancelScheduled();
			view().closeHeaderOrFooter();
			return true;
		}
		if (!ready() || activeRequest == 0) {
			return false;
		}
		int canceled = activeRequest;
		activeRequest = 0;
		if (activeRefresh) {
			view().finishRefresh(false);
		} else {
			view().finishLoadMore(false);
		}
		操作取消(canceled);
		return true;
	}

	@Override
	public void 重置加载状态() {
		checkAlive();
		view().resetNoMoreData();
	}

	@Override
	public void addComponent(组件 component) {
		checkAlive();
		if (!(component instanceof 视图组件)) {
			throw new IllegalArgumentException("智能刷新框只接受一个直接可视内容组件。");
		}
		视图组件 child = (视图组件) component;
		View nativeView = child.getView();
		if (nativeView == null || (content != null && content != child)
				|| (nativeView.getParent() != null && nativeView.getParent() != getView())) {
			throw new IllegalArgumentException("内容无效、已有父容器或超过一个直接内容组件。");
		}
		if (content == child && nativeView.getParent() == getView()) {
			return;
		}
		// 内容自带的列表或网页负责滚动，不增加额外 ScrollView。
		content = child;
		initialContent = child;
		view().setRefreshContent(nativeView);
	}

	@Override
	public 布局 getLayout() {
		checkAlive();
		return null;
	}

	@Override
	public ViewGroup getLayoutManager() {
		checkAlive();
		return view();
	}

	@Override
	public void onDestroy() {
		销毁();
	}

	@Override
	public void 销毁() {
		if (destroyed) {
			return;
		}
		checkAlive();
		destroyed = true;
		activeRequest = 0;
		MainActivity.getContext().removeOnDestroyListener(this);
		view().setListener(null);
		// 基础销毁会移出视图；原生 detach 负责清除动画、延时任务及位移。
		super.销毁();
		features.release();
		if (replacementContent != null) {
			replacementContent.restore();
			replacementContent = null;
		}
		content = null;
	}

	/**
	 * 校验当前业务结果仍属于附着中的同方向请求。
	 *
	 * @param requestId 调用方保存的序号。
	 * @param refresh 是否完成刷新。
	 * @return 请求仍有效时为真。
	 */
	private boolean accepts(int requestId, boolean refresh) {
		return ready() && requestId > 0 && activeRequest == requestId && activeRefresh == refresh
				&& (refresh ? view().isRefreshing() : view().isLoading());
	}

	/**
	 * 查询是否具有真实内容并仍附着在界面。
	 *
	 * @return 可以接收业务操作时为真。
	 */
	private boolean ready() {
		return content != null && view().isAvailable();
	}

	/**
	 * 获取本组件创建的固定原生类型。
	 *
	 * @return 原生刷新视图。
	 */
	private SmartRefreshView view() {
		return (SmartRefreshView) getView();
	}

	/**
	 * 阻止销毁后访问及工作线程直接操作界面。
	 *
	 * @throws 未初始化实例错误 组件已经销毁。
	 * @throws IllegalArgumentException 当前不在主线程。
	 */
	private void checkAlive() {
		if (destroyed) {
			throw new 未初始化实例错误();
		}
		if (Looper.myLooper() != Looper.getMainLooper()) {
			throw new IllegalArgumentException("请在主线程操作智能刷新框。");
		}
	}

	/**
	 * 将所选上游版本的状态转换为稳定的中文说明。
	 *
	 * @param state 原生状态。
	 * @return 当前状态的中文名称。
	 */
	private static String stateText(RefreshState state) {
		switch (state) {
			case None:
				return "空闲";
			case PullDownToRefresh:
				return "下拉中";
			case PullUpToLoad:
				return "上拉中";
			case PullDownCanceled:
				return "取消下拉";
			case PullUpCanceled:
				return "取消上拉";
			case ReleaseToRefresh:
				return "等待释放刷新";
			case ReleaseToLoad:
				return "等待释放加载";
			case RefreshReleased:
				return "刷新释放中";
			case LoadReleased:
				return "加载释放中";
			case Refreshing:
				return "正在刷新";
			case Loading:
				return "正在加载";
			case RefreshFinish:
				return "刷新完成";
			case LoadFinish:
				return "加载完成";
			case ReleaseToTwoLevel:
				return "等待释放进入二楼";
			case TwoLevelReleased:
				return "二楼释放中";
			case TwoLevel:
				return "二楼已打开";
			case TwoLevelFinish:
				return "二楼关闭中";
			default:
				throw new 刷新错误("未知原生刷新状态。");
		}
	}

	private int headerType;

	@Override
	public int 刷新头样式() {
		checkAlive();
		return headerType;
	}

	@Override
	public void 刷新头样式(int value) {
		checkAlive();
		ensureConfigurable();
		if (value < 0 || value > 15) {
			throw new IllegalArgumentException("配置数值超出范围。");
		}
		features.select(true, value);
		headerType = value;
	}

	private int footerType;

	@Override
	public int 加载尾样式() {
		checkAlive();
		return footerType;
	}

	@Override
	public void 加载尾样式(int value) {
		checkAlive();
		ensureConfigurable();
		if (value < 0 || value > 2) {
			throw new IllegalArgumentException("配置数值超出范围。");
		}
		features.select(false, value);
		footerType = value;
	}

	private boolean translateHeader;

	@Override
	public boolean 下拉内容跟随() {
		checkAlive();
		return translateHeader;
	}

	@Override
	public void 下拉内容跟随(boolean value) {
		checkAlive();
		ensureConfigurable();
		view().setEnableHeaderTranslationContent(value);
		translateHeader = value;
	}

	private boolean translateFooter;

	@Override
	public boolean 上拉内容跟随() {
		checkAlive();
		return translateFooter;
	}

	@Override
	public void 上拉内容跟随(boolean value) {
		checkAlive();
		ensureConfigurable();
		view().setEnableFooterTranslationContent(value);
		translateFooter = value;
	}

	private boolean pureScroll;

	@Override
	public boolean 纯滚动模式() {
		checkAlive();
		return pureScroll;
	}

	@Override
	public void 纯滚动模式(boolean value) {
		checkAlive();
		ensureConfigurable();
		view().setEnablePureScrollMode(value);
		pureScroll = value;
	}

	private boolean scrollWhenLoaded;

	@Override
	public boolean 加载完成滚动内容() {
		checkAlive();
		return scrollWhenLoaded;
	}

	@Override
	public void 加载完成滚动内容(boolean value) {
		checkAlive();
		ensureConfigurable();
		view().setEnableScrollContentWhenLoaded(value);
		scrollWhenLoaded = value;
	}

	private boolean scrollWhenRefreshed;

	@Override
	public boolean 刷新完成滚动内容() {
		checkAlive();
		return scrollWhenRefreshed;
	}

	@Override
	public void 刷新完成滚动内容(boolean value) {
		checkAlive();
		ensureConfigurable();
		view().setEnableScrollContentWhenRefreshed(value);
		scrollWhenRefreshed = value;
	}

	private boolean followNoMore;

	@Override
	public boolean 末页尾部跟随() {
		checkAlive();
		return followNoMore;
	}

	@Override
	public void 末页尾部跟随(boolean value) {
		checkAlive();
		ensureConfigurable();
		view().setEnableFooterFollowWhenNoMoreData(value);
		followNoMore = value;
	}

	private boolean clipHeader;

	@Override
	public boolean 背后刷新头剪裁() {
		checkAlive();
		return clipHeader;
	}

	@Override
	public void 背后刷新头剪裁(boolean value) {
		checkAlive();
		ensureConfigurable();
		view().setEnableClipHeaderWhenFixedBehind(value);
		clipHeader = value;
	}

	private boolean clipFooter;

	@Override
	public boolean 背后加载尾剪裁() {
		checkAlive();
		return clipFooter;
	}

	@Override
	public void 背后加载尾剪裁(boolean value) {
		checkAlive();
		ensureConfigurable();
		view().setEnableClipFooterWhenFixedBehind(value);
		clipFooter = value;
	}

	private boolean customBoundary;

	@Override
	public boolean 启用自定义边界() {
		checkAlive();
		return customBoundary;
	}

	@Override
	public void 启用自定义边界(boolean value) {
		checkAlive();
		ensureConfigurable();
		customBoundary = value;
	}

	private float headerInset;

	@Override
	public float 头部起始偏移() {
		checkAlive();
		return headerInset;
	}

	@Override
	public void 头部起始偏移(float value) {
		checkAlive();
		ensureConfigurable();
		if (!Float.isFinite(value) || value < 0 || value > 10000) {
			throw new IllegalArgumentException("配置数值超出范围。");
		}
		view().setHeaderInsetStart(value);
		headerInset = value;
	}

	private float footerInset;

	@Override
	public float 尾部起始偏移() {
		checkAlive();
		return footerInset;
	}

	@Override
	public void 尾部起始偏移(float value) {
		checkAlive();
		ensureConfigurable();
		if (!Float.isFinite(value) || value < 0 || value > 10000) {
			throw new IllegalArgumentException("配置数值超出范围。");
		}
		view().setFooterInsetStart(value);
		footerInset = value;
	}

	private float headerMaxRate;

	@Override
	public float 下拉最大比例() {
		checkAlive();
		return headerMaxRate;
	}

	@Override
	public void 下拉最大比例(float value) {
		checkAlive();
		ensureConfigurable();
		if (!Float.isFinite(value) || value < 1 || value >= 10) {
			throw new IllegalArgumentException("配置数值超出范围。");
		}
		view().setHeaderMaxDragRate(value);
		headerMaxRate = value;
	}

	private float footerMaxRate;

	@Override
	public float 上拉最大比例() {
		checkAlive();
		return footerMaxRate;
	}

	@Override
	public void 上拉最大比例(float value) {
		checkAlive();
		ensureConfigurable();
		if (!Float.isFinite(value) || value < 1 || value >= 10) {
			throw new IllegalArgumentException("配置数值超出范围。");
		}
		view().setFooterMaxDragRate(value);
		footerMaxRate = value;
	}

	private int interpolatorMode;

	@Override
	public int 回弹插值模式() {
		checkAlive();
		return interpolatorMode;
	}

	@Override
	public void 回弹插值模式(int value) {
		checkAlive();
		ensureConfigurable();
		if (value < 0 || value > 10) {
			throw new IllegalArgumentException("配置数值超出范围。");
		}
		interpolatorMode = value;
		applyInterpolator();
	}

	private float interpolatorFactor;

	@Override
	public float 回弹插值因子() {
		checkAlive();
		return interpolatorFactor;
	}

	@Override
	public void 回弹插值因子(float value) {
		checkAlive();
		ensureConfigurable();
		if (!Float.isFinite(value) || value <= 0 || value > 20) {
			throw new IllegalArgumentException("配置数值超出范围。");
		}
		interpolatorFactor = value;
		applyInterpolator();
	}

	@Override
	public void 头部移动(boolean dragging, float percent, int offset, int height, int maxHeight) {
		EventDispatcher.dispatchEvent(this, "头部移动", dragging, percent, offset, height, maxHeight);
	}

	@Override
	public void 头部释放(int height, int maxHeight) {
		EventDispatcher.dispatchEvent(this, "头部释放", height, maxHeight);
	}

	@Override
	public void 头部动画开始(int height, int maxHeight) {
		EventDispatcher.dispatchEvent(this, "头部动画开始", height, maxHeight);
	}

	@Override
	public void 头部结束(boolean success) {
		EventDispatcher.dispatchEvent(this, "头部结束", success);
	}

	@Override
	public void 头部水平拖动(float percent, int offset, int maxOffset) {
		EventDispatcher.dispatchEvent(this, "头部水平拖动", percent, offset, maxOffset);
	}

	@Override
	public void 尾部移动(boolean dragging, float percent, int offset, int height, int maxHeight) {
		EventDispatcher.dispatchEvent(this, "尾部移动", dragging, percent, offset, height, maxHeight);
	}

	@Override
	public void 尾部释放(int height, int maxHeight) {
		EventDispatcher.dispatchEvent(this, "尾部释放", height, maxHeight);
	}

	@Override
	public void 尾部动画开始(int height, int maxHeight) {
		EventDispatcher.dispatchEvent(this, "尾部动画开始", height, maxHeight);
	}

	@Override
	public void 尾部结束(boolean success) {
		EventDispatcher.dispatchEvent(this, "尾部结束", success);
	}

	@Override
	public void 尾部水平拖动(float percent, int offset, int maxOffset) {
		EventDispatcher.dispatchEvent(this, "尾部水平拖动", percent, offset, maxOffset);
	}

	@Override
	public void 准备打开二楼(BooleanReferenceParameter allowed) {
		EventDispatcher.dispatchEvent(this, "准备打开二楼", allowed);
	}

	@Override
	public void 二楼已打开() {
		EventDispatcher.dispatchEvent(this, "二楼已打开");
	}

	@Override
	public void 二楼已关闭() {
		EventDispatcher.dispatchEvent(this, "二楼已关闭");
	}

	@Override
	public void 飞行动画结束() {
		EventDispatcher.dispatchEvent(this, "飞行动画结束");
	}

	@Override
	public void 判断下拉边界(BooleanReferenceParameter allowed) {
		EventDispatcher.dispatchEvent(this, "判断下拉边界", allowed);
	}

	@Override
	public void 判断上拉边界(BooleanReferenceParameter allowed) {
		EventDispatcher.dispatchEvent(this, "判断上拉边界", allowed);
	}

	@Override
	public void 计算回弹位置(float input, SingleReferenceParameter output) {
		EventDispatcher.dispatchEvent(this, "计算回弹位置", input, output);
	}

	@Override
	public 刷新外观 取刷新头外观() {
		checkAlive();
		return features.appearance(true);
	}

	@Override
	public void 应用刷新头外观(刷新外观 appearance) {
		checkAlive();
		ensureConfigurable();
		features.apply(true, appearance);
	}

	@Override
	public void 设置自定义刷新头(可视组件 content) {
		checkAlive();
		ensureConfigurable();
		features.custom(true, content);
		headerType = 16;
	}

	@Override
	public 刷新外观 取加载尾外观() {
		checkAlive();
		return features.appearance(false);
	}

	@Override
	public void 应用加载尾外观(刷新外观 appearance) {
		checkAlive();
		ensureConfigurable();
		features.apply(false, appearance);
	}

	@Override
	public void 设置自定义加载尾(可视组件 content) {
		checkAlive();
		ensureConfigurable();
		features.custom(false, content);
		footerType = 3;
	}

	@Override
	public void 设置二楼内容(可视组件 content) {
		checkAlive();
		ensureConfigurable();
		features.floor(content);
		headerType = 4;
	}

	@Override
	public void 配置二楼(float refreshRate, float floorRate, float maxRate, int duration, float bottomRate, boolean enabled, boolean refresh, boolean close, int baseType) {
		checkAlive();
		ensureConfigurable();
		features.configureFloor(refreshRate, floorRate, maxRate, duration, bottomRate, enabled, refresh, close, baseType);
	}

	@Override
	public boolean 打开二楼(boolean notify) {
		checkAlive();
		return ready() && features.twoLevel(true, notify);
	}

	@Override
	public boolean 关闭二楼() {
		checkAlive();
		return ready() && features.twoLevel(false, false);
	}

	@Override
	public void 设置游戏状态(int state) {
		checkAlive();
		features.game(state);
	}

	@Override
	public void 结束飞行动画() {
		checkAlive();
		features.finishFlight();
	}

	@Override
	public void 绑定飞行场景(飞行场景 scene, 可视组件 image) {
		checkAlive();
		ensureConfigurable();
		if (!(scene instanceof 飞行场景Impl) || !(image instanceof 视图组件)) {
			throw new IllegalArgumentException("飞行场景或图标类型无效。");
		}
		features.bindFlight(((飞行场景Impl) scene).scene(), ((视图组件) image).getView());
	}

	@Override
	public boolean 动画刷新(int delay, int duration, float rate, boolean animationOnly) {
		checkAlive();
		if (delay < 0 || delay > 32767 || duration <= 0 || duration > 10000 || !Float.isFinite(rate)
				|| rate <= 0 || rate > headerMaxRate) {
			throw new IllegalArgumentException("动画参数超出允许范围。");
		}
		boolean accepted = ready() && !scheduledAuto && enableRefresh
				&& view().autoRefresh(delay, duration, rate, animationOnly);
		scheduledAuto = scheduledAuto || accepted;
		return accepted;
	}

	@Override
	public boolean 动画加载(int delay, int duration, float rate, boolean animationOnly) {
		checkAlive();
		if (delay < 0 || delay > 32767 || duration <= 0 || duration > 10000 || !Float.isFinite(rate)
				|| rate <= 0 || rate > footerMaxRate) {
			throw new IllegalArgumentException("动画参数超出允许范围。");
		}
		boolean accepted = ready() && !scheduledAuto && enableLoadMore && !view().hasNoMoreData()
				&& view().autoLoadMore(delay, duration, rate, animationOnly);
		scheduledAuto = scheduledAuto || accepted;
		return accepted;
	}

	@Override
	public boolean 延时完成刷新(int requestId, int delay, boolean success, int endState) {
		checkAlive();
		if (delay < 0 || delay > 32767 || endState < -1 || endState > 1) {
			throw new IllegalArgumentException("完成参数超出范围。");
		}
		if (!accepts(requestId, true)) {
			return false;
		}
		activeRequest = 0;
		Boolean ended = !success || endState == -1 ? null : Boolean.valueOf(endState == 1);
		view().finishRefresh(delay, success, ended);
		return true;
	}

	@Override
	public boolean 延时完成加载(int requestId, int delay, boolean success, boolean noMoreData) {
		checkAlive();
		if (delay < 0 || delay > 32767) {
			throw new IllegalArgumentException("完成延时超出范围。");
		}
		if (!accepts(requestId, false)) {
			return false;
		}
		activeRequest = 0;
		view().finishLoadMore(delay, success, success && noMoreData);
		return true;
	}

	@Override
	public void 关闭头尾() {
		checkAlive();
		if (!取消当前操作()) {
			view().cancelScheduled();
			if (view().isRefreshing()) {
				view().finishRefresh(false);
			} else if (view().isLoading()) {
				view().finishLoadMore(false);
			} else {
				view().closeHeaderOrFooter();
			}
		}
		scheduledAuto = false;
	}

	@Override
	public void 设置回弹贝塞尔(float x1, float y1, float x2, float y2) {
		checkAlive();
		ensureConfigurable();
		if (!Float.isFinite(x1) || !Float.isFinite(y1) || !Float.isFinite(x2) || !Float.isFinite(y2)
				|| x1 < 0 || x1 > 1 || x2 < 0 || x2 > 1) {
			throw new IllegalArgumentException("贝塞尔控制点无效。");
		}
		curve = new PathInterpolator(x1, y1, x2, y2);
		interpolatorMode = 10;
		applyInterpolator();
	}

	@Override
	public void 设置固定头部(可视组件 component) {
		checkAlive();
		ensureConfigurable();
		if (view().wasAttached()) {
			throw new 刷新错误("固定头尾必须在首次附着前设置。");
		}
		view().setFixedHeaderViewId(contentViewId(component));
	}

	@Override
	public void 设置固定尾部(可视组件 component) {
		checkAlive();
		ensureConfigurable();
		if (view().wasAttached()) {
			throw new 刷新错误("固定头尾必须在首次附着前设置。");
		}
		view().setFixedFooterViewId(contentViewId(component));
	}

	@Override
	public void 设置头部跟随视图(可视组件 component) {
		checkAlive();
		ensureConfigurable();
		view().setHeaderTranslationViewId(contentViewId(component));
	}

	@Override
	public void 设置尾部跟随视图(可视组件 component) {
		checkAlive();
		ensureConfigurable();
		view().setFooterTranslationViewId(contentViewId(component));
	}

	/**
	 * 查询内部异步回调是否仍允许通知用户。
	 *
	 * @return 尚未销毁并仍附着时为真。
	 */
	boolean isUsable() {
		return !destroyed && view().isAvailable();
	}

	/**
	 * 拒绝动画和业务期间替换结构或改变关键参数。
	 */
	private void ensureConfigurable() {
		if (view().getState() != RefreshState.None || activeRequest != 0 || scheduledAuto) {
			throw new 刷新错误("请在刷新框空闲时修改结构或外观。");
		}
	}

	/**
	 * 校验指定的固定或跟随视图确实位于内容子树。
	 *
	 * @param component 内容内视图；空引用表示清除设置。
	 * @return 实际 Android 视图标识。
	 */
	private int contentViewId(可视组件 component) {
		if (component == null) {
			return View.NO_ID;
		}
		if (!(component instanceof 视图组件) || content == null) {
			throw new IllegalArgumentException("必须提供内容子树中的可视组件。");
		}
		View target = ((视图组件) component).getView();
		for (View node = target; node != null; node = node.getParent() instanceof View ? (View) node.getParent() : null) {
			if (node == content.getView()) {
				if (target.getId() <= 0) {
					target.setId(View.generateViewId());
				}
				return target.getId();
			}
		}
		throw new IllegalArgumentException("固定或跟随组件必须位于当前内容子树中。");
	}

	/**
	 * 装配原生或自定义的回弹曲线。
	 */
	private void applyInterpolator() {
		Interpolator interpolator;
		switch (interpolatorMode) {
			case 0:
				interpolator = new com.scwang.smart.refresh.layout.util.SmartUtil(0);
				break;
			case 1:
				interpolator = new LinearInterpolator();
				break;
			case 2:
				interpolator = new AccelerateInterpolator(interpolatorFactor);
				break;
			case 3:
				interpolator = new DecelerateInterpolator(interpolatorFactor);
				break;
			case 4:
				interpolator = new AccelerateDecelerateInterpolator();
				break;
			case 5:
				interpolator = new BounceInterpolator();
				break;
			case 6:
				interpolator = new OvershootInterpolator(interpolatorFactor);
				break;
			case 7:
				interpolator = new AnticipateInterpolator(interpolatorFactor);
				break;
			case 8:
				interpolator = new AnticipateOvershootInterpolator(interpolatorFactor);
				break;
			case 10:
				interpolator = curve;
				break;
			default:
				interpolator = new Interpolator() {
					@Override
					public float getInterpolation(float input) {
						SingleReferenceParameter output = new SingleReferenceParameter(input);
						if (isUsable()) {
							计算回弹位置(input, output);
						}
						if (!Float.isFinite(output.get())) {
							throw new IllegalArgumentException("自定义回弹位置必须是有限数值。");
						}
						return output.get();
					}
				};
		}
		view().setReboundInterpolator(interpolator);
	}

	@Override
	public void 置分页结束(boolean ended) {
		checkAlive();
		ensureConfigurable();
		view().setNoMoreData(ended);
	}

	@Override
	public void 设置主题颜色(int[] colors) {
		checkAlive();
		if (colors == null || colors.length == 0 || colors.length > 32) {
			throw new IllegalArgumentException("请提供 1 至 32 个主题颜色。");
		}
		primaryColor = colors[0];
		accentColor = colors.length > 1 ? colors[1] : primaryColor;
		features.theme(colors.clone());
	}

	@Override
	public void 设置资源主题(int[] ids) {
		checkAlive();
		if (ids == null || ids.length == 0 || ids.length > 32) {
			throw new IllegalArgumentException("请提供 1 至 32 个颜色资源标识。");
		}
		int[] colors = new int[ids.length];
		for (int index = 0; index < ids.length; index++) {
			colors[index] = MainActivity.getContext().getColor(ids[index]);
		}
		设置主题颜色(colors);
	}

	@Override
	public void 设置头尾像素尺寸(boolean header, int width, int height) {
		checkAlive();
		ensureConfigurable();
		if (width < -2 || height < -2) {
			throw new IllegalArgumentException("原生布局宽高不能小于 -2。");
		}
		if (header) {
			view().setRefreshHeader(view().getRefreshHeader(), width, height);
		} else {
			view().setRefreshFooter(view().getRefreshFooter(), width, height);
		}
		// 上游复用旧布局参数时会忽略宽高实参，显式置入本次尺寸才能真正生效。
		View target = header ? view().getRefreshHeader().getView() : view().getRefreshFooter().getView();
		target.setLayoutParams(new com.scwang.smart.refresh.layout.SmartRefreshLayout.LayoutParams(width, height));
		features.reapplyColors();
	}

	@Override
	public void 设置头尾像素高度(boolean header, int pixels) {
		checkAlive();
		ensureConfigurable();
		if (pixels <= 0 || pixels > 10000) {
			throw new IllegalArgumentException("像素高度必须为 1 至 10000。");
		}
		float dp = pixels / MainActivity.getContext().getResources().getDisplayMetrics().density;
		if (header) {
			headerHeight = dp;
			view().setHeaderHeightPx(pixels);
		} else {
			footerHeight = dp;
			view().setFooterHeightPx(pixels);
		}
	}

	@Override
	public void 设置头尾像素偏移(boolean header, int pixels) {
		checkAlive();
		ensureConfigurable();
		if (pixels < 0 || pixels > 10000) {
			throw new IllegalArgumentException("像素偏移超出范围。");
		}
		float dp = pixels / MainActivity.getContext().getResources().getDisplayMetrics().density;
		if (header) {
			headerInset = dp;
			view().setHeaderInsetStartPx(pixels);
		} else {
			footerInset = dp;
			view().setFooterInsetStartPx(pixels);
		}
	}

	@Override
	public void 替换刷新内容(可视组件 component) {
		checkAlive();
		ensureConfigurable();
		if (initialContent == null || view().hasFixedContent()) {
			throw new 刷新错误("请先建立初始内容，且不要在固定头尾生效后替换内容。");
		}
		BorrowedView next = new BorrowedView(component, view());
		view().setRefreshContent(next.view);
		if (replacementContent != null) {
			replacementContent.restore();
		}
		replacementContent = next;
		content = (视图组件) component;
	}

	@Override
	public void 恢复原内容() {
		checkAlive();
		ensureConfigurable();
		if (replacementContent != null) {
			view().setRefreshContent(initialContent.getView());
			replacementContent.restore();
			replacementContent = null;
			content = initialContent;
		}
	}

	@Override
	public 可视组件 取当前内容() {
		checkAlive();
		return content;
	}

	@Override
	public int 取头尾像素高度(boolean header) {
		checkAlive();
		return view().nativeHeight(header);
	}

	@Override
	public boolean 重新测量头尾(boolean header) {
		checkAlive();
		ensureConfigurable();
		return view().remeasure(header);
	}

}
