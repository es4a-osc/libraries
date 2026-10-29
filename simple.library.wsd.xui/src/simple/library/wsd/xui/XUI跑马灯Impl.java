package simple.library.wsd.xui;

import java.util.List;

import simple.runtime.errors.非法参数错误;

import com.xuexiang.xui.widget.textview.marqueen.ComplexItemEntity;

import com.xuexiang.xui.widget.textview.marqueen.ComplexViewMF;

import android.widget.RelativeLayout;

import java.util.ArrayList;

import java.util.Arrays;

import android.view.View;
import android.widget.TextView;

import com.xuexiang.xui.widget.textview.marqueen.MarqueeFactory;
import com.xuexiang.xui.widget.textview.marqueen.MarqueeView;
import com.xuexiang.xui.widget.textview.marqueen.SimpleNoticeMF;

import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * XUI跑马灯的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI跑马灯Impl extends XUI视图组件基类 implements XUI跑马灯 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private MarqueeView view;

	/**
	 * 创建XUI跑马灯。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI跑马灯Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new MarqueeView(ComponentSupport.createContext());

		return view;
	}

	@Override
	public void 置刻度间隔(int interval) {
		view.setInterval(interval);
	}

	@Override
	public void 置动画时长(int animDuration) {
		view.setAnimDuration(animDuration);
	}

	@Override
	public void 置进出场动画(int animInId, int animOutID) {
		view.setAnimInAndOut(animInId, animOutID);
	}

	@Override
	public void 置公告数组(String[] items) {
		SimpleNoticeMF factory = new SimpleNoticeMF(view.getContext());
		factory.setData(Arrays.asList(ComponentSupport.copyTextArray(items)));
		factory.setOnItemClickListener(new MarqueeFactory.OnItemClickListener<TextView, String>() {
			@Override
			public void onItemClick(View clicked, MarqueeFactory.ViewHolder<TextView, String> holder) {
				公告被单击(holder.mPosition, holder.mData);
			}
		});
		view.setMarqueeFactory(factory);
		ThemeSupport.applyFont(view);
	}

	@Override
	public void 开始公告() {
		view.startFlipping();
	}

	@Override
	public void 停止公告() {
		view.stopFlipping();
	}

	@Override
	public void 公告被单击(int index, String text) {
		EventDispatcher.dispatchEvent(this, "公告被单击", index, text);
	}

	@Override
	public void 置复合公告(String[] titles, String[] descriptions, String[] times) {
		String[] heading = ComponentSupport.copyTextArray(titles);
		String[] detail = ComponentSupport.copyTextArray(descriptions);
		String[] time = ComponentSupport.copyTextArray(times);
		if (heading.length != detail.length || heading.length != time.length) {
			throw new 非法参数错误();
		}
		List<ComplexItemEntity> data = new ArrayList<ComplexItemEntity>();
		for (int index = 0; index < heading.length; index++) {
			data.add(new ComplexItemEntity(heading[index], detail[index], time[index]));
		}
		ComplexViewMF factory = new ComplexViewMF(view.getContext());
		factory.setData(data);
		factory.setOnItemClickListener(new MarqueeFactory.OnItemClickListener<RelativeLayout, ComplexItemEntity>() {
			@Override
			public void onItemClick(View clicked, MarqueeFactory.ViewHolder<RelativeLayout, ComplexItemEntity> holder) {
				公告被单击(holder.mPosition, holder.mData.getTitle());
				复合公告被单击(holder.mPosition, holder.mData.getTitle(), holder.mData.getSubTitle(), holder.mData.getTime());
			}
		});
		view.stopFlipping();
		view.setMarqueeFactory(factory);
		ThemeSupport.applyFont(view);
	}

	@Override
	public void 复合公告被单击(int index, String title, String description, String time) {
		EventDispatcher.dispatchEvent(this, "复合公告被单击", index, title, description, time);
	}

}
