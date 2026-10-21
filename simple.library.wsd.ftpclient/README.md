# FTP客户端

提供“FTP客户端”非可视组件，支持 FTP、SFTP 登录、远程文件管理，以及同步和后台文件传输。

默认使用 FTP，采用被动模式、二进制传输和 GBK 控制编码。使用 SFTP 时设置协议、服务器、端口和账号密码；SFTP 要求 Android 8.0 及以上。

SFTP 首次连接须核实并确认服务器公钥，后续连接自动校验已保存的记录；公钥变化时需重新确认。也可指定可信的服务器公钥指纹进行严格校验。

同步网络操作须在后台线程调用。一个组件同时处理一项后台传输，通过完成事件取得结果。下载会覆盖同名文件，递归传输失败时可能留下部分内容。

> 注意：目前 D8 3.3.20 暂不兼容随库的 JSch 依赖，引用本库的项目会在 DEX 转换阶段构建失败。

## 依赖来源

- [Apache Commons Net 3.0.1](https://commons.apache.org/proper/commons-net/)：FTP 支持。
- [维护版 JSch 2.28.8](https://github.com/mwiede/jsch)：SFTP 支持，使用 Android 适配版本。
