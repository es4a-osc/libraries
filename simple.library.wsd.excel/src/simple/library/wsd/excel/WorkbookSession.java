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

	/**
	 * 创建可写工作簿并准备输出位置。
	 *
	 * @param name 要操作的名称
	 * @param path 文件或目录路径
	 * @return 操作成功时为 {@code true}，失败时为 {@code false}
	 */
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

	/**
	 * 向工作簿指定单元格写入文本。
	 *
	 * @param row 行索引，从 0 开始
	 * @param column 列索引，从 0 开始
	 * @param text 待处理文本
	 * @return 操作成功时为 {@code true}，失败时为 {@code false}
	 */
	synchronized boolean add(int row, int column, String text) {
		if (output == null || !cell(row, column)) return false;

		try {
			output.addCell(new Label(column, row, text == null ? "" : text));
			return true;
		} catch (WriteException | RuntimeException exception) {
			return false;
		}
	}

	/**
	 * 合并指定的行列区域。
	 *
	 * @param startRow 起始行索引
	 * @param startColumn 起始列索引
	 * @param endRow 结束行索引
	 * @param endColumn 结束列索引
	 * @return 操作成功时为 {@code true}，失败时为 {@code false}
	 */
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

	/**
	 * 将可写工作簿内容写入输出文件。
	 *
	 * @return 操作成功时为 {@code true}，失败时为 {@code false}
	 */
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

	/**
	 * 打开现有工作簿用于读取。
	 *
	 * @param path 文件或目录路径
	 * @return 操作成功时为 {@code true}，失败时为 {@code false}
	 */
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

	/**
	 * 返回当前工作表的数据行数。
	 *
	 * @return 数据行数或行数据
	 */
	synchronized int rows() {
		return input == null ? 0 : input.getRows();
	}

	/**
	 * 返回当前工作表的数据列数。
	 *
	 * @return 数据列数
	 */
	synchronized int columns() {
		return input == null ? 0 : input.getColumns();
	}

	/**
	 * 读取指定单元格文本。
	 *
	 * @param row 行索引，从 0 开始
	 * @param column 列索引，从 0 开始
	 * @return 读取到的数据
	 */
	synchronized String read(int row, int column) {
		return input == null || row < 0 || column < 0 || row >= rows() || column >= columns()
		? "" : input.getCell(column, row).getContents();
	}

	/**
	 * 关闭当前对象持有的资源。
	 */
	synchronized void close() {
		closeWriter();
		closeReader();
	}

	/* 内部辅助 */

	/**
	 * 检查行列索引是否位于 XLS 工作表支持的范围内。
	 *
	 * @param row 行索引，从 0 开始
	 * @param column 列索引，从 0 开始
	 * @return 行索引在 0 至 65535、列索引在 0 至 255 范围内时为真。
	 */
	private static boolean cell(int row, int column) {
		return row >= 0 && row < 65536 && column >= 0 && column < 256;
	}

	/**
	 * 关闭可写工作簿并清除写入状态。
	 *
	 * @return 操作成功时为 {@code true}，失败时为 {@code false}
	 */
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

	/**
	 * 关闭只读工作簿并清除读取状态。
	 */
	private void closeReader() {
		input = null;
		if (reader != null) {
			reader.close();
			reader = null;
		}
	}
}
