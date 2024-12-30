package com.example.ble_audiospasialdariesp32sonaraudio.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun UIPosisiObjek(
    xPos:Float,
    yPos:Float,
    radius:Float,
    modifier: Modifier
){
    // State untuk posisi handle (di lingkaran)
    var handlePosition by remember { mutableStateOf(Offset.Zero) }
    val radius = 500f  // Radius lingkaran



    // Listen for changes to the handle position from outside
    LaunchedEffect(xPos, yPos) {
        handlePosition = Offset(xPos, yPos)
    }


    // Menggambar lingkaran dan handle yang bisa ditarik
    Canvas(modifier = modifier
        .size(300.dp)

    ) {
        // Gambar lingkaran utama
        drawCircle(
            color = Color.Gray,
            radius = radius,
            style = Stroke(width = 4.dp.toPx())
        )

        // Gambar handler posisi objek
        drawCircle(
            color = Color.Red,
            radius = 15.dp.toPx(),
            center = Offset(
                x = center.x + handlePosition.x,
                y = center.y - handlePosition.y
            )
        )

    }
}