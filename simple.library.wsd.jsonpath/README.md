# JSONPath库

提供“JSONPath操作”“JSONPath文档”和“JSONPath结果”对象，使用 JSONPath 表达式查询 JSON，并批量修改、添加、删除或重命名节点。

支持过滤和通配查询；查询结果是独立快照。未匹配与匹配到 JSON null 含义不同，读取结果前应检查是否存在及数据类型。

所有操作同步执行，大文档或复杂查询应在后台处理，同一文档串行访问。写操作失败保留原文档，不自动创建缺失的父对象。

只处理 JSON 标准数据类型，不执行脚本或自动联网。文件读取和保存由应用完成。

## 依赖来源

- [Jayway JsonPath 2.10.0](https://github.com/json-path/JsonPath)
- [json-smart 2.6.0](https://github.com/netplex/json-smart-v2)
- [accessors-smart 2.6.0](https://github.com/netplex/json-smart-v2)
- [ASM 9.7.1](https://asm.ow2.io/)
- [SLF4J API 1.7.36](https://www.slf4j.org/)
- [SLF4J NOP 1.7.36](https://www.slf4j.org/)：无输出日志实现。
- [Jakarta JSON-P 2.1.3](https://github.com/jakartaee/jsonp-api)
- [Jakarta JSON-B 2.0.0](https://github.com/jakartaee/jsonb-api)
- [Jettison 1.5.4](https://github.com/jettison-json/jettison)
- [StAX API 1.0-2](https://repo.maven.apache.org/maven2/javax/xml/stream/stax-api/1.0-2/)
