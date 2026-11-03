package simple.library.wsd.accessibility;

import java.util.List;

import android.view.accessibility.AccessibilityRecord;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 无障碍事件中的只读记录。独立取出的记录使用后释放；从记录取得的节点也须单独释放。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public class 无障碍记录 {

	/**
	 * 本包装拥有的记录或事件副本，释放后为空。
	 */
	private AccessibilityRecord value;

	/**
	 * 接管记录的独立副本。
	 *
	 * @param value 非空原生记录
	 */
	无障碍记录(AccessibilityRecord value) {
		this.value = value;
	}

	/**
	 * 检查记录是否仍有效。
	 *
	 * @return 有效的原生记录
	 */
	final AccessibilityRecord require() {
		if (value == null) {
			throw new IllegalStateException("事件或记录已释放；保留回调事件请先复制事件");
		}

		return value;
	}

	/* 属性 */

	/**
	 * 类名。
	 *
	 * @return 类名。
	 */
	@SimpleProperty
	public String 类名() {
		return AccessibilitySupport.text(require().getClassName());
	}

	/**
	 * 内容描述。
	 *
	 * @return 内容描述。
	 */
	@SimpleProperty
	public String 内容描述() {
		return AccessibilitySupport.text(require().getContentDescription());
	}

	/**
	 * 更改前文本。
	 *
	 * @return 更改前文本。
	 */
	@SimpleProperty
	public String 更改前文本() {
		return AccessibilitySupport.text(require().getBeforeText());
	}

	/**
	 * 窗口编号；索引、数量和滚动值保留系统原始值，未提供时可能为负数。
	 *
	 * @return 窗口编号；索引、数量和滚动值保留系统原始值，未提供时可能为负数。
	 */
	@SimpleProperty
	public int 窗口编号() {
		return require().getWindowId();
	}

	/**
	 * 增加字符数；索引、数量和滚动值保留系统原始值，未提供时可能为负数。
	 *
	 * @return 增加字符数；索引、数量和滚动值保留系统原始值，未提供时可能为负数。
	 */
	@SimpleProperty
	public int 增加字符数() {
		return require().getAddedCount();
	}

	/**
	 * 减少字符数；索引、数量和滚动值保留系统原始值，未提供时可能为负数。
	 *
	 * @return 减少字符数；索引、数量和滚动值保留系统原始值，未提供时可能为负数。
	 */
	@SimpleProperty
	public int 减少字符数() {
		return require().getRemovedCount();
	}

	/**
	 * 项目数；索引、数量和滚动值保留系统原始值，未提供时可能为负数。
	 *
	 * @return 项目数；索引、数量和滚动值保留系统原始值，未提供时可能为负数。
	 */
	@SimpleProperty
	public int 项目数() {
		return require().getItemCount();
	}

	/**
	 * 当前索引；索引、数量和滚动值保留系统原始值，未提供时可能为负数。
	 *
	 * @return 当前索引；索引、数量和滚动值保留系统原始值，未提供时可能为负数。
	 */
	@SimpleProperty
	public int 当前索引() {
		return require().getCurrentItemIndex();
	}

	/**
	 * 首可见索引；索引、数量和滚动值保留系统原始值，未提供时可能为负数。
	 *
	 * @return 首可见索引；索引、数量和滚动值保留系统原始值，未提供时可能为负数。
	 */
	@SimpleProperty
	public int 首可见索引() {
		return require().getFromIndex();
	}

	/**
	 * 尾可见索引；索引、数量和滚动值保留系统原始值，未提供时可能为负数。
	 *
	 * @return 尾可见索引；索引、数量和滚动值保留系统原始值，未提供时可能为负数。
	 */
	@SimpleProperty
	public int 尾可见索引() {
		return require().getToIndex();
	}

	/**
	 * 水平滚动量；索引、数量和滚动值保留系统原始值，未提供时可能为负数。
	 *
	 * @return 水平滚动量；索引、数量和滚动值保留系统原始值，未提供时可能为负数。
	 */
	@SimpleProperty
	public int 水平滚动量() {
		return require().getScrollX();
	}

	/**
	 * 垂直滚动量；索引、数量和滚动值保留系统原始值，未提供时可能为负数。
	 *
	 * @return 垂直滚动量；索引、数量和滚动值保留系统原始值，未提供时可能为负数。
	 */
	@SimpleProperty
	public int 垂直滚动量() {
		return require().getScrollY();
	}

	/**
	 * 最大水平滚动量；索引、数量和滚动值保留系统原始值，未提供时可能为负数。
	 *
	 * @return 最大水平滚动量；索引、数量和滚动值保留系统原始值，未提供时可能为负数。
	 */
	@SimpleProperty
	public int 最大水平滚动量() {
		return require().getMaxScrollX();
	}

	/**
	 * 最大垂直滚动量；索引、数量和滚动值保留系统原始值，未提供时可能为负数。
	 *
	 * @return 最大垂直滚动量；索引、数量和滚动值保留系统原始值，未提供时可能为负数。
	 */
	@SimpleProperty
	public int 最大垂直滚动量() {
		return require().getMaxScrollY();
	}

	/**
	 * 已启用。
	 *
	 * @return 已启用。
	 */
	@SimpleProperty
	public boolean 已启用() {
		return require().isEnabled();
	}

	/**
	 * 已勾选。
	 *
	 * @return 已勾选。
	 */
	@SimpleProperty
	public boolean 已勾选() {
		return require().isChecked();
	}

	/**
	 * 全屏。
	 *
	 * @return 全屏。
	 */
	@SimpleProperty
	public boolean 全屏() {
		return require().isFullScreen();
	}

	/**
	 * 密码字段。
	 *
	 * @return 密码字段。
	 */
	@SimpleProperty
	public boolean 密码字段() {
		return require().isPassword();
	}

	/**
	 * 可滚动。
	 *
	 * @return 可滚动。
	 */
	@SimpleProperty
	public boolean 可滚动() {
		return require().isScrollable();
	}

	/* 方法 */

	/**
	 * 取得记录中的文本数组快照。
	 *
	 * @return 文本数组；无文本时为空数组
	 */
	@SimpleFunction
	public String[] 取文本() {
		List<CharSequence> texts = require().getText();
		String[] result = new String[texts.size()];
		for (int i = 0; i < texts.size(); i++) {
			result[i] = AccessibilitySupport.text(texts.get(i));
		}

		return result;
	}

	/**
	 * 取得触发记录的节点，使用后释放。受监听包、系统授权和目标窗口变化影响。
	 *
	 * @return 源节点；无法取得时为空值
	 */
	@SimpleFunction
	public 无障碍节点 取源节点() {
		return 无障碍节点.wrap(require().getSource());
	}

	/**
	 * 取得独立记录副本，使用后释放。
	 *
	 * @return 独立记录
	 */
	@SimpleFunction
	public 无障碍记录 复制记录() {
		return new 无障碍记录(AccessibilityRecord.obtain(require()));
	}

	/**
	 * 释放记录或事件，可重复调用。事件派发者在回调结束后也会自动释放回调事件。
	 */
	@SimpleFunction
	public void 释放() {
		if (value != null) {
			value.recycle();
			value = null;
		}
	}
}
