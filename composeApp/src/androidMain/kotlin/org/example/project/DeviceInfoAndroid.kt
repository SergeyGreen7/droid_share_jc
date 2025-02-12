package org.example.project

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import org.example.project.data.DeviceInfoCommon
import org.example.project.data.InfoType

class DeviceInfoAndroid : DeviceInfoCommon {

    var bleScanResult: ScanResult? = null

    @SuppressLint("MissingPermission")
    constructor(result: ScanResult)  : super() {
        type            = InfoType.BLE
        deviceName      = if (result.device.name != null) result.device.name else "null"
        deviceInfo      = result.device.address
        deviceAddress   = result.device.address
        bleScanResult   = result
    }

    fun toCommon(): DeviceInfoCommon {
        return DeviceInfoCommon(deviceName, deviceInfo)
    }
}