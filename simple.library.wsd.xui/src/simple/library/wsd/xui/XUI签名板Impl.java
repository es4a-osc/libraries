package simple.library.wsd.xui;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import com.xuexiang.xui.widget.imageview.SignatureView;

import simple.runtime.components.组件容器;

/**
 * XUI签名板的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI签名板Impl extends XUI视图组件基类 implements XUI签名板 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private SignatureView view;

	/**
	 * 创建XUI签名板。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI签名板Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new SignatureView(ComponentSupport.createContext()) {

			/**
			 * 保持一次笔画的触摸归属，结束后恢复父容器的手势拦截。
			 */
			@Override
			public boolean onTouchEvent(MotionEvent event) {
				ViewParent parent = getParent();
				int action = event.getActionMasked();

				// 从落笔开始保留整段手势，避免滚动容器在纵向移动时抢走笔画。
				if (parent != null && action == MotionEvent.ACTION_DOWN) {
					parent.requestDisallowInterceptTouchEvent(true);
				}

				try {
					return super.onTouchEvent(event);
				} finally {
					// 抬笔或系统取消均释放拦截限制，板外的后续手势仍可滚动页面。
					if (parent != null && (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)) {
						parent.requestDisallowInterceptTouchEvent(false);
					}
				}
			}
		};

		return view;
	}

	@Override
	public void 清空() {
		view.clear();
	}

	@Override
	public void 置画笔尺寸(float penSize) {
		view.setPenSize(penSize);
	}

	@Override
	public void 启用边框(boolean hasBorder) {
		view.enableBorder(hasBorder);
	}

	@Override
	public void 置画板背景颜色(int backColor) {
		view.setBackColor(backColor);
	}

	@Override
	public void 置画笔颜色(int penColor) {
		view.setPenColor(penColor);
	}

	@Override
	public void 置视图尺寸(int width, int height) {
		view.setViewSize(width, height);
	}

	@Override
	public void 保存签名(String path) {
		ComponentSupport.saveImage(view.getSnapshot(), path);
	}

	@Override
	public boolean 是否已绘制() {
		return view.getTouched();
	}
}
