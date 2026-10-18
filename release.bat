@ECHO OFF
REM Simple扩展类库发布批处理脚本 xhwsd@qq.com 2026-10-14

REM --------------------------------------------------

:接受参数
REM 类库根目录（支持拖入类库文件夹或传递类库文件夹路径）
IF "%~1" == "" (
	ECHO - 请指定类库文件夹
	ECHO - 用法: %~nx0 "类库文件夹"
	GOTO 失败退出
)
FOR %%I IN ("%~1") DO SET "LIBRARY_HOME=%%~fI"
IF NOT EXIST "%LIBRARY_HOME%\classes.jar" (
	ECHO - 未找到类库包: "%LIBRARY_HOME%\classes.jar"
	GOTO 失败退出
)
IF NOT EXIST "%LIBRARY_HOME%\library.json" (
	ECHO - 未找到类库清单: "%LIBRARY_HOME%\library.json"
	GOTO 失败退出
)

REM --------------------------------------------------

:定义变量
REM SDK根目录
FOR %%I IN ("%~dp0..\sdk") DO SET "SDK_HOME=%%~fI"
REM 优先使用本地SDK清单，不存在时使用公开清单
SET "SDK_FILE=%SDK_HOME%\sdk.local.json"
IF NOT EXIST "%SDK_FILE%" SET "SDK_FILE=%SDK_HOME%\sdk.json"
REM 类库目录名
FOR %%I IN ("%LIBRARY_HOME%") DO SET "LIBRARY_NAME=%%~nxI"
REM SDK中的当前类库目录
SET "RELEASE_DIR=%SDK_HOME%\libraries\%LIBRARY_NAME%"
IF NOT EXIST "%SDK_FILE%" (
	ECHO - 未找到SDK入口: "%SDK_FILE%"
	GOTO 失败退出
)

REM --------------------------------------------------

:清理目录
REM 发布目录必须是SDK类库目录的直接子目录
FOR %%I IN ("%RELEASE_DIR%\..") DO IF /I NOT "%%~fI" == "%SDK_HOME%\libraries" (
	ECHO - 发布目录不在SDK类库目录内。
	GOTO 失败退出
)
REM 沿双方父目录检查，避免删除开发目录或将交付目录复制到自身
SET "SOURCE_PARENT=%LIBRARY_HOME%"
SET "TARGET_PARENT=%RELEASE_DIR%"
:检查路径
IF /I "%SOURCE_PARENT%" == "%RELEASE_DIR%" (
	ECHO - 开发目录与发布目录重叠。
	GOTO 失败退出
)
IF /I "%TARGET_PARENT%" == "%LIBRARY_HOME%" (
	ECHO - 开发目录与发布目录重叠。
	GOTO 失败退出
)
SET "PARENT_PATHS=%SOURCE_PARENT%|%TARGET_PARENT%"
FOR %%I IN ("%SOURCE_PARENT%\..") DO SET "SOURCE_PARENT=%%~fI"
FOR %%I IN ("%TARGET_PARENT%\..") DO SET "TARGET_PARENT=%%~fI"
IF /I NOT "%SOURCE_PARENT%|%TARGET_PARENT%" == "%PARENT_PATHS%" GOTO 检查路径
REM 整体替换当前类库，清除改名或删除后残留的文件
IF EXIST "%RELEASE_DIR%" (
	RD /Q /S "%RELEASE_DIR%"
	IF EXIST "%RELEASE_DIR%" (
		ECHO - 清理类库目录失败：%RELEASE_DIR%
		GOTO 失败退出
	)
)
MD "%RELEASE_DIR%"
IF ERRORLEVEL 1 GOTO 失败退出

REM --------------------------------------------------

:复制文件
REM 类库JAR、清单及可选许可证文件
FOR %%F IN (classes.jar library.json README.md LICENSE LICENSE.txt) DO (
	IF EXIST "%LIBRARY_HOME%\%%F" (
		COPY /Y "%LIBRARY_HOME%\%%F" "%RELEASE_DIR%\%%F" >NUL
		IF ERRORLEVEL 1 (
			ECHO - 复制类库文件失败: %%F
			GOTO 失败退出
		)
	)
)
ECHO - 复制类库文件成功！

REM --------------------------------------------------

