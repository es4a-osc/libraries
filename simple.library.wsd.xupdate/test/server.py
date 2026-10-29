"""
XUpdate 独立测试的本机 HTTP 服务，不访问外部服务器。
xhwsd@qq.com 2026-10-28

先用同一测试工程、同一 android_debug.p12 生成较高版本 APK，再执行：
python server.py --apk "升级APK绝对路径"
adb -s 实际设备编号 reverse tcp:18765 tcp:18765

界面用例依赖本文件提供的固定端点。服务端版本号 99 是可重复的检查模拟值，
系统安装是否成功必须核对 APK 的真实包名、签名及安装后的真实版本。
"""

import argparse
import hashlib
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
import json
import ssl
from pathlib import Path
import time
from urllib.parse import parse_qs, urlsplit


class UpdateServer(ThreadingHTTPServer):
	"""只保存本次测试的 APK 路径及接口监听地址。"""

	def __init__(self, address, apk, version_code=99):
		"""校验测试 APK 存在，监听本机地址，不读取用户业务文件。"""
		self.requests = []
		self.recover = False
		self.version_code = version_code
		self.apk = apk.resolve(strict=True)
		if not self.apk.is_file():
			raise ValueError("必须指定 APK 文件。")
		# 固定本次会话的文件内容，避免其它构建替换 APK 后改变摘要或 Range 响应。
		self.payload = self.apk.read_bytes()
		super().__init__(address, UpdateHandler)


