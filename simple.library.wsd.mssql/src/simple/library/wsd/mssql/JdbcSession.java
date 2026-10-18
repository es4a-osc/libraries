package simple.library.wsd.mssql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 连接与结果集的唯一所有者，同一连接上的操作串行执行。
 */
final class JdbcSession {

	private Connection connection;
	private boolean disposed;

	/* 会话操作 */

	/**
	 * 建立底层连接。
	 *
	 * @param host 服务器地址
	 * @param user 认证用户名
	 * @param password 认证密码
	 * @param database 数据库名称
	 * @return 操作成功时为 {@code true}，失败时为 {@code false}
	 */
	synchronized boolean connect(String host, String user, String password, String database) {
		if (disposed || !disconnect() || host == null || host.trim().isEmpty() || database == null || database.isEmpty()) return false;

		try {
			Class.forName("net.sourceforge.jtds.jdbc.Driver");

			// 配置登录和超时参数，所有连接都由当前会话负责关闭。
			Properties properties = new Properties();
			properties.setProperty("user", user == null ? "" : user);
			properties.setProperty("password", password == null ? "" : password);
			properties.setProperty("loginTimeout", "15");
			properties.setProperty("socketTimeout", "15");

			// 连接成功后才保存 JDBC 连接。
			connection = DriverManager.getConnection("jdbc:jtds:sqlserver://" + host + "/" + database, properties);
			return true;
		} catch (SQLException | ClassNotFoundException | RuntimeException exception) {
			return false;
		}
	}

	/**
	 * 执行 SQL 查询并返回结果数据。
	 *
	 * @param sql 要执行的 SQL 语句
	 * @param labels 要读取的列名数组；为空引用时按结果集中全部列的顺序读取。
	 * @return 按行保存的文本数组列表；未连接或查询失败时返回空列表。
	 */
	synchronized List<String[]> query(String sql, String[] labels) {
		List<String[]> rows = new ArrayList<String[]>();
		if (disposed || connection == null) return rows;

		try (Statement statement = connection.createStatement()) {
			statement.setQueryTimeout(15);

			try (ResultSet result = statement.executeQuery(sql)) {
				int count = labels == null ? result.getMetaData().getColumnCount() : labels.length;

				// 按列顺序复制结果；SQL NULL 统一表示为空文本。
				while (result.next()) {
					String[] row = new String[count];

					for (int index = 0; index < count; index++) {
						String value = labels == null ? result.getString(index + 1) : result.getString(labels[index]);
						row[index] = value == null ? "" : value;
					}

					rows.add(row);
				}
			}
		} catch (SQLException | RuntimeException exception) {
			rows.clear();
		}

		return rows;
	}

	/**
	 * 执行 SQL 更新语句并按受影响行数判断结果。
	 *
	 * @param sql 要执行的 SQL 语句
	 * @return 至少影响一行时为真；没有影响行、连接不可用或执行异常时为假。
	 */
	synchronized boolean execute(String sql) {
		if (disposed || connection == null) return false;

		try (Statement statement = connection.createStatement()) {
			statement.setQueryTimeout(15);
			return statement.executeUpdate(sql) > 0;
		} catch (SQLException | RuntimeException exception) {
			return false;
		}
	}

	/**
	 * 释放连接、线程及按要求移除生命周期监听器。
	 */
	synchronized void dispose() {
		disposed = true;
		disconnect();
	}

	/**
	 * 关闭当前连接并清理连接状态。
	 *
	 * @return 操作成功时为 {@code true}，失败时为 {@code false}
	 */
	synchronized boolean disconnect() {
		if (connection == null) return true;

		try {
			connection.close();
			connection = null;
			return true;
		}
		catch (SQLException | RuntimeException exception) {
			return false;
		}
	}
}
