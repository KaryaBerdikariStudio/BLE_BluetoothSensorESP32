package com.example.ble_audiospasialdariesp32sonaraudio.domain.model

import com.example.ble_audiospasialdariesp32sonaraudio.domain.repo.ConnectionState

data class ESP32DataResult(
    val jarak:Float,
    val orientasi:FloatArray,
    val kecepatanPutaran:FloatArray,
    val kecepatanTranslasi:FloatArray,
    val connectionState: ConnectionState
)

