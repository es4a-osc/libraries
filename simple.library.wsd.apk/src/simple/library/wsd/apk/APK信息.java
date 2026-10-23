package simple.library.wsd.apk;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;

import simple.runtime.android.MainApplication;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleProperty;

/**
 * 一次解析独立 APK，将名称、包名、版本和图标保存为对象自身的只读快照。
 *
 * <p>不安装应用，不枚举已安装应用，也不执行 APK 中的代码。解析是同步操作，
 * 大文件应由调用方放到后台线程；属性读取不再访问原 APK。
 * 对象不持有 Activity、PackageManager、Drawable 或打开的文件。</p>
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class APK信息 {

	private static final int MAX_ICON_SIZE = 256;

	private Snapshot snapshot = Snapshot.EMPTY;
	private String lastError = "";

	/**
	 * 创建尚未解析安装包的独立对象。
	 */
	public APK信息() {
	}

	/* 只读属性 */

	/**
	 * 返回是否已有完整的解析结果。
	 *
	 * @return 最近一次解析成功且尚未清空时返回真。
	 */
	@SimpleProperty
	public synchronized boolean 已解析() {
		return snapshot != Snapshot.EMPTY;
	}

	/**
	 * 返回解析时设备语言对应的应用显示名称。
	 *
	 * @return 系统解析的名称；没有结果时为空文本。
	 */
	@SimpleProperty
	public synchronized String 应用名称() {
		return snapshot.label;
	}

	/**
	 * 返回安装包声明的应用包名。
	 *
	 * @return 完整包名；没有结果时为空文本。
	 */
	@SimpleProperty
	public synchronized String 包名() {
		return snapshot.packageName;
	}

	/**
	 * 返回安装包声明的版本名称。
	 *
	 * @return 版本名称；安装包未声明或没有结果时为空文本。
	 */
	@SimpleProperty
	public synchronized String 版本名() {
		return snapshot.versionName;
	}

	/**
	 * 返回安装包的长整数版本号。
	 *
	 * <p>Android 9.0 起保留高位 versionCodeMajor 与低位 versionCode，
	 * 较早系统使用平台提供的 32 位版本号。没有结果时为 0，应结合已解析判断。</p>
	 *
	 * @return 平台提供的版本号。
	 */
	@SimpleProperty
	public synchronized long 版本号() {
		return snapshot.versionCode;
	}

	/**
	 * 返回系统解析图标的 PNG 字节副本，最长边不超过 256 像素。
	 *
	 * <p>包含绘制后的透明度，不保证与 APK 原始图标文件的尺寸、格式或字节一致。
	 * 未声明图标时使用平台默认应用图标。调用者可修改返回数组，不影响对象内的快照。</p>
	 *
	 * @return PNG 字节集；没有结果时返回空字节集。
	 */
	@SimpleProperty
	public synchronized byte[] 图标字节集() {
		return snapshot.icon.clone();
	}

	/**
	 * 返回最近一次解析失败的原因。
	 *
	 * @return 错误说明；新建、解析成功或清空后为空文本。
	 */
	@SimpleProperty
	public synchronized String 错误信息() {
		return lastError;
	}

	/* 解析与清空 */

	/**
	 * 解析本地独立 APK，并一次性保存名称、包名、版本和 PNG 图标。
	 *
	 * <p>相对路径以本应用私有 files 目录为基准；绝对路径必须已经可以读取。
	 * 不直接读取 content URI、网址、资产名或 APKS/XAPK 等容器。
	 * 选择器返回的文档应先复制到私有目录，再传入本地路径。
	 * 每次调用都复制到独立的私有临时路径再解析，以隔离平台资源缓存，完成后清理临时副本。
	 * 需要额外的复制时间和临时存储空间；失败时清空旧快照并设置错误信息。
	 * 删除或替换原文件不会自动更新已有快照，需再次调用本方法。</p>
	 *
	 * @param path 已有且可读取的独立 APK 文件路径。
	 * @return 完整解析成功返回真；参数、路径、资源或平台解析失败返回假。
	 */
	@SimpleFunction
	public synchronized boolean 解析(String path) {
		snapshot = Snapshot.EMPTY;
		lastError = "";
		File staged = null;

		try {
			Context context = MainApplication.getContext();
			if (context == null) {
				throw new IllegalStateException("当前应用上下文不可用");
			}

			File source = resolveArchive(context, path);
			staged = File.createTempFile("simple-apk-", ".apk", context.getCacheDir());
			copyArchive(source, staged);
			File file = staged;

			// 独立临时路径避开平台的同路径资源缓存，防止连续导入时图标与包名错配。
			// 只在显式解析时访问归档；属性读取不再调用 PackageManager。
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageArchiveInfo(file.getPath(), 0);
			if (info == null || info.applicationInfo == null || info.packageName == null
					|| info.packageName.length() == 0) {
				throw new IOException("不是平台可解析的独立 APK");
			}
			if (Build.VERSION.SDK_INT >= 21 && info.splitNames != null && info.splitNames.length > 0) {
				throw new IOException("不支持拆分 APK 组合，请使用独立 APK");
			}

			// 归档尚未安装，资源读取必须明确指向这份 APK，而不是已安装版本。
			ApplicationInfo application = info.applicationInfo;
			application.sourceDir = file.getPath();
			application.publicSourceDir = file.getPath();
			CharSequence label = application.loadLabel(manager);
			byte[] icon = renderIcon(application.loadIcon(manager));
			long version = readVersionCode(info);

			snapshot = new Snapshot(label == null ? info.packageName : label.toString(),
				info.packageName, info.versionName == null ? "" : info.versionName, version, icon);
		} catch (Exception error) {
			String message = error.getMessage();
			lastError = "解析 APK 失败：" + (message == null || message.length() == 0
				? error.getClass().getSimpleName() : message);
		} finally {
			if (staged != null) {
				try {
					if (staged.exists() && !staged.delete()) {
						throw new IOException("无法删除解析临时副本：" + staged.getPath());
					}
				} catch (Exception error) {
					snapshot = Snapshot.EMPTY;
					lastError = (lastError.length() == 0 ? "解析 APK 失败" : lastError)
						+ "；临时副本清理失败：" + error.getMessage();
				}
			}
		}

		return snapshot != Snapshot.EMPTY;
	}

	/**
	 * 释放当前信息和图标快照，并清空错误说明。
	 *
	 * <p>不删除原 APK 或调用者保存的 PNG 文件，清空后仍可再次解析。</p>
	 */
	@SimpleFunction
	public synchronized void 清空() {
		snapshot = Snapshot.EMPTY;
		lastError = "";
	}

	/* 路径、版本与图标 */

	/**
	 * 校验本地文件，不通过读取 URI 或扩展权限来绕过系统访问限制。
	 *
	 * @param context 当前应用上下文，仅用于相对路径基准。
	 * @param path 调用者提供的文件系统路径。
	 * @return 规范化的可读文件。
	 * @throws IOException 目标不是已有可读文件。
	 */
	private static File resolveArchive(Context context, String path) throws IOException {
		if (path == null || path.trim().length() == 0 || path.contains("://")) {
			throw new IllegalArgumentException("请提供 APK 文件路径，文档 URI 请先复制到私有目录");
		}

		File file = new File(path);
		if (!file.isAbsolute()) {
			file = new File(context.getFilesDir(), path);
		}
		file = file.getCanonicalFile();

		if (!file.isFile() || !file.canRead()) {
			throw new IOException("APK 文件不存在或不可读取：" + file.getPath());
		}

		return file;
	}

	/**
	 * 创建本次解析专用的文件内容，避免复用固定路径的资源缓存。
	 *
	 * @param source 调用者提供的原始文件，不修改其内容。
	 * @param target 本次拥有并负责清理的私有临时文件。
	 * @throws IOException 文件读取、写入或关闭失败。
	 */
	private static void copyArchive(File source, File target) throws IOException {
		try (FileInputStream input = new FileInputStream(source);
				FileOutputStream output = new FileOutputStream(target)) {
			byte[] buffer = new byte[65536];
			int count;
			while ((count = input.read(buffer)) != -1) {
				output.write(buffer, 0, count);
			}
		}
	}

	/**
	 * 按系统版本读取完整版本号，旧字段按无符号低 32 位解释。
	 *
	 * @param info 本次平台解析结果。
	 * @return 完整版本号。
	 */
	private static long readVersionCode(PackageInfo info) {
		return Build.VERSION.SDK_INT >= 28 ? info.getLongVersionCode() : readLegacyVersionCode(info);
	}

	/**
	 * API 27 及以下没有完整版本号接口，读取清单的原始低位字段。
	 *
	 * @param info 本次平台解析结果。
	 * @return 无符号低 32 位版本号。
	 */
	@SuppressWarnings("deprecation")
	private static long readLegacyVersionCode(PackageInfo info) {
		return ((long) info.versionCode) & 0xffffffffL;
	}

	/**
	 * 将普通、矢量或自适应 Drawable 绘制成有界 PNG，只回收本方法创建的位图。
	 *
	 * @param drawable 平台从 APK 解析的图标。
	 * @return 渲染后的 PNG 字节。
	 * @throws IOException 图标不存在或编码失败。
	 */
	private static byte[] renderIcon(Drawable drawable) throws IOException {
		if (drawable == null) {
			throw new IOException("平台未能提供应用图标");
		}

		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		if (width <= 0 || height <= 0) {
			width = 96;
			height = 96;
		}
		double scale = Math.min(1.0, (double) MAX_ICON_SIZE / Math.max(width, height));
		width = Math.max(1, (int) Math.round(width * scale));
		height = Math.max(1, (int) Math.round(height * scale));

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Rect previousBounds = new Rect(drawable.getBounds());

		try {
			drawable.setBounds(0, 0, width, height);
			drawable.draw(new Canvas(bitmap));
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
				throw new IOException("图标 PNG 编码失败");
			}

			return output.toByteArray();
		} finally {
			drawable.setBounds(previousBounds);
			bitmap.recycle();
		}
	}

	/**
	 * 已提交的结果只包含独立值，不保留平台资源或文件句柄。
	 */
	private static final class Snapshot {

		private static final Snapshot EMPTY = new Snapshot("", "", "", 0, new byte[0]);

		private final String label;
		private final String packageName;
		private final String versionName;
		private final long versionCode;
		private final byte[] icon;

		/**
		 * 保存一次 APK 解析得到的应用信息与图标快照。
		 *
		 * @param label 应用显示名称
		 * @param packageName 应用包名
		 * @param versionName 应用版本名称
		 * @param versionCode 应用版本号
		 * @param icon 应用图标 PNG 数据
		 */
		private Snapshot(String label, String packageName, String versionName, long versionCode, byte[] icon) {
			this.label = label;
			this.packageName = packageName;
			this.versionName = versionName;
			this.versionCode = versionCode;
			this.icon = icon;
		}
	}
}
