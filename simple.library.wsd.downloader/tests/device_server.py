"""供 Android 设备测试的本机 HTTP 固定数据服务；仅监听回环地址。"""
import argparse
import hashlib
import json
import threading
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

DATA = bytes((i * 31 + i // 257) % 256 for i in range(4 * 1024 * 1024))
REQUESTS = []
LOCK = threading.Lock()


class Handler(BaseHTTPRequestHandler):
	protocol_version = "HTTP/1.1"

	def handle(self):
		try:
			super().handle()
		except (ConnectionResetError, ConnectionAbortedError, BrokenPipeError):
			pass

	def log_message(self, *_):
		pass

	def do_GET(self):
		route = self.path.split("?", 1)[0]
		value = self.headers.get("Range", "")
		with LOCK:
			REQUESTS.append({"path": route, "range": value, "if_range": self.headers.get("If-Range", "")})
		if route == "/info":
			with LOCK:
				data = json.dumps({"size": len(DATA), "sha256": hashlib.sha256(DATA).hexdigest(), "requests": REQUESTS}, ensure_ascii=False).encode()
			self.send_response(200)
			self.send_header("Content-Length", str(len(data)))
			self.end_headers()
			self.wfile.write(data)
			return
		if route == "/missing":
			self.send_response(404)
			self.send_header("Content-Length", "0")
			self.end_headers()
			return
		offset = int(value[6:-1]) if value and route != "/ignore" else 0
		partial = bool(value) and route != "/ignore"
		if offset >= len(DATA):
			self.send_response(416)
			self.send_header("Content-Range", f"bytes */{len(DATA)}")
			self.send_header("Content-Length", "0")
			self.end_headers()
			return
		self.send_response(206 if partial else 200)
		self.send_header("ETag", '"es4a-download-fixture-v1"')
		if partial:
			start = offset + 1 if route == "/bad-range" else offset
			self.send_header("Content-Range", f"bytes {start}-{len(DATA)-1}/{len(DATA)}")
		chunked = route == "/chunked"
		self.send_header("Transfer-Encoding" if chunked else "Content-Length", "chunked" if chunked else str(len(DATA) - offset))
		self.end_headers()
		try:
			for position in range(offset, len(DATA), 8192):
				block = DATA[position:position + 8192]
				if chunked:
					self.wfile.write(f"{len(block):X}\r\n".encode())
				self.wfile.write(block)
				if chunked:
					self.wfile.write(b"\r\n")
				self.wfile.flush()
				time.sleep(0.04)
			if chunked:
				self.wfile.write(b"0\r\n\r\n")
				self.wfile.flush()
		except (BrokenPipeError, ConnectionResetError, ConnectionAbortedError):
			pass


if __name__ == "__main__":
	parser = argparse.ArgumentParser()
	parser.add_argument("--port", type=int, default=18765)
	args = parser.parse_args()
	server = ThreadingHTTPServer(("127.0.0.1", args.port), Handler)
	server.daemon_threads = True
	print(json.dumps({"port": server.server_port, "size": len(DATA), "sha256": hashlib.sha256(DATA).hexdigest()}), flush=True)
	try:
		server.serve_forever()
	except KeyboardInterrupt:
		pass
	finally:
		server.server_close()
