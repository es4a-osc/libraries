package simple.library.wsd.xutil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Looper;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;

import com.xuexiang.xutil.XUtil;
import com.xuexiang.xutil.data.ACache;
import com.xuexiang.xutil.data.SPUtils;

import simple.runtime.android.MainActivity;

/**
 * 集中处理 Simple 上下文、输入边界及 XUtil 上游需要补充的兼容约束。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class XUtilSupport {

	// 仅保留进程级 Application 上下文，不缓存窗口或注册重复的生命周期回调。
	private static Context applicationContext;

	/**
	 * 内部辅助类不创建实例。
	 */
	private XUtilSupport() {
	}

	/**
	 * 按需初始化 XUtil；明确选择 Context 重载，不依赖上游 Provider 的启动时序。
	 *
	 * @return 应用上下文。
	 */
	static synchronized Context context() {
		if (applicationContext == null) {
			applicationContext = activity().getApplicationContext();
			XUtil.init(applicationContext);
		}
		return applicationContext;
	}

	/**
	 * 获取当前有效窗口，不使用上游尚未登记当前 Activity 的生命周期栈。
	 *
	 * @return 当前 Simple 窗口宿主。
	 */
	static Activity activity() {
		Activity activity = MainActivity.getContext();
		if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
			throw new IllegalStateException("当前没有有效的 Simple 窗口");
		}
		return activity;
	}

	/**
	 * 将可空的字符序列统一为普通文本。
	 *
	 * @param value 上游返回的字符序列。
	 * @return 非空文本。
	 */
	static String text(CharSequence value) {
		return value == null ? "" : value.toString();
	}

	/**
	 * 拒绝在工作线程直接修改界面。
	 */
	static void requireMainThread() {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			throw new IllegalStateException("此操作必须在界面线程调用");
		}
	}

	/**
	 * 阻止域名及接口枚举等阻塞操作占用界面线程。
	 */
	static void requireWorkerThread() {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			throw new IllegalStateException("此操作必须在工作线程调用");
		}
	}

	/**
	 * 检查非负计数，避免上游把无效参数当成正常结果。
	 *
	 * @param value 计数。
	 */
	static void nonNegative(int value) {
		if (value < 0) {
			throw new IllegalArgumentException("数值不能为负数");
		}
	}

	/**
	 * 检查有限的零到一比例。
	 *
	 * @param value 比例。
	 */
	static void fraction(float value) {
		if (Float.isNaN(value) || value < 0 || value > 1) {
			throw new IllegalArgumentException("比例必须在 0 到 1 之间");
		}
	}

	/**
	 * 校验私有存储的逻辑名称，不将它解释为外部路径。
	 *
	 * @param name 配置或缓存名称。
	 * @return 经过校验的名称。
	 */
	private static String storageName(String name) {
		if (name == null || name.trim().isEmpty() || name.equals(".") || name.equals("..")
				|| name.indexOf('/') >= 0 || name.indexOf('\\') >= 0 || name.indexOf('\0') >= 0) {
			throw new IllegalArgumentException("名称不能为空或包含路径分隔符");
		}
		return "xutil_" + name;
	}

	/**
	 * 获得命名配置，不改变应用其它配置的命名空间。
	 *
	 * @param name 配置名。
	 * @return 私有配置。
	 */
	static SharedPreferences preferences(String name) {
		context();
		return SPUtils.getSharedPreferences(storageName(name));
	}

	/**
	 * 获得应用内部缓存，避免上游默认目录随外部存储状态变化。
	 *
	 * @param name 缓存名。
	 * @return XUtil 缓存实例。
	 */
	static synchronized ACache cache(String name) {
		return ACache.get(new File(context().getCacheDir(), storageName(name)));
	}

	/**
	 * 把公开的零值不过期转换为上游约定，不接受负有效期。
	 *
	 * @param seconds 秒数。
	 * @return 上游有效期。
	 */
	static int cacheTtl(int seconds) {
		nonNegative(seconds);
		return seconds == 0 ? -1 : seconds;
	}

	/**
	 * 建立每次调用独立的日期格式器，避免共享可变格式器的线程竞争。
	 *
	 * @param pattern 日期格式。
	 * @param zone 时区标识。
	 * @return 严格日期格式器。
	 */
	static SimpleDateFormat dateFormat(String pattern, String zone) {
		boolean known = false;
		for (String id : TimeZone.getAvailableIDs()) {
			if (id.equals(zone)) {
				known = true;
				break;
			}
		}
		if (!known || pattern == null || pattern.isEmpty()) {
			throw new IllegalArgumentException("日期格式或时区无效");
		}
		SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.CHINA);
		format.setLenient(false);
		format.setTimeZone(TimeZone.getTimeZone(zone));
		return format;
	}

	/**
	 * 严格消费完整日期文本，不把解析失败与 Unix 起点混淆。
	 *
	 * @param text 日期文本。
	 * @param pattern 格式。
	 * @param zone 时区。
	 * @return 毫秒时间戳。
	 */
	static long parseDate(String text, String pattern, String zone) {
		SimpleDateFormat format = dateFormat(pattern, zone);
		ParsePosition position = new ParsePosition(0);
		Date date = format.parse(text, position);
		if (date == null || position.getIndex() != text.length()) {
			throw new IllegalArgumentException("日期文本与指定格式不匹配");
		}
		return date.getTime();
	}

	/**
	 * 获得网络服务；API 24 起的系统网络对象覆盖本项目全部支持范围。
	 *
	 * @return 系统网络服务。
	 */
	static ConnectivityManager connectivity() {
		ConnectivityManager manager = context().getSystemService(ConnectivityManager.class);
		if (manager == null) {
			throw new IllegalStateException("设备不提供网络服务");
		}
		return manager;
	}

	/**
	 * 取得默认网络能力，保留网络消失时的空结果。
	 *
	 * @return 当前网络能力，或无默认网络时的空值。
	 */
	static NetworkCapabilities networkCapabilities() {
		ConnectivityManager manager = connectivity();
		Network network = manager.getActiveNetwork();
		return network == null ? null : manager.getNetworkCapabilities(network);
	}

	/**
	 * 读取完整 UTF-8 资产，异常保留原因，不把缺失资产伪装为空文本。
	 *
	 * @param path 资产相对路径。
	 * @return 资产文本。
	 */
	static String readAsset(String path) {
		try (InputStream input = context().getAssets().open(path);
				ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[8192];
			int size;
			while ((size = input.read(buffer)) != -1) {
				output.write(buffer, 0, size);
			}
			return new String(output.toByteArray(), StandardCharsets.UTF_8);
		} catch (IOException failure) {
			throw ioFailure(failure);
		}
	}

	/**
	 * 将 I/O 原因传到 Simple 调用边界，不静默吞掉失败。
	 *
	 * @param failure 原始异常。
	 * @return 包含原始原因的异常。
	 */
	static IllegalStateException ioFailure(IOException failure) {
		return new IllegalStateException("文件或网络操作失败：" + failure.getMessage(), failure);
	}

	/**
	 * 使用所有支持版本均可用的文件流读取 UTF-8 文本。
	 *
	 * @param path 文件路径。
	 * @return 完整文本。
	 * @throws IOException 读取失败。
	 */
	static String readText(String path) throws IOException {
		try (InputStream input = new FileInputStream(path);
				ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[8192];
			int size;
			while ((size = input.read(buffer)) != -1) {
				output.write(buffer, 0, size);
			}
			return new String(output.toByteArray(), StandardCharsets.UTF_8);
		}
	}

	/**
	 * 防止压缩过程把正在生成的 ZIP 再加入自身。
	 *
	 * @param source 源文件或目录。
	 * @param destination ZIP 输出路径。
	 * @throws IOException 路径解析失败或输出位于输入中。
	 */
	static void checkZipDestination(String source, String destination) throws IOException {
		String from = new File(source).getCanonicalPath();
		String to = new File(destination).getCanonicalPath();
		if (to.equals(from) || to.startsWith(from + File.separator)) {
			throw new IOException("目标 ZIP 不能位于源文件或源目录中");
		}
	}

	/**
	 * 检查目标路径的每一级，不跟随已经存在的符号链接。
	 *
	 * @param path 待写入路径。
	 * @throws IOException 目标路径包含链接。
	 */
	private static void rejectLinks(File path) throws IOException {
		File data = context().getFilesDir().getParentFile();
		String logicalData = data.getAbsolutePath();
		String canonicalData = data.getCanonicalPath();
		// lstat 在 API 21 起可用；不依赖 API 26 才提供的 java.nio.file。
		for (File current = path; current != null; current = current.getParentFile()) {
			// Android 的 /data/user/0 本身可以是系统别名；仅信任当前应用数据根的映射。
			// 根目录内部的自建链接仍逐级拒绝，不把平台别名误判为用户的解压链接目标。
			if (current.getAbsolutePath().equals(logicalData) || current.getAbsolutePath().equals(canonicalData)) {
				break;
			}
			try {
				if (OsConstants.S_ISLNK(Os.lstat(current.getAbsolutePath()).st_mode)) {
					throw new IOException("解压目标不能包含符号链接：" + current);
				}
			} catch (ErrnoException failure) {
				if (failure.errno != OsConstants.ENOENT) {
					throw new IOException("无法检查解压路径：" + current, failure);
				}
			}
		}
	}

	/**
	 * 在实际写入边界逐条校验 ZIP 路径；不调用缺少路径校验的上游解压实现。
	 *
	 * @param source ZIP 文件。
	 * @param destination 目标目录。
	 * @param limit 累计解压字节上限。
	 * @return 已解压的文件路径。
	 */
	static String[] unzip(String source, String destination, long limit) {
		if (limit <= 0) {
			throw new IllegalArgumentException("解压字节上限必须大于零");
		}
		try (ZipFile archive = new ZipFile(source)) {
			File requestedRoot = new File(destination).getAbsoluteFile();
			rejectLinks(requestedRoot);
			File root = requestedRoot.getCanonicalFile();
			ensureDirectory(root);
			List<String> files = new ArrayList<String>();
			Enumeration<? extends ZipEntry> entries = archive.entries();
			byte[] buffer = new byte[8192];
			long remaining = limit;
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				String name = entry.getName().replace('\\', '/');
				File requested = new File(root, name);
				rejectLinks(requested);
				File output = requested.getCanonicalFile();
				if (name.isEmpty() || name.startsWith("/") || name.indexOf(':') >= 0
						|| !output.getPath().startsWith(root.getPath() + File.separator)) {
					throw new IOException("ZIP 条目超出目标目录：" + name);
				}
				rejectLinks(output);
				if (entry.isDirectory()) {
					ensureDirectory(output);
					continue;
				}
				ensureDirectory(output.getParentFile());
				// 拒绝覆盖已有文件，避免重复条目或预置文件被静默替换。
				if (!output.createNewFile()) {
					throw new IOException("解压文件已经存在：" + output);
				}
				try (InputStream input = archive.getInputStream(entry);
						OutputStream target = new FileOutputStream(output)) {
					int size;
					while ((size = input.read(buffer)) != -1) {
						if (size > remaining) {
							throw new IOException("超过累计解压字节上限");
						}
						target.write(buffer, 0, size);
						remaining -= size;
					}
				}
				files.add(output.toString());
			}
			return files.toArray(new String[0]);
		} catch (IOException failure) {
			throw ioFailure(failure);
		}
	}

	/**
	 * 创建或确认目录，不把同名普通文件误认为成功。
	 *
	 * @param directory 目录。
	 * @throws IOException 目录不可用。
	 */
	private static void ensureDirectory(File directory) throws IOException {
		if (!directory.isDirectory() && !directory.mkdirs()) {
			throw new IOException("无法创建目录：" + directory);
		}
	}

	/**
	 * 限制为普通电话号码，不允许通过号码注入额外的 URI 参数。
	 *
	 * @param number 电话号码。
	 * @return 合法号码。
	 */
	static String phoneNumber(String number) {
		if (number == null || !number.matches("[+0-9() -]+")) {
			throw new IllegalArgumentException("电话号码格式无效");
		}
		return number;
	}
}
