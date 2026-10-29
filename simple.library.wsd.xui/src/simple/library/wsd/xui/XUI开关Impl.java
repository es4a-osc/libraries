package simple.library.wsd.xui;

import android.content.res.ColorStateList;
import android.view.View;
import android.widget.CompoundButton;

import com.xuexiang.xui.widget.button.switchbutton.SwitchButton;

import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * XUI开关的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI开关Impl extends XUI视图组件基类 implements XUI开关 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private SwitchButton view;

	/**
	 * 创建XUI开关。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI开关Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new SwitchButton(ComponentSupport.createContext());

		view.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton button, boolean checked) {
				选中改变(checked);
			}
		});

		return view;
	}

	@Override
	public void 置选中无事件(boolean checked) {
		view.setCheckedNoEvent(checked);
	}

	@Override
	public void 置选中立即无事件(boolean checked) {
		view.setCheckedImmediatelyNoEvent(checked);
	}

	@Override
	public void 置选中立即(boolean checked) {
		view.setCheckedImmediately(checked);
	}

	@Override
	public long 取动画毫秒() {
		return view.getAnimationDuration();
	}

	@Override
	public void 置动画毫秒(long animationDuration) {
		view.setAnimationDuration(animationDuration);
	}

	@Override
	public float 取滑块范围比例() {
		return view.getThumbRangeRatio();
	}

	@Override
	public void 置滑块范围比例(float thumbRangeRatio) {
		view.setThumbRangeRatio(thumbRangeRatio);
	}

	@Override
	public void 置滑块外边距(float left, float top, float right, float bottom) {
		view.setThumbMargin(left, top, right, bottom);
	}

	@Override
	public void 置滑块尺寸(int width, int height) {
		view.setThumbSize(width, height);
	}

	@Override
	public float 取滑块宽度() {
		return view.getThumbWidth();
	}

	@Override
	public float 取滑块高度() {
		return view.getThumbHeight();
	}

	@Override
	public float 取滑块圆角() {
		return view.getThumbRadius();
	}

	@Override
	public void 置滑块圆角(float thumbRadius) {
		view.setThumbRadius(thumbRadius);
	}

	@Override
	public float 取背景圆角() {
		return view.getBackRadius();
	}

	@Override
	public void 置背景圆角(float backRadius) {
		view.setBackRadius(backRadius);
	}

	@Override
	public boolean 是否渐变背景() {
		return view.isFadeBack();
	}

	@Override
	public void 置渐变背景(boolean fadeBack) {
		view.setFadeBack(fadeBack);
	}

	@Override
	public int 取着色颜色() {
		return view.getTintColor();
	}

	@Override
	public void 置着色颜色(int tintColor) {
		view.setTintColor(tintColor);
	}

	@Override
	public String 取文本开() {
		return String.valueOf(view.getTextOn());
	}

	@Override
	public String 取文本关() {
		return String.valueOf(view.getTextOff());
	}

	@Override
	public void 置文本滑块内缩(int textThumbInset) {
		view.setTextThumbInset(textThumbInset);
	}

	@Override
	public void 置文本扩展(int textExtra) {
		view.setTextExtra(textExtra);
	}

	@Override
	public void 置文本调整(int textAdjust) {
		view.setTextAdjust(textAdjust);
	}

	@Override
	public boolean 取选中() {
		return view.isChecked();
	}

	@Override
	public void 置选中(boolean checked) {
		view.setChecked(checked);
	}

	@Override
	public void 置滑块图像(String image) {
		view.setThumbDrawable(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置背景图像(String image) {
		view.setBackDrawable(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置背景颜色(int color) {
		view.setBackColor(ColorStateList.valueOf(color));
	}

	@Override
	public void 置滑块颜色(int color) {
		view.setThumbColor(ColorStateList.valueOf(color));
	}

	@Override
	public void 选中改变(boolean checked) {
		EventDispatcher.dispatchEvent(this, "选中改变", checked);
	}

	@Override
	public void 置状态文本(String on, String off) {
		view.setText(on, off);
	}

}
