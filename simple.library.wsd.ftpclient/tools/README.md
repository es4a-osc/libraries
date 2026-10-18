# JSch 字节码适配工具

文档类型：构建说明。核对日期：2026-10-16。适用范围：本库的 JSch 2.28.8 原版 JAR 与现有 JDK 8、dx 工具链。

直接使用维护版 JSch 的发布包会引入 Lambda 和多版本实现。本目录的 `jsch-2.28.8.jar` 是上游二进制原包，`prepare-jsch.ps1` 校验其 SHA-256，使用本目录 Retrolambda 2.5.7 转换全部基础类及接口默认方法，保留资源和授权，去除 Java 9+ 的多版本类，再生成 `libs/jsch-2.28.8-android.jar`。中间产物只写入本库 `build/`，不修改公共编译器、SDK 或上游源码。

在 PowerShell 运行 `libraries/simple.library.wsd.ftpclient/tools/prepare-jsch.ps1` 可从保留的原包重建；随后运行类库构建及回归。日常构建直接使用已有适配 JAR，不需要联网或执行转换。升级 JSch、JDK 或转换工具后，必须重新检查字节码、协议互通和 Android 端行为，不能把旧测试结果沿用为新版本验证。

JSch 原包来自 [Maven Central](https://repo.maven.apache.org/maven2/com/github/mwiede/jsch/2.28.8/)，SHA-256 为 `2afca1357d816028f2ac33f22407d2488a68406682973949cebb1b8ea6688a8b`。源码项目为 [mwiede/jsch](https://github.com/mwiede/jsch)，授权见 [JSch](../libs/LICENSE-jsch-LICENSE.txt)、[JZlib](../libs/LICENSE-jsch-LICENSE.JZlib.txt) 和 [jBCrypt](../libs/LICENSE-jsch-LICENSE.jBCrypt.txt)。

Retrolambda 原件来自 [Maven Central](https://repo.maven.apache.org/maven2/net/orfjackal/retrolambda/retrolambda/2.5.7/)，SHA-256 为 `590ad5ddefd57f47245f5470d24431b5bf2384086f8966c17545289efbe6b12e`。它只在构建期使用，不进入示例 APK。工具及内含 Guava 18.0、retrolambda-api 使用 [Apache License 2.0](LICENSE-retrolambda.txt)，内含 ASM 7.3.1、Minlog 1.3 的授权见 [ASM](LICENSE-asm.txt)、[Minlog](LICENSE-minlog.txt)。

转换只处理语言字节码，不移植 Java 8 API；本库明确将 SFTP 限制为 Android 8.0+，FTP 默认行为不受此限制。
