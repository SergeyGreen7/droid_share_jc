package org.example.project.connection

import org.example.project.ContextFactory
import org.example.project.data.DeviceInfoCommon
import java.util.UUID

interface BleManager {
    fun getName() : String

    fun configBleService(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        createServerCommand: String,
        destroyServerCommand: String,
        createServerCallback: (name: String) -> Unit,
        destroyServerCallback: () -> Unit)
    fun startBleService()
    fun stopBleService()

    fun configBleScanner(
        serviceUuid: UUID,
        onListUpdate: (deviceList: List<DeviceInfoCommon>) -> Unit)
    fun startBleScanner()
    fun stopBleScanner()

    fun configBleClient(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        callback: (flag: Boolean) -> Unit)
    fun setBleClientDataToSend(data: String)
    fun connectBleClient(index: Int)
    fun sendMessageBleClient(message: String)
    fun disconnectBleClient()
}

expect fun getBleManager(
    contextFactory: ContextFactory
): BleManager


