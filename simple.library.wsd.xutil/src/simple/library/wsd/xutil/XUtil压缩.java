package simple.library.wsd.xutil;

import java.io.IOException;

import com.xuexiang.xutil.file.ZipUtils;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * ZIP 压缩、目录索引与受路径边界约束的解压。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public final class XUtil压缩 {

	/**
	 * 函数集仅提供静态成员，不创建实例。
	 */
	private XUtil压缩() {
	}

	/**
	 * 压缩文件或目录为 ZIP；不能把目标 ZIP 放入源目录。
	 *
	 * @param source 源路径。
	 * @param destination 目标文件。
	 * @return 判断或操作的结果。
	 */
	@SimpleFunction
	public static boolean 压缩(String source, String destination) {
		try {
			XUtilSupport.checkZipDestination(source, destination);
			return ZipUtils.zipFile(source, destination);
		} catch (IOException failure) {
			throw XUtilSupport.ioFailure(failure);
		}
	}

	/**
	 * 列出 ZIP 的条目名称，损坏归档抛出异常。
	 *
	 * @param path 路径。
	 * @return 返回的文本数组。
	 */
	@SimpleFunction
	public static String[] 列出(String path) {
		try {
			return ZipUtils.getFilesPath(path).toArray(new String[0]);
		} catch (IOException failure) {
			throw XUtilSupport.ioFailure(failure);
		}
	}

	/**
	 * 解压 ZIP 并返回文件路径；拒绝路径越界及符号链接目标，累计解压上限由调用者指定。
	 *
	 * @param path 路径。
	 * @param destination 目标目录。
	 * @param limit 所有文件累计解压上限，必须大于零；失败可能保留已写入文件。
	 * @return 返回的文本数组。
	 */
	@SimpleFunction
	public static String[] 解压(String path, String destination, long limit) {
		return XUtilSupport.unzip(path, destination, limit);
	}
}
