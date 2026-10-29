# AgentWeb浏览器库

基于 AgentWeb 的增强网页浏览器扩展库，可嵌入 Simple 窗口，支持导航、进度及错误、JavaScript 消息、Cookie、文件选择上传、指定位置下载和网页视频全屏。

使用 JDK 11、Android API 30 编译，最低 API 24，采用 AndroidX；宿主主题使用 AppCompat 主题。所有组件操作在界面线程调用。切换窗口时暂停浏览、退出全屏、取消文件选择和本实例未完成的下载；销毁后不再派发异步事件。

文件上传通过系统文档选择器返回 URI，支持单选、多选和取消，不将 URI 当作物理路径；不提供直接拍照、录像或网页摄像头、麦克风、定位授权。下载仅接受 HTTP/HTTPS，由应用响应下载请求并明确指定未存在的目标文件，不自动打开文件；取消或失败可能留下部分文件。

JavaScript 消息接口需要显式注册，只暴露 `postMessage(text)`；仅向可信内容注册。Cookie 和 WebView 缓存为应用内共享数据，清除会影响其它浏览框；下载不自动携带 WebView Cookie。页面导航不打开外部应用协议。证书错误拒绝继续，HTTPS 页面不加载明文子资源。视频全屏沿用宿主方向，退出时恢复原系统栏状态。

## 依赖来源

- [AgentWeb core 5.0.6 AndroidX](https://github.com/Justson/AgentWeb/tree/v5.0.6-androidx)
- [Downloader 5.0.4 AndroidX](https://github.com/Justson/Downloader/tree/v5.0.4-androidx)
- [AndroidX AppCompat 1.3.1](https://developer.android.com/jetpack/androidx/releases/appcompat)
- [AndroidX Legacy 1.0.0](https://developer.android.com/jetpack/androidx/releases/legacy)
- [Material Components 1.4.0](https://github.com/material-components/material-components-android/tree/1.4.0)
- [Tabler Icons](https://github.com/tabler/tabler-icons)
