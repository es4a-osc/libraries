package simple.library.wsd.iconics;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 图标动画参数。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 图标动画配置 {
	private int p类型 = 0;

	/**
	 * 0旋转，1透明，2缩放。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public int 类型() {
		return p类型;
	}

	/**
	 * 0旋转，1透明，2缩放。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 类型(int value) {
		p类型 = value;
	}

	private long p时长 = 1000L;

	/**
	 * 单次动画时长，毫秒，大于零。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public long 时长() {
		return p时长;
	}

	/**
	 * 单次动画时长，毫秒，大于零。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 时长(long value) {
		p时长 = value;
	}

	private long p延时 = 0L;

	/**
	 * 开始延迟，毫秒。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public long 延时() {
		return p延时;
	}

	/**
	 * 开始延迟，毫秒。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 延时(long value) {
		p延时 = value;
	}

	private int p重复次数 = -1;

	/**
	 * 重复次数，-1为无限。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public int 重复次数() {
		return p重复次数;
	}

	/**
	 * 重复次数，-1为无限。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 重复次数(int value) {
		p重复次数 = value;
	}

	private boolean p往返 = false;

	/**
	 * 是否往返重复。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public boolean 往返() {
		return p往返;
	}

	/**
	 * 是否往返重复。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 往返(boolean value) {
		p往返 = value;
	}

	private int p插值 = 0;

	/**
	 * 0线性，1加速，2减速，3先加速后减速，4弹跳。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public int 插值() {
		return p插值;
	}

	/**
	 * 0线性，1加速，2减速，3先加速后减速，4弹跳。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 插值(int value) {
		p插值 = value;
	}

	private float p起值 = 0f;

	/**
	 * 旋转单位为度；透明度为0至255；缩放为非负比例。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public float 起值() {
		return p起值;
	}

	/**
	 * 旋转单位为度；透明度为0至255；缩放为非负比例。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 起值(float value) {
		p起值 = value;
	}

	private float p止值 = 360f;

	/**
	 * 结束值；透明闪烁宜设255，缩放宜设1。
	 *
	 * @return 当前值。
	 */
	@SimpleProperty
	public float 止值() {
		return p止值;
	}

	/**
	 * 结束值；透明闪烁宜设255，缩放宜设1。
	 *
	 * @param value 新值。
	 */
	@SimpleProperty
	public void 止值(float value) {
		p止值 = value;
	}

	/**
	 * 创建独立实例。
	 */
	public 图标动画配置() {
	}

	/**
	 * 复制动画配置，后续修改互不影响。
	 *
	 * @return 复制动画配置，后续修改互不影响。
	 */
	@SimpleFunction
	public 图标动画配置 复制() {
		图标动画配置 out = new 图标动画配置();
		out.p类型 = p类型;
		out.p时长 = p时长;
		out.p延时 = p延时;
		out.p重复次数 = p重复次数;
		out.p往返 = p往返;
		out.p插值 = p插值;
		out.p起值 = p起值;
		out.p止值 = p止值;
		return out;
	}
}
