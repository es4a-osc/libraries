package simple.library.wsd.xui;

import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.xuexiang.xui.widget.imageview.crop.CropImageType;
import com.xuexiang.xui.widget.imageview.crop.CropImageView;

import simple.runtime.components.组件容器;

/**
 * XUI图片裁剪的原生视图及 Simple 事件适配。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class XUI图片裁剪Impl extends XUI视图组件基类 implements XUI图片裁剪 {

	// 视图在父类构造期间创建；不得用字段初始化器重置此处状态。
	private CropImageView view;

	/**
	 * 创建XUI图片裁剪。
	 *
	 * @param container 容纳组件的 Simple 容器。
	 */
	public XUI图片裁剪Impl(组件容器 container) {
		super(container);
	}

	@Override
	protected View createView() {
		view = new CropImageView(ComponentSupport.createContext());

		return view;
	}

	@Override
	public void 置图片路径(String imagePath) {
		view.setImagePath(imagePath);
	}

	@Override
	public void 置裁剪覆盖层视图可见性(int visibility) {
		view.setCropOverlayViewVisibility(visibility);
	}

	@Override
	public void 置固定宽高比例(boolean fixAspectRatio) {
		view.setFixedAspectRatio(fixAspectRatio);
	}

	@Override
	public void 置辅助线(int guidelines) {
		view.setGuidelines(guidelines);
	}

	@Override
	public void 置宽高比例(int aspectRatioX, int aspectRatioY) {
		view.setAspectRatio(aspectRatioX, aspectRatioY);
	}

	@Override
	public void 保存裁剪(String path) {
		ComponentSupport.saveImage(view.getCroppedImage(), path);
	}

	@Override
	public void 置图片(String image) {
		Drawable drawable = ComponentSupport.getDrawable(image);
		view.setImageBitmap(ComponentSupport.toBitmap(drawable));
	}

	@Override
	public void 旋转图片(int degrees) {
		view.rotateImage(degrees);
	}

	@Override
	public void 镜像图片(boolean horizontal) {
		view.reverseImage(horizontal ? CropImageType.REVERSE_TYPE.LEFT_RIGHT : CropImageType.REVERSE_TYPE.UP_DOWN);
	}

	@Override
	public float[] 取裁剪区域() {
		if (view.getWidth() == 0 || view.getHeight() == 0 || view.getImageView().getDrawable() == null) {
			throw new ComponentStateException("图片尚未完成布局。");
		}
		RectF rect = view.getActualCropRect();
		return new float[] {rect.left, rect.top, rect.right, rect.bottom};
	}

}
