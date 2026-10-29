package simple.library.wsd.xui;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xuexiang.xui.widget.layout.linkage.view.LinkageRecyclerView;

import simple.runtime.components.组件容器;

/**
 * 联动回收列表的数据更新、选择外观和滚动定位。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI联动列表Impl extends XUI列表基类 implements XUI联动列表 {

	private LinkageRecyclerView view;
	private RecyclerView.Adapter<RecyclerView.ViewHolder> adapter;

	/**
	 * 创建联动列表。
	 *
	 * @param container 所属容器。
	 */
	public XUI联动列表Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		data = new ListData();
		view = new LinkageRecyclerView(ComponentSupport.createContext());
		view.setLayoutManager(new LinearLayoutManager(view.getContext()));
		adapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
			@Override
			public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
				TextView text = new TextView(parent.getContext());
				text.setTextSize(16);
				text.setPadding(24, 24, 24, 24);
				text.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				return new RecyclerView.ViewHolder(text) {
				};
			}

			@Override
			public void onBindViewHolder(final RecyclerView.ViewHolder holder, int index) {
				((TextView) holder.itemView).setText(data.get(index).title);
				holder.itemView.setBackgroundColor(data.selected == index ? 0x2233B5E5 : Color.TRANSPARENT);
				holder.itemView.setActivated(data.selected == index);
				holder.itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View source) {
						int position = holder.getBindingAdapterPosition();
						if (position != RecyclerView.NO_POSITION) {
							项目被单击(position, data.get(position).title);
						}
					}
				});
			}

			@Override
			public int getItemCount() {
				return data.entries.size();
			}
		};
		view.setAdapter(adapter);
		return view;
	}

	@Override
	protected void refreshData() {
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void scrollTo(int index, boolean smooth) {
		if (smooth) {
			view.smoothScrollToPosition(index);
		} else {
			view.scrollToPosition(index);
		}
	}
}
