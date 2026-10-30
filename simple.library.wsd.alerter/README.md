# 提示条库

提供“提示条”非可视组件，在当前窗口内显示标题、正文、左右图标、进度和操作按钮，支持进入退出动画、提示音、震动和交互事件。

所有成员在界面线程调用。配置用于下次显示；同一窗口的新提示条替换旧提示条。显示立即返回，通过被显示、被隐藏、被单击和按钮被单击事件接收结果。

## 使用条件

原始 Alerter AAR 位于 `aars/`，其它依赖由 `library.gradle` 声明。通过 SDK 扩展库工具发布完整交付目录，应用构建需要其中的依赖、清单、资源及资源符号，不能只复制 `classes.jar`。

使用 AndroidX。应用主题必须提供 `selectableItemBackground` 属性，例如 `@style/Theme.AppCompat.Light.NoActionBar`；使用仅有平台属性的 Holo 主题会导致提示条无法取得所需背景资源。使用提示音的内容 URI 或外部图片时，调用方应具备相应读取权限。

## 依赖来源

- [Alerter 7.2.4](https://github.com/Tapadoo/Alerter)，随附 AndroidX 版本
- [AndroidX AppCompat 1.2.0](https://developer.android.com/jetpack/androidx/releases/appcompat)
- [AndroidX Annotation 1.2.0](https://developer.android.com/jetpack/androidx/releases/annotation)
- [Kotlin 标准库 1.4.21](https://github.com/JetBrains/kotlin/tree/v1.4.21)
- [JetBrains Annotations 13.0](https://github.com/JetBrains/java-annotations)

[运行示例](example/project.properties)：用 ES4A 打开示例工程并构建运行。
