# XH2网络库

基于 [XHttp2](https://github.com/xuexiangjys/XHttp2) 封装，提供网络请求、重试、缓存、Cookie、证书配置、上传下载和批量请求。

需要 Android 7.0（API 24）及以上及 AndroidX。客户端在所属窗口显示后使用，离开窗口会取消请求；同步发送须在工作线程执行。

内存响应默认最多 8 MiB，大文件应下载至文件。文件与文档 URI 须具备读取授权。缓存策略可能产生两次响应；Cookie 默认按会话隔离，证书与主机名默认严格校验。

## 依赖来源

- [XHttp2 2.0.4](https://github.com/xuexiangjys/XHttp2/tree/2.0.4)
