package com.example.ble_audiospasialdariesp32sonaraudio.presentation.screen

import android.bluetooth.BluetoothAdapter
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.ble_audiospasialdariesp32sonaraudio.datahandler.audiospasial.AudioSpasialManager
import com.example.ble_audiospasialdariesp32sonaraudio.domain.repo.ConnectionState
import com.example.ble_audiospasialdariesp32sonaraudio.domain.repo.DataHandler
import com.example.ble_audiospasialdariesp32sonaraudio.permissions.SystemBroadcastReceiver
import com.example.ble_audiospasialdariesp32sonaraudio.presentation.BluetoothLEViewModel
import com.example.ble_audiospasialdariesp32sonaraudio.presentation.OccupiedGridViewModel
import com.example.ble_audiospasialdariesp32sonaraudio.presentation.components.GridUI

@Composable
fun PosisiSampelDataScreen(
    bluetoothLEViewModel: BluetoothLEViewModel = hiltViewModel(),
    audioSpasialManager: AudioSpasialManager,
    gridViewModel: OccupiedGridViewModel,
    onBluetoothStateChanged: () -> Unit
) {
    val context = LocalContext.current

    // Receiver for Bluetooth state changes
    SystemBroadcastReceiver(systemAction = BluetoothAdapter.ACTION_STATE_CHANGED) { bluetoothState ->
        val action = bluetoothState?.action ?: return@SystemBroadcastReceiver
        Log.d("DisplayTextScreen", "Bluetooth State Changed: $action")
        if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            onBluetoothStateChanged()
        }
    }


    val jarak by bluetoothLEViewModel.jarak.collectAsState()
    val timestamp by bluetoothLEViewModel.timestamp.collectAsState()
    val kecepatanAngular by bluetoothLEViewModel.kecepatanAngular.collectAsState()
    val kecepatanAkselerasi by bluetoothLEViewModel.kecepatanAkselerasi.collectAsState()
    val connectionState by bluetoothLEViewModel.connectionState.collectAsState()

    val listDataSudut = DataHandler().RollPitchYaw(context, timestamp, kecepatanAngular, kecepatanAkselerasi)
    val listDataAkselerasi = DataHandler().PosisiXYZ(context, timestamp, kecepatanAkselerasi)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            when (connectionState) {
                ConnectionState.Connected -> {
                    var posisiX by remember { mutableStateOf(listDataAkselerasi.positionX.toString()) }
                    var posisiY by remember { mutableStateOf(listDataAkselerasi.positionY.toString()) }
                    var posisiZ by remember { mutableStateOf(listDataAkselerasi.positionZ.toString()) }

                    var roll by remember { mutableStateOf(listDataSudut.roll) }
                    var pitch by remember { mutableStateOf(listDataSudut.pitch) }
                    var yaw by remember { mutableStateOf(listDataSudut.yaw) }

                    jarak?.let { gridViewModel.tagOccupiedGrid(it, pitch, yaw) }

                    GridUI(
                        gridViewModel
                    )

                    // Display Akselerasi (Position)
                    Text(text = "Akselerasi : ")
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(text = "X : $posisiX")
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Y : $posisiY")
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Z : $posisiZ")

                    Spacer(modifier = Modifier.width(40.dp))

                    // Display Sudut (Roll, Pitch, Yaw)
                    Text(text = "Sudut : ")
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(text = "Roll : $roll")
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Pitch : $pitch")
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Yaw : $yaw")

                    Spacer(modifier = Modifier.width(40.dp))

                    // Display Jarak
                    Text(text = "Jarak : ${jarak.toString()}")

                    Spacer(modifier = Modifier.width(40.dp))

                    // Display Kecepatan Angular (Gyro)
                    Text(text = "Kecepatan Angular (Gyro) : ")
                    Spacer(modifier = Modifier.width(20.dp))

                    // Loop through and display the elements of the kecepatanAngular array
                    kecepatanAngular!!.forEachIndexed { index, value ->
                        Text(text = "Gyro $index: $value")
                    }// Loop through and display the elements of the kecepatanAngular array\
                    Spacer(modifier = Modifier.width(40.dp))
                    kecepatanAkselerasi!!.forEachIndexed { index, value ->
                        Text(text = "Aksel $index: $value")
                    }


                }
                ConnectionState.Uninitialized -> {
                    Text(text = "Koneksi belum diinisialisasi")
                    bluetoothLEViewModel.startConnection()
                }
                else -> {
                    Text(text = "Menghubungkan...")
                }
            }
        }
    }

    // Close BLE connection when this screen is removed
    DisposableEffect(Unit) {
        onDispose {
            bluetoothLEViewModel.closeConnection()
        }
    }
}

