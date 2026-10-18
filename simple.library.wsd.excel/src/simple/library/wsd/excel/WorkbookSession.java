package simple.library.wsd.excel;

import java.io.File;
import java.io.IOException;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * 拥有一份 XLS 写入会话和一份读取会话，所有工作簿都在本边界释放。
 */
final class WorkbookSession {

	private WritableWorkbook writer;
	private WritableSheet output;
	private Workbook reader;
	private Sheet input;

	/* 会话操作 */

	synchronized boolean create(String name, String path) {
		if (writer != null || name == null || path == null || !path.toLowerCase(java.util.Locale.ROOT).endsWith(".xls")) return false;

		try {

			// 写入会话独立持有工作簿；失败时统一关闭并清空引用。
			writer = Workbook.createWorkbook(new File(path));
			output = writer.createSheet(name, 0);
			return true;
		} catch (IOException | RuntimeException exception) {
			closeWriter();
			return false;
		}
	}

	synchronized boolean add(int row, int column, String text) {
		if (output == null || !cell(row, column)) return false;

		try {
			output.addCell(new Label(column, row, text == null ? "" : text));
			return true;
		} catch (WriteException | RuntimeException exception) {
			return false;
		}
	}

	synchronized boolean merge(int startRow, int startColumn, int endRow, int endColumn) {
		if (output == null || !cell(startRow, startColumn) || !cell(endRow, endColumn)
			|| endRow < startRow || endColumn < startColumn) return false;
		try {
			output.mergeCells(startColumn, startRow, endColumn, endRow);
			return true;
		} catch (WriteException | RuntimeException exception) {
			return false;
		}
	}

	synchronized boolean write() {
		if (writer == null) return false;

		try {
			writer.write();
			return closeWriter();
		} catch (IOException | RuntimeException exception) {
			closeWriter();
			return false;
		}
	}

	synchronized boolean open(String path) {
		closeReader();
		if (path == null) return false;

		try {
			reader = Workbook.getWorkbook(new File(path));
			if (reader.getNumberOfSheets() == 0) {
				closeReader();
				return false;
			}

			// 读取接口只操作第一张工作表。
			input = reader.getSheet(0);
			return true;
		} catch (IOException | BiffException | RuntimeException exception) {
			closeReader();
			return false;
		}
	}

	synchronized int rows() {
		return input == null ? 0 : input.getRows();
	}

	synchronized int columns() {
		return input == null ? 0 : input.getColumns();
	}

	synchronized String read(int row, int column) {
		return input == null || row < 0 || column < 0 || row >= rows() || column >= columns()
		? "" : input.getCell(column, row).getContents();
	}

	synchronized void close() {
		closeWriter();
		closeReader();
	}

	/* 内部辅助 */

	private static boolean cell(int row, int column) {
		return row >= 0 && row < 65536 && column >= 0 && column < 256;
	}

	private boolean closeWriter() {
		// 先清空写入会话引用，再关闭工作簿，关闭失败也不复用失效状态。
		WritableWorkbook previous = writer;
		writer = null;
		output = null;
		if (previous == null) return true;

		try {
			previous.close();
			return true;
		}
		catch (IOException | WriteException | RuntimeException exception) {
			return false;
		}
	}

	private void closeReader() {
		input = null;
		if (reader != null) {
			reader.close();
			reader = null;
		}
	}
}
