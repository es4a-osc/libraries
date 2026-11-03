# MPAC图表库

提供 MPAC折线图、MPAC柱状图、MPAC横向柱状图、MPAC饼状图、MPAC散点图、MPAC气泡图、MPAC雷达图、MPAC蜡烛图和 MPAC组合图，支持多数据组、选择事件、拖动及缩放。

使用 JDK 11、Android API 30 编译，最低支持 API 24。MPAndroidChart AAR 与 AndroidX 注解通过 `library.gradle` 声明，由 SDK 工具链统一准备。

图表在界面线程操作。先初始化、设置项目名称、添加数据，再构建显示；饼图不需要项目名称。横轴按分类索引排列，不是任意数值或时间轴。

雷达图至少需要三个项目；饼图和雷达图数值须非负，饼图非空数据的总和须大于零。气泡大小须非负，蜡烛图的最高、最低值须包含开盘和收盘值。

公共接口为 MPAC图表和 MPAC坐标图表。

## 依赖来源

- [MPAndroidChart 3.2.0](https://github.com/PhilJay/MPAndroidChart/tree/v3.2.0)

[运行示例](example/project.properties)：用 ES4A 打开示例工程并构建运行。
