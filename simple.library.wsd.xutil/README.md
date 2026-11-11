# XU工具库

基于 [XUtil](https://github.com/xuexiangjys/XUtil) 封装，提供文本、正则、编码、摘要、对称加解密、日期、文件、压缩、缓存、网络和设备等静态函数集。

需要 Android 7.0（API 24）及以上及 AndroidX。权限按实际引用的函数集收集，危险权限仍须由应用取得授权。域名解析在工作线程执行，剪贴板、软键盘和拨号界面在界面线程使用。

对称加解密显式指定密钥、变换和初始向量；GCM nonce 不可在同一密钥下重复，DES/3DES 仅用于旧协议兼容。

解压拒绝路径越界、符号链接和覆盖已有文件，失败可能留下部分内容。身份证检查仅验证本地格式和校验位，随机文本不用于安全令牌；不提供 Root/Shell 或受限设备标识访问。

## 依赖来源

- [XUtil Core、Sub 2.0.0](https://github.com/xuexiangjys/XUtil)
