@ECHO OFF
REM Simple扩展类库构造批处理脚本 xhwsd@qq.com 2026-10-14

REM --------------------------------------------------

:接受参数
REM 类库根目录（支持拖入类库文件夹或传递类库文件夹路径）
IF "%~1" == "" (
	ECHO - 请指定类库文件夹
	ECHO - 用法: %~nx0 "类库文件夹"
	GOTO 失败退出
)
FOR %%I IN ("%~1") DO SET "LIBRARY_HOME=%%~fI"
IF NOT EXIST "%LIBRARY_HOME%\src" (
	ECHO - 未找到类库源码目录: %LIBRARY_HOME%\src
	GOTO 失败退出
)

REM --------------------------------------------------

:定义变量
REM JavaSDK根目录
FOR %%I IN ("%~dp0..\sdk\tools\jdk-11") DO SET "JAVA_HOME=%%~fI"
REM Android指定API等级目录（编译Simple运行库需要）
FOR %%I IN ("%~dp0..\sdk\tools\android\platforms\android-30") DO SET "ANDROID_LEVEL=%%~fI"
REM Simple根目录（编译器和所有运行库JAR包所在的根目录）
FOR %%I IN ("%~dp0..\sdk\simple") DO SET "SIMPLE_HOME=%%~fI"

REM --------------------------------------------------

:编译源文件
REM 源文件编译后输出类目录
SET "CLASSES_DIR=%LIBRARY_HOME%\build\classes"
REM 检验指定目录是否存在
IF EXIST "%CLASSES_DIR%" (
	REM 删除指定目录
	RD /Q /S "%CLASSES_DIR%"
)
REM 创建目录
MD "%CLASSES_DIR%"
REM 所有依赖JAR包
SET "CLASS_PATH=%ANDROID_LEVEL%\android.jar;%SIMPLE_HOME%\SimpleAndroidRuntime.jar;%LIBRARY_HOME%\libs\*"
REM 欲要编译源文件列表
SET "SOURCE_LIST=%LIBRARY_HOME%\build\source.txt"
REM javac按UTF-8读取参数文件，临时切换代码页生成源码列表后恢复
FOR /F "tokens=2 delims=:" %%I IN ('CHCP') DO SET "SOURCE_CODE_PAGE=%%I"
CHCP 65001 >NUL
DIR "%LIBRARY_HOME%\src\*.java" /S /B >"%SOURCE_LIST%"
CHCP %SOURCE_CODE_PAGE% >NUL
REM 执行 javac.exe 编译源文件
"%JAVA_HOME%\bin\javac" -Xlint:unchecked -Xlint:deprecation -Xdiags:verbose -encoding utf-8 -d "%CLASSES_DIR%" -classpath "%CLASS_PATH%" "@%SOURCE_LIST%"
REM 检验前面 javac.exe 控制台程序返回值是否为 0
IF NOT %ERRORLEVEL% == 0 (
	ECHO - 编译源文件失败！
	GOTO 失败退出
)
ECHO - 编译源文件成功！

REM --------------------------------------------------

:打包类文件
REM 执行 jar.exe 打包类文件
"%JAVA_HOME%\bin\jar" -cf "%LIBRARY_HOME%\classes.jar" -C "%CLASSES_DIR%" .
REM 检验前面 jar.exe 控制台程序返回值是否为 0
IF NOT %ERRORLEVEL% == 0 (
	ECHO - 打包类文件失败！
	GOTO 失败退出
)
ECHO - 打包类文件成功！

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
