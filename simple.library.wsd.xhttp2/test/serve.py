"""本机可控 HTTP/TLS 服务，ADB 反向映射端口后供唯一 test 工程验证。

只监听回环地址；证书与密码均为临时测试数据，不安装到系统信任存储。
运行条件：Python 3、cryptography；生成的应用资产采用平面路径。
"""

from datetime import datetime, timedelta, timezone
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import parse_qs, urlsplit
import base64
import hashlib
import http.client
import json
import select
import socket
import ssl
import threading
import time

from cryptography import x509
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.hazmat.primitives.serialization import pkcs12
from cryptography.x509.oid import ExtendedKeyUsageOID, NameOID


ROOT = Path(__file__).resolve().parent
ASSETS = ROOT / "assets"
TLS = ROOT / "build/verification/tls"
COUNTERS = {}
LOCK = threading.Lock()
BARRIERS = {}
CONDITION = threading.Condition(LOCK)


def fixtures():
	"""生成只用于本次验证的 CA、localhost 服务端及双向认证客户端。"""
	ASSETS.mkdir(parents=True, exist_ok=True)
	TLS.mkdir(parents=True, exist_ok=True)
	now = datetime.now(timezone.utc)
	required = [ASSETS / "xhttp2-test-ca.pem", ASSETS / "xhttp2-test-client.p12", TLS / "server.pem", TLS / "server-key.pem"]
	if all(path.is_file() for path in required):
		current = x509.load_pem_x509_certificate((TLS / "server.pem").read_bytes())
		if current.not_valid_after_utc > now + timedelta(minutes=5):
			# 重启服务时复用仍有效的测试证书，避免使已编译 APK 的 CA 资产突然失效。
			return
	ca_key = rsa.generate_private_key(public_exponent=65537, key_size=2048)
	ca_name = x509.Name([x509.NameAttribute(NameOID.COMMON_NAME, "ES4A XHttp2 temporary test CA")])
	ca = (x509.CertificateBuilder().subject_name(ca_name).issuer_name(ca_name)
		  .public_key(ca_key.public_key()).serial_number(x509.random_serial_number())
		  .not_valid_before(now - timedelta(minutes=5)).not_valid_after(now + timedelta(days=2))
		  .add_extension(x509.BasicConstraints(ca=True, path_length=0), critical=True)
		  .sign(ca_key, hashes.SHA256()))
	(ASSETS / "xhttp2-test-ca.pem").write_bytes(ca.public_bytes(serialization.Encoding.PEM))

	def leaf(name, usage, server=False):
		key = rsa.generate_private_key(public_exponent=65537, key_size=2048)
		builder = (x509.CertificateBuilder()
				   .subject_name(x509.Name([x509.NameAttribute(NameOID.COMMON_NAME, name)]))
				   .issuer_name(ca_name).public_key(key.public_key())
				   .serial_number(x509.random_serial_number())
				   .not_valid_before(now - timedelta(minutes=5)).not_valid_after(now + timedelta(days=2))
				   .add_extension(x509.BasicConstraints(ca=False, path_length=None), critical=True)
				   .add_extension(x509.ExtendedKeyUsage([usage]), critical=False))
		if server:
			builder = builder.add_extension(x509.SubjectAlternativeName([x509.DNSName("localhost")]), critical=False)
		return key, builder.sign(ca_key, hashes.SHA256())

	server_key, server = leaf("localhost", ExtendedKeyUsageOID.SERVER_AUTH, True)
	(TLS / "server.pem").write_bytes(server.public_bytes(serialization.Encoding.PEM))
	(TLS / "server-key.pem").write_bytes(server_key.private_bytes(
		serialization.Encoding.PEM, serialization.PrivateFormat.PKCS8, serialization.NoEncryption()))
	client_key, client = leaf("ES4A test client", ExtendedKeyUsageOID.CLIENT_AUTH)
	# 仅为 API 24 的 PKCS12 读取兼容生成测试容器，不作为业务密码学配置。
	encryption = (serialization.PrivateFormat.PKCS12.encryption_builder().kdf_rounds(2048)
				  .key_cert_algorithm(pkcs12.PBES.PBESv1SHA1And3KeyTripleDESCBC)
				  .hmac_hash(hashes.SHA1()).build(b"es4a-test"))
	(ASSETS / "xhttp2-test-client.p12").write_bytes(pkcs12.serialize_key_and_certificates(
		b"test-client", client_key, client, [ca], encryption))


