# CalendarView日历库

提供年月周日历、默认／单选／范围／多选、日期标记、周月收缩联动、公农历转换和 Simple 自定义绘制。

- 公历支持 1900—2099 年；农历转换另接受对应的 1899 年末日期。节日与节气采用上游内置数据，不提供法定放假安排。
- `日历框` 可独立使用；`日历布局` 内放一个日历框及一个内容容器。所有操作在主线程执行；同步绘制和拦截回调中不能修改日历。
- 绘图对象仅在当前同步回调中有效；图片需提前载入，日历销毁时自动释放。标记附加数据中的对象按引用保留，不随进程重建恢复。
- 主要用法见 [example](example/)。本库封装开源版本，不包含上游付费的横竖切换等能力。

## 依赖来源

- [CalendarView 3.7.1](https://github.com/huanghaibin-dev/CalendarView)，源码取自提交 `f5479ea3baefdbba2453cea19a208eca83baeb9e`，适配说明见 [vendor/CalendarView](vendor/CalendarView/)
- [AndroidX ViewPager 1.0.0](https://developer.android.com/jetpack/androidx/releases/viewpager)
- [AndroidX RecyclerView 1.2.1](https://developer.android.com/jetpack/androidx/releases/recyclerview)
- [AndroidX Annotation 1.1.0](https://developer.android.com/jetpack/androidx/releases/annotation)，仅编译使用
