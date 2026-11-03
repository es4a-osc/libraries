package simple.library.wsd.accessibility;

import simple.runtime.annotations.SimpleDataElement;
import simple.runtime.annotations.SimpleObject;

/**
 * 无障碍事件、节点动作、全局动作和配置的常量。常量存在不表示当前设备支持该能力；API 条件见各项说明。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 无障碍常量 {

	/**
	 * 常量类不创建实例。
	 */
	private 无障碍常量() {
	}

	/* 全局动作 */

	/**
	 * 返回。
	 */
	@SimpleDataElement
	public static final int 全局_返回 = 1;

	/**
	 * 桌面。
	 */
	@SimpleDataElement
	public static final int 全局_桌面 = 2;

	/**
	 * 最近任务。
	 */
	@SimpleDataElement
	public static final int 全局_最近任务 = 3;

	/**
	 * 通知栏。
	 */
	@SimpleDataElement
	public static final int 全局_通知栏 = 4;

	/**
	 * 快捷设置。
	 */
	@SimpleDataElement
	public static final int 全局_快捷设置 = 5;

	/**
	 * 电源菜单。
	 */
	@SimpleDataElement
	public static final int 全局_电源菜单 = 6;

	/**
	 * 切换分屏。
	 */
	@SimpleDataElement
	public static final int 全局_切换分屏 = 7;

	/**
	 * 锁屏。需要 API 28 及以上。
	 */
	@SimpleDataElement
	public static final int 全局_锁屏 = 8;

	/**
	 * 截图。需要 API 28 及以上。
	 */
	@SimpleDataElement
	public static final int 全局_截图 = 9;

	/* 事件类型 */

	/**
	 * 全部。
	 */
	@SimpleDataElement
	public static final int 事件_全部 = -1;

	/**
	 * 单击。
	 */
	@SimpleDataElement
	public static final int 事件_单击 = 1;

	/**
	 * 长按。
	 */
	@SimpleDataElement
	public static final int 事件_长按 = 2;

	/**
	 * 选中。
	 */
	@SimpleDataElement
	public static final int 事件_选中 = 4;

	/**
	 * 输入焦点。
	 */
	@SimpleDataElement
	public static final int 事件_输入焦点 = 8;

	/**
	 * 文本改变。
	 */
	@SimpleDataElement
	public static final int 事件_文本改变 = 16;

	/**
	 * 窗口状态改变。
	 */
	@SimpleDataElement
	public static final int 事件_窗口状态改变 = 32;

	/**
	 * 通知改变。
	 */
	@SimpleDataElement
	public static final int 事件_通知改变 = 64;

	/**
	 * 悬停进入。
	 */
	@SimpleDataElement
	public static final int 事件_悬停进入 = 128;

	/**
	 * 悬停退出。
	 */
	@SimpleDataElement
	public static final int 事件_悬停退出 = 256;

	/**
	 * 窗口内容改变。
	 */
	@SimpleDataElement
	public static final int 事件_窗口内容改变 = 2048;

	/**
	 * 滚动。
	 */
	@SimpleDataElement
	public static final int 事件_滚动 = 4096;

	/**
	 * 文本选择改变。
	 */
	@SimpleDataElement
	public static final int 事件_文本选择改变 = 8192;

	/**
	 * 公告。
	 */
	@SimpleDataElement
	public static final int 事件_公告 = 16384;

	/**
	 * 无障碍焦点。
	 */
	@SimpleDataElement
	public static final int 事件_无障碍焦点 = 32768;

	/**
	 * 清除无障碍焦点。
	 */
	@SimpleDataElement
	public static final int 事件_清除无障碍焦点 = 65536;

	/**
	 * 按粒度移动。
	 */
	@SimpleDataElement
	public static final int 事件_按粒度移动 = 131072;

	/**
	 * 触摸探索开始。
	 */
	@SimpleDataElement
	public static final int 事件_触摸探索开始 = 512;

	/**
	 * 触摸探索结束。
	 */
	@SimpleDataElement
	public static final int 事件_触摸探索结束 = 1024;

	/**
	 * 手势检测开始。
	 */
	@SimpleDataElement
	public static final int 事件_手势检测开始 = 262144;

	/**
	 * 手势检测结束。
	 */
	@SimpleDataElement
	public static final int 事件_手势检测结束 = 524288;

	/**
	 * 触摸开始。
	 */
	@SimpleDataElement
	public static final int 事件_触摸开始 = 1048576;

	/**
	 * 触摸结束。
	 */
	@SimpleDataElement
	public static final int 事件_触摸结束 = 2097152;

	/**
	 * 窗口列表改变。
	 */
	@SimpleDataElement
	public static final int 事件_窗口列表改变 = 4194304;

	/**
	 * 上下文单击。
	 */
	@SimpleDataElement
	public static final int 事件_上下文单击 = 8388608;

	/**
	 * 读取上下文。
	 */
	@SimpleDataElement
	public static final int 事件_读取上下文 = 16777216;

	/* 节点动作 */

	/**
	 * 输入焦点。
	 */
	@SimpleDataElement
	public static final int 节点动作_输入焦点 = 1;

	/**
	 * 清除输入焦点。
	 */
	@SimpleDataElement
	public static final int 节点动作_清除输入焦点 = 2;

	/**
	 * 选择。
	 */
	@SimpleDataElement
	public static final int 节点动作_选择 = 4;

	/**
	 * 取消选择。
	 */
	@SimpleDataElement
	public static final int 节点动作_取消选择 = 8;

	/**
	 * 单击。
	 */
	@SimpleDataElement
	public static final int 节点动作_单击 = 16;

	/**
	 * 长按。
	 */
	@SimpleDataElement
	public static final int 节点动作_长按 = 32;

	/**
	 * 无障碍焦点。
	 */
	@SimpleDataElement
	public static final int 节点动作_无障碍焦点 = 64;

	/**
	 * 清除无障碍焦点。
	 */
	@SimpleDataElement
	public static final int 节点动作_清除无障碍焦点 = 128;

	/**
	 * 下一粒度。
	 */
	@SimpleDataElement
	public static final int 节点动作_下一粒度 = 256;

	/**
	 * 上一粒度。
	 */
	@SimpleDataElement
	public static final int 节点动作_上一粒度 = 512;

	/**
	 * 下一HTML元素。
	 */
	@SimpleDataElement
	public static final int 节点动作_下一HTML元素 = 1024;

	/**
	 * 上一HTML元素。
	 */
	@SimpleDataElement
	public static final int 节点动作_上一HTML元素 = 2048;

	/**
	 * 向前滚动。
	 */
	@SimpleDataElement
	public static final int 节点动作_向前滚动 = 4096;

	/**
	 * 向后滚动。
	 */
	@SimpleDataElement
	public static final int 节点动作_向后滚动 = 8192;

	/**
	 * 复制。
	 */
	@SimpleDataElement
	public static final int 节点动作_复制 = 16384;

	/**
	 * 粘贴。
	 */
	@SimpleDataElement
	public static final int 节点动作_粘贴 = 32768;

	/**
	 * 剪切。
	 */
	@SimpleDataElement
	public static final int 节点动作_剪切 = 65536;

	/**
	 * 选择文本。
	 */
	@SimpleDataElement
	public static final int 节点动作_选择文本 = 131072;

	/**
	 * 展开。
	 */
	@SimpleDataElement
	public static final int 节点动作_展开 = 262144;

	/**
	 * 折叠。
	 */
	@SimpleDataElement
	public static final int 节点动作_折叠 = 524288;

	/**
	 * 关闭。
	 */
	@SimpleDataElement
	public static final int 节点动作_关闭 = 1048576;

	/**
	 * 设置文本。
	 */
	@SimpleDataElement
	public static final int 节点动作_设置文本 = 2097152;

	/**
	 * 显示到屏幕。
	 */
	@SimpleDataElement
	public static final int 节点动作_显示到屏幕 = 16908342;

	/**
	 * 滚动到位置。
	 */
	@SimpleDataElement
	public static final int 节点动作_滚动到位置 = 16908343;

	/**
	 * 向上滚动。
	 */
	@SimpleDataElement
	public static final int 节点动作_向上滚动 = 16908344;

	/**
	 * 向下滚动。
	 */
	@SimpleDataElement
	public static final int 节点动作_向下滚动 = 16908346;

	/**
	 * 向左滚动。
	 */
	@SimpleDataElement
	public static final int 节点动作_向左滚动 = 16908345;

	/**
	 * 向右滚动。
	 */
	@SimpleDataElement
	public static final int 节点动作_向右滚动 = 16908347;

	/**
	 * 上下文单击。
	 */
	@SimpleDataElement
	public static final int 节点动作_上下文单击 = 16908348;

	/**
	 * 设置进度。
	 */
	@SimpleDataElement
	public static final int 节点动作_设置进度 = 16908349;

	/**
	 * 移动窗口。需要 API 26 及以上。
	 */
	@SimpleDataElement
	public static final int 节点动作_移动窗口 = 16908354;

	/**
	 * 显示工具提示。需要 API 28 及以上。
	 */
	@SimpleDataElement
	public static final int 节点动作_显示工具提示 = 16908356;

	/**
	 * 隐藏工具提示。需要 API 28 及以上。
	 */
	@SimpleDataElement
	public static final int 节点动作_隐藏工具提示 = 16908357;

	/* 反馈类型 */

	/**
	 * 全部。
	 */
	@SimpleDataElement
	public static final int 反馈_全部 = -1;

	/**
	 * 语音。
	 */
	@SimpleDataElement
	public static final int 反馈_语音 = 1;

	/**
	 * 触觉。
	 */
	@SimpleDataElement
	public static final int 反馈_触觉 = 2;

	/**
	 * 声音。
	 */
	@SimpleDataElement
	public static final int 反馈_声音 = 4;

	/**
	 * 视觉。
	 */
	@SimpleDataElement
	public static final int 反馈_视觉 = 8;

	/**
	 * 通用。
	 */
	@SimpleDataElement
	public static final int 反馈_通用 = 16;

	/**
	 * 盲文。
	 */
	@SimpleDataElement
	public static final int 反馈_盲文 = 32;

	/* 配置标志 */

	/**
	 * 默认。
	 */
	@SimpleDataElement
	public static final int 标志_默认 = 1;

	/**
	 * 包含非重要视图。
	 */
	@SimpleDataElement
	public static final int 标志_包含非重要视图 = 2;

	/**
	 * 请求触摸探索。
	 */
	@SimpleDataElement
	public static final int 标志_请求触摸探索 = 4;

	/**
	 * 报告资源ID。
	 */
	@SimpleDataElement
	public static final int 标志_报告资源ID = 16;

	/**
	 * 过滤按键。
	 */
	@SimpleDataElement
	public static final int 标志_过滤按键 = 32;

	/**
	 * 读取交互窗口。
	 */
	@SimpleDataElement
	public static final int 标志_读取交互窗口 = 64;

	/**
	 * 无障碍音量。需要 API 26 及以上。
	 */
	@SimpleDataElement
	public static final int 标志_无障碍音量 = 128;

	/**
	 * 请求辅助按钮。需要 API 26 及以上。
	 */
	@SimpleDataElement
	public static final int 标志_请求辅助按钮 = 256;

	/**
	 * 请求指纹手势。需要 API 26 及以上。
	 */
	@SimpleDataElement
	public static final int 标志_请求指纹手势 = 512;

	/* 服务能力 */

	/**
	 * 读取窗口。
	 */
	@SimpleDataElement
	public static final int 能力_读取窗口 = 1;

	/**
	 * 触摸探索。
	 */
	@SimpleDataElement
	public static final int 能力_触摸探索 = 2;

	/**
	 * 过滤按键。
	 */
	@SimpleDataElement
	public static final int 能力_过滤按键 = 8;

	/**
	 * 控制放大。
	 */
	@SimpleDataElement
	public static final int 能力_控制放大 = 16;

	/**
	 * 派发手势。
	 */
	@SimpleDataElement
	public static final int 能力_派发手势 = 32;

	/**
	 * 指纹手势。需要 API 26 及以上。
	 */
	@SimpleDataElement
	public static final int 能力_指纹手势 = 64;

	/* 焦点类型 */

	/**
	 * 输入。
	 */
	@SimpleDataElement
	public static final int 焦点_输入 = 1;

	/**
	 * 无障碍。
	 */
	@SimpleDataElement
	public static final int 焦点_无障碍 = 2;

	/* 焦点查找方向 */

	/**
	 * 向后。
	 */
	@SimpleDataElement
	public static final int 焦点方向_向后 = 1;

	/**
	 * 向前。
	 */
	@SimpleDataElement
	public static final int 焦点方向_向前 = 2;

	/**
	 * 向左。
	 */
	@SimpleDataElement
	public static final int 焦点方向_向左 = 17;

	/**
	 * 向上。
	 */
	@SimpleDataElement
	public static final int 焦点方向_向上 = 33;

	/**
	 * 向右。
	 */
	@SimpleDataElement
	public static final int 焦点方向_向右 = 66;

	/**
	 * 向下。
	 */
	@SimpleDataElement
	public static final int 焦点方向_向下 = 130;

	/* 文本移动粒度 */

	/**
	 * 字符。
	 */
	@SimpleDataElement
	public static final int 粒度_字符 = 1;

	/**
	 * 单词。
	 */
	@SimpleDataElement
	public static final int 粒度_单词 = 2;

	/**
	 * 行。
	 */
	@SimpleDataElement
	public static final int 粒度_行 = 4;

	/**
	 * 段落。
	 */
	@SimpleDataElement
	public static final int 粒度_段落 = 8;

	/**
	 * 页。
	 */
	@SimpleDataElement
	public static final int 粒度_页 = 16;

	/* 软键盘显示策略 */

	/**
	 * 自动。
	 */
	@SimpleDataElement
	public static final int 软键盘_自动 = 0;

	/**
	 * 隐藏。
	 */
	@SimpleDataElement
	public static final int 软键盘_隐藏 = 1;

	/**
	 * 忽略硬键盘。需要 API 29 及以上。
	 */
	@SimpleDataElement
	public static final int 软键盘_忽略硬键盘 = 2;

	/* 窗口类型 */

	/**
	 * 应用。
	 */
	@SimpleDataElement
	public static final int 窗口_应用 = 1;

	/**
	 * 输入法。
	 */
	@SimpleDataElement
	public static final int 窗口_输入法 = 2;

	/**
	 * 系统。
	 */
	@SimpleDataElement
	public static final int 窗口_系统 = 3;

	/**
	 * 无障碍覆盖。
	 */
	@SimpleDataElement
	public static final int 窗口_无障碍覆盖 = 4;

	/**
	 * 分屏分隔。
	 */
	@SimpleDataElement
	public static final int 窗口_分屏分隔 = 5;

	/* 指纹手势 */

	/**
	 * 向上。需要 API 26 及受支持硬件。
	 */
	@SimpleDataElement
	public static final int 指纹手势_向上 = 4;

	/**
	 * 向下。需要 API 26 及受支持硬件。
	 */
	@SimpleDataElement
	public static final int 指纹手势_向下 = 8;

	/**
	 * 向左。需要 API 26 及受支持硬件。
	 */
	@SimpleDataElement
	public static final int 指纹手势_向左 = 2;

	/**
	 * 向右。需要 API 26 及受支持硬件。
	 */
	@SimpleDataElement
	public static final int 指纹手势_向右 = 1;

	/* 触摸探索手势 */

	/**
	 * 向上。
	 */
	@SimpleDataElement
	public static final int 触摸手势_向上 = 1;

	/**
	 * 向下。
	 */
	@SimpleDataElement
	public static final int 触摸手势_向下 = 2;

	/**
	 * 向左。
	 */
	@SimpleDataElement
	public static final int 触摸手势_向左 = 3;

	/**
	 * 向右。
	 */
	@SimpleDataElement
	public static final int 触摸手势_向右 = 4;

	/**
	 * 先上后下。
	 */
	@SimpleDataElement
	public static final int 触摸手势_先上后下 = 7;

	/**
	 * 先下后上。
	 */
	@SimpleDataElement
	public static final int 触摸手势_先下后上 = 8;

	/**
	 * 先左后右。
	 */
	@SimpleDataElement
	public static final int 触摸手势_先左后右 = 5;

	/**
	 * 先右后左。
	 */
	@SimpleDataElement
	public static final int 触摸手势_先右后左 = 6;

	/**
	 * 先上后左。
	 */
	@SimpleDataElement
	public static final int 触摸手势_先上后左 = 13;

	/**
	 * 先上后右。
	 */
	@SimpleDataElement
	public static final int 触摸手势_先上后右 = 14;

	/**
	 * 先下后左。
	 */
	@SimpleDataElement
	public static final int 触摸手势_先下后左 = 15;

	/**
	 * 先下后右。
	 */
	@SimpleDataElement
	public static final int 触摸手势_先下后右 = 16;

	/**
	 * 先左后上。
	 */
	@SimpleDataElement
	public static final int 触摸手势_先左后上 = 9;

	/**
	 * 先左后下。
	 */
	@SimpleDataElement
	public static final int 触摸手势_先左后下 = 10;

	/**
	 * 先右后上。
	 */
	@SimpleDataElement
	public static final int 触摸手势_先右后上 = 11;

	/**
	 * 先右后下。
	 */
	@SimpleDataElement
	public static final int 触摸手势_先右后下 = 12;

	/* 集合选择模式 */

	/**
	 * 无。
	 */
	@SimpleDataElement
	public static final int 集合选择_无 = 0;

	/**
	 * 单选。
	 */
	@SimpleDataElement
	public static final int 集合选择_单选 = 1;

	/**
	 * 多选。
	 */
	@SimpleDataElement
	public static final int 集合选择_多选 = 2;

	/* 范围类型 */

	/**
	 * 整数。
	 */
	@SimpleDataElement
	public static final int 范围_整数 = 0;

	/**
	 * 小数。
	 */
	@SimpleDataElement
	public static final int 范围_小数 = 1;

	/**
	 * 百分比。
	 */
	@SimpleDataElement
	public static final int 范围_百分比 = 2;

	/* 字幕边缘样式 */

	/**
	 * 无。
	 */
	@SimpleDataElement
	public static final int 字幕边缘_无 = 0;

	/**
	 * 轮廓。
	 */
	@SimpleDataElement
	public static final int 字幕边缘_轮廓 = 1;

	/**
	 * 阴影。
	 */
	@SimpleDataElement
	public static final int 字幕边缘_阴影 = 2;

	/**
	 * 凸起。
	 */
	@SimpleDataElement
	public static final int 字幕边缘_凸起 = 3;

	/**
	 * 凹下。
	 */
	@SimpleDataElement
	public static final int 字幕边缘_凹下 = 4;

	/**
	 * 未指定。
	 */
	@SimpleDataElement
	public static final int 字幕边缘_未指定 = -1;
}
