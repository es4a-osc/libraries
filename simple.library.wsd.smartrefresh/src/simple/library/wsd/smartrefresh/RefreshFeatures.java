package simple.library.wsd.smartrefresh;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.scwang.smart.refresh.classics.ClassicsAbstract;
import com.scwang.smart.refresh.footer.BallPulseFooter;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.*;
import com.scwang.smart.refresh.header.flyrefresh.MountainSceneView;
import com.scwang.smart.refresh.header.fungame.FunGameView;
import com.scwang.smart.refresh.header.listener.OnTwoLevelListener;
import com.scwang.smart.refresh.layout.api.RefreshComponent;
import com.scwang.smart.refresh.layout.api.RefreshFooter;
import com.scwang.smart.refresh.layout.api.RefreshHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.RefreshState;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;
import com.scwang.smart.refresh.layout.util.SmartUtil;
import com.scwang.smart.refresh.layout.wrapper.RefreshHeaderWrapper;
import com.scwang.smart.refresh.layout.wrapper.RefreshFooterWrapper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import simple.runtime.components.可视组件;
import simple.runtime.errors.文件未存在错误;
import simple.runtime.parameters.BooleanReferenceParameter;
import simple.runtime.components.impl.android.util.ImageUtil;

/**
 * 按固定上游版本装配全部头尾样式、外观及二楼内容，不触碰 Simple 运行库。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class RefreshFeatures {
	private final 智能刷新框Impl owner;
	private final SmartRefreshView view;
	private 刷新外观 headerAppearance = new 刷新外观();
	private 刷新外观 footerAppearance = new 刷新外观();
	private int headerType;
	private int footerType;
	private RefreshHeader baseHeader;
	private BorrowedView customHeader;
	private BorrowedView customFooter;
	private BorrowedView floorContent;
	private MountainSceneView flyScene;
	private View flyImage;
	private int floorBaseType;
	private float floorRate = 1.9f;
	private float floorRefreshRate = 1f;
	private float floorMaxRate = 2.5f;
	private float floorBottomRate = 1f / 6f;
	private int floorDuration = 1000;
	private boolean floorEnabled = true;
	private boolean floorRefreshEnabled = true;
	private boolean floorPullToClose = true;

	/**
	 * 建立本实例的样式快照，加载尾使用对应方向的缺省文案。
	 *
	 * @param owner 事件及设置的拥有者。
	 * @param view 原生刷新容器。
	 */
	RefreshFeatures(智能刷新框Impl owner, SmartRefreshView view) {
		this.owner = owner;
		this.view = view;
		footerAppearance.拉动提示("上拉加载更多");
		footerAppearance.释放提示("松开立即加载");
		footerAppearance.处理中提示("正在加载…");
		footerAppearance.另一方向提示("正在刷新…");
	}

	/**
	 * 读取一份可以由 Simple 修改的独立外观快照。
	 *
	 * @param header 是否读取刷新头。
	 * @return 外观副本。
	 */
	刷新外观 appearance(boolean header) {
		return (header ? headerAppearance : footerAppearance).copy();
	}

	/**
	 * 先验证图片及格式，再保存并重建对应头尾。
	 *
	 * @param header 是否应用刷新头。
	 * @param value 配置快照。
	 */
	void apply(boolean header, 刷新外观 value) {
		if (value == null) {
			throw new IllegalArgumentException("外观不能为空引用。");
		}
		刷新外观 next = value.copy();
		drawable(next.箭头图片());
		drawable(next.进度图片());
		if (header) {
			headerAppearance = next;
			installHeader();
		} else {
			footerAppearance = next;
			installFooter();
		}
	}

	/**
	 * 修改内置样式；自定义内容在切回内置样式时恢复原位置。
	 *
	 * @param header 是否修改刷新头。
	 * @param type 头为 0 至 15，尾为 0 至 2。
	 */
	void select(boolean header, int type) {
		if (type < 0 || type > (header ? 15 : 2)) {
			throw new IllegalArgumentException("头尾样式编号无效。");
		}
		if (header && type == 8 && (flyScene == null || flyImage == null)) {
			throw new 刷新错误("选择飞行样式前，请先绑定飞行场景和飞行图标。");
		}
		if (header) {
			if (customHeader != null) {
				view.setRefreshHeader(new ConfiguredHeader(view.getContext(), headerAppearance));
				customHeader.restore();
				customHeader = null;
			}
			if (type != 4) {
				restoreFloor();
			}
			headerType = type;
			installHeader();
		} else {
			if (customFooter != null) {
				view.setRefreshFooter(new ConfiguredFooter(view.getContext(), footerAppearance));
				customFooter.restore();
				customFooter = null;
			}
			footerType = type;
			installFooter();
		}
	}

	/**
	 * 暂借外部内容作为自定义头尾，当前样式外观提供变换方式和结束停留时间。
	 *
	 * @param header 是否设置刷新头。
	 * @param component 同窗口中、刷新框以外的可视组件。
	 */
	void custom(boolean header, 可视组件 component) {
		BorrowedView next = new BorrowedView(component, view);
		if (header) {
			if (customHeader != null) {
				customHeader.restore();
			}
			restoreFloor();
			customHeader = next;
			headerType = 16;
			installHeader();
		} else {
			if (customFooter != null) {
				customFooter.restore();
			}
			customFooter = next;
			footerType = 3;
			installFooter();
		}
	}

	/**
	 * 暂借二楼内容并启用二楼头，内容尺寸由二楼外壳负责。
	 *
	 * @param component 同窗口中独立的可视组件。
	 */
	void floor(可视组件 component) {
		BorrowedView next = new BorrowedView(component, view);
		if (customHeader != null) {
			view.setRefreshHeader(new ConfiguredHeader(view.getContext(), headerAppearance));
			customHeader.restore();
			customHeader = null;
		}
		restoreFloor();
		floorContent = next;
		headerType = 4;
		installHeader();
	}

	/**
	 * 绑定原生飞行场景与同窗口的飞行图标，不改变其原父容器。
	 *
	 * @param scene 场景原生视图。
	 * @param image 图标原生视图。
	 */
	void bindFlight(MountainSceneView scene, View image) {
		if (scene == null || image == null || image == view || scene.getRootView() != view.getRootView()
				|| image.getRootView() != view.getRootView()) {
			throw new IllegalArgumentException("飞行场景和图标必须属于同一窗口。");
		}
		flyScene = scene;
		flyImage = image;
		if (headerType == 8) {
			installHeader();
		}
	}

	/**
	 * 配置二楼的比例、开关、一级样式和动画。
	 *
	 * @param refreshRate 刷新触发比例。
	 * @param rate 二楼触发比例。
	 * @param maxRate 最大拖动比例。
	 * @param duration 二楼动画毫秒数。
	 * @param bottomRate 底部上滑关闭区域比例。
	 * @param enabled 是否允许二楼。
	 * @param refresh 是否允许一级刷新。
	 * @param close 是否允许上滑关闭。
	 * @param baseType 二楼一级头样式，禁止嵌套二楼或飞行。
	 */
	void configureFloor(float refreshRate, float rate, float maxRate, int duration, float bottomRate,
			boolean enabled, boolean refresh, boolean close, int baseType) {
		if (!Float.isFinite(refreshRate) || !Float.isFinite(rate) || !Float.isFinite(maxRate)
				|| !Float.isFinite(bottomRate) || refreshRate <= 0 || rate <= refreshRate || maxRate < rate
				|| maxRate >= 10 || duration < 0 || duration > 10000 || bottomRate < 0 || bottomRate > 1
				|| baseType < 0 || baseType > 15 || baseType == 4 || baseType == 8) {
			throw new IllegalArgumentException("二楼比例、动画或一级样式不合法。");
		}
		floorRefreshRate = refreshRate;
		floorRate = rate;
		floorMaxRate = maxRate;
		floorDuration = duration;
		floorBottomRate = bottomRate;
		floorEnabled = enabled;
		floorRefreshEnabled = refresh;
		floorPullToClose = close;
		floorBaseType = baseType;
		if (headerType == 4) {
			installHeader();
		}
	}

	/**
	 * 请求打开或关闭二楼。
	 *
	 * @param open 是否打开。
	 * @param notify 是否通过准备打开事件允许调用方拒绝。
	 * @return 当前具备二楼头并受理请求时为真。
	 */
	boolean twoLevel(boolean open, boolean notify) {
		if (!(view.getRefreshHeader() instanceof TwoLevelHeader)) {
			return false;
		}
		TwoLevelHeader header = (TwoLevelHeader) view.getRefreshHeader();
		if (open) {
			if (!floorEnabled || view.getState() != RefreshState.None) {
				return false;
			}
			header.openTwoLevel(notify);
		} else {
			if (!view.getState().isTwoLevel) {
				return false;
			}
			header.finishTwoLevel();
		}
		return true;
	}

	/**
	 * 更改游戏的业务显示状态。
	 *
	 * @param state 0 准备，1 进行，2 结束，3 完成，4 失败。
	 */
	void game(int state) {
		if (!(baseHeader instanceof FunGameView) || state < 0 || state > 4) {
			throw new 刷新错误("请先选择游戏样式，并使用 0 至 4 的状态。");
		}
		((FunGameView) baseHeader).postStatus(state);
	}

	/**
	 * 结束飞行返回动画并在完成时通知 Simple。
	 */
	void finishFlight() {
		if (!(baseHeader instanceof FlyRefreshHeader)) {
			throw new 刷新错误("当前不是飞行样式。");
		}
		((FlyRefreshHeader) baseHeader).finishRefresh();
	}

	/**
	 * 将统一主题更新到各自的外观快照。
	 *
	 * @param primary 主色。
	 * @param accent 强调色。
	 */
	void colors(int primary, int accent) {
		headerAppearance.置主题颜色(new int[0]);
		footerAppearance.置主题颜色(new int[0]);
		headerAppearance.主颜色(primary);
		headerAppearance.强调颜色(accent);
		footerAppearance.主颜色(primary);
		footerAppearance.强调颜色(accent);
		reapplyColors();
	}

	/**
	 * 宿主重新附着后恢复每个头尾各自的颜色。
	 */
	void reapplyColors() {
		if (view.getRefreshHeader() != null) {
			applyColors(view.getRefreshHeader(), headerAppearance);
		}
		if (view.getRefreshFooter() != null) {
			applyColors(view.getRefreshFooter(), footerAppearance);
		}
	}

	/**
	 * 销毁时恢复所有借用内容并停止飞行图标动画。
	 */
	void release() {
		if (customHeader != null) {
			customHeader.restore();
			customHeader = null;
		}
		if (customFooter != null) {
			customFooter.restore();
			customFooter = null;
		}
		restoreFloor();
		if (flyImage != null) {
			flyImage.animate().cancel();
			flyImage.clearAnimation();
		}
		flyScene = null;
		flyImage = null;
	}

	/**
	 * 恢复二楼内容的原归属。
	 */
	private void restoreFloor() {
		if (floorContent != null) {
			floorContent.restore();
			floorContent = null;
		}
	}

	/**
	 * 创建并安装当前刷新头，恢复从特殊样式切回时的正常高度。
	 */
	private void installHeader() {
		RefreshHeader header;
		if (headerType == 16) {
			customHeader.detach();
			header = new CustomHeader(customHeader.view);
		} else if (headerType == 4) {
			TwoLevelHeader floor = new TwoLevelHeader(view.getContext());
			if (floorContent != null) {
				floorContent.detach();
				floor.addView(floorContent.view, new ViewGroup.LayoutParams(-1, -1));
			} else {
				TextView text = new TextView(view.getContext());
				text.setText("二楼内容");
				text.setGravity(android.view.Gravity.CENTER);
				floor.addView(text, new ViewGroup.LayoutParams(-1, -1));
			}
			baseHeader = createHeader(floorBaseType);
			floor.setRefreshHeader(baseHeader).setRefreshRate(floorRefreshRate).setFloorRate(floorRate)
					.setMaxRate(floorMaxRate).setFloorDuration(floorDuration)
					.setBottomPullUpToCloseRate(floorBottomRate).setEnableTwoLevel(floorEnabled)
					.setEnableRefresh(floorRefreshEnabled).setEnablePullToCloseTwoLevel(floorPullToClose);
			floor.setOnTwoLevelListener(new OnTwoLevelListener() {
				@Override
				public boolean onTwoLevel(RefreshLayout layout) {
					BooleanReferenceParameter allowed = new BooleanReferenceParameter(true);
					if (!owner.isUsable()) {
						return false;
					}
					owner.准备打开二楼(allowed);
					return allowed.get();
				}
			});
			header = floor;
		} else {
			baseHeader = createHeader(headerType);
			header = baseHeader;
		}
		view.setRefreshHeader(header);
		view.setEnableRefresh(owner.允许下拉());
		if (headerType != 8) {
			view.setHeaderHeight(owner.刷新头高度());
			view.setHeaderTriggerRate(headerType == 4 ? floorRefreshRate : owner.下拉触发比例());
			view.setHeaderMaxDragRate(headerType == 4 ? floorMaxRate : owner.下拉最大比例());
			view.setEnableOverScrollDrag(owner.允许越界拖动());
		}
		applyColors(header, headerAppearance);
	}

	/**
	 * 根据编号创建内置刷新头并应用相应外观。
	 *
	 * @param type 内置编号。
	 * @return 独立原生刷新头。
	 */
	private RefreshHeader createHeader(int type) {
		Context context = view.getContext();
		RefreshHeader header;
		switch (type) {
			case 0:
				header = new ConfiguredHeader(context, headerAppearance);
				break;
			case 1:
				header = new BezierRadarHeader(context).setEnableHorizontalDrag(headerAppearance.雷达横向拖动());
				break;
			case 2:
				MaterialHeader material = new MaterialHeader(context);
				material.setSize(headerAppearance.Material大号圆圈() ? 0 : 1);
				material.setShowBezierWave(headerAppearance.Material波浪());
				material.setScrollableWhenRefreshing(headerAppearance.Material刷新中滚动());
				material.setProgressBackgroundColorSchemeColor(headerAppearance.主颜色());
				material.setColorSchemeColors(animationColors(headerAppearance));
				header = material;
				break;
			case 3:
				header = new FalsifyHeader(context);
				break;
			case 5:
				header = new BezierCircleHeader(context);
				break;
			case 6:
				header = new DeliveryHeader(context);
				break;
			case 7:
				header = new DropBoxHeader(context);
				break;
			case 8:
				FlyRefreshHeader fly = new ManagedFly(context);
				fly.setUp(flyScene, flyImage);
				header = fly;
				break;
			case 9:
				header = new ConfiguredBattle(context, headerAppearance);
				break;
			case 10:
				header = new ConfiguredBlock(context, headerAppearance);
				break;
			case 11:
				header = new PhoenixHeader(context);
				break;
			case 12:
				StoreHouseHeader store = new StoreHouseHeader(context);
				store.setLineWidth(Math.max(1, SmartUtil.dp2px(headerAppearance.仓库线宽())));
				store.setDropHeight(SmartUtil.dp2px(headerAppearance.仓库下落高度()));
				store.setLoadingAniDuration(headerAppearance.仓库动画时长());
				float[] points = headerAppearance.取仓库线段();
				if (points.length == 0) {
					store.initWithString(headerAppearance.仓库文字(), headerAppearance.仓库字号());
				} else {
					ArrayList<float[]> segments = new ArrayList<float[]>();
					for (int index = 0; index < points.length; index += 4) {
						segments.add(new float[] { points[index], points[index + 1], points[index + 2], points[index + 3] });
					}
					store.initWithPointList(segments);
				}
				store.setScale(headerAppearance.仓库缩放());
				header = store;
				break;
			case 13:
				header = new TaurusHeader(context);
				break;
			case 14:
				header = new WaterDropHeader(context);
				break;
			case 15:
				WaveSwipeHeader wave = new WaveSwipeHeader(context);
				wave.setColorSchemeColors(animationColors(headerAppearance));
				header = wave;
				break;
			default:
				throw new IllegalArgumentException("刷新头样式编号无效。");
		}
		if (header instanceof FunGameView) {
			FunGameView game = (FunGameView) header;
			game.mMaskTextTopPull = headerAppearance.游戏拉动提示();
			game.mMaskTextTopRelease = headerAppearance.游戏释放提示();
			game.mMaskTextBottom = headerAppearance.游戏操作提示();
			game.mTextGameOver = headerAppearance.游戏结束提示();
			game.mTextLoading = headerAppearance.游戏加载提示();
			game.mTextLoadingFinish = headerAppearance.游戏完成提示();
			game.mTextLoadingFailed = headerAppearance.游戏失败提示();
		}
		applyColors(header, headerAppearance);
		return header;
	}

	/**
	 * 安装当前加载尾。
	 */
	private void installFooter() {
		RefreshFooter footer;
		if (footerType == 3) {
			customFooter.detach();
			footer = new CustomFooter(customFooter.view);
		} else if (footerType == 1) {
			BallPulseFooter ball = new BallPulseFooter(view.getContext()).setNormalColor(footerAppearance.球普通颜色())
					.setAnimatingColor(footerAppearance.球动画颜色());
			if (footerAppearance.变换样式() >= 0) {
				ball.setSpinnerStyle(spinnerStyle(footerAppearance.变换样式()));
			}
			footer = ball;
		} else if (footerType == 2) {
			footer = new FalsifyFooter(view.getContext());
		} else {
			footer = new ConfiguredFooter(view.getContext(), footerAppearance);
		}
		view.setRefreshFooter(footer);
		view.setFooterHeight(owner.加载尾高度());
		applyColors(footer, footerAppearance);
		// 切换样式后，新尾部也必须接收已经存在的分页结束状态。
		footer.setNoMoreData(view.hasNoMoreData());
	}

	/**
	 * 构造动画颜色数组，未指定时使用当前强调色。
	 *
	 * @param appearance 外观快照。
	 * @return 独立的非空颜色数组。
	 */
	private static int[] animationColors(刷新外观 appearance) {
		int[] values = appearance.取动画颜色();
		return values.length == 0 ? new int[] { appearance.强调颜色() } : values;
	}

	/**
	 * 处理上游明确保留给框架调用的颜色回调，使不同头尾可以拥有各自主题。
	 *
	 * @param component 需要配置的原生组件。
	 * @param appearance 本实例外观。
	 */
	@SuppressWarnings("deprecation")
	private static void applyColors(RefreshComponent component, 刷新外观 appearance) {
		// 上游仅将框架回调标为弃用；没有可以逐个设置所有样式颜色的等价新接口。
		int[] theme = appearance.取主题颜色();
		component.setPrimaryColors(theme.length == 0 ? new int[] { appearance.主颜色(), appearance.强调颜色() } : theme);
		if (appearance.变换样式() >= 0) {
			if (component instanceof ClassicsAbstract) {
				((ClassicsAbstract<?>) component).setSpinnerStyle(spinnerStyle(appearance.变换样式()));
			} else if (component instanceof BallPulseFooter) {
				((BallPulseFooter) component).setSpinnerStyle(spinnerStyle(appearance.变换样式()));
			}
		}
	}

	/**
	 * 映射调用方显式选择的变换方式，保留仍有实际用途的上游拉伸模式。
	 *
	 * @param value 0 至 4 的样式编号。
	 * @return 上游样式。
	 */
	@SuppressWarnings("deprecation")
	private static SpinnerStyle spinnerStyle(int value) {
		if (value == 1) {
			// Scale 已被建议替换，但显式选择拉伸时仍须保留其真实布局行为。
			return SpinnerStyle.Scale;
		}
		return SpinnerStyle.values[value < 0 ? 0 : value];
	}

	/**
	 * 读取 Simple 图片标识并保留缺失文件的真实错误。
	 *
	 * @param value 图片标识；空文本使用原生图形。
	 * @return 图片或空引用。
	 */
	private static Drawable drawable(String value) {
		try {
			return value.isEmpty() ? null : ImageUtil.getDrawable(value);
		} catch (IOException exception) {
			throw new 文件未存在错误(value);
		}
	}

	/**
	 * 为经典头尾应用公共文字、图片、尺寸和停留时间。
	 *
	 * @param component 经典组件。
	 * @param appearance 外观快照。
	 */
	private static void classic(ClassicsAbstract<?> component, 刷新外观 appearance) {
		component.setPrimaryColor(appearance.主颜色()).setAccentColor(appearance.强调颜色());
		component.setTextSizeTitle(appearance.标题字号()).setFinishDuration(appearance.完成停留时间());
		component.setDrawableArrowSize(appearance.箭头尺寸()).setDrawableProgressSize(appearance.进度尺寸());
		component.setDrawableMarginRight(appearance.图形右边距());
		if (!appearance.箭头图片().isEmpty()) {
			component.setArrowDrawable(drawable(appearance.箭头图片()));
		}
		if (!appearance.进度图片().isEmpty()) {
			component.setProgressDrawable(drawable(appearance.进度图片()));
		}
	}

	/**
	 * 通过受保护字段提供每个经典刷新头独立的文案，不修改上游静态全局文字。
	 */
	private static final class ConfiguredHeader extends ClassicsHeader {
		/**
		 * 创建使用外观快照的经典刷新头。
		 *
		 * @param context 当前活动。
		 * @param appearance 外观快照。
		 */
		ConfiguredHeader(Context context, 刷新外观 appearance) {
			super(context);
			mTextPulling = appearance.拉动提示();
			mTextRelease = appearance.释放提示();
			mTextRefreshing = appearance.处理中提示();
			mTextFinish = appearance.完成提示();
			mTextFailed = appearance.失败提示();
			mTextLoading = appearance.另一方向提示();
			mTextSecondary = appearance.二楼提示();
			mTitleText.setText(mTextPulling);
			classic(this, appearance);
			setEnableLastTime(appearance.显示更新时间());
			setTimeFormat(new SimpleDateFormat(appearance.时间格式(), Locale.getDefault()));
			setTextSizeTime(appearance.时间字号());
			setTextTimeMarginTop(appearance.时间上边距());
			if (appearance.更新时间() >= 0) {
				setLastUpdateTime(new Date(appearance.更新时间()));
			}
			if (!appearance.更新时间文字().isEmpty()) {
				setLastUpdateText(appearance.更新时间文字());
			}
		}
	}

	/**
	 * 提供每个经典加载尾独立的文案。
	 */
	private static final class ConfiguredFooter extends ClassicsFooter {
		/**
		 * 创建使用外观快照的经典加载尾。
		 *
		 * @param context 当前活动。
		 * @param appearance 外观快照。
		 */
		ConfiguredFooter(Context context, 刷新外观 appearance) {
			super(context);
			mTextPulling = appearance.拉动提示();
			mTextRelease = appearance.释放提示();
			mTextLoading = appearance.处理中提示();
			mTextFinish = appearance.完成提示();
			mTextFailed = appearance.失败提示();
			mTextRefreshing = appearance.另一方向提示();
			mTextNothing = appearance.无更多提示();
			mTitleText.setText(mTextPulling);
			classic(this, appearance);
		}
	}

	/**
	 * 把用户内容作为真实刷新头，动画由公开的移动和阶段事件驱动。
	 */
	private final class CustomHeader extends RefreshHeaderWrapper {
		/**
		 * 创建用户刷新头。
		 *
		 * @param content 已借出的视图。
		 */
		CustomHeader(View content) {
			super(content);
			mSpinnerStyle = spinnerStyle(headerAppearance.变换样式());
		}

		@Override
		public int onFinish(RefreshLayout layout, boolean success) {
			return headerAppearance.完成停留时间();
		}

		@Override
		public boolean isSupportHorizontalDrag() {
			return headerAppearance.自定义横向拖动();
		}

		@Override
		public void onHorizontalDrag(float percent, int offset, int maxOffset) {
			if (owner.isUsable()) {
				owner.头部水平拖动(percent, offset, maxOffset);
			}
		}
	}

	/**
	 * 把用户内容作为真实加载尾，保持原生分页结束标志。
	 */
	private final class CustomFooter extends RefreshFooterWrapper {
		/**
		 * 创建用户加载尾。
		 *
		 * @param content 已借出的视图。
		 */
		CustomFooter(View content) {
			super(content);
			mSpinnerStyle = spinnerStyle(footerAppearance.变换样式());
		}

		@Override
		public int onFinish(RefreshLayout layout, boolean success) {
			return footerAppearance.完成停留时间();
		}

		@Override
		public boolean setNoMoreData(boolean value) {
			return true;
		}

		@Override
		public boolean isSupportHorizontalDrag() {
			return footerAppearance.自定义横向拖动();
		}

		@Override
		public void onHorizontalDrag(float percent, int offset, int maxOffset) {
			if (owner.isUsable()) {
				owner.尾部水平拖动(percent, offset, maxOffset);
			}
		}
	}

	/**
	 * 将完整主题数组应用到当前头尾并保留到各自快照。
	 *
	 * @param colors ARGB 颜色序列。
	 */
	void theme(int[] colors) {
		headerAppearance.置主题颜色(colors);
		footerAppearance.置主题颜色(colors);
		view.setPrimaryColors(colors);
		reapplyColors();
	}

	/**
	 * 同步坦克游戏首次显示的遮罩文案。
	 */
	private static final class ConfiguredBattle extends FunGameBattleCityHeader {
		/**
		 * 创建坦克游戏头。
		 *
		 * @param context 当前活动。
		 * @param appearance 外观快照。
		 */
		ConfiguredBattle(Context context, 刷新外观 appearance) {
			super(context);
			mMaskViewTop.setText(appearance.游戏拉动提示());
			mMaskViewBottom.setText(appearance.游戏操作提示());
		}
	}

	/**
	 * 同步打砖块游戏首次显示的遮罩文案。
	 */
	private static final class ConfiguredBlock extends FunGameHitBlockHeader {
		/**
		 * 创建打砖块游戏头。
		 *
		 * @param context 当前活动。
		 * @param appearance 外观快照。
		 */
		ConfiguredBlock(Context context, 刷新外观 appearance) {
			super(context);
			mMaskViewTop.setText(appearance.游戏拉动提示());
			mMaskViewBottom.setText(appearance.游戏操作提示());
		}
	}

	/**
	 * 飞行头在离开窗口时停止原生动画，归还外部图标的初始变换。
	 */
	private final class ManagedFly extends FlyRefreshHeader {
		private final View plane = flyImage;
		private final MountainSceneView scene = flyScene;
		private final float[] transforms;

		/**
		 * 保存绑定图标的初始位置、缩放和旋转。
		 *
		 * @param context 当前窗口上下文。
		 */
		ManagedFly(Context context) {
			super(context);
			transforms = new float[] { plane.getTranslationX(), plane.getTranslationY(), plane.getScaleX(),
					plane.getScaleY(), plane.getRotation(), plane.getRotationX(), plane.getRotationY() };
		}

		@Override
		protected void onAttachedToWindow() {
			super.onAttachedToWindow();
			setUp(scene, plane);
			view.setEnableRefresh(owner.允许下拉());
		}

		@Override
		public void finishRefresh(final android.animation.AnimatorListenerAdapter listener) {
			super.finishRefresh(new android.animation.AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(android.animation.Animator animation) {
					if (owner.isUsable()) {
						owner.飞行动画结束();
						if (listener != null) {
							listener.onAnimationEnd(animation);
						}
					}
				}
			});
		}

		@Override
		protected void onDetachedFromWindow() {
			// cancel 会触发结束回调，先移除整棵动画树的监听，避免离屏后通知业务。
			if (mFlyAnimator != null) {
				removeAnimationListeners(mFlyAnimator);
				mFlyAnimator.cancel();
				mFlyAnimator = null;
			}
			mIsRefreshing = false;
			mCurrentPercent = 0;
			mOffset = 0;
			setUp(null, null);
			plane.animate().cancel();
			plane.clearAnimation();
			plane.setTranslationX(transforms[0]);
			plane.setTranslationY(transforms[1]);
			plane.setScaleX(transforms[2]);
			plane.setScaleY(transforms[3]);
			plane.setRotation(transforms[4]);
			plane.setRotationX(transforms[5]);
			plane.setRotationY(transforms[6]);
			scene.updatePercent(0);
			super.onDetachedFromWindow();
		}
	}

	/**
	 * 递归解除飞行组合动画的回调和属性更新监听。
	 *
	 * @param animation 要取消的原生动画。
	 */
	private static void removeAnimationListeners(android.animation.Animator animation) {
		animation.removeAllListeners();
		if (animation instanceof android.animation.AnimatorSet) {
			for (android.animation.Animator child : ((android.animation.AnimatorSet) animation).getChildAnimations()) {
				removeAnimationListeners(child);
			}
		} else if (animation instanceof android.animation.ValueAnimator) {
			((android.animation.ValueAnimator) animation).removeAllUpdateListeners();
		}
	}
}
