package simple.library.wsd.iconics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import simple.runtime.android.MainActivity;
import simple.runtime.components.可视组件;
import simple.runtime.components.组件容器;
import simple.runtime.events.EventDispatcher;

/**
 * 图标组合动画及生命周期。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class 图标动画Impl extends simple.runtime.components.impl.组件Impl
		implements 图标动画, MainActivity.OnDestroyListener, MainActivity.OnStopListener, MainActivity.OnResumeListener,
		View.OnAttachStateChangeListener {

	private final Map<Integer, android.animation.ValueAnimator> animations = new LinkedHashMap<Integer, android.animation.ValueAnimator>();
	private final Map<Integer, 图标动画配置> configurations = new LinkedHashMap<Integer, 图标动画配置>();
	private int nextId;
	private int boundPosition;
	private boolean hostStopped;
	private View target;
	private MotionDrawable drawable;
	private boolean destroyed;
	private int generation;
	private final Set<android.animation.ValueAnimator> autoPaused = new HashSet<android.animation.ValueAnimator>();

	/**
	 * 创建并监听宿主生命周期。
	 *
	 * @param container 所属窗口。
	 */
	public 图标动画Impl(组件容器 container) {
		super(container);
		MainActivity m = MainActivity.getContext();
		m.addOnDestroyListener(this);
		m.addOnStopListener(this);
		m.addOnResumeListener(this);
	}

	/**
	 * 拒绝销毁后调用。
	 */
	private void check() {
		Support.main();
		if (destroyed) {
			throw new 图标错误("动画组件已销毁");
		}
	}

	@Override
	public void 绑定(可视组件 component, 图标图像 image, int position) {
		check();
		if (image == null || position < -2 || position > 5) {
			throw new 图标错误("绑定参数无效");
		}
		image.check();
		View v = Support.view(component);
		if (position == -1 && !(v instanceof ImageView) || position >= 0 && !(v instanceof TextView)) {
			throw new 图标错误("目标不支持此图标位置");
		}
		清空();
		if (target != null) {
			target.removeOnAttachStateChangeListener(this);
		}
		Support.prepareShadow(v, image.value);
		target = v;
		boundPosition = position;
		if (target instanceof TextView) {
			target.setTag(R.id.es4a_iconics_slots, null);
		}
		target.addOnAttachStateChangeListener(this);
		drawable = new MotionDrawable(Support.copy(image.value));
		if (position == -2) {
			target.setBackground(drawable);
		} else if (position == -1) {
			((ImageView) target).setImageDrawable(drawable);
		} else {
			TextView t = (TextView) target;
			android.graphics.drawable.Drawable[] a = position >= 4 ? t.getCompoundDrawablesRelative()
					: t.getCompoundDrawables();
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			a[position == 4 ? 0 : position == 5 ? 2 : position] = drawable;
			if (position >= 4) {
				t.setCompoundDrawablesRelative(a[0], a[1], a[2], a[3]);
			} else {
				t.setCompoundDrawables(a[0], a[1], a[2], a[3]);
			}
		}
	}

	@Override
	public int 添加(图标动画配置 c) {
		check();
		if (drawable == null || c == null) {
			throw new 图标错误("请先绑定图标并提供配置");
		}
		if (c.类型() < 0 || c.类型() > 2 || c.时长() <= 0 || c.延时() < 0 || c.重复次数() < -1 || c.插值() < 0 || c.插值() > 4
				|| !Float.isFinite(c.起值()) || !Float.isFinite(c.止值())
				|| c.类型() == 1 && (c.起值() < 0 || c.止值() < 0 || c.起值() > 255 || c.止值() > 255)
				|| c.类型() == 2 && (c.起值() < 0 || c.止值() < 0)) {
			throw new 图标错误("动画参数越界");
		}
		if (nextId == Integer.MAX_VALUE) {
			throw new 图标错误("动画编号已耗尽，请新建组件");
		}
		final int index = nextId++;
		final int type = c.类型();
		final MotionDrawable bound = drawable;
		bound.types.put(index, type);
		bound.values.put(index, c.起值());
		android.animation.ValueAnimator a = android.animation.ValueAnimator.ofFloat(c.起值(), c.止值());
		a.setDuration(c.时长());
		a.setStartDelay(c.延时());
		a.setRepeatCount(c.重复次数());
		a.setRepeatMode(c.往返() ? android.animation.ValueAnimator.REVERSE : android.animation.ValueAnimator.RESTART);
		android.animation.TimeInterpolator[] interpolators = { new android.view.animation.LinearInterpolator(),
				new android.view.animation.AccelerateInterpolator(),
				new android.view.animation.DecelerateInterpolator(),
				new android.view.animation.AccelerateDecelerateInterpolator(),
				new android.view.animation.BounceInterpolator() };
		a.setInterpolator(interpolators[c.插值()]);
		a.addUpdateListener(new android.animation.ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(android.animation.ValueAnimator animation) {
				if (!ownsTarget()) {
					autoPaused.remove(animation);
					animation.cancel();
					return;
				}
				bound.values.put(index, (Float) animation.getAnimatedValue());
				bound.invalidateSelf();
			}
		});
		a.addListener(new android.animation.AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(android.animation.Animator a) {
				动画开始(index);
			}

			@Override
			public void onAnimationEnd(android.animation.Animator a) {
				动画结束(index);
			}

			@Override
			public void onAnimationRepeat(android.animation.Animator a) {
				动画重复(index);
			}

			@Override
			public void onAnimationCancel(android.animation.Animator a) {
				动画取消(index);
			}
		});
		a.addPauseListener(new android.animation.AnimatorListenerAdapter() {
			@Override
			public void onAnimationPause(android.animation.Animator a) {
				动画暂停(index);
			}

			@Override
			public void onAnimationResume(android.animation.Animator a) {
				动画继续(index);
			}
		});
		animations.put(index, a);
		configurations.put(index, c.复制());
		return index;
	}

	@Override
	public void 开始() {
		check();
		visible();
		autoPaused.clear();
		for (android.animation.ValueAnimator a : new ArrayList<android.animation.ValueAnimator>(animations.values())) {
			a.start();
		}
	}

	@Override
	public void 暂停() {
		check();
		autoPaused.clear();
		for (android.animation.ValueAnimator a : animations.values()) {
			a.pause();
		}
	}

	@Override
	public void 继续() {
		check();
		visible();
		autoPaused.clear();
		for (android.animation.ValueAnimator a : animations.values()) {
			a.resume();
		}
	}

	@Override
	public void 取消() {
		check();
		autoPaused.clear();
		for (android.animation.ValueAnimator a : new ArrayList<android.animation.ValueAnimator>(animations.values())) {
			a.cancel();
		}
	}

	@Override
	public void 结束() {
		check();
		autoPaused.clear();
		for (android.animation.ValueAnimator a : new ArrayList<android.animation.ValueAnimator>(animations.values())) {
			a.end();
		}
	}

	@Override
	public void 反向() {
		check();
		visible();
		for (android.animation.ValueAnimator a : animations.values()) {
			a.reverse();
		}
	}

	@Override
	public boolean 正在运行() {
		check();
		for (android.animation.ValueAnimator a : animations.values()) {
			if (a.isRunning() && !a.isPaused()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void 清空() {
		check();
		generation++;
		取消();
		for (android.animation.ValueAnimator a : animations.values()) {
			a.removeAllListeners();
			a.removeAllUpdateListeners();
		}
		animations.clear();
		configurations.clear();
		generation++;
		if (drawable != null) {
			drawable.values.clear();
			drawable.types.clear();
			drawable.invalidateSelf();
		}
	}

	@Override
	public void onStop() {
		hostStopped = true;
		pauseAutomatic();
	}

	/**
	 * 暂停当前运行项，记住需要自动恢复的集合。
	 */
	private void pauseAutomatic() {
		for (android.animation.ValueAnimator a : animations.values()) {
			if (a.isStarted() && !a.isPaused()) {
				autoPaused.add(a);
				a.pause();
			}
		}
	}

	@Override
	public void onResume() {
		hostStopped = false;
		resumeAutomatic();
	}

	/**
	 * 只恢复此前自动暂停且仍绑定的项。
	 */
	private void resumeAutomatic() {
		if (!hostStopped && ownsTarget() && target.isAttachedToWindow() && target.isShown()) {
			for (android.animation.ValueAnimator a : autoPaused) {
				a.resume();
			}
			autoPaused.clear();
		}
	}

	@Override
	public void onViewDetachedFromWindow(View v) {
		pauseAutomatic();
	}

	@Override
	public void onViewAttachedToWindow(View v) {
		resumeAutomatic();
	}

	@Override
	public void onDestroy() {
		销毁();
	}

	@Override
	public void 销毁() {
		if (destroyed) {
			return;
		}
		destroyed = true;
		for (android.animation.ValueAnimator a : animations.values()) {
			a.removeAllListeners();
			a.removeAllUpdateListeners();
			a.cancel();
		}
		animations.clear();
		configurations.clear();
		autoPaused.clear();
		if (target != null) {
			target.removeOnAttachStateChangeListener(this);
		}
		target = null;
		drawable = null;
		MainActivity m = MainActivity.getContext();
		m.removeOnStopListener(this);
		m.removeOnResumeListener(this);
		m.removeOnDestroyListener(this);
		super.销毁();
	}

	/**
	 * 合成旋转、缩放和透明度；浮点插值保留透明度下限。
	 */
	private static final class MotionDrawable extends android.graphics.drawable.Drawable {
		private final IconicsDrawable source;
		final Map<Integer, Integer> types = new LinkedHashMap<Integer, Integer>();
		final Map<Integer, Float> values = new LinkedHashMap<Integer, Float>();

		/**
		 * 包装独立图标。
		 *
		 * @param source 图标快照。
		 */
		MotionDrawable(IconicsDrawable source) {
			this.source = source;
		}

		@Override
		public void draw(Canvas canvas) {
			int saved = canvas.save();
			android.graphics.Rect b = getBounds();
			float alpha = 1f;
			for (int i : types.keySet()) {
				float v = values.get(i);
				switch (types.get(i)) {
				case 0:
					canvas.rotate(v, b.exactCenterX(), b.exactCenterY());
					break;
				case 1:
					alpha *= v / 255f;
					break;
				case 2:
					canvas.scale(v, v, b.exactCenterX(), b.exactCenterY());
					break;
				default:
					break;
				}
			}
			int old = source.getAlpha();
			source.setAlpha(Math.round(old * alpha));
			source.setBounds(b);
			source.draw(canvas);
			source.setAlpha(old);
			canvas.restoreToCount(saved);
		}

		@Override
		public android.graphics.drawable.Drawable getCurrent() {
			return source;
		}

		@Override
		public boolean onLayoutDirectionChanged(int direction) {
			return source.setLayoutDirection(direction);
		}

		@Override
		public int getIntrinsicWidth() {
			return source.getIntrinsicWidth();
		}

		@Override
		public int getIntrinsicHeight() {
			return source.getIntrinsicHeight();
		}

		@Override
		public void setAlpha(int a) {
			source.setAlpha(a);
			invalidateSelf();
		}

		@Override
		public void setColorFilter(ColorFilter c) {
			source.setColorFilter(c);
			invalidateSelf();
		}

		@Override
		@SuppressWarnings("deprecation")
		public int getOpacity() {
			// Drawable 的抽象契约仍要求提供透明度类型。
			return android.graphics.PixelFormat.TRANSLUCENT;
		}

		@Override
		public boolean isStateful() {
			return source.isStateful();
		}

		@Override
		protected boolean onStateChange(int[] s) {
			boolean changed = source.setState(s);
			if (changed) {
				invalidateSelf();
			}
			return changed;
		}
	}

	@Override
	public void 动画开始(int index) {
		if (!destroyed) {
			emit("动画开始", index);
		}
	}

	@Override
	public void 动画结束(int index) {
		if (!destroyed) {
			emit("动画结束", index);
		}
	}

	@Override
	public void 动画重复(int index) {
		if (!destroyed) {
			emit("动画重复", index);
		}
	}

	@Override
	public void 动画取消(int index) {
		if (!destroyed) {
			emit("动画取消", index);
		}
	}

	@Override
	public void 动画暂停(int index) {
		if (!destroyed) {
			emit("动画暂停", index);
		}
	}

	@Override
	public void 动画继续(int index) {
		if (!destroyed) {
			emit("动画继续", index);
		}
	}

	/**
	 * 在当前动画操作完成后通知；重绑、清空和销毁丢弃旧通知。
	 *
	 * @param name  事件名。
	 * @param index 动画序号。
	 */
	private void emit(final String name, final int index) {
		final int expected = generation;
		new android.os.Handler(android.os.Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				if (!destroyed && generation == expected && animations.containsKey(index)) {
					EventDispatcher.dispatchEvent(图标动画Impl.this, name, index);
				}
			}
		});
	}

	/**
	 * 目标上的图像被其他操作替换时不再继续消耗动画资源。
	 *
	 * @return 当前仍持有目标位置。
	 */
	private boolean ownsTarget() {
		if (target == null || drawable == null) {
			return false;
		}
		if (boundPosition == -2) {
			return target.getBackground() == drawable;
		}
		if (boundPosition == -1) {
			return ((ImageView) target).getDrawable() == drawable;
		}
		android.graphics.drawable.Drawable[] a = boundPosition >= 4 ? ((TextView) target).getCompoundDrawablesRelative()
				: ((TextView) target).getCompoundDrawables();
		return a[boundPosition == 4 ? 0 : boundPosition == 5 ? 2 : boundPosition] == drawable;
	}

	/**
	 * 要求目标可见并且绑定仍有效。
	 */
	private void visible() {
		if (hostStopped || !ownsTarget() || !target.isAttachedToWindow() || !target.isShown()) {
			throw new 图标错误("目标不可见或图像已替换，请重新绑定");
		}
	}

	/**
	 * 校验稳定动画编号。
	 *
	 * @param id 动画编号。
	 * @return 当前动画。
	 */
	private android.animation.ValueAnimator item(int id) {
		check();
		android.animation.ValueAnimator a = animations.get(id);
		if (a == null) {
			throw new 图标错误("动画不存在或已移除：" + id);
		}
		return a;
	}

	@Override
	public void 开始单项(int id) {
		android.animation.ValueAnimator a = item(id);
		visible();
		autoPaused.remove(a);
		a.start();
	}

	@Override
	public void 暂停单项(int id) {
		android.animation.ValueAnimator a = item(id);
		autoPaused.remove(a);
		a.pause();
	}

	@Override
	public void 继续单项(int id) {
		android.animation.ValueAnimator a = item(id);
		visible();
		autoPaused.remove(a);
		a.resume();
	}

	@Override
	public void 取消单项(int id) {
		android.animation.ValueAnimator a = item(id);
		autoPaused.remove(a);
		a.cancel();
	}

	@Override
	public void 结束单项(int id) {
		android.animation.ValueAnimator a = item(id);
		autoPaused.remove(a);
		a.end();
	}

	@Override
	public void 反向单项(int id) {
		android.animation.ValueAnimator a = item(id);
		visible();
		autoPaused.remove(a);
		a.reverse();
	}

	@Override
	public void 移除(int id) {
		android.animation.ValueAnimator a = item(id);
		autoPaused.remove(a);
		a.removeAllListeners();
		a.removeAllUpdateListeners();
		a.cancel();
		animations.remove(id);
		configurations.remove(id);
		drawable.types.remove(id);
		drawable.values.remove(id);
		drawable.invalidateSelf();
	}

	@Override
	public boolean 单项正在运行(int id) {
		android.animation.ValueAnimator a = item(id);
		return a.isRunning() && !a.isPaused();
	}

	@Override
	public boolean 单项已开始(int id) {
		return item(id).isStarted();
	}

	@Override
	public boolean 单项已暂停(int id) {
		return item(id).isPaused();
	}

	@Override
	public float 取单项当前值(int id) {
		android.animation.ValueAnimator a = item(id);
		Object v = a.getAnimatedValue();
		return v == null ? configurations.get(id).起值() : ((Number) v).floatValue();
	}

	@Override
	public float 取单项进度(int id) {
		return item(id).getAnimatedFraction();
	}

	@Override
	public void 设置单项进度(int id, float fraction) {
		android.animation.ValueAnimator a = item(id);
		if (!Float.isFinite(fraction) || fraction < 0 || fraction > 1) {
			throw new 图标错误("进度须为0至1");
		}
		a.setCurrentFraction(fraction);
	}

	@Override
	public long 取单项播放时间(int id) {
		return item(id).getCurrentPlayTime();
	}

	@Override
	public void 设置单项播放时间(int id, long time) {
		android.animation.ValueAnimator a = item(id);
		if (time < 0) {
			throw new 图标错误("播放时间不能为负");
		}
		a.setCurrentPlayTime(time);
	}

	@Override
	public 图标动画配置 取得单项配置(int id) {
		item(id);
		return configurations.get(id).复制();
	}

	@Override
	public int[] 取动画编号() {
		check();
		int[] out = new int[animations.size()];
		int i = 0;
		for (int id : animations.keySet()) {
			out[i++] = id;
		}
		return out;
	}

	@Override
	public int 取动画数量() {
		check();
		return animations.size();
	}

	@Override
	public void 解除绑定() {
		check();
		清空();
		if (target != null) {
			target.removeOnAttachStateChangeListener(this);
		}
		target = null;
		drawable = null;
	}

	@Override
	public boolean 是否已绑定() {
		check();
		return ownsTarget();
	}

	@Override
	public byte[] 取当前帧PNG() {
		check();
		if (drawable == null) {
			throw new 图标错误("请先绑定图像");
		}
		int width = drawable.getIntrinsicWidth(), height = drawable.getIntrinsicHeight();
		if (width <= 0 || height <= 0 || width > 4096 || height > 4096) {
			throw new 图标错误("导出尺寸须为1至4096像素");
		}
		android.graphics.Rect old = new android.graphics.Rect(drawable.getBounds());
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		try {
			drawable.setBounds(0, 0, width, height);
			drawable.draw(new Canvas(bitmap));
			java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			return out.toByteArray();
		} finally {
			drawable.setBounds(old);
			bitmap.recycle();
		}
	}
}
