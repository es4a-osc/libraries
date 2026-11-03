# ES4A 扩展类库

维护 Simple 扩展类库的 Java 源码、清单、依赖、图标和使用示例，各类库按包名组织为独立目录。

## 类库列表

以下列出本仓库维护的类库源码，不表示所用 SDK 已安装全部类库；用途和必要条件以各库自述及清单为准。

| 类库 | 用途 |
| --- | --- |
| [演示扩展库](com.example.demo/) | 可视组件和普通对象的入门演示。 |
| [跨程序辅助库](simple.library.wsd.accessibility/) | 无障碍节点读取、操作和事件监听。 |
| [ACE图表库](simple.library.wsd.achartengine/) | 饼状图、柱状图与折线图。 |
| [AW浏览器库](simple.library.wsd.agentweb/) | 网页导航、JavaScript 交互、文件上传下载及视频全屏。 |
| [Alerter提示条库](simple.library.wsd.alerter/) | 窗口内提示条、图标、按钮、进度和动画。 |
| [APK信息库](simple.library.wsd.apk/) | 独立 APK 名称、包名、版本和 PNG 图标读取。 |
| [低功耗蓝牙库](simple.library.wsd.blebluetooth/) | BLE 扫描、连接、特征读写和通知订阅。 |
| [蓝牙打印机库](simple.library.wsd.bluetoothprinter/) | 经典蓝牙打印机连接和原库 ESC/POS 指令。 |
| [CV日历库](simple.library.wsd.calendarview/) | 年月周日历、日期选择与标记、周月收缩联动和公农历工具。 |
| [CP城市选择库](simple.library.wsd.citypicker/) | 城市搜索、字母索引、热门城市和定位结果回填。 |
| [颜色选择框库](simple.library.wsd.colorpicker/) | 色环与明暗渐变选色、ARGB 预览及确认取消事件。 |
| [下载器库](simple.library.wsd.downloader/) | HTTP/HTTPS 文件下载、暂停、续传与任务记录。 |
| [EXCEL操作库](simple.library.wsd.excel/) | Excel 97-2003 .xls 的第一张工作表读写。 |
| [FTP客户端](simple.library.wsd.ftpclient/) | FTP/SFTP、结构化列表、流、分页及后台传输。 |
| [GIF图片框库](simple.library.wsd.gif/) | GIF 载入、播放控制、循环、缩放和资源清理。 |
| [分组列表框库](simple.library.wsd.grouplist/) | 分组展开、收起及子项展示和交互。 |
| [HTTP服务库](simple.library.wsd.httpserver/) | HTTP 和 WebSocket 服务。 |
| [字体图标库](simple.library.wsd.iconics/) | 字体图标、七类控件、文字混排、组合动画及 PNG 输出。 |
| [图像操作库](simple.library.wsd.image/) | 图片变换、尺寸读取、裁剪、分割、圆角、倒影和压缩。 |
| [图片浏览器库](simple.library.wsd.imagebrowser/) | 静态图片浏览、翻页、手势缩放和异步载入。 |
| [JSONPath库](simple.library.wsd.jsonpath/) | JSON路径查询、批量修改、新增、删除和属性重命名。 |
| [HTML解析库](simple.library.wsd.jsoup/) | HTML 文档解析、CSS 查询、文本属性提取及文档修改。 |
| [LuaJ脚本库](simple.library.wsd.luaj/) | 纯 Java Lua 5.1 脚本、表与函数、协程和宿主回调。 |
| [MPAC图表库](simple.library.wsd.mpandroidchart/) | 折线、柱状、横向柱状、饼状、散点、气泡、雷达、蜡烛与组合图。 |
| [MQTT客户端](simple.library.wsd.mqttclient/) | MQTT 发布、订阅与连接事件。 |
| [MsSQL数据库](simple.library.wsd.mssql/) | SQL Server JDBC 同步与异步访问。 |
| [MySQL数据库](simple.library.wsd.mysql/) | MySQL JDBC 同步与异步访问。 |
| [状态条通知库](simple.library.wsd.notification/) | 应用通知的构建、发送、更新和删除。 |
| [OTG通讯库](simple.library.wsd.otg/) | CH34x USB 串口通讯。 |
| [下拉刷新框库](simple.library.wsd.pulldownrefresh/) | 上下拉手势、自定义提示及可滚动内容容器。 |
| [QuickJS脚本库](simple.library.wsd.quickjs/) | JavaScript 脚本、模块、双向回调、Promise、字节码与资源控制。 |
| [SRL下拉刷新框库](simple.library.wsd.smartrefresh/) | 下拉刷新、上拉加载、内置头尾样式、二楼与自定义交互。 |
| [滑动页面框库](simple.library.wsd.viewpager/) | 可左右滑动切换页面的可视容器。 |
| [WS客户端库](simple.library.wsd.wsclient/) | WebSocket 连接、文本收发和连接状态事件。 |
| [XH2网络库](simple.library.wsd.xhttp2/) | 网络请求、上传下载、缓存、Cookie 和证书配置。 |
| [XQRC二维码库](simple.library.wsd.xqrcode/) | 艺术二维码生成、图片识别、独立扫码页和内嵌预览。 |
| [XUI界面库](simple.library.wsd.xui/) | 可视组件、对话框、菜单、消息提示和界面工具。 |
| [XU更新库](simple.library.wsd.xupdate/) | 应用更新、快捷入口、下载续传和更新流程定制。 |
| [XU工具库](simple.library.wsd.xutil/) | 文本、文件、缓存、编码、网络和设备静态函数集。 |
| [ZX二维码库](simple.library.wsd.zxing/) | 二维码和条形码生成、图片识别及相机扫描。 |

## 文档

下载并解压[完整 SDK](https://dwz.wsd.cx/es4a-sdk)，开发、构建、验证和接入 SDK 按以下文档操作：

- [Simple 类库开发](https://gitee.com/es4a/sdk/blob/main/docs/Simple类库开发.md)
- [类库清单定义](https://gitee.com/es4a/sdk/blob/main/docs/类库清单定义.md)
- [全部开发文档](https://gitee.com/es4a/sdk/blob/main/docs/README.md)

## 相关链接

- [开源仓库](https://gitee.com/es4a)
- [留言反馈](https://dwz.wsd.cx/wsd-ly)
