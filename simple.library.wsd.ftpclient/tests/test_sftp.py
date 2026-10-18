"""使用本机 Paramiko 临时服务器验证实际 SSH/SFTP 协议，不连接业务服务器。"""
import argparse
import base64
import errno
import hashlib
import json
import logging
import os
from pathlib import Path
import secrets
import socket
import stat
import subprocess
import sys
import tempfile
import threading
import time

import paramiko


class RootedSftp(paramiko.SFTPServerInterface):
	def __init__(self, server, root):
		super().__init__(server)
		self.root = Path(root).resolve()

	def local(self, path):
		target = (self.root / path.lstrip('/')).resolve()
		if os.path.commonpath([str(self.root), str(target)]) != str(self.root):
			raise PermissionError(errno.EACCES, 'outside fixture root')
		return target

	def canonicalize(self, path):
		return '/' + os.path.relpath(self.local(path), self.root).replace('\\', '/').strip('.')

	def list_folder(self, path):
		try:
			result = []
			for child in self.local(path).iterdir():
				attrs = paramiko.SFTPAttributes.from_stat(child.stat())
				attrs.filename = child.name
				result.append(attrs)
			return result
		except OSError as ex:
			return paramiko.SFTPServer.convert_errno(ex.errno)

	def stat(self, path):
		try:
			return paramiko.SFTPAttributes.from_stat(self.local(path).stat())
		except OSError as ex:
			return paramiko.SFTPServer.convert_errno(ex.errno)

	def lstat(self, path):
		if path == '/link':
			attrs = paramiko.SFTPAttributes()
			attrs.st_mode = stat.S_IFLNK | 0o777
			return attrs
		return self.stat(path)

	def open(self, path, flags, attr):
		if path.endswith('/blocked.bin'):
			return paramiko.SFTP_PERMISSION_DENIED
		try:
			fd = os.open(self.local(path), flags | getattr(os, 'O_BINARY', 0), 0o600)
			access = flags & (os.O_WRONLY | os.O_RDWR)
			stream = os.fdopen(fd, 'rb' if access == os.O_RDONLY else ('wb' if access == os.O_WRONLY else 'r+b'))
			handle = paramiko.SFTPHandle(flags)
			if access != os.O_WRONLY:
				handle.readfile = stream
			if access != os.O_RDONLY:
				handle.writefile = stream
			return handle
		except OSError as ex:
			return paramiko.SFTPServer.convert_errno(ex.errno)

	def mutate(self, function, *paths):
		try:
			function(*(self.local(path) for path in paths))
			return paramiko.SFTP_OK
		except OSError as ex:
			return paramiko.SFTPServer.convert_errno(ex.errno)

	def remove(self, path):
		return self.mutate(os.remove, path)

	def rename(self, old, new):
		return self.mutate(os.replace, old, new)

	def posix_rename(self, old, new):
		return self.rename(old, new)

	def mkdir(self, path, attr):
		return self.mutate(os.mkdir, path)

	def rmdir(self, path):
		return self.mutate(os.rmdir, path)


class Authentication(paramiko.ServerInterface):
	def __init__(self, username, password, attempts):
		self.username, self.password = username, password
		self.attempts = attempts

	def get_allowed_auths(self, username):
		return 'password'

	def check_auth_password(self, username, password):
		self.attempts.append(1)
		return paramiko.AUTH_SUCCESSFUL if (username, password) == (self.username, self.password) else paramiko.AUTH_FAILED

	def check_channel_request(self, kind, channel_id):
		return paramiko.OPEN_SUCCEEDED if kind == 'session' else paramiko.OPEN_FAILED_ADMINISTRATIVELY_PROHIBITED


class Fixture:
	def __init__(self, root, bind='127.0.0.1'):
		self.root = root
		self.username, self.password = 'es4a-test', secrets.token_hex(12)
		self.key = paramiko.RSAKey.generate(2048)
		self.pin = 'SHA256:' + base64.b64encode(hashlib.sha256(self.key.asbytes()).digest()).decode().rstrip('=')
		self.listener = socket.socket()
		self.listener.bind((bind, 0))
		self.listener.listen(8)
		self.listener.settimeout(0.2)
		self.port = self.listener.getsockname()[1]
		self.stopped = threading.Event()
		self.connections = []
		self.auth_attempts = []
		threading.Thread(target=self.accept, daemon=True).start()

	def accept(self):
		while not self.stopped.is_set():
			try:
				sock, _ = self.listener.accept()
			except socket.timeout:
				continue
			except OSError:
				return
			threading.Thread(target=self.serve, args=(sock,), daemon=True).start()

	def serve(self, sock):
		transport = paramiko.Transport(sock)
		self.connections.append(transport)
		try:
			transport.add_server_key(self.key)
			transport.set_subsystem_handler('sftp', paramiko.SFTPServer, RootedSftp, root=self.root)
			transport.start_server(server=Authentication(self.username, self.password, self.auth_attempts))
			while transport.is_active() and not self.stopped.wait(0.1):
				pass
		except (paramiko.SSHException, EOFError, OSError):
			pass
		finally:
			transport.close()

	def close(self):
		self.stopped.set()
		self.listener.close()
		for transport in self.connections:
			transport.close()
		for transport in self.connections:
			transport.join(timeout=2)


def main():
	parser = argparse.ArgumentParser()
	parser.add_argument('--serve', action='store_true', help='temporary emulator test endpoint; stop with interrupt')
	parser.add_argument('--bind', default='127.0.0.1')
	args = parser.parse_args()
	logging.getLogger('paramiko').setLevel(logging.CRITICAL)
	library = Path(__file__).resolve().parents[1]
	workspace = library.parents[1]
	with tempfile.TemporaryDirectory(prefix='sftp-fixture-', dir=library / 'build') as temporary:
		remote = Path(temporary) / 'remote'
		remote.mkdir()
		fixture = Fixture(remote, args.bind)
		try:
			if args.serve:
				(remote / 'readme.txt').write_text('ES4A SFTP host confirmation fixture\n', encoding='utf-8')
				(remote / 'folder').mkdir()
				print(json.dumps(dict(port=fixture.port, username=fixture.username, password=fixture.password, fingerprint=fixture.pin)), flush=True)
				while True:
					command = sys.stdin.readline().strip()
					if command == 'stop':
						break
					if command == 'rotate':
						fixture.key = paramiko.RSAKey.generate(2048)
						fixture.pin = 'SHA256:' + base64.b64encode(hashlib.sha256(fixture.key.asbytes()).digest()).decode().rstrip('=')
						print(json.dumps(dict(event='rotated', fingerprint=fixture.pin)), flush=True)
					elif command == 'stats':
						print(json.dumps(dict(auth_attempts=len(fixture.auth_attempts))), flush=True)
					elif not command:
						time.sleep(0.1)
				return
			classpath = os.pathsep.join(str(p) for p in [library / 'build/test-classes', library / 'classes.jar', library / 'libs/jsch-2.28.8-android.jar'])
			result = subprocess.run([str(workspace / 'sdk/tools/jdk1.8.0_503/bin/java.exe'), '-cp', classpath,
				'simple.library.wsd.ftpclient.SftpIntegrationTest', '127.0.0.1', str(fixture.port), fixture.username,
				fixture.password, fixture.pin, temporary], timeout=90)
			raise SystemExit(result.returncode)
		finally:
			fixture.close()


if __name__ == '__main__':
	main()
