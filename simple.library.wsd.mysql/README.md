# MySQL数据库

基于 [MySQL Connector/J](https://github.com/mysql/mysql-connector-j) 封装，通过 JDBC 直连 MySQL，提供同步查询、写入及异步结果事件。

需要可直接访问的服务器，认证、字符集与 TLS 能力受随库驱动限制。同步调用应在后台执行；查询中的 SQL NULL 转为空文本，失败返回空集合。

## 依赖来源

- [MySQL Connector/J 5.1.36](https://repo.maven.apache.org/maven2/mysql/mysql-connector-java/5.1.36/)
