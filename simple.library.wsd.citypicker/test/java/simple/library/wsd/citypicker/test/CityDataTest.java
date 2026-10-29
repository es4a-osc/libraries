package simple.library.wsd.citypicker.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import simple.library.wsd.citypicker.internal.City;
import simple.library.wsd.citypicker.internal.CityStore;

/**
 * 验证城市数据排序、查询和快照隔离；不替代 SQLite、主题及弹层设备测试。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class CityDataTest {

	/**
	 * 测试入口只提供静态执行方式。
	 */
	private CityDataTest() {
	}

	/**
	 * 执行数据边界和不变性检查，任一失败均以异常结束进程。
	 *
	 * @param arguments 无需参数。
	 */
	public static void main(String[] arguments) {
		City unknown = new City("无拼音", "测试省", "", "UNKNOWN");
		City shenzhen = new City("深圳", "广东", "ShenZhen", "101280601");
		City beijing = new City("北京", "北京", "beijing", "101010100");
		City hangzhou = new City("杭州", "浙江", "hangzhou", "101210101");
		List<City> source = new ArrayList<City>(Arrays.asList(unknown, shenzhen, beijing, hangzhou));
		List<City> sorted = CityStore.sorted(source);
		check(sorted.get(0) == beijing && sorted.get(3) == unknown, "字母排序及井号末尾");
		check(source.get(0) == unknown, "排序不得修改调用者列表");
		check(CityStore.search(sorted, " SHEN ").get(0) == shenzhen, "拼音不区分大小写并忽略首尾空白");
		check(CityStore.search(sorted, "州").get(0) == hangzhou, "中文名称子串");
		check(CityStore.search(sorted, "henz").isEmpty(), "拼音必须匹配前缀");
		check(CityStore.search(sorted, "不存在的城市").isEmpty(), "无结果不得保留旧数据");
		List<City> all = CityStore.search(sorted, "");
		all.clear();
		check(sorted.size() == 4, "查询结果与原列表隔离");
		check(CityStore.sorted(new ArrayList<City>()).isEmpty(), "空城市列表");
		check(unknown.section().equals("#"), "空拼音分组");
		System.out.println("CityDataTest: 9 checks passed");
	}

	/**
	 * 检查可观察的数据结果。
	 *
	 * @param condition 本项检查是否成立。
	 * @param message 用于定位失败的检查说明。
	 */
	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
