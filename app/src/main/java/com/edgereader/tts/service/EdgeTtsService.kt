package com.edgereader.tts.service

import android.speech.tts.SynthesisCallback
import android.speech.tts.SynthesisRequest
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeechService
import android.util.Log
import com.edgereader.tts.network.EdgeTtsClient
import com.edgereader.tts.network.TtsEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient

class EdgeTtsService : TextToSpeechService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private lateinit var ttsClient: EdgeTtsClient
    private var currentSynthesisJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        // In a real app, this should be injected via Hilt or dependency injection
        ttsClient = EdgeTtsClient(OkHttpClient())
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onIsLanguageAvailable(lang: String?, country: String?, variant: String?): Int {
        // Here we can check against supported Edge TTS languages.
        // For now, we support standard languages to make it appear in settings.
        return TextToSpeech.LANG_AVAILABLE
    }

    override fun onGetLanguage(): Array<String> {
        return arrayOf("uz", "UZ", "")
    }

    override fun onLoadLanguage(lang: String?, country: String?, variant: String?): Int {
        return onIsLanguageAvailable(lang, country, variant)
    }

    override fun onStop() {
        // Called when TextToSpeech.stop() is invoked, for example during fast forward
        // in Envision AI or screen reader interruption.
        currentSynthesisJob?.cancel()
    }

    override fun onSynthesizeText(request: SynthesisRequest?, callback: SynthesisCallback?) {
        if (request == null || callback == null) return

        val text = request.charSequenceText?.toString()
        if (text.isNullOrBlank()) {
            callback.done()
            return
        }

        // Determine language and voice from SharedPreferences
        val sharedPrefs = getSharedPreferences("tts_settings", MODE_PRIVATE)
        val voiceName = sharedPrefs.getString("default_voice", "uz-UZ-MadinaNeural") ?: "uz-UZ-MadinaNeural"

        callback.start(24000, android.media.AudioFormat.ENCODING_PCM_16BIT, 1)

        currentSynthesisJob = serviceScope.launch {
            ttsClient.synthesize(text, voiceName)
                .catch { e ->
                    Log.e("EdgeTtsService", "Synthesis error", e)
                    callback.error()
                }
                .collect { event ->
                    when (event) {
                        is TtsEvent.AudioChunk -> {
                            val data = event.data
                            // Send bytes to Android TTS system
                            val result = callback.audioAvailable(data, 0, data.size)
                            if (result == TextToSpeech.ERROR) {
                                // Buffer full or error
                                Log.e("EdgeTtsService", "Audio available callback returned error")
                            }
                        }
                        is TtsEvent.Done -> {
                            callback.done()
                        }
                        is TtsEvent.Error -> {
                            Log.e("EdgeTtsService", "Edge TTS error: ${event.message}")
                            callback.error()
                        }
                        is TtsEvent.Metadata -> {
                            // Can be used to report word boundaries if TextToSpeech supports it
                        }
                    }
                }
        }
        
        // Wait for job to finish since TextToSpeechService expects synchronous completion of this method
        // or we use runBlocking in extreme cases, but since we are handling callbacks, 
        // returning from onSynthesizeText while feeding audio asynchronously is standard.
        // Actually, Android TTS expects onSynthesizeText to block until all audio is written,
        // so we must block here.
        runBlocking {
            currentSynthesisJob?.join()
        }
    }
}
