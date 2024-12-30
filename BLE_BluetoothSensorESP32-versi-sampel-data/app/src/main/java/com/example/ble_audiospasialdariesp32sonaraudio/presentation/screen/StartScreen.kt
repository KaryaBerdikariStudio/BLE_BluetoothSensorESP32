package com.example.ble_audiospasialdariesp32sonaraudio.presentation.screen

import android.bluetooth.BluetoothAdapter
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.ble_audiospasialdariesp32sonaraudio.domain.repo.ConnectionState
import com.example.ble_audiospasialdariesp32sonaraudio.permissions.PermissionUtils
import com.example.ble_audiospasialdariesp32sonaraudio.presentation.BluetoothLEViewModel
import com.example.ble_audiospasialdariesp32sonaraudio.presentation.Screen
import com.example.ble_audiospasialdariesp32sonaraudio.presentation.components.ButtonNavigasi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun StartScreen(
    navController: NavController,
    onBluetoothStateChanged:()->Unit,
    bluetoothAdapter: BluetoothAdapter,
    bluetoothLEViewModel: BluetoothLEViewModel = hiltViewModel()
) {

    //Log.d("koneksi", "koneksinya : ${bluetoothLEViewModel.connectionState}")

    val connectionState by bluetoothLEViewModel.connectionState.collectAsState()

    if(connectionState == ConnectionState.Connected){
        bluetoothLEViewModel.closeConnection()
    }

    //PermissionHandler
    val permissionsState = rememberMultiplePermissionsState(permissions = PermissionUtils.permissions)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            ButtonNavigasi(
                navController = navController,
                onBluetoothStateChanged = onBluetoothStateChanged,
                permissionState = permissionsState,
                route = Screen.ScanAndPairedDeviceScreen.route,
                aksi = "Mulai",
                bluetoothAdapter = bluetoothAdapter
            )
            Spacer(modifier = Modifier.height(30.dp))

            Row {

                ButtonNavigasi(
                    navController = navController,
                    onBluetoothStateChanged = onBluetoothStateChanged,
                    permissionState = permissionsState,
                    route = Screen.SpatialAudioScreen.route,
                    aksi = "Audio Spasial Test",
                    bluetoothAdapter = bluetoothAdapter
                )
                Spacer(modifier = Modifier.width(20.dp))
                ButtonNavigasi(
                    navController = navController,
                    onBluetoothStateChanged = onBluetoothStateChanged,
                    permissionState = permissionsState,
                    route = Screen.PosisiSampelDataScreen.route,
                    aksi = "Sampel Data",
                    bluetoothAdapter = bluetoothAdapter
                )
                Spacer(modifier = Modifier.width(20.dp))
                ButtonNavigasi(
                    navController = navController,
                    onBluetoothStateChanged = onBluetoothStateChanged,
                    permissionState = permissionsState,
                    route = Screen.ConfigScreen.route,
                    aksi = "Konfigurasi",
                    bluetoothAdapter = bluetoothAdapter
                )

            }
        }
    }

}