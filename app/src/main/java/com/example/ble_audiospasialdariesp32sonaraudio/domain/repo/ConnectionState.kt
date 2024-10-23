package com.example.ble_audiospasialdariesp32sonaraudio.domain.repo

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.flow.MutableStateFlow

//Data koneksi lagi ngapain
sealed interface ConnectionState {
    object Connected: ConnectionState
    object Disconnected: ConnectionState
    object Uninitialized: ConnectionState
    object Connecting: ConnectionState

}