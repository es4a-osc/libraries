# 图片浏览器库

基于 [ImageBrowse](https://github.com/brucetoo/ImageBrowse) 封装，在独立界面浏览静态图片，支持翻页、双击和双指缩放，可读取本地资源、已授权文档及 HTTP/HTTPS 图片。

最多 1000 项，每张输入不超过 20 MiB，预览采样至宽高不超过 2048 像素。GIF 仅显示静态帧，不自动校正 EXIF 方向；文件和网络访问受系统授权及安全策略限制。

## 依赖来源

- [ImageBrowse](https://github.com/brucetoo/ImageBrowse)
- [bm-x/PhotoView](https://github.com/bm-x/PhotoView)
- [AndroidX ViewPager 1.0.0](https://developer.android.com/jetpack/androidx/releases/viewpager)
