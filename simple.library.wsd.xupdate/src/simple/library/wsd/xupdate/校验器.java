package simple.library.wsd.xupdate;

import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleObject;

/**
 * 替换下载文件的最终校验；只有通过后才报告下载完成和允许后续安装。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
public interface 校验器 {

	/**
	 * 验证已下载的完整文件。
	 *
	 * @param path 文件绝对路径。
	 * @param info 预期大小及校验信息。
	 * @return 是否通过校验。
	 */
	@SimpleFunction
	boolean 校验(String path, 更新信息 info);
}
