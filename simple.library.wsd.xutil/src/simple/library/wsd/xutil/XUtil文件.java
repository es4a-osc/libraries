package simple.library.wsd.xutil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.xuexiang.xutil.file.FileUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 文件路径、文本读写和目录操作；仅能访问应用实际有权访问的路径。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUtil文件 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil文件() {
	}

	/**
	 * 取得应用私有目录的绝对路径，不需要存储权限。
	 *
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 取私有目录() {
		return XUtilSupport.context().getFilesDir().getAbsolutePath();
	}

	/**
	 * 取得应用私有目录的绝对路径，不需要存储权限。
	 *
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 取缓存目录() {
		return XUtilSupport.context().getCacheDir().getAbsolutePath();
	}

	/**
	 * 按给定路径是否存在；删除目录时递归删除，请先确认目标。
	 *
	 * @param path 路径。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否存在(String path) {
		return FileUtils.isFileExists(path);
	}

	/**
	 * 按给定路径是否文件；删除目录时递归删除，请先确认目标。
	 *
	 * @param path 路径。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否文件(String path) {
		return FileUtils.isFile(path);
	}

	/**
	 * 按给定路径是否目录；删除目录时递归删除，请先确认目标。
	 *
	 * @param path 路径。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否目录(String path) {
		return FileUtils.isDir(path);
	}

	/**
	 * 按给定路径创建目录；删除目录时递归删除，请先确认目标。
	 *
	 * @param path 路径。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 创建目录(String path) {
		return FileUtils.createOrExistsDir(path);
	}

	/**
	 * 按给定路径创建文件；删除目录时递归删除，请先确认目标。
	 *
	 * @param path 路径。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 创建文件(String path) {
		return FileUtils.createOrExistsFile(path);
	}

	/**
	 * 按给定路径删除；删除目录时递归删除，请先确认目标。
	 *
	 * @param path 路径。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 删除(String path) {
		return FileUtils.delete(path);
	}

	/**
	 * 取得文件字节数；不存在或不是文件时返回 -1。
	 *
	 * @param path 路径。
	 * @return 返回的长整数值。
	 */
	@SimpleFunction
	public static long 取文件长度(String path) {
		// String 重载还会尝试联网读取 URL；本函数只查询本地文件，不引入隐式网络行为。
		return FileUtils.getFileLength(new File(path));
	}

	/**
	 * 以 UTF-8 读取完整文本，读取失败抛出异常；大文件应使用流式接口。
	 *
	 * @param path 路径。
	 * @return 返回的文本。
	 */
	@SimpleFunction
	public static String 读文本(String path) {
		try {
			return XUtilSupport.readText(path);
		} catch (IOException failure) {
			throw XUtilSupport.ioFailure(failure);
		}
	}

	/**
	 * 以 UTF-8 写入文本，必要时创建父目录；写入失败抛出异常。
	 *
	 * @param path 路径。
	 * @param text 文本。
	 * @param append 追加。
	 */
	@SimpleFunction
	public static void 写文本(String path, String text, boolean append) {
		try {
			File file = new File(path).getAbsoluteFile();
			if (!FileUtils.createOrExistsDir(file.getParentFile())) {
				throw new IOException("无法创建父目录：" + file.getParent());
			}
			try (FileOutputStream output = new FileOutputStream(file, append)) {
				output.write(text.getBytes(StandardCharsets.UTF_8));
			}
		} catch (IOException failure) {
			throw XUtilSupport.ioFailure(failure);
		}
	}

	/**
	 * 列出目录直接子项的绝对路径并稳定排序；目录不可读时抛出异常。
	 *
	 * @param path 路径。
	 * @return 返回的文本数组。
	 */
	@SimpleFunction
	public static String[] 列出(String path) {
		File[] files = new File(path).listFiles();
		if (files == null) {
			throw new IllegalArgumentException("无法读取目录：" + path);
		}
		String[] paths = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			paths[i] = files[i].getAbsolutePath();
		}
		java.util.Arrays.sort(paths);
		return paths;
	}
}
