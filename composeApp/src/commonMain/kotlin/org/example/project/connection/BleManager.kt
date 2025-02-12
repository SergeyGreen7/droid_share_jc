package org.example.project.connection

import org.example.project.data.DeviceInfoCommon
import java.util.UUID

abstract class BleManager {

    private var bleClientConnected = false

    fun isBleClientConnected(): Boolean {
        return bleClientConnected
    }

    fun connectBleClient(index: Int) {
        bleClientConnected = true
        connectBleClientImpl(index)
    }

    fun disconnectBleClient() {
        bleClientConnected = false
        disconnectBleClientImpl()
    }

    fun sendMessageBleClient(message: String) {
        if (bleClientConnected) {
            sendMessageBleClientImpl(message)
        }
    }

    abstract fun getName() : String

    abstract fun configBleService(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        createServerCommand: String,
        destroyServerCommand: String,
        createServerCallback: (name: String) -> Unit,
        destroyServerCallback: () -> Unit)
    abstract fun startBleService()
    abstract fun stopBleService()

    abstract fun configBleScanner(
        serviceUuid: UUID,
        onListUpdate: (deviceList: List<DeviceInfoCommon>) -> Unit)
    abstract fun startBleScanner()
    abstract fun stopBleScanner()

    // TODO: add notification from BLE client in case if message is failed to send
    abstract fun configBleClient(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        callback: (flag: Boolean) -> Unit)
    abstract fun setBleClientDataToSend(data: String)
    protected abstract fun connectBleClientImpl(index: Int)
    protected abstract fun disconnectBleClientImpl()
    protected abstract fun sendMessageBleClientImpl(message: String)
}

expect fun getBleManager(): BleManager


