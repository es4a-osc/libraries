# 城市选择库

提供整页弹出的城市选择界面，支持中文或拼音搜索、字母索引、悬浮分组、热门城市、自定义城市数据和定位结果回填。

使用 JDK 11、Android API 30 编译，最低支持 API 24，采用 AndroidX。界面线程调用，在所属 Simple 窗口显示后打开，切换窗口时自动关闭。定位由应用自行实现，本库不包含定位 SDK，也不申请定位权限。

通过 `置主题` 使用继承 `CityPickerDialogTheme` 的主题，定制搜索、列表、热门城市、索引及悬浮提示的 `cp*` 主题属性；通过 `置动画` 指定窗口动画。配置用于下次显示。

内置数据库随 CityPicker 1.0.2 移植，不保证行政区划及时更新。城市代码保留数据源原值，不应当作国家行政区划代码；应用可通过自定义城市数据替换内置列表。

## 依赖来源

- [CityPicker 1.0.2：源码、资源及城市数据库](https://github.com/xuexiangjys/CityPicker/tree/1.0.2)
- [AndroidX AppCompat 1.3.1](https://developer.android.com/jetpack/androidx/releases/appcompat)
- [AndroidX RecyclerView 1.2.1](https://developer.android.com/jetpack/androidx/releases/recyclerview)
- [Tabler Icons](https://github.com/tabler/tabler-icons)

[运行示例](example/project.properties)：用 ES4A 打开示例工程并构建运行。
