package simple.library.wsd.xui;

import androidx.viewpager.widget.ViewPager;

import com.xuexiang.xui.widget.banner.anim.BaseAnimator;
import com.xuexiang.xui.widget.banner.anim.select.RotateEnter;
import com.xuexiang.xui.widget.banner.anim.select.ZoomInEnter;
import com.xuexiang.xui.widget.banner.transform.DepthTransformer;
import com.xuexiang.xui.widget.banner.transform.FadeSlideTransformer;
import com.xuexiang.xui.widget.banner.transform.FlowTransformer;
import com.xuexiang.xui.widget.banner.transform.RotateDownTransformer;
import com.xuexiang.xui.widget.banner.transform.RotateUpTransformer;
import com.xuexiang.xui.widget.banner.transform.ZoomOutSlideTransformer;
import com.xuexiang.xui.widget.banner.widget.banner.base.BaseBanner;

import simple.runtime.errors.非法参数错误;

/**
 * 在轮播效果常量与 XUI 页面变换器之间建立确定映射。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class BannerEffects {

	/**
	 * 静态工具不创建实例。
	 */
	private BannerEffects() {
	}

	/**
	 * 把效果立即应用于已有页面，也记录到后续数据刷新使用的上游构建状态。
	 *
	 * @param banner 原生轮播。
	 * @param effect 轮播效果常量，值与上游变换器类名的前缀一致。
	 */
	static void apply(BaseBanner<?, ?> banner, String effect) {
		if (effect == null) {
			throw new 非法参数错误();
		}

		Class<? extends ViewPager.PageTransformer> type;
		ViewPager.PageTransformer transformer;
		switch (effect) {
			case XUI视图组件.轮播效果_深度:
				type = DepthTransformer.class;
				transformer = new DepthTransformer();
				break;
			case XUI视图组件.轮播效果_淡入滑动:
				type = FadeSlideTransformer.class;
				transformer = new FadeSlideTransformer();
				break;
			case XUI视图组件.轮播效果_流动:
				type = FlowTransformer.class;
				transformer = new FlowTransformer();
				break;
			case XUI视图组件.轮播效果_向下旋转:
				type = RotateDownTransformer.class;
				transformer = new RotateDownTransformer();
				break;
			case XUI视图组件.轮播效果_向上旋转:
				type = RotateUpTransformer.class;
				transformer = new RotateUpTransformer();
				break;
			case XUI视图组件.轮播效果_缩小滑动:
				type = ZoomOutSlideTransformer.class;
				transformer = new ZoomOutSlideTransformer();
				break;
			case XUI视图组件.轮播效果_无:
				type = null;
				transformer = null;
				break;
			default:
				throw new 非法参数错误();
		}
		banner.setTransformerClass(type);
		banner.getViewPager().setPageTransformer(true, transformer);
	}

	/**
	 * 将稳定常量映射为上游自带的指示器动画，不导出 Class 或动画对象。
	 *
	 * @param effect 动画常量。
	 * @return 原生动画类，空表示关闭动画。
	 */
	static Class<? extends BaseAnimator> indicatorClass(int effect) {
		switch (effect) {
		case 0:
			return null;
		case 1:
			return ZoomInEnter.class;
		case 2:
			return RotateEnter.class;
		default:
			throw new 非法参数错误();
		}
	}

}
