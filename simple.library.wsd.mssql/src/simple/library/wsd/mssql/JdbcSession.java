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

	synchronized boolean execute(String sql) {
		if (disposed || connection == null) return false;

		try (Statement statement = connection.createStatement()) {
			statement.setQueryTimeout(15);
			return statement.executeUpdate(sql) > 0;
		} catch (SQLException | RuntimeException exception) {
			return false;
		}
	}

	synchronized void dispose() {
		disposed = true;
		disconnect();
	}

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
