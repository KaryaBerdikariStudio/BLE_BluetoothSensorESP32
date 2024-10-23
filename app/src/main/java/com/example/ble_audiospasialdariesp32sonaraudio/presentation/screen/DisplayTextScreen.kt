package com.example.ble_audiospasialdariesp32sonaraudio.presentation.screen

import android.bluetooth.BluetoothAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun DisplayTextScreen(
    onBluetoothStateChanged: ()-> Unit,
    bluetoothAdapter: BluetoothAdapter,
    jarak: Float,
    orientasi: FloatArray
){
    val context = LocalContext.current


}