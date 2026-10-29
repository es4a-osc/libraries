package simple.library.wsd.mpandroidchart;

/**
 * 在普通 JVM 中验证数据模型的复制、失败原子性和数值边界，不替代 Android 绘图验证。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class ChartModelTest {

	private static int assertions;

	/**
	 * 测试入口类不创建实例。
	 */
	private ChartModelTest() {
	}

	/**
	 * 执行模型回归；任一条件不满足即以断言失败结束。
	 *
	 * @param args 不使用命令行参数。
	 */
	public static void main(String[] args) {
		ChartModel model = new ChartModel();
		String[] names = { "甲", "乙", "丙" };
		float[] values = { -2, 0, 6 };
		model.setNames(names);
		names[0] = "已被调用方修改";
		model.add(ChartModel.LINE, "折线", new float[][] { values }, 0, 1);
		values[0] = 999;
		verify("甲".equals(model.names[0]), "名称必须独立复制");
		verify(model.series.get(0).columns[0][0] == -2, "数据必须独立复制并允许负数");
		verify(model.series.get(0).axis == 1, "数据组必须保留右轴归属");

		// 失败必须在写入模型之前发生，已有数据组保持不变。
		rejectSeries(model, ChartModel.LINE, new float[][] { { 1 } }, 0);
		rejectSeries(model, ChartModel.LINE, new float[][] { { 1, Float.NaN, 3 } }, 0);
		rejectSeries(model, ChartModel.LINE, new float[][] { { 1, Float.POSITIVE_INFINITY, 3 } }, 0);
		rejectSeries(model, ChartModel.LINE, new float[][] { { 1, 2, 3 } }, 2);
		rejectSeries(model, ChartModel.BUBBLE, new float[][] { { 1, 2, 3 }, { 0, -1, 2 } }, 0);
		rejectSeries(model, ChartModel.RADAR, new float[][] { { 0, -1, 2 } }, 0);
		rejectSeries(model, ChartModel.CANDLE, new float[][] { { 3, 3, 3 }, { 2, 4, 4 }, { 1, 1, 1 }, { 3, 3, 3 } }, 0);
		verify(model.series.size() == 1, "无效数据不能新增半个数据组");

		boolean rejected = false;
		try {
			model.setNames(new String[] { "新名称" });
		} catch (IllegalArgumentException exception) {
			rejected = true;
		}
		verify(rejected && model.names.length == 3, "已有数据时不能替换分类名称");
		model.clear();
		verify(model.names.length == 0 && model.series.isEmpty(), "清空必须释放名称及数据组");
		model.setNames(new String[] { "甲", "乙" });
		rejectSeries(model, ChartModel.RADAR, new float[][] { { 1, 2 } }, 0);
		model.clear();
		model.setNames(new String[] { "甲", "乙", "丙" });
		model.add(ChartModel.RADAR, "全零雷达", new float[][] { { 0, 0, 0 } }, 0, 0);
		model.add(ChartModel.BUBBLE, "零大小", new float[][] { { 1, 2, 3 }, { 0, 0, 0 } }, 0, 0);
		verify(model.series.size() == 2, "全零雷达及零大小气泡是有效输入");

		ChartModel pie = new ChartModel();
		pie.addSlice("零", 0, 0);
		rejected = false;
		try {
			pie.checkPie();
		} catch (IllegalArgumentException exception) {
			rejected = true;
		}
		verify(rejected, "非空全零饼图不能计算比例");
		pie.addSlice("正数", 2, 0);
		pie.checkPie();
		verify(pie.series.size() == 2, "饼图零值扇区可以与正值同时存在");
		rejected = false;
		try {
			pie.addSlice("负数", -1, 0);
		} catch (IllegalArgumentException exception) {
			rejected = true;
		}
		verify(rejected && pie.series.size() == 2, "拒绝负数扇区且保留原图");
		ChartModel.checkRange(0, -10, 10);
		ChartModel.checkRange(1, 0, 1);
		rejected = false;
		try {
			ChartModel.checkRange(0, 1, 1);
		} catch (IllegalArgumentException exception) {
			rejected = true;
		}
		verify(rejected, "纵轴范围不能退化为零长度");
		System.out.println("ChartModelTest PASS: " + assertions + " assertions");
	}

	/**
	 * 验证无效数据被拒绝，且原模型中的数据组数量保持不变。
	 *
	 * @param model 已有模型。
	 * @param kind 需要检查的图表类型。
	 * @param columns 无效输入列。
	 * @param axis 待检查的纵轴编号。
	 */
	private static void rejectSeries(ChartModel model, int kind, float[][] columns, int axis) {
		int before = model.series.size();
		boolean rejected = false;
		try {
			model.add(kind, "无效输入", columns, 0, axis);
		} catch (IllegalArgumentException exception) {
			rejected = true;
		}
		verify(rejected && model.series.size() == before, "无效数据应被完整拒绝：" + kind);
	}

	/**
	 * 记录一个有明确预期的条件。
	 *
	 * @param condition 实际结果是否符合预期。
	 * @param message 条件不满足时的失败原因。
	 */
	private static void verify(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
		assertions++;
	}
}
