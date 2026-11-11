# HTML解析库

基于 [jsoup](https://github.com/jhy/jsoup) 封装，提供 HTML/XML 解析、CSS/XPath 查询、DOM 节点处理、正文清洗、表单字段提取和输出配置。

解析会补全不完整的 HTML，不用于严格格式校验，也不执行 JavaScript 或自动下载网页资源。文件读取和大文档处理应在后台执行，同一文档串行访问。正文清洗只适用于 HTML 正文片段，不适用于脚本、样式或 URL 字符串；展示和保存应使用清洗输出。

## 依赖来源

- [jsoup 1.15.4](https://jsoup.org/)