class Handler(BaseHTTPRequestHandler):
	protocol_version = "HTTP/1.1"

	def log_message(self, message, *args):
		print(self.command, self.path, message % args, flush=True)

	def send(self, data, status=200, headers=(), stable=False):
		if isinstance(data, (dict, list)):
			data = json.dumps(data, ensure_ascii=False).encode("utf8")
		if isinstance(data, str):
			data = data.encode("utf8")
		if stable:
			self.send_response_only(status)
		else:
			self.send_response(status)
		self.send_header("Content-Type", "application/json; charset=utf-8")
		self.send_header("Content-Length", str(len(data)))
		for name, value in headers:
			self.send_header(name, value)
		self.end_headers()
		if self.command != "HEAD":
			self.wfile.write(data)

	def body(self):
		if self.headers.get("Transfer-Encoding", "").lower() == "chunked":
			data = bytearray()
			while True:
				size = int(self.rfile.readline().split(b";", 1)[0], 16)
				if not size:
					while self.rfile.readline().strip():
						pass
					return bytes(data)
				data.extend(self.rfile.read(size))
				if self.rfile.read(2) != b"\r\n":
					raise ValueError("invalid chunk boundary")
		return self.rfile.read(int(self.headers.get("Content-Length", "0")))

	def handle_method(self):
		parsed = urlsplit(self.path)
		query = parse_qs(parsed.query, keep_blank_values=True)
		value = lambda key, default="": query.get(key, [default])[0]
		path = parsed.path
		key = path + ":" + value("id", "default")
		with LOCK:
			COUNTERS[key] = COUNTERS.get(key, 0) + 1
			count = COUNTERS[key]
		try:
			data = self.body()
		except (OSError, ValueError):
			# 分块上传取消可能发生在块头或块体之间，读端已断开时不再构造响应。
			self.close_connection = True
			return
		try:
			if path == "/retry" and count <= int(value("fail", "2")):
				time.sleep(float(value("delay", "0.5")))
			if path == "/delay":
				time.sleep(float(value("seconds", "1")))
			if path == "/stats":
				with LOCK:
					self.send(dict(COUNTERS))
			elif path == "/barrier":
				group = value("group")
				with CONDITION:
					state = BARRIERS.setdefault(group, {"active": 0, "peak": 0})
					state["active"] += 1
					state["peak"] = max(state["peak"], state["active"])
					CONDITION.notify_all()
					deadline = time.monotonic() + 0.6
					while state["peak"] < 2 and time.monotonic() < deadline:
						CONDITION.wait(max(0, deadline - time.monotonic()))
					peak = state["peak"]
					state["active"] -= 1
				self.send({"peak": peak, "headers": list(self.headers.raw_items())})
			elif path == "/fallback":
				if count == 1:
					self.send({"value": "saved"}, headers=[("Cache-Control", "no-store")])
				else:
					self.send({"error": "controlled fallback failure"}, 503)
			elif path == "/business":
				self.send({"code": int(value("code", "0")), "msg": "业务结果", "data": {"items": ["中文", "next"]}})
			elif path == "/business-fraction":
				self.send({"code": 0.5, "msg": "not an integer", "data": {}})
			elif path == "/bad-json":
				self.send("{broken")
			elif path.startswith("/status/"):
				self.send({"error": "controlled HTTP error"}, int(path.rsplit("/", 1)[1]))
			elif path in ("/cache", "/http-cache", "/stable"):
				self.send({"value": value("body", "stable")}, headers=[
					("Cache-Control", "max-age=120" if path == "/http-cache" else "no-store"),
					("X-Test", "stable" if path == "/stable" else str(count))], stable=path == "/stable")
			elif path == "/redirect":
				self.send("", 302, [("Location", "/echo")])
			elif path.startswith("/file"):
				length = int(value("size", "262144"))
				block = b"ES4A-XHttp2-"
				content = (block * (length // len(block) + 1))[:length]
				self.send_response(200)
				self.send_header("Content-Length", str(length + (17 if path == "/file-short" else 0)))
				self.send_header("Content-Type", "application/octet-stream")
				self.send_header("X-SHA256", hashlib.sha256(content).hexdigest())
				self.end_headers()
				if self.command != "HEAD":
					for start in range(0, length, 8192):
						self.wfile.write(content[start:start + 8192])
						self.wfile.flush()
						time.sleep(float(value("delay", "0")))
				if path == "/file-short":
					self.close_connection = True
			else:
				peer = self.connection.getpeercert() if isinstance(self.connection, ssl.SSLSocket) else None
				cookie_headers = [("Set-Cookie", entry) for entry in query.get("set", [])]
				self.send({
					"method": self.command, "query": query, "body": data.decode("utf8", errors="replace"),
					"base64": base64.b64encode(data).decode(), "sha256": hashlib.sha256(data).hexdigest(),
					"headers": list(self.headers.raw_items()), "count": count, "peer": peer,
				}, headers=cookie_headers + [("X-Duplicate", "one"), ("X-Duplicate", "two")])
		except (BrokenPipeError, ConnectionResetError, ConnectionAbortedError, ssl.SSLError):
			# 超时、取消和错误长度用例会主动断开连接，仍保留服务器计数供断言。
			self.close_connection = True

	do_GET = handle_method
	do_HEAD = handle_method
	do_POST = handle_method
	do_PUT = handle_method
	do_PATCH = handle_method
	do_DELETE = handle_method
	do_OPTIONS = handle_method


class ProxyHandler(Handler):
	"""只代理本机测试端口，验证 Basic 代理认证、头隔离和 HTTPS CONNECT。"""

	def authorized(self):
		expected = "Basic " + base64.b64encode(b"qa:proxy").decode("ascii")
		if self.headers.get("Proxy-Authorization") == expected:
			return True
		self.send("", 407, [("Proxy-Authenticate", 'Basic realm="ES4A test"')])
		return False

	def handle_method(self):
		if not self.authorized():
			self.close_connection = True
			return
		target = urlsplit(self.path)
		if target.scheme != "http" or target.hostname not in ("127.0.0.1", "localhost") or target.port != 18780:
			self.send("Only local test destination is allowed", 403)
			return
		data = self.body()
		connection = http.client.HTTPConnection(target.hostname, target.port, timeout=5)
		try:
			path = target.path or "/"
			if target.query:
				path += "?" + target.query
			connection.putrequest(self.command, path, skip_host=True, skip_accept_encoding=True)
			for name, value in self.headers.raw_items():
				if name.lower() not in ("proxy-authorization", "proxy-connection", "connection", "transfer-encoding", "content-length"):
					connection.putheader(name, value)
			connection.putheader("Content-Length", str(len(data)))
			connection.endheaders(data)
			response = connection.getresponse()
			body = response.read()
			self.send_response_only(response.status)
			for name, value in response.getheaders():
				if name.lower() not in ("connection", "transfer-encoding"):
					self.send_header(name, value)
			self.end_headers()
			if self.command != "HEAD":
				self.wfile.write(body)
		finally:
			connection.close()

	def do_CONNECT(self):
		if not self.authorized():
			self.close_connection = True
			return
		host, separator, port = self.path.rpartition(":")
		if not separator or host not in ("127.0.0.1", "localhost") or port not in ("18743", "18744"):
			self.send("Only local test destination is allowed", 403)
			return
		with socket.create_connection((host, int(port)), timeout=5) as remote:
			self.send_response_only(200, "Connection established")
			self.end_headers()
			self.wfile.flush()
			peers = [self.connection, remote]
			while True:
				ready, _, _ = select.select(peers, [], [], 10)
				if not ready:
					break
				for source in ready:
					data = source.recv(32768)
					if not data:
						self.close_connection = True
						return
					destination = remote if source is self.connection else self.connection
					destination.sendall(data)
		self.close_connection = True

	do_GET = handle_method
	do_HEAD = handle_method
	do_POST = handle_method
	do_PUT = handle_method
	do_PATCH = handle_method
	do_DELETE = handle_method
	do_OPTIONS = handle_method


def main():
	fixtures()
	servers = []
	for port, tls, mutual in [(18780, False, False), (18743, True, False), (18744, True, True)]:
		server = ThreadingHTTPServer(("127.0.0.1", port), Handler)
		server.daemon_threads = True
		if tls:
			context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
			context.minimum_version = ssl.TLSVersion.TLSv1_2
			context.load_cert_chain(TLS / "server.pem", TLS / "server-key.pem")
			if mutual:
				context.load_verify_locations(ASSETS / "xhttp2-test-ca.pem")
				context.verify_mode = ssl.CERT_REQUIRED
			server.socket = context.wrap_socket(server.socket, server_side=True)
		threading.Thread(target=server.serve_forever, daemon=True).start()
		servers.append(server)
		print("READY", port, "TLS" if tls else "HTTP", "mutual" if mutual else "", flush=True)
	proxy = ThreadingHTTPServer(("127.0.0.1", 18781), ProxyHandler)
	proxy.daemon_threads = True
	threading.Thread(target=proxy.serve_forever, daemon=True).start()
	servers.append(proxy)
	print("READY", 18781, "HTTP proxy", flush=True)
	try:
		while True:
			time.sleep(1)
	except KeyboardInterrupt:
		for server in servers:
			server.shutdown()
			server.server_close()


if __name__ == "__main__":
	main()
