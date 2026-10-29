package simple.library.wsd.xupdate;

import com.xuexiang.xupdate.entity.UpdateEntity;

import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 可创建和修改的版本信息；提交给更新器后使用独立快照。
 * 文件大小以字节表示，默认解析时会将 XUpdate 接口的 KB 转换为字节。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class 更新信息 {

	private boolean available = false;
	private int versionCode = 0;
	private String versionName = "";
	private String content = "";
	private String url = "";
	private long size = 0;
	private String md5 = "";
	private boolean forced = false;
	private boolean ignorable = false;

	/**
	 * 创建无更新的空版本信息。
	 */
	public 更新信息() {
	}

	/**
	 * 取得是否有可用更新；自定义解析时须明确设置。
	 *
	 * @return 是否有可用更新；自定义解析时须明确设置。
	 */
	@SimpleProperty
	public boolean 有更新() {
		return available;
	}

	/**
	 * 设置是否有可用更新；自定义解析时须明确设置。
	 *
	 * @param value 是否有可用更新；自定义解析时须明确设置。
	 */
	@SimpleProperty
	public void 有更新(boolean value) {
		available = value;
	}

	/**
	 * 取得服务端版本号，必须大于零且大于本机版本才作为更新。
	 *
	 * @return 服务端版本号，必须大于零且大于本机版本才作为更新。
	 */
	@SimpleProperty
	public int 版本号() {
		return versionCode;
	}

	/**
	 * 设置服务端版本号，必须大于零且大于本机版本才作为更新。
	 *
	 * @param value 服务端版本号，必须大于零且大于本机版本才作为更新。
	 */
	@SimpleProperty
	public void 版本号(int value) {
		versionCode = value;
	}

	/**
	 * 取得向用户显示的版本名称。
	 *
	 * @return 向用户显示的版本名称。
	 */
	@SimpleProperty
	public String 版本名() {
		return versionName;
	}

	/**
	 * 设置向用户显示的版本名称。
	 *
	 * @param value 向用户显示的版本名称。
	 */
	@SimpleProperty
	public void 版本名(String value) {
		versionName = value == null ? "" : value;
	}

	/**
	 * 取得向用户显示的更新说明，支持换行。
	 *
	 * @return 向用户显示的更新说明，支持换行。
	 */
	@SimpleProperty
	public String 更新内容() {
		return content;
	}

	/**
	 * 设置向用户显示的更新说明，支持换行。
	 *
	 * @param value 向用户显示的更新说明，支持换行。
	 */
	@SimpleProperty
	public void 更新内容(String value) {
		content = value == null ? "" : value;
	}

	/**
	 * 取得完整的 HTTP 或 HTTPS APK 下载地址。
	 *
	 * @return 完整的 HTTP 或 HTTPS APK 下载地址。
	 */
	@SimpleProperty
	public String 下载地址() {
		return url;
	}

	/**
	 * 设置完整的 HTTP 或 HTTPS APK 下载地址。
	 *
	 * @param value 完整的 HTTP 或 HTTPS APK 下载地址。
	 */
	@SimpleProperty
	public void 下载地址(String value) {
		url = value == null ? "" : value;
	}

	/**
	 * 取得预期文件大小，单位为字节；零表示未知。
	 *
	 * @return 预期文件大小，单位为字节；零表示未知。
	 */
	@SimpleProperty
	public long 文件大小() {
		return size;
	}

	/**
	 * 设置预期文件大小，单位为字节；零表示未知。
	 *
	 * @param value 预期文件大小，单位为字节；零表示未知。
	 */
	@SimpleProperty
	public void 文件大小(long value) {
		size = value;
	}

	/**
	 * 取得APK 的 32 位十六进制 MD5；空文本表示不校验摘要。
	 *
	 * @return APK 的 32 位十六进制 MD5；空文本表示不校验摘要。
	 */
	@SimpleProperty
	public String MD5() {
		return md5;
	}

	/**
	 * 设置APK 的 32 位十六进制 MD5；空文本表示不校验摘要。
	 *
	 * @param value APK 的 32 位十六进制 MD5；空文本表示不校验摘要。
	 */
	@SimpleProperty
	public void MD5(String value) {
		md5 = value == null ? "" : value;
	}

	/**
	 * 取得隐藏提示框关闭按钮；程序仍可取消，系统安装仍由用户确认。
	 *
	 * @return 隐藏提示框关闭按钮；程序仍可取消，系统安装仍由用户确认。
	 */
	@SimpleProperty
	public boolean 强制更新() {
		return forced;
	}

	/**
	 * 设置隐藏提示框关闭按钮；程序仍可取消，系统安装仍由用户确认。
	 *
	 * @param value 隐藏提示框关闭按钮；程序仍可取消，系统安装仍由用户确认。
	 */
	@SimpleProperty
	public void 强制更新(boolean value) {
		forced = value;
	}

	/**
	 * 取得允许用户忽略该版本；强制更新优先，不显示忽略入口。
	 *
	 * @return 允许用户忽略该版本；强制更新优先，不显示忽略入口。
	 */
	@SimpleProperty
	public boolean 允许忽略() {
		return ignorable;
	}

	/**
	 * 设置允许用户忽略该版本；强制更新优先，不显示忽略入口。
	 *
	 * @param value 允许用户忽略该版本；强制更新优先，不显示忽略入口。
	 */
	@SimpleProperty
	public void 允许忽略(boolean value) {
		ignorable = value;
	}

	/**
	 * 从上游默认解析结果复制数据并转换大小单位。
	 *
	 * @param entity 上游解析结果，不得为空。
	 * @return 与上游对象独立的版本信息。
	 */
	static 更新信息 fromEntity(UpdateEntity entity) {
		更新信息 info = new 更新信息();
		info.available = entity.isHasUpdate();
		info.versionCode = entity.getVersionCode();
		info.versionName = entity.getVersionName() == null ? "" : entity.getVersionName();
		info.content = entity.getUpdateContent() == null ? "" : entity.getUpdateContent();
		info.url = entity.getDownloadUrl() == null ? "" : entity.getDownloadUrl();
		info.size = Math.multiplyExact(entity.getSize(), 1024L);
		info.md5 = entity.getMd5() == null ? "" : entity.getMd5();
		info.forced = entity.isForce();
		info.ignorable = entity.isIgnorable();
		return info;
	}

	/**
	 * 创建与原对象独立的值快照，防止事件结束后改写活动任务。
	 *
	 * @return 当前全部字段的副本。
	 */
	更新信息 copy() {
		更新信息 info = new 更新信息();
		info.available = available;
		info.versionCode = versionCode;
		info.versionName = versionName;
		info.content = content;
		info.url = url;
		info.size = size;
		info.md5 = md5;
		info.forced = forced;
		info.ignorable = ignorable;
		return info;
	}
}
