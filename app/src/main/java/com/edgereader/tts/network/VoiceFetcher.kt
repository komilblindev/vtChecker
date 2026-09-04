package com.edgereader.tts.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

@Serializable
data class VoiceModel(
    @SerialName("Name") val name: String,
    @SerialName("ShortName") val shortName: String,
    @SerialName("Gender") val gender: String,
    @SerialName("Locale") val locale: String,
    @SerialName("SuggestedCodec") val suggestedCodec: String,
    @SerialName("FriendlyName") val friendlyName: String,
    @SerialName("Status") val status: String
)

class VoiceFetcher(private val client: OkHttpClient) {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    suspend fun fetchVoices(): List<VoiceModel> = withContext(Dispatchers.IO) {
        val url = "https://speech.platform.bing.com/consumer/speech/synthesize/readaloud/voices/list?trustedclienttoken=6A5AA1D4EAFF4E9FB37E23D68491D6F4"
        val request = Request.Builder()
            .url(url)
            .addHeader("Origin", "chrome-extension://jdiccldimpdaibmpdkjnbmckianbfold")
            .build()
            
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val bodyString = response.body?.string() ?: "[]"
            return@withContext json.decodeFromString<List<VoiceModel>>(bodyString)
        } else {
            throw Exception("Failed to fetch voices: ${response.code}")
        }
    }
}