class UpdateHandler(BaseHTTPRequestHandler):
	"""提供成功、取消、下载失败和响应格式等受控用例。"""

	def do_GET(self):
		"""处理检查接口或 APK 下载。"""
		self.handle_request(False)

	def do_POST(self):
		"""处理 UTF-8 表单及请求头核对。"""
		self.handle_request(True)

	def handle_request(self, post):
		"""使用同一 APK 动态计算摘要，避免硬编码文件长度或校验值。"""
		endpoint = urlsplit(self.path).path
		length = int(self.headers.get("Content-Length", "0"))
		if length < 0 or length > 1024 * 1024:
			self.respond(b"request too large", 413)
			return
		body = self.rfile.read(length) if post else b""
		try:
			data = self.server.payload
			if endpoint == "/stats":
				self.respond(json.dumps(self.server.requests).encode("utf-8"))
				return
			if endpoint == "/reset":
				self.server.requests.clear()
				self.server.recover = False
				self.respond(b"{}")
				return
			if endpoint == "/allow-recovery":
				self.server.recover = True
				self.respond(b"{}")
				return
			if endpoint == "/recoverapk" and not self.server.recover:
				self.respond(b"temporary unavailable", 503)
				return
			if endpoint in ("/rangeapk", "/norangeapk", "/recoverapk"):
				# 原样记录实际 Range；测试必须证明暂停后的请求起点大于零。
				range_header = self.headers.get("Range", "")
				start, end = 0, len(data) - 1
				partial = endpoint != "/norangeapk" and range_header.startswith("bytes=")
				if partial:
					left, right = range_header[6:].split("-", 1)
					start = int(left or "0")
					end = min(int(right) if right else end, end)
					if start > end:
						self.respond(b"range invalid", 416)
						return
				entry = {"path": endpoint, "range": range_header, "start": start,
					"end": end, "status": 206 if partial else 200, "sent": 0,
					"header": self.headers.get("X-Update-Test", "")}
				self.server.requests.append(entry)
				self.send_response(entry["status"])
				self.send_header("Content-Type", "application/vnd.android.package-archive")
				self.send_header("Content-Length", str(end - start + 1))
				if endpoint != "/norangeapk":
					self.send_header("Accept-Ranges", "bytes")
				if partial:
					self.send_header("Content-Range", f"bytes {start}-{end}/{len(data)}")
				self.end_headers()
				for position in range(start, end + 1, 16384):
					block = data[position:min(end + 1, position + 16384)]
					self.wfile.write(block)
					self.wfile.flush()
					entry["sent"] += len(block)
					time.sleep(0.08)
				return
			if endpoint in ("/apk", "/slowapk"):
				self.send_response(200)
				self.send_header("Content-Type", "application/vnd.android.package-archive")
				self.send_header("Content-Length", str(len(data)))
				self.end_headers()
				for position in range(0, len(data), 16384):
					self.wfile.write(data[position:position + 16384])
					self.wfile.flush()
					time.sleep(0.1 if endpoint == "/slowapk" else 0.002)
				return
			if endpoint == "/slowcheck":
				time.sleep(3)
			if endpoint == "/error":
				self.respond(b"controlled failure", 503)
				return
			if endpoint == "/malformed":
				self.respond(b"{not-json")
				return
			if endpoint == "/custom":
				self.respond(b"custom")
				return

			# 严格核对表单转义和实例请求头，不依赖第三方网络库。
			name = "2.0"
			if endpoint == "/post":
				if not post or parse_qs(body.decode("utf-8")) != {"城市": ["杭州 + 上海"]}:
					raise ValueError("POST 参数不符。")
				if self.headers.get("X-Update-Test") != "isolated":
					raise ValueError("请求头不符。")
				name = "POST已验证"
			if endpoint == "/json":
				if not post or json.loads(body.decode("utf8")) != {"城市": "杭州 + 上海"}:
					raise ValueError("JSON 参数不符。")
				if not self.headers.get("Content-Type", "").startswith("application/json"):
					raise ValueError("JSON Content-Type 不符。")
				name = "JSON已验证"
			status = 0 if endpoint == "/none" else 2 if endpoint == "/forced" else 3 if endpoint == "/ignore" else 1
			port = self.server.server_address[1]
			value = {
				"Code": 0,
				"UpdateStatus": status,
				"VersionCode": self.server.version_code,
				"VersionName": name,
				"ModifyContent": "独立测试更新\n校验完成后才允许安装。",
				"DownloadUrl": f"http://127.0.0.1:{port}/" + ("slowapk" if endpoint == "/slowdownload" else "recoverapk" if endpoint == "/resume-recover" else "norangeapk" if endpoint == "/resume-norange" else "rangeapk" if endpoint.startswith("/resume") else "apk"),
				"ApkSize": 1 if endpoint == "/badsize" else 0,
				"ApkMd5": "0" * 32 if endpoint in ("/badmd5", "/resume-badmd5") else hashlib.md5(data).hexdigest(),
			}
			self.respond(json.dumps(value, ensure_ascii=False).encode("utf-8"))
		except (BrokenPipeError, ConnectionResetError, ConnectionAbortedError):
			# 客户端取消连接是预期用例，不将其伪装为成功下载。
			pass
		except Exception as exception:
			self.respond(str(exception).encode("utf-8"), 500)

	def respond(self, data, status=200):
		"""发送具有明确长度和编码的响应。"""
		self.send_response(status)
		self.send_header("Content-Type", "application/json; charset=utf-8")
		self.send_header("Content-Length", str(len(data)))
		self.end_headers()
		self.wfile.write(data)


if __name__ == "__main__":
	parser = argparse.ArgumentParser(description="XUpdate 本机功能测试服务，仅监听 127.0.0.1。")
	parser.add_argument("--apk", type=Path, required=True, help="同包名、同签名的升级测试 APK。")
	parser.add_argument("--port", type=int, default=18765, help="监听端口，默认 18765。")
	parser.add_argument("--version-code", type=int, default=99, help="服务端声明的真实 APK 版本号，安装验证时与 APK 一致。")
	parser.add_argument("--certificate", type=Path, help="仅用于本地 TLS 负例的证书。")
	parser.add_argument("--key", type=Path, help="仅用于本地 TLS 负例的密钥。")
	arguments = parser.parse_args()
	if bool(arguments.certificate) != bool(arguments.key):
		parser.error("证书和密钥必须同时指定。")
	with UpdateServer(("127.0.0.1", arguments.port), arguments.apk, arguments.version_code) as server:
		if arguments.certificate:
			context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
			context.load_cert_chain(arguments.certificate, arguments.key)
			server.socket = context.wrap_socket(server.socket, server_side=True)
		print(f"测试服务：http://127.0.0.1:{arguments.port}", flush=True)
		server.serve_forever()
