-- 专用临时 SQL Server 初始化；SQL Server 2005+，不含账号密码。
-- 新建 es4a 数据库及一张带标记演示表，不更改服务器配置。
-- 已有同名数据库时立即停止，不向已有库继续建表；库表保留供查看。
IF DB_ID(N'es4a') IS NOT NULL
BEGIN
	RAISERROR(N'数据库 es4a 已存在，初始化已停止；请人工核对，不覆盖。',16,1);
	RETURN;
END;
EXEC(N'CREATE DATABASE [es4a]');
EXEC(N'
USE [es4a];
CREATE TABLE [dbo].[es4a_library_example] (
	[id] nvarchar(36) NOT NULL PRIMARY KEY,
	[name] nvarchar(64) NOT NULL,
	[phone] nvarchar(32) NOT NULL,
	[note] nvarchar(max) NULL
);
EXEC sys.sp_addextendedproperty
	@name=N''ES4A_Example'', @value=N''ES4A MSSQL example v1'',
	@level0type=N''SCHEMA'', @level0name=N''dbo'',
	@level1type=N''TABLE'', @level1name=N''es4a_library_example'';
');
