package com.edgereader.tts.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBookScreen(
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var textContent by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yangi Kitob Yaratish") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Orqaga")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Kitob nomi") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = textContent,
                onValueChange = { textContent = it },
                label = { Text("Matnni kiriting yoki joylang") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Takes remaining space
                maxLines = 10
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { /* TODO: Trigger ViewModel to generate TTS */ },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && textContent.isNotBlank()
            ) {
                Text("Ovozga aylantirish (Generatsiya)")
            }
        }
    }
}
