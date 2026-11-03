# XU更新库

基于 [XUpdate](https://github.com/xuexiangjys/XUpdate) 和 [XUpdateAPI](https://github.com/xuexiangjys/XUpdateAPI) 封装，提供应用更新、下载校验、安装请求、暂停续传和更新流程定制，采用 AndroidX。

需自行提供配套的版本 JSON 接口及 APK 下载地址。更新包须与已安装应用包名一致、签名兼容且版本号更高；系统安装仍需授权和用户确认，不支持静默安装。

默认离开窗口取消任务，显式转入后台后通过前台服务继续，后台下载需要通知授权；不保证进程结束后恢复。暂停续传需提供真实 APK MD5，服务器不支持 Range 时重新完整下载。

可选大小字段 `ApkSize` 按整数 KB 严格校验；非整 KB 文件应省略或填 0，精确字节大小通过自定义解析提供。

## 依赖来源

- [XUpdate 2.1.5](https://github.com/xuexiangjys/XUpdate/tree/2.1.5)
- [XUpdateAPI 1.0.1](https://github.com/xuexiangjys/XUpdateAPI)
- [Aria 3.8.15](https://github.com/AriaLyy/Aria)
- [okhttputils 2.6.2](https://github.com/hongyangAndroid/okhttputils)
