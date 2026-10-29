"""从不含 build/ 或源码的类库副本构建临时用例，验证便携交付和权限按需收集。"""

from pathlib import Path
import hashlib
import json
import os
import shutil
import subprocess
import xml.etree.ElementTree as ET


LIB = Path(__file__).resolve().parent.parent
SDK = LIB.parents[1] / "sdk"
OUTPUT = LIB / "test/build/verification/delivery"
ANDROID = "{http://schemas.android.com/apk/res/android}"


def save(path, text):
	path.parent.mkdir(parents=True, exist_ok=True)
	path.write_bytes(text.replace("\r\n", "\n").replace("\n", "\r\n").encode("utf8"))


def main():
	portable = OUTPUT / "libraries" / LIB.name
	portable.mkdir(parents=True, exist_ok=True)
	files = []
	for name in ("library.json", "classes.jar", "dependencies.json", "dependencies.module", "AndroidManifest.generated.xml", "R.txt"):
		source = LIB / name
		if source.exists():
			shutil.copy2(source, portable / name)
			files.append(name)
	for name in ("aars", "libs", "res", "assets", "icons", "jni"):
		source = LIB / name
		if source.is_dir():
			shutil.copytree(source, portable / name, dirs_exist_ok=True)
	if (portable / "build").exists() or (portable / "src").exists():
		raise RuntimeError("便携验证副本不应含生成目录或源码")
	cases = {
		"dto": ("别名 请求配置 = simple.library.wsd.xhttp2.XHttp请求\n", "\t变量 请求 为 请求配置\n\t请求 = 创建 请求配置\n\t请求.地址 = \"echo\"\n", "", []),
		"tools": ("别名 网络工具 = simple.library.wsd.xhttp2.XHttp工具\n", "\t变量 文本 为 文本型\n\t文本 = 网络工具.网址编码(\"中文 +\")\n", "", []),
		"client": ("", "", "\t\t$定义 网络 $为 simple.library.wsd.xhttp2.XHttp客户端\n\t\t$结束 $定义\n", ["android.permission.INTERNET"]),
	}
	environment = os.environ.copy()
	environment.update(JAVA_HOME=str(SDK / "tools/jdk-11.0.32.1"), ANDROID_HOME=str(SDK / "tools/android-sdk"),
					   SIMPLE_HOME=str(SDK / "simple"), LIBRARIES_HOME=str(portable.parent), ES4A_SDK_HOME=str(SDK))
	classpath = ";".join(str(value) for value in [SDK / "tools/android-sdk/platforms/android-30/tools/lib/d8.jar",
											   SDK / "simple/SimpleCompiler.jar", SDK / "simple/libs/*"])
	reports = []
	for name, (aliases, code, components, expected) in cases.items():
		project = OUTPUT / name
		(project / "assets").mkdir(parents=True, exist_ok=True)
		(project / "res").mkdir(parents=True, exist_ok=True)
		source = project / f"src/qa/xhttp2/{name}/主窗口.simple"
		save(source, aliases + "\n事件 主窗口.初始化()\n" + code + "结束 事件\n\n$属性\n\t$资源 $窗口\n\t$定义 主窗口 $为 窗口\n"
			 + components + "\t$结束 $定义\n$结束 $属性\n")
		properties = project / "project.properties"
		save(properties, f"main=qa.xhttp2.{name}.主窗口\nname=XHttp2交付验证\nversion.code=1\nversion.name=1.0\nsource=./src\nassets=./assets\nres=./res\nbuild=./build\n")
		result = subprocess.run([SDK / "tools/jdk-11.0.32.1/bin/java.exe", "-Dfile.encoding=UTF-8", "-cp", classpath,
								 "simple.compiler.Main", properties], env=environment, capture_output=True,
								text=True, encoding="utf8", errors="replace")
		save(project / "compile.log", result.stdout + result.stderr)
		if result.returncode:
			raise RuntimeError(f"{name} 构建失败\n" + result.stdout[-3000:] + result.stderr[-3000:])
		manifest = project / "build/manifests/AndroidManifest.xml"
		permissions = sorted(element.attrib[ANDROID + "name"] for element in ET.parse(manifest).getroot().findall("uses-permission"))
		if permissions != expected:
			raise RuntimeError(f"{name} 权限不符：{permissions}，期望 {expected}")
		apk = project / "build/apks/signed.apk"
		reports.append({"case": name, "permissions": permissions, "apk": str(apk), "sha256": hashlib.sha256(apk.read_bytes()).hexdigest()})
	report = {"portableLibrary": str(portable), "withoutBuildAndSources": True, "copiedDescriptors": files, "cases": reports}
	save(OUTPUT / "report.json", json.dumps(report, ensure_ascii=False, indent=2) + "\n")
	print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
	main()
