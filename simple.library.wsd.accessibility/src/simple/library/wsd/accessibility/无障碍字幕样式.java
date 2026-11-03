package simple.library.wsd.accessibility;

import android.view.accessibility.CaptioningManager;

import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 系统字幕首选样式快照。颜色使用 Android ARGB 整数，自定义状态与颜色分别报告。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 无障碍字幕样式 {

	/**
	 * 系统字幕的不可变首选样式。
	 */
	private final CaptioningManager.CaptionStyle value;

	/**
	 * 保存不可变字幕样式。
	 *
	 * @param value 系统字幕样式
	 */
	无障碍字幕样式(CaptioningManager.CaptionStyle value) {
		this.value = value;
	}

	/* 属性 */

	/**
	 * 背景色。
	 *
	 * @return 背景色。
	 */
	@SimpleProperty
	public int 背景色() {
		return value.backgroundColor;
	}

	/**
	 * 前景色。
	 *
	 * @return 前景色。
	 */
	@SimpleProperty
	public int 前景色() {
		return value.foregroundColor;
	}

	/**
	 * 窗口颜色。
	 *
	 * @return 窗口颜色。
	 */
	@SimpleProperty
	public int 窗口颜色() {
		return value.windowColor;
	}

	/**
	 * 边缘颜色。
	 *
	 * @return 边缘颜色。
	 */
	@SimpleProperty
	public int 边缘颜色() {
		return value.edgeColor;
	}

	/**
	 * 边缘类型。
	 *
	 * @return 边缘类型。
	 */
	@SimpleProperty
	public int 边缘类型() {
		return value.edgeType;
	}

	/**
	 * 自定义背景色。
	 *
	 * @return 自定义背景色。
	 */
	@SimpleProperty
	public boolean 自定义背景色() {
		return value.hasBackgroundColor();
	}

	/**
	 * 自定义前景色。
	 *
	 * @return 自定义前景色。
	 */
	@SimpleProperty
	public boolean 自定义前景色() {
		return value.hasForegroundColor();
	}

	/**
	 * 自定义窗口颜色。
	 *
	 * @return 自定义窗口颜色。
	 */
	@SimpleProperty
	public boolean 自定义窗口颜色() {
		return value.hasWindowColor();
	}

	/**
	 * 自定义边缘颜色。
	 *
	 * @return 自定义边缘颜色。
	 */
	@SimpleProperty
	public boolean 自定义边缘颜色() {
		return value.hasEdgeColor();
	}

	/**
	 * 自定义边缘。
	 *
	 * @return 自定义边缘。
	 */
	@SimpleProperty
	public boolean 自定义边缘() {
		return value.hasEdgeType();
	}

	/**
	 * 首选字体样式编号，0 普通、1 粗体、2 斜体、3 粗斜体；未指定字体时为 0。
	 *
	 * @return 首选字体样式编号，0 普通、1 粗体、2 斜体、3 粗斜体；未指定字体时为 0。
	 */
	@SimpleProperty
	public int 字体样式() {
		return value.getTypeface() == null ? 0 : value.getTypeface().getStyle();
	}
}
