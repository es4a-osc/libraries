package simple.library.wsd.excel;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.components.组件;

/**
 * 基于 JExcelAPI 读写 Excel 97-2003 .xls 的第一张工作表，不支持 .xlsx。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
@SimpleComponent
public interface EXCEL操作 extends 组件 {

	/* 组件方法 */

	/**
	 * 新建并覆盖目标 .xls 文件，创建第一张工作表；已有未写出会话时拒绝。成功后添加单元格并调用写出工作表。
	 *
	 * @param table 新建的第一张工作表名称。
	 * @param fileName 工作簿文件路径；相对路径以应用私有文件目录为基准。
	 * @return 当前写入会话完成此操作时返回真，参数无效或写入失败时返回假。
	 */
	@SimpleFunction
	boolean 添加工作表(String table, String fileName);

	/**
	 * 向当前写入工作表添加文本单元格，参数顺序为行、列，索引从 0 开始。
	 *
	 * @param row 零基行索引，XLS 有效范围为 0 至 65535。
	 * @param column 零基列索引，XLS 有效范围为 0 至 255。
	 * @param title 要写入单元格的文本，空引用按空文本处理。
	 * @return 当前写入会话完成此操作时返回真，参数无效或写入失败时返回假。
	 */
	@SimpleFunction
	boolean 添加单元格(int row, int column, String title);

	/**
	 * 按起始行、起始列、结束行、结束列合并区域，索引从 0 开始。
	 *
	 * @param startRow 合并区域的起始行，零基索引。
	 * @param startColumn 合并区域的起始列，零基索引。
	 * @param endRow 合并区域的结束行，不能小于起始行。
	 * @param endColumn 合并区域的结束列，不能小于起始列。
	 * @return 当前写入会话完成此操作时返回真，参数无效或写入失败时返回假。
	 */
	@SimpleFunction
	boolean 合并单元格(int startRow, int startColumn, int endRow, int endColumn);

	/**
	 * 写出并关闭当前工作簿，成功返回真；失败返回假并释放写入资源。
	 *
	 * @return 写入并关闭工作簿均成功时返回真，否则返回假。
	 */
	@SimpleFunction
	boolean 写出工作表();

	/**
	 * 打开 .xls 文件的第一张工作表；失败返回假并清除之前的读取状态。
	 *
	 * @param fileName 工作簿文件路径；相对路径以应用私有文件目录为基准。
	 * @return 已打开第一张工作表时返回真，否则返回假。
	 */
	@SimpleFunction
	boolean 读取工作表(String fileName);

	/**
	 * 返回当前读取工作表行数，未打开返回 0。
	 *
	 * @return 当前读取工作表的行数，未打开时为 0。
	 */
	@SimpleFunction
	int 读取总行数();

	/**
	 * 返回当前读取工作表列数，未打开返回 0。
	 *
	 * @return 当前读取工作表的列数，未打开时为 0。
	 */
	@SimpleFunction
	int 读取总列数();

	/**
	 * 按行、列读取格式化文本，索引从 0 开始；未打开或越界返回空文本。
	 *
	 * @param row 零基行索引，XLS 有效范围为 0 至 65535。
	 * @param column 零基列索引，XLS 有效范围为 0 至 255。
	 * @return 格式化的单元格文本；未打开工作表或索引越界时为空文本。
	 */
	@SimpleFunction
	String 读取单元格(int row, int column);
}
