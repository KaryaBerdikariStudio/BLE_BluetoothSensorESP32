package com.example.ble_audiospasialdariesp32sonaraudio.presentation.screen

import android.bluetooth.BluetoothAdapter
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.ble_audiospasialdariesp32sonaraudio.domain.repo.ConnectionState
import com.example.ble_audiospasialdariesp32sonaraudio.permissions.SystemBroadcastReceiver
import com.example.ble_audiospasialdariesp32sonaraudio.presentation.BluetoothLEViewModel
import com.example.ble_audiospasialdariesp32sonaraudio.presentation.Screen
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun DisplayTextScreen(
    onBluetoothStateChanged: () -> Unit,
    bluetoothLEViewModel: BluetoothLEViewModel = hiltViewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val connectionState by bluetoothLEViewModel.connectionState.collectAsState()
    val jarak by bluetoothLEViewModel.jarak.collectAsState() // Observe the distance from ViewModel

    // Receiver for Bluetooth state changes
    SystemBroadcastReceiver(systemAction = BluetoothAdapter.ACTION_STATE_CHANGED) { bluetoothState ->
        val action = bluetoothState?.action ?: return@SystemBroadcastReceiver
        Log.d("DisplayTextScreen", "Bluetooth State Changed: $action")
        if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            onBluetoothStateChanged()
        }
    }

    // Show distance or a loading/error message
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (connectionState) {
            ConnectionState.Connected -> {
                if (jarak != null) {
                    Text(text = "Jarak: $jarak")
                } else {
                    Text(text = "Jarak tidak tersedia")
                }
            }
            ConnectionState.Uninitialized -> {
                Text(text = "Koneksi belum diinisialisasi")
            }
            else -> {
                Text(text = "Menghubungkan...")
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
