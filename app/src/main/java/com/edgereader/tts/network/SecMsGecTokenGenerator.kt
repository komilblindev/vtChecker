package com.edgereader.tts.network

import java.security.MessageDigest

object SecMsGecTokenGenerator {

    private const val TRUSTED_CLIENT_TOKEN = "6A5AA1D4EAFF4E9FB37E23D68491D6F4"
    
    // Windows epoch is 1601-01-01. UNIX epoch is 1970-01-01.
    // Difference in milliseconds: 11,644,473,600,000
    private const val EPOCH_DIFF_MS = 11644473600000L

    /**
     * Generates the Sec-MS-GEC DRM token required by Edge TTS API.
     * @param clockSkewMs an optional clock adjustment in case the server returns 403.
     */
    fun generateToken(clockSkewMs: Long = 0L): String {
        val currentTimeMs = System.currentTimeMillis() + clockSkewMs
        
        // Convert to Windows FILETIME (100-nanosecond intervals)
        val fileTimeTicks = (currentTimeMs + EPOCH_DIFF_MS) * 10000L
        
        // Quantize to 5-minute windows (5 minutes = 300 seconds = 3,000,000,000 ticks)
        val window = 3000000000L
        val roundedTicks = fileTimeTicks - (fileTimeTicks % window)
        
        val stringToHash = "$roundedTicks$TRUSTED_CLIENT_TOKEN"
        
        return sha256Hex(stringToHash)
    }

    private fun sha256Hex(input: String): String {
        val bytes = input.toByteArray(Charsets.US_ASCII)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02X".format(it) }
    }
}
