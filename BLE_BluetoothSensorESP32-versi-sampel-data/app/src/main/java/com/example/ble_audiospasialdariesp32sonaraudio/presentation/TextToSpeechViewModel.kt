package com.example.ble_audiospasialdariesp32sonaraudio.presentation

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ble_audiospasialdariesp32sonaraudio.domain.model.TTSData
import kotlinx.coroutines.launch
import java.util.Locale

class TextToSpeechViewModel(
    context: Context
):ViewModel() {
    private var tts: TextToSpeech? = null
    var data by mutableStateOf(TTSData())
        private set

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
            }
        }
    }

    fun updateText(newText: String) {
        data = data.copy(text = newText)
    }

    fun toggleTTS(isEnabled: Boolean) {
        data = data.copy(isTTSEnabled = isEnabled)
    }

    fun speakText() {
        if (data.isTTSEnabled && data.text.isNotEmpty()) {
            viewModelScope.launch {
                tts?.speak(data.text, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.shutdown()
    }
}