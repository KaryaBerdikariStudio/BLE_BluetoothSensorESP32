package com.example.ble_audiospasialdariesp32sonaraudio.domain.model

import androidx.compose.runtime.MutableState

data class TTSData(
    val isTTSEnabled: Boolean = true,
    val text:String = ""
)
