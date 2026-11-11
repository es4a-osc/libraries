package simple.library.wsd.iconics;

import com.mikepenz.iconics.typeface.GenericFont;
import com.mikepenz.iconics.typeface.ITypeface;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 字体信息和自定义映射。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 图标字体 {
	ITypeface value;
	boolean registered;

	/**
	 * 创建独立实例。
	 */
	public 图标字体() {
	}

	/**
	 * 字体名称。
	 *
	 * @return 字体名称。
	 */
	@SimpleFunction
	public String 取名称() {
		check();
		return value.getFontName();
	}

	/**
	 * 三个字符的前缀。
	 *
	 * @return 三个字符的前缀。
	 */
	@SimpleFunction
	public String 取前缀() {
		check();
		return value.getMappingPrefix();
	}

	/**
	 * 字体版本。
	 *
	 * @return 字体版本。
	 */
	@SimpleFunction
	public String 取版本() {
		check();
		return value.getVersion();
	}

	/**
	 * 字体作者。
	 *
	 * @return 字体作者。
	 */
	@SimpleFunction
	public String 取作者() {
		check();
		return value.getAuthor();
	}

	/**
	 * 字体网址。
	 *
	 * @return 字体网址。
	 */
	@SimpleFunction
	public String 取网址() {
		check();
		return value.getUrl();
	}

	/**
	 * 字体说明。
	 *
	 * @return 字体说明。
	 */
	@SimpleFunction
	public String 取说明() {
		check();
		return value.getDescription();
	}

	/**
	 * 字体许可证。
	 *
	 * @return 字体许可证。
	 */
	@SimpleFunction
	public String 取许可证() {
		check();
		return value.getLicense();
	}

	/**
	 * 字体许可证网址。
	 *
	 * @return 字体许可证网址。
	 */
	@SimpleFunction
	public String 取许可证网址() {
		check();
		return value.getLicenseUrl();
	}

	/**
	 * 字体图标数量。
	 *
	 * @return 字体图标数量。
	 */
	@SimpleFunction
	public int 取图标数量() {
		check();
		return value.getIconCount();
	}

	/**
	 * 所有图标名称。
	 *
	 * @return 所有图标名称。
	 */
	@SimpleFunction
	public String[] 取图标名称() {
		check();
		return value.getIcons().toArray(new String[0]);
	}

	/**
	 * 添加字符映射，字符码限 Unicode BMP。
	 *
	 * @param name 名称。
	 * @param code 字符码。
	 */
	@SimpleFunction
	public void 添加映射(String name, int code) {
		check();
		if (registered || !(value instanceof GenericFont)) {
			throw new 图标错误("只能修改尚未注册的自定义字体");
		}
		Support.requireName(name);
		if (code < 0 || code > 65535 || (code >= 55296 && code <= 57343)) {
			throw new 图标错误("字体映射必须是非代理项的 BMP 字符");
		}
		((GenericFont) value).registerIcon(name, (char) code);
	}

	/**
	 * 查询图标字符码。
	 *
	 * @param name 图标名称。
	 * @return 查询图标字符码。
	 */
	@SimpleFunction
	public int 取字符码(String name) {
		check();
		return Support.icon(value, name).getCharacter();
	}

	/**
	 * 拒绝未配置字体。
	 */
	void check() {
		Support.main();
		if (value == null) {
			throw new 图标错误("请通过图标工具创建或查询字体");
		}
	}

	/**
	 * 读取字体资源标识；资产或文件字体没有资源时返回负数。
	 *
	 * @return 读取字体资源标识；资产或文件字体没有资源时返回负数。
	 */
	@SimpleFunction
	public int 取字体资源() {
		check();
		return value.getFontRes();
	}
}
