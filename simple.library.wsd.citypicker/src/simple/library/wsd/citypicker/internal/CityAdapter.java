package simple.library.wsd.citypicker.internal;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import simple.library.wsd.citypicker.R;

/**
 * 沿用 CityPicker 的定位、热门和字母分组布局；搜索结果使用独立数据视图。
 * 定位回填只更新定位行，不能改写当前搜索结果的首项。
 *
 * @author xuexiang
 * @author 树先生 xhwsd@qq.com
 */
final class CityAdapter extends RecyclerView.Adapter<CityAdapter.Holder> {

	/**
	 * 将列表选择与重新定位请求交给当前 Dialog。
	 */
	interface Listener {
		/**
		 * 用户选中一条城市记录。
		 *
		 * @param city 被选中的城市。
		 */
		void pick(City city);

		/**
		 * 用户要求重新定位。
		 */
		void locate();
	}

	private final List<City> hot;
	private final Listener listener;
	private List<City> cities;
	private boolean searching;
	private City located;
	private int locationState;

	/**
	 * 创建由单个 Dialog 拥有的列表适配器。
	 *
	 * @param cities 当前城市数据。
	 * @param hot 热门城市快照，允许为空。
	 * @param located 初始定位城市，允许为空。
	 * @param listener 用户操作监听器。
	 */
	CityAdapter(List<City> cities, List<City> hot, City located, Listener listener) {
		this.cities = cities;
		this.hot = hot;
		this.located = located;
		this.listener = listener;
		locationState = located == null ? 0 : 1;
	}

	/**
	 * 替换搜索快照；保留独立的定位结果和热门城市。
	 *
	 * @param cities 新的列表数据。
	 * @param searching 是否仅展示匹配城市。
	 */
	void update(List<City> cities, boolean searching) {
		this.cities = cities;
		this.searching = searching;
		notifyDataSetChanged();
	}

	/**
	 * 更新定位状态，搜索期间也保留结果供清除搜索后使用。
	 *
	 * @param city 定位成功的城市，失败或等待时允许为空。
	 * @param state 0 等待，1 成功，2 失败。
	 */
	void location(City city, int state) {
		located = city;
		locationState = state;
		if (!searching) {
			notifyItemChanged(0);
		}
	}

	/**
	 * 返回列表位置的分组文本，供索引和悬浮分组共用。
	 *
	 * @param position 有效的适配器位置。
	 * @return 当前行所属分组。
	 */
	String section(int position) {
		if (!searching && position < 2) {
			return position == 0 ? "定位" : "热门";
		}
		return cities.get(position - (searching ? 0 : 2)).section();
	}

	@Override
	public int getItemCount() {
		return cities.size() + (searching ? 0 : 2);
	}

	@Override
	public int getItemViewType(int position) {
		return !searching && position < 2 ? position : 2;
	}

	@NonNull
	@Override
	public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
		int layout = type == 0 ? R.layout.cp_list_item_location_layout
				: type == 1 ? R.layout.cp_list_item_hot_layout : R.layout.cp_list_item_default_layout;
		return new Holder(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false), parent);
	}

	@Override
	public void onBindViewHolder(@NonNull Holder holder, int position) {
		View view = holder.itemView;
		int type = getItemViewType(position);
		if (type == 0) {
			TextView label = view.findViewById(R.id.cp_list_item_location);
			label.setText(locationState == 1 ? located.name : view.getContext().getString(
					locationState == 2 ? R.string.cp_locate_failed : R.string.cp_locating));
			View target = view.findViewById(R.id.cp_list_item_location_layout);
			Context context = view.getContext();
			int spacing = ThemeValues.dimension(context, R.attr.cpGridItemSpace, 8);
			int padding = context.getResources().getDimensionPixelSize(R.dimen.cp_default_padding);
			int indexWidth = context.getResources().getDimensionPixelSize(R.dimen.cp_index_bar_width);
			// 绑定发生在加入父视图之前，使用创建行时已知的列表，不能读取 itemView.getParent()。
			int available = holder.parent.getWidth();
			ViewGroup.LayoutParams parameters = target.getLayoutParams();
			parameters.width = Math.max(1, (available - 2 * padding - indexWidth - 2 * spacing) / 3);
			target.setLayoutParams(parameters);
			target.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View clicked) {
					if (locationState == 1 && located != null) {
						listener.pick(located);
					} else if (locationState == 2) {
						listener.locate();
					}
				}
			});
		} else if (type == 1) {
			RecyclerView grid = view.findViewById(R.id.cp_hot_list);
			grid.setLayoutManager(new GridLayoutManager(view.getContext(), 3));
			grid.setNestedScrollingEnabled(false);
			grid.setAdapter(new HotAdapter(hot, listener));
		} else {
			final City city = cities.get(position - (searching ? 0 : 2));
			TextView label = view.findViewById(R.id.cp_list_item_name);
			label.setText(city.name);
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View clicked) {
					listener.pick(city);
				}
			});
		}
	}

	/**
	 * 统一保留一行布局，不缓存可能因搜索失效的适配器位置。
	 */
	static final class Holder extends RecyclerView.ViewHolder {

		final ViewGroup parent;

		/**
		 * 保存当前行视图。
		 *
		 * @param view 已创建的行布局。
		 * @param parent 所属列表，用于布局前的可用宽度计算。
		 */
		Holder(View view, ViewGroup parent) {
			super(view);
			this.parent = parent;
		}
	}

	/**
	 * 热门城市网格，宽度由父列表决定，不使用整个显示屏宽度推算。
	 */
	private static final class HotAdapter extends RecyclerView.Adapter<Holder> {

		private final List<City> cities;
		private final Listener listener;

		/**
		 * 绑定当前 Dialog 的热门城市列表。
		 *
		 * @param cities 热门城市快照。
		 * @param listener 城市选择监听器。
		 */
		HotAdapter(List<City> cities, Listener listener) {
			this.cities = cities;
			this.listener = listener;
		}

		@NonNull
		@Override
		public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
			Context context = parent.getContext();
			View view = LayoutInflater.from(context).inflate(R.layout.cp_grid_item_layout, parent, false);
			int spacing = ThemeValues.dimension(context, R.attr.cpGridItemSpace, 8);
			RecyclerView.LayoutParams parameters = new RecyclerView.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			parameters.setMargins(spacing / 2, spacing / 2, spacing / 2, spacing / 2);
			view.setLayoutParams(parameters);
			return new Holder(view, parent);
		}

		@Override
		public void onBindViewHolder(@NonNull Holder holder, int position) {
			final City city = cities.get(position);
			TextView label = holder.itemView.findViewById(R.id.cp_gird_item_name);
			label.setText(city.name);
			holder.itemView.findViewById(R.id.cp_grid_item_layout).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					listener.pick(city);
				}
			});
		}

		@Override
		public int getItemCount() {
			return cities.size();
		}
	}
}
