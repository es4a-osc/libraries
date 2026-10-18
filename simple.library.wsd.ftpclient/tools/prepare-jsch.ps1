# 只转换 Java 字节码，不修改上游 Java 源码或算法配置；保留官方原包供重建核验。
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$sftpRoot = Split-Path -Parent $PSScriptRoot
$sftpWorkspace = Split-Path -Parent (Split-Path -Parent $sftpRoot)
$sftpJava = Join-Path $sftpWorkspace 'sdk/tools/jdk1.8.0_503/bin/java.exe'
$sftpOriginal = Join-Path $sftpRoot 'tools/jsch-2.28.8.jar'
$sftpTool = Join-Path $PSScriptRoot 'retrolambda-2.5.7.jar'
if ((Get-FileHash -LiteralPath $sftpOriginal -Algorithm SHA256).Hash -ne '2afca1357d816028f2ac33f22407d2488a68406682973949cebb1b8ea6688a8b') { throw 'JSch original checksum mismatch' }
if ((Get-FileHash -LiteralPath $sftpTool -Algorithm SHA256).Hash -ne '590ad5ddefd57f47245f5470d24431b5bf2384086f8966c17545289efbe6b12e') { throw 'Retrolambda checksum mismatch' }
$sftpBuild = [IO.Path]::GetFullPath((Join-Path $sftpRoot 'build'))
$sftpInput = Join-Path $sftpBuild 'jsch-input'
$sftpOutput = Join-Path $sftpBuild 'jsch-output'
foreach ($sftpDirectory in @($sftpInput, $sftpOutput)) {
	if (-not ([IO.Path]::GetFullPath($sftpDirectory)).StartsWith($sftpBuild + '\', [StringComparison]::OrdinalIgnoreCase)) { throw 'Invalid generated directory' }
	if (Test-Path -LiteralPath $sftpDirectory) { Remove-Item -LiteralPath $sftpDirectory -Recurse -Force }
	New-Item -ItemType Directory -Path $sftpDirectory -Force | Out-Null
}
$sftpZip = [IO.Compression.ZipFile]::OpenRead($sftpOriginal)
try {
	foreach ($sftpEntry in $sftpZip.Entries) {
		if ($sftpEntry.FullName.StartsWith('META-INF/') -or -not $sftpEntry.FullName.EndsWith('.class')) { continue }
		$sftpTarget = [IO.Path]::GetFullPath((Join-Path $sftpInput $sftpEntry.FullName))
		if (-not $sftpTarget.StartsWith($sftpInput + '\', [StringComparison]::OrdinalIgnoreCase)) { throw 'Invalid ZIP entry' }
		New-Item -ItemType Directory -Path (Split-Path -Parent $sftpTarget) -Force | Out-Null
		[IO.Compression.ZipFileExtensions]::ExtractToFile($sftpEntry, $sftpTarget, $true)
	}
} finally { $sftpZip.Dispose() }
& $sftpJava "-Dretrolambda.inputDir=$sftpInput" "-Dretrolambda.outputDir=$sftpOutput" "-Dretrolambda.classpath=$sftpInput" '-Dretrolambda.bytecodeVersion=51' '-Dretrolambda.defaultMethods=true' -jar $sftpTool
if ($LASTEXITCODE -ne 0) { throw 'Bytecode conversion failed' }
$sftpPrepared = Join-Path $sftpBuild 'jsch-2.28.8-android.jar'
if (Test-Path -LiteralPath $sftpPrepared) { Remove-Item -LiteralPath $sftpPrepared }
$sftpArchive = [IO.Compression.ZipFile]::Open($sftpPrepared, [IO.Compression.ZipArchiveMode]::Create)
$sftpZip = [IO.Compression.ZipFile]::OpenRead($sftpOriginal)
try {
	foreach ($sftpEntry in $sftpZip.Entries) {
		$sftpName = $sftpEntry.FullName
		if ($sftpName.EndsWith('/') -or $sftpName.EndsWith('.class') -or $sftpName -eq 'META-INF/MANIFEST.MF' -or $sftpName.StartsWith('META-INF/versions/')) { continue }
		$sftpCopy = $sftpArchive.CreateEntry($sftpName)
		$sftpCopy.LastWriteTime = [DateTimeOffset]::new(1980, 1, 1, 0, 0, 0, [TimeSpan]::Zero)
		$sftpRead = $sftpEntry.Open(); $sftpWrite = $sftpCopy.Open()
		try { $sftpRead.CopyTo($sftpWrite) } finally { $sftpRead.Dispose(); $sftpWrite.Dispose() }
	}
	$sftpManifest = $sftpArchive.CreateEntry('META-INF/MANIFEST.MF')
	$sftpManifest.LastWriteTime = [DateTimeOffset]::new(1980, 1, 1, 0, 0, 0, [TimeSpan]::Zero)
	$sftpWriter = [IO.StreamWriter]::new($sftpManifest.Open(), [Text.UTF8Encoding]::new($false))
	try { $sftpWriter.Write("Manifest-Version: 1.0`r`nImplementation-Version: 2.28.8`r`nES4A-Compatibility: Retrolambda-2.5.7-Java7`r`n`r`n") } finally { $sftpWriter.Dispose() }
	foreach ($sftpFile in (Get-ChildItem -LiteralPath $sftpOutput -File -Recurse | Sort-Object FullName)) {
		$sftpName = $sftpFile.FullName.Substring($sftpOutput.Length + 1).Replace('\', '/')
		$sftpCopy = $sftpArchive.CreateEntry($sftpName)
		$sftpCopy.LastWriteTime = [DateTimeOffset]::new(1980, 1, 1, 0, 0, 0, [TimeSpan]::Zero)
		$sftpRead = [IO.File]::OpenRead($sftpFile.FullName); $sftpWrite = $sftpCopy.Open()
		try { $sftpRead.CopyTo($sftpWrite) } finally { $sftpRead.Dispose(); $sftpWrite.Dispose() }
	}
} finally { $sftpZip.Dispose(); $sftpArchive.Dispose() }
Copy-Item -LiteralPath $sftpPrepared -Destination (Join-Path $sftpRoot 'libs/jsch-2.28.8-android.jar') -Force
Write-Output 'Prepared JSch Android bytecode; SFTP runtime requires Android 8.0+.'
