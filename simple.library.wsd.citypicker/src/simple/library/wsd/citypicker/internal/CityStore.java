package simple.library.wsd.citypicker.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 将 CityPicker 1.0.2 的数据库复制到应用私有目录，并提供独立的排序、搜索快照。
 * 不持有 Activity，不跨选择器共享可变列表或数据库句柄。
 *
 * @author xuexiang
 * @author 树先生 xhwsd@qq.com
 */
public final class CityStore {

	/**
	 * 工具类不创建实例，避免误以为其持有数据库连接。
	 */
	private CityStore() {
	}

	/**
	 * 读取随库携带的数据库；复制或查询失败时报告错误，不返回伪造的空列表。
	 *
	 * @param context 用于访问应用资产和私有数据库目录的上下文。
	 * @return 已排序的城市列表。
	 */
	public static List<City> load(Context context) {
		File target = context.getDatabasePath("es4a_citypicker_102.db");
		if (!target.isFile()) {
			File parent = target.getParentFile();
			if (parent == null || (!parent.isDirectory() && !parent.mkdirs())) {
				throw new IllegalStateException("无法创建城市数据库目录。");
			}

			// 先写独立临时文件，复制完整后再安装，避免下次读取半份数据库。
			File staging = new File(parent, "es4a_citypicker_102.tmp");
			try (InputStream input = context.getAssets().open("es4a_citypicker_102.db");
					FileOutputStream output = new FileOutputStream(staging)) {
				byte[] buffer = new byte[8192];
				int count;
				while ((count = input.read(buffer)) != -1) {
					output.write(buffer, 0, count);
				}
				output.getFD().sync();
			} catch (IOException exception) {
				throw new IllegalStateException("复制城市数据库失败。", exception);
			}
			if (!staging.renameTo(target)) {
				throw new IllegalStateException("安装城市数据库失败。");
			}
		}

		List<City> cities = new ArrayList<City>();
		try (SQLiteDatabase database = SQLiteDatabase.openDatabase(target.getPath(), null, SQLiteDatabase.OPEN_READONLY);
				Cursor cursor = database.rawQuery("SELECT c_name, c_province, c_pinyin, c_code FROM cities", null)) {
			while (cursor.moveToNext()) {
				cities.add(new City(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)));
			}
		}
		return sorted(cities);
	}

	/**
	 * 复制并排序数据，同字母组按拼音排列，保持井号组在最后。
	 *
	 * @param cities 原始城市列表。
	 * @return 与原列表独立的有序副本。
	 */
	public static List<City> sorted(List<City> cities) {
		List<City> result = new ArrayList<City>(cities);
		Collections.sort(result, new Comparator<City>() {
			@Override
			public int compare(City left, City right) {
				String a = left.section().equals("#") ? "[" : left.section();
				String b = right.section().equals("#") ? "[" : right.section();
				int section = a.compareTo(b);
				return section == 0 ? left.pinyin.compareTo(right.pinyin) : section;
			}
		});
		return result;
	}

	/**
	 * 按上游语义匹配名称子串或拼音前缀；空关键字返回完整列表副本。
	 *
	 * @param cities 当前选择器的数据快照。
	 * @param keyword 用户输入。
	 * @return 搜索结果。
	 */
	public static List<City> search(List<City> cities, String keyword) {
		String query = keyword.trim().toLowerCase(Locale.ROOT);
		List<City> result = new ArrayList<City>();
		for (City city : cities) {
			if (city.name.contains(query) || city.pinyin.startsWith(query)) {
				result.add(city);
			}
		}
		return result;
	}
}
