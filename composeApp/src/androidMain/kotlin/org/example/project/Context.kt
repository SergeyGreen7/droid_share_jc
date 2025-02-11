package org.example.project

import android.bluetooth.BluetoothManager
import android.content.Context
import android.net.wifi.WifiManager
import androidx.core.content.ContextCompat.getSystemService

internal lateinit var appContext: Context

internal val bluetoothManager: BluetoothManager by lazy {
    getSystemService(appContext, BluetoothManager::class.java)!!
}
internal val wifiManager: WifiManager by lazy {
    getSystemService(appContext, WifiManager::class.java)!!
}

