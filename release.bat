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
IF NOT EXIST "%RELEASE_DIR%" (
	MD "%RELEASE_DIR%"
	IF ERRORLEVEL 1 GOTO 失败退出
)

REM --------------------------------------------------

:复制文件
REM 类库JAR、清单及可选许可证文件
FOR %%F IN (classes.jar library.json LICENSE LICENSE.txt) DO (
	IF EXIST "%LIBRARY_HOME%\%%F" (
		COPY /Y "%LIBRARY_HOME%\%%F" "%RELEASE_DIR%\%%F" >NUL
		IF ERRORLEVEL 1 (
			ECHO - 复制文件失败: %%F
			GOTO 失败退出
		)
	)
)

REM --------------------------------------------------

:复制目录
REM 只复制交付目录，不复制源码、构建目录或样例
FOR %%D IN (libs res assets jni icons) DO (
	IF EXIST "%LIBRARY_HOME%\%%D" (
		XCOPY "%LIBRARY_HOME%\%%D\*" "%RELEASE_DIR%\%%D\" /E /I /H /Y /Q >NUL
		IF ERRORLEVEL 2 (
			ECHO - 复制目录失败: %%D
			GOTO 失败退出
		)
	)
)
ECHO - 类库文件发布完成: "%RELEASE_DIR%"

REM --------------------------------------------------

:注册类库
REM 注册类库清单
powershell.exe -NoProfile -Command ^
	"$ErrorActionPreference='Stop';" ^
	"$file=$env:SDK_FILE;" ^
	"$sdk=Get-Content -LiteralPath $file -Raw -Encoding UTF8 | ConvertFrom-Json;" ^
	"$entry='libraries/'+$env:LIBRARY_NAME+'/library.json';" ^
	"if ($sdk.libraries -contains $entry) { exit 0 };" ^
	"$sdk | Add-Member -NotePropertyName libraries -NotePropertyValue (@($sdk.libraries | Where-Object { $_ }) + $entry) -Force;" ^
	"$json=ConvertTo-Json -InputObject $sdk -Depth 100;" ^
	"[IO.File]::WriteAllText($file,$json+[Environment]::NewLine,[Text.UTF8Encoding]::new($false));"
IF ERRORLEVEL 1 (
	ECHO - 类库注册失败！
	GOTO 失败退出
)
ECHO - 类库注册完成！


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
