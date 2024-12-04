package com.example.ble_audiospasialdariesp32sonaraudio.presentation

import android.bluetooth.BluetoothAdapter
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ble_audiospasialdariesp32sonaraudio.datahandler.audiospasial.AudioSpasialManager
import com.example.ble_audiospasialdariesp32sonaraudio.presentation.screen.DisplayTextScreen
import com.example.ble_audiospasialdariesp32sonaraudio.presentation.screen.PosisiSampelDataScreen
import com.example.ble_audiospasialdariesp32sonaraudio.presentation.screen.StartScreen

@Composable
fun Navigation(
    onBluetoothStateChanged: () -> Unit,
    bluetoothAdapter: BluetoothAdapter,
    context: Context
){
    val navController = rememberNavController()
    val bluetoothLEViewModel = hiltViewModel<BluetoothLEViewModel>()
    val audioSpasialManager = AudioSpasialManager(context)

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
                bluetoothLEViewModel = bluetoothLEViewModel
            )
        }

        composable(Screen.DisplayTextScreen.route){
            DisplayTextScreen(
                onBluetoothStateChanged = onBluetoothStateChanged,
                bluetoothLEViewModel = bluetoothLEViewModel,
                navController = navController
            )
        }

        composable(Screen.PosisiSampelDataScreen.route){
            PosisiSampelDataScreen(
                bluetoothLEViewModel,
                audioSpasialManager,
                onBluetoothStateChanged
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
    object PosisiSampelDataScreen:Screen("posisi_sampel_data_screen")
}