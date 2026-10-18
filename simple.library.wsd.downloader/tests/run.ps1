# 在本机 HTTP 服务上验证下载状态机，不替代 Android 设备测试。
$ErrorActionPreference = 'Stop'
$downloadRoot = Split-Path -Parent $PSScriptRoot
$downloadWorkspace = Split-Path -Parent (Split-Path -Parent $downloadRoot)
$downloadJava = Join-Path $downloadWorkspace 'sdk/tools/jdk1.8.0_503/bin'
$downloadClasses = Join-Path $downloadRoot 'build/test-classes'
$downloadOptions = $env:JAVA_TOOL_OPTIONS
try {
	$env:JAVA_TOOL_OPTIONS = '-Dfile.encoding=UTF-8'
	& (Join-Path (Split-Path -Parent $downloadRoot) 'build.bat') $downloadRoot --no-pause
	if ($LASTEXITCODE -ne 0) {
		throw 'Library build failed'
	}
	New-Item -ItemType Directory -Path $downloadClasses -Force | Out-Null
	& (Join-Path $downloadJava 'javac.exe') -encoding UTF-8 -cp (Join-Path $downloadRoot 'classes.jar') -d $downloadClasses (Join-Path $PSScriptRoot 'DownloadEngineTest.java')
	if ($LASTEXITCODE -ne 0) {
		throw 'Test compilation failed'
	}
	$downloadData = Join-Path $downloadRoot ('build/http-test-' + [Guid]::NewGuid().ToString('N'))
	& (Join-Path $downloadJava 'java.exe') -cp ($downloadClasses + ';' + (Join-Path $downloadRoot 'classes.jar')) simple.library.wsd.downloader.internal.DownloadEngineTest $downloadData
	if ($LASTEXITCODE -ne 0) {
		throw 'Download regression failed'
	}
} finally {
	$env:JAVA_TOOL_OPTIONS = $downloadOptions
}
