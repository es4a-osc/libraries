# MsSQL数据库类库

提供非可视组件 **MsSQL数据库**：SQL Server JDBC 同步与异步访问。

- `src/`：公开注解接口与实现；`library.json`：ES4A 元数据；`classes.jar`：构建产物。
- `libs/`：jTDS 1.2.7。
- `icons/`：适配 currentColor 的 SVG；`sample/`：[可复现的 Simple 项目与操作步骤](sample/README.md)。

保留 14 个方法和 7 个事件，连接数据库仍为异步接口，断开连接仍返回逻辑值。异步任务串行执行，查询结果独立捕获，事件切回主线程；同步方法阻塞调用线程。SQL NULL 为空文本，失败为空集合；字段为空或 * 返回全部列。保留原库写入结果语义：影响行数大于 0 才返回真，0 行和失败返回假。原驱动与目标 SQL Server 的认证及 TLS 兼容性需实测。DEX 阶段保留原驱动匿名内部类元数据的警告，未影响本次 APK 构建。

在类库仓库运行 `build.bat "simple.library.wsd.mssql"` 构建；需要安装时自行运行 `release.bat "simple.library.wsd.mssql"`，再在 ES4A 选择相应 SDK 入口并刷新类库。本次未安装或注册到 SDK。

核对日期：2026-10-15。已验证 Java 构建、公开注解与清单、扩展元数据消费和样例 APK 编译。尚未完成真实扩展宿主可见交互和 Android 真机验证，编译成功不代表硬件、网络、后台或数据库兼容性已经通过。

通用开发与清单规则见 [SDK 文档](https://gitee.com/es4a/sdk/tree/main/docs)。

若 UTF-8 终端下 JDK 8 报“找不到”中文文件名，在当前 PowerShell 进程设置 `$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8"` 后重新构建；无需改源码文件名。
