package simple.library.wsd.xupdate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 只处理任务私有文件的完整复制，不扩大共享路径或接触其它任务文件。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class UpdateFiles {

	private UpdateFiles() {
	}

	/**
	 * 写入临时文件并在完整关闭后提交目标路径。
	 *
	 * @param source 可读的来源文件。
	 * @param target 任务私有目标文件。
	 * @throws IOException 读取、创建目录或提交失败。
	 */
	static void copy(File source, File target) throws IOException {
		if (source.getCanonicalFile().equals(target.getCanonicalFile())) {
			return;
		}
		if (!source.isFile() || source.length() == 0) {
			throw new IOException("来源文件不存在或为空。");
		}
		if (!target.getParentFile().isDirectory() && !target.getParentFile().mkdirs()) {
			throw new IOException("无法创建私有文件目录。");
		}
		File temporary = new File(target.getPath() + ".copy");
		try {
			try (FileInputStream input = new FileInputStream(source);
					FileOutputStream output = new FileOutputStream(temporary)) {
				byte[] buffer = new byte[32768];
				int length;
				while ((length = input.read(buffer)) != -1) {
					if (Thread.currentThread().isInterrupted()) {
						throw new IOException("文件复制已取消。");
					}
					output.write(buffer, 0, length);
				}
				output.getFD().sync();
			}
			if (!temporary.renameTo(target)) {
				throw new IOException("无法提交私有文件。");
			}
		} finally {
			if (temporary.exists()) {
				temporary.delete();
			}
		}
	}
}
