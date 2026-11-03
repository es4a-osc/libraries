# OTG通讯库

基于 [沁恒 CH34x Android 驱动](https://www.wch.cn/downloads/CH341SER_ANDROID_ZIP.html) 封装，提供 USB 转串口设备连接及串口数据收发。

设备须支持 USB Host/OTG，具备合适的接线、接口电平和串口参数，并取得 USB 授权。当前仅识别 `1a86:7523`、`1a86:5523` 两组 VID:PID；首次打开遇到授权请求时返回假，授权后需重新打开。

## 依赖来源

- [沁恒（WCH）CH34x Android 驱动](https://www.wch.cn/downloads/CH341SER_ANDROID_ZIP.html)
