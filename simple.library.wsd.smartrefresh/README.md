# SRL下拉刷新框库

基于 [SmartRefreshLayout](https://github.com/scwang90/SmartRefreshLayout) 封装，提供下拉刷新、上拉加载、内置头尾样式、二楼、自定义外观和阶段事件。

仅允许一个直接可视子组件；宿主窗口须关闭自身滚动，并为刷新框安排明确高度。所有成员在主线程使用，同步边界及插值回调应避免耗时操作。

业务完成须对应原请求序号。切换窗口会使请求失效，但不会取消应用任务。自定义头尾和二楼内容仅借用同窗口组件；固定头尾须在首次附着前配置，生效后不支持替换整个内容。

## 依赖来源

- [SmartRefreshLayout 3.0.0-alpha](https://github.com/scwang90/SmartRefreshLayout/tree/V3.0.0-alpha)
