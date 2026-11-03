package simple.library.wsd.accessibility;

import java.util.List;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 无障碍服务取得的节点视图。界面变化后可能失效，刷新失败应重新查询。仅可串行使用，完成后释放；修改界面使用动作，不直接改写节点元数据。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 无障碍节点 {

	/**
	 * 本包装拥有的原生节点，释放后为空。
	 */
	private AccessibilityNodeInfo value;

	/**
	 * 接管一个非空原生节点。
	 *
	 * @param value 原生节点的所有权
	 */
	无障碍节点(AccessibilityNodeInfo value) {
		this.value = value;
	}

	/**
	 * 接管可空原生节点。
	 *
	 * @param value 查询所得节点
	 * @return 节点包装，原生节点为空时返回空值
	 */
	static 无障碍节点 wrap(AccessibilityNodeInfo value) {

		return value == null ? null : new 无障碍节点(value);
	}

	/**
	 * 检查节点的显式生命周期。
	 *
	 * @return 尚未释放的原生节点
	 */
	AccessibilityNodeInfo require() {
		if (value == null) {
			throw new IllegalStateException("节点已释放");
		}

		return value;
	}

	/* 属性 */

	/**
	 * 子节点数；系统未报告时保留 Android 原始值。
	 *
	 * @return 子节点数；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public int 子节点数() {
		return require().getChildCount();
	}

	/**
	 * 包名。文本不存在时为空文本。
	 *
	 * @return 包名。文本不存在时为空文本。
	 */
	@SimpleProperty
	public String 包名() {
		return AccessibilitySupport.text(require().getPackageName());
	}

	/**
	 * 类名。文本不存在时为空文本。
	 *
	 * @return 类名。文本不存在时为空文本。
	 */
	@SimpleProperty
	public String 类名() {
		return AccessibilitySupport.text(require().getClassName());
	}

	/**
	 * 文本。文本不存在时为空文本。
	 *
	 * @return 文本。文本不存在时为空文本。
	 */
	@SimpleProperty
	public String 文本() {
		return AccessibilitySupport.text(require().getText());
	}

	/**
	 * 内容描述。文本不存在时为空文本。
	 *
	 * @return 内容描述。文本不存在时为空文本。
	 */
	@SimpleProperty
	public String 内容描述() {
		return AccessibilitySupport.text(require().getContentDescription());
	}

	/**
	 * 资源ID。文本不存在时为空文本。
	 *
	 * @return 资源ID。文本不存在时为空文本。
	 */
	@SimpleProperty
	public String 资源ID() {
		return AccessibilitySupport.text(require().getViewIdResourceName());
	}

	/**
	 * 错误文本。文本不存在时为空文本。
	 *
	 * @return 错误文本。文本不存在时为空文本。
	 */
	@SimpleProperty
	public String 错误文本() {
		return AccessibilitySupport.text(require().getError());
	}

	/**
	 * 可点击；系统未报告时保留 Android 原始值。
	 *
	 * @return 可点击；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public boolean 可点击() {
		return require().isClickable();
	}

	/**
	 * 可长按；系统未报告时保留 Android 原始值。
	 *
	 * @return 可长按；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public boolean 可长按() {
		return require().isLongClickable();
	}

	/**
	 * 可滚动；系统未报告时保留 Android 原始值。
	 *
	 * @return 可滚动；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public boolean 可滚动() {
		return require().isScrollable();
	}

	/**
	 * 可编辑；系统未报告时保留 Android 原始值。
	 *
	 * @return 可编辑；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public boolean 可编辑() {
		return require().isEditable();
	}

	/**
	 * 已启用；系统未报告时保留 Android 原始值。
	 *
	 * @return 已启用；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public boolean 已启用() {
		return require().isEnabled();
	}

	/**
	 * 可见；系统未报告时保留 Android 原始值。
	 *
	 * @return 可见；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public boolean 可见() {
		return require().isVisibleToUser();
	}

	/**
	 * 可获得焦点；系统未报告时保留 Android 原始值。
	 *
	 * @return 可获得焦点；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public boolean 可获得焦点() {
		return require().isFocusable();
	}

	/**
	 * 已获得输入焦点；系统未报告时保留 Android 原始值。
	 *
	 * @return 已获得输入焦点；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public boolean 已获得输入焦点() {
		return require().isFocused();
	}

	/**
	 * 已获得无障碍焦点；系统未报告时保留 Android 原始值。
	 *
	 * @return 已获得无障碍焦点；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public boolean 已获得无障碍焦点() {
		return require().isAccessibilityFocused();
	}

	/**
	 * 可勾选；系统未报告时保留 Android 原始值。
	 *
	 * @return 可勾选；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public boolean 可勾选() {
		return require().isCheckable();
	}

	/**
	 * 已勾选；系统未报告时保留 Android 原始值。
	 *
	 * @return 已勾选；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public boolean 已勾选() {
		return require().isChecked();
	}

	/**
	 * 已选择；系统未报告时保留 Android 原始值。
	 *
	 * @return 已选择；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public boolean 已选择() {
		return require().isSelected();
	}

	/**
	 * 密码字段；系统未报告时保留 Android 原始值。
	 *
	 * @return 密码字段；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public boolean 密码字段() {
		return require().isPassword();
	}

	/**
	 * 多行；系统未报告时保留 Android 原始值。
	 *
	 * @return 多行；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public boolean 多行() {
		return require().isMultiLine();
	}

	/**
	 * 可弹出窗口；系统未报告时保留 Android 原始值。
	 *
	 * @return 可弹出窗口；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public boolean 可弹出窗口() {
		return require().canOpenPopup();
	}

	/**
	 * 可关闭；系统未报告时保留 Android 原始值。
	 *
	 * @return 可关闭；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public boolean 可关闭() {
		return require().isDismissable();
	}

	/**
	 * 上下文可点击；系统未报告时保留 Android 原始值。
	 *
	 * @return 上下文可点击；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public boolean 上下文可点击() {
		return require().isContextClickable();
	}

	/**
	 * 内容无效；系统未报告时保留 Android 原始值。
	 *
	 * @return 内容无效；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public boolean 内容无效() {
		return require().isContentInvalid();
	}

	/**
	 * 重要视图；系统未报告时保留 Android 原始值。
	 *
	 * @return 重要视图；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public boolean 重要视图() {
		return require().isImportantForAccessibility();
	}

	/**
	 * 最大文本长度；系统未报告时保留 Android 原始值。
	 *
	 * @return 最大文本长度；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public int 最大文本长度() {
		return require().getMaxTextLength();
	}

	/**
	 * 选择区起点；系统未报告时保留 Android 原始值。
	 *
	 * @return 选择区起点；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public int 选择区起点() {
		return require().getTextSelectionStart();
	}

	/**
	 * 选择区终点；系统未报告时保留 Android 原始值。
	 *
	 * @return 选择区终点；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public int 选择区终点() {
		return require().getTextSelectionEnd();
	}

	/**
	 * 移动粒度；系统未报告时保留 Android 原始值。
	 *
	 * @return 移动粒度；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public int 移动粒度() {
		return require().getMovementGranularities();
	}

	/**
	 * 输入类型；系统未报告时保留 Android 原始值。
	 *
	 * @return 输入类型；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public int 输入类型() {
		return require().getInputType();
	}

	/**
	 * 绘制顺序；系统未报告时保留 Android 原始值。
	 *
	 * @return 绘制顺序；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public int 绘制顺序() {
		return require().getDrawingOrder();
	}

	/**
	 * 窗口编号；系统未报告时保留 Android 原始值。
	 *
	 * @return 窗口编号；系统未报告时保留 Android 原始值。
	 */
	@SimpleProperty
	public int 窗口编号() {
		return require().getWindowId();
	}

	/**
	 * 提示文本，需要 Android API 26 及以上。
	 *
	 * @return 提示文本，需要 Android API 26 及以上。
	 */
	@SimpleProperty
	public String 提示文本() {
		AccessibilitySupport.api(26, "提示文本");
		return AccessibilitySupport.text(require().getHintText());
	}

	/**
	 * 显示提示文本，需要 Android API 26 及以上。
	 *
	 * @return 显示提示文本，需要 Android API 26 及以上。
	 */
	@SimpleProperty
	public boolean 显示提示文本() {
		AccessibilitySupport.api(26, "显示提示文本");
		return require().isShowingHintText();
	}

	/**
	 * 工具提示，需要 Android API 28 及以上。
	 *
	 * @return 工具提示，需要 Android API 28 及以上。
	 */
	@SimpleProperty
	public String 工具提示() {
		AccessibilitySupport.api(28, "工具提示");
		return AccessibilitySupport.text(require().getTooltipText());
	}

	/**
	 * 窗格标题，需要 Android API 28 及以上。
	 *
	 * @return 窗格标题，需要 Android API 28 及以上。
	 */
	@SimpleProperty
	public String 窗格标题() {
		AccessibilitySupport.api(28, "窗格标题");
		return AccessibilitySupport.text(require().getPaneTitle());
	}

	/**
	 * 节点是否声明为标题；旧系统依据集合项的标题信息判断。
	 *
	 * @return 节点是否声明为标题；旧系统依据集合项的标题信息判断。
	 */
	@SimpleProperty
	public boolean 标题节点() {
		return AccessibilitySupport.heading(require());
	}

	/**
	 * 屏幕阅读器焦点，需要 Android API 28 及以上。
	 *
	 * @return 屏幕阅读器焦点，需要 Android API 28 及以上。
	 */
	@SimpleProperty
	public boolean 屏幕阅读器焦点() {
		AccessibilitySupport.api(28, "屏幕阅读器焦点");
		return require().isScreenReaderFocusable();
	}

	/**
	 * 状态描述，需要 Android API 30 及以上。
	 *
	 * @return 状态描述，需要 Android API 30 及以上。
	 */
	@SimpleProperty
	public String 状态描述() {
		AccessibilitySupport.api(30, "状态描述");
		return AccessibilitySupport.text(require().getStateDescription());
	}

	/* 方法 */

	/**
	 * 取得对应节点的独立引用，使用后释放。
	 *
	 * @return 对应节点；不存在时为空值
	 */
	@SimpleFunction
	public 无障碍节点 取父节点() {
		return wrap(require().getParent());
	}

	/**
	 * 取得对应节点的独立引用，使用后释放。
	 *
	 * @return 对应节点；不存在时为空值
	 */
	@SimpleFunction
	public 无障碍节点 取标签目标() {
		return wrap(require().getLabelFor());
	}

	/**
	 * 取得对应节点的独立引用，使用后释放。
	 *
	 * @return 对应节点；不存在时为空值
	 */
	@SimpleFunction
	public 无障碍节点 取标签节点() {
		return wrap(require().getLabeledBy());
	}

	/**
	 * 取得对应节点的独立引用，使用后释放。
	 *
	 * @return 对应节点；不存在时为空值
	 */
	@SimpleFunction
	public 无障碍节点 取前序节点() {
		return wrap(require().getTraversalBefore());
	}

	/**
	 * 取得对应节点的独立引用，使用后释放。
	 *
	 * @return 对应节点；不存在时为空值
	 */
	@SimpleFunction
	public 无障碍节点 取后序节点() {
		return wrap(require().getTraversalAfter());
	}

	/**
	 * 取得直接子节点，使用后释放。
	 *
	 * @param index 从零开始的直接子节点索引
	 * @return 对应子节点；系统无法提供时为空值
	 * @throws IndexOutOfBoundsException 索引无效
	 */
	@SimpleFunction
	public 无障碍节点 取子节点(int index) {
		AccessibilityNodeInfo node = require();
		if (index < 0 || index >= node.getChildCount()) {
			throw new IndexOutOfBoundsException("子节点索引越界：" + index);
		}

		return wrap(node.getChild(index));
	}

	/**
	 * 取得节点的像素矩形快照。
	 *
	 * @return 屏幕或父容器坐标系中的矩形
	 */
	@SimpleFunction
	public 无障碍边界 取屏幕边界() {

		Rect bounds = new Rect();
		require().getBoundsInScreen(bounds);
		return new 无障碍边界(bounds);
	}

	/**
	 * 取得父视图内的像素边界；无障碍父节点可能不是实际父视图，结果也受系统放大影响，定位屏幕应使用取屏幕边界。
	 *
	 * @return 系统报告的父视图坐标矩形
	 */
	@SimpleFunction
	@SuppressWarnings("deprecation")
	public 无障碍边界 取父内边界() {

		Rect bounds = new Rect();
		// 此接口保留父视图坐标语义；屏幕边界不能等价替代，API 30 内无等价新接口。
		require().getBoundsInParent(bounds);
		return new 无障碍边界(bounds);
	}

	/**
	 * 取得节点关联的窗口。使用后释放窗口。
	 *
	 * @return 关联信息；系统未提供时为空值
	 */
	@SimpleFunction
	public 无障碍窗口 取窗口() {
		android.view.accessibility.AccessibilityWindowInfo result = require().getWindow();
		return result == null ? null : new 无障碍窗口(result);
	}

	/**
	 * 取得节点关联的集合信息。
	 *
	 * @return 关联信息；系统未提供时为空值
	 */
	@SimpleFunction
	public 无障碍集合信息 取集合信息() {
		AccessibilityNodeInfo.CollectionInfo result = require().getCollectionInfo();
		return result == null ? null : new 无障碍集合信息(result);
	}

	/**
	 * 取得节点关联的集合项。
	 *
	 * @return 关联信息；系统未提供时为空值
	 */
	@SimpleFunction
	public 无障碍集合项 取集合项() {
		AccessibilityNodeInfo.CollectionItemInfo result = require().getCollectionItemInfo();
		return result == null ? null : new 无障碍集合项(result, AccessibilitySupport.heading(require()));
	}

	/**
	 * 取得节点关联的范围。
	 *
	 * @return 关联信息；系统未提供时为空值
	 */
	@SimpleFunction
	public 无障碍范围 取范围() {
		AccessibilityNodeInfo.RangeInfo result = require().getRangeInfo();
		return result == null ? null : new 无障碍范围(result);
	}

	/**
	 * 在当前节点及其后代中查询，结果列表使用后释放。
	 *
	 * @param text 非空查询文本；资源 ID 使用 包名:id/名称
	 * @return 匹配节点列表；无匹配时数量为零
	 */
	@SimpleFunction
	public 无障碍节点列表 按文本查找(String text) {
		AccessibilitySupport.notEmpty(text, "查询内容");
		return new 无障碍节点列表(require().findAccessibilityNodeInfosByText(text));
	}

	/**
	 * 在当前节点及其后代中查询，结果列表使用后释放。
	 *
	 * @param viewId 非空查询文本；资源 ID 使用 包名:id/名称
	 * @return 匹配节点列表；无匹配时数量为零
	 */
	@SimpleFunction
	public 无障碍节点列表 按资源ID查找(String viewId) {
		AccessibilitySupport.notEmpty(viewId, "查询内容");
		return new 无障碍节点列表(require().findAccessibilityNodeInfosByViewId(viewId));
	}

	/**
	 * 在当前节点及其后代中按内容描述查找。遍历上限为一万个节点，超限抛出异常，结果列表使用后释放。
	 *
	 * @param description 非空内容描述
	 * @param exact 真为完整匹配，假为包含匹配
	 * @return 匹配节点列表
	 */
	@SimpleFunction
	public 无障碍节点列表 按描述查找(String description, boolean exact) {
		AccessibilitySupport.notEmpty(description, "内容描述");
		return AccessibilitySupport.findDescription(require(), description, exact);
	}

	/**
	 * 查找焦点节点，使用后释放。
	 *
	 * @param direction 焦点类型或焦点方向，见无障碍常量
	 * @return 匹配节点；没有时为空值
	 */
	@SimpleFunction
	public 无障碍节点 查找焦点(int direction) {
		return wrap(require().findFocus(direction));
	}

	/**
	 * 查找焦点节点，使用后释放。
	 *
	 * @param direction 焦点类型或焦点方向，见无障碍常量
	 * @return 匹配节点；没有时为空值
	 */
	@SimpleFunction
	public 无障碍节点 沿方向查找焦点(int direction) {
		return wrap(require().focusSearch(direction));
	}

	/**
	 * 取得节点当前声明支持的动作描述数组。
	 *
	 * @return 动作描述数组
	 */
	@SimpleFunction
	public 无障碍动作[] 取动作列表() {
		List<AccessibilityNodeInfo.AccessibilityAction> actions = require().getActionList();
		无障碍动作[] result = new 无障碍动作[actions.size()];
		for (int i = 0; i < actions.size(); i++) {
			result[i] = new 无障碍动作(actions.get(i));
		}

		return result;
	}

	/**
	 * 向节点发送无参数动作；返回值表示目标接受动作，不保证界面已完成更新。
	 *
	 * @param action 节点动作常量或取动作列表所得编号
	 * @return 目标报告接受时为真，否则为假
	 */
	@SimpleFunction
	public boolean 执行动作(int action) {
		return require().performAction(action);
	}

	/**
	 * 向节点发送带参数动作。
	 *
	 * @param action 节点动作编号
	 * @param arguments 对应动作的参数包
	 * @return 目标报告接受时为真，否则为假
	 */
	@SimpleFunction
	public boolean 带参执行动作(int action, 无障碍动作参数 arguments) {
		AccessibilitySupport.notNull(arguments, "动作参数");
		return require().performAction(action, new Bundle(arguments.values));
	}

	/**
	 * 请求节点单击。
	 *
	 * @return 目标报告接受时为真，否则为假
	 */
	@SimpleFunction
	public boolean 单击() {
		return require().performAction(AccessibilityNodeInfo.ACTION_CLICK);
	}

	/**
	 * 请求节点长按。
	 *
	 * @return 目标报告接受时为真，否则为假
	 */
	@SimpleFunction
	public boolean 长按() {
		return require().performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
	}

	/**
	 * 请求节点向前滚动。
	 *
	 * @return 目标报告接受时为真，否则为假
	 */
	@SimpleFunction
	public boolean 向前滚动() {
		return require().performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
	}

	/**
	 * 请求节点向后滚动。
	 *
	 * @return 目标报告接受时为真，否则为假
	 */
	@SimpleFunction
	public boolean 向后滚动() {
		return require().performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
	}

	/**
	 * 请求节点复制。
	 *
	 * @return 目标报告接受时为真，否则为假
	 */
	@SimpleFunction
	public boolean 复制() {
		return require().performAction(AccessibilityNodeInfo.ACTION_COPY);
	}

	/**
	 * 请求节点粘贴。
	 *
	 * @return 目标报告接受时为真，否则为假
	 */
	@SimpleFunction
	public boolean 粘贴() {
		return require().performAction(AccessibilityNodeInfo.ACTION_PASTE);
	}

	/**
	 * 请求节点剪切。
	 *
	 * @return 目标报告接受时为真，否则为假
	 */
	@SimpleFunction
	public boolean 剪切() {
		return require().performAction(AccessibilityNodeInfo.ACTION_CUT);
	}

	/**
	 * 请求节点选择。
	 *
	 * @return 目标报告接受时为真，否则为假
	 */
	@SimpleFunction
	public boolean 选择() {
		return require().performAction(AccessibilityNodeInfo.ACTION_SELECT);
	}

	/**
	 * 请求节点取消选择。
	 *
	 * @return 目标报告接受时为真，否则为假
	 */
	@SimpleFunction
	public boolean 取消选择() {
		return require().performAction(AccessibilityNodeInfo.ACTION_CLEAR_SELECTION);
	}

	/**
	 * 请求节点获取输入焦点。
	 *
	 * @return 目标报告接受时为真，否则为假
	 */
	@SimpleFunction
	public boolean 获取输入焦点() {
		return require().performAction(AccessibilityNodeInfo.ACTION_FOCUS);
	}

	/**
	 * 请求节点清除输入焦点。
	 *
	 * @return 目标报告接受时为真，否则为假
	 */
	@SimpleFunction
	public boolean 清除输入焦点() {
		return require().performAction(AccessibilityNodeInfo.ACTION_CLEAR_FOCUS);
	}

	/**
	 * 请求节点获取无障碍焦点。
	 *
	 * @return 目标报告接受时为真，否则为假
	 */
	@SimpleFunction
	public boolean 获取无障碍焦点() {
		return require().performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
	}

	/**
	 * 请求节点清除无障碍焦点。
	 *
	 * @return 目标报告接受时为真，否则为假
	 */
	@SimpleFunction
	public boolean 清除无障碍焦点() {
		return require().performAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
	}

	/**
	 * 请求节点展开。
	 *
	 * @return 目标报告接受时为真，否则为假
	 */
	@SimpleFunction
	public boolean 展开() {
		return require().performAction(AccessibilityNodeInfo.ACTION_EXPAND);
	}

	/**
	 * 请求节点折叠。
	 *
	 * @return 目标报告接受时为真，否则为假
	 */
	@SimpleFunction
	public boolean 折叠() {
		return require().performAction(AccessibilityNodeInfo.ACTION_COLLAPSE);
	}

	/**
	 * 请求节点关闭。
	 *
	 * @return 目标报告接受时为真，否则为假
	 */
	@SimpleFunction
	public boolean 关闭() {
		return require().performAction(AccessibilityNodeInfo.ACTION_DISMISS);
	}

	/**
	 * 使用设置文本动作替换真实控件内容，空文本表示请求清空。
	 *
	 * @param text 替换后的文本，不为空引用
	 * @return 目标接受设置文本动作时为真
	 */
	@SimpleFunction
	public boolean 设置文本(String text) {
		AccessibilitySupport.notNull(text, "文本");
		Bundle arguments = new Bundle();
		arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
		return require().performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
	}

	/**
	 * 从界面刷新当前节点快照。失败后应释放并重新查询。
	 *
	 * @return 仍能连接到原节点并完成刷新时为真
	 */
	@SimpleFunction
	public boolean 刷新() {
		return require().refresh();
	}

	/**
	 * 复制当前节点引用，两个包装对象独立释放，不复制真实界面。
	 *
	 * @return 独立节点包装
	 */
	@SimpleFunction
	public 无障碍节点 复制节点() {
		return new 无障碍节点(AccessibilityNodeInfo.obtain(require()));
	}

	/**
	 * 释放原生节点，可重复调用；随后除释放外的操作均拒绝。
	 */
	@SimpleFunction
	public void 释放() {
		if (value != null) {
			value.recycle();
			value = null;
		}
	}
}
