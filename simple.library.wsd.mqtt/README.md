# MQTT通讯库

提供“MQTT通讯”非可视组件，支持消息发布、主题订阅和连接事件，服务器地址可使用 `tcp://` 或 `ssl://`。

一个组件维护一条连接，每个客户端须使用不同的客户端 ID。重新连接前先断开，收到断开事件后再发起连接；`关闭服务` 会永久关闭当前组件。

`取IMEI码` 在系统限制设备标识读取时可能返回 ANDROID_ID 或应用安装标识，不能将其视为固定的 IMEI。后台服务运行受 Android 系统限制，不保证应用退出后的持续连接。

## 依赖来源

- [Eclipse Paho Java Client 1.2.0](https://github.com/eclipse-paho/paho.mqtt.java)
- [Paho Android Service 1.1.1](https://github.com/eclipse-paho/paho.mqtt.android)：使用 AndroidX LocalBroadcastManager 的本地适配包，保留该版本接口。
- [AndroidX LocalBroadcastManager 1.0.0](https://developer.android.com/jetpack/androidx/releases/localbroadcastmanager)：Paho 服务使用的进程内广播。

[运行示例](example/project.properties)：用 ES4A 打开示例工程并构建运行。
