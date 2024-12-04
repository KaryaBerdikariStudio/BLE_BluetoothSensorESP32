package com.example.ble_audiospasialdariesp32sonaraudio.presentation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ble_audiospasialdariesp32sonaraudio.datahandler.ble.ESP32DataReceiveManager
import com.example.ble_audiospasialdariesp32sonaraudio.domain.repo.ConnectionState
import com.example.ble_audiospasialdariesp32sonaraudio.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("MissingPermission")
@HiltViewModel
class BluetoothLEViewModel @Inject constructor(
    private val eSP32DataReceiveManager: ESP32DataReceiveManager
) : ViewModel() {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Uninitialized)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _initializingMessage = MutableStateFlow<String?>("Memulai Aplikasi")
    val initializingMessage: StateFlow<String?> = _initializingMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _jarak = MutableStateFlow<Float?>(null) // Change back to MutableStateFlow for single latest value
    val jarak: StateFlow<Float?> = _jarak.asStateFlow() // Expose it as StateFlow for the UI

    private val  _timestamp = MutableStateFlow<Long?>(null)
    val timestamp: StateFlow<Long?> = _timestamp.asStateFlow()

    private val _kecepatanAngular = MutableStateFlow<DoubleArray?>(null)
    val kecepatanAngular = _kecepatanAngular.asStateFlow()

    private val _kecepatanAkselerasi = MutableStateFlow<DoubleArray?>(null)
    val kecepatanAkselerasi = _kecepatanAkselerasi.asStateFlow()


    init {
        subscribeToChanges() // Subscribe immediately on ViewModel creation
    }

    private var hasSubscribed = false

    private fun subscribeToChanges() {
        if (!hasSubscribed) {
            hasSubscribed = true
            viewModelScope.launch {
                eSP32DataReceiveManager.dataFlow.collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _connectionState.value = result.data.connectionState
                            _jarak.value = result.data.jarak // Update the StateFlow with new jarak value
                            _timestamp.value = result.data.timestamp
                            _kecepatanAngular.value = result.data.kecepatanPutaran
                            _kecepatanAkselerasi.value = result.data.kecepatanTranslasi

                            Log.d("BluetoothLEViewModel", "Updated Connection in ViewModel: ${_connectionState.value}")
                            Log.d("BluetoothLEViewModel", "Updated Jarak in ViewModel: ${jarak.value}")
                            Log.d("BluetoothLEViewModel", "Updated Gyro in ViewModel: ${_kecepatanAngular.value}")
                            Log.d("BluetoothLEViewModel", "Updated Gyro in ViewModel: ${_kecepatanAkselerasi.value}")

                        }
                        is Resource.Loading -> {
                            _initializingMessage.value = result.message
                            _connectionState.value = ConnectionState.Connecting
                        }
                        is Resource.Error -> {
                            _errorMessage.value = result.errorMessage
                            _connectionState.value = ConnectionState.Uninitialized
                        }
                    }
                }
            }
        }
    }

    fun startConnection() {
        eSP32DataReceiveManager.startConnection()
        subscribeToChanges() // Subscribe to changes on start connection
    }

    fun reconnect() {
        eSP32DataReceiveManager.reconnect()
    }

    fun disconnect() {
        eSP32DataReceiveManager.disconnect()
    }

    fun closeConnection() {
        eSP32DataReceiveManager.closeConnection()
    }

    override fun onCleared() {
        super.onCleared()
        eSP32DataReceiveManager.closeConnection()
    }
}

