# -*- coding: utf-8 -*-
import hashlib
import os
import comtypes.client
import api as nvda_api

def get_focused_file_path():
    """Gets the path of the currently focused file in Windows Explorer or Desktop."""
    try:
        obj = nvda_api.getFocusObject()
        if not obj:
            return None
        
        app_name = obj.appModule.appName.lower() if obj.appModule else ""
        if app_name != "explorer":
            return None
            
        shell = comtypes.client.CreateObject("Shell.Application", dynamic=True)
        for window in shell.Windows():
            # Match the window handle
            if window.hwnd == obj.windowHandle:
                items = window.Document.SelectedItems()
                if items.Count > 0:
                    return items.Item(0).Path
                    
        # Fallback for desktop
        desktop_handle = None # just conceptual fallback
        # A more robust fallback would check clipboard if the user pressed Ctrl+C
    except Exception:
        pass
        
    return None

def calculate_sha256(filepath):
    """Calculates SHA-256 hash of a file efficiently by reading in chunks."""
    if not os.path.isfile(filepath):
        return None
        
    sha256_hash = hashlib.sha256()
    try:
        with open(filepath, "rb") as f:
            for byte_block in iter(lambda: f.read(65536), b""):
                sha256_hash.update(byte_block)
        return sha256_hash.hexdigest()
    except IOError:
        return None
