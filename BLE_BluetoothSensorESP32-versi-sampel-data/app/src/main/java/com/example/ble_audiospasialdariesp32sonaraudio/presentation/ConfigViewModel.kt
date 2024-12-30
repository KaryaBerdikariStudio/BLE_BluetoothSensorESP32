package com.example.ble_audiospasialdariesp32sonaraudio.presentation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.ble_audiospasialdariesp32sonaraudio.domain.model.ConfigData

class ConfigViewModel : ViewModel() {

    // State for ConfigData
    private val _configData: MutableState<ConfigData> =
        mutableStateOf(ConfigData(minJarak = 0f, maxJarak = 500f))
    val configData: ConfigData
        get() = _configData.value

    // Update minJarak
    fun updateMinJarak(minJarak: Float) {
        _configData.value = _configData.value.copy(minJarak = minJarak)
    }

    // Update maxJarak
    fun updateMaxJarak(maxJarak: Float) {
        _configData.value = _configData.value.copy(maxJarak = maxJarak)
    }
}