# XUI界面库

基于 [XUI](https://github.com/xuexiangjys/XUI) 封装，提供按钮、文本、图片、选择器、列表和布局等可视组件，以及对话框、菜单、引导、消息提示和界面工具。

需要 Android 7.0（API 24）及以上及 AndroidX。组件使用独立主题上下文，弹层须在所属窗口显示后使用，切换窗口时自动关闭；不提供独立 Activity 或 Fragment 宿主。

自定义样式和字体须随应用打包，外部文件须具备访问权限。输出 JPEG 不保留透明度；富文本链接通过事件回传，不自动打开网址。

## 依赖来源

- [XUI 1.2.1](https://github.com/xuexiangjys/XUI/tree/1.2.1)
