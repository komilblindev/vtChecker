# -*- coding: utf-8 -*-
import urllib.request
import urllib.error
import json
import os
import uuid
import mimetypes
import addonHandler
addonHandler.initTranslation()

API_BASE_URL = "https://www.virustotal.com/api/v3"

def check_hash(sha256: str, api_key: str) -> dict:
	url = f"{API_BASE_URL}/files/{sha256}"
	req = urllib.request.Request(url, headers={"x-apikey": api_key})
	try:
		with urllib.request.urlopen(req, timeout=10) as response:
			return json.loads(response.read().decode('utf-8'))
	except urllib.error.HTTPError as e:
		if e.code == 404:
			return None # Hash not found
		raise

def upload_file(filepath: str, api_key: str) -> dict:
	"""Uploads a file to VirusTotal. Max size 32MB."""
	url = f"{API_BASE_URL}/files"
	boundary = uuid.uuid4().hex
	
	filename = os.path.basename(filepath)
	mime_type = mimetypes.guess_type(filename)[0] or 'application/octet-stream'
	
	with open(filepath, 'rb') as f:
		file_content = f.read()

	body = bytearray()
	body.extend(f"--{boundary}\r\n".encode('utf-8'))
	body.extend(f'Content-Disposition: form-data; name="file"; filename="{filename}"\r\n'.encode('utf-8'))
	body.extend(f"Content-Type: {mime_type}\r\n\r\n".encode('utf-8'))
	body.extend(file_content)
	body.extend(f"\r\n--{boundary}--\r\n".encode('utf-8'))

	req = urllib.request.Request(url, data=body, headers={
		"x-apikey": api_key,
		"Content-Type": f"multipart/form-data; boundary={boundary}"
	})
	
	with urllib.request.urlopen(req, timeout=60) as response:
		return json.loads(response.read().decode('utf-8'))

def get_quota(api_key: str) -> dict:
	url = f"{API_BASE_URL}/users/{api_key}/overall_quotas"
	req = urllib.request.Request(url, headers={"x-apikey": api_key})
	with urllib.request.urlopen(req, timeout=10) as response:
		return json.loads(response.read().decode('utf-8'))

def format_short_summary(data: dict) -> str:
	if not data or 'data' not in data:
		return _("No results.")
	
	attrs = data['data']['attributes']
	stats = attrs.get('last_analysis_stats', {})
	malicious = stats.get('malicious', 0)
	suspicious = stats.get('suspicious', 0)
	undetected = stats.get('undetected', 0)
	total = sum(stats.values()) if stats else 0
	
	return _("Detection ratio: {count}/{total}").format(count=malicious + suspicious, total=total)

def format_detailed_report(data: dict) -> str:
	if not data or 'data' not in data:
		return _("No detailed results available.")
	
	attrs = data['data']['attributes']
	results = attrs.get('last_analysis_results', {})
	
	report_lines = []
	for engine, details in results.items():
		if details['category'] in ('malicious', 'suspicious'):
			report_lines.append(f"{engine}: {details['result']}")
			
	if not report_lines:
		return _("No engines detected this file as malicious.")
		
	return "\n".join(report_lines)

def check_url(url_string: str, api_key: str) -> dict:
	import base64
	url_id = base64.urlsafe_b64encode(url_string.encode('utf-8')).decode('utf-8').strip("=")
	endpoint = f"{API_BASE_URL}/urls/{url_id}"
	req = urllib.request.Request(endpoint, headers={"x-apikey": api_key})
	try:
		with urllib.request.urlopen(req, timeout=10) as response:
			return json.loads(response.read().decode('utf-8'))
	except urllib.error.HTTPError as e:
		if e.code == 404:
			return None
		raise
