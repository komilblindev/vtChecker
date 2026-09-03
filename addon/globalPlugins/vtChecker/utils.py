# -*- coding: utf-8 -*-
import hashlib
import os
import api as nvda_api
import ctypes

def get_focused_file_path():
    """Gets the path of the currently focused file in Windows Explorer or Desktop."""
    try:
        obj = nvda_api.getFocusObject()
        if not obj:
            return None
        
        app_name = obj.appModule.appName.lower() if obj.appModule else ""
        if app_name != "explorer":
            return None
            
        try:
            import comtypes.client
            shell = comtypes.client.CreateObject("Shell.Application", dynamic=True)
            windows = shell.Windows()
            for i in range(windows.Count):
                window = windows.Item(i)
                win_hwnd = getattr(window, "HWND", getattr(window, "hwnd", None))
                if win_hwnd == obj.windowHandle:
                    items = window.Document.SelectedItems()
                    if items.Count > 0:
                        item = items.Item(0)
                        path = getattr(item, "Path", getattr(item, "path", None))
                        if path:
                            return path
        except Exception:
            pass # Fallback to clipboard
            
        # Fallback: Clipboard approach (very reliable for both Explorer and Desktop)
        import keyboardHandler
        import time
        
        # Send Ctrl+C
        keyboardHandler.KeyboardInputGesture.fromName("control+c").send()
        time.sleep(0.1)
        
        CF_HDROP = 15
        user32 = ctypes.windll.user32
        shell32 = ctypes.windll.shell32
        
        if user32.OpenClipboard(0):
            try:
                hdrop = user32.GetClipboardData(CF_HDROP)
                if hdrop:
                    count = shell32.DragQueryFileW(hdrop, 0xFFFFFFFF, None, 0)
                    if count > 0:
                        buf = ctypes.create_unicode_buffer(260)
                        shell32.DragQueryFileW(hdrop, 0, buf, 260)
                        return buf.value
            finally:
                user32.CloseClipboard()

    except Exception:
        pass
        
    return None

def calculate_sha256(filepath):
    """Calculates SHA-256 hash of a file efficiently by reading in chunks."""
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
