# 图片浏览器库

提供“图片浏览器”非可视组件，在独立界面中查看一张或多张静态图片，支持翻页、双击和双指缩放。

支持资产、Android 资源、本地文件、已授权的文档 URI 和 HTTP/HTTPS 图片。图片异步读取，网络访问遵守系统安全策略，文件访问须具备相应授权。

列表最多 1000 项，每张输入不超过 20 MiB，图片按宽高不超过 2048 像素采样显示。放大查看的是预览图；GIF 只显示静态帧，不自动校正 EXIF 方向。

## 依赖来源

- [ImageBrowse](https://github.com/brucetoo/ImageBrowse)：图片缩放控件。
- [bm-x/PhotoView](https://github.com/bm-x/PhotoView)：ImageBrowse 中 PhotoView 的上游来源。
- [AndroidX ViewPager 1.0.0](https://developer.android.com/jetpack/androidx/releases/viewpager)：分页支持，来自 Google。

[运行示例](example/project.properties)：用 ES4A 打开示例工程并构建运行。
