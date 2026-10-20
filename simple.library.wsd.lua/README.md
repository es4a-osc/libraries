# Lua脚本库

基于 LuaJava 和 Lua **5.1.5**，提供实例、堆栈、值、表、函数和宿主回调对象。用法见[独立示例](sample/)。

| 类型 | 用途 |
| --- | --- |
| `Lua实例` | 创建和关闭解释器，执行代码／文件，读写全局值，创建表及注册宿主函数 |
| `Lua堆栈` | 栈顶、索引、压栈、取值和弹栈；`Lua实例` 继承它 |
| `Lua值` | 查询类型、读取值及显式释放稳定引用 |
| `Lua表` | 文本键、数值键、嵌套表、成员删除和键快照 |
| `Lua函数` | 直接传入参数并取得多个返回值 |
| `Lua回调` | 由 Simple 对象继承，覆写 `处理调用(堆栈)` 注册宿主函数 |

普通对象的完整类型名前缀为 `simple.library.wsd.lua.`。使用 `创建 Lua实例` 即初始化，结束时调用 `关闭()`；堆栈、值、表和函数由实例取得，不直接创建。

- `执行代码`、`执行文件`、`Lua函数.调用`、`调用函数` 返回 `Lua值()`，无结果时为空数组；保留结果顺序以及中间、末尾的 nil。高层操作成功或失败都恢复原栈顶，加载和执行失败进入 Simple 错误分支。
- 参数使用 `变体型()`；基本值自动转换，字节集作为二进制字符串，显式对象保留 Java 桥接，同一实例的 `Lua值` 按引用传入。Lua 5.1 数值沿用双精度规则，长整数不保证完整精度。
- 返回的每个值，包括 nil、表、函数、键快照及通过 `到表／到函数` 复制的引用，使用后分别调用 `释放()`。引用不依赖原栈位置，不允许跨实例使用；关闭实例会使全部引用失效，随后释放或重复关闭均无副作用。不依赖垃圾回收及时释放原生资源。
- `Lua表` 和全局读写使用原始字段访问，不触发元方法。`取长度()` 返回 Lua 5.1 的数组边界，有空洞时不代表成员总数；`取键()` 返回顺序不保证的独立键快照。
- 回调同步运行，业务参数从 1 开始，不包含 LuaJava 自身参数；压入结果后返回数量。回调堆栈仅在当前调用帧和线程有效，不能保存后继续使用；回调中不允许关闭正在执行的实例。参考[宿主加法](sample/src/simple/library/wsd/lua/sample/宿主加法.simple)。
- 单次方法按实例串行保护，多步底层栈操作仍须避免跨线程交错；调用线程不会自动切换到界面线程。底层正索引从 1 开始，负索引从 -1 开始，不开放伪索引。

随库提供 `libs/luajava-1.1.0.jar` 和 `jni/armeabi/libluajava.so`。JAR 沿用当前 Android 适配包，文件名按项目约定标记版本；原生库仅支持 **32 位 ARM**，不提供 arm64、x86 或 x86_64 构建。执行文件仍受 Android 文件访问限制；脚本与应用共享进程、权限和 Java 对象，不是安全沙箱。

对应源码包为 [luajava-1.1.0-sources.jar](libs/sources/luajava-1.1.0-sources.jar)，由 ES4A 根据与当前 JAR 全部成员签名核对一致的 [AndroLua 历史 LuaJava 绑定](https://github.com/mkottman/AndroLua/tree/5fc281797e02418c5a924b6263eb410a5af45749/src/org/keplerproject/luajava)打包，保留原始 JavaDoc 和许可证，供 IDE 源码关联使用。

LuaJava 与 Lua 的 MIT 许可分别见 `libs/LICENSE-luajava.txt`、`libs/LICENSE-lua.txt`；语法见 [Lua 5.1 手册](https://www.lua.org/manual/5.1/)。图标来自 [Tabler Icons](https://github.com/tabler/tabler-icons)，许可见 `icons/LICENSE.txt`。
