# FTP客户端

提供非可视组件 **FTP客户端**，通过协议属性选择 FTP 或 SFTP，共用登录、文件管理、同步传输及后台完成事件。保留原有 13 个方法和 2 个事件，默认仍为 FTP；类库标识为 `simple.library.wsd.ftpclient`。

- `src/`：注解接口、协议会话与组件实现；`library.json`：清单；`classes.jar`：构建产物。
- `libs/`：运行依赖及授权文件；`tools/`：[SSH 依赖的可重建字节码转换](tools/README.md)。
- `sample/`：[默认应用图标的 Simple 示例](sample/README.md)，已加入调试工作区。
- `tests/`：FTP 回归及临时本机 SFTP 服务的真实通信测试。

| 协议 | 使用要求 |
| --- | --- |
| FTP | 默认模式，原版 Commons Net 3.0.1；被动模式、二进制传输、GBK 控制编码 |
| SFTP | 设置协议为协议_SFTP，填写地址、端口（通常为 22）和账号密码；首次连接确认并记住服务器，Android 8.0+ |

SFTP 默认不需要手动填写指纹。首次连接由组件弹框显示服务器与公钥指纹，用户核实后点击“信任并连接”，记录保存在本应用私有存储中；同一地址、端口的后续连接及应用重启自动校验。公钥变化时显示新旧指纹，只有明确确认更新信任才连接；取消、返回、确认超时或保存失败均拒绝连接。确认最多等待 60 秒，用户应通过可信渠道核实首次或变化的公钥。组件销毁会取消正在等待的确认。

`服务器公钥指纹` 保留为高级选项：指定可信 `SHA256:Base64` 值时严格匹配，不弹出信任确认，也不使用已保存记录兜底；格式错误或不匹配均拒绝。默认空文本使用上述确认流程。协议和指纹仍在下次登录生效，不切换现有连接。保存记录在同一应用的组件之间共享，按主机和端口隔离，不保存账号密码；卸载或清空应用数据会删除记录。

SFTP 通过维护版 JSch 2.28.8 实现，算法可用性取决于 Android 密码提供者；没有加入额外密码提供者或私钥认证接口。

同步网络方法须在后台线程调用。同一组件串行执行操作，只接受一项后台传输；开始方法返回真只表示接受任务，最终结果由主线程的上传完毕、下载完毕事件返回。`错误信息` 提供最近操作说明；SFTP 可区分空列表与失败，FTP 沿用有限错误信息，空列表仍可能表示失败。

上传、下载可递归处理目录；子项失败即失败，可能留下部分内容，同名下载文件会覆盖。名称参数是单个名称，下载不跟随远程符号链接，最多递归 128 层；上传跳过隐藏子项。相对本地目录基于应用内部 files，外部绝对路径仍受系统存储权限限制。

传输发现连接断开时尝试使用上次登录信息重连，SFTP 重连仍校验手动指定或应用保存的公钥，新公钥需再次确认；显式断开清除凭据。销毁后拒绝新操作并抑制回调，后台关闭连接，不提供立即取消。连接、控制/通道读取超时为 5 秒，并非整项任务总时限。

依赖授权：Commons Net 3.0.1 使用 [Apache License 2.0](libs/LICENSE-commons-net.txt)，附带 [NOTICE](libs/NOTICE-commons-net.txt)；JSch 及其内含代码的授权见 [JSch](libs/LICENSE-jsch-LICENSE.txt)、[JZlib](libs/LICENSE-jsch-LICENSE.JZlib.txt) 和 [jBCrypt](libs/LICENSE-jsch-LICENSE.jBCrypt.txt)。

`libs/jsch-2.28.8-android.jar` 是 JSch 2.28.8 的 Android 字节码适配产物，未修改上游 Java 源码或算法配置。上游二进制原包保存在 `tools/jsch-2.28.8.jar`，仅用于核验与重建，不是源码 JAR，也不作为运行依赖；来源、哈希及重建步骤见 [适配工具说明](tools/README.md)。未附加 Bouncy Castle、JNA 等可选依赖，不保证所有 SSH 算法或私钥格式。

在类库仓库运行 `build.bat "simple.library.wsd.ftpclient" --no-pause` 构建；在工作区根目录运行 `libraries/simple.library.wsd.ftpclient/tests/run.ps1` 执行 FTP 回归并编译测试，再运行 `python -X utf8 libraries/simple.library.wsd.ftpclient/tests/test_sftp.py` 执行真实 SFTP 测试（需本机已有 Paramiko）。

核对日期：2026-10-16。已通过类库构建、23 项 FTP 回归、25 项公钥信任检查、49 项本机 SFTP 集成检查、清单校验及示例 APK 编译。通过 Escrcpy 在 Android 14 验证首次取消、确认、目录读取、重启后自动校验、公钥变化拒绝与确认更新，以及手动错误指纹拒绝。Android 8.0–13、真机及外部存储尚未实测。

使用前按类库仓库既有流程安装到选用的 SDK 并刷新类库。通用规范见 [SDK 类库文档](https://gitee.com/es4a/sdk/tree/main/docs)。
