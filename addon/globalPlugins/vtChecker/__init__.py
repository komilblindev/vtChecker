# -*- coding: utf-8 -*-
import globalPluginHandler
import scriptHandler
import ui
import core
import threading
import config
from logHandler import log
import gui
import api as nvda_api
import addonHandler

from . import vt_settings
from . import api as vt_api
from . import utils as vt_utils

addonHandler.initTranslation()

class GlobalPlugin(globalPluginHandler.GlobalPlugin):
    
    def __init__(self, *args, **kwargs):
        super(GlobalPlugin, self).__init__(*args, **kwargs)
        gui.settingsDialogs.NVDASettingsDialog.categoryClasses.append(vt_settings.VTSettingsPanel)
        self.worker_thread = None
        
    def terminate(self):
        try:
            gui.settingsDialogs.NVDASettingsDialog.categoryClasses.remove(vt_settings.VTSettingsPanel)
        except ValueError:
            pass
        super(GlobalPlugin, self).terminate()
        
    @scriptHandler.script(
        description=_("Checks the focused file with VirusTotal. Press twice for a detailed report."),
        category=_("VirusTotal"),
        gesture=None
    )
    def script_checkFile(self, gesture):
        if scriptHandler.getLastScriptRepeatCount() == 0:
            self.do_check(full_report=False)
        else:
            self.do_check(full_report=True)
            
    def do_check(self, full_report=False):
        api_key = config.conf["vtChecker"].get("apiKey", "")
        if not api_key:
            ui.message(_("Please set your VirusTotal API key in NVDA settings."))
            return
            
        filepath = vt_utils.get_focused_file_path()
        if not filepath:
            ui.message(_("No file focused in Windows Explorer."))
            return
            
        ui.message(_("Checking VirusTotal..."))
        
        def run():
            try:
                sha256 = vt_utils.calculate_sha256(filepath)
                if not sha256:
                    core.callLater(10, ui.message, _("Could not read file for hashing."))
                    return
                
                res = vt_api.check_hash(sha256, api_key)
                
                if res is None: # Not found
                    if config.conf["vtChecker"].get("autoUpload", False):
                        core.callLater(10, ui.message, _("Hash not found. Uploading file..."))
                        if config.conf["vtChecker"].get("audioBeep", True):
                            import tones
                            tones.beep(500, 200)
                            
                        upload_res = vt_api.upload_file(filepath, api_key)
                        
                        if config.conf["vtChecker"].get("audioBeep", True):
                            import tones
                            tones.beep(1000, 200)
                            
                        core.callLater(10, ui.message, _("File uploaded and queued for analysis. Try checking again later."))
                        return
                    else:
                        core.callLater(10, ui.message, _("File not found in VirusTotal database."))
                        return
                
                if full_report:
                    report = vt_api.format_detailed_report(res)
                    core.callLater(10, ui.browseableMessage, report, _("VirusTotal Detailed Report"))
                else:
                    summary = vt_api.format_short_summary(res)
                    core.callLater(10, ui.message, summary)
                    if config.conf["vtChecker"].get("autoCopy", True):
                        nvda_api.copyToClip(summary)
                        core.callLater(1000, ui.message, _("Result copied to clipboard."))
                        
            except Exception as e:
                log.error("VirusTotal Check Error", exc_info=True)
                core.callLater(10, ui.message, _("Network or API error occurred."))
                
        self.worker_thread = threading.Thread(target=run)
        self.worker_thread.daemon = True
        self.worker_thread.start()
