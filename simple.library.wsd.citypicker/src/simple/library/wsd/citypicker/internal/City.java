package simple.library.wsd.citypicker.internal;

import java.util.Locale;

/**
 * 一条独立城市记录；沿用 CityPicker 1.0.2 的名称、省份、拼音及代码语义。
 *
 * @author xuexiang
 * @author 树先生 xhwsd@qq.com
 */
public final class City {

	public final String name;
	public final String province;
	public final String pinyin;
	public final String code;

	/**
	 * 创建不可变城市记录，不替调用方推断拼音或城市代码。
	 *
	 * @param name 非空城市名称。
	 * @param province 省份。
	 * @param pinyin 完整拼音，用于搜索及分组。
	 * @param code 数据源中的城市代码。
	 */
	public City(String name, String province, String pinyin, String code) {
		if (name == null || name.trim().isEmpty() || province == null || pinyin == null || code == null) {
			throw new IllegalArgumentException("城市名称不能为空，其余字段不能为 null。");
		}
		this.name = name;
		this.province = province;
		this.pinyin = pinyin.toLowerCase(Locale.ROOT);
		this.code = code;
	}

	/**
	 * 取得字母分组；没有拉丁字母开头的拼音统一进入末尾的井号分组。
	 *
	 * @return A 至 Z，或井号。
	 */
	public String section() {
		if (!pinyin.isEmpty()) {
			char first = pinyin.charAt(0);
			if (first >= 'a' && first <= 'z') {
				return String.valueOf(Character.toUpperCase(first));
			}
		}
		return "#";
	}
}
