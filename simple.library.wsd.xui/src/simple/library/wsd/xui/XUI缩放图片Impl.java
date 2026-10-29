package simple.library.wsd.xui;

import android.graphics.Canvas;

import simple.runtime.errors.非法参数错误;

import android.widget.ImageView;

import android.graphics.Bitmap;

import android.graphics.RectF;
import android.view.View;

import com.xuexiang.xui.widget.imageview.photoview.PhotoView;
import com.xuexiang.xui.widget.imageview.photoview.PhotoViewAttacher;

import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * XUI缩放图片的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI缩放图片Impl extends XUI视图组件基类 implements XUI缩放图片 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private PhotoView view;

	/**
	 * 创建XUI缩放图片。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI缩放图片Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new PhotoView(ComponentSupport.createContext());
		view.setOnMatrixChangeListener(new PhotoViewAttacher.OnMatrixChangedListener() {
			@Override
			public void onMatrixChanged(RectF rectangle) {
				显示区域改变(rectangle.left, rectangle.top, rectangle.right, rectangle.bottom);
			}
		});

		view.setOnScaleChangeListener(new PhotoViewAttacher.OnScaleChangeListener() {

			@Override
			public void onScaleChange(float factor, float focusX, float focusY) {
				缩放改变(factor, focusX, focusY);
			}
		});

		view.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {

			@Override
			public void onPhotoTap(View image, float x, float y) {
				图片被单击(x, y);
			}

			@Override
			public void onOutsidePhotoTap() {
				图片外被单击();
			}
		});

		return view;
	}

	@Override
	public void 置旋转到(float rotationDegree) {
		view.setRotationTo(rotationDegree);
	}

	@Override
	public float 取最小缩放() {
		return view.getMinimumScale();
	}

	@Override
	public float 取中间缩放() {
		return view.getMediumScale();
	}

	@Override
	public float 取最大缩放() {
		return view.getMaximumScale();
	}

	@Override
	public float 取缩放() {
		return view.getScale();
	}

	@Override
	public void 置允许边缘拦截(boolean allow) {
		view.setAllowParentInterceptOnEdge(allow);
	}

	@Override
	public void 置最小缩放(float minimumScale) {
		view.setMinimumScale(minimumScale);
	}

	@Override
	public void 置中间缩放(float mediumScale) {
		view.setMediumScale(mediumScale);
	}

	@Override
	public void 置最大缩放(float maximumScale) {
		view.setMaximumScale(maximumScale);
	}

	@Override
	public void 置缩放级别(float minimumScale, float mediumScale, float maximumScale) {
		view.setScaleLevels(minimumScale, mediumScale, maximumScale);
	}

	@Override
	public void 置缩放(float scale) {
		view.setScale(scale);
	}

	@Override
	public void 置缩放(float scale, boolean animate) {
		view.setScale(scale, animate);
	}

	@Override
	public void 置缩放(float scale, float focalX, float focalY, boolean animate) {
		view.setScale(scale, focalX, focalY, animate);
	}

	@Override
	public void 置可缩放(boolean zoomable) {
		view.setZoomable(zoomable);
	}

	@Override
	public void 置缩放过渡时长(int milliseconds) {
		view.setZoomTransitionDuration(milliseconds);
	}

	@Override
	public void 置图片(String image) {
		view.setImageDrawable(ComponentSupport.getDrawable(image));
	}

	@Override
	public void 缩放改变(float factor, float focusX, float focusY) {
		EventDispatcher.dispatchEvent(this, "缩放改变", factor, focusX, focusY);
	}

	@Override
	public void 图片被单击(float x, float y) {
		EventDispatcher.dispatchEvent(this, "图片被单击", x, y);
	}

	@Override
	public void 图片外被单击() {
		EventDispatcher.dispatchEvent(this, "图片外被单击");
	}

	@Override
	public void 重置缩放() {
		// PhotoView.resetMatrix 会先 cleanup 并清空视图引用；只重置控制器矩阵才能继续交互。
		((PhotoViewAttacher) view.getIPhotoViewImplementation()).resetMatrix();
	}

	@Override
	public float[] 取显示区域() {
		RectF rect = view.getDisplayRect();
		return rect == null ? new float[0] : new float[] {rect.left, rect.top, rect.right, rect.bottom};
	}

	@Override
	public void 置图片缩放模式(int mode) {
		if (mode < 1 || mode >= ImageView.ScaleType.values().length) {
			throw new 非法参数错误();
		}
		view.setScaleType(ImageView.ScaleType.values()[mode]);
	}

	@Override
	public void 显示区域改变(float left, float top, float right, float bottom) {
		EventDispatcher.dispatchEvent(this, "显示区域改变", left, top, right, bottom);
	}

	@Override
	public void 保存可见图片(String path) {
		if (view.getWidth() <= 0 || view.getHeight() <= 0 || view.getDrawable() == null) {
			throw new ComponentStateException("请先设置图片并等待视图完成布局。");
		}
		Bitmap visible = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
		view.draw(new Canvas(visible));
		try {
			ComponentSupport.saveImage(visible, path);
		} finally {
			visible.recycle();
		}
	}

}
