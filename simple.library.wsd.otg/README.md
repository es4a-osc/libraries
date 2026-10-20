# OTG通讯库

提供非可视组件 **OTG通讯**：CH34x USB 串口通讯。

- `src/`：公开注解接口与实现；`library.json`：ES4A 元数据；`classes.jar`：构建产物。
- `libs/`：`ch34x-uart-driver.jar`，原文件名为 `CH34xUARTDriver.jar`。
- `icons/`：适配 currentColor 的 SVG；`sample/`：[可复现的 Simple 项目与操作步骤](sample/README.md)。

底层依赖为南京沁恒（WCH）的旧版 CH34x USB 串口驱动，内部包名为 `cn.wch.ch34xuartdriver`；本地 JAR 的具体版本和再分发许可尚未确认。厂商资料见[沁恒官方下载](https://www.wch.cn/downloads/CH341SER_ANDROID_ZIP.html)。

暂未提供此旧版驱动对应的源码 JAR。截至 2026-10-20，所检查的官方 Android SDK 2.3 分发包提供新版驱动二进制和调用示例，未包含底层驱动 Java 源码；公开旧版示例同样只附带驱动 JAR。未取得与当前二进制匹配的原始源码及 JavaDoc，不使用新版或反编译代码替代。

配套设备为采用 CH340/CH341 芯片的 USB 转串口模块或线缆，当前实现仅识别 `1a86:7523`、`1a86:5523` 两组 USB VID:PID。手机或平板需支持 USB Host/OTG，并使用合适的 OTG 转接线；连接串口设备时还需匹配接口电平、接线及通信参数。不声明支持 FTDI、CP210x 或所有 CH34x、OTG 设备，具体模块尚需实测。

打开时若需要系统 USB 授权，返回假；授权完成后再次点击打开。关闭或拔出后接收线程退出，旧会话事件不再派发。

在类库仓库运行 `build.bat "simple.library.wsd.otg"` 构建；需要安装时自行运行 `release.bat "simple.library.wsd.otg"`，再在 ES4A 选择相应 SDK 入口并刷新类库。

核对日期：2026-10-15。已验证 Java 构建、公开注解与清单、扩展元数据消费和样例 APK 编译。尚未完成真实扩展宿主可见交互和 Android 真机验证，编译成功不代表硬件、网络、后台或数据库兼容性已经通过。

通用开发与清单规则见 [SDK 文档](https://gitee.com/es4a/sdk/tree/main/docs)。

若 UTF-8 终端下 JDK 8 报“找不到”中文文件名，在当前 PowerShell 进程设置 `$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8"` 后重新构建；无需改源码文件名。
