"""为现有 test 工程构建设备回归辅助包；先编译正式测试 APK，以复用其调试证书。"""

from pathlib import Path
import json
import os
import subprocess
import tempfile
import zipfile


LIB = Path(__file__).resolve().parent.parent
SDK = LIB.parents[1] / "sdk"
JDK = SDK / "tools/jdk-11.0.32.1/bin"
ANDROID = SDK / "tools/android-sdk"
BUILD_TOOLS = ANDROID / "build-tools/36.1.0"
API = ANDROID / "platforms/android-30/android.jar"
OUTPUT = LIB / "test/build/verification"


def run(arguments):
	result = subprocess.run([str(value) for value in arguments], capture_output=True, text=True, encoding="utf8", errors="replace")
	if result.stdout:
		print(result.stdout[-4000:])
	if result.stderr:
		print(result.stderr[-4000:])
	if result.returncode:
		raise SystemExit(result.returncode)


def main():
	OUTPUT.mkdir(parents=True, exist_ok=True)
	classes = OUTPUT / "classes"
	classes.mkdir(exist_ok=True)
	dependencies = json.loads((LIB / "build/ide/classpath.json").read_text(encoding="utf8"))["libraries"]
	classpath = [str(LIB / "classes.jar")] + [item["path"] for item in dependencies]
	sources = sorted((LIB / "test/java").rglob("*.java"))
	run([JDK / "javac.exe", "-J-Dfile.encoding=UTF-8", "-encoding", "UTF-8", "-source", "11", "-target", "11",
		 "-Xlint:all,-options", "-Werror", "-classpath", ";".join(classpath), "-d", classes, *sources])
	jar = OUTPUT / "probe.jar"
	run([JDK / "jar.exe", "cf", jar, "-C", classes, "."])
	dex = OUTPUT / "dexs"
	dex.mkdir(exist_ok=True)
	arguments = [JDK / "java.exe", "-cp", BUILD_TOOLS / "lib/d8.jar", "com.android.tools.r8.D8", "--min-api", "24", "--lib", API]
	for dependency in classpath:
		if not dependency.replace("\\", "/").endswith(("/android.jar", "/core-lambda-stubs.jar")):
			arguments += ["--classpath", dependency]
	run(arguments + ["--output", dex, jar])
	manifest = OUTPUT / "AndroidManifest.xml"
	manifest.write_text('''<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="simple.library.wsd.xhttp2.regression">
<uses-sdk android:minSdkVersion="24" android:targetSdkVersion="30"/>
<application android:label="XHttp2 回归辅助">
<provider android:name="simple.library.wsd.xhttp2.UnknownLengthProvider" android:authorities="simple.library.wsd.xhttp2.test.source" android:exported="true"/>
</application>
<instrumentation android:name="simple.library.wsd.xhttp2.HttpRegression" android:targetPackage="simple.library.wsd.xhttp2.test"/>
</manifest>''', encoding="utf8")
	unsigned = OUTPUT / "unsigned.apk"
	run([BUILD_TOOLS / "aapt2.exe", "link", "-I", API, "--manifest", manifest, "-o", unsigned])
	with zipfile.ZipFile(unsigned, "a") as archive:
		for path in sorted(dex.glob("classes*.dex")):
			archive.write(path, path.name)
	aligned = OUTPUT / "aligned.apk"
	signed = OUTPUT / "signed.apk"
	run([BUILD_TOOLS / "zipalign.exe", "-f", "4", unsigned, aligned])
	# 本机签名工具复用既有辅助 APK 路径时曾生成截断文件，使用全新输出并在成功后原子替换。
	with tempfile.TemporaryDirectory(prefix="probe-sign-", dir=OUTPUT) as directory:
		fresh = Path(directory) / "signed.apk"
		run([JDK / "java.exe", "-jar", BUILD_TOOLS / "lib/apksigner.jar", "sign", "--ks", LIB / "test/build/android_debug.p12",
			 "--ks-type", "PKCS12", "--ks-key-alias", "AndroidDebugKey", "--ks-pass", "pass:android", "--out", fresh, aligned])
		with zipfile.ZipFile(fresh) as archive:
			if archive.testzip() is not None:
				raise RuntimeError("辅助 APK 归档校验失败")
		os.replace(fresh, signed)
		sidecar = Path(str(fresh) + ".idsig")
		if sidecar.exists():
			os.replace(sidecar, Path(str(signed) + ".idsig"))
	print("PROBE_APK", signed, signed.stat().st_size)


if __name__ == "__main__":
	main()
