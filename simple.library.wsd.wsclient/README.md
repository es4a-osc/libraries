# WebSocket客户端库

基于 [OkHttp](https://github.com/square/okhttp) 封装，提供 `ws://`、`wss://` 连接、文本消息收发和连接状态事件。

仅支持文本消息，`wss://` 使用系统信任的证书校验。服务器须可从设备访问，再次打开连接会替换旧连接。

## 依赖来源

- [OkHttp 3.9.0](https://github.com/square/okhttp)
- [Okio 1.13.0](https://github.com/square/okio)
