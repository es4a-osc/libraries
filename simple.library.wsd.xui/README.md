# XUI界面库

将 XUI 的按钮、文本、编辑、图片、选择器、进度、列表、标签和布局组件接入现有 Simple 窗口。提供标准可视组件属性，以及各组件的数据、状态和事件接口。另提供对话框、菜单、日期及多级选择器、引导、消息提示和界面工具。

导出类型统一使用 `XUI` 前缀，如 `XUI按钮`、`XUI阴影按钮`、`XUI超级按钮` 和 `XUI对话框`；完整类型位于 `simple.library.wsd.xui` 命名空间。

使用 JDK 11、Android API 30 编译，最低支持 API 24，统一使用 AndroidX。不会导出 XUI 的独立 Activity、Fragment、引导页宿主或图片预览窗口；图片裁剪、缩放和编辑组件可直接嵌入 Simple 窗口。

弹层功能须在所属 Simple 窗口显示后调用，切换窗口时自动关闭；侧滑返回容器通过 Simple 窗口的返回键事件处理返回。对话框和气泡的自定义内容暂借当前窗口内的组件，关闭后恢复其原父容器及可见性；标题、状态及附属内容在解除配置或销毁时恢复；确认前校验事件可阻止提交。日期与多级选择器的外观配置在下次显示时生效，滚动变化不替代确认结果。列表全量替换数据时清除选择，单项更新保留选择。滚动容器支持布局完成后的主动定位，位置与范围均采用物理像素。

富文本可设置链接或点击标识，点击通过事件回传，不会自动打开网址；关闭富文本工具后，已应用片段的旧回调失效。底部列表和网格菜单支持图标、红点及禁用项；用户取消与所有关闭原因分别通知。引导支持图片、区域聚焦、点击或手动推进及跳过；按标识仅显示一次的记录保存在当前应用中，可主动重置。样式配置在下一次显示时生效。

组件内部使用独立 XUI 主题上下文。`XUI主题工具` 可设置后续新建组件的样式覆盖和字体资产；不重建窗口，也不修改已有组件或 Simple 宿主主题。自定义样式须随应用打包，字体须放入应用资产目录。字体资产配置用于新建的文字视图，不覆盖调用方随后设置的字体。图片接口支持成员说明中列出的资源标识、资产文件名或外部路径；轮播、图片网格、图文列表及联动网页支持其成员说明列出的网络地址，相应组件会声明网络权限。图片输出路径须可写且目录已存在，相对路径以应用内部文件目录为基准。图片编辑可输出 PNG 或 JPEG；JPEG 不保留透明通道。定时公告采用 Unix 毫秒截止时间，零表示永久；移出窗口时暂停任务，重新附着后移除已过期消息。

## 依赖来源

- [XUI 1.2.1](https://github.com/xuexiangjys/XUI/tree/1.2.1)
- [AndroidX RecyclerView 1.2.1](https://developer.android.com/jetpack/androidx/releases/recyclerview)
- [Material Components 1.4.0](https://github.com/material-components/material-components-android/releases/tag/1.4.0)
- [Glide 4.11.0](https://github.com/bumptech/glide/releases/tag/v4.11.0)
- [Tabler Icons](https://github.com/tabler/tabler-icons)
