package simple.library.wsd.xhttp2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.OpenableColumns;

/**
 * 仅输出合成测试数据的管道提供者，保留未知长度以实测分块上传和取消。
 * 辅助包与正式应用不同 UID，测试必须经过 ContentResolver 的真实跨进程文件描述符。
 *
 * @author 树先生 xhwsd@qq.com
 */
public final class UnknownLengthProvider extends ContentProvider {

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public String getType(Uri uri) {
		return "application/octet-stream";
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] args, String order) {
		MatrixCursor result = new MatrixCursor(new String[] {OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE});
		result.addRow(new Object[] {"synthetic.bin", null});
		return result;
	}

	@Override
	public ParcelFileDescriptor openFile(final Uri uri, String mode) throws FileNotFoundException {
		if (!"r".equals(mode)) {
			throw new FileNotFoundException("测试来源只读。");
		}
		try {
			final ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
			Thread writer = new Thread(new Runnable() {
				@Override
				public void run() {
					try (OutputStream output = new ParcelFileDescriptor.AutoCloseOutputStream(pipe[1])) {
						if ("/slow".equals(uri.getPath())) {
							byte[] block = new byte[8192];
							for (int index = 0; index < 512; index++) {
								output.write(block);
								SystemClock.sleep(10);
							}
						} else {
							output.write("未知长度中文来源".getBytes(StandardCharsets.UTF_8));
						}
					} catch (IOException expected) {
						// 查询长度或取消上传会关闭读端，管道写端随之退出，不保留后台循环。
					}
				}
			}, "xhttp2-test-content");
			writer.setDaemon(true);
			writer.start();
			return pipe[0];
		} catch (IOException failure) {
			throw new FileNotFoundException(failure.toString());
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException("测试来源只读。");
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] args) {
		throw new UnsupportedOperationException("测试来源只读。");
	}

	@Override
	public int delete(Uri uri, String selection, String[] args) {
		throw new UnsupportedOperationException("测试来源只读。");
	}
}
