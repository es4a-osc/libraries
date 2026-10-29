package simple.library.wsd.xui;

import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.xuexiang.xui.utils.SnackbarUtils;
import com.xuexiang.xui.widget.popupwindow.bar.Es4aCookie;
import com.xuexiang.xui.widget.popupwindow.good.GoodView;
import com.xuexiang.xui.widget.toast.XToast;

import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * 提示的 XUI 实现与 Simple 生命周期适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI提示Impl extends XUI弹层基类 implements XUI提示 {

	// 配置只作用于下一次显示；已经显示的消息继续使用自己的快照。
	private int[] barColors;
	private Drawable barIcon;
	private int[] barMargins = new int[4];
	private int[] snackbarColors;
	private int[] floatingRange;
	private int floatingDuration = 800;
	private float floatingFromAlpha = 1;
	private float floatingToAlpha;

	/**
	 * 创建XUI提示。
	 *
	 * @param container 所属 Simple 窗口。
	 */
	public XUI提示Impl(组件容器 container) {
		super(container);
	}

	private Toast toast;
	private Snackbar snackbar;
	private Es4aCookie bar;
	private GoodView floating;

	@Override
	public void 显示吐司(String text, int style, boolean longDuration) {
		requireVisibleOwner();
		ComponentSupport.validateIndex(style, 5);
		if (toast != null) {
			toast.cancel();
		}
		int duration = longDuration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
		switch (style) {
			case 1:
				toast = XToast.info(ComponentSupport.createContext(), text, duration);
				break;
			case 2:
				toast = XToast.success(ComponentSupport.createContext(), text, duration);
				break;
			case 3:
				toast = XToast.warning(ComponentSupport.createContext(), text, duration);
				break;
			case 4:
				toast = XToast.error(ComponentSupport.createContext(), text, duration);
				break;
			default:
				toast = XToast.normal(ComponentSupport.createContext(), text, duration);
				break;
		}
		toast.show();
	}

	@Override
	public void 显示消息条(String title, String message, String action, long duration, boolean bottom) {
		requireVisibleOwner();
		if (duration <= 0) {
			throw new 非法参数错误();
		}
		if (bar != null) {
			bar.dismiss();
		}
		bar = new Es4aCookie(ComponentSupport.createContext(), title, message, action, duration, bottom,
			new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					操作被单击();
				}
			});
		FrameLayout.LayoutParams parameters = new FrameLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		bar.applyStyle(barColors, barIcon);
		parameters.setMargins(barMargins[0], barMargins[1], barMargins[2], barMargins[3]);
		parameters.gravity = bottom ? Gravity.BOTTOM : Gravity.TOP;
		getOverlayContainer().addView(bar, parameters);
	}

	@Override
	public void 显示飘字(可视组件 anchor, String text, int color, int size) {
		View target = requireAnchor(anchor);
		if (size <= 0 || text.isEmpty()) {
			throw new 非法参数错误();
		}
		if (floating != null) {
			floating.dismiss();
		}
		floating = new GoodView(ComponentSupport.createContext());
		// 先设置字号再测量文字，避免弹层沿用缺省字号的宽度。
		floating.setDuration(floatingDuration);
		floating.setAlpha(floatingFromAlpha, floatingToAlpha);
		if (floatingRange != null) {
			floating.setDistance(floatingRange[1]);
			floating.setTranslateY(-floatingRange[0], floatingRange[1]);
		}
		floating.setTextInfo(text, color, size);
		View content = floating.getContentView();
		int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		content.measure(measureSpec, measureSpec);
		// 上游以整数截断文字宽度；使用内容视图的测量值保留末尾字形。
		floating.setWidth(content.getMeasuredWidth());
		if (floatingRange != null) {
			floating.setHeight(floatingRange[1] + content.getMeasuredHeight());
		}
		floating.show(target);
	}

	@Override
	public void 关闭() {
		if (snackbar != null) {
			snackbar.dismiss();
			snackbar = null;
		}
		if (toast != null) {
			toast.cancel();
			toast = null;
		}
		if (bar != null) {
			bar.dismiss();
			bar = null;
		}
		if (floating != null) {
			floating.dismiss();
			floating = null;
		}
	}

	@Override
	public void 操作被单击() {
		dispatchEvent("操作被单击");
	}

	@Override
	public void 显示操作条(String text, String action, int duration) {
		View root = requireVisibleOwner();
		if (duration == 0 || duration < -1) {
			throw new 非法参数错误();
		}
		if (snackbar != null) {
			snackbar.dismiss();
		}
		SnackbarUtils builder = SnackbarUtils.Custom(root, text, duration);
		if (!action.isEmpty()) {
			builder.setAction(action, new View.OnClickListener() {
				@Override
				public void onClick(View source) {
					操作被单击();
				}
			});
		}
		snackbar = builder.getSnackbar();
		if (snackbarColors != null) {
			snackbar.setBackgroundTint(snackbarColors[0]);
			snackbar.setTextColor(snackbarColors[1]);
			snackbar.setActionTextColor(snackbarColors[2]);
		}
		builder.show();
	}

	@Override
	public void 置消息条颜色(int background, int title, int message, int action) {
		barColors = new int[] {background, title, message, action};
	}

	@Override
	public void 置消息条图标(String image) {
		barIcon = ComponentSupport.getDrawable(image);
	}

	@Override
	public void 置消息条边距(int left, int top, int right, int bottom) {
		barMargins = new int[] {toPixels(left), toPixels(top), toPixels(right), toPixels(bottom)};
	}

	@Override
	public void 置操作条颜色(int background, int text, int action) {
		snackbarColors = new int[] {background, text, action};
	}

	@Override
	public void 置飘字范围(int start, int end) {
		if (end < start) {
			throw new 非法参数错误();
		}
		floatingRange = new int[] {toPixels(start), toPixels(end)};
	}

	@Override
	public void 置飘字透明(float from, float to) {
		if (!Float.isFinite(from) || !Float.isFinite(to) || from < 0 || from > 1 || to < 0 || to > 1) {
			throw new 非法参数错误();
		}
		floatingFromAlpha = from;
		floatingToAlpha = to;
	}

	@Override
	public void 置飘字时长(int duration) {
		if (duration <= 0) {
			throw new 非法参数错误();
		}
		floatingDuration = duration;
	}

	/**
	 * 在保存配置之前统一换算，拒绝无效尺寸和整数溢出。
	 *
	 * @param value 非负 dp。
	 * @return 对应像素数。
	 */
	private int toPixels(int value) {
		double pixels = value * (double) ComponentSupport.createContext().getResources().getDisplayMetrics().density;
		if (value < 0 || pixels > Integer.MAX_VALUE / 2) {
			throw new 非法参数错误();
		}
		return (int) Math.round(pixels);
	}

}
