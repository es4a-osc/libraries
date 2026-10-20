# 蓝牙打印机库

提供非可视组件 **蓝牙打印机**：经典蓝牙打印机连接和原库 ESC/POS 指令。

- `src/`：公开注解接口与实现；`library.json`：ES4A 元数据；`classes.jar`：构建产物。
- `libs/`：`printer-api.jar`，原文件名为 `PrinterAPI.jar`。
- `icons/`：适配 currentColor 的 SVG；`sample/`：[可复现的 Simple 项目与操作步骤](sample/README.md)。

底层依赖属于深圳群索（Qunsuo/QS）打印 SDK 系列，内部包名为 `com.qs.helper.printer`；本地旧版 JAR 的具体版本和再分发许可尚未确认。厂商资料见[群索官方下载](https://www.qsprinter.com/download.html)。

截至 2026-10-20，已检查原始分发包，以及群索官方的 `QS-8001-PrinterTools_SDK.zip`、`PrinterTools_5802_eclipse.zip` 和 `PrinterTools_5802_studio.rar`。这些 SDK 提供调用示例和底层 JAR，未找到与当前 `printer-api.jar` 对应的 `com.qs.helper.printer` 原始源码及 JavaDoc，因此暂未提供源码 JAR，也未配置源码关联。现有运行依赖保持不变。

配套设备需支持经典蓝牙 SPP 连接及本库使用的 ESC/POS 打印指令，不提供 BLE 打印协议。手机需支持经典蓝牙，并完成权限授权、蓝牙开启及系统配对。具体打印机型号尚未实测，厂商提供 SDK 的型号列表不代表本库的兼容列表；图片、条码、纸宽和切纸能力需按型号确认。

底层 PrinterAPI 使用全局连接，组件层限制同一时刻只有一个组件占用；另一个组件连接会失败。文本按 GBK 编码，打印机需支持相应字符集。图像末行不足 24 点时补白，并恢复缺省行距。

在类库仓库运行 `build.bat "simple.library.wsd.bluetoothprinter"` 构建；需要安装时自行运行 `release.bat "simple.library.wsd.bluetoothprinter"`，再在 ES4A 选择相应 SDK 入口并刷新类库。

核对日期：2026-10-15。已验证 Java 构建、公开注解与清单、扩展元数据消费和样例 APK 编译。尚未完成真实扩展宿主可见交互和 Android 真机验证，编译成功不代表硬件、网络、后台或数据库兼容性已经通过。

通用开发与清单规则见 [SDK 文档](https://gitee.com/es4a/sdk/tree/main/docs)。

若 UTF-8 终端下 JDK 8 报“找不到”中文文件名，在当前 PowerShell 进程设置 `$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8"` 后重新构建；无需改源码文件名。
