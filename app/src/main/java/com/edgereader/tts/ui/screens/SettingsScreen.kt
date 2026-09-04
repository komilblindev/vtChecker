package com.edgereader.tts.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("tts_settings", Context.MODE_PRIVATE) }
    
    var selectedVoice by remember { 
        mutableStateOf(sharedPrefs.getString("default_voice", "uz-UZ-MadinaNeural") ?: "uz-UZ-MadinaNeural") 
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TTS Sozlamalari") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Orqaga")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text(
                text = "Tizim (Envision AI / TalkBack) uchun standart ovozni tanlang:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val voices = listOf(
                "uz-UZ-MadinaNeural" to "O'zbekcha - Madina (Ayol)",
                "uz-UZ-SardorNeural" to "O'zbekcha - Sardor (Erkak)",
                "ru-RU-SvetlanaNeural" to "Ruscha - Svetlana (Ayol)",
                "ru-RU-DmitryNeural" to "Ruscha - Dmitry (Erkak)",
                "en-US-AriaNeural" to "Inglizcha - Aria (Ayol)"
            )
            
            voices.forEach { (voiceId, displayName) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (selectedVoice == voiceId),
                        onClick = {
                            selectedVoice = voiceId
                            sharedPrefs.edit().putString("default_voice", voiceId).apply()
                        }
                    )
                    Text(text = displayName, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
