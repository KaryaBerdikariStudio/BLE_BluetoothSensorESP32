package com.example.ble_audiospasialdariesp32sonaraudio.presentation

import android.bluetooth.BluetoothAdapter
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ble_audiospasialdariesp32sonaraudio.presentation.screen.StartScreen

@Composable
fun Navigation(
    onBluetoothStateChanged: () -> Unit,
    bluetoothAdapter: BluetoothAdapter,
    bluetoothLEViewModel: BluetoothLEViewModel = hiltViewModel()
){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.StartScreen.route
    ){
        composable(Screen.StartScreen.route){
            StartScreen(
                navController,
                onBluetoothStateChanged,
                bluetoothAdapter
            )
        }
        composable(Screen.ScanAndPairedDeviceScreen.route){
            ScanAndPairedDeviceScreen(
                onBluetoothStateChanged = onBluetoothStateChanged,
                navController = navController,

            )
        }


    }
}

sealed class Screen (val route:String){
    object StartScreen:Screen("start_screen")
    object ScanAndPairedDeviceScreen:Screen ("scan_and_paired_device_screen")
    object DisplayTextScreen:Screen("display_text_screen")
    object SpatialAudioScreen:Screen("spatial_audio_screen")
    object ConfigScreen:Screen("config_screen")
}