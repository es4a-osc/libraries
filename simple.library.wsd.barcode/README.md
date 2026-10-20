# 条码库

提供二维码、CODE_128 条形码生成、图片识别和相机扫描，组件名为 **条码**。使用方式见 [示例项目](sample/)。

- 生成方法返回 PNG 字节数组。示例将图片保存到应用私有目录，并演示再次识别。
- 图片参数支持绝对文件路径、应用私有目录的相对路径或资产名称；示例自带 `logo.png` 和 `test.png`。
- 扫描框样式 `1` 使用 ZBar，`2` 使用 ZXing；扫描前须取得相机权限。连续扫描、闪光灯和提示音设置影响随后打开的扫描界面，与原组件一样在应用内共享。
- ZBar 图片识别与样式 `1` 依赖原始 `jni/armeabi/` 原生库，仅适用于能加载这些 ARM 32 位二进制的设备；不能据此认为支持仅有 64 位 ABI 的设备。样式 `2` 使用 Java ZXing 解码。

## 依赖与图标

`libs/zxing-core-2.1.jar` 使用 Maven Central 的 [ZXing Core 2.1](https://repo.maven.apache.org/maven2/com/google/zxing/core/2.1/) 原版制品，本地文件名增加 `zxing-` 前缀以避免通用名称 `core` 与其他依赖混淆；对应源码位于 `libs/sources/zxing-core-2.1-sources.jar`，Apache-2.0 许可文本见 `libs/LICENSE-zxing-core.txt`。

`libs/zbar-decoder.jar` 和两个原生库保留原始二进制，版本尚未确认。[ZBar](https://github.com/mchehab/zbar) 上游采用 LGPL-2.1，许可文本见 `libs/LICENSE-zbar.txt`；该文本不代表已核实原始分发包的具体版本、全部修改和完整授权来源，`libiconv.so` 的具体构建来源同样待核实。

Java 源码对照包见 [zbar-decoder-sources.jar](libs/sources/zbar-decoder-sources.jar)：以 [ZBar 历史 Java 绑定](https://github.com/ZBar/ZBar/tree/6ed227f4d59fb6e0f5be173997c46941e93a1e7d/java/net/sourceforge/zbar)为基础，保留原始 JavaDoc，并按现有 JAR 补齐库名常量及 `ZBarDecoder` 加载名称。此包由 ES4A 整理用于 IDE 源码关联，不是原始分发方的源码发布包，也不包含原生 SO 源码。

图标来自 [Tabler Icons](https://github.com/tabler/tabler-icons)，MIT 许可文本见 `icons/LICENSE.txt`。

本次验证覆盖 Java 构建、示例 APK 编译、清单和原生库打包；相机、闪光灯、提示音和 ZBar 原生库加载仍需在兼容的 Android 真机上验证。
