"""验证已安装示例的环形比例、居中、样式往返及显示模式，保存真实界面截图。"""

from pathlib import Path
import argparse
import hashlib
import json
import re
import subprocess
import time
import xml.etree.ElementTree as ET


LIB = Path(__file__).resolve().parent.parent
ADB = LIB.parents[1] / "sdk/tools/android-sdk/platform-tools/adb.exe"
OUTPUT = LIB / "test/build/verification/progress-aspect"
PACKAGE = "simple.library.wsd.xui.example"
REMOTE = "/data/local/tmp/es4a-xui-progress-aspect"


def main():
	parser = argparse.ArgumentParser(description=__doc__)
	parser.add_argument("--serial", required=True)
	serial = parser.parse_args().serial
	OUTPUT.mkdir(parents=True, exist_ok=True)
	results = []

	def adb(*arguments):
		return subprocess.run([str(ADB), "-s", serial, *map(str, arguments)], check=True, capture_output=True).stdout

	def shell(*arguments):
		return adb("shell", *arguments)

	def tree():
		shell("uiautomator", "dump", REMOTE + ".xml")
		return ET.fromstring(shell("cat", REMOTE + ".xml"))

	def bounds(node):
		return [int(value) for value in re.findall(r"\d+", node.attrib["bounds"])]

	def find(model, text):
		return next((node for node in model.iter("node") if text in (
			node.get("text"), node.get("content-desc"))), None)

	def scroll(model):
		node = next((item for item in model.iter("node") if item.get("scrollable") == "true"), None)
		area = bounds(node) if node is not None else bounds(next(model.iter("node")))
		x = area[2] - 12
		shell("input", "swipe", x, int(area[1] + (area[3] - area[1]) * 0.8),
			x, int(area[1] + (area[3] - area[1]) * 0.2), 240)
		time.sleep(0.2)

	def touch(text):
		for _ in range(9):
			model = tree()
			node = find(model, text)
			if node is not None:
				left, top, right, bottom = bounds(node)
				viewport = bounds(next(model.iter("node")))
				if bottom > viewport[3] - 100:
					# 首页入口露出一部分时先滚到安全点击区，不点击仍在视口边缘的条目。
					scroll(model)
					continue
				shell("input", "tap", (left + right) // 2, (top + bottom) // 2)
				time.sleep(0.25)
				return
			scroll(model)
		raise AssertionError("未找到可操作项目：" + text)

	def verify(name, circular, feedback=""):
		model = tree()
		node = find(model, "样式进度条")
		if node is None:
			raise AssertionError("未找到进度条")
		left, top, right, bottom = bounds(node)
		width, height = right - left, bottom - top
		viewport = bounds(next(model.iter("node")))
		if circular:
			assert width > 0 and abs(width - height) <= 1, (name, width, height)
			assert abs(left + right - viewport[0] - viewport[2]) <= 2, (name, "环形未水平居中")
		else:
			assert width > height * 4 and width >= (viewport[2] - viewport[0]) * 0.85, (name, width, height)
		if feedback:
			assert any(feedback in item.get("text", "") for item in model.iter("node")), (name, feedback)
		assert not any("材质" in item.get("text", "") for item in model.iter("node")), "界面残留生硬文案"
		shell("screencap", "-p", REMOTE + ".png")
		adb("pull", REMOTE + ".png", OUTPUT / (name + ".png"))
		(OUTPUT / (name + ".xml")).write_bytes(ET.tostring(model, encoding="utf-8"))
		results.append({"state": name, "bounds": [left, top, right, bottom], "width": width,
			"height": height, "circular": circular, "passed": True})

	try:
		shell("am", "force-stop", PACKAGE)
		shell("am", "start", "-W", "-n", PACKAGE + "/simple.runtime.android.MainActivity")
		touch("进度与加载")
		assert find(tree(), "样式进度条") is not None or find(tree(), "开始进度动画") is not None, "未进入进度示例"
		# 滚到底部后再测，避免把屏幕边缘裁剪的可访问边界误认为组件的真实高度。
		for _ in range(3):
			scroll(tree())
		verify("环形等待", True)
		touch("切换进度模式")
		verify("环形任务进度", True, "进度：65%")
		touch("切换为水平进度")
		verify("水平任务进度", False, "主进度和缓冲进度保持")
		touch("切换进度模式")
		verify("水平等待", False, "当前为等待模式")
		touch("切换为环形进度")
		verify("切回环形", True)
		touch("返回")
		touch("进度与加载")
		for _ in range(3):
			scroll(tree())
		verify("重新进入环形", True)
	finally:
		apk = LIB / "example/build/apks/signed.apk"
		report = {"serial": serial, "api": shell("getprop", "ro.build.version.sdk").decode().strip(),
			"apk": str(apk), "sha256": hashlib.sha256(apk.read_bytes()).hexdigest(), "states": results,
			"passed": len(results) == 6 and all(item["passed"] for item in results)}
		(OUTPUT / "report.json").write_bytes((json.dumps(report, ensure_ascii=False, indent=2)
			+ "\n").replace("\n", "\r\n").encode("utf8"))
		print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
	main()
