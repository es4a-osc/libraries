package simple.library.wsd.downloader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import simple.library.wsd.downloader.internal.DownloadEngine;

/**
 * 使用应用私有 SQLite 保存下载记录，按稳定任务编号读取和更新。
 * 文件大小和断点通过 long 存取；任务代次仅用于进程内回调校验，不写入数据库。
 *
 * @author 树先生 xhwsd@qq.com
 */
final class DownloadStore extends SQLiteOpenHelper implements DownloadEngine.Store {

	DownloadStore(Context context) {
		super(context, "simple.library.wsd.downloader.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE downloads (id INTEGER PRIMARY KEY, url TEXT NOT NULL, "
			+ "path TEXT NOT NULL, state INTEGER NOT NULL, total INTEGER NOT NULL, "
			+ "done INTEGER NOT NULL, etag TEXT NOT NULL, modified TEXT NOT NULL, error TEXT NOT NULL)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		throw new SQLiteException("不支持的下载记录版本。");
	}

	@Override
	public synchronized List<DownloadEngine.Record> load() throws IOException {
		Cursor cursor = null;

		try {
			cursor = getReadableDatabase().query("downloads", null, null, null, null, null, "id");
			List<DownloadEngine.Record> result = new ArrayList<DownloadEngine.Record>();

			while (cursor.moveToNext()) {
				DownloadEngine.Record record = new DownloadEngine.Record();
				record.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
				record.url = cursor.getString(cursor.getColumnIndexOrThrow("url"));
				record.path = cursor.getString(cursor.getColumnIndexOrThrow("path"));
				record.state = cursor.getInt(cursor.getColumnIndexOrThrow("state"));
				record.total = cursor.getLong(cursor.getColumnIndexOrThrow("total"));
				record.done = cursor.getLong(cursor.getColumnIndexOrThrow("done"));
				record.etag = cursor.getString(cursor.getColumnIndexOrThrow("etag"));
				record.modified = cursor.getString(cursor.getColumnIndexOrThrow("modified"));
				record.error = cursor.getString(cursor.getColumnIndexOrThrow("error"));
				result.add(record);
			}

			return result;
		} catch (SQLiteException e) {
			throw new IOException("无法读取下载记录。", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	@Override
	public synchronized void save(DownloadEngine.Record record) throws IOException {
		try {
			ContentValues values = new ContentValues();
			values.put("id", record.id);
			values.put("url", record.url);
			values.put("path", record.path);
			values.put("state", record.state);
			values.put("total", record.total);
			values.put("done", record.done);
			values.put("etag", record.etag);
			values.put("modified", record.modified);
			values.put("error", record.error);
			if (getWritableDatabase().insertWithOnConflict("downloads", null, values, SQLiteDatabase.CONFLICT_REPLACE) == -1) {
				throw new SQLiteException("写入失败。");
			}
		} catch (SQLiteException e) {
			throw new IOException("无法保存下载记录。", e);
		}
	}
}
