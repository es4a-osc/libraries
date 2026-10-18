package simple.library.wsd.mysql;

import simple.runtime.annotations.SimpleComponent;
import simple.runtime.annotations.SimpleObject;
import simple.runtime.annotations.SimpleFunction;
import simple.runtime.annotations.SimpleEvent;
import simple.runtime.annotations.UsesPermissions;
import simple.runtime.components.组件;
import simple.runtime.collections.集合;

/**
 * JDBC 直连组件，异步任务按提交顺序执行，事件在主线程返回。
 * SQL 片段由调用方构造，同步方法会阻塞线程。
 *
 * @author 树先生 xhwsd@qq.com
 */
@SimpleObject
@SimpleComponent
@UsesPermissions(
	permissionNames =
		// 访问网络套接字。
		"android.permission.INTERNET"
)
public interface MySQL数据库 extends 组件 {

	/* 组件事件 */

	/**
	 * 操作完成在主线程通知，逻辑参数为结果。
	 *
	 * @param succeeded 操作是否成功。
	 */
	@SimpleEvent
	void 连接数据库完毕(boolean succeeded);

	/**
	 * 操作完成在主线程通知；查询结果每项为文本数组，失败为空集合。
	 *
	 * @param result 查询结果集合，每项为文本数组；无记录或失败时为空集合。
	 */
	@SimpleEvent
	void 查询记录完毕(集合 result);

	/**
	 * 操作完成在主线程通知，逻辑参数为结果。
	 *
	 * @param succeeded 操作是否成功。
	 */
	@SimpleEvent
	void 修改记录完毕(boolean succeeded);

	/**
	 * 操作完成在主线程通知，逻辑参数为结果。
	 *
	 * @param succeeded 操作是否成功。
	 */
	@SimpleEvent
	void 添加记录完毕(boolean succeeded);

	/**
	 * 操作完成在主线程通知，逻辑参数为结果。
	 *
	 * @param succeeded 操作是否成功。
	 */
	@SimpleEvent
	void 删除记录完毕(boolean succeeded);

	/**
	 * 操作完成在主线程通知，逻辑参数为结果。
	 *
	 * @param succeeded 操作是否成功。
	 */
	@SimpleEvent
	void 执行语句完毕(boolean succeeded);

	/**
	 * 操作完成在主线程通知；查询结果每项为文本数组，失败为空集合。
	 *
	 * @param result 查询结果集合，每项为文本数组；无记录或失败时为空集合。
	 */
	@SimpleEvent
	void 执行查询类SQL完毕(集合 result);

	/**
	 * 操作完成在主线程通知，逻辑参数为结果。
	 *
	 * @param succeeded 操作是否成功。
	 */
	@SimpleEvent
	void 断开连接完毕(boolean succeeded);

	/* 组件方法 */

	/**
	 * 异步连接指定数据库，结果通过连接数据库完毕事件返回。
	 *
	 * @param address 数据库主机与端口，如 192.168.1.5:3306。
	 * @param username 登录用户名。
	 * @param password 登录密码。
	 * @param database 要连接的数据库名称。
	 */
	@SimpleFunction
	void 连接数据库(String address, String username, String password, String database);

	/**
	 * 连接 host:port 和指定数据库；重新连接先关闭旧连接，失败后保持未连接。 阻塞调用线程，勿在主线程使用。
	 *
	 * @param address 数据库主机与端口，如 192.168.1.5:3306。
	 * @param username 登录用户名。
	 * @param password 登录密码。
	 * @param database 要连接的数据库名称。
	 * @return 连接成功时返回真，参数无效或连接失败时返回假。
	 */
	@SimpleFunction
	boolean 连接数据库_同步(String address, String username, String password, String database);

	/**
	 * 查询指定表的全部记录及字段，不附加 WHERE 条件。
	 *
	 * <p>结果通过原有查询完成事件返回。</p>
	 *
	 * @param table 目标表名，须由调用方保证为合法 SQL 标识符。
	 */
	@SimpleFunction
	void 查询记录(String table);

	/**
	 * 按指定字段查询表内全部记录，不附加 WHERE 条件。
	 *
	 * <p>结果通过原有查询完成事件返回。</p>
	 *
	 * @param table 目标表名，须由调用方保证为合法 SQL 标识符。
	 * @param columns 查询或提取的字段列表，与完整方法的含义一致。
	 */
	@SimpleFunction
	void 查询记录(String table, String columns);

	/**
	 * 异步查询记录，结果通过查询记录完毕事件返回。
	 *
	 * @param table 目标表名，由调用方提供合法 SQL 标识符。
	 * @param columns 查询字段或 SQL 字段列表；不进行 SQL 转义。
	 * @param condition SQL 条件片段，不包含 WHERE 关键字；不进行 SQL 转义。
	 */
	@SimpleFunction
	void 查询记录(String table, String columns, String condition);

	/**
	 * 查询指定表的全部记录及字段，不附加 WHERE 条件。
	 *
	 * <p>阻塞调用线程，应在后台调用。</p>
	 *
	 * @param table 目标表名，须由调用方保证为合法 SQL 标识符。
	 * @return 查询结果集合；SQL NULL、空结果及失败行为与完整方法一致。
	 */
	@SimpleFunction
	集合 查询记录_同步(String table);

	/**
	 * 按指定字段查询表内全部记录，不附加 WHERE 条件。
	 *
	 * <p>阻塞调用线程，应在后台调用。</p>
	 *
	 * @param table 目标表名，须由调用方保证为合法 SQL 标识符。
	 * @param columns 查询或提取的字段列表，与完整方法的含义一致。
	 * @return 查询结果集合；SQL NULL、空结果及失败行为与完整方法一致。
	 */
	@SimpleFunction
	集合 查询记录_同步(String table, String columns);

