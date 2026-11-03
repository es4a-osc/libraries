package simple.library.wsd.ftpclient;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * API 24 可用的本地文件操作；下载使用同目录临时文件及原子重命名提交。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class LocalFiles {
	/**
	 * 文件工具不可创建实例。
	 */
	private LocalFiles() {}

	/**
	 * 检查路径是否为符号链接，不跟随目标。
	 *
	 * @param file 目标路径。
	 * @return 是否为链接。
	 * @throws IOException 无法读取路径属性。
	 */
	static boolean isSymbolicLink(File file) throws IOException {
		try {
			return OsConstants.S_ISLNK(Os.lstat(file.getAbsolutePath()).st_mode);
		} catch (ErrnoException error) {
			if (error.errno == OsConstants.ENOENT) {
				return false;
			}
			throw new IOException(error);
		}
	}

	/**
	 * 读取普通文件尺寸，不用零表示缺失文件。
	 *
	 * @param file 目标文件。
	 * @return 文件字节数。
	 * @throws IOException 文件不存在或不是普通文件。
	 */
	static long size(File file) throws IOException {
		if (!file.isFile()) {
			throw new IOException("本地普通文件不存在：" + file);
		}
		return file.length();
	}

	/**
	 * 确保父目录存在。
	 *
	 * @param directory 目标目录。
	 * @throws IOException 不能创建目录。
	 */
	static void createDirectories(File directory) throws IOException {
		if (!directory.isDirectory() && !directory.mkdirs()) {
			throw new IOException("不能创建目录：" + directory);
		}
	}

	/**
	 * 创建原先不存在的单个目录。
	 *
	 * @param directory 新目录。
	 * @throws IOException 目录存在或无法创建。
	 */
	static void createDirectory(File directory) throws IOException {
		if (!directory.mkdir()) {
			throw new IOException("目标目录已存在或无法创建：" + directory);
		}
	}

	/**
	 * 建立本实例拥有的唯一断点文件。
	 *
	 * @param directory 目标所在目录。
	 * @param prefix 文件名前缀。
	 * @param suffix 文件名后缀。
	 * @return 实际新文件。
	 * @throws IOException 文件创建失败。
	 */
	static File createTempFile(File directory, String prefix, String suffix) throws IOException {
		return File.createTempFile(prefix, suffix, directory);
	}

	/**
	 * 复制现有目标到本实例断点文件。
	 *
	 * @param source 现有文件。
	 * @param target 拥有的断点文件。
	 * @throws IOException 复制失败。
	 */
	static void copy(File source, File target) throws IOException {
		try (InputStream input = new FileInputStream(source);
				OutputStream output = new FileOutputStream(target)) {
			byte[] buffer = new byte[65536];
			int count;
			while ((count = input.read(buffer)) >= 0) {
				if (count > 0) {
					output.write(buffer, 0, count);
				}
			}
		}
	}

	/**
	 * 将同目录断点文件原子提交到最终目标。
	 *
	 * @param source 本实例断点文件。
	 * @param target 最终文件。
	 * @throws IOException 重命名失败。
	 */
	static void move(File source, File target) throws IOException {
		try {
			Os.rename(source.getAbsolutePath(), target.getAbsolutePath());
		} catch (ErrnoException error) {
			throw new IOException(error);
		}
	}

	/**
	 * 删除本实例拥有的文件；不存在时无操作。
	 *
	 * @param file 断点文件。
	 * @throws IOException 删除失败。
	 */
	static void deleteIfExists(File file) throws IOException {
		if (file.exists() && !file.delete()) {
			throw new IOException("不能删除本地断点：" + file);
		}
	}

	/**
	 * 列出本地目录条目。
	 *
	 * @param directory 需要读取的目录。
	 * @return 条目数组。
	 * @throws IOException 无法读取目录。
	 */
	static File[] list(File directory) throws IOException {
		File[] result = directory.listFiles();
		if (result == null) {
			throw new IOException("无法读取本地目录：" + directory);
		}
		return result;
	}
}
