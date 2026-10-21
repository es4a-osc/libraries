# OTG通讯库

提供“OTG通讯”非可视组件，通过沁恒（WCH）CH34x 驱动连接 USB 转串口设备，进行串口数据收发。

手机或平板须支持 USB Host/OTG，并配备合适的转接线。当前只识别 `1a86:7523`、`1a86:5523` 两组 USB VID:PID，不能据此认为支持所有 CH34x 或其它 USB 串口设备。

连接前须匹配设备接口电平、接线和串口参数。需要 USB 授权时，首次打开返回假；授权完成后再次打开。关闭或拔出设备后停止接收。

## 依赖来源

- [沁恒（WCH）CH34x Android 驱动](https://www.wch.cn/downloads/CH341SER_ANDROID_ZIP.html)：随库旧版驱动的具体发布版本尚未确认。
