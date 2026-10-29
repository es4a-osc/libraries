"""在隔离类库目录编译唯一测试工程及临时权限用例，不安装或发布到 SDK。"""

import argparse
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import xml.etree.ElementTree as ET
import zipfile


LIBRARY = Path(__file__).resolve().parents[1]
WORKSPACE = LIBRARY.parents[1]
SDK = WORKSPACE / "sdk"
TEST = LIBRARY / "test"
BUILD = TEST / "build"
sys.stdout.reconfigure(encoding="utf-8", errors="replace")
sys.stderr.reconfigure(encoding="utf-8", errors="replace")
ANDROID = "{http://schemas.android.com/apk/res/android}"
JAVA_TYPES = {
	"String": "文本型", "int": "整数型", "long": "长整数型", "boolean": "逻辑型",
	"float": "单精度小数型", "double": "双精度小数型", "String[]": "文本型()",
	"int[]": "整数型()", "byte[]": "字节型()", "float[]": "单精度小数型()",
}


def write(path, text):
	"""测试输入统一为 UTF-8、CRLF，不修改类库原始文件。"""
	path.parent.mkdir(parents=True, exist_ok=True)
	path.write_bytes((text.replace("\r\n", "\n").rstrip() + "\n").replace("\n", "\r\n").encode("utf-8"))


def execute(args, log, env=None):
	"""保存完整构建输出，失败时显示末尾诊断并返回非零状态。"""
	log.parent.mkdir(parents=True, exist_ok=True)
	with log.open("w", encoding="utf-8") as stream:
		result = subprocess.run([str(value) for value in args], cwd=WORKSPACE,
			stdout=stream, stderr=subprocess.STDOUT, env=env, check=False)
	if result.returncode:
		print(log.read_text(encoding="utf-8", errors="replace")[-10000:])
		raise RuntimeError(f"命令失败，退出码 {result.returncode}；日志：{log}")


def validate_metadata():
	"""核对每个 Java 导出函数与清单的类型、静态标记、顺序及参数命名。"""
	manifest = json.loads((LIBRARY / "library.json").read_text(encoding="utf-8"))
	definitions = [item for group in manifest["categories"] for item in group["definitions"]]
	count = 0
	for definition in definitions:
		path = LIBRARY / "src" / Path(*definition["type"].split(".")).with_suffix(".java")
		source = path.read_text(encoding="utf-8")
		declarations = re.findall(r"@SimpleFunction\s+public static ([\w\[\]]+) (\w+)\(([^)]*)\)", source)
		functions = definition["functions"]
		assert len(declarations) == len(functions), definition["name"]
		for (result, name, parameters), function in zip(declarations, functions):
			assert function["name"] == name and function["static"] is True, name
			assert function.get("return") == JAVA_TYPES.get(result), name
			arguments = [value.strip().split() for value in parameters.split(",") if value.strip()]
			assert len(arguments) == len(function["params"]), name
			for (kind, parameter), exported in zip(arguments, function["params"]):
				assert re.fullmatch(r"[a-z][A-Za-z0-9]*", parameter), parameter
				assert exported["type"] == JAVA_TYPES[kind], name
				assert re.search(r"[\u4e00-\u9fff]", exported["name"]), name
			count += 1
	print(f"清单核对：{len(definitions)} 个函数集，{count} 个静态函数")


def prepare_fixture():
	"""只复制可交付文件，证明不依赖源码目录或开发 build 产物。"""
	home = BUILD / "libraries"
	installed = home / LIBRARY.name
	installed.mkdir(parents=True, exist_ok=True)
	for name in ("classes.jar", "dependencies.json", "dependencies.module", "AndroidManifest.generated.xml", "R.txt", "library.json"):
		shutil.copy2(LIBRARY / name, installed / name)
	for name in ("aars", "libs", "files", "res", "assets", "jni"):
		if (LIBRARY / name).is_dir():
			shutil.copytree(LIBRARY / name, installed / name, dirs_exist_ok=True)
	assert not (installed / "build").exists() and not (installed / "src").exists()
	model = json.loads((LIBRARY / "build/ide/classpath.json").read_text(encoding="utf-8"))
	classpath = os.pathsep.join([str(LIBRARY / "classes.jar")] + [item["path"] for item in model["libraries"]])
	java_home = SDK / "tools/jdk-11.0.32.1/bin"
	classes = BUILD / "checks/classes"
	classes.mkdir(parents=True, exist_ok=True)
	source = TEST / "java/XUtilChecks.java"
	execute([java_home / "javac.exe", "-J-Dfile.encoding=UTF-8", "-J-Dsun.stderr.encoding=UTF-8",
		"-encoding", "UTF-8", "-source", "11", "-target", "11",
		"-Xlint:all", "-Werror", "-classpath", classpath, "-d", classes, source], BUILD / "checks/javac.log")
	helper = home / "xutil-checks"
	helper.mkdir(parents=True, exist_ok=True)
	previous = helper / "aars/permissions.aar"
	if previous.exists():
		previous.unlink()
	execute([java_home / "jar.exe", "cf", helper / "classes.jar", "-C", classes, "."], BUILD / "checks/jar.log")
	# 独立来源的 AAR 声明相同权限，用来验证限定来源的移除规则不会误删其它依赖。
	with zipfile.ZipFile(helper / "permissions.aar", "w") as archive:
		archive.writestr("AndroidManifest.xml", '<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="fixture.independent.permissions">'
			'<uses-sdk android:minSdkVersion="24" android:targetSdkVersion="30" />'
			'<uses-permission android:name="android.permission.INTERNET" />'
			'<uses-permission android:name="android.permission.CAMERA" />'
			'<uses-feature android:name="android.hardware.camera" android:required="false" />'
			'</manifest>')
	return home


