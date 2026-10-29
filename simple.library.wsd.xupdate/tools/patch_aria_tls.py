"""
将已核对的 ConnectionHelp 编译回本库 Aria HTTP 包，保留系统 HTTPS 证书及域名校验。
先用 library.ps1 prepare 准备类路径；不改变依赖版本，不修改其它模块或系统 TLS 配置。
xhwsd@qq.com 2026-10-29
"""

from pathlib import Path
import io
import json
import subprocess
import zipfile


def main():
	"""替换已核对的连接及初始失败策略，校验输出 ZIP 可读取。"""
	library = Path(__file__).resolve().parent.parent
	sdk = library.parents[1] / "sdk"
	model = json.loads((library / "build/ide/classpath.json").read_text("utf8"))
	classpath = ";".join(item["path"] for item in model["libraries"])
	output = library / "build/dependency-patches/aria-tls"
	output.mkdir(parents=True, exist_ok=True)
	command = [str(sdk / "tools/jdk-11.0.32.1/bin/javac.exe"), "-encoding", "UTF-8",
		"-source", "8", "-target", "8", "-Xlint:all,-options", "-Werror", "-classpath", classpath, "-d", str(output),
		str(library / "tools/ConnectionHelp.java"), str(library / "tools/HttpDFileInfoTask.java")]
	subprocess.run(command, check=True)
	entry = "com/arialyy/aria/http/ConnectionHelp.class"
	patches = {str(path.relative_to(output)).replace("\\", "/"): path.read_bytes()
		for path in output.rglob("*.class")}
	artifact = library / "aars/httpComponent-3.8.15.aar"
	with zipfile.ZipFile(artifact) as archive:
		entries = [(item, archive.read(item.filename)) for item in archive.infolist()]
	classes = next(data for item, data in entries if item.filename == "classes.jar")
	result = io.BytesIO()
	with zipfile.ZipFile(io.BytesIO(classes)) as source, zipfile.ZipFile(result, "w", zipfile.ZIP_DEFLATED) as target:
		assert entry in source.namelist()
		for item in source.infolist():
			target.writestr(item, patches.get(item.filename, source.read(item.filename)))
	temporary = artifact.with_suffix(".aar.tmp")
	with zipfile.ZipFile(temporary, "w", zipfile.ZIP_DEFLATED) as target:
		for item, data in entries:
			target.writestr(item, result.getvalue() if item.filename == "classes.jar" else data)
	with zipfile.ZipFile(temporary) as archive:
		assert archive.testzip() is None
	temporary.replace(artifact)
	print("Aria HTTPS system validation restored:", artifact)


if __name__ == "__main__":
	main()
