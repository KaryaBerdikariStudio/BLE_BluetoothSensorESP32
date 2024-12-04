package com.example.ble_audiospasialdariesp32sonaraudio.datahandler.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import com.example.ble_audiospasialdariesp32sonaraudio.domain.repo.ESP32DataReceiveManager
import com.example.ble_audiospasialdariesp32sonaraudio.domain.model.ESP32DataResult
import com.example.ble_audiospasialdariesp32sonaraudio.domain.repo.ConnectionState
import com.example.ble_audiospasialdariesp32sonaraudio.util.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@SuppressLint("MissingPermission")
class ESP32DataReceiveManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter
) : ESP32DataReceiveManager {

    override val dataFlow: MutableSharedFlow<Resource<ESP32DataResult>> = MutableSharedFlow()
    @Volatile
    private var connectionState: ConnectionState = ConnectionState.Uninitialized

    private val DEVICE_NAME = "ESP32-BLE"
    private val SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
    private val CHARASTERISTICS_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"
    private val CLIENT_CHARACTERISTIC_CONFIG_UUID = "00002902-0000-1000-8000-00805f9b34fb"

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private var gatt: BluetoothGatt? = null
    private var isScanning = false
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if (result.device.name == DEVICE_NAME && isScanning) {
                isScanning = false
                bleScanner.stopScan(this)
                connectionState = ConnectionState.Connecting

                // Limit launching coroutines; handle connection process in one coroutine
                coroutineScope.launch {
                    dataFlow.emit(Resource.Loading(message = "Menghubungkan ke perangkat..."))
                    // Handle bonding and connection in the same coroutine
                    handleConnection(result.device)
                }
            }
        }
    }

    private suspend fun handleConnection(device: BluetoothDevice) {
        val isPaired = BluetoothAdapter.getDefaultAdapter().bondedDevices.any {
            it.address == device.address
        }

        if (!isPaired) {
            device.createBond() // Starting bonding process
            dataFlow.emit(Resource.Loading(message = "Memasang perangkat..."))
        }
        device.connectGatt(context, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE)
    }


    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("BluetoothGattCallback", "Terhubung ke perangkat")
                gatt?.requestMtu(517)
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("BluetoothGattCallback", "Putus sambungan dari perangkat")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt?.getService(UUID.fromString(SERVICE_UUID))
                val characteristic = service?.getCharacteristic(UUID.fromString(CHARASTERISTICS_UUID))
                gatt?.setCharacteristicNotification(characteristic, true)

                val descriptor = characteristic?.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG_UUID))
                descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt?.writeDescriptor(descriptor)
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            characteristic?.value?.let { value ->
                try {
                    // Log the raw value of the characteristic (you may want to inspect it)
                    Log.d("BluetoothGattCallback", "Characteristic Changed: ${value.decodeToString()}")

                    val dataString = value.decodeToString().split("/")
                    val _jarak = dataString[0].toFloat()
                    val _timestamp = dataString[1].toLong()
                    val _kecepatanPutaranArray = dataString[2].split(";")
                    val _kecepatanTranslasiArray = dataString[3].split(";")

                    // Log the parsed values
                    Log.d("BluetoothGattCallback", "Parsed Jarak: $_jarak")
                    Log.d("BluetoothGattCallback", "Parsed Timestamp: $_timestamp")
                    Log.d("BluetoothGattCallback", "Parsed Kecepatan Putaran: ${_kecepatanPutaranArray.joinToString()}")
                    Log.d("BluetoothGattCallback", "Parsed Kecepatan Translasi: ${_kecepatanTranslasiArray.joinToString()}")

                    connectionState = ConnectionState.Connected

                    // Emit data flow with success
                    coroutineScope.launch {
                        dataFlow.emit(
                            Resource.Success(
                                ESP32DataResult(
                                    jarak = _jarak,
                                    timestamp = _timestamp,
                                    kecepatanPutaran = doubleArrayOf(
                                        _kecepatanPutaranArray[0].toDouble(),
                                        _kecepatanPutaranArray[1].toDouble(),
                                        _kecepatanPutaranArray[2].toDouble()

                                    ),
                                    kecepatanTranslasi = doubleArrayOf(
                                        _kecepatanTranslasiArray[0].toDouble(),
                                        _kecepatanTranslasiArray[1].toDouble(),
                                        _kecepatanTranslasiArray[2].toDouble()
                                    ),
                                    connectionState = connectionState
                                )
                            )
                        )
                        Log.d("BluetoothGattCallback", "Parsed Kecepatan Putaran: ${_kecepatanPutaranArray[0].toString()}")

                        // Log the successful data emission
                        Log.d("BluetoothGattCallback", "Data emitted successfully to dataFlow.")
                    }
                } catch (e: Exception) {
                    Log.e("BluetoothGattCallback", "Error parsing characteristic value: ${e.message}")
                    coroutineScope.launch {
                        dataFlow.emit(Resource.Error("Gagal memproses data dari karakteristik: ${e.message}"))
                    }
                }
            }
        }

    }

    override fun startConnection() {
        if (isScanning) return
        isScanning = true
        coroutineScope.launch {
            dataFlow.emit(Resource.Loading(message = "Mencari perangkat..."))
        }
        bleScanner.startScan(scanCallback)
    }

    override fun reconnect() {
        disconnect()
        startConnection()
    }

    override fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        coroutineScope.launch {
            dataFlow.emit(Resource.Loading(message = "Koneksi diputus dari perangkat."))
        }
    }

    override fun startReceiving() {
        gatt?.let { connectedGatt ->
            if (connectionState == ConnectionState.Connected) {
                val characteristic = connectedGatt.getService(UUID.fromString(SERVICE_UUID))
                    ?.getCharacteristic(UUID.fromString(CHARASTERISTICS_UUID))
                characteristic?.let {
                    connectedGatt.readCharacteristic(it)
                } ?: coroutineScope.launch {
                    dataFlow.emit(Resource.Error("Karakteristik tidak ditemukan."))
                }
            } else {
                coroutineScope.launch {
                    dataFlow.emit(Resource.Error("Tidak dapat menerima data, koneksi belum terhubung."))
                }
            }
        }
    }

    override fun closeConnection() {
        bleScanner.stopScan(scanCallback)
        gatt?.close()
    }


}
