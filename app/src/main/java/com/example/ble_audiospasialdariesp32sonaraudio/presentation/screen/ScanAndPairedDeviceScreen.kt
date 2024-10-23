package com.example.ble_audiospasialdariesp32sonaraudio.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.ble_audiospasialdariesp32sonaraudio.domain.repo.ConnectionState
import com.example.ble_audiospasialdariesp32sonaraudio.permissions.SystemBroadcastReceiver

@SuppressLint("MissingPermission")
@Composable
fun ScanAndPairedDeviceScreen(
    onBluetoothStateChanged: () -> Unit,
    navController: NavController,
    bluetoothLEViewModel: BluetoothLEViewModel = hiltViewModel()
) {

    //Hilangkan Status dan Navigation Bar
    val view = LocalView.current
    val window = (view.context as Activity).window
    val insetsController = WindowCompat.getInsetsController(window, view)

    // Mengumpulkan state dari ViewModel
    val connectionState by remember { mutableStateOf(bluetoothLEViewModel.connectionState) }
    val initializingMessage by remember { mutableStateOf(bluetoothLEViewModel.initializingMessage) }

    insetsController.apply {
        hide(WindowInsetsCompat.Type.systemBars())
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }




    Log.d("ScanAndPairedDeviceScreen", "Masuk Halaman Scan And Pair")

    SystemBroadcastReceiver(systemAction = BluetoothAdapter.ACTION_STATE_CHANGED) { bluetoothState ->
        val action = bluetoothState?.action ?: return@SystemBroadcastReceiver
        Log.d("ScanAndPairedScree", "Bluetooth State Changed: $action")
        if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            onBluetoothStateChanged()
        }
    }

    LaunchedEffect(Unit) {
        bluetoothLEViewModel.startConnection()
    }

    // LaunchedEffect untuk memantau perubahan connectionState
    LaunchedEffect(connectionState) {
        Log.d("Status Koneksi", "Koneksi = $connectionState")


        when (connectionState) {
            ConnectionState.Connected -> {
                // Pindah ke layar lain saat koneksi berhasil
                navController.navigate(Screen.DisplayTextScreen.route) {
                    // Pop up to the start destination, inclusive
                    popUpTo(Screen.ScanAndPairedDeviceScreen.route) { inclusive = true }
                }
            }
            ConnectionState.Uninitialized -> {
                // Tampilkan progress indicator saat masih menginisialisasi
                bluetoothLEViewModel.startConnection()
            }
            else -> {
                // Koneksi lainnya (Connecting/Disconnected), Anda bisa menambahkan logika tambahan jika diperlukan
               Log.d("test","Aloha")
            }
        }
    }

    // Menampilkan CircularProgressIndicator
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()

        Spacer(Modifier.size(16.dp))

        initializingMessage?.let { Text(text = it) }
    }

    DisposableEffect(

    ) { }


    //Tutup koneksi kalau masuk navigasi
    //memory leak

}
