package com.example.ble_bluetoothlowenergyesp32.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.example.ble_bluetoothlowenergyesp32.data.JarakReceiveManager
import com.example.ble_bluetoothlowenergyesp32.data.ble.JarakBLEReceiveManagerController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBluetoothAdapter(@ApplicationContext context:Context):BluetoothAdapter{
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return  manager.adapter
    }

    @Provides
    @Singleton
    fun provideJarakReceiveManager(
        @ApplicationContext context: Context,
        bluetoothAdapter: BluetoothAdapter
    ):JarakReceiveManager{
        return JarakBLEReceiveManagerController(bluetoothAdapter, context
        )
    }



}