# MySQL数据库

提供“MySQL数据库”非可视组件，通过 JDBC 直连 MySQL，支持同步查询、写入及异步结果事件。

同步操作会阻塞调用线程；异步任务按提交顺序执行，结果事件回到主线程。查询结果按行返回文本数组，SQL NULL 转为空文本，失败返回空集合；写入执行成功返回真，包括影响 0 行的情况。

需要能够直接访问 MySQL。认证、字符集和 TLS 能力受随库驱动限制，应与目标服务器配置匹配。

## 依赖来源

- [MySQL Connector/J 5.1.36](https://repo.maven.apache.org/maven2/mysql/mysql-connector-java/5.1.36/)

[运行示例](example/project.properties)：用 ES4A 打开示例工程并构建运行。
