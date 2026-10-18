# ES4A 扩展类库

维护 Simple 扩展类库的 Java 源码、清单、依赖、图标和样例，各类库按包名组织为独立目录。


## 类库列表

| 类库 | 用途 |
| --- | --- |
| [演示扩展库](com.example.demo/) | 可视组件和普通对象的入门演示。 |
| [APK信息库](simple.library.wsd.apk/) | 独立 APK 名称、包名、版本和 PNG 图标读取。 |
| [跨程序辅助库](simple.library.wsd.accessibility/) | 无障碍节点读取、操作和事件监听。 |
| [低功耗蓝牙库](simple.library.wsd.blebluetooth/) | BLE 扫描、连接、特征读写和通知订阅。 |
| [GIF图片框库](simple.library.wsd.gif/) | GIF 载入、播放控制、循环、缩放和资源清理。 |
| [分组列表框库](simple.library.wsd.grouplist/) | 分组展开、收起及子项展示和交互。 |
| [HTTP服务库](simple.library.wsd.httpserver/) | HTTP 和 WebSocket 服务。 |
| [图像操作库](simple.library.wsd.image/) | 图片变换、尺寸读取、裁剪、分割、圆角、倒影和压缩。 |
| [Lua脚本库](simple.library.wsd.lua/) | Lua 5.1.5 代码执行、参数传入和返回值读取，附带 32 位 ARM 原生库。 |
| [条码库](simple.library.wsd.barcode/) | 二维码和条形码生成、图片识别及相机扫描。 |
| [状态条通知库](simple.library.wsd.notification/) | 应用通知的构建、发送、更新和删除。 |
| [滑动页面框库](simple.library.wsd.viewpager/) | 可左右滑动切换页面的可视容器。 |
| [WebSocket客户端库](simple.library.wsd.wsclient/) | WebSocket 连接、文本收发和连接状态事件。 |
| [OTG通讯库](simple.library.wsd.otg/) | CH34x USB 串口通讯。 |
| [MQTT通讯库](simple.library.wsd.mqtt/) | MQTT 发布、订阅与连接事件。 |
| [蓝牙打印机库](simple.library.wsd.bluetoothprinter/) | 经典蓝牙打印机连接和原库 ESC/POS 指令。 |
| [FTP客户端库](simple.library.wsd.ftpclient/) | FTP/SFTP 文件管理、后台传输及 SFTP 公钥校验。 |
| [下载器库](simple.library.wsd.downloader/) | HTTP/HTTPS 文件下载、暂停、续传与任务记录。 |
| [EXCEL操作库](simple.library.wsd.excel/) | Excel 97-2003 .xls 的第一张工作表读写。 |
| [MySQL数据库](simple.library.wsd.mysql/) | MySQL JDBC 同步与异步访问。 |
| [MsSQL数据库类库](simple.library.wsd.mssql/) | SQL Server JDBC 同步与异步访问。 |


## 目录

- `build.bat`：编译指定类库，生成该类库目录下的 `classes.jar`。
- `release.bat`：将指定类库的已有交付文件复制到同级 `sdk/libraries/`。
- 各类库目录：`src/` 为 Java 源码，`library.json` 为清单，`sample/` 为使用样例；依赖和资源按需放在 `libs/`、`res/`、`assets/`、`jni/`、`icons/`。
- 各类库的 `build/`：编译生成目录。
- `_extras/`：暂存资料；暂不可用的应用更新库保存在此目录，不随仓库提供。


## 构建

在 ES4A 根工作区打开某个类库内的文件，通过 VS Code“运行任务”执行 **ES4A: 构建类库**。执行 **ES4A: 发布类库到SDK** 将该库已有的交付文件更新到同级 `sdk`，不重新编译；仅处理当前标签页所属类库，附带 `sample/` 示例，排除 `libs/sources/` 和 `sample/build/`。

将已准备好 JDK 和 Android 工具的 [ES4A SDK](https://gitee.com/es4a/sdk) 与本仓库同级放置；工具准备见 SDK 的 README。

```text
工作目录/
├─ libraries/
└─ sdk/
```

在本仓库目录运行，例如：

```powershell
.\build.bat ".\com.example.demo"
.\release.bat ".\com.example.demo"
```

也可以将类库文件夹拖到对应 BAT 上。


## 接入 SDK

`release.bat` 用已有交付文件整体替换 `sdk/libraries/<类库目录>/` 并自动注册，该目录中的旧文件会被清除；优先更新 `sdk.local.json`，不存在时更新 `sdk.json`。

本地开发可先将 `sdk.json` 复制为 `sdk.local.json`，再在 ES4A 中选择该本地清单。使用已编译类库开发项目，无需携带 Java 源码或重新构建类库。


## 文档

通用开发和清单文档统一随 SDK 维护：

- [文档入口](https://gitee.com/es4a/sdk/blob/main/docs/README.md)
- [Simple 类库开发](https://gitee.com/es4a/sdk/blob/main/docs/Simple%20类库开发.md)
- [类库清单定义](https://gitee.com/es4a/sdk/blob/main/docs/类库清单定义.md)


## 相关链接

- [开源仓库](https://gitee.com/es4a)
- [留言反馈](https://dwz.wsd.cx/wsd-ly)
