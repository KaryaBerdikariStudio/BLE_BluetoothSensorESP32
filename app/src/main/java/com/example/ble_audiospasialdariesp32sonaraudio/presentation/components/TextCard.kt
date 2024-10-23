package com.example.ble_audiospasialdariesp32sonaraudio.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TextCard(
    jarak: Float,
    orientasi: FloatArray
){
    Box(
        modifier = Modifier
            .size(width = 200.dp, height = 300.dp)
            .background(color = Color.LightGray)
    ){
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Jarak = $jarak cm")

            Spacer(Modifier.size(16.dp))
            Text(
                text = "Orientasi   : \n    Vertical: ${orientasi[0]} derajat,\n    Horizontal: ${orientasi[1]}  derajat"
            )
        }

    }
}