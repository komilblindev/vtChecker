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
		
		checkLimitBtn = sHelper.addItem(wx.Button(self, label=_("Check API Limits")))
		checkLimitBtn.Bind(wx.EVT_BUTTON, self.onCheckLimit)
		
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

	def onCheckLimit(self, evt):
		api_key = self.apiKeyEdit.GetValue().strip()
		if not api_key:
			wx.MessageBox(_("Please enter your API Key first."), _("Error"), wx.OK | wx.ICON_ERROR)
			return
			
		import threading
		import urllib.request, json
		
		def check():
			try:
				url = f"https://www.virustotal.com/api/v3/users/{api_key}"
				req = urllib.request.Request(url, headers={"x-apikey": api_key})
				with urllib.request.urlopen(req, timeout=10) as response:
					data = json.loads(response.read().decode('utf-8'))
					attrs = data.get("data", {}).get("attributes", {})
					quotas = attrs.get("quotas", {})
					daily = quotas.get("api_requests_daily", {}).get("user", {})
					hourly = quotas.get("api_requests_hourly", {}).get("user", {})
					monthly = quotas.get("api_requests_monthly", {}).get("user", {})
					
					daily_used = daily.get("used", 0)
					daily_allowed = daily.get("allowed", 500)
					hourly_used = hourly.get("used", 0)
					hourly_allowed = hourly.get("allowed", 0)
					
					msg = _("VirusTotal API Limits:\n\n")
					msg += _("Minute Limit: 4 requests / minute (Standard Free Limit)\n")
					msg += _("Hourly Usage: {used} / {allowed}\n").format(used=hourly_used, allowed=hourly_allowed)
					msg += _("Daily Usage: {used} / {allowed}\n").format(used=daily_used, allowed=daily_allowed)
					
					wx.CallAfter(wx.MessageBox, msg, _("API Limits"), wx.OK | wx.ICON_INFORMATION)
			except Exception as e:
				wx.CallAfter(wx.MessageBox, _("Failed to retrieve limits. Check your API key and connection."), _("Error"), wx.OK | wx.ICON_ERROR)
				
		threading.Thread(target=check).start()

	def onSave(self):
		config.conf["vtChecker"]["apiKey"] = self.apiKeyEdit.GetValue().strip()
		config.conf["vtChecker"]["audioBeep"] = self.audioBeepCheckbox.GetValue()
		config.conf["vtChecker"]["autoCopy"] = self.autoCopyCheckbox.GetValue()
		config.conf["vtChecker"]["autoUpload"] = self.autoUploadCheckbox.GetValue()
