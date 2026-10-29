package simple.library.wsd.xui;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;

import com.xuexiang.xui.R;
import com.xuexiang.xui.widget.searchview.MaterialSearchView;

import simple.runtime.android.MainActivity;
import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * XUI搜索框的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI搜索框Impl extends XUI视图组件基类 implements XUI搜索框,
		MainActivity.OnActivityResultListener, View.OnAttachStateChangeListener {

	private boolean submitSuggestions;

	// 平台 Activity 支持完整正整数请求号；独立区间避免与其它组件的返回结果混淆。
	private static final AtomicInteger NEXT_VOICE_REQUEST = new AtomicInteger(0x58550000);

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private MaterialSearchView view;
	private boolean voiceEnabled;
	private boolean released;
	private int voiceRequest;

	/**
	 * 创建XUI搜索框。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI搜索框Impl(组件容器 container) {
		super(container);
		MainActivity.getContext().addOnActivityResultListener(this);
		view.addOnAttachStateChangeListener(this);
	}

	@Override
	protected View createView() {
		view = new MaterialSearchView(ComponentSupport.createContext()) {
			@Override
			public void showVoice(boolean show) {
				// 上游的可用性判断及 Activity 强转不适用于主题包装上下文。
				// 图标仍随输入状态变化；真正的启动能力在调用时检查并报告。
				View button = findViewById(R.id.action_voice_btn);
				if (button != null) {
					button.setVisibility(show && voiceEnabled ? View.VISIBLE : View.GONE);
				}
			}
		};
		view.findViewById(R.id.action_voice_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View source) {
				开始语音搜索();
			}
		});

		view.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String text) {
				搜索提交(text);
				return true;
			}

			@Override
			public boolean onQueryTextChange(String text) {
				搜索词改变(text);
				return false;
			}
		});

		view.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
			@Override
			public void onSearchViewShown() {
				已展开();
			}

			@Override
			public void onSearchViewClosed() {
				已收起();
			}
		});
		return view;
	}

	@Override
	public void 置文字颜色(int color) {
		view.setTextColor(color);
	}

	@Override
	public void 置提示文本颜色(int color) {
		view.setHintTextColor(color);
	}

	@Override
	public void 置提示文本(String hint) {
		view.setHint(hint);
	}

	@Override
	public void 置输入类型(int inputType) {
		view.setInputType(inputType);
	}

	@Override
	public void 置语音搜索(boolean voiceSearch) {
		voiceEnabled = voiceSearch;
		if (!voiceSearch) {
			voiceRequest = 0;
		}
		view.setVoiceSearch(voiceSearch);
		EditText input = view.findViewById(R.id.searchTextView);
		view.showVoice(TextUtils.isEmpty(input.getText()));
	}

	@Override
	public boolean 是否支持语音搜索() {
		if (released) {
			return false;
		}
		return !MainActivity.getContext().getPackageManager().queryIntentActivities(createVoiceIntent(), 0).isEmpty();
	}

	@Override
	public boolean 开始语音搜索() {
		if (released || !voiceEnabled || !view.isAttachedToWindow() || !view.isShown()) {
			return rejectVoice("请在当前可见搜索框启用语音搜索后调用。");
		}
		if (voiceRequest != 0) {
			return rejectVoice("当前搜索框已有语音识别正在进行。");
		}
		int request = NEXT_VOICE_REQUEST.getAndIncrement();
		if (request <= 0) {
			return rejectVoice("语音识别请求标识已耗尽。");
		}

		// 先登记请求，再启动外部界面；失败时撤销登记，允许重试。
		voiceRequest = request;
		try {
			MainActivity.getContext().startActivityForResult(createVoiceIntent(), request);
			return true;
		} catch (ActivityNotFoundException failure) {
			voiceRequest = 0;
			return rejectVoice("设备没有可用的语音识别应用。");
		} catch (RuntimeException failure) {
			voiceRequest = 0;
			String message = failure.getMessage();
			return rejectVoice(message == null ? "无法启动语音识别。" : message);
		}
	}

	/**
	 * 查询与启动共用识别意图，避免可用性判断与实际调用不一致。
	 *
	 * @return 系统搜索短语识别意图。
	 */
	private static Intent createVoiceIntent() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
		return intent;
	}

	/**
	 * 用事件解释识别失败，不把无法启动伪装成成功。
	 *
	 * @param message 失败原因。
	 * @return 始终为假，表示未启动识别或没有取得结果。
	 */
	private boolean rejectVoice(String message) {
		语音搜索失败(message);
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (released || voiceRequest == 0 || requestCode != voiceRequest) {
			return;
		}
		voiceRequest = 0;
		if (!view.isAttachedToWindow() || !view.isShown()) {
			return;
		}
		if (resultCode == Activity.RESULT_CANCELED) {
			语音搜索已取消();
			return;
		}
		if (resultCode != Activity.RESULT_OK || data == null) {
			rejectVoice("语音识别没有返回有效结果。");
			return;
		}
		String text;
		try {
			ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			text = results == null || results.isEmpty() ? null : results.get(0);
		} catch (RuntimeException failure) {
			// 返回数据由外部应用提供；类型或反序列化错误不能使宿主崩溃。
			rejectVoice("语音识别返回的数据格式无效。");
			return;
		}
		if (text == null || text.trim().isEmpty()) {
			rejectVoice("语音识别没有返回有效文字。");
			return;
		}
		// 复用键盘提交路径，依次通知文字变化与搜索提交，不另建第二套搜索状态。
		view.setQuery(text, true);
	}

	@Override
	public void onViewAttachedToWindow(View source) {
		// 再次进入窗口时可以发起新请求，不复用离开前的识别结果。
	}

	@Override
	public void onViewDetachedFromWindow(View source) {
		voiceRequest = 0;
	}

	@Override
	public void 销毁() {
		if (released) {
			return;
		}
		released = true;
		voiceRequest = 0;
		MainActivity.getContext().removeOnActivityResultListener(this);
		view.removeOnAttachStateChangeListener(this);
		super.销毁();
	}

	@Override
	public void 语音搜索已取消() {
		EventDispatcher.dispatchEvent(this, "语音搜索已取消");
	}

	@Override
	public void 语音搜索失败(String message) {
		EventDispatcher.dispatchEvent(this, "语音搜索失败", message);
	}

	@Override
	public void 显示候选() {
		view.showSuggestions();
	}

	@Override
	public void 置提交开单击(boolean submit) {
		view.setSubmitOnClick(submit);
		submitSuggestions = submit;
	}

	@Override
	public void 置候选(String[] suggestions) {
		String[] data = ComponentSupport.copyTextArray(suggestions);
		view.setSuggestions(data);
		if (data.length == 0) {
			// 上游只隐藏背景，不清理旧适配器；空候选必须同时移除旧列表。
			view.setAdapter(null);
			view.dismissSuggestions();
		}
		view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View clicked, int index, long id) {
				String text = String.valueOf(parent.getItemAtPosition(index));
				view.setQuery(text, submitSuggestions);
				候选被单击(index, text);
			}
		});
	}

	@Override
	public void 隐藏候选() {
		view.dismissSuggestions();
	}

	@Override
	public boolean 是否搜索打开() {
		return view.isSearchOpen();
	}

	@Override
	public void 置动画毫秒(int duration) {
		view.setAnimationDuration(duration);
	}

	@Override
	public void 展开搜索() {
		view.showSearch();
	}

	@Override
	public void 展开搜索(boolean animate) {
		view.showSearch(animate);
	}

	@Override
	public void 关闭搜索() {
		voiceRequest = 0;
		view.closeSearch();
	}

	@Override
	public void 置省略(boolean ellipsize) {
		view.setEllipsize(ellipsize);
	}

	@Override
	public void 置搜索词(String text, boolean submit) {
		view.setQuery(text, submit);
	}

	@Override
	public void 置语音图标(String image) {
		view.setVoiceIcon(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置返回图标(String image) {
		view.setBackIcon(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置候选图标(String image) {
		view.setSuggestionIcon(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 置候选背景(String image) {
		view.setSuggestionBackground(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 搜索提交(String text) {
		EventDispatcher.dispatchEvent(this, "搜索提交", text);
	}

	@Override
	public void 搜索词改变(String text) {
		EventDispatcher.dispatchEvent(this, "搜索词改变", text);
	}

	@Override
	public void 已展开() {
		EventDispatcher.dispatchEvent(this, "已展开");
	}

	@Override
	public void 已收起() {
		EventDispatcher.dispatchEvent(this, "已收起");
	}

	@Override
	public void 候选被单击(int index, String text) {
		EventDispatcher.dispatchEvent(this, "候选被单击", index, text);
	}

}
