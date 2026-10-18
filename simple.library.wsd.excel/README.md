# EXCEL操作库

提供非可视组件 **EXCEL操作**：Excel 97-2003 .xls 的第一张工作表读写。

- `src/`：公开注解接口与实现；`library.json`：ES4A 元数据；`classes.jar`：构建产物。
- `libs/`：jxl-2.6.3.jar。
- `icons/`：适配 currentColor 的 SVG；`sample/`：[可复现的 Simple 项目与操作步骤](sample/README.md)。

只支持 `.xls`，不支持 `.xlsx`。所有行列索引从 0 开始，参数顺序为行、列；仅操作第一张工作表。`添加工作表` 新建并覆盖目标文件，不向已有工作簿追加工作表；已有未写出会话时拒绝再次创建。成功写出后关闭写会话，重开或销毁时释放读会话。相对路径以应用私有 files 目录为基准，无需共享存储权限；绝对路径由调用方保证可访问。

在类库仓库运行 `build.bat "simple.library.wsd.excel"` 构建；需要安装时自行运行 `release.bat "simple.library.wsd.excel"`，再在 ES4A 选择相应 SDK 入口并刷新类库。本次未安装或注册到 SDK。

核对日期：2026-10-15。已验证 Java 构建、公开注解与清单、扩展元数据消费和样例 APK 编译。尚未完成真实扩展宿主可见交互和 Android 真机验证，编译成功不代表硬件、网络、后台或数据库兼容性已经通过。

通用开发与清单规则见 [SDK 文档](https://gitee.com/es4a/sdk/tree/main/docs)。

若 UTF-8 终端下 JDK 8 报“找不到”中文文件名，在当前 PowerShell 进程设置 `$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8"` 后重新构建；无需改源码文件名。
