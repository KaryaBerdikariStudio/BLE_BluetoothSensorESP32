package com.example.ble_audiospasialdariesp32sonaraudio.permissions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

@Composable
fun SystemBroadcastReceiver(
    systemAction: String,
    onSystemEvent: (intent: Intent?) -> Unit
) {
    val context = LocalContext.current

    // Memastikan fungsi callback terbaru digunakan
    val currentOnSystemEvent by rememberUpdatedState(onSystemEvent)

    DisposableEffect(context, systemAction) {
        // Membuat filter untuk menerima action yang ditentukan
        val intentFilter = IntentFilter(systemAction)

        // Membuat BroadcastReceiver
        val broadcast = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                currentOnSystemEvent(intent) // Panggil fungsi callback dengan intent
            }
        }

        // Mendaftarkan receiver
        context.registerReceiver(broadcast, intentFilter)

        // Menghapus receiver saat composable tidak lagi digunakan
        onDispose {
            try {
                context.unregisterReceiver(broadcast)
            } catch (e: IllegalArgumentException) {
                // Receiver mungkin tidak terdaftar, bisa abaikan atau log kesalahan
            }
        }
    }
}