:复制目录
REM 复制依赖和示例目录
FOR %%D IN (libs res assets jni icons sample) DO (
	IF EXIST "%LIBRARY_HOME%\%%D\" (
		REM 排除依赖源码和示例构建目录
		ROBOCOPY "%LIBRARY_HOME%\%%D" "%RELEASE_DIR%\%%D" /E /XD "%LIBRARY_HOME%\libs\sources" "%LIBRARY_HOME%\sample\build" /R:0 >NUL
		IF ERRORLEVEL 8 (
			ECHO - 复制类库目录失败: %%D
			GOTO 失败退出
		)
	)
)
ECHO - 复制类库目录成功！

REM --------------------------------------------------

:注册类库
REM 注册类库清单
powershell.exe -NoProfile -Command ^
	"$ErrorActionPreference='Stop';" ^
	"<# 读取清单，保留原有UTF-8 BOM状态。 #>" ^
	"$file=$env:SDK_FILE;" ^
	"$bytes=[IO.File]::ReadAllBytes($file);" ^
	"$bom=$bytes.Length -ge 3 -and $bytes[0] -eq 239 -and $bytes[1] -eq 187 -and $bytes[2] -eq 191;" ^
	"$text=[IO.File]::ReadAllText($file,[Text.Encoding]::UTF8);" ^
	"$sdk=ConvertFrom-Json -InputObject $text;" ^
	"<# 检查libraries数组；已注册时直接退出，不写文件。 #>" ^
	"if ($sdk.libraries -isnot [array] -or @($sdk.libraries | Where-Object { $_ -isnot [string] }).Count) {" ^
	"	throw 'libraries必须是现有的字符串数组。'" ^
	"};" ^
	"$entry='libraries/'+$env:LIBRARY_NAME+'/library.json';" ^
	"if ($sdk.libraries -contains $entry) { exit 0 };" ^
	"<# 定位数组内容；正则保留JSON字符串中的转义和括号。 #>" ^
	"$arrays=[regex]::Matches($text,'\x22libraries\x22\s*:\s*\[(?<body>(?:\x22(?:\\.|[^\x22\\])*\x22|[^\x22\]])*)\]');" ^
	"if ($arrays.Count -ne 1) { throw '无法唯一定位libraries数组。' };" ^
	"$body=$arrays[0].Groups['body'];" ^
	"$trimmed=$body.Value.TrimEnd();" ^
	"<# 沿用数组的换行和缩进，为空的多行数组补一级缩进。 #>" ^
	"$nl=[regex]::Match($body.Value,'\r?\n').Value;" ^
	"$indent=[regex]::Match($body.Value,'(?:\r?\n)([ \t]*)').Groups[1].Value;" ^
	"if ($nl -and -not $trimmed.Length) {" ^
	"	$indent+=if ($indent.Contains([char]9) -or -not $indent) { [string][char]9 } else { $indent }" ^
	"};" ^
	"<# 只插入新条目，不重新排版其它内容。 #>" ^
	"$addition=ConvertTo-Json -InputObject $entry -Compress;" ^
	"if ($nl) {" ^
	"	$addition=$nl+$indent+$addition" ^
	"} elseif ($trimmed.Length) {" ^
	"	$addition=' '+$addition" ^
	"};" ^
	"if ($trimmed.Length) { $addition=','+$addition };" ^
	"$updated=$text.Insert($body.Index+$trimmed.Length,$addition);" ^
	"<# 确认根级libraries仅追加了当前条目，然后写回清单。 #>" ^
	"$result=ConvertFrom-Json -InputObject $updated;" ^
	"if (@($result.libraries).Count -ne (@($sdk.libraries).Count+1) -or $result.libraries[-1] -cne $entry) {" ^
	"	throw '类库注册校验失败。'" ^
	"};" ^
	"[IO.File]::WriteAllText($file,$updated,[Text.UTF8Encoding]::new($bom));"
IF ERRORLEVEL 1 (
	ECHO - 注册到SDK清单失败！
	GOTO 失败退出
)
ECHO - 注册到SDK清单成功！


REM --------------------------------------------------

:成功退出
ECHO.
IF /I NOT "%~2" == "--no-pause" PAUSE
EXIT /B 0

REM --------------------------------------------------

:失败退出
ECHO.
IF /I NOT "%~2" == "--no-pause" PAUSE
EXIT /B 1
