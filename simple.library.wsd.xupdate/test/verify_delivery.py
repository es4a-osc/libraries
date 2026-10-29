"""
验证 XUpdate 单目录交付、两套内部包和示例入口隔离。
隔离副本不携带源目录 build 或其它扩展库；编译测试和示例后检查实际 APK。
xhwsd@qq.com 2026-10-29
"""

from pathlib import Path
import json
import os
import shutil
import subprocess
import zipfile


ROOT = Path(__file__).resolve().parents[3]
SDK = ROOT / "sdk"
LIBRARY = ROOT / "libraries/simple.library.wsd.xupdate"
OUTPUT = LIBRARY / "test/build/verification/merged"


def run(command, name, environment=None):
	"""保留实际工具诊断，失败后不继续验证其它产物。"""
	result = subprocess.run(list(map(str, command)), env=environment,
		capture_output=True, text=True, encoding="utf8", errors="replace")
	log = result.stdout + result.stderr
	(OUTPUT / (name + ".log")).write_text(log, encoding="utf8")
	print(name, "exit=" + str(result.returncode), flush=True)
	if result.returncode:
		print(log)
		raise SystemExit(result.returncode)


def snapshot():
	"""仅复制单库交付输入；本地依赖必须完整且均在库目录中。"""
	home = OUTPUT / "installed"
	if home.exists():
		home.resolve().relative_to(OUTPUT.resolve())
		shutil.rmtree(home)
	target = home / LIBRARY.name
	target.mkdir(parents=True)
	for name in ("classes.jar", "library.json", "dependencies.json", "dependencies.module",
			"AndroidManifest.generated.xml", "R.txt", "res", "assets", "files"):
		source = LIBRARY / name
		if source.is_dir():
			shutil.copytree(source, target / name)
		elif source.is_file():
			shutil.copy2(source, target / name)
	for entry in json.loads((LIBRARY / "dependencies.json").read_text("utf8"))["files"]:
		source = (LIBRARY / entry["file"]).resolve(strict=True)
		source.relative_to(LIBRARY.resolve())
		destination = target / entry["file"]
		destination.parent.mkdir(parents=True, exist_ok=True)
		shutil.copy2(source, destination)
	return home


def verify_delivery():
	"""确认一份 JAR 提供所有封装，第三方二进制仍由依赖描述管理。"""
	with zipfile.ZipFile(LIBRARY / "classes.jar") as archive:
		for name in ("xupdate/更新器", "xupdate/UpdateController",
				"xupdateapi/快捷更新器", "xupdateapi/Aria下载器"):
			assert "simple/library/wsd/" + name + ".class" in archive.namelist(), name
		assert not any(name.startswith("com/arialyy/") for name in archive.namelist())
	manifest = json.loads((LIBRARY / "library.json").read_text("utf8"))
	types = [item["type"] for category in manifest["categories"] for item in category["definitions"]]
	assert len(types) == len(set(types)), "清单存在重复导出类型"
	for name in ("xupdate.更新器", "xupdateapi.快捷更新器", "xupdateapi.Aria下载器"):
		assert "simple.library.wsd." + name in types
	base = LIBRARY / "example/src/simple/library/wsd/xupdate/example"
	for name in ("主窗口", "xupdate/更新演示", "xupdate/提示演示", "xupdate/流程定制", "xupdate/下载安装"):
		assert "simple.library.wsd.xupdateapi" not in (base / (name + ".simple")).read_text("utf8")
	for name in ("快捷更新", "断点续传"):
		text = (base / "xupdateapi" / (name + ".simple")).read_text("utf8")
		assert "simple.library.wsd.xupdateapi.快捷更新器" in text
		assert "$为 simple.library.wsd.xupdate.更新器" not in text


def main():
	"""严格编译 Java，再以唯一类库目录构建真实 Simple 测试和示例。"""
	OUTPUT.mkdir(parents=True, exist_ok=True)
	verify_delivery()
	model = json.loads((LIBRARY / "build/ide/classpath.json").read_text("utf8"))
	classpath = ";".join(item["path"] for item in model["libraries"])
	classes = OUTPUT / "strict"
	classes.mkdir(exist_ok=True)
	arguments = ["-encoding", "UTF-8", "-source", "11", "-target", "11",
		"-Xlint:all,-options", "-Werror", "-classpath", classpath, "-d", str(classes)]
	arguments += [str(path) for path in sorted((LIBRARY / "src").rglob("*.java"))]
	argument_file = classes / "javac.args"
	argument_file.write_text("\n".join('"' + value.replace("\\", "/") + '"'
		for value in arguments), encoding="utf8")
	run([SDK / "tools/jdk-11.0.32.1/bin/javac.exe", "@" + str(argument_file)], "strict")
	environment = os.environ.copy()
	environment.update(JAVA_HOME=str(SDK / "tools/jdk-11.0.32.1"),
		ANDROID_HOME=str(SDK / "tools/android-sdk"), SIMPLE_HOME=str(SDK / "simple"),
		ES4A_SDK_HOME=str(SDK), LIBRARIES_HOME=str(snapshot()))
	classpath = ";".join(map(str, (SDK / "tools/android-sdk/platforms/android-30/tools/lib/d8.jar",
		SDK / "simple/SimpleCompiler.jar", SDK / "simple/libs/*")))
	for kind in ("test", "example"):
		run([SDK / "tools/jdk-11.0.32.1/bin/java.exe", "-Dfile.encoding=UTF-8",
			"-Dsun.stdout.encoding=UTF-8", "-Dsun.stderr.encoding=UTF-8", "-classpath", classpath,
			"simple.compiler.Main", LIBRARY / kind / "project.properties"], kind, environment)
		with zipfile.ZipFile(LIBRARY / kind / "build/apks/signed.apk") as archive:
			data = b"".join(archive.read(name) for name in archive.namelist() if name.endswith(".dex"))
			for marker in (b"Lcom/arialyy/aria/", b"Lcom/xuexiang/xupdate/easy/",
					b"Lsimple/library/wsd/xupdate/", b"Lsimple/library/wsd/xupdateapi/"):
				assert marker in data, "APK 缺失依赖：" + str(marker)
	print("XUPDATE_MERGED_DELIVERY_PASS", flush=True)


if __name__ == "__main__":
	main()
