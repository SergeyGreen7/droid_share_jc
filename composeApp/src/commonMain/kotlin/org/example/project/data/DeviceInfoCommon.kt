package org.example.project.data

enum class InfoType {
    TEST,
    WIFI_DIRECT_PEER,
    WIFI_DIRECT_SERVICE,
    BLUETOOTH,
    NSD,
    BLE
}

open class DeviceInfoCommon {
    var type: InfoType = InfoType.TEST
    var deviceName: String
    var deviceInfo: String
    var deviceAddress: String

    constructor() {
        deviceName      = "not defined name"
        deviceInfo      = "not defined info"
        deviceAddress   = "not defined info"
    }

    constructor(name: String, info: String) {
        deviceName      = name
        deviceInfo      = info
        deviceAddress   = info
    }
}
