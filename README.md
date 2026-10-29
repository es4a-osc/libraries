# ES4A 扩展类库

维护 Simple 扩展类库的 Java 源码、清单、依赖、图标和独立测试，各类库按包名组织为独立目录。


## 类库列表

以下列出本仓库维护的类库源码，不表示所用 SDK 已安装全部类库；用途和必要条件以各库自述及清单为准。

| 类库 | 用途 |
| --- | --- |
| [演示扩展库](com.example.demo/) | 可视组件和普通对象的入门演示。 |
| [Alerter提示条库](simple.library.wsd.alerter/) | 窗口内提示条、图标、按钮、进度和动画。 |
| [AgentWeb浏览器库](simple.library.wsd.agentweb/) | 网页导航、JavaScript 交互、文件上传下载及视频全屏。 |
| [CityPicker城市选择库](simple.library.wsd.citypicker/) | 城市搜索、字母索引、热门城市和定位结果回填。 |
| [APK信息库](simple.library.wsd.apk/) | 独立 APK 名称、包名、版本和 PNG 图标读取。 |
| [跨程序辅助库](simple.library.wsd.accessibility/) | 无障碍节点读取、操作和事件监听。 |
| [低功耗蓝牙库](simple.library.wsd.blebluetooth/) | BLE 扫描、连接、特征读写和通知订阅。 |
| [GIF图片框库](simple.library.wsd.gif/) | GIF 载入、播放控制、循环、缩放和资源清理。 |
| [分组列表框库](simple.library.wsd.grouplist/) | 分组展开、收起及子项展示和交互。 |
| [HTTP服务库](simple.library.wsd.httpserver/) | HTTP 和 WebSocket 服务。 |
| [图像操作库](simple.library.wsd.image/) | 图片变换、尺寸读取、裁剪、分割、圆角、倒影和压缩。 |
| [图片浏览器库](simple.library.wsd.imagebrowser/) | 静态图片浏览、翻页、手势缩放和异步载入。 |
| [JSONPath库](simple.library.wsd.jsonpath/) | JSON路径查询、批量修改、新增、删除和属性重命名。 |
| [HTML解析库](simple.library.wsd.jsoup/) | HTML 文档解析、CSS 查询、文本属性提取及文档修改。 |
| [颜色选择框库](simple.library.wsd.colorpicker/) | 色环与明暗渐变选色、ARGB 预览及确认取消事件。 |
| [Lua脚本库](simple.library.wsd.lua/) | Lua 5.1.5 代码执行、参数传入和返回值读取，附带 32 位 ARM 原生库。 |
| [条码库](simple.library.wsd.barcode/) | 二维码和条形码生成、图片识别及相机扫描。 |
| [AChartEngine图表库](simple.library.wsd.achartengine/) | 饼状图、柱状图与折线图。 |
| [MPAndroidChart图表库](simple.library.wsd.mpandroidchart/) | 折线、柱状、横向柱状、饼状、散点、气泡、雷达、蜡烛与组合图。 |
| [拉动刷新框库](simple.library.wsd.pullrefresh/) | 上下拉手势、自定义提示及可滚动内容容器。 |
| [状态条通知库](simple.library.wsd.notification/) | 应用通知的构建、发送、更新和删除。 |
| [滑动页面框库](simple.library.wsd.viewpager/) | 可左右滑动切换页面的可视容器。 |
| [WS客户端库](simple.library.wsd.wsclient/) | WebSocket 连接、文本收发和连接状态事件。 |
| [OTG通讯库](simple.library.wsd.otg/) | CH34x USB 串口通讯。 |
| [MQTT通讯库](simple.library.wsd.mqtt/) | MQTT 发布、订阅与连接事件。 |
| [蓝牙打印机库](simple.library.wsd.bluetoothprinter/) | 经典蓝牙打印机连接和原库 ESC/POS 指令。 |
| [FTP客户端](simple.library.wsd.ftpclient/) | FTP/SFTP 文件管理、后台传输及 SFTP 公钥校验。 |
| [下载器库](simple.library.wsd.downloader/) | HTTP/HTTPS 文件下载、暂停、续传与任务记录。 |
| [EXCEL操作库](simple.library.wsd.excel/) | Excel 97-2003 .xls 的第一张工作表读写。 |
| [MySQL数据库](simple.library.wsd.mysql/) | MySQL JDBC 同步与异步访问。 |
| [MsSQL数据库](simple.library.wsd.mssql/) | SQL Server JDBC 同步与异步访问。 |
| [XHttp2网络库](simple.library.wsd.xhttp2/) | 网络请求、上传下载、缓存、Cookie 和证书配置。 |
| [XUI界面库](simple.library.wsd.xui/) | 可视组件、对话框、菜单、消息提示和界面工具。 |
| [XUpdate更新库](simple.library.wsd.xupdate/) | 应用更新、快捷入口、下载续传和更新流程定制。 |
| [XUtil工具库](simple.library.wsd.xutil/) | 文本、文件、缓存、编码、网络和设备静态函数集。 |


## 目录

- 各类库目录：`src/` 为 Java 源码，`library.json` 为清单，`library.gradle` 声明命名空间及依赖，`test/` 为本库独立测试项目。
- 本地 JAR 放在 `libs/`，本地 AAR 放在 `aars/`；资源按需放在 `res/`、`assets/`、`jni/`、`icons/`。
- 编译结果：库根目录的 `classes.jar`、`dependencies.json`、`dependencies.module` 及必要的清单、资源附加文件。
- `build/`：各库的开发生成目录，无需随编译结果传播。


## 构建

下载并解压[完整 SDK](https://dwz.wsd.cx/es4a-sdk)，与本仓库同级放置：

```text
工作目录/
├─ libraries/
└─ sdk/
```

统一使用 SDK 的 `utils/library/library.ps1`，在本仓库目录运行，例如：

```powershell
& "..\sdk\utils\library\library.ps1" compile ".\com.example.demo"
```

`compile` 准备依赖、生成 VS Code 工作区并编译类库；仅刷新依赖时使用 `prepare`。也可打开生成的 `.code-workspace`，通过任务执行。详情见 [扩展库开发工具](https://gitee.com/es4a/sdk/blob/main/utils/library/README.md)。

编译后的类库目录可直接复制传播，无需先发布到 SDK。复制时保留依赖描述、本地原包和自有资源，不必携带 `build/`。


## 测试

发布前使用各库的 `test/project.properties` 编译测试 APK，并在设备或模拟器上验证功能。必要条件见测试界面或源码；编译成功不能代替功能验证。


## 接入 SDK

测试通过后，在本仓库目录执行：

```powershell
& "..\sdk\utils\library\library.ps1" publish ".\com.example.demo"
```

`publish` 将已有编译结果安装到 SDK 的 `libraries/<基础包名>/`，不重新编译，不携带实现源码、测试、源码附件或开发缓存。复制规则见 [扩展库开发工具](https://gitee.com/es4a/sdk/blob/main/utils/library/README.md)。

安装目录中的 `library.json` 即表示注册，更新后刷新 ES4A 类库视图。


## 文档

通用开发和清单文档统一随 SDK 维护：

- [文档入口](https://gitee.com/es4a/sdk/blob/main/docs/README.md)
- [Simple 类库开发](https://gitee.com/es4a/sdk/blob/main/docs/Simple类库开发.md)
- [类库清单定义](https://gitee.com/es4a/sdk/blob/main/docs/类库清单定义.md)


## 相关链接

- [开源仓库](https://gitee.com/es4a)
- [留言反馈](https://dwz.wsd.cx/wsd-ly)
