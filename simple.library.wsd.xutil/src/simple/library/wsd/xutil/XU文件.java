package simple.library.wsd.xutil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
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
public final class XU文件 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XU文件() {
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
	 * 取得应用私有缓存目录的绝对路径，不需要存储权限。
	 *
	 * @return 缓存目录绝对路径。
	 */
	@SimpleFunction
	public static String 取缓存目录() {
		return XUtilSupport.context().getCacheDir().getAbsolutePath();
	}

	/**
	 * 判断路径指向的文件或目录是否存在。
	 *
	 * @param path 路径。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否存在(String path) {
		return FileUtils.isFileExists(path);
	}

	/**
	 * 判断路径是否为普通文件。
	 *
	 * @param path 路径。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否文件(String path) {
		return FileUtils.isFile(path);
	}

	/**
	 * 判断路径是否为目录。
	 *
	 * @param path 路径。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 是否目录(String path) {
		return FileUtils.isDir(path);
	}

	/**
	 * 创建目录及缺失的父目录；目录已存在也视为成功。
	 *
	 * @param path 路径。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 创建目录(String path) {
		return FileUtils.createOrExistsDir(path);
	}

	/**
	 * 创建文件及缺失的父目录；普通文件已存在也视为成功。
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
	 * @param path   路径。
	 * @param text   文本。
	 * @param append 追加。
	 */
	@SimpleFunction
	public static void 写文本(String path, String text, boolean append) {
		写字节集(path, text.getBytes(StandardCharsets.UTF_8), append);
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

	/**
	 * 读取完整二进制文件；大文件应使用流式接口，读取失败抛出错误。
	 *
	 * @param path 路径。
	 * @return 读取完整二进制文件；大文件应使用流式接口，读取失败抛出错误。
	 */
	@SimpleFunction
	public static byte[] 读字节集(String path) {
		try (FileInputStream input = new FileInputStream(path);
				ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[8192];
			int n;
			while ((n = input.read(buffer)) != -1) {
				output.write(buffer, 0, n);
			}
			return output.toByteArray();
		} catch (IOException failure) {
			throw XUtilSupport.ioFailure(failure);
		}
	}

	/**
	 * 写入二进制数据，自动创建父目录；假为覆盖，真为追加。
	 *
	 * @param path   路径。
	 * @param data   数据。
	 * @param append 追加。
	 */
	@SimpleFunction
	public static void 写字节集(String path, byte[] data, boolean append) {
		try {
			File file = new File(path).getAbsoluteFile();
			if (!FileUtils.createOrExistsDir(file.getParentFile())) {
				throw new IOException("无法创建父目录");
			}
			try (FileOutputStream output = new FileOutputStream(file, append)) {
				output.write(data);
			}
		} catch (IOException failure) {
			throw XUtilSupport.ioFailure(failure);
		}
	}

	/**
	 * 取得路径末尾的文件名。
	 *
	 * @param path 路径。
	 * @return 取得路径末尾的文件名。
	 */
	@SimpleFunction
	public static String 取文件名(String path) {
		return FileUtils.getFileName(path);
	}

	/**
	 * 取得不含点号的文件扩展名。
	 *
	 * @param path 路径。
	 * @return 取得不含点号的文件扩展名。
	 */
	@SimpleFunction
	public static String 取扩展名(String path) {
		return FileUtils.getFileExtension(path);
	}

	/**
	 * 取得路径的父目录文本。
	 *
	 * @param path 路径。
	 * @return 取得路径的父目录文本。
	 */
	@SimpleFunction
	public static String 取父目录(String path) {
		return FileUtils.getDirName(path);
	}

	/**
	 * 取得文件最后修改的毫秒时间戳；不存在返回0。
	 *
	 * @param path 路径。
	 * @return 取得文件最后修改的毫秒时间戳；不存在返回0。
	 */
	@SimpleFunction
	public static long 取修改时间(String path) {
		return new File(path).lastModified();
	}

	/**
	 * 在原目录内重命名文件或目录，目标存在时返回假。
	 *
	 * @param path 路径。
	 * @param name 仅为文件名，不能包含路径分隔符。
	 * @return 在原目录内重命名文件或目录，目标存在时返回假。
	 */
	@SimpleFunction
	public static boolean 重命名(String path, String name) {
		if (name == null || name.isEmpty() || name.equals(".") || name.equals("..") || name.indexOf(47) >= 0
				|| name.indexOf(92) >= 0) {
			throw new IllegalArgumentException("新名称必须为单个文件名");
		}
		File source = new File(path).getAbsoluteFile();
		File dest = new File(source.getParentFile(), name);
		return !dest.exists() && source.renameTo(dest);
	}
}
