# -*- coding: utf-8 -*-
import hashlib
import os
import api as nvda_api
import time
import keyboardHandler

def check_clipboard_for_file():
	try:
		import win32clipboard
		win32clipboard.OpenClipboard()
		try:
			if win32clipboard.IsClipboardFormatAvailable(win32clipboard.CF_HDROP):
				data = win32clipboard.GetClipboardData(win32clipboard.CF_HDROP)
				if data and len(data) > 0:
					return data[0]
		finally:
			win32clipboard.CloseClipboard()
	except Exception:
		# Fallback to fixed ctypes if win32clipboard fails
		import ctypes
		user32 = ctypes.windll.user32
		shell32 = ctypes.windll.shell32
		user32.GetClipboardData.restype = ctypes.c_void_p
		user32.GetClipboardData.argtypes = [ctypes.c_uint]
		shell32.DragQueryFileW.restype = ctypes.c_uint
		shell32.DragQueryFileW.argtypes = [ctypes.c_void_p, ctypes.c_uint, ctypes.c_wchar_p, ctypes.c_uint]
		if user32.OpenClipboard(0):
			try:
				hdrop = user32.GetClipboardData(15) # CF_HDROP
				if hdrop:
					count = shell32.DragQueryFileW(hdrop, 0xFFFFFFFF, None, 0)
					if count > 0:
						buf = ctypes.create_unicode_buffer(260)
						shell32.DragQueryFileW(hdrop, 0, buf, 260)
						return buf.value
			finally:
				user32.CloseClipboard()
	return None

def get_focused_file_path():
	# 1. First, check if there is already a file in the clipboard
	path = check_clipboard_for_file()
	if path and os.path.isfile(path):
		return path
		
	# 2. Try to get it by sending Ctrl+C
	try:
		keyboardHandler.KeyboardInputGesture.fromName("control+c").send()
		time.sleep(0.15)
		path = check_clipboard_for_file()
		if path and os.path.isfile(path):
			return path
	except Exception:
		pass
		
	return None

def calculate_sha256(filepath):
	if not filepath or not os.path.isfile(filepath):
		return None
	sha256_hash = hashlib.sha256()
	try:
		with open(filepath, "rb") as f:
			for byte_block in iter(lambda: f.read(65536), b""):
				sha256_hash.update(byte_block)
		return sha256_hash.hexdigest()
	except IOError:
		return None
