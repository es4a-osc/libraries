package simple.library.wsd.citypicker;

import java.util.ArrayList;
import java.util.List;

import android.content.DialogInterface;
import android.os.Looper;
import android.view.View;

import simple.library.wsd.citypicker.internal.City;
import simple.library.wsd.citypicker.internal.CityDialog;
import simple.library.wsd.citypicker.internal.CityStore;
import simple.runtime.android.MainActivity;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;
import simple.runtime.components.impl.组件Impl;
import simple.runtime.components.impl.android.视图组件;
import simple.runtime.events.EventDispatcher;

/**
 * 将 CityPicker 的数据和整页弹层绑定到所属 Simple 窗口，隔离各次定位请求。
 * 配置、搜索结果和生命周期不依赖其它 ES4A 扩展库。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 城市选择器Impl extends 组件Impl implements 城市选择器,
		MainActivity.OnDestroyListener, View.OnAttachStateChangeListener {

	private List<City> customCities;
	private List<City> builtInCities;
	private List<City> hotCities;
	private City initialLocation;
	private int theme = R.style.CityPickerDialogTheme;
	private int animation = R.style.CityPickerAnimation;
	private View owner;
	private CityDialog dialog;
	private boolean destroyed;
	private long sequence;
	private long pendingRequest;

	/**
	 * 创建属于指定窗口的城市选择功能。
	 *
	 * @param container 所属 Simple 窗口。
	 */
	public 城市选择器Impl(组件容器 container) {
		super(container);
		使用默认热门城市();
		MainActivity.getContext().addOnDestroyListener(this);
	}

	@Override
	public void 初始化() {
		if (owner == null) {
			可视组件 component = (可视组件) getComponentContainer();
			owner = ((视图组件) component).getView();
			owner.addOnAttachStateChangeListener(this);
		}
		super.初始化();
	}

	@Override
	public void 置主题(String style) {
		checkThread();
		theme = resource(style, R.style.CityPickerDialogTheme);
	}

	@Override
	public void 置动画(String style) {
		checkThread();
		animation = resource(style, 0);
	}

	@Override
	public void 置城市数据(String[] names, String[] provinces, String[] pinyins, String[] codes) {
		checkThread();
		checkArrays(names, provinces, pinyins, codes);
		List<City> result = new ArrayList<City>();
		for (int i = 0; i < names.length; i++) {
			result.add(new City(names[i], provinces[i], pinyins[i], codes[i]));
		}
		customCities = CityStore.sorted(result);
	}

	@Override
	public void 使用内置城市() {
		checkThread();
		customCities = null;
	}

	@Override
	public void 置热门城市(String[] names, String[] provinces, String[] codes) {
		checkThread();
		checkArrays(names, provinces, codes);
		List<City> result = new ArrayList<City>();
		for (int i = 0; i < names.length; i++) {
			result.add(new City(names[i], provinces[i], "", codes[i]));
		}
		hotCities = result;
	}

	@Override
	public void 使用默认热门城市() {
		checkThread();
		置热门城市(new String[] {"北京", "上海", "广州", "深圳", "天津", "杭州", "南京", "成都", "武汉"},
				new String[] {"北京", "上海", "广东", "广东", "天津", "浙江", "江苏", "四川", "湖北"},
				new String[] {"101010100", "101020100", "101280101", "101280601", "101030100",
					"101210101", "101190101", "101270101", "101200101"});
	}

	@Override
	public void 置定位城市(String name, String province, String code) {
		checkThread();
		initialLocation = new City(name, province, "", code);
	}

	@Override
	public void 清除定位城市() {
		checkThread();
		initialLocation = null;
	}

	@Override
	public boolean 回填定位(long request, String name, String province, String code) {
		checkThread();
		if (!accepts(request)) {
			return false;
		}
		City city = new City(name, province, "", code);
		pendingRequest = 0;
		dialog.location(city, 1);
		return true;
	}

	@Override
	public boolean 定位失败(long request) {
		checkThread();
		if (!accepts(request)) {
			return false;
		}
		pendingRequest = 0;
		dialog.location(null, 2);
		return true;
	}

	@Override
	public void 显示() {
		checkThread();
		if (destroyed || owner == null || !owner.isAttachedToWindow() || !owner.isShown()) {
			throw new IllegalStateException("请在所属 Simple 窗口显示后调用城市选择器。");
		}
		if (是否显示()) {
			return;
		}
		if (customCities == null && builtInCities == null) {
			builtInCities = CityStore.load(MainActivity.getContext());
		}

		// 每次显示独立快照，后续配置变化不改写正在展示的数据。
		final CityDialog current = new CityDialog(MainActivity.getContext(), theme, animation,
				customCities == null ? builtInCities : customCities, hotCities, initialLocation,
				new CityDialog.Listener() {
					@Override
					public void picked(City city) {
						城市已选择(city.name, city.province, city.pinyin, city.code);
					}

					@Override
					public void locate() {
						requestLocation();
					}
				});
		dialog = current;
		current.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface source) {
				选择已取消();
			}
		});
		current.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface source) {
				if (dialog == current) {
					dialog = null;
					pendingRequest = 0;
				}
				已关闭();
			}
		});
		try {
			current.show();
		} catch (RuntimeException exception) {
			dialog = null;
			current.setOnDismissListener(null);
			current.dismiss();
			throw exception;
		}
		if (initialLocation == null) {
			requestLocation();
		}
	}

	@Override
	public void 关闭() {
		checkThread();
		pendingRequest = 0;
		if (dialog != null) {
			dialog.dismiss();
		}
	}

	@Override
	public boolean 是否显示() {
		checkThread();
		return dialog != null && dialog.isShowing();
	}

	/**
	 * 开始新的定位请求，重试同时废弃此前尚未回填的请求标记。
	 */
	private void requestLocation() {
		if (!是否显示()) {
			return;
		}
		pendingRequest = ++sequence;
		定位已请求(pendingRequest);
	}

	/**
	 * 校验回填请求仍属于正在显示的这一次定位。
	 *
	 * @param request 调用方传回的请求标记。
	 * @return 是否为当前未完成请求。
	 */
	private boolean accepts(long request) {
		return request != 0 && request == pendingRequest && 是否显示();
	}

	/**
	 * 按运行时资源包解析样式名称，并拒绝不存在或类型错误的标识。
	 *
	 * @param style @style/名称 或 @包名:style/名称。
	 * @param fallback 空文本使用的样式编号。
	 * @return 样式资源编号。
	 */
	private int resource(String style, int fallback) {
		if (style == null) {
			throw new IllegalArgumentException("样式不能为 null。");
		}
		if (style.isEmpty()) {
			return fallback;
		}
		if (!style.matches("@(?:[a-zA-Z0-9_.]+:)?style/[a-zA-Z0-9_.]+")) {
			throw new IllegalArgumentException("样式使用 @style/名称 格式。");
		}
		int result = MainActivity.getContext().getResources().getIdentifier(style.substring(1),
				"style", MainActivity.getContext().getPackageName());
		if (result == 0) {
			throw new IllegalArgumentException("找不到样式：" + style);
		}
		return result;
	}

	/**
	 * 校验平行数组，先完整检查再替换配置，避免失败时留下半份数据。
	 *
	 * @param arrays 需要长度一致的文本数组。
	 */
	private static void checkArrays(String[]... arrays) {
		int count = arrays[0] == null ? -1 : arrays[0].length;
		for (String[] array : arrays) {
			if (array == null || array.length != count) {
				throw new IllegalArgumentException("城市数据数组不能为 null，长度必须一致。");
			}
		}
	}

	/**
	 * 所有公开操作与事件在界面线程串行处理。
	 */
	private static void checkThread() {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			throw new IllegalStateException("城市选择器必须在界面线程调用。");
		}
	}

	@Override
	public void 城市已选择(String name, String province, String pinyin, String code) {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "城市已选择", name, province, pinyin, code);
		}
	}

	@Override
	public void 选择已取消() {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "选择已取消");
		}
	}

	@Override
	public void 已关闭() {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "已关闭");
		}
	}

	@Override
	public void 定位已请求(long request) {
		if (!destroyed) {
			EventDispatcher.dispatchEvent(this, "定位已请求", request);
		}
	}

	@Override
	public void onViewAttachedToWindow(View view) {
		// 重新进入所属窗口时保留配置，但不自动重开已关闭的选择器。
	}

	@Override
	public void onViewDetachedFromWindow(View view) {
		关闭();
	}

	@Override
	public void 销毁() {
		checkThread();
		if (destroyed) {
			return;
		}
		destroyed = true;
		关闭();
		if (owner != null) {
			owner.removeOnAttachStateChangeListener(this);
		}
		MainActivity.getContext().removeOnDestroyListener(this);
		builtInCities = null;
		customCities = null;
		super.销毁();
	}

	@Override
	public void onDestroy() {
		销毁();
	}
}
