<#
编译 QuickJS JNI 补充层；使用已校验的引擎 AAR 和指定 Zig，不下载工具或修改 SDK。
运行所需原生库输出到本库 jni/；中间文件留在 build/native/。
树先生 xhwsd@qq.com
#>
[CmdletBinding()]
param (
	[string]$Zig = $env:ES4A_ZIG,
	[string]$SdkRoot
)

$ErrorActionPreference = 'Stop'
$libraryRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
if (-not $Zig) {
	$Zig = Join-Path $libraryRoot 'build/toolchain/ziglang/zig.exe'
}
if (-not $SdkRoot) {
	$SdkRoot = [IO.Path]::GetFullPath((Join-Path $libraryRoot '../../sdk'))
}
$jdkInclude = Join-Path $SdkRoot 'tools/jdk-11.0.32.1/include'
$archivePath = Join-Path $libraryRoot 'aars/wrapper-android-3.2.3.aar'
foreach ($required in @($Zig, $archivePath, (Join-Path $jdkInclude 'jni.h'))) {
	if (-not (Test-Path -LiteralPath $required -PathType Leaf)) {
		throw "缺少原生构建输入：$required"
	}
}

$work = Join-Path $libraryRoot 'build/native'
[IO.Directory]::CreateDirectory($work) | Out-Null
$utf8 = [Text.UTF8Encoding]::new($false)
$source = [IO.File]::ReadAllText((Join-Path $PSScriptRoot 'intrinsics.js'))
$literal = 'static const char ES4A_INTRINSICS[] =' + "`r`n"
foreach ($line in ($source -split '\r?\n')) {
	$literal += (ConvertTo-Json -InputObject ($line + "`n") -Compress) + "`r`n"
}
$literal += ";`r`n"
[IO.File]::WriteAllText((Join-Path $work 'intrinsics.inc'), $literal, $utf8)

Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [IO.Compression.ZipFile]::OpenRead($archivePath)
try {
	$targets = [ordered]@{
		'arm64-v8a' = 'aarch64-linux-android'
		'armeabi-v7a' = 'arm-linux-androideabi'
		'x86' = 'x86-linux-android'
		'x86_64' = 'x86_64-linux-android'
	}
	foreach ($abi in $targets.Keys) {
		$inputDirectory = Join-Path $work $abi
		$outputDirectory = Join-Path $libraryRoot "jni/$abi"
		[IO.Directory]::CreateDirectory($inputDirectory) | Out-Null
		[IO.Directory]::CreateDirectory($outputDirectory) | Out-Null
		$entry = $archive.GetEntry("jni/$abi/libquickjs-android-wrapper.so")
		if (-not $entry) {
			throw "引擎包缺少 ABI：$abi"
		}
		$engine = Join-Path $inputDirectory 'libquickjs-android-wrapper.so'
		[IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $engine, $true)
		$arguments = @(
			'cc', '-target', $targets[$abi], '-O2', '-fno-sanitize=all',
			'-shared', '-fPIC', '-ffreestanding', '-fno-stack-protector', '-nostdlib',
			'-DCONFIG_BIGNUM', "-I$(Join-Path $PSScriptRoot 'include')", "-I$jdkInclude",
			"-I$work", "-I$PSScriptRoot", (Join-Path $PSScriptRoot 'bridge.c'), $engine,
			'-Wl,-z,max-page-size=16384', '-Wl,-soname,libes4a-quickjs.so',
			'-o', (Join-Path $outputDirectory 'libes4a-quickjs.so')
		)
		& $Zig @arguments
		if ($LASTEXITCODE -ne 0) {
			throw "JNI 构建失败：$abi"
		}
		Write-Output "JNI 构建完成：$abi"
	}
} finally {
	$archive.Dispose()
}
