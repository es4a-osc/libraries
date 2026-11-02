package simple.library.wsd.iconics;

import android.graphics.*;
import android.view.*;
import android.widget.*;
import com.mikepenz.iconics.*;
import com.mikepenz.iconics.typeface.*;
import java.util.*;
import simple.runtime.annotations.*;
import simple.runtime.components.*;

/**
 * 图标动画控制组件。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleComponent
@SimpleObject
public interface 图标动画 extends 组件 {
	/**
	 * 当前操作完成后在主线程异步通知；清空、移除、重绑或销毁丢弃旧通知。
	 *
	 * @param index 由添加返回的稳定动画编号。
	 */
	@SimpleEvent
	void 动画开始(int index);
	/**
	 * 当前操作完成后在主线程异步通知；清空、移除、重绑或销毁丢弃旧通知。
	 *
	 * @param index 由添加返回的稳定动画编号。
	 */
	@SimpleEvent
	void 动画结束(int index);
	/**
	 * 当前操作完成后在主线程异步通知；清空、移除、重绑或销毁丢弃旧通知。
	 *
	 * @param index 由添加返回的稳定动画编号。
	 */
	@SimpleEvent
	void 动画重复(int index);
	/**
	 * 当前操作完成后在主线程异步通知；清空、移除、重绑或销毁丢弃旧通知。
	 *
	 * @param index 由添加返回的稳定动画编号。
	 */
	@SimpleEvent
	void 动画取消(int index);
	/**
	 * 当前操作完成后在主线程异步通知；清空、移除、重绑或销毁丢弃旧通知。
	 *
	 * @param index 由添加返回的稳定动画编号。
	 */
	@SimpleEvent
	void 动画暂停(int index);
	/**
	 * 当前操作完成后在主线程异步通知；清空、移除、重绑或销毁丢弃旧通知。
	 *
	 * @param index 由添加返回的稳定动画编号。
	 */
	@SimpleEvent
	void 动画继续(int index);
	/**
	 * 绑定目标；-2背景，-1图片，0左1上2右3下4起始5结束；更换目标清空原动画。
	 *
	 * @param target 目标。
	 * @param image 图像。
	 * @param position 位置。
	 */
	@SimpleFunction
	public void 绑定(可视组件 target, 图标图像 image, int position);
	/**
	 * 复制配置并返回稳定编号；移除其他项不改变编号，清空不复用旧编号。
	 *
	 * @param config 配置。
	 * @return 复制配置并返回稳定编号；移除其他项不改变编号，清空不复用旧编号。
	 */
	@SimpleFunction
	public int 添加(图标动画配置 config);
	/**
	 * 开始全部动画。
	 */
	@SimpleFunction
	public void 开始();
	/**
	 * 暂停全部动画。
	 */
	@SimpleFunction
	public void 暂停();
	/**
	 * 继续已暂停动画。
	 */
	@SimpleFunction
	public void 继续();
	/**
	 * 取消全部动画并保留当前位置。
	 */
	@SimpleFunction
	public void 取消();
	/**
	 * 立即到达动画终点。
	 */
	@SimpleFunction
	public void 结束();
	/**
	 * 反向播放全部动画。
	 */
	@SimpleFunction
	public void 反向();
	/**
	 * 取消并移除全部配置。
	 */
	@SimpleFunction
	public void 清空();
	/**
	 * 是否至少一个动画正在运行。
	 *
	 * @return 是否至少一个动画正在运行。
	 */
	@SimpleFunction
	public boolean 正在运行();

	/**
	 * 从头启动指定动画。
	 *
	 * @param id 编号。
	 */
	@SimpleFunction
	public void 开始单项(int id);

	/**
	 * 手动暂停指定动画，返回前台不会自动恢复。
	 *
	 * @param id 编号。
	 */
	@SimpleFunction
	public void 暂停单项(int id);

	/**
	 * 继续指定已暂停动画。
	 *
	 * @param id 编号。
	 */
	@SimpleFunction
	public void 继续单项(int id);

	/**
	 * 取消指定动画，保留当前绘制值。
	 *
	 * @param id 编号。
	 */
	@SimpleFunction
	public void 取消单项(int id);

	/**
	 * 结束指定动画并到达终点。
	 *
	 * @param id 编号。
	 */
	@SimpleFunction
	public void 结束单项(int id);

	/**
	 * 反向播放指定动画。
	 *
	 * @param id 编号。
	 */
	@SimpleFunction
	public void 反向单项(int id);

	/**
	 * 移除指定动画及效果，其他编号保持不变，旧回调丢弃。
	 *
	 * @param id 编号。
	 */
	@SimpleFunction
	public void 移除(int id);

	/**
	 * 指定动画是否正在运行。
	 *
	 * @param id 编号。
	 * @return 指定动画是否正在运行。
	 */
	@SimpleFunction
	public boolean 单项正在运行(int id);

	/**
	 * 指定动画是否已开始，包括延时或暂停。
	 *
	 * @param id 编号。
	 * @return 指定动画是否已开始，包括延时或暂停。
	 */
	@SimpleFunction
	public boolean 单项已开始(int id);

	/**
	 * 指定动画是否暂停。
	 *
	 * @param id 编号。
	 * @return 指定动画是否暂停。
	 */
	@SimpleFunction
	public boolean 单项已暂停(int id);

	/**
	 * 读取当前插值结果；尚未开始时返回配置起值。
	 *
	 * @param id 编号。
	 * @return 读取当前插值结果；尚未开始时返回配置起值。
	 */
	@SimpleFunction
	public float 取单项当前值(int id);

	/**
	 * 读取当前插值进度，部分插值器可能超出0至1。
	 *
	 * @param id 编号。
	 * @return 读取当前插值进度，部分插值器可能超出0至1。
	 */
	@SimpleFunction
	public float 取单项进度(int id);

	/**
	 * 设置当前周期进度0至1，不自动开始计时。
	 *
	 * @param id 编号。
	 * @param fraction 进度。
	 */
	@SimpleFunction
	public void 设置单项进度(int id, float fraction);

	/**
	 * 读取播放时间，单位毫秒。
	 *
	 * @param id 编号。
	 * @return 读取播放时间，单位毫秒。
	 */
	@SimpleFunction
	public long 取单项播放时间(int id);

	/**
	 * 设置播放时间，单位毫秒，不自动开始计时。
	 *
	 * @param id 编号。
	 * @param time 时间。
	 */
	@SimpleFunction
	public void 设置单项播放时间(int id, long time);

	/**
	 * 返回添加时配置的独立副本。
	 *
	 * @param id 编号。
	 * @return 返回添加时配置的独立副本。
	 */
	@SimpleFunction
	public 图标动画配置 取得单项配置(int id);

	/**
	 * 按添加顺序读取仍存在的稳定编号。
	 *
	 * @return 按添加顺序读取仍存在的稳定编号。
	 */
	@SimpleFunction
	public int[] 取动画编号();

	/**
	 * 读取仍存在的动画数量。
	 *
	 * @return 读取仍存在的动画数量。
	 */
	@SimpleFunction
	public int 取动画数量();

	/**
	 * 清空动画并释放目标引用，保留目标上的静态图像。
	 */
	@SimpleFunction
	public void 解除绑定();

	/**
	 * 查询当前绑定是否仍有效。
	 *
	 * @return 查询当前绑定是否仍有效。
	 */
	@SimpleFunction
	public boolean 是否已绑定();
	/**
	 * 将当前动画组合的绘制结果输出为PNG；导出尺寸采用图标固有像素尺寸。
	 *
	 * @return 当前帧PNG字节。
	 */
	@SimpleFunction
	public byte[] 取当前帧PNG();
}
