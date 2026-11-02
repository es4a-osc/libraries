package simple.library.wsd.iconics;

import android.graphics.*;
import android.view.*;
import android.widget.*;
import com.mikepenz.iconics.*;
import com.mikepenz.iconics.typeface.*;
import java.util.*;
import simple.runtime.annotations.*;
import simple.runtime.components.*;

/**
 * 字体图标原生控件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 字体图标文本框Impl extends WidgetBase implements 字体图标文本框 {
	/**
	 * 创建控件。
	 *
	 * @param container 所属容器。
	 */
	public 字体图标文本框Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		Support.init();
		return new com.mikepenz.iconics.view.IconicsTextView(Support.context());
	}
}
