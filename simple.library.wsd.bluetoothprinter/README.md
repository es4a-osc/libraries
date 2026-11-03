# 蓝牙打印机库

基于 [群索打印 SDK](https://www.qsprinter.com/download.html) 封装，提供经典蓝牙打印机连接，以及 ESC/POS 文本、图片和条码打印。

需要支持蓝牙 SPP 及相应指令的打印机，并完成设备配对和权限授权。同一时刻仅允许一个组件占用连接；文本采用 GBK，纸宽、字符集、切纸等能力取决于实际打印机。

## 依赖来源

- [群索（Qunsuo/QS）打印 SDK](https://www.qsprinter.com/download.html)
