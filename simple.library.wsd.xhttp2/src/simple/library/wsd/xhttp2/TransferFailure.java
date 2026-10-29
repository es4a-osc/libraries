package simple.library.wsd.xhttp2;

import java.io.IOException;

/**
 * 将证书、缓存和文件处理阶段与普通网络 IOException 区分，同时保留原始原因。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class TransferFailure extends IOException {

	private static final long serialVersionUID = 1L;
	final String category;

	TransferFailure(String category, String message, Throwable cause) {
		super(message, cause);
		this.category = category;
	}
}
