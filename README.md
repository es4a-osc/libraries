# ES4A 扩展类库

维护 Simple 扩展类库的 Java 源码、清单、依赖、图标和样例，各类库按包名组织为独立目录。


## 类库列表

| 类库 | 用途 |
| --- | --- |
| [演示扩展库](com.example.demo/) | 可视组件和普通对象的入门演示。 |
| [跨程序辅助库](simple.library.wsd.accessibility/) | 无障碍节点读取、操作和事件监听。 |
| [低功耗蓝牙库](simple.library.wsd.blebluetooth/) | BLE 扫描、连接、特征读写和通知订阅。 |
| [分组列表框库](simple.library.wsd.grouplist/) | 分组展开、收起及子项展示和交互。 |
| [HTTP服务库](simple.library.wsd.httpserver/) | HTTP 和 WebSocket 服务。 |
| [状态条通知库](simple.library.wsd.notification/) | 应用通知的构建、发送、更新和删除。 |
| [应用更新库](simple.library.wsd.update/) | 版本检查、更新提示和安装包下载。 |
| [滑动页面框库](simple.library.wsd.viewpager/) | 可左右滑动切换页面的可视容器。 |


## 目录

- `build.bat`：编译指定类库，生成该类库目录下的 `classes.jar`。
- `release.bat`：将指定类库的已有交付文件复制到同级 `sdk/libraries/`。
- 各类库目录：`src/` 为 Java 源码，`library.json` 为清单，`sample/` 为使用样例；依赖和资源按需放在 `libs/`、`res/`、`assets/`、`jni/`、`icons/`。
- 各类库的 `build/`：编译生成目录。


## 构建

在 ES4A 根工作区打开某个类库内的文件，通过 VS Code“运行任务”执行 **ES4A: 构建类库**。执行 **ES4A: 发布类库到SDK** 将该库已有的交付文件更新到同级 `sdk`，不重新编译；仅处理当前标签页所属类库，不复制源码、构建目录或样例。

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

`release.bat` 将已有交付文件安装到 `sdk/libraries/<类库目录>/` 并自动注册；优先更新 `sdk.local.json`，不存在时更新 `sdk.json`。

本地开发可先将 `sdk.json` 复制为 `sdk.local.json`，再在 ES4A 中选择该本地清单。使用已编译类库开发项目，无需携带 Java 源码或重新构建类库。


## 文档

通用开发和清单文档统一随 SDK 维护：

- [文档入口](https://gitee.com/es4a/sdk/blob/main/docs/README.md)
- [Simple 类库开发](https://gitee.com/es4a/sdk/blob/main/docs/Simple%20类库开发.md)
- [类库清单定义](https://gitee.com/es4a/sdk/blob/main/docs/类库清单定义.md)


## 相关链接

- [开源仓库](https://gitee.com/es4a)
- [留言反馈](https://dwz.wsd.cx/wsd-ly)
