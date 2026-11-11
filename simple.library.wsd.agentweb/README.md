# AW浏览器库

基于 [AgentWeb](https://github.com/Justson/AgentWeb) 和 [Downloader](https://github.com/Justson/Downloader) 封装，提供嵌入式网页浏览、数据加载、请求头、JavaScript 交互、Cookie、文件上传下载和视频全屏。

需要 Android 7.0（API 24）及以上、AndroidX 和 AppCompat 主题，组件在界面线程使用。切换窗口会暂停浏览并取消未完成的文件选择和下载。

上传使用系统文档选择器，不提供网页摄像头、麦克风或定位授权。下载仅接受 HTTP/HTTPS，取消或失败可能留下部分文件。JavaScript 接口仅向可信页面开放；Cookie 与 WebView 缓存由应用内共享，下载不自动携带页面 Cookie。

## 依赖来源

- [AgentWeb core 5.0.6 AndroidX](https://github.com/Justson/AgentWeb/tree/v5.0.6-androidx)
- [Downloader 5.0.4 AndroidX](https://github.com/Justson/Downloader/tree/v5.0.4-androidx)
