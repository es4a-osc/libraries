# Fastjson2库

基于 [Fastjson2](https://github.com/alibaba/fastjson2) 封装，提供 JSON 文本和字节解析、类型化数据读写、JSONB 顺序读写、基础 JSONPath、读写配置、过滤、运行库集合及哈希表互转、批量对象映射及自定义 Simple 普通对象编解码。

采用官方 Android 5 兼容版 JAR，适用于本 SDK 的 Android 7 及以上项目。此版本仅支持根、属性和非负下标路径，不包含 JSON Schema、路径过滤、通配符及聚合；复杂路径可使用独立的 JSONPath 库。

值和子节点采用复制语义，修改子节点后须显式写回；对象和配置在同一线程串行使用。JSONB 顺序写入数组需要显式给出元素数量，外部符号表去重后最多 32768 项。回调同步执行，不得重入自身；保留的属性名不能重复，过滤结果最多嵌套 512 层。复杂数据应在后台处理。大整数和高精度小数用十进制文本读写；转换为运行库 JSON值 时受运行库数值能力限制；转回 Simple 数据时，整数超长或小数无法保持十进制文本往返精度会报错。Simple 对象映射仅用于业务数据对象，不能直接映射窗口、组件或资源持有者。普通对象内的运行库数据字段不接受内部引用；会生成内部引用的编码同样报错。共享容器可关闭 `ReferenceDetection` 按重复数据输出，循环图使用 `数据转值` 与 `到Simple数据` 整体互转。输入中的字面量 `$ref` 须显式启用读取特性 `DisableReferenceDetect`。

## 依赖来源

- [Fastjson2 2.0.66.android5](https://github.com/alibaba/fastjson2)
