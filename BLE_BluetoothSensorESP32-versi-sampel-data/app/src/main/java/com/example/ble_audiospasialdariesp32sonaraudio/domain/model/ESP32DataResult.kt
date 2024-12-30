package com.example.ble_audiospasialdariesp32sonaraudio.domain.model

import com.example.ble_audiospasialdariesp32sonaraudio.domain.repo.ConnectionState

data class ESP32DataResult(
    val jarak:Float,
    val kecepatanPutaran:DoubleArray,
    val kecepatanTranslasi:DoubleArray,
    val timestamp: Long,
    val connectionState: ConnectionState
)

