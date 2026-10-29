package simple.library.wsd.xui;

import android.view.View;
import android.widget.TextView;

import com.xuexiang.xui.R;
import com.xuexiang.xui.widget.textview.supertextview.CommonTextView;

import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;
import simple.runtime.events.EventDispatcher;

/**
 * XUI通用文本的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI通用文本Impl extends XUI视图组件基类 implements XUI通用文本 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private CommonTextView view;

	/**
	 * 创建XUI通用文本。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI通用文本Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new CommonTextView(ComponentSupport.createContext());

		view.setOnCommonTextViewClickListener(new CommonTextView.OnCommonTextViewClickListener() {

			@Override
			public void onLeftViewClick() {
				区域被单击("左侧");
			}

			@Override
			public void onCenterViewClick() {
				区域被单击("中间");
			}

			@Override
			public void onRightViewClick() {
				区域被单击("右侧");
			}
		});

		return view;
	}

	@Override
	public void 置左侧文本(String string) {
		view.setLeftTextString(string);
	}

	@Override
	public void 置左侧顶部文本(String string) {
		view.setLeftTopTextString(string);
	}

	@Override
	public void 置左侧底部文本(String string) {
		view.setLeftBottomTextString(string);
	}

	@Override
	public void 置左侧文本尺寸(float size) {
		view.setLeftTextSize(size);
	}

	@Override
	public void 置左侧文本颜色(int color) {
		view.setLeftTextColor(color);
	}

	@Override
	public void 置中间文本(String string) {
		view.setCenterTextString(string);
	}

	@Override
	public void 置中间顶部文本(String string) {
		view.setCenterTopTextString(string);
	}

	@Override
	public void 置中间底部文本(String string) {
		view.setCenterBottomTextString(string);
	}

	@Override
	public void 置中间文本尺寸(float size) {
		view.setCenterTextSize(size);
	}

	@Override
	public void 置中间文本颜色(int color) {
		view.setCenterTextColor(color);
	}

	@Override
	public void 置右侧文本(String string) {
		view.setRightTextString(string);
	}

	@Override
	public void 置右侧顶部文本(String string) {
		view.setRightTopTextString(string);
	}

	@Override
	public void 置右侧底部文本(String string) {
		view.setRightBottomTextString(string);
	}

	@Override
	public void 置右侧文本尺寸(float size) {
		view.setRightTextSize(size);
	}

	@Override
	public void 置右侧文本颜色(int color) {
		view.setRightTextColor(color);
	}

	@Override
	public String 取左侧文本() {
		return String.valueOf(view.getLeftTextString());
	}

	@Override
	public String 取左侧顶部文本() {
		return String.valueOf(view.getLeftTopTextString());
	}

	@Override
	public String 取左侧底部文本() {
		return String.valueOf(view.getLeftBottomTextString());
	}

	@Override
	public String 取中间文本() {
		return String.valueOf(view.getCenterTextString());
	}

	@Override
	public String 取中间顶部文本() {
		return String.valueOf(view.getCenterTopTextString());
	}

	@Override
	public String 取中间底部文本() {
		return String.valueOf(view.getCenterBottomTextString());
	}

	@Override
	public String 取右侧文本() {
		return String.valueOf(view.getRightTextString());
	}

	@Override
	public String 取右侧顶部文本() {
		return String.valueOf(view.getRightTopTextString());
	}

	@Override
	public String 取右侧底部文本() {
		return String.valueOf(view.getRightBottomTextString());
	}

	@Override
	public void 置左侧视图是否可单击(boolean isClickable) {
		view.setLeftViewIsClickable(isClickable);
	}

	@Override
	public void 置中间视图是否可单击(boolean isClickable) {
		view.setCenterViewIsClickable(isClickable);
	}

	@Override
	public void 置右侧视图是否可单击(boolean isClickable) {
		view.setRightViewIsClickable(isClickable);
	}

	@Override
	public void 置左侧图像左侧(String image) {
		view.setLeftDrawableLeft(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置左侧图像顶部(String image) {
		view.setLeftDrawableTop(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置左侧图像右侧(String image) {
		view.setLeftDrawableRight(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置左侧图像底部(String image) {
		view.setLeftDrawableBottom(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置中间图像左侧(String image) {
		view.setCenterDrawableLeft(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置中间图像顶部(String image) {
		view.setCenterDrawableTop(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置中间图像右侧(String image) {
		view.setCenterDrawableRight(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置中间图像底部(String image) {
		view.setCenterDrawableBottom(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置右侧图像左侧(String image) {
		view.setRightDrawableLeft(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置右侧图像顶部(String image) {
		view.setRightDrawableTop(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置右侧图像右侧(String image) {
		view.setRightDrawableRight(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置右侧图像底部(String image) {
		view.setRightDrawableBottom(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 区域被单击(String region) {
		EventDispatcher.dispatchEvent(this, "区域被单击", region);
	}

	@Override
	public void 置区域字体(int region, String asset, float size) {
		TextView[] regions = {view.<TextView>findViewById(R.id.cLeftTopTextId), view.<TextView>findViewById(R.id.cLeftTextId), view.<TextView>findViewById(R.id.cLeftBottomTextId), view.<TextView>findViewById(R.id.cCenterTopTextId), view.<TextView>findViewById(R.id.cCenterTextId), view.<TextView>findViewById(R.id.cCenterBottomTextId), view.<TextView>findViewById(R.id.cRightTopTextId), view.<TextView>findViewById(R.id.cRightTextId), view.<TextView>findViewById(R.id.cRightBottomTextId)};
		ComponentSupport.validateIndex(region, regions.length);
		if (!Float.isFinite(size) || size <= 0) {
			throw new 非法参数错误();
		}
		TextView target = regions[region];
		if (target == null) {
			throw new ComponentStateException("该文字区域尚未创建，请先设置区域文字。");
		}
		target.setTypeface(ThemeSupport.loadFont(asset));
		target.setTextSize(size);
	}

}
