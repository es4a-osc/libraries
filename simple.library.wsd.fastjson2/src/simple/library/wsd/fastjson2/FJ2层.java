package simple.library.wsd.fastjson2;

/**
 * 顺序读写共享的单层结构状态，不向 Simple 导出。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class FJ2层 {

	/**
	 * 容器类型：1 为对象，2 为数组。
	 */
	final int kind;

	/**
	 * 定长数组的元素数量；对象或未知长度数组为 -1。
	 */
	final int expected;

	/**
	 * 已经处理的直接子值数量。
	 */
	int count;

	/**
	 * 对象名称已处理，正在等待对应的值。
	 */
	boolean pending;

	/**
	 * 当前对象等待写入的属性名，用于维护引用路径。
	 */
	String name;

	/**
	 * 手动开始的写入容器身份；读取器不使用。
	 */
	Object token;

	/**
	 * 文本读取时，最近一个子值之后是否包含分隔逗号。
	 */
	boolean comma;

	/**
	 * 创建对象或数组的结构状态。
	 *
	 * @param kind     对象为 1，数组为 2。
	 * @param expected 定长数组长度；未知长度或对象为 -1。
	 */
	FJ2层(int kind, int expected) {
		this.kind = kind;
		this.expected = expected;
	}
}
