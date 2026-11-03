# ZX二维码库

基于 [ZXing](https://github.com/zxing/zxing) 和 [ZBar](https://github.com/mchehab/zbar) 封装，提供二维码、CODE_128 条形码生成、图片识别和相机扫描，生成结果为 PNG 字节集。

相机扫描需要相机权限。扫描样式 1 和图片识别依赖 ARM 32 位 ZBar 原生库，仅支持 64 位应用的设备无法使用；扫描样式 2 使用 Java ZXing。

## 依赖来源

- [ZXing Core 2.1](https://github.com/zxing/zxing)
- [ZBar](https://github.com/mchehab/zbar)
- [GNU libiconv](https://www.gnu.org/software/libiconv/)
