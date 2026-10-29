"""通过实际屏幕点击验证示例两步引导，覆盖普通步骤、图片区域及重复进入。"""

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
OUTPUT = LIB / "test/build/verification/guide-click/example"
PACKAGE = "simple.library.wsd.xui.example"
REMOTE = "/data/local/tmp/es4a-xui-guide-example"
FIRST = "第一步：点击高亮区域继续。"
SECOND = "第二步：使用这里返回目录。"


def main():
	parser = argparse.ArgumentParser(description=__doc__)
	parser.add_argument("--serial", required=True)
	serial = parser.parse_args().serial
	OUTPUT.mkdir(parents=True, exist_ok=True)
	cases = []

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
		return next((node for node in model.iter("node") if text in (node.get("text"), node.get("content-desc"))), None)

	def tap_at(x, y):
		shell("input", "tap", x, y)
		time.sleep(0.5)

	def touch(text):
		for _ in range(10):
			model = tree()
			node = find(model, text)
			viewport = bounds(next(model.iter("node")))
			if node is not None:
				left, top, right, bottom = bounds(node)
				if bottom <= viewport[3] - 100:
					point = ((left + right) // 2, (top + bottom) // 2)
					tap_at(*point)
					return point
			scroll = next((item for item in model.iter("node") if item.get("scrollable") == "true"), None)
			area = bounds(scroll) if scroll is not None else viewport
			x = area[2] - 12
			shell("input", "swipe", x, int(area[1] + (area[3] - area[1]) * 0.8), x,
				int(area[1] + (area[3] - area[1]) * 0.2), 240)
			time.sleep(0.3)
		raise AssertionError("未找到可操作项目：" + text)

	def check_text(expected):
		model = tree()
		assert find(model, expected) is not None, expected
		return model

	def capture(name, model):
		shell("screencap", "-p", REMOTE + ".png")
		adb("pull", REMOTE + ".png", OUTPUT / (name + ".png"))
		(OUTPUT / (name + ".xml")).write_bytes(ET.tostring(model, encoding="utf-8"))

	def standard(name):
		# 第一高亮就是入口按钮；先保存真实位置，再点遮罩相同位置推进。
		point = touch("开始两步引导")
		capture(name + "第一步", check_text(FIRST))
		tap_at(*point)
		model = check_text(SECOND)
		assert find(model, FIRST) is None
		capture(name + "第二步", model)
		navigation = find(model, "消息与引导")
		assert navigation is not None
		left, top, right, bottom = bounds(navigation)
		tap_at((left + right) // 2, (top + bottom) // 2)
		model = check_text("两步引导已完成。")
		assert find(model, SECOND) is None
		capture(name + "完成", model)
		cases.append({"case": name, "actualHighlightClicks": 2, "passed": True})

	try:
		shell("am", "force-stop", PACKAGE)
		shell("am", "start", "-W", "-n", PACKAGE + "/simple.runtime.android.MainActivity")
		touch("消息与引导")
		standard("普通引导")
		touch("返回")
		touch("消息与引导")
		standard("重新进入引导")
		touch("重置首次引导记录")
		touch("只显示一次的区域引导")
		model = tree()
		assert any(node.get("resource-id", "").endswith("/gcv_img") for node in model.iter("node")), "区域图片未显示"
		capture("区域图片第一步", model)
		content = next(node for node in model.iter("node") if node.get("resource-id") == "android:id/content")
		origin = bounds(content)
		density = int(re.findall(r"\d+", shell("wm", "density").decode())[-1]) / 160
		tap_at(int(origin[0] + 136 * density), int(origin[1] + 120 * density))
		model = check_text("图片提示之后，也可以继续显示文字引导。点击高亮区域结束。")
		capture("区域图片第二步", model)
		navigation = find(model, "消息与引导")
		left, top, right, bottom = bounds(navigation)
		tap_at((left + right) // 2, (top + bottom) // 2)
		model = check_text("两步引导已完成。")
		assert not any(node.get("resource-id", "").endswith("/gcv_img") for node in model.iter("node"))
		capture("区域图片完成", model)
		cases.append({"case": "区域图片引导", "actualHighlightClicks": 2, "passed": True})
	finally:
		apk = LIB / "example/build/apks/signed.apk"
		report = {"serial": serial, "api": shell("getprop", "ro.build.version.sdk").decode().strip(),
			"apk": str(apk), "sha256": hashlib.sha256(apk.read_bytes()).hexdigest(), "cases": cases,
			"passed": len(cases) == 3 and all(item["passed"] for item in cases)}
		(OUTPUT / "report.json").write_bytes((json.dumps(report, ensure_ascii=False, indent=2) + "\n")
			.replace("\n", "\r\n").encode("utf8"))
		print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
	main()
