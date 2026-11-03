# MQTT客户端

基于 [Eclipse Paho Java Client](https://github.com/eclipse-paho/paho.mqtt.java) 封装，提供 MQTT 3.1/3.1.1 异步连接、发布订阅、离线缓冲、遗嘱和 TLS 配置，支持 `tcp://` 与 `ssl://`。

需要 Android 7.0（API 24）及以上及可访问的 Broker，组件与事件在主线程使用。持久会话须使用稳定且唯一的客户端 ID，补发消息可能重复；不保证系统休眠或应用退出后持续连接。TLS 默认校验证书和主机名。

## 依赖来源

- [Eclipse Paho Java Client 1.2.5](https://github.com/eclipse-paho/paho.mqtt.java/tree/v1.2.5)
