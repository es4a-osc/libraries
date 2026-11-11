# ES4A 扩展类库

维护 Simple 扩展类库的 Java 源码、清单、依赖、图标和使用示例，各类库按包名组织为独立目录。

## 类库列表

以下按类库目录名升序列出本仓库维护的类库源码，不表示所用 SDK 已安装全部类库；用途和必要条件以各库自述及清单为准。

| 类库 | 用途 |
| --- | --- |
| [演示扩展库](com.example.demo/) | 可视组件、普通对象、属性、事件和方法的扩展开发入门演示。 |
| [无障碍服务库](simple.library.wsd.accessibility/) | 无障碍节点与窗口访问、事件监听、手势、放大及软键盘等辅助控制；需用户启用系统服务。 |
| [ACE图表库](simple.library.wsd.achartengine/) | 折线、平滑曲线、时间、柱状、区间柱、散点、气泡、组合、饼状、圆环和仪表盘，支持多数据组、双轴、交互及图片导出。 |
| [AW浏览器库](simple.library.wsd.agentweb/) | 网页导航、数据加载、请求头、JavaScript 交互、Cookie、文件上传下载及视频全屏。 |
| [提示条库](simple.library.wsd.alerter/) | 窗口内标题与正文提示、左右图标、操作按钮、进度、自定义布局、动画及交互事件。 |
| [APK信息库](simple.library.wsd.apk/) | 本地独立 APK 的名称、包名、版本及 PNG 图标读取，不支持 APKS、XAPK 或拆分 APK 组合。 |
| [低功耗蓝牙库](simple.library.wsd.blebluetooth/) | BLE 设备扫描、GATT 连接、服务发现、特征读写和通知订阅。 |
| [蓝牙打印机库](simple.library.wsd.bluetoothprinter/) | 经典蓝牙 SPP 打印机连接，以及 ESC/POS 文本、图片和条码打印。 |
| [CV日历库](simple.library.wsd.calendarview/) | 年月周日历、单选与范围及多选、日期标记、周月收缩联动、公农历转换和自定义绘制。 |
| [CP城市选择库](simple.library.wsd.citypicker/) | 中文或拼音搜索、字母索引、热门城市、主题与动画、自定义数据及异步搜索，支持应用提供的定位结果回填。 |
| [颜色选择框库](simple.library.wsd.colorpicker/) | 色环与明暗渐变选色、ARGB 预览及确认取消事件；透明度沿用初始颜色。 |
| [下载器库](simple.library.wsd.downloader/) | HTTP/HTTPS 文件下载、暂停、继续、取消和私有断点记录；任务随应用进程运行。 |
| [EXCEL操作库](simple.library.wsd.excel/) | Excel 97–2003 格式 .xls 文件的第一张工作表读写，不支持 .xlsx。 |
| [Fastjson2库](simple.library.wsd.fastjson2/) | JSON/JSONB 解析与类型化数据操作、顺序读写、基础 JSONPath、配置、过滤、集合互转及自定义对象编解码。 |
| [FTP客户端](simple.library.wsd.ftpclient/) | FTP/SFTP 连接、文件管理、上传下载和断点续传，支持 FTP 数据流、列表分页及 SFTP 服务器公钥校验。 |
| [GIF图片框库](simple.library.wsd.gif/) | 从资产、资源、文件或字节集载入 GIF，支持播放控制、循环和缩放。 |
| [分组列表框库](simple.library.wsd.grouplist/) | 分组与子项管理、展开收起、图片与文字及按钮展示、业务标记和交互事件。 |
| [HTTP服务库](simple.library.wsd.httpserver/) | 应用内 HTTP 路由、请求头与主体读取、响应发送及重定向，以及 WebSocket 服务与消息收发。 |
| [字体图标库](simple.library.wsd.iconics/) | 字体图标、七类图标控件、文字混排、状态着色、自定义字体、组合动画及 PNG 输出。 |
| [图像操作库](simple.library.wsd.image/) | 图片旋转、倾斜、缩放、镜像、裁剪、分割、圆角、倒影、压缩和尺寸读取。 |
| [图片浏览器库](simple.library.wsd.imagebrowser/) | 本地资源、已授权文档及 HTTP/HTTPS 静态图片浏览、滑动翻页、双击和双指缩放；GIF 仅显示静态帧。 |
| [JSONPath库](simple.library.wsd.jsonpath/) | JSON 路径查询、读取选项、限量查询、逐项访问，以及节点批量新增、映射、修改、删除和重命名。 |
| [HTML解析库](simple.library.wsd.jsoup/) | HTML/XML 解析、CSS/XPath 查询、DOM 修改、正文清洗、表单字段提取及输出配置，不执行脚本或自动联网。 |
| [LuaJ脚本库](simple.library.wsd.luaj/) | 纯 Java Lua 5.1 脚本执行、值与表、函数和多返回值、协程、字节码、调试标准库及 Simple 同步回调。 |
| [MPAC图表库](simple.library.wsd.mpandroidchart/) | 折线、柱状、横向柱状、饼状、散点、气泡、雷达、蜡烛和组合图，支持堆叠、双轴、样式与格式器、手势及图片导出。 |
| [MQTT客户端库](simple.library.wsd.mqttclient/) | MQTT 3.1/3.1.1 异步连接、发布订阅、遗嘱、自动重连、离线缓冲、操作令牌及 TLS 配置。 |
| [MsSQL数据库](simple.library.wsd.mssql/) | 通过 JDBC 直连 SQL Server，支持同步查询、写入及异步结果事件。 |
| [MySQL数据库](simple.library.wsd.mysql/) | 通过 JDBC 直连 MySQL，支持同步查询、写入及异步结果事件。 |
| [状态条通知库](simple.library.wsd.notification/) | 通知构建、发送、更新、移除、分组、样式和进度，以及点击、按钮、回复和清除事件。 |
| [OTG通讯库](simple.library.wsd.otg/) | CH34x USB 转串口设备连接、串口参数配置及数据收发，需设备支持 USB Host/OTG 并取得授权。 |
| [下拉刷新框库](simple.library.wsd.pulldownrefresh/) | 上下拉手势、刷新事件、状态与辅助提示，以及按面板布局纵向滚动的内容容器。 |
| [QuickJS脚本库](simple.library.wsd.quickjs/) | JavaScript 脚本与模块、值类型、双向对象绑定、Promise、计时器、工作线程、字节码及运行时资源控制。 |
| [SRL下拉刷新框库](simple.library.wsd.smartrefresh/) | 下拉刷新、上拉加载、内置头尾、二楼、自定义外观、飞行场景、阶段事件及独立请求序号管理。 |
| [滑动页面框库](simple.library.wsd.viewpager/) | 将可视组件作为分页内容，支持触摸滑动、程序切换和页面事件。 |
| [WS客户端库](simple.library.wsd.wsclient/) | ws/wss 连接、请求头设置、文本消息收发和连接状态事件，不支持二进制消息。 |
| [XH2网络库](simple.library.wsd.xhttp2/) | 网络请求与响应解析、上传下载、进度、缓存、Cookie、自定义证书、代理及批量请求。 |
| [XQRC二维码库](simple.library.wsd.xqrcode/) | 普通、图标及艺术背景二维码生成、图片识别、独立扫码页和内嵌相机预览，识别格式与扫描外观可配置。 |
| [XUI界面库](simple.library.wsd.xui/) | 按钮、文本、图片、选择器、列表和布局等可视组件，以及对话框、菜单、引导、消息提示和界面工具。 |
| [XU更新库](simple.library.wsd.xupdate/) | 版本检查、更新提示、下载校验、系统安装请求、后台下载、暂停续传、快捷接入及更新流程定制。 |
| [XU工具库](simple.library.wsd.xutil/) | 文本与正则、编码与摘要、对称加解密、日期与大数、字节集、配置缓存、文件压缩、网络设备及常用系统静态函数集。 |
| [ZX二维码库](simple.library.wsd.zxing/) | 二维码和 CODE_128 条形码生成、图片识别及相机扫描；依赖 ZBar 的功能仅支持 ARM 32 位。 |

## 文档

下载并解压[完整 SDK](https://dwz.wsd.cx/es4a-sdk)，开发、构建、验证和接入 SDK 按以下文档操作：

- [Simple 类库开发](https://gitee.com/es4a/sdk/blob/main/docs/Simple类库开发.md)
- [类库清单定义](https://gitee.com/es4a/sdk/blob/main/docs/类库清单定义.md)
- [全部开发文档](https://gitee.com/es4a/sdk/blob/main/docs/README.md)

## 相关链接

- [开源仓库](https://gitee.com/es4a)
- [留言反馈](https://dwz.wsd.cx/wsd-ly)
