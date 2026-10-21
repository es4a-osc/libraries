# 条码库

提供“条码”组件，支持二维码、CODE_128 条形码生成、图片识别和相机扫描。生成结果为 PNG 字节集。

扫描前须取得相机权限。扫描样式 1 和图片识别使用 ZBar，需要设备能够加载随库的 ARM 32 位原生库；扫描样式 2 使用 Java ZXing 解码。仅支持 64 位应用的设备不能使用随库的 ZBar 原生功能。

图片来源支持资产名、绝对文件路径和应用私有目录的相对路径。连续扫描、闪光灯和提示音设置供应用内的扫描界面共享。

## 依赖来源

- [ZXing Core 2.1](https://github.com/zxing/zxing)
- [ZBar](https://github.com/mchehab/zbar)
- [GNU libiconv](https://www.gnu.org/software/libiconv/)

随库 ZBar 包装及原生库的具体分发来源和版本尚未确认。
