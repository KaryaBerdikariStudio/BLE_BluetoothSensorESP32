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
    SystemBroadcastReceiver(systemAction = BluetoothAdapter.ACTION_STATE_CHANGED) { bluetoothState ->
        val action = bluetoothState?.action ?: return@SystemBroadcastReceiver
        Log.d("ScanAndPairedScreen", "Bluetooth State Changed: $action")
        if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            onBluetoothStateChanged()
        }
    }

    // Hide Status and Navigation Bar
    val view = LocalView.current
    val window = (view.context as Activity).window
    val insetsController = WindowCompat.getInsetsController(window, view)

    insetsController.apply {
        hide(WindowInsetsCompat.Type.systemBars())
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    // Observe state from ViewModel
    val connectionState by bluetoothLEViewModel.connectionState.collectAsState()
    val initializingMessage by bluetoothLEViewModel.initializingMessage.collectAsState()

    LaunchedEffect(Unit) {
        bluetoothLEViewModel.startConnection()
    }

    // Navigate to DisplayTextScreen when connected
    navController.navigate(Screen.PosisiSampelDataScreen.route) {
        // Only pop if the previous screen is in the back stack
        launchSingleTop = true // Ensures only one instance of the destination exists
        // Use this if you want to avoid duplicate entries in the back stack
        popUpTo(Screen.ScanAndPairedDeviceScreen.route) {
            inclusive = true // This will pop the current screen as well
        }
    }

    // Show CircularProgressIndicator and initialization message
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.size(16.dp))
            initializingMessage?.let { Text(text = it) }
        }
    }
}

