# HTML解析库

基于 [jsoup](https://github.com/jhy/jsoup) 封装，提供 HTML 文档解析、CSS 查询、文本与属性提取、相对链接补全及文档修改。

解析会补全不完整的 HTML，不用于严格格式校验，也不执行 JavaScript 或自动下载网页资源。文件读取和大文档处理应在后台执行，同一文档串行访问。

## 依赖来源

- [jsoup 1.15.4](https://jsoup.org/)
