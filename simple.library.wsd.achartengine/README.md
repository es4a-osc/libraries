# AChartEngine图表库

提供饼状图、柱状图和折线图三个可视组件。先执行 `初始化图表`，逐项 `添加数据`，最后 `构建图表`；追加数据后再次构建，重新初始化会清空数据和图形。

- 使用 AChartEngine 1.0.0，提供单组系列图表。
- 在界面线程操作，字体大小按 SP，颜色使用 ARGB 整数。不请求权限、不联网，不依赖原生 SO，不提高当前 SDK 的最低 Android 版本。
- 柱状图和折线图为单组系列，横轴按项目添加顺序显示，允许零和负数；数值和坐标范围必须有限。
- 饼状图数值必须非负，非空数据中至少有一个正数；每个项目单独设置颜色。
- 没有数据时构建会清空图形；反复构建不改变数据或扩大坐标。初始化前、销毁后、非界面线程调用，以及无效参数都会抛出异常。
- 背景颜色等图表样式在构建时应用；外部改变背景颜色后重新构建即可同步图形。

用法和验证操作见[示例项目](sample/)。使用现有公共 `build.bat`、`release.bat` 构建和安装本库，不需要独立构建脚本。

依赖 JAR 的 SHA-256：`815e880f885330f85e88ae43845123e9327d7a1ecc6603f46e69ca383e9a0fb9`。
AChartEngine 使用 Apache-2.0，许可文本见 [libs/LICENSE-achartengine.txt](libs/LICENSE-achartengine.txt)，上游源码见 [AChartEngine](https://github.com/ddanny/achartengine)。

组件图标使用 Tabler Icons 的 chart-pie、chart-bar、chart-line；MIT 许可文本见 [icons/LICENSE](icons/LICENSE)。
