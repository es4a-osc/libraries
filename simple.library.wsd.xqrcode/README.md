# XQRC二维码库

基于 [XQRCode](https://github.com/xuexiangjys/XQRCode) 和 [ZXing](https://github.com/zxing/zxing) 封装，提供普通、图标及艺术背景二维码生成、图片识别、独立扫码页和内嵌相机预览，不依赖原生库。

相机扫描需要相机权限，同一时刻仅允许一个预览。图像输入不超过 32 MiB，采样至最长边 2048 像素，小码可能需要裁剪后识别。

ZXing 使用本库私有包名，可与 ZX二维码库共存；后者的 ARM 32 位限制仍然存在。识别不会自动打开链接，外部文件和文档 URI 须具备访问权限。

## 依赖来源

- [XQRCode 1.1.1](https://github.com/xuexiangjys/XQRCode/tree/1.1.1)
- [ZXing Core 3.3.3](https://github.com/zxing/zxing/tree/zxing-3.3.3)
