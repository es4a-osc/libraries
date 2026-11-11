# JSONPath库

基于 [Jayway JsonPath](https://github.com/json-path/JsonPath) 封装，通过 JSONPath 表达式查询 JSON，支持读取选项、限量查询和逐项访问，并批量新增、映射、修改、删除或重命名节点。

操作同步执行，大文档或复杂查询应在后台处理，同一文档串行访问；回调中可查询或复制，但不能重入修改或访问当前文档。未匹配与匹配到 JSON null 含义不同；不执行脚本、自动联网或自动创建缺失的父对象。

## 依赖来源

- [Jayway JsonPath 2.10.0](https://github.com/json-path/JsonPath)
