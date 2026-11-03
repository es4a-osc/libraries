# MsSQL数据库类库

基于 [jTDS](https://jtds.sourceforge.net/) 封装，通过 JDBC 直连 SQL Server，提供同步查询、写入及异步结果事件。

需要可直接访问的服务器，认证与 TLS 能力受随库 jTDS 驱动限制。同步调用应在后台执行；查询中的 SQL NULL 转为空文本，失败返回空集合。

## 依赖来源

- [jTDS 1.2.7](https://jtds.sourceforge.net/)
