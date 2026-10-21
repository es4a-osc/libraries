# Lua脚本库

基于 Lua 5.1.5 和 LuaJava，提供 Lua 实例、堆栈、值、表、函数和宿主回调对象，支持代码及文件执行、全局变量访问和 Simple 自定义函数注册。

创建 `Lua实例` 后即可执行脚本，结束时调用 `关闭()`。表、函数和值由实例取得，使用完毕须释放；关闭实例后，其全部引用失效，不允许跨实例使用。

函数调用支持多个返回值，保留 nil 的位置。Lua 5.1 数值采用双精度表示，长整数可能丢失精度。

随库原生库仅支持 ARM 32 位，不提供 arm64、x86 或 x86_64 版本。脚本与应用共享进程、权限和 Java 对象；调用线程不会自动切换，同一实例应避免跨线程交错操作。

Lua 语法可参考 [Lua 5.1 手册](https://www.lua.org/manual/5.1/)。

## 依赖来源

- [Lua 5.1.5](https://www.lua.org/versions.html#5.1)
- [LuaJava](https://github.com/LuaDist/luajava)：使用 Android 适配绑定。
