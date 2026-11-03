# CV日历库

基于 [CalendarView](https://github.com/huanghaibin-dev/CalendarView) 封装，提供年月周日历、单选与范围及多选、日期标记、周月收缩联动、公农历转换和自定义绘制。

公历支持 1900—2099 年，农历转换另接受对应的 1899 年末日期。节日与节气来自内置数据，不代表法定放假安排；不包含上游付费功能。

组件在主线程使用，同步绘制和拦截回调中不能修改日历；绘图对象仅在当前回调中有效。

## 依赖来源

- [CalendarView 3.7.1](https://github.com/huanghaibin-dev/CalendarView)
