# XHttp2网络库

将 XHttp2 的请求、重试和缓存能力接入 Simple，提供独立客户端、可复用请求、响应快照、上传下载、Cookie、证书配置和批量请求。类型位于 `simple.library.wsd.xhttp2`。

使用 JDK 11、Android API 30 编译，最低 API 24，统一采用 AndroidX。客户端在所属窗口显示后使用，事件回到界面线程；同步发送只能在工作线程调用。离开窗口会取消请求，销毁后不可复用。

请求发送时保存独立配置。缓存双发策略可能产生两次响应，但请求结束只通知一次。内存响应默认最多 8 MiB，大文件使用绝对下载路径；下载通过临时文件和原子替换提交，失败或取消保留已有目标，已经提交的下载不再接受取消。文件及 `content` URI 必须可读，URI 授权由调用方取得。

默认客户端会话独立，明确指定同一会话名称才共享 Cookie 和缓存；Cookie 的持久化需显式启用。默认采用系统证书和严格主机名校验，自定义 CA 不会关闭主机名验证。证书来源支持绝对路径、`asset://` 及已授权的 `content` URI。

本地 XHttp2 AAR 仅移除固定权限声明，代码和其它条目保持上游原版；网络权限由客户端类型声明，不附加存储或挂载权限。

## 依赖来源

- [XHttp2 2.0.4](https://github.com/xuexiangjys/XHttp2/tree/2.0.4)
- [OkHttp 3.14.9](https://github.com/square/okhttp/tree/parent-3.14.9)
- [Retrofit 2.9.0](https://github.com/square/retrofit/tree/parent-2.9.0)
- [RxJava 2.2.20](https://github.com/ReactiveX/RxJava/tree/v2.2.20)
- [RxAndroid 2.1.1](https://github.com/ReactiveX/RxAndroid/tree/v2.1.1)
- [Gson 2.8.5](https://github.com/google/gson/tree/gson-parent-2.8.5)
- [DiskLruCache 2.0.2](https://github.com/JakeWharton/DiskLruCache)
- [AndroidX AppCompat 1.1.0](https://developer.android.com/jetpack/androidx/releases/appcompat)
- [Tabler Icons](https://github.com/tabler/tabler-icons)

[运行示例](example/project.properties)：用 ES4A 打开示例工程并构建运行。
