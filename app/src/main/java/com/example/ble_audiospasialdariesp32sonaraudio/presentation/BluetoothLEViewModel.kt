package com.example.ble_audiospasialdariesp32sonaraudio.presentation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ble_audiospasialdariesp32sonaraudio.datahandler.ble.ESP32DataReceiveManager
import com.example.ble_audiospasialdariesp32sonaraudio.domain.repo.ConnectionState
import com.example.ble_audiospasialdariesp32sonaraudio.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("MissingPermission")
@HiltViewModel
class BluetoothLEViewModel @Inject constructor(
    private val eSP32DataReceiveManager: ESP32DataReceiveManager
):ViewModel() {

    var initializingMessage by mutableStateOf<String?>(value = "Memulai Aplikasi")
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private  set

    var jarak by mutableStateOf(0f)
        private set

    var orientasi by mutableStateOf(floatArrayOf(0f, 0f))
        private set

    var connectionState by mutableStateOf<ConnectionState>(ConnectionState.Uninitialized)


    private var hasSubscribed = false

    private fun subscribedToChanges(){
        Log.d("subskreb", "Result subskreb")
        viewModelScope.launch{
            eSP32DataReceiveManager.dataFlow.collect{ result ->
                when(result){
                    is Resource.Success ->{
                        connectionState = result.data.connectionState
                        jarak = result.data.jarak
                        orientasi = result.data.orientasi
                    }
                    is Resource.Loading ->{
                        initializingMessage = result.message
                        connectionState = ConnectionState.Connecting
                    }
                    is Resource.Error ->{
                        errorMessage = result.errorMessage
                        connectionState = ConnectionState.Uninitialized
                    }
                }
            }
        }
    }


    fun startConnection() {
        eSP32DataReceiveManager.startConnection()
        subscribedToChanges()
    }

    fun reconnect(){
        eSP32DataReceiveManager.reconnect()
    }

    fun disconnect() {
        eSP32DataReceiveManager.disconnect()
    }

    override fun onCleared(){
        super.onCleared()
        eSP32DataReceiveManager.closeConnection()
    }
}