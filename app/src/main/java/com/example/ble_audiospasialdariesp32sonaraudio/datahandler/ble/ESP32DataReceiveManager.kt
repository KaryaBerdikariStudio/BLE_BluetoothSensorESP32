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
import kotlinx.coroutines.delay
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


    // Tambahkan variabel untuk menyimpan keadaan koneksi
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
                coroutineScope.launch {
                    dataFlow.emit(Resource.Loading(message = "Mencoba Terhubung ke Perangkat..."))
                    connectionState = ConnectionState.Connecting // Ubah keadaan menjadi Connecting

                    val isPaired = BluetoothAdapter.getDefaultAdapter().bondedDevices.any { it.address == result.device.address }
                    if (!isPaired) {
                        val bondingResult = result.device.createBond()
                        Log.i("Bluetooth", "Bonding dimulai: $bondingResult")

                        // Kirim hasil ke resource
                        if (bondingResult) {
                            dataFlow.emit(Resource.Loading(message = "Memasang Perangkat..."))
                        } else {
                            result.device.createBond()
                        }
                    }
                }

                result.device.connectGatt(context, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE)
                isScanning = false
            }
        }
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("BluetoothGattCallback", "Terhubung ke perangkat")
                connectionState = ConnectionState.Connected
                gatt?.requestMtu(517)
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("BluetoothGattCallback", "Putus sambungan dari perangkat")
                connectionState = ConnectionState.Disconnected
            } else {
                Log.d("BluetoothGattCallback", "Status koneksi berubah: $newState")
            }
        }


        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt?.getService(UUID.fromString(SERVICE_UUID))
                val characteristic = service?.getCharacteristic(UUID.fromString(CHARASTERISTICS_UUID))

                // Enable notifications for the characteristic
                gatt?.setCharacteristicNotification(characteristic, true)

                // Optionally write a descriptor to enable notifications on the server side
                val descriptor = characteristic?.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG_UUID))
                descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt?.writeDescriptor(descriptor)
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            characteristic?.value?.let { value ->
               Log.d("BluetoothGattCallback", "Data diterima: ${value.decodeToString()} + oncharchanged")
                try {
                    val dataString = value.decodeToString().split("/")

                    val _jarak = dataString[0].toFloat()
                    val _orientasiArray = dataString[1].split(";")
                    val _kecepatanPutaranArray = dataString[2].split(";")
                    val _kecepatanTranslasiArray = dataString[3].split(";")
                    connectionState = ConnectionState.Connected // Update state to Connected

                    // Log status koneksi

                    Log.d("ConnectionState", "Status koneksi diperbarui: $connectionState. Jarak: $_jarak")

                    coroutineScope.launch {
                        dataFlow.emit(
                            Resource.Success(
                                ESP32DataResult(
                                    jarak = _jarak,
                                    orientasi = floatArrayOf(
                                        _orientasiArray[0].toFloat(),
                                        _orientasiArray[1].toFloat()
                                    ),
                                    kecepatanPutaran = floatArrayOf(
                                        _kecepatanPutaranArray[0].toFloat(),
                                        _kecepatanPutaranArray[1].toFloat()
                                    ),
                                    kecepatanTranslasi = floatArrayOf(
                                        _kecepatanTranslasiArray[0].toFloat(),
                                        _kecepatanTranslasiArray[1].toFloat(),
                                        _kecepatanTranslasiArray[2].toFloat()
                                    ),
                                    connectionState = connectionState
                                )
                            )
                        )

                        // Log untuk mengonfirmasi pengiriman data
                       Log.d("DataFlow", "Data berhasil dikirim ke dataFlow: jarak=$_jarak, orientasi=${_orientasiArray.joinToString()}, kecepatanPutaran=${_kecepatanPutaranArray.joinToString()}, kecepatanTranslasi=${_kecepatanTranslasiArray.joinToString()}, status koneksi: $connectionState")
                    }
                } catch (e: Exception) {
                    Log.e("BluetoothGattCallback", "Error parsing characteristic value: ${e.message}")
                    coroutineScope.launch {
                        dataFlow.emit(Resource.Error(errorMessage = "Gagal memproses data dari karakteristik: ${e.message}"))
                    }
                }
            }
        }




        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                characteristic?.value?.let { value ->
                    try {
                        val dataString = value.decodeToString().split("/")

                        val _jarak = dataString[0].toFloat()
                        val _orientasiArray = dataString[1].split(";")
                        val _kecepatanPutaranArray = dataString[2].split(";")
                        val _kecepatanTranslasiArray = dataString[3].split(";")
                        connectionState = ConnectionState.Connected // Update state to Connected

                        coroutineScope.launch {
                            dataFlow.emit(
                                Resource.Success(
                                    ESP32DataResult(
                                        jarak = _jarak,
                                        orientasi = floatArrayOf(
                                            _orientasiArray[0].toFloat(),
                                            _orientasiArray[1].toFloat()
                                        ),
                                        kecepatanPutaran = floatArrayOf(
                                            _kecepatanPutaranArray[0].toFloat(),
                                            _kecepatanPutaranArray[1].toFloat()
                                        ),
                                        kecepatanTranslasi = floatArrayOf(
                                            _kecepatanTranslasiArray[0].toFloat(),
                                            _kecepatanTranslasiArray[1].toFloat(),
                                            _kecepatanTranslasiArray[2].toFloat()
                                        ),
                                        connectionState = ConnectionState.Connected // Update state to Connected
                                    )
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("BluetoothGattCallback", "Error parsing characteristic value: ${e.message}")
                        coroutineScope.launch {
                            dataFlow.emit(Resource.Error(errorMessage = "Gagal memproses data dari karakteristik: ${e.message}"))
                        }
                    }
                }
            }
        }

    }

    override fun startConnection() {
        if (isScanning) return // Cegah pemindaian ganda

        isScanning = true
        coroutineScope.launch {
            dataFlow.emit(Resource.Loading(message = "Mencari Perangkat..."))
            // Loop hingga perangkat terpasang
            while (isScanning) {
                // Periksa perangkat yang sudah dipasangkan
                val pairedDevices = BluetoothAdapter.getDefaultAdapter().bondedDevices
                val isPaired = pairedDevices.any { it.name == DEVICE_NAME }

                if (isPaired) {
                    dataFlow.emit(Resource.Loading(message = "Memasang Perangkat"))
                    break // Keluar dari loop jika perangkat sudah terpasang
                }

                // Jeda sebelum mencoba pemindaian berikutnya
                delay(500) // Sesuaikan jeda sesuai kebutuhan
            }

        }



        bleScanner.startScan(scanCallback) // Mulai pemindaian
    }

    override fun reconnect() {
        if (gatt != null) {
            disconnect() // Putuskan koneksi yang ada terlebih dahulu
        }
        // Memulai kembali koneksi
        startConnection()
    }

    override fun disconnect() {
        gatt?.disconnect() // Putuskan koneksi
        gatt?.close() // Tutup koneksi
        gatt = null // Set gatt ke null
        connectionState = ConnectionState.Disconnected // Update keadaan koneksi
        coroutineScope.launch {
            dataFlow.emit(Resource.Loading(message = "Koneksi diputus.")) // Emit pesan
        }
    }

    override fun startReceiving() {
        // Cek jika gatt terhubung
        gatt?.let { connectedGatt ->
            if (connectionState == ConnectionState.Connected) {
                // Jika GATT dan state koneksi baik, lanjutkan menerima data
                val characteristic = connectedGatt.getService(UUID.fromString(SERVICE_UUID))
                    ?.getCharacteristic(UUID.fromString(CHARASTERISTICS_UUID))
                characteristic?.let {
                    connectedGatt.readCharacteristic(it) // Membaca karakteristik
                } ?: coroutineScope.launch {
                    dataFlow.emit(Resource.Error(errorMessage = "Karakteristik tidak ditemukan."))
                }
            } else {
                coroutineScope.launch {
                    dataFlow.emit(Resource.Error(errorMessage = "Tidak dapat menerima data, koneksi tidak terhubung."))
                }
            }
        }
    }

    override fun closeConnection() {
        bleScanner.stopScan(scanCallback)
        gatt?.close()
    }
}
