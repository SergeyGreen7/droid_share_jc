package org.example.project.data

enum class InfoType {
    TEST,
    WIFI_DIRECT_PEER,
    WIFI_DIRECT_SERVICE,
    BLUETOOTH,
    NSD,
    BLE
}

enum class ConnectionState {
    DISCONNECTED,
    CONNECTED
}

open class DeviceInfoCommon {
    var type: InfoType = InfoType.TEST
    lateinit var deviceName: String
    lateinit var deviceInfo: String
    lateinit var deviceAddress: String
    lateinit var state: ConnectionState

    constructor() {
        deviceName      = "not defined name"
        deviceInfo      = "not defined info"
        deviceAddress   = "not defined info"
        state           = ConnectionState.DISCONNECTED
    }

    constructor(name: String, info: String) {
        deviceName      = name
        deviceInfo      = info
        deviceAddress   = info
        state           = ConnectionState.DISCONNECTED
    }
}
