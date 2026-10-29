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

	static UploadPart file(String name, String path, String filename, String type) {
		return new UploadPart(name, HttpSupport.nonempty(path), null, filename, type);
	}

	static UploadPart bytes(String name, byte[] data, String filename, String type) {
		return new UploadPart(name, "", HttpSupport.bytes(data), filename, type);
	}

	UploadPart copy() {
		return new UploadPart(name, path, data == null ? null : data.clone(), filename, type);
	}

	/**
	 * 只查询来源长度，未知时返回 -1，不消耗内容流。
	 */
	private long length(Context context) throws IOException {
		if (data != null) {
			return data.length;
		}
		if (path.startsWith("content://")) {
			try (AssetFileDescriptor descriptor = context.getContentResolver().openAssetFileDescriptor(Uri.parse(path), "r")) {
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

	RequestBody body(final Context context) {
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
				try (InputStream input = data == null ? HttpSupport.open(context, path) : new ByteArrayInputStream(data)) {
					byte[] buffer = new byte[16384];
					int count;
					while ((count = input.read(buffer)) != -1) {
						sink.write(buffer, 0, count);
					}
				}
			}
		};
	}
}