	/**
	 * 查询返回集合，每项为文本数组；字段为空或 * 返回全部列，SQL NULL 为空文本，失败返回空集合。 阻塞调用线程，勿在主线程使用。
	 *
	 * @param table 目标表名，由调用方提供合法 SQL 标识符。
	 * @param columns 查询字段或 SQL 字段列表；不进行 SQL 转义。
	 * @param condition SQL 条件片段，不包含 WHERE 关键字；不进行 SQL 转义。
	 * @return 查询结果集合，每项为文本数组；SQL NULL 转为空文本，无记录或失败时为空集合。
	 */
	@SimpleFunction
	集合 查询记录_同步(String table, String columns, String condition);

	/**
	 * 异步执行查询 SQL，结果通过执行查询类SQL完毕事件返回。
	 *
	 * @param sql 完整 SQL 语句，由调用方保证语法及参数转义。
	 */
	@SimpleFunction
	void 执行查询类SQL(String sql);

	/**
	 * 查询返回集合，每项为文本数组；字段为空或 * 返回全部列，SQL NULL 为空文本，失败返回空集合。 阻塞调用线程，勿在主线程使用。
	 *
	 * @param sql 完整 SQL 语句，由调用方保证语法及参数转义。
	 * @return 查询结果集合，每项为文本数组；SQL NULL 转为空文本，无记录或失败时为空集合。
	 */
	@SimpleFunction
	集合 执行查询类SQL_同步(String sql);

	/**
	 * 异步执行 SQL 操作，结果通过修改记录完毕事件返回。
	 *
	 * @param table 目标表名，由调用方提供合法 SQL 标识符。
	 * @param assignments UPDATE 的字段赋值片段，不包含 SET 关键字。
	 * @param condition SQL 条件片段，不包含 WHERE 关键字；不进行 SQL 转义。
	 */
	@SimpleFunction
	void 修改记录(String table, String assignments, String condition);

	/**
	 * 执行调用方提供的 SQL 或 SQL 片段；执行成功（包括影响 0 行）返回真。 阻塞调用线程，勿在主线程使用。
	 *
	 * @param table 目标表名，由调用方提供合法 SQL 标识符。
	 * @param assignments UPDATE 的字段赋值片段，不包含 SET 关键字。
	 * @param condition SQL 条件片段，不包含 WHERE 关键字；不进行 SQL 转义。
	 * @return SQL 执行成功时返回真，包括影响 0 行的情况；失败时返回假。
	 */
	@SimpleFunction
	boolean 修改记录_同步(String table, String assignments, String condition);

	/**
	 * 异步执行 SQL 操作，结果通过添加记录完毕事件返回。
	 *
	 * @param table 目标表名，由调用方提供合法 SQL 标识符。
	 * @param columns 查询字段或 SQL 字段列表；不进行 SQL 转义。
	 * @param values INSERT 的值列表，字符串须由调用方正确转义并加引号。
	 */
	@SimpleFunction
	void 添加记录(String table, String columns, String values);

	/**
	 * 执行调用方提供的 SQL 或 SQL 片段；执行成功（包括影响 0 行）返回真。 阻塞调用线程，勿在主线程使用。
	 *
	 * @param table 目标表名，由调用方提供合法 SQL 标识符。
	 * @param columns 查询字段或 SQL 字段列表；不进行 SQL 转义。
	 * @param values INSERT 的值列表，字符串须由调用方正确转义并加引号。
	 * @return SQL 执行成功时返回真，包括影响 0 行的情况；失败时返回假。
	 */
	@SimpleFunction
	boolean 添加记录_同步(String table, String columns, String values);

	/**
	 * 异步执行 SQL 操作，结果通过删除记录完毕事件返回。
	 *
	 * @param table 目标表名，由调用方提供合法 SQL 标识符。
	 * @param condition SQL 条件片段，不包含 WHERE 关键字；不进行 SQL 转义。
	 */
	@SimpleFunction
	void 删除记录(String table, String condition);

	/**
	 * 执行调用方提供的 SQL 或 SQL 片段；执行成功（包括影响 0 行）返回真。 阻塞调用线程，勿在主线程使用。
	 *
	 * @param table 目标表名，由调用方提供合法 SQL 标识符。
	 * @param condition SQL 条件片段，不包含 WHERE 关键字；不进行 SQL 转义。
	 * @return SQL 执行成功时返回真，包括影响 0 行的情况；失败时返回假。
	 */
	@SimpleFunction
	boolean 删除记录_同步(String table, String condition);

	/**
	 * 异步执行 SQL 操作，结果通过执行语句完毕事件返回。
	 *
	 * @param sql 完整 SQL 语句，由调用方保证语法及参数转义。
	 */
	@SimpleFunction
	void 执行语句(String sql);

	/**
	 * 执行调用方提供的 SQL 或 SQL 片段；执行成功（包括影响 0 行）返回真。 阻塞调用线程，勿在主线程使用。
	 *
	 * @param sql 完整 SQL 语句，由调用方保证语法及参数转义。
	 * @return SQL 执行成功时返回真，包括影响 0 行的情况；失败时返回假。
	 */
	@SimpleFunction
	boolean 执行语句_同步(String sql);

	/**
	 * 异步执行 SQL 操作，结果通过断开连接完毕事件返回。
	 */
	@SimpleFunction
	void 断开连接();

	/**
	 * 释放当前连接，可重复调用。 阻塞调用线程，勿在主线程使用。
	 *
	 * @return 连接已关闭或原本没有连接时返回真，关闭失败时返回假。
	 */
	@SimpleFunction
	boolean 断开连接_同步();
}
