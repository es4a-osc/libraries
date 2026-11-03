package simple.library.wsd.quickjs;

/**
 * 库内部回调；返回的拥有引用在交给引擎后自动释放。
 *
 * @author 树先生 xhwsd@qq.com
 */
abstract class ManagedCallback extends QuickJS回调 {
	/**
	 * 创建内部回调。
	 */
	ManagedCallback() {
	}
}
