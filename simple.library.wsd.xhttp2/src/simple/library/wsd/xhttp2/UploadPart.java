package simple.library.wsd.xhttp2;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import okio.BufferedSink;

/**
 * 上传文件的不可变来源描述，每次写入重新打开文件，支持重试及未知长度的内容提供者。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class UploadPart {

	final String name;
	final String path;
	final String filename;
	final String type;
	final byte[] data;

	/**
	 * 创建并校验不可变上传项，拒绝含目录或换行的文件名。
	 *
	 * @param name     字段或会话名称。
	 * @param path     文件、URI 或成员路径，遵循对应操作的约束。
	 * @param data     已经复制的内存文件内容。
	 * @param filename 不包含目录的上传文件名。
	 * @param type     有效的 MIME 类型及可选字符集。
	 */
	private UploadPart(String name, String path, byte[] data, String filename, String type) {
		this.name = HttpSupport.nonempty(name);
		this.path = path;
		this.filename = HttpSupport.nonempty(filename);
		this.type = HttpSupport.media(type).toString();
		this.data = data;
		if (filename.contains("\r") || filename.contains("\n") || filename.contains("/") || filename.contains("\\")) {
			throw new IllegalArgumentException("上传文件名不能包含目录或换行。");
		}
	}

	/**
	 * 创建在每次发送时重新打开的文件来源。
	 *
	 * @param name     字段或会话名称。
	 * @param path     文件、URI 或成员路径，遵循对应操作的约束。
	 * @param filename 不包含目录的上传文件名。
	 * @param type     有效的 MIME 类型及可选字符集。
	 * @return 创建在每次发送时重新打开的文件来源。
	 */
	static UploadPart file(String name, String path, String filename, String type) {
		return new UploadPart(name, HttpSupport.nonempty(path), null, filename, type);
	}

	/**
	 * 复制字节数组并创建内存上传项。
	 *
	 * @param name     字段或会话名称。
	 * @param data     已经复制的内存文件内容。
	 * @param filename 不包含目录的上传文件名。
	 * @param type     有效的 MIME 类型及可选字符集。
	 * @return 复制字节数组并创建内存上传项。
	 */
	static UploadPart bytes(String name, byte[] data, String filename, String type) {
		return new UploadPart(name, "", HttpSupport.bytes(data), filename, type);
	}

	/**
	 * 复制上传配置和内存内容，文件路径仍引用同一来源。
	 *
	 * @return 复制上传配置和内存内容，文件路径仍引用同一来源。
	 */
	UploadPart copy() {
		return new UploadPart(name, path, data == null ? null : data.clone(), filename, type);
	}

	/**
	 * 只查询来源长度，未知时返回 -1，不消耗内容流。
	 *
	 * @param context 用于访问应用文件及资产的上下文。
	 * @return 独立的结果值或快照，语义与方法说明一致。
	 * @throws IOException 文件或文档来源无法查询。
	 */
	private long length(Context context) throws IOException {
		if (data != null) {
			return data.length;
		}
		if (path.startsWith("content://")) {
			try (AssetFileDescriptor descriptor = context.getContentResolver().openAssetFileDescriptor(Uri.parse(path),
					"r")) {
				return descriptor == null ? -1 : descriptor.getLength();
			}
		}
		if (path.startsWith("asset://")) {
			return -1;
		}
		File file = new File(path);
		if (!file.isAbsolute() || !file.isFile()) {
			throw new IOException("上传文件不存在：" + path);
		}
		return file.length();
	}

	/**
	 * 构造可重复读取且响应取消状态的上传请求体。
	 *
	 * @param context 用于访问应用文件及资产的上下文。
	 * @param control 共享的取消和提交状态。
	 * @return 构造可重复读取且响应取消状态的上传请求体。
	 */
	RequestBody body(final Context context, final TransferControl control) {
		return new RequestBody() {
			@Override
			public MediaType contentType() {
				return HttpSupport.media(type);
			}

			@Override
			public long contentLength() throws IOException {
				return length(context);
			}

			@Override
			public void writeTo(BufferedSink sink) throws IOException {
				try (InputStream input = data == null ? HttpSupport.open(context, path)
						: new ByteArrayInputStream(data)) {
					byte[] buffer = new byte[16384];
					int count;
					while ((count = input.read(buffer)) != -1) {
						control.check();
						sink.write(buffer, 0, count);
					}
				}
			}
		};
	}
}
