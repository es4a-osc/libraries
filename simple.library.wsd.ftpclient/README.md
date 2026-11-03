# FTP客户端

基于 [Apache Commons Net](https://commons.apache.org/proper/commons-net/) 和 [JSch](https://github.com/mwiede/jsch) 封装，提供 FTP/SFTP 连接、文件管理、上传下载、断点续传，以及 FTP 数据流和列表分页。

SFTP 需要 Android 8.0 及以上，连接前须核实服务器公钥。同步网络与流操作应在后台线程串行执行，打开的数据流须显式关闭。

取消传输会断开连接；断点不支持进程重启恢复，续传要求已有文件前缀与源文件一致。目录传输要求目标不存在，失败可能留下部分内容。

## 依赖来源

- [Apache Commons Net 3.6](https://commons.apache.org/proper/commons-net/)
- [维护版 JSch 2.28.8](https://github.com/mwiede/jsch)