def compiler_environment(home):
	"""复用实际 SDK 编译器，仅将类库扫描位置指向测试隔离目录。"""
	env = os.environ.copy()
	env.update(JAVA_HOME=str(SDK / "tools/jdk-11.0.32.1"), ANDROID_HOME=str(SDK / "tools/android-sdk"),
		SIMPLE_HOME=str(SDK / "simple"), LIBRARIES_HOME=str(home), ES4A_SDK_HOME=str(SDK))
	return env


def compile_project(project, log, env):
	"""调用正式编译器，保持 JDK、Android API 和配套依赖与 SDK 一致。"""
	classpath = os.pathsep.join(str(path) for path in [SDK / "tools/android-sdk/platforms/android-30/tools/lib/d8.jar",
		SDK / "simple/SimpleCompiler.jar", SDK / "simple/libs/*"])
	execute([SDK / "tools/jdk-11.0.32.1/bin/java.exe", "-Dfile.encoding=UTF-8", "-Dsun.stdout.encoding=UTF-8",
		"-Dsun.stderr.encoding=UTF-8", "-classpath", classpath, "simple.compiler.Main", project], log, env)


def permission_case(home, name, code, expected, independent=False):
	"""生成临时单元并检查正式合并清单中的权限及宿主节点。"""
	root = BUILD / "permission-cases" / name
	package = "simple.library.wsd.xutil.permission." + name
	source = root / "src" / Path(*package.split(".")) / "验证.simple"
	write(source, code + '\n$属性\n\t$资源 $窗口\n\t$定义 验证 $为 窗口\n\t$结束 $定义\n$结束 $属性\n')
	write(root / "project.properties", f"main={package}.验证\nname=XUtil权限检查\nsource=./src\nres=./res\nassets=./assets\nbuild=./build\n")
	# 同一个测试辅助库的本地 AAR 只在此用例加入；其它用例保持干净。
	helper = home / "xutil-checks"
	index = helper / "aars/permissions.aar"
	if independent:
		index.parent.mkdir(parents=True, exist_ok=True)
		shutil.copy2(helper / "permissions.aar", index)
	elif index.exists():
		index.unlink()
	compile_project(root / "project.properties", root / "compile.log", compiler_environment(home))
	manifest = ET.parse(root / "build/manifests/AndroidManifest.xml").getroot()
	actual = {node.get(ANDROID + "name") for node in manifest.findall("uses-permission")}
	assert actual == expected, (name, actual, expected)
	assert not manifest.findall("application/provider"), name
	assert not any("PermissionActivity" in node.get(ANDROID + "name", "") for node in manifest.findall("application/activity")), name
	features = manifest.findall("uses-feature")
	if independent:
		assert any(node.get(ANDROID + "name") == "android.hardware.camera" for node in features), name
	else:
		assert not any("camera" in node.get(ANDROID + "name", "") for node in features), name
	print(f"权限用例 {name}：{sorted(actual)}")
	return sorted(actual)


def main():
	parser = argparse.ArgumentParser(description=__doc__)
	parser.add_argument("--permissions", action="store_true", help="额外构建临时权限组合用例")
	args = parser.parse_args()
	validate_metadata()
	home = prepare_fixture()
	if args.permissions:
		text = '事件 验证.初始化()\n\t断言真(simple.library.wsd.xutil.XUtil文本.反转("ab") = "ba")\n结束 事件\n'
		cases = {}
		cases["unused"] = permission_case(home, "unused", text, set())
		cases["dial"] = permission_case(home, "dial", '事件 验证.初始化()\n\tsimple.library.wsd.xutil.XUtil电话.打开拨号("5551234")\n结束 事件', set())
		cases["network"] = permission_case(home, "network", '事件 验证.初始化()\n\tsimple.library.wsd.xutil.XUtil网络.是否已连接()\n结束 事件', {"android.permission.ACCESS_NETWORK_STATE"})
		cases["domain"] = permission_case(home, "domain", '过程 后台解析()\n\tsimple.library.wsd.xutil.XUtil域名.解析("localhost")\n结束 过程', {"android.permission.INTERNET"})
		cases["call"] = permission_case(home, "call", '事件 验证.初始化()\n\tsimple.library.wsd.xutil.XUtil通话.是否已授权()\n结束 事件', {"android.permission.CALL_PHONE"})
		cases["independent"] = permission_case(home, "independent", text + '\n过程 额外来源()\n\tsimple.library.wsd.xutil.test.XUtilChecks.isComplete()\n结束 过程', {"android.permission.INTERNET", "android.permission.CAMERA"}, True)
		# 清除临时独立 AAR 的声明，防止最终交互 APK 带上用于对照的 CAMERA 权限。
		(home / "xutil-checks/aars/permissions.aar").unlink()
		write(BUILD / "permission-results.json", json.dumps(cases, ensure_ascii=False, indent=2))
	compile_project(TEST / "project.properties", BUILD / "compile.log", compiler_environment(home))
	manifest = ET.parse(BUILD / "manifests/AndroidManifest.xml").getroot()
	permissions = {node.get(ANDROID + "name") for node in manifest.findall("uses-permission")}
	assert permissions == {"android.permission.ACCESS_NETWORK_STATE", "android.permission.INTERNET", "android.permission.CALL_PHONE"}, permissions
	print(f"测试 APK：{BUILD / 'apks/signed.apk'}")


if __name__ == "__main__":
	main()
