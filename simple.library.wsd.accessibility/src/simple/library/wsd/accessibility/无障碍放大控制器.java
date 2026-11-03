package simple.library.wsd.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Region;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 查询及修改系统显示放大状态。改变事件由无障碍服务组件报告。控制器仅在取得它的服务连接存续期间有效，必须在主线程使用。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 无障碍放大控制器 {

	/**
	 * 取得控制器时的服务连接，用于拒绝旧连接上的操作。
	 */
	private final AccessibilityListener owner;

	/**
	 * 绑定当前连接的系统服务。
	 *
	 * @param owner 已连接的服务
	 */
	无障碍放大控制器(AccessibilityListener owner) {
		this.owner = owner;
	}

	/**
	 * 拒绝断线、错误线程或版本不支持的控制器。
	 *
	 * @return 有效的原生控制器
	 */
	private AccessibilityService.MagnificationController require() {
		AccessibilitySupport.mainThread();
		AccessibilitySupport.api(24, "无障碍放大控制器");
		if (AccessibilityListener.current() != owner) {
			throw new IllegalStateException("控制器所属服务已断开，请重新取得控制器");
		}

		return owner.getMagnificationController();
	}

	/* 属性 */

	/**
	 * 中心X；中心以未缩放屏幕像素表示，未放大时比例为 1。
	 *
	 * @return 中心X；中心以未缩放屏幕像素表示，未放大时比例为 1。
	 */
	@SimpleProperty
	public float 中心X() {
		return require().getCenterX();
	}

	/**
	 * 中心Y；中心以未缩放屏幕像素表示，未放大时比例为 1。
	 *
	 * @return 中心Y；中心以未缩放屏幕像素表示，未放大时比例为 1。
	 */
	@SimpleProperty
	public float 中心Y() {
		return require().getCenterY();
	}

	/**
	 * 比例；中心以未缩放屏幕像素表示，未放大时比例为 1。
	 *
	 * @return 比例；中心以未缩放屏幕像素表示，未放大时比例为 1。
	 */
	@SimpleProperty
	public float 比例() {
		return require().getScale();
	}

	/* 方法 */

	/**
	 * 取得可放大区域的外接矩形，不表示矩形中每一点均可放大。
	 *
	 * @return 屏幕像素矩形
	 */
	@SimpleFunction
	public 无障碍边界 取区域边界() {
		Region region = require().getMagnificationRegion();
		return new 无障碍边界(region.getBounds());
	}

	/**
	 * 检查像素点是否在可放大区域内。
	 *
	 * @param x 屏幕像素横坐标
	 * @param y 屏幕像素纵坐标
	 * @return 点在区域内时为真
	 */
	@SimpleFunction
	public boolean 区域包含(int x, int y) {
		Region region = require().getMagnificationRegion();
		return region.contains(x, y);
	}

	/**
	 * 请求修改系统放大比例，仍受系统允许范围限制。
	 *
	 * @param scale 不小于 1 的有限倍率
	 * @param animate 是否使用过渡动画
	 * @return 系统接受修改时为真
	 */
	@SimpleFunction
	public boolean 设置比例(float scale, boolean animate) {
		AccessibilitySupport.finite(scale, "比例");
		if (scale < 1) {
			throw new IllegalArgumentException("放大比例不能小于 1");
		}

		return require().setScale(scale, animate);
	}

	/**
	 * 请求改变放大中心。
	 *
	 * @param x 未缩放屏幕像素横坐标
	 * @param y 未缩放屏幕像素纵坐标
	 * @param animate 是否使用过渡动画
	 * @return 系统接受修改时为真
	 */
	@SimpleFunction
	public boolean 设置中心(float x, float y, boolean animate) {
		AccessibilitySupport.coordinate(x, y);
		return require().setCenter(x, y, animate);
	}

	/**
	 * 恢复默认放大比例和中心。
	 *
	 * @param animate 是否使用过渡动画
	 * @return 系统接受重置时为真
	 */
	@SimpleFunction
	public boolean 重置(boolean animate) {
		return require().reset(animate);
	}
}
