# 提示条库

基于 [Alerter](https://github.com/Tapadoo/Alerter) 封装，在当前窗口内显示提示条，支持标题、正文、图标、按钮、进度、自定义内容布局、图标颜色矩阵、动画和交互事件。

采用 AndroidX，宿主主题须提供 `selectableItemBackground` 属性，例如 AppCompat 主题。组件在界面线程使用，同一窗口的新提示条会替换旧提示条；外部图片及提示音 URI 须具备读取权限。

## 依赖来源

- [Alerter 7.2.4](https://github.com/Tapadoo/Alerter)

自定义内容使用 Android 布局资源；内置标题、正文、图标和进度配置仅作用于布局中具有对应 Alerter 编号的控件。
