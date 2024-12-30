package com.example.ble_audiospasialdariesp32sonaraudio.domain.repo

import com.example.ble_audiospasialdariesp32sonaraudio.domain.model.ESP32DataResult
import com.example.ble_audiospasialdariesp32sonaraudio.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow

//
interface ESP32DataReceiveManager {

    val dataFlow:MutableSharedFlow<Resource<ESP32DataResult>>

    fun reconnect()

    fun disconnect()

    fun startReceiving()

    fun startConnection()

    fun closeConnection()

}