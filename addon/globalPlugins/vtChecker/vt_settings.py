# -*- coding: utf-8 -*-
import wx
import gui
from gui.settingsDialogs import SettingsPanel
import config
import addonHandler

# Initialize config structure
confspec = {
	"apiKey": "string(default='')",
	"audioBeep": "boolean(default=True)",
	"autoCopy": "boolean(default=True)",
	"autoUpload": "boolean(default=False)",
}
config.conf.spec["vtChecker"] = confspec

addonHandler.initTranslation()

class VTSettingsPanel(SettingsPanel):
	title = _("VirusTotal")
	
	def makeSettings(self, settingsSizer):
		sHelper = gui.guiHelper.BoxSizerHelper(self, sizer=settingsSizer)
		
		self.apiKeyEdit = sHelper.addLabeledControl(
			_("VirusTotal &API Key:"),
			wx.TextCtrl,
			value=config.conf["vtChecker"]["apiKey"]
		)
		
		getApiBtn = sHelper.addItem(wx.Button(self, label=_("Get API &Key from website")))
		getApiBtn.Bind(wx.EVT_BUTTON, self.onGetApiKey)
		
		self.audioBeepCheckbox = sHelper.addItem(
			wx.CheckBox(self, label=_("Play audio &beep during file upload"))
		)
		self.audioBeepCheckbox.SetValue(config.conf["vtChecker"]["audioBeep"])
		
		self.autoCopyCheckbox = sHelper.addItem(
			wx.CheckBox(self, label=_("Automatically &copy results to clipboard"))
		)
		self.autoCopyCheckbox.SetValue(config.conf["vtChecker"]["autoCopy"])
		
		self.autoUploadCheckbox = sHelper.addItem(
			wx.CheckBox(self, label=_("Automatically &upload file if hash not found"))
		)
		self.autoUploadCheckbox.SetValue(config.conf["vtChecker"]["autoUpload"])


	def onGetApiKey(self, evt):
		import os
		os.startfile("https://www.virustotal.com/gui/join-us")

	def onSave(self):
		config.conf["vtChecker"]["apiKey"] = self.apiKeyEdit.GetValue().strip()
		config.conf["vtChecker"]["audioBeep"] = self.audioBeepCheckbox.GetValue()
		config.conf["vtChecker"]["autoCopy"] = self.autoCopyCheckbox.GetValue()
		config.conf["vtChecker"]["autoUpload"] = self.autoUploadCheckbox.GetValue()
