package com.edgereader.tts.network

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.UUID

sealed class TtsEvent {
    data class AudioChunk(val data: ByteArray) : TtsEvent()
    data class Metadata(val json: String) : TtsEvent()
    data class Error(val message: String, val is403: Boolean = false) : TtsEvent()
    object Done : TtsEvent()
}

class EdgeTtsClient(private val client: OkHttpClient) {

    // Ideally, this should be fetched remotely (Firebase/RemoteConfig) as it changes.
    private var secMsGecVersion = "1-130.0.2849.68"
    private val endpoint = "wss://speech.platform.bing.com/consumer/speech/synthesize/readaloud/edge/v1"

    fun updateGecVersion(version: String) {
        secMsGecVersion = version
    }

    /**
     * Synthesizes text into a stream of audio chunks and metadata.
     * @param clockSkewMs for adjusting token time if 403 occurs.
     */
    fun synthesize(
        text: String,
        voiceName: String,
        rate: String = "+0%",
        pitch: String = "+0Hz",
        clockSkewMs: Long = 0L
    ): Flow<TtsEvent> = callbackFlow {
        val connectionId = UUID.randomUUID().toString().replace("-", "")
        val token = SecMsGecTokenGenerator.generateToken(clockSkewMs)
        val url = "$endpoint?TrustedClientToken=6A5AA1D4EAFF4E9FB37E23D68491D6F4" +
                "&Sec-MS-GEC=$token" +
                "&Sec-MS-GEC-Version=$secMsGecVersion" +
                "&ConnectionId=$connectionId"

        val request = Request.Builder()
            .url(url)
            .addHeader("Origin", "chrome-extension://jdiccldimpdaibmpdkjnbmckianbfold")
            .build()

        val webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                sendConfig(webSocket)
                sendSsml(webSocket, text, voiceName, rate, pitch)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                if (text.contains("Path:turn.end")) {
                    trySend(TtsEvent.Done)
                    webSocket.close(1000, "Done")
                } else if (text.contains("Path:audio.metadata")) {
                    // Extract payload logic goes here
                    trySend(TtsEvent.Metadata(text))
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                super.onMessage(webSocket, bytes)
                // Binary payload is the audio. 
                // It has a 2-byte header containing the length of the string metadata,
                // followed by the string metadata (Path:audio), followed by the MP3 bytes.
                val byteArray = bytes.toByteArray()
                if (byteArray.size > 2) {
                    val headerLength = ((byteArray[0].toInt() and 0xFF) shl 8) or (byteArray[1].toInt() and 0xFF)
                    val offset = 2 + headerLength
                    if (offset < byteArray.size) {
                        val audioData = byteArray.copyOfRange(offset, byteArray.size)
                        trySend(TtsEvent.AudioChunk(audioData))
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                val is403 = response?.code == 403
                trySend(TtsEvent.Error(t.message ?: "WebSocket Error", is403))
                close(t)
            }
        })

        awaitClose { webSocket.cancel() }
    }

    private fun sendConfig(webSocket: WebSocket) {
        val timestamp = System.currentTimeMillis()
        val msg = "X-Timestamp:$timestamp\r\n" +
                "Content-Type:application/json; charset=utf-8\r\n" +
                "Path:speech.config\r\n\r\n" +
                """{"context":{"synthesis":{"audio":{"metadataoptions":{"sentenceBoundaryEnabled":"false","wordBoundaryEnabled":"true"},"outputFormat":"audio-24khz-48kbitrate-mono-mp3"}}}}"""
        webSocket.send(msg)
    }

    private fun sendSsml(
        webSocket: WebSocket,
        text: String,
        voiceName: String,
        rate: String,
        pitch: String
    ) {
        val requestId = UUID.randomUUID().toString().replace("-", "")
        val timestamp = System.currentTimeMillis()
        
        // Very basic escaping for XML
        val escapedText = text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("'", "&apos;")
            .replace("\"", "&quot;")

        val ssml = "<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='en-US'>" +
                "<voice name='$voiceName'>" +
                "<prosody rate='$rate' pitch='$pitch'>$escapedText</prosody>" +
                "</voice></speak>"

        val msg = "X-RequestId:$requestId\r\n" +
                "Content-Type:application/ssml+xml\r\n" +
                "X-Timestamp:$timestamp\r\n" +
                "Path:ssml\r\n\r\n" +
                ssml

        webSocket.send(msg)
    }
}
