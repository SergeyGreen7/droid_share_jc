package org.example.project

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.net.nsd.NsdServiceInfo
import android.net.wifi.p2p.WifiP2pDevice
import org.example.project.data.DeviceInfoCommon
import org.example.project.data.InfoType

class WifiDirectServiceInfo (
    val instanceName: String,
    val registrationType: String,
    val device: WifiP2pDevice,
) {}

class DeviceInfoAndroid : DeviceInfoCommon {
    // var type: InfoType = InfoType.TEST
//    lateinit var deviceName: String
//    lateinit var deviceInfo: String
//    lateinit var deviceAddress: String
//    lateinit var state: ConnectionState

    var wifiP2pDevice: WifiP2pDevice? = null
    var bluetoothDevice: BluetoothDevice? = null
    var nsdServiceInfo: NsdServiceInfo? = null
    var bleScanResult: ScanResult? = null

    constructor(name: String, info: String) : super() {
        deviceName      = name
        deviceInfo      = info
        deviceAddress   = info
        wifiP2pDevice   = null
        bluetoothDevice = null
        nsdServiceInfo  = null
        bleScanResult   = null
    }

    constructor(device: WifiP2pDevice) : super() {
        type            = InfoType.WIFI_DIRECT_PEER
        deviceName      = device.deviceName
        deviceInfo      = getWifiP2pDeviceStatus(device.status)
        deviceAddress   = device.deviceAddress
        wifiP2pDevice   = device
        bluetoothDevice = null
        nsdServiceInfo  = null
        bleScanResult   = null
    }

//    constructor(service: WifiDirectServiceInfo): this(service.device) {
//        type        = InfoType.WIFI_DIRECT_SERVICE
//        deviceName  = service.instanceName
//        deviceInfo  = service.registrationType
//    }
//
//    @SuppressLint("MissingPermission")
//    constructor(device: BluetoothDevice) : super() {
//        type            = InfoType.BLUETOOTH
//        deviceName      = if (device.name != null) device.name else "null"
//        deviceInfo      = device.address
//        deviceAddress   = device.address
//        wifiP2pDevice   = null
//        bluetoothDevice = device
//        nsdServiceInfo  = null
//        bleScanResult   = null
//    }
//
//    constructor(info: NsdServiceInfo) : super() {
//        type            = InfoType.NSD
//        deviceName      = if (info.serviceName != null) info.serviceName else "null"
//        deviceInfo      = info.serviceType
//        deviceAddress   = info.host.toString()
//        wifiP2pDevice   = null
//        bluetoothDevice = null
//        nsdServiceInfo  = info
//        bleScanResult   = null
//    }

    @SuppressLint("MissingPermission")
    constructor(result: ScanResult)  : super() {
        type            = InfoType.BLE
        deviceName      = if (result.device.name != null) result.device.name else "null"
        deviceInfo      = result.device.address
        deviceAddress   = result.device.address
        wifiP2pDevice   = null
        bluetoothDevice = null
        nsdServiceInfo  = null
        bleScanResult   = result
    }

    fun toCommon(): DeviceInfoCommon {
        return DeviceInfoCommon(deviceName, deviceInfo)
    }
}

private fun getWifiP2pDeviceStatus(deviceStatus: Int): String {
    return when (deviceStatus) {
        WifiP2pDevice.AVAILABLE     -> "Available"
        WifiP2pDevice.INVITED       -> "Invited"
        WifiP2pDevice.CONNECTED     -> "Connected"
        WifiP2pDevice.FAILED        -> "Failed"
        WifiP2pDevice.UNAVAILABLE   -> "Unavailable"
        else -> "Unknown"
    }
}
