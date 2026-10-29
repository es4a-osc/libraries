package simple.library.wsd.xui;

import java.util.Arrays;

import android.view.View;

import com.xuexiang.xui.widget.picker.wheelview.WheelView;
import com.xuexiang.xui.widget.picker.wheelview.listener.OnItemSelectedListener;
import com.xuexiang.xui.widget.picker.widget.adapter.ArrayWheelAdapter;

import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * XUI滚轮的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI滚轮Impl extends XUI视图组件基类 implements XUI滚轮 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private WheelView view;

	// 当前数据用于校验索引及事件文本。
	private String[] items;

	/**
	 * 创建XUI滚轮。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI滚轮Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new WheelView(ComponentSupport.createContext());

		// 上游只在存在 XML 属性时填入默认颜色；Java 构造须补齐同样的不透明默认值。
		// 外侧画笔会在滚动时更新透明度，中间文字与分隔线则始终使用此处设置的颜色。
		view.setTextColorOut(0xFFA8A8A8);
		view.setTextColorCenter(0xFF2A2A2A);
		view.setDividerColor(0xFFD5D5D5);

		items = new String[0];

		view.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(int index) {
				项目被选择(index, items[index]);
			}
		});

		return view;
	}

	@Override
	public void 置循环(boolean cyclic) {
		view.setCyclic(cyclic);
	}

	@Override
	public void 置文字大小(float size) {
		view.setTextSize(size);
	}

	@Override
	public void 置标签(String label) {
		view.setLabel(label);
	}

	@Override
	public void 置对齐(int gravity) {
		view.setGravity(gravity);
	}

	@Override
	public void 置是否选项(boolean options) {
		view.setIsOptions(options);
	}

	@Override
	public void 置文本颜色外侧(int textColorOut) {
		view.setTextColorOut(textColorOut);
		view.invalidate();
	}

	@Override
	public void 置文本颜色中间(int textColorCenter) {
		view.setTextColorCenter(textColorCenter);
		view.invalidate();
	}

	@Override
	public void 置文本横向偏移(int textXOffset) {
		view.setTextXOffset(textXOffset);
	}

	@Override
	public void 置分隔线颜色(int dividerColor) {
		view.setDividerColor(dividerColor);
		view.invalidate();
	}

	@Override
	public void 置行间距倍数(float lineSpacingMultiplier) {
		view.setLineSpacingMultiplier(lineSpacingMultiplier);
	}

	@Override
	public boolean 是否循环() {
		return view.isLoop();
	}

	@Override
	public float 取项目高度() {
		return view.getItemHeight();
	}

	@Override
	public void 置项目数组(String[] itemsValue) {
		items = ComponentSupport.copyTextArray(itemsValue);
		view.setAdapter(new ArrayWheelAdapter<String>(Arrays.asList(items)));
	}

	@Override
	public int 取项目数() {
		return items.length;
	}

	@Override
	public void 选择项目(int index) {
		ComponentSupport.validateIndex(index, items.length);
		view.setCurrentItem(index);
	}

	@Override
	public int 取选中索引() {
		return items.length == 0 ? -1 : view.getCurrentItem();
	}

	@Override
	public void 项目被选择(int index, String text) {
		EventDispatcher.dispatchEvent(this, "项目被选择", index, text);
	}
}
