package simple.library.wsd.xui;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xuexiang.xui.R;

/**
 * 复用上游菜单行布局，以本地图片和显式启用状态构建独立的菜单快照。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class BottomMenuAdapter extends BaseAdapter {

	/**
	 * 交由菜单会话处理实际选择，避免行控件持有其它会话的状态。
	 */
	interface SelectionListener {
		void onSelected(int position);
	}

	private final String[] items;
	private final Drawable[] images;
	private final boolean[] redPoints;
	private final boolean[] enabled;
	private final int checked;
	private final SelectionListener listener;

	/**
	 * 接收已校验且独立复制的本次菜单数据。
	 *
	 * @param items 项目文字。
	 * @param images 本地图片。
	 * @param redPoints 红点状态。
	 * @param enabled 启用状态。
	 * @param checked 初始勾选索引。
	 * @param listener 本次会话的选择回调。
	 */
	BottomMenuAdapter(String[] items, Drawable[] images, boolean[] redPoints, boolean[] enabled,
		int checked, SelectionListener listener) {
		this.items = items;
		this.images = images;
		this.redPoints = redPoints;
		this.enabled = enabled;
		this.checked = checked;
		this.listener = listener;
	}

	@Override
	public int getCount() {
		return items.length;
	}

	@Override
	public String getItem(int position) {
		return items[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return enabled[position];
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.xui_bottom_sheet_list_item, parent, false);
		}
		ImageView image = convertView.findViewById(R.id.bottom_dialog_list_item_img);
		TextView text = convertView.findViewById(R.id.bottom_dialog_list_item_title);
		View point = convertView.findViewById(R.id.bottom_dialog_list_item_point);
		View mark = convertView.findViewById(R.id.bottom_dialog_list_item_mark_view_stub);
		text.setText(items[position]);
		image.setImageDrawable(images[position]);
		image.setVisibility(images[position] == null ? View.GONE : View.VISIBLE);
		// 上游布局用 wrap_content 覆盖样式尺寸，纯背景红点必须明确大小。
		int pointSize = parent.getResources().getDimensionPixelSize(R.dimen.xui_tips_point_size);
		point.getLayoutParams().width = pointSize;
		point.getLayoutParams().height = pointSize;
		point.setBackgroundResource(R.drawable.xui_tips_point);
		point.setVisibility(redPoints[position] ? View.VISIBLE : View.GONE);
		// ViewStub 首次显示后会替换自身，后续通过布局中的勾选图标定位。
		if (mark == null) {
			mark = convertView.findViewById(R.id.bottom_dialog_list_item_mark);
		}
		if (mark != null) {
			mark.setVisibility(checked == position ? View.VISIBLE : View.GONE);
		}
		convertView.setEnabled(enabled[position]);
		convertView.setAlpha(enabled[position] ? 1.0f : 0.4f);
		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (enabled[position]) {
					listener.onSelected(position);
				}
			}
		});
		ThemeSupport.applyFont(convertView);
		return convertView;
	}
}
