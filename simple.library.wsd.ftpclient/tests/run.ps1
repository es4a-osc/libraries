# 构建当前类库并执行 FTP 回归；真实 SFTP 测试另外需要本机 Python Paramiko。
$ErrorActionPreference = 'Stop'
$transferRoot = Split-Path -Parent $PSScriptRoot
$transferWorkspace = Split-Path -Parent (Split-Path -Parent $transferRoot)
$transferJava = Join-Path $transferWorkspace 'sdk/tools/jdk1.8.0_503/bin'
$transferPreviousOptions = $env:JAVA_TOOL_OPTIONS
try {
	$env:JAVA_TOOL_OPTIONS = '-Dfile.encoding=UTF-8'
	& (Join-Path (Split-Path -Parent $transferRoot) 'build.bat') $transferRoot --no-pause
	if ($LASTEXITCODE -ne 0) { throw 'Library build failed' }
	$transferClasses = Join-Path $transferRoot 'build/test-classes'
	New-Item -ItemType Directory -Path $transferClasses -Force | Out-Null
	$transferCp = (Join-Path $transferRoot 'classes.jar') + ';' + (Join-Path $transferRoot 'libs/*')
	$transferSources = @(Get-ChildItem -LiteralPath $PSScriptRoot -Filter '*.java' -Recurse | Select-Object -ExpandProperty FullName)
	& (Join-Path $transferJava 'javac.exe') -encoding UTF-8 -cp $transferCp -d $transferClasses $transferSources
	if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed' }
	& (Join-Path $transferJava 'java.exe') -cp ($transferClasses + ';' + $transferCp) simple.library.wsd.ftpclient.SftpHostTrustTest
	if ($LASTEXITCODE -ne 0) { throw 'Host trust regression failed' }
	$transferData = Join-Path $transferRoot ('build/ftp-test-' + [Guid]::NewGuid().ToString('N'))
	try {
		& (Join-Path $transferJava 'java.exe') -cp ($transferClasses + ';' + $transferCp) simple.library.wsd.ftpclient.FtpSessionTest $transferData
		if ($LASTEXITCODE -ne 0) { throw 'FTP regression failed' }
	} finally {
		$transferData = [IO.Path]::GetFullPath($transferData)
		$transferBuild = [IO.Path]::GetFullPath((Join-Path $transferRoot 'build'))
		if (-not $transferData.StartsWith($transferBuild + '\', [StringComparison]::OrdinalIgnoreCase)) { throw 'Invalid test directory' }
		if (Test-Path -LiteralPath $transferData) { Remove-Item -LiteralPath $transferData -Recurse -Force }
	}
} finally {
	$env:JAVA_TOOL_OPTIONS = $transferPreviousOptions
}
