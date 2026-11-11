# MPAC图表库

基于 [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) 封装，提供折线、柱状、横向柱状、饼状、散点、气泡、雷达、蜡烛及组合图，支持分类和数值横坐标、堆叠柱、双数值轴、样式与格式器、数据更新、手势选择、视图控制和图片导出。

需要 Android 7.0（API 24）及以上，图表在界面线程操作。先初始化，再添加数据并构建；修改数据或配置后刷新可保留缩放和平移。数值横坐标须非递减，不能与分类数据混用。雷达图至少三个项目，饼图和雷达数值须非负，非空饼图至少一个正数。

配置和数组应用时复制，格式器回调保持同一实例；格式器中不能操作图表。图片导出需已完成布局，返回 PNG 或 JPEG 字节，不写文件。低层自定义绘制器、Canvas、原生视图和第三方对象不作为 Simple 接口导出。

依赖 [MPAndroidChart 3.2.0](https://github.com/PhilJay/MPAndroidChart/tree/v3.2.0)。
