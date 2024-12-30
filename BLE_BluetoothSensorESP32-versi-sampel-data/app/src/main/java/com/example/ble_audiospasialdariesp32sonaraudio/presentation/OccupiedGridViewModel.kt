package com.example.ble_audiospasialdariesp32sonaraudio.presentation

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlin.math.cos
import kotlin.math.sin

class OccupiedGridViewModel(
    context: Context,
) : ViewModel() {

    private val radius = 500
    private val gridSize = 10 // Fixed grid size (10x10x10)
    private var occupiedGrid: MutableState<Array<Array<Array<Boolean>>>> =
        mutableStateOf(Array(gridSize) { Array(gridSize) { Array(gridSize) { false } } })


    fun tagOccupiedGrid(
        distance: Float,
        pitch: Int,
        yaw: Int
    ) {
        if (distance > radius) {
            // Ignore out-of-bound radius
            return
        }

        // Convert from spherical (distance, pitch, yaw) to Cartesian
        val (x, y, z) = sphericalToCartesian(distance, pitch, yaw)

        // Map Cartesian to grid indices
        val indices = mapToGridIndices(x, y, z)
        if (indices != null) {
            val (i, j, k) = indices
            occupiedGrid.value[i][j][k] = true
        }
    }

    /**
     * Resets the grid to its initial state (all cells unoccupied).
     */
    fun resetGrid() {
        occupiedGrid.value = Array(gridSize) { Array(gridSize) { Array(gridSize) { false } } }
    }


    private fun sphericalToCartesian(distance: Float, pitch: Int, yaw: Int): Triple<Double, Double, Double> {
        // Convert pitch and yaw from degrees to radians
        val pitchRad = pitch * (Math.PI / 180.0)
        val yawRad = yaw * (Math.PI / 180.0)

        val x = distance * cos(pitchRad) * cos(yawRad)
        val y = distance * cos(pitchRad) * sin(yawRad)
        val z = distance * sin(pitchRad)

        return Triple(x, y, z)
    }

    /**
     * Converts Cartesian coordinates to grid indices.
     */
    private fun mapToGridIndices(x: Double, y: Double, z: Double): Triple<Int, Int, Int>? {
        // Normalize coordinates to fit within gridSize
        val normalizedX = (x / radius) * (gridSize / 2.0)
        val normalizedY = (y / radius) * (gridSize / 2.0)
        val normalizedZ = (z / radius) * (gridSize / 2.0)

        val i = ((normalizedX + gridSize / 2.0).toInt()).coerceIn(0, gridSize - 1)
        val j = ((normalizedY + gridSize / 2.0).toInt()).coerceIn(0, gridSize - 1)
        val k = ((normalizedZ + gridSize / 2.0).toInt()).coerceIn(0, gridSize - 1)

        return Triple(i, j, k)
    }

    /**
     * Getter for the current state of the occupied grid.
     */
    fun getOccupiedGrid(): Array<Array<Array<Boolean>>> {
        return occupiedGrid.value
    }
}
