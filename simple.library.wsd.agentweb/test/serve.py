"""为 AgentWeb 独立测试提供本机网页、上传、下载和取消端点。

运行 python test/serve.py；设备通过 adb reverse tcp:18764 tcp:18764 访问。
仅绑定本机回环地址，不保存上传内容；测试结束后按 Ctrl+C 退出。
"""

from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
import time


ASSETS = Path(__file__).resolve().parent / "assets"


class TestHandler(SimpleHTTPRequestHandler):

	def __init__(self, *arguments, **keywords):
		"""将静态请求限定在本测试工程的资产目录。"""
		super().__init__(*arguments, directory=str(ASSETS), **keywords)


	def do_GET(self):
		"""提供与指定目标文件不同的建议文件名，检查下载封装不会擅自改名。"""
		if self.path not in ("/download.txt", "/slow.bin"):
			super().do_GET()
			return

		data = b"ES4A-AGENTWEB-DOWNLOAD-OK\n" if self.path == "/download.txt" else b"A" * 65536
		count = 1 if self.path == "/download.txt" else 120
		self.send_response(200)
		self.send_header("Content-Type", "application/octet-stream")
		self.send_header("Content-Disposition", 'attachment; filename="agentweb-test.dat"')
		self.send_header("Content-Length", str(len(data) * count))
		self.end_headers()

		# 慢速响应保留足够的取消时间；客户端取消属于此测试的正常结束。
		try:
			for _ in range(count):
				self.wfile.write(data)
				self.wfile.flush()
				if count > 1:
					time.sleep(0.2)
		except (BrokenPipeError, ConnectionResetError, ConnectionAbortedError):
			pass


	def do_POST(self):
		"""确认测试文件已通过网页发到本机，不把收到的文件落盘。"""
		if self.path != "/upload":
			self.send_error(404)
			return
		try:
			length = int(self.headers.get("Content-Length", "0"))
		except ValueError:
			self.send_error(400)
			return
		if not 0 < length <= 1024 * 1024:
			self.send_error(413)
			return

		data = self.rfile.read(length)
		valid = b"ES4A-AGENTWEB-UPLOAD-OK" in data
		body = b"OK" if valid else b"FAIL"
		print("UPLOAD_OK", valid, "bytes", len(data), flush=True)
		self.send_response(200)
		self.send_header("Content-Type", "text/plain; charset=utf-8")
		self.send_header("Content-Length", str(len(body)))
		self.end_headers()
		self.wfile.write(body)


if __name__ == "__main__":
	with ThreadingHTTPServer(("127.0.0.1", 18764), TestHandler) as server:
		print("AgentWeb 测试服务：http://127.0.0.1:18764", flush=True)
		try:
			server.serve_forever()
		except KeyboardInterrupt:
			pass
