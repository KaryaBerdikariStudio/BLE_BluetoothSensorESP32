package com.example.ble_audiospasialdariesp32sonaraudio.presentation.components

import android.bluetooth.BluetoothAdapter
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ButtonNavigasi(
    navController: NavController,
    onBluetoothStateChanged: () -> Unit,
    permissionState:MultiplePermissionsState,
    route:String,
    aksi:String,
    bluetoothAdapter: BluetoothAdapter
){
    Box(
        modifier = Modifier
            .size(150.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color.Blue, CircleShape)
            .clickable {
                if (permissionState.allPermissionsGranted){
                    if (bluetoothAdapter.isEnabled){
                        navController.navigate(route)
                        Log.d("Navigasi", "$route")
                    }else{
                        onBluetoothStateChanged()
                    }
                }else{
                    permissionState.launchMultiplePermissionRequest()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = aksi,
            fontSize = 35.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }

}