package simple.library.wsd.achartengine;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 图表共用配置、数据与交互接口。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface ACE坐标图表 extends ACE图表 {

	/**
	 * 设置分类索引名称，数值坐标数据不需要调用。
	 *
	 * @param names 分类名称，添加数据前设置；更换名称前先清空。
	 */
	@SimpleFunction
	void 设置项目名称(String[] names);

	/**
	 * 设置横轴和左右轴标题，构建或刷新后生效。
	 *
	 * @param x     横轴标题。
	 * @param left  左数值轴标题。
	 * @param right 右数值轴标题。
	 */
	@SimpleFunction
	void 设置轴标题(String x, String left, String right);

	/**
	 * 保存轴样式副本，构建或刷新后生效；数值轴刻度数量和角度为上游共享设置。
	 *
	 * @param axis  0 横轴，1 左数值轴，2 右数值轴。
	 * @param style 要复制的轴配置。
	 */
	@SimpleFunction
	void 设置轴样式(int axis, ACE轴样式 style);

	/**
	 * 固定指定坐标轴范围，构建或刷新后生效。
	 *
	 * @param axis 0 横轴，1 左数值轴，2 右数值轴。
	 * @param min  有限数值，必须小于最大值。
	 * @param max  有限数值。
	 */
	@SimpleFunction
	void 设置轴范围(int axis, double min, double max);

	/**
	 * 恢复指定轴按数据自动计算范围，下一次构建图表后生效。
	 *
	 * @param axis 0 横轴，1 左数值轴，2 右数值轴。
	 */
	@SimpleFunction
	void 恢复自动范围(int axis);

	/**
	 * 设置指定刻度位置的文字标签，相同坐标替换，构建或刷新后生效。
	 *
	 * @param axis  0 横轴，1 左数值轴，2 右数值轴。
	 * @param value 有限轴坐标。
	 * @param text  非空引用的文字。
	 */
	@SimpleFunction
	void 添加轴文字(int axis, double value, String text);

	/**
	 * 清空自定义轴文字，分类名称保留。
	 *
	 * @param axis 0 横轴，1 左数值轴，2 右数值轴。
	 */
	@SimpleFunction
	void 清空轴文字(int axis);

	/**
	 * 替换整组数据并保留样式，分类图须保持项目数和原索引，刷新或构建后生效。
	 *
	 * @param group  从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @param xs     严格递增的有限数值数组，不允许重复。
	 * @param values 按整列拼接；气泡先纵坐标后大小，区间先下限后上限。
	 */
	@SimpleFunction
	void 更新数据组(int group, double[] xs, double[] values);

	/**
	 * 向数值横坐标数据组追加一项，刷新或构建后生效。
	 *
	 * @param group  从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @param x      必须大于本组最后横坐标。
	 * @param values 此项目各列数值，顺序与更新数据组一致。
	 */
	@SimpleFunction
	void 追加项目(int group, double x, double[] values);

	/**
	 * 删除数值横坐标数据组中的一项，至少保留一项，刷新或构建后生效。
	 *
	 * @param group 从 0 开始的数据组编号；饼图和仪表盘固定为 0。
	 * @param index 从 0 开始的项目编号。
	 */
	@SimpleFunction
	void 删除项目(int group, int index);

	/**
	 * 直接设置已构建图表的可见范围；横轴范围对两条数值轴同步。
	 *
	 * @param axis   0 左轴，1 右轴。
	 * @param bounds 横轴最小、横轴最大、纵轴最小、纵轴最大，必须分别递增。
	 */
	@SimpleFunction
	void 设置可见范围(int axis, double[] bounds);

	/**
	 * 返回横轴最小、最大和指定纵轴最小、最大。
	 *
	 * @param axis 0 左轴，1 右轴。
	 * @return 返回横轴最小、最大和指定纵轴最小、最大。
	 */
	@SimpleFunction
	double[] 取可见范围(int axis);

	/**
	 * 保存拖动边界限制；缩放跨度为正数数据跨度，构建或刷新后生效。
	 *
	 * @param bounds 四个数值：横轴最小、最大和纵轴最小、最大；空数组取消限制。
	 */
	@SimpleFunction
	void 设置拖动边界(double[] bounds);

	/**
	 * 保存缩放跨度限制；缩放跨度为正数数据跨度，构建或刷新后生效。
	 *
	 * @param bounds 四个数值：横轴最小、最大和纵轴最小、最大；空数组取消限制。
	 */
	@SimpleFunction
	void 设置缩放跨度(double[] bounds);

	/**
	 * 转换已完成绘制的纵向图表坐标，返回横纵坐标；像素坐标单位 DIP，横向图形不支持此换算。
	 *
	 * @param x    数据坐标或相对图表左上角的 DIP 坐标。
	 * @param y    数据坐标或相对图表左上角的 DIP 坐标。
	 * @param axis 0 左轴，1 右轴。
	 * @return 转换已完成绘制的纵向图表坐标，返回横纵坐标；像素坐标单位 DIP，横向图形不支持此换算。
	 */
	@SimpleFunction
	double[] 数据坐标转像素(double x, double y, int axis);

	/**
	 * 转换已完成绘制的纵向图表坐标，返回横纵坐标；像素坐标单位 DIP，横向图形不支持此换算。
	 *
	 * @param x    数据坐标或相对图表左上角的 DIP 坐标。
	 * @param y    数据坐标或相对图表左上角的 DIP 坐标。
	 * @param axis 0 左轴，1 右轴。
	 * @return 转换已完成绘制的纵向图表坐标，返回横纵坐标；像素坐标单位 DIP，横向图形不支持此换算。
	 */
	@SimpleFunction
	double[] 像素转数据坐标(double x, double y, int axis);

	/**
	 * 将指定项目标记为缺失，不改变原数值；构建或刷新后生效，缺失点不能主动选择。
	 *
	 * @param group   从零开始的数据组编号。
	 * @param index   从零开始的项目编号。
	 * @param missing True 留空，False 恢复原始数值。
	 */
	@SimpleFunction
	void 设置项目缺失(int group, int index, boolean missing);

	/**
	 * 查询指定项目的缺失标记。
	 *
	 * @param group 从零开始的数据组编号。
	 * @param index 从零开始的项目编号。
	 * @return 查询指定项目的缺失标记。
	 */
	@SimpleFunction
	boolean 项目是否缺失(int group, int index);
}
