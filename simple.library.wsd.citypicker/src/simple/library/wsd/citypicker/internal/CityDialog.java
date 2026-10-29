package simple.library.wsd.citypicker.internal;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import simple.library.wsd.citypicker.R;

/**
 * 将 CityPicker 1.0.2 的整页选择界面移植到普通 Dialog，不要求 FragmentActivity。
 * 数据、主题和监听器只属于本次显示，关闭后不继续提交定位结果。
 *
 * @author xuexiang
 * @author 树先生 xhwsd@qq.com
 */
public final class CityDialog extends Dialog implements CityAdapter.Listener {

	/**
	 * 接收选择结果与定位请求，生命周期由扩展组件管理。
	 */
	public interface Listener {
		/**
		 * 用户选择了城市，Dialog 已先关闭。
		 *
		 * @param city 选中的城市记录。
		 */
		void picked(City city);

		/**
		 * 请求一次定位，调用方异步回填结果。
		 */
		void locate();
	}

	private final List<City> cities;
	private final List<City> hot;
	private final Listener listener;
	private final int animation;
	private City located;
	private CityAdapter adapter;
	private EditText search;
	private RecyclerView list;
	private View empty;
	private View clear;
	private SideIndexBar index;

	/**
	 * 创建具有独立主题和数据快照的城市选择界面。
	 *
	 * @param context 宿主 Activity。
	 * @param theme 继承 CityPickerDialogTheme 的主题编号。
	 * @param animation 窗口动画样式，零表示不使用动画。
	 * @param cities 已排序城市列表。
	 * @param hot 热门城市列表。
	 * @param located 初始定位城市，允许为空。
	 * @param listener 选择和定位监听器。
	 */
	public CityDialog(Context context, int theme, int animation, List<City> cities,
			List<City> hot, City located, Listener listener) {
		super(context, theme);
		this.animation = animation;
		this.cities = new ArrayList<City>(cities);
		this.hot = new ArrayList<City>(hot);
		this.located = located;
		this.listener = listener;
	}

	@Override
	protected void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.cp_dialog_city_picker);
		search = findViewById(R.id.cp_search_box);
		list = findViewById(R.id.cp_city_recyclerView);
		empty = findViewById(R.id.cp_empty_view);
		clear = findViewById(R.id.cp_clear_all);
		index = findViewById(R.id.cp_side_index_bar);
		TextView overlay = findViewById(R.id.cp_overlay);

		// 普通 Dialog 自行提供主题，宿主不必继承第三方 Activity 或修改应用主题。
		adapter = new CityAdapter(cities, hot, located, this);
		final LinearLayoutManager manager = new LinearLayoutManager(getContext());
		list.setLayoutManager(manager);
		list.setAdapter(adapter);
		list.setItemAnimator(null);
		list.addItemDecoration(new SectionDecoration(getContext(), adapter));
		index.bind(overlay, new SideIndexBar.Listener() {
			@Override
			public void selected(String section) {
				for (int i = 0; i < adapter.getItemCount(); i++) {
					if (adapter.section(i).equals(section)) {
						manager.scrollToPositionWithOffset(i, 0);
						return;
					}
				}
			}
		});
		search.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence text, int start, int count, int after) {
				// 搜索仅在新文本提交后更新列表。
			}

			@Override
			public void onTextChanged(CharSequence text, int start, int before, int count) {
				// 不在输入法修改文本的中间阶段改变选择。
			}

			@Override
			public void afterTextChanged(Editable text) {
				filter(text.toString());
			}
		});
		clear.setContentDescription("清除搜索");
		clear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				search.setText("");
			}
		});
		findViewById(R.id.cp_cancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				cancel();
			}
		});
		setCanceledOnTouchOutside(false);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Window window = getWindow();
		if (window != null) {
			window.setBackgroundDrawableResource(android.R.color.white);
			window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			if (Build.VERSION.SDK_INT >= 30) {
				applyInsets(window);
			} else {
				resizeLegacy(window);
			}
			window.setWindowAnimations(animation);
		}
	}

	/**
	 * API 30 起直接消费系统栏与输入法边距，搜索框和列表均保持可见。
	 *
	 * @param window 本次 Dialog 的窗口。
	 */
	private void applyInsets(Window window) {
		window.setDecorFitsSystemWindows(false);
		window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
				| WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		View content = findViewById(android.R.id.content);
		content.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
			@Override
			public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
				Insets space = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.ime());
				view.setPadding(space.left, space.top, space.right, space.bottom);
				return insets;
			}
		});
		content.requestApplyInsets();
	}

	/**
	 * API 24 至 29 仍通过窗口的传统输入法调整模式完成相同行为。
	 *
	 * @param window 本次 Dialog 的窗口。
	 */
	@SuppressWarnings("deprecation")
	private void resizeLegacy(Window window) {
		window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
				| WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	/**
	 * 搜索框与当前城市列表同时更新；空结果不保留旧的可点击城市。
	 *
	 * @param keyword 用户输入的名称或拼音。
	 */
	private void filter(String keyword) {
		boolean searching = !keyword.trim().isEmpty();
		List<City> result = CityStore.search(cities, keyword);
		adapter.update(result, searching);
		empty.setVisibility(searching && result.isEmpty() ? View.VISIBLE : View.GONE);
		clear.setVisibility(keyword.isEmpty() ? View.GONE : View.VISIBLE);
		index.setVisibility(searching ? View.GONE : View.VISIBLE);
		list.scrollToPosition(0);
	}

	/**
	 * 将当前有效定位结果交给定位行，不影响搜索输入。
	 *
	 * @param city 定位城市，等待或失败时允许为空。
	 * @param state 0 等待，1 成功，2 失败。
	 */
	public void location(City city, int state) {
		located = city;
		adapter.location(city, state);
	}

	@Override
	public void pick(City city) {
		dismiss();
		listener.picked(city);
	}

	@Override
	public void locate() {
		location(null, 0);
		listener.locate();
	}

	@Override
	public void dismiss() {
		if (search != null) {
			InputMethodManager input = getContext().getSystemService(InputMethodManager.class);
			if (input != null) {
				input.hideSoftInputFromWindow(search.getWindowToken(), 0);
			}
		}
		super.dismiss();
	}
}
