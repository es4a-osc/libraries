package com.jayway.jsonpath.internal;

/**
 * 固定适配 JsonPath 2.10.0 的节点定位引用，避免反射及重新解析未转义的路径文本。
 *
 * <p>
 * 只读取上游定位引用的父容器和访问键，不修改上游类或全局配置。
 * </p>
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class ES4APathAccess {
	private static final Class<?> ROOT_TYPE = PathRef.createRoot(new Object()).getClass();
	private static final Class<?> MULTI_TYPE = PathRef.create(new Object(), java.util.Collections.<String>emptyList())
			.getClass();

	/**
	 * 内部桥接不创建实例。
	 */
	private ES4APathAccess() {
	}

	/**
	 * 读取已经定位的父容器。
	 *
	 * @param reference 上游节点引用。
	 * @return 父对象或父数组。
	 */
	public static Object parent(PathRef reference) {
		return reference.parent;
	}

	/**
	 * 读取属性名、属性名集合或数组索引。
	 *
	 * @param reference 上游节点引用。
	 * @return 节点访问键。
	 */
	public static Object accessor(PathRef reference) {
		return reference.getAccessor();
	}

	/**
	 * 区分真正的根引用和名称恰为美元符号的对象属性。
	 *
	 * @param reference 上游节点引用。
	 * @return 当前引用表示整个根值时为真。
	 */
	public static boolean isRoot(PathRef reference) {
		return reference.getClass() == ROOT_TYPE;
	}

	/**
	 * 识别多属性引用；其上游访问键是拼接文本，不能按分隔符反向拆解。
	 *
	 * @param reference 上游节点引用。
	 * @return 一次选择多个对象属性时为真。
	 */
	public static boolean isMultiProperty(PathRef reference) {
		return reference.getClass() == MULTI_TYPE;
	}
}
