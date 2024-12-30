package com.example.ble_audiospasialdariesp32sonaraudio.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ble_audiospasialdariesp32sonaraudio.presentation.ConfigViewModel

@Composable
fun ConfigScreen(viewModel: ConfigViewModel) {
    // Access current config data
    val configData = viewModel.configData

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Configuration")

        Spacer(modifier = Modifier.height(16.dp))

        // Slider for minJarak
        Column {
            Text("Minimum Distance: ${configData.minJarak} cm")
            Slider(
                value = configData.minJarak,
                onValueChange = { viewModel.updateMinJarak(it) },
                valueRange = 0f..configData.maxJarak,
                steps = 10,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Slider for maxJarak
        Column {
            Text("Maximum Distance: ${configData.maxJarak} cm")
            Slider(
                value = configData.maxJarak,
                onValueChange = { viewModel.updateMaxJarak(it) },
                valueRange = configData.minJarak..configData.maxJarak,
                steps = 10,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display current config values
        Text(
            text = "Current Config:\nMin Distance: ${configData.minJarak} cm\nMax Distance: ${configData.maxJarak} cm"
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Button to Reset Config
        Button(
            onClick = {
                viewModel.updateMinJarak(0f)
                viewModel.updateMaxJarak(500f)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset to Default")
        }
    }
}
