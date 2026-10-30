# XUtil工具库

基于 XUtil 2.0.0 https://github.com/xuexiangjys/XUtil 封装的 AndroidX 静态函数集，提供文本、正则、编码、摘要、日期、文件、压缩、配置、缓存、网络、设备及常用附加工具。

通过类型调用，例如 `simple.library.wsd.xutil.XUtil文本.反转("示例")`；无需创建实例。最低 API 24，使用 JDK 11、API 30 编译。

权限按引用的函数集收集：网络状态需要 `ACCESS_NETWORK_STATE`，域名解析需要 `INTERNET`，直接通话需要 `CALL_PHONE`。危险权限仍须通过运行库权限请求组件取得系统授权。类库不会自行弹出授权框。

配置和缓存使用应用私有目录，无需存储权限。文件函数接受实际路径，可通过“取私有目录”“取缓存目录”获得私有路径；其它路径仍受系统访问限制。解压拒绝路径越界、符号链接目标及覆盖已有文件，需指定累计解压上限，失败可能保留已经写出的文件。域名解析须在工作线程调用；剪贴板、软键盘和拨号界面须在界面线程调用。

身份证检查仅为本地格式及校验位验证。随机文本不用于安全令牌。本地 AAR 仅裁剪固定权限、硬件要求和未使用的权限 Activity／文件共享 Provider；其它条目保持上游内容，原包与适配包校验值见 `aars/XUtil-changes.txt`。

当前不导出 Activity／Fragment 管理、Root／Shell、受限设备标识和系统限制的 Wi-Fi 开关等接口。

依赖来源：

- XUtil Core、Sub 2.0.0 https://github.com/xuexiangjys/XUtil
- AndroidX AppCompat 1.3.1 https://developer.android.com/jetpack/androidx/releases/appcompat
- Material Components 1.4.0 https://github.com/material-components/material-components-android
- Gson 2.8.5 https://github.com/google/gson

[运行示例](example/project.properties)：用 ES4A 打开示例工程并构建运行。
