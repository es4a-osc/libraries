package simple.library.wsd.achartengine;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * ACE仪表样式，应用时复制配置，不向调用方暴露绘制器。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class ACE仪表样式 {

	/**
	 * 表盘最小值，必须小于最大值。
	 */
	double min = 0;

	/**
	 * 表盘最大值。
	 */
	double max = 100;

	/**
	 * 最小值对应角度。
	 */
	double angleMin = 330;

	/**
	 * 最大值对应角度，不能等于起始角度。
	 */
	double angleMax = 30;

	/**
	 * 小刻度正间距；总刻度数不得超过 2000。
	 */
	double minor = 5;

	/**
	 * 大刻度正间距；总刻度数不得超过 2000。
	 */
	double major = 20;

	/**
	 * 创建默认配置。
	 */
	public ACE仪表样式() {
	}

	/**
	 * 表盘最小值，必须小于最大值。
	 *
	 * @return 表盘最小值，必须小于最大值。
	 */
	@SimpleProperty
	public double 最小值() {
		return min;
	}

	/**
	 * 表盘最小值，必须小于最大值。
	 *
	 * @param value 表盘最小值，必须小于最大值。
	 */
	@SimpleProperty
	public void 最小值(double value) {
		Checks.range(value, -1e+100, 1e+100);
		min = value;
	}

	/**
	 * 表盘最大值。
	 *
	 * @return 表盘最大值。
	 */
	@SimpleProperty
	public double 最大值() {
		return max;
	}

	/**
	 * 表盘最大值。
	 *
	 * @param value 表盘最大值。
	 */
	@SimpleProperty
	public void 最大值(double value) {
		Checks.range(value, -1e+100, 1e+100);
		max = value;
	}

	/**
	 * 最小值对应角度。
	 *
	 * @return 最小值对应角度。
	 */
	@SimpleProperty
	public double 起始角度() {
		return angleMin;
	}

	/**
	 * 最小值对应角度。
	 *
	 * @param value 最小值对应角度。
	 */
	@SimpleProperty
	public void 起始角度(double value) {
		Checks.range(value, -3600, 3600);
		angleMin = value;
	}

	/**
	 * 最大值对应角度，不能等于起始角度。
	 *
	 * @return 最大值对应角度，不能等于起始角度。
	 */
	@SimpleProperty
	public double 结束角度() {
		return angleMax;
	}

	/**
	 * 最大值对应角度，不能等于起始角度。
	 *
	 * @param value 最大值对应角度，不能等于起始角度。
	 */
	@SimpleProperty
	public void 结束角度(double value) {
		Checks.range(value, -3600, 3600);
		angleMax = value;
	}

	/**
	 * 小刻度正间距；总刻度数不得超过 2000。
	 *
	 * @return 小刻度正间距；总刻度数不得超过 2000。
	 */
	@SimpleProperty
	public double 小刻度间距() {
		return minor;
	}

	/**
	 * 小刻度正间距；总刻度数不得超过 2000。
	 *
	 * @param value 小刻度正间距；总刻度数不得超过 2000。
	 */
	@SimpleProperty
	public void 小刻度间距(double value) {
		Checks.range(value, 1e-100, 1e+100);
		minor = value;
	}

	/**
	 * 大刻度正间距；总刻度数不得超过 2000。
	 *
	 * @return 大刻度正间距；总刻度数不得超过 2000。
	 */
	@SimpleProperty
	public double 大刻度间距() {
		return major;
	}

	/**
	 * 大刻度正间距；总刻度数不得超过 2000。
	 *
	 * @param value 大刻度正间距；总刻度数不得超过 2000。
	 */
	@SimpleProperty
	public void 大刻度间距(double value) {
		Checks.range(value, 1e-100, 1e+100);
		major = value;
	}

	/**
	 * 返回独立的配置副本。
	 *
	 * @return 返回独立的配置副本。
	 */
	@SimpleFunction
	public ACE仪表样式 复制() {
		ACE仪表样式 result = new ACE仪表样式();
		result.min = min;
		result.max = max;
		result.angleMin = angleMin;
		result.angleMax = angleMax;
		result.minor = minor;
		result.major = major;
		return result;
	}
}
