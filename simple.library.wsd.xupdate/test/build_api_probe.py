"""
构建唯一 test 工程的原生回归辅助 APK；先编译扩展库与 test，沿用测试签名。
输出只进入 test/build/verification/api，不安装到 SDK。
xhwsd@qq.com 2026-10-29
"""

from pathlib import Path
import json
import subprocess
import zipfile
import tempfile
import shutil


def run(arguments):
	"""保留实际工具诊断，任何非零退出码都中止后续打包。"""
	result = subprocess.run(list(map(str, arguments)), capture_output=True,
		text=True, encoding="utf8", errors="replace")
	if result.stdout:
		print(result.stdout)
	if result.stderr:
		print(result.stderr)
	if result.returncode:
		raise SystemExit(result.returncode)


def main():
	"""使用 SDK 的 JDK 11、API 30 和既有测试证书构建辅助 APK。"""
	root = Path(__file__).resolve().parents[3]
	library = root / "libraries/simple.library.wsd.xupdate"
	output = library / "test/build/verification/api"
	output.mkdir(parents=True, exist_ok=True)
	sdk = root / "sdk"
	jdk = sdk / "tools/jdk-11.0.32.1/bin"
	android = sdk / "tools/android-sdk"
	tools = android / "build-tools/36.1.0"
	platform = android / "platforms/android-30/android.jar"
	model = json.loads((library / "build/ide/classpath.json").read_text("utf8"))
	classpath = [str(library / "classes.jar")] + [item["path"] for item in model["libraries"]]

	# 原生测试只引用正式库，不把正式库、运行库再次打入辅助 APK。
	classes = output / "classes"
	classes.mkdir(exist_ok=True)
	sources = sorted((library / "test/java").rglob("*.java"))
	run([jdk / "javac.exe", "-J-Dfile.encoding=UTF-8", "-encoding", "UTF-8",
		"-source", "11", "-target", "11", "-Xlint:all,-options", "-Werror",
		"-classpath", ";".join(classpath), "-d", classes, *sources])
	jar = output / "probe.jar"
	run([jdk / "jar.exe", "cf", jar, "-C", classes, "."])
	dexs = output / "dexs"
	dexs.mkdir(exist_ok=True)
	run([jdk / "java.exe", "-cp", tools / "lib/d8.jar", "com.android.tools.r8.D8",
		"--min-api", "24", "--lib", platform, "--classpath", sdk / "simple/SimpleAndroidRuntime.jar",
		"--classpath", library / "classes.jar", "--output", dexs, jar])

	manifest = """<manifest xmlns:android="http://schemas.android.com/apk/res/android"
	package="simple.library.wsd.xupdateapi.regression">
	<uses-sdk android:minSdkVersion="24" android:targetSdkVersion="30" />
	<application android:label="XUpdate API 回归辅助" />
	<instrumentation android:name="simple.library.wsd.xupdate.XUpdateApiInstrumentation"
		android:targetPackage="simple.library.wsd.xupdate.test" />
</manifest>
"""
	(output / "AndroidManifest.xml").write_bytes(manifest.replace("\n", "\r\n").encode("utf8"))
	unsigned = output / "unsigned.apk"
	run([tools / "aapt2.exe", "link", "-I", platform, "--manifest", output / "AndroidManifest.xml", "-o", unsigned])
	with zipfile.ZipFile(unsigned, "a") as archive:
		archive.write(dexs / "classes.dex", "classes.dex")
	run([tools / "zipalign.exe", "-f", "4", unsigned, output / "aligned.apk"])
	with tempfile.TemporaryDirectory(prefix="es4a-xupdateapi-probe-") as temporary:
		signed = Path(temporary) / "signed.apk"
		run([jdk / "java.exe", "-jar", tools / "lib/apksigner.jar", "sign", "--ks",
			library / "test/build/android_debug.p12", "--ks-type", "PKCS12", "--ks-key-alias",
			"AndroidDebugKey", "--ks-pass", "pass:android", "--out", signed, output / "aligned.apk"])
		with zipfile.ZipFile(signed) as archive:
			if archive.testzip() is not None:
				raise RuntimeError("测试辅助 APK 的 ZIP 校验失败。")
		shutil.copyfile(signed, output / "signed.apk")
	print("PROBE_APK", output / "signed.apk", (output / "signed.apk").stat().st_size)


if __name__ == "__main__":
	main()
