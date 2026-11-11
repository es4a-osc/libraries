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
	WireReply reply;
	String key = "";

	/**
	 * 保存本地处理阶段、可读信息和原始异常。
	 *
	 * @param category 用于对外区分的错误阶段。
	 * @param message  可读的错误说明。
	 * @param cause    保留的原始错误。
	 */
	TransferFailure(String category, String message, Throwable cause) {
		super(message, cause);
		this.category = category;
	}
}
