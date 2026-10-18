# WebSocket客户端库

提供不可视组件 **WS客户端**，支持 `ws://`、`wss://` 连接和文本消息收发，由原 WS客户端类库移植到 Simple。

- `src/`：组件接口与 Android 实现。
- `library.json`、`classes.jar`：清单与编译产物。
- `libs/`：原版 OkHttp 3.9.0、Okio 1.13.0 及许可说明。
- `icons/`：Tabler `plug-connected` 图标及 MIT 许可。
- `sample/`：连接、发送、关闭和四个事件的 Simple 样例，参考原 WS客户端示例。

在类库仓库运行 `build.bat "simple.library.wsd.wsclient"` 构建，再通过 `release.bat "simple.library.wsd.wsclient"` 安装到同级 SDK，并在 ES4A 中选择对应 SDK 清单。

打开样例项目，将地址改为实际 WebSocket 服务地址后连接；收到“连接成功”再发送文本。手机连接电脑服务时使用电脑的局域网地址。`wss://` 使用系统信任的证书校验。

请求头在连接前添加或删除，修改不影响现有连接。再次打开连接会替换旧连接；正常关闭触发“连接断开”，连接或发送失败触发“出现错误”。事件在主线程执行，组件销毁时释放连接。仅支持文本消息，保留原来的无参数错误事件。

通用开发和清单说明见 [SDK 文档](https://gitee.com/es4a/sdk/tree/main/docs)。
