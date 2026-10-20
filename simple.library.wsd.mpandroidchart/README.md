# MPAndroidChart图表库

提供折线图、柱状图、横向柱状图、饼状图、散点图、气泡图、雷达图、蜡烛图和组合图。使用 MPAndroidChart 2.1.5。

所有图表在界面线程操作。先初始化、设置项目名称（饼图除外）、添加数据，再构建；支持多数据组和选择事件。直角坐标图支持左右轴、拖动及缩放，折线支持平滑；组合图可组合折线、柱状、散点、气泡和蜡烛数据。

- 横轴按分类索引排列，不是任意数值或时间轴。数据数组必须与项目名称等长，传入后复制；无效数据抛出异常并保留已有数据。
- 雷达图至少三个项目，数值非负；气泡大小非负；饼图非负且非空数据总和必须为正。蜡烛图最高、最低值必须包含开盘和收盘值。
- 图上气泡标注大小，蜡烛标注最高值；选择事件返回数值轴值，蜡烛返回收盘值。每组颜色使用 ARGB；蜡烛上涨空心、下跌实心。动画默认关闭，可设置毫秒时长；更新或销毁时取消动画。
- 不请求权限、不联网、不依赖原生 SO，不改变当前 SDK 的最低 Android 版本。低版本设备仍需实际验证。

使用现有公共 `build.bat`、`release.bat` 构建并安装，调用与验证操作见[示例项目](sample/)。组件名称与 AChartEngine 库可以相同，使用完整包名区分。

依赖原始文件 SHA-256：`04bc96d10c70884dee8c4dcf76ec3aa068083949a51e920efa8fece3f51379db`。
MPAndroidChart Copyright 2015 Philipp Jahoda，Apache-2.0，见 [许可证](libs/LICENSE-MPAndroidChart.txt)和[上游 v2.1.5 源码](https://github.com/PhilJay/MPAndroidChart/tree/v2.1.5)。

对应源码包为 [mpandroidchartlibrary-2-1-5-sources.jar](libs/sources/mpandroidchartlibrary-2-1-5-sources.jar)，由 ES4A 根据官方 v2.1.5 标签[源码](https://github.com/PhilJay/MPAndroidChart/tree/e3a7b0969b97f4014122a6dce275c20928f14088/MPChartLib/src)打包，保留原始 JavaDoc 和许可证，供 IDE 关联源码使用。

图标使用 Tabler Icons 描边 SVG，MIT [许可证](icons/LICENSE)。折线使用 chart-dots，与 AChartEngine 的 chart-line 区分；柱图使用 chart-bar-popular，横向柱图旋转此图形；组合图使用 chart-covariate，其余按图表类型使用对应图标。
