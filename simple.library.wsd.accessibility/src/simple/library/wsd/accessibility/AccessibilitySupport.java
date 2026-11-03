package simple.library.wsd.accessibility;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import android.accessibilityservice.GestureDescription;
import android.os.Build;
import android.os.Looper;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * 无障碍封装的参数、线程、版本和原生引用所有权边界。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class AccessibilitySupport {

	/**
	 * 工具类不创建实例。
	 */
	private AccessibilitySupport() {
	}

	/**
	 * 将可空的 Android 文本转换为 Simple 文本。
	 *
	 * @param value 原生文本
	 * @return 普通文本，原生值为空时为空文本
	 */
	static String text(CharSequence value) {

		return value == null ? "" : value.toString();
	}

	/**
	 * 拒绝空引用参数。
	 *
	 * @param value 参数值
	 * @param name 显示名称
	 */
	static void notNull(Object value, String name) {
		if (value == null) {
			throw new IllegalArgumentException(name + "不能是空引用");
		}
	}

	/**
	 * 拒绝空的查询名称。
	 *
	 * @param value 参数文本
	 * @param name 显示名称
	 */
	static void notEmpty(String value, String name) {
		notNull(value, name);
		if (value.length() == 0) {
			throw new IllegalArgumentException(name + "不能为空文本");
		}
	}

	/**
	 * 拒绝非有限浮点值。
	 *
	 * @param value 数值
	 * @param name 显示名称
	 */
	static void finite(float value, String name) {
		if (Float.isNaN(value) || Float.isInfinite(value)) {
			throw new IllegalArgumentException(name + "必须是有限数");
		}
	}

	/**
	 * 校验屏幕像素坐标。
	 *
	 * @param x 横坐标
	 * @param y 纵坐标
	 */
	static void coordinate(float x, float y) {
		finite(x, "横坐标");
		finite(y, "纵坐标");
		if (x < 0 || y < 0) {
			throw new IllegalArgumentException("屏幕坐标不能为负数");
		}
	}

	/**
	 * 在直接执行新 API 前检查设备版本。
	 *
	 * @param level 最低系统 API
	 * @param feature 功能名称
	 */
	static void api(int level, String feature) {
		if (Build.VERSION.SDK_INT < level) {
			throw new UnsupportedOperationException(feature + "需要 Android API " + level + " 及以上");
		}
	}

	/**
	 * 确保组件和系统控制器在主线程串行操作。
	 */
	static void mainThread() {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			throw new IllegalStateException("无障碍组件及控制器必须在主线程使用");
		}
	}

	/**
	 * 校验笔画时长并避免整数溢出。
	 *
	 * @param start 非负开始时间，毫秒
	 * @param duration 正数持续时间，毫秒
	 */
	static void duration(long start, long duration) {
		long max = GestureDescription.getMaxGestureDuration();
		if (start < 0 || duration <= 0 || duration > max || start > max - duration) {
			throw new IllegalArgumentException("笔画开始时间和持续时间超出系统允许范围");
		}
	}

	/**
	 * 有界深度优先查询内容描述，失败时释放本次取得的全部节点。
	 *
	 * @param root 不接管的起始节点
	 * @param description 查询文本
	 * @param exact 是否完整匹配
	 * @return 接管全部匹配节点的列表
	 */
	static 无障碍节点列表 findDescription(AccessibilityNodeInfo root, String description, boolean exact) {
		ArrayDeque<AccessibilityNodeInfo> pending = new ArrayDeque<AccessibilityNodeInfo>();
		List<AccessibilityNodeInfo> matches = new ArrayList<AccessibilityNodeInfo>();
		pending.push(AccessibilityNodeInfo.obtain(root));
		int count = 0;
		try {
			while (!pending.isEmpty()) {
				AccessibilityNodeInfo node = pending.pop();
				boolean retained = false;
				try {
					if (++count > 10000) {
						throw new IllegalStateException("描述查询超过一万个节点，请缩小起始节点范围");
					}

					String value = text(node.getContentDescription());
					if (exact ? description.equals(value) : value.contains(description)) {
						matches.add(node);
						retained = true;
					}
					for (int i = node.getChildCount() - 1; i >= 0; i--) {
						AccessibilityNodeInfo child = node.getChild(i);
						if (child != null) {
							pending.push(child);
						}
					}
				} finally {
					if (!retained) {
						node.recycle();
					}
				}
			}

			无障碍节点列表 result = new 无障碍节点列表(matches);
			matches.clear();
			return result;
		} finally {
			for (AccessibilityNodeInfo node : pending) {
				node.recycle();
			}
			for (AccessibilityNodeInfo node : matches) {
				node.recycle();
			}
		}
	}

	/**
	 * 从节点读取标题语义，兼容尚无节点标题属性的旧系统。
	 *
	 * @param node 有效节点
	 * @return 节点或旧集合项声明为标题时为真
	 */
	static boolean heading(AccessibilityNodeInfo node) {
		if (Build.VERSION.SDK_INT >= 28) {
			return node.isHeading();
		}

		return legacyHeading(node.getCollectionItemInfo());
	}

	/**
	 * API 24–27 只能通过集合项取得标题标记。
	 *
	 * @param item 可空集合项
	 * @return 旧系统集合项报告的标题标记
	 */
	@SuppressWarnings("deprecation")
	private static boolean legacyHeading(AccessibilityNodeInfo.CollectionItemInfo item) {
		return item != null && item.isHeading();
	}
}
