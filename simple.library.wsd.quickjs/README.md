# QuickJS脚本库

基于 [QuickJS](https://bellard.org/quickjs/) 和 [QuickJS Wrapper](https://github.com/HarlonWang/quickjs-wrapper) 封装，提供 JavaScript 脚本、模块、双向调用、异步调度、计时器、工作线程、字节码及运行时资源管理。

支持 Android 7.0 及以上；不包含浏览器 DOM、Node.js 和 qjs 的 std/os 模块。字节码及序列化数据仅用于同版本、可信来源。Simple 自定义方法使用成员列表显式绑定；工作线程不支持共享缓冲区及转移列表。

## 依赖来源

- [QuickJS f209a855](https://github.com/bellard/quickjs/tree/f209a8556bc6e4e87cd0fb8c0932b47c5f3eeae5)
- [QuickJS Wrapper 3.2.3](https://github.com/HarlonWang/quickjs-wrapper)
