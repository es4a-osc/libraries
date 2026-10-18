package simple.library.wsd.gif;

/**
 * 维护播放时间、用户暂停和不可见暂停，所有状态由界面线程串行访问。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class PlaybackClock {

	private int duration;
	private int position;
	private long anchor;
	private boolean animated;
	private boolean playing;
	private boolean visible;
	private boolean loop = true;
	private boolean ended;

	/* 载入与播放控制 */

	/**
	 * 载入图像的时间信息，重置位置、锚点和播放结束标记。
	 *
	 * <p>保留现有可见状态和循环设置；只有动态图且启用自动播放时才记录播放意图。</p>
	 *
	 * @param duration 动画周期，单位毫秒；小于 1 时按 1 毫秒处理。
	 * @param animated 是否包含多个图像帧。
	 * @param autoPlay 是否在载入后自动播放。
	 * @param now 同一单调时钟基准下的当前毫秒数，调用方使用 SystemClock.uptimeMillis()。
	 */
	void load(int duration, boolean animated, boolean autoPlay, long now) {
		this.duration = Math.max(1, duration);
		this.animated = animated;
		position = 0;
		anchor = now;
		playing = animated && autoPlay;
		ended = false;
	}

	/**
	 * 清除图像的播放状态，恢复为时长 1 毫秒的静态占位状态。
	 *
	 * <p>位置回到开头并清除结束标记，保留现有可见状态和循环设置。</p>
	 *
	 * @param now 同一单调时钟基准下的当前毫秒数。
	 */
	void clear(long now) {
		load(1, false, false, now);
	}

	/**
	 * 从开头重新播放动态图，并清除播放结束标记。
	 *
	 * <p>静态图保持停播；不可见时只记录播放意图，重新可见后才推进时间。</p>
	 *
	 * @param now 同一单调时钟基准下的当前毫秒数。
	 */
	void start(long now) {
		position = 0;
		anchor = now;
		playing = animated;
		ended = false;
	}

	/**
	 * 先结算到当前时刻，再暂停播放并保留当前位置。
	 *
	 * <p>若本次结算已到达单次播放末尾，保留已结束状态。</p>
	 *
	 * @param now 同一单调时钟基准下的当前毫秒数。
	 */
	void pause(long now) {
		advance(now);
		playing = false;
	}

	/**
	 * 从当前位置继续播放动态图；单次播放已结束时改为从开头播放。
	 *
	 * <p>重新设置时间锚点，不补计暂停期间的时长；静态图保持停播。</p>
	 *
	 * @param now 同一单调时钟基准下的当前毫秒数。
	 */
	void resume(long now) {
		if (ended) {
			position = 0;
			ended = false;
		}
		anchor = now;
		playing = animated;
	}

	/**
	 * 停止播放并回到开头，清除结束标记。
	 *
	 * <p>保留图像的时长、动态图标记、循环设置和可见状态。</p>
	 *
	 * @param now 同一单调时钟基准下的当前毫秒数。
	 */
	void stop(long now) {
		playing = false;
		position = 0;
		anchor = now;
		ended = false;
	}

	/* 可见状态与循环设置 */

	/**
	 * 按原可见状态结算时间，再切换可见状态并重置锚点。
	 *
	 * <p>不可见期间不累积播放时间；切换可见状态不会撤销用户主动暂停。</p>
	 *
	 * @param visible 当前是否允许图像可见。
	 * @param now 同一单调时钟基准下的当前毫秒数。
	 * @return 本次结算使非循环播放首次到达末尾时返回真，否则返回假。
	 */
	boolean setVisible(boolean visible, long now) {
		boolean finished = advance(now);
		this.visible = visible;
		anchor = now;
		return finished;
	}

	/**
	 * 设置循环方式，在下一次时间推进时生效。
	 *
	 * <p>不重置位置，也不自动重新启动已经结束的单次播放。</p>
	 *
	 * @param loop 真表示循环播放，假表示播放至末尾后停止。
	 */
	void setLoop(boolean loop) {
		this.loop = loop;
	}

	/* 时间查询与推进 */

	/**
	 * 查询播放意图和可见状态是否同时允许时间推进。
	 *
	 * @return 处于播放状态且可见时返回真，否则返回假。
	 */
	boolean isAdvancing() {
		return playing && visible;
	}

	/**
	 * 读取最近一次结算得到的播放位置，不推进时钟。
	 *
	 * @return 从 0 开始的毫秒位置；非循环播放结束时为周期时长减 1。
	 */
	int position() {
		return position;
	}

	/**
	 * 结算距上次锚点的非负时间间隔，并更新播放位置。
	 *
	 * <p>无论当前是否允许推进，都更新锚点，避免补计暂停或不可见期间的时长。
	 * 循环播放按周期取余；非循环播放到达末尾后停止，后续调用不重复报告结束。</p>
	 *
	 * @param now 同一单调时钟基准下的当前毫秒数；早于锚点时按零间隔结算。
	 * @return 本次使非循环播放首次到达末尾时返回真，否则返回假。
	 */
	boolean advance(long now) {
		long elapsed = Math.max(0L, now - anchor);
		anchor = now;

		if (!isAdvancing()) {
			return false;
		}

		long next = position + elapsed;
		if (loop) {
			position = (int) (next % duration);
		} else if (next >= duration) {
			position = duration - 1;
			playing = false;
			ended = true;
			return true;
		} else {
			position = (int) next;
		}

		return false;
	}
}
