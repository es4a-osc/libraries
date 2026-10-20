package simple.library.wsd.pullrefresh;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 显示指定方向的状态提示和辅助文本，不推断应用业务结果或生成时间。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class LoadingIndicator extends FrameLayout {

	private final TextView hint;
	private final TextView auxiliary;
	private final ProgressBar progress;
	private final ImageView arrow;
	private final String[] texts;
	private final float arrowRotation;
	private int state;

	/**
	 * 创建方向提示，初始显示默认拉动文案，辅助文本为空。
	 *
	 * @param context 视图上下文。
	 * @param header 是否为下拉方向。
	 */
	LoadingIndicator(Context context, boolean header) {
		super(context);

		String prefix = header ? "pull_to_refresh_header" : "pull_to_load_footer";
		LayoutInflater.from(context).inflate(resource(prefix, "layout"), this, true);
		hint = (TextView) findViewById(resource(prefix + "_hint_textview", "id"));
		auxiliary = (TextView) findViewById(resource(prefix + "_auxiliary_textview", "id"));
		progress = (ProgressBar) findViewById(resource(prefix + "_progressbar", "id"));
		arrow = (ImageView) findViewById(resource(prefix + "_arrow", "id"));

		texts = header ? new String[] {"下拉刷新", "松开刷新", "正在刷新", ""}
			: new String[] {"上拉加载更多", "松开加载", "正在加载", ""};
		arrowRotation = header ? 0 : 180;
		state = -1;
		showState(false, false);
	}

	/**
	 * 更新提示状态；相同状态的连续手势不重复修改界面。
	 *
	 * @param busy 是否正在等待应用完成操作。
	 * @param ready 是否已超过松手触发距离。
	 */
	void showState(boolean busy, boolean ready) {
		int next = busy ? 2 : ready ? 1 : 0;
		if (state == next) {
			return;
		}

		state = next;
		showText(hint, texts[state]);
		showText(auxiliary, texts[3]);
		progress.setVisibility(busy ? View.VISIBLE : View.INVISIBLE);
		arrow.setVisibility(busy ? View.INVISIBLE : View.VISIBLE);
		arrow.setRotation(arrowRotation + (ready ? 180 : 0));
	}

	/**
	 * 取得指定状态文案或辅助文本。
	 *
	 * @param index 0 为拉动提示，1 为释放提示，2 为处理提示，3 为辅助文本。
	 * @return 当前文本。
	 */
	String text(int index) {
		return texts[index];
	}

	/**
	 * 修改文案；当前可见状态和辅助文本立即刷新，不触发业务事件。
	 *
	 * @param index 0 为拉动提示，1 为释放提示，2 为处理提示，3 为辅助文本。
	 * @param text 文案，空文本隐藏对应文字。
	 * @throws IllegalArgumentException 文本为 null。
	 */
	void text(int index, String text) {
		if (text == null) {
			throw new IllegalArgumentException("提示文本不能为 null；隐藏文字请使用空文本。");
		}

		texts[index] = text;
		if (index == state) {
			showText(hint, text);
		} else if (index == 3) {
			showText(auxiliary, text);
		}
	}

	/**
	 * 显示文字，空文本不占用文本行的高度。
	 *
	 * @param target 目标文字视图。
	 * @param text 待显示文本。
	 */
	private void showText(TextView target, String text) {
		target.setText(text);
		target.setVisibility(text.length() == 0 ? View.GONE : View.VISIBLE);
	}

	/**
	 * 查找带类库前缀的应用资源，缺失时给出具体名称。
	 *
	 * @param name 资源短名。
	 * @param type Android 资源类型。
	 * @return 资源编号。
	 * @throws IllegalStateException 类库资源未正确打包。
	 */
	private int resource(String name, String type) {
		String qualified = "es4a_pullrefresh_" + name;
		int id = getResources().getIdentifier(qualified, type, getContext().getPackageName());
		if (id == 0) {
			throw new IllegalStateException("拉动刷新框缺少资源：" + type + "/" + qualified);
		}

		return id;
	}
}
