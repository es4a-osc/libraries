# MQTT通讯库

提供非可视组件 **MQTT通讯**：MQTT 发布、订阅与连接事件。

- `src/`：公开注解接口与实现；`library.json`：ES4A 元数据；`classes.jar`：构建产物。
- `libs/`：Paho MQTT 1.2.0、Paho Android Service 1.1.1、Support v4 23.2.1。
- `icons/`：适配 currentColor 的 SVG；`sample/`：[可复现的 Simple 项目与操作步骤](sample/README.md)。

支持 `tcp://`、`ssl://`；一个组件维护一条连接。连接中、已连接或正在清理时再次连接会通知连接失败；先断开，收到断开事件后再连接。`关闭服务` 永久关闭本组件；销毁时释放连接和服务绑定。保留原 Paho Android 后台服务，其后台保活及新系统行为需真机验证。`取IMEI码` 是兼容名称，受限时返回 ANDROID_ID 或本应用安装标识，不保证为 IMEI；多个客户端必须使用不同 ID。

在类库仓库运行 `build.bat "simple.library.wsd.mqtt"` 构建；需要安装时自行运行 `release.bat "simple.library.wsd.mqtt"`，再在 ES4A 选择相应 SDK 入口并刷新类库。本次未安装或注册到 SDK。

核对日期：2026-10-15。已验证 Java 构建、公开注解与清单、扩展元数据消费和样例 APK 编译；尚未完成真实扩展宿主可见交互和 Android 真机验证，编译成功不代表硬件、网络、后台或数据库兼容性已经通过。

通用开发与清单规则见 [SDK 文档](https://gitee.com/es4a/sdk/tree/main/docs)。

若 UTF-8 终端下 JDK 8 报“找不到”中文文件名，在当前 PowerShell 进程设置 `$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8"` 后重新构建；无需改源码文件名。
