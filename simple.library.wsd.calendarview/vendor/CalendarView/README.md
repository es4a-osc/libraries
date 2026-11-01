# CalendarView 上游源码

来源：[huanghaibin-dev/CalendarView](https://github.com/huanghaibin-dev/CalendarView)，开源版本 3.7.1，固定提交 `f5479ea3baefdbba2453cea19a208eca83baeb9e`。保留原署名及 [许可证](../../libs/LICENSE-CalendarView.txt)。

本地适配：

- Java 包重定位至 `simple.library.wsd.calendarview.vendor`，资源统一使用 `es4a_cv_` 前缀；通过 `library.gradle` 与封装一并编译。
- 增加代码构建布局、动态配置、选择状态查询及动画释放入口，连接 Simple 的组件、同步绘制与事件模型。
- 纯日期运算使用 UTC 日序，补齐公历 1900 年初对应的农历 1899 年月份；修正不限最大跨度时的范围校验及填满高度的最小项高度。
- RecyclerView 使用有效位置检查；年视图按设备版本读取可用窗口尺寸，保留低版本实现。
- 动态切换周月模式立即转换，取消实例持有的动画；首次附着后保留窗口往返的展开状态。

本目录保留上游源码排版；自主维护的接口、日期快照和绘图桥接位于 `../../src/`。
