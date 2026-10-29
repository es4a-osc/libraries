package simple.library.wsd.xui;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xuexiang.xui.widget.XUIWrapContentListView;

import simple.runtime.components.组件容器;
import simple.runtime.errors.非法参数错误;

/**
 * 自适应列表的文本、图文数据更新与原生行复用，滚动范围由最大高度限制。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI自适应列表Impl extends XUI列表基类 implements XUI自适应列表 {

	private XUIWrapContentListView view;
	private BaseAdapter adapter;

	/**
	 * 创建自适应列表。
	 *
	 * @param container 所属容器。
	 */
	public XUI自适应列表Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		data = new ListData();
		view = new XUIWrapContentListView(ComponentSupport.createContext());
		adapter = new BaseAdapter() {
			@Override
			public int getCount() {
				return data.entries.size();
			}

			@Override
			public Object getItem(int position) {
				return data.get(position).title;
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View recycled, ViewGroup parent) {
				// 仅复用本适配器创建的行，不保留上一次数据源的视图或加载任务。
				LinearLayout row = recycled instanceof LinearLayout ? (LinearLayout) recycled : null;
				if (row == null) {
					row = new LinearLayout(view.getContext());
					row.setGravity(Gravity.CENTER_VERTICAL);
					int gap = Math.round(12 * row.getResources().getDisplayMetrics().density);
					row.setPadding(gap, gap, gap, gap);
					ImageView icon = new ImageView(row.getContext());
					icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
					row.addView(icon, new LinearLayout.LayoutParams(gap * 4, gap * 4));
					LinearLayout words = new LinearLayout(row.getContext());
					words.setOrientation(LinearLayout.VERTICAL);
					words.setPadding(gap, 0, 0, 0);
					TextView title = new TextView(row.getContext());
					title.setTextSize(16);
					words.addView(title);
					TextView detail = new TextView(row.getContext());
					detail.setTextSize(13);
					words.addView(detail);
					row.addView(words, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
				}
				ImageView icon = (ImageView) row.getChildAt(0);
				Glide.with(icon).clear(icon);
				icon.setImageDrawable(null);
				icon.setVisibility(data.get(position).image.isEmpty() ? View.GONE : View.VISIBLE);
				if (!data.get(position).image.isEmpty()) {
					ComponentSupport.loadImage(icon, data.get(position).image);
				}
				LinearLayout words = (LinearLayout) row.getChildAt(1);
				((TextView) words.getChildAt(0)).setText(data.get(position).title);
				((TextView) words.getChildAt(1)).setText(data.get(position).detail);
				words.getChildAt(1).setVisibility(data.get(position).detail.isEmpty() ? View.GONE : View.VISIBLE);
				row.setActivated(data.selected == position);
				row.setBackgroundColor(data.selected == position ? 0x2233B5E5 : android.graphics.Color.TRANSPARENT);
				return row;
			}
		};
		view.setAdapter(adapter);
		view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View child, int index, long id) {
				项目被单击(index, data.get(index).title);
			}
		});
		return view;
	}

	@Override
	protected void refreshData() {
		adapter.notifyDataSetChanged();
		view.requestLayout();
	}

	@Override
	protected void scrollTo(int index, boolean smooth) {
		if (smooth) {
			view.smoothScrollToPosition(index);
		} else {
			view.setSelection(index);
		}
	}

	@Override
	public void 置最大高度(int height) {
		if (height < 0) {
			throw new 非法参数错误();
		}
		view.setMaxHeight(height);
		view.requestLayout();
	}

	@Override
	public void 置图文项目(String[] titles, String[] details, String[] images) {
		if (details == null || images == null) {
			throw new 非法参数错误();
		}
		data.replace(titles, details, images);
		refreshData();
	}

	@Override
	public void 置图文项目内容(int index, String title, String detail, String image) {
		ListData.Entry entry = new ListData.Entry(title, detail, image);
		data.get(index);
		data.entries.set(index, entry);
		refreshData();
	}

	@Override
	public void 添加图文项目(String title, String detail, String image) {
		data.entries.add(new ListData.Entry(title, detail, image));
		refreshData();
	}
}
