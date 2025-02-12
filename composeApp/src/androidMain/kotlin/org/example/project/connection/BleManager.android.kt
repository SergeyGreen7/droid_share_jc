package org.example.project.connection

import android.annotation.SuppressLint
import org.example.project.appContext
import org.example.project.bluetoothManager
import org.example.project.connection.gatt.GattClient
import org.example.project.connection.gatt.GattScanner
import org.example.project.connection.gatt.GattServer
import org.example.project.data.DeviceInfoCommon
import org.example.project.wifiManager
import java.util.UUID

@SuppressLint("MissingPermission")
class AndroidBleManager : BleManager() {

    private var gattClient: GattClient
    private var gattServer: GattServer
    private var gattScanner: GattScanner
    private var name = ""

    init {
        gattScanner = GattScanner(bluetoothManager.adapter.bluetoothLeScanner)
        gattServer = GattServer(appContext, bluetoothManager)
        gattClient = GattClient(appContext)

        val multicastLock = wifiManager.createMulticastLock("multicastLock")
        multicastLock.setReferenceCounted(true)
        multicastLock.acquire()

        name = bluetoothManager.adapter.name
    }

    override fun getName(): String = name

    override fun configBleService(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        createServerCommand: String,
        destroyServerCommand: String,
        createServerCallback: (name: String) -> Unit,
        destroyServerCallback: () -> Unit,
    ) {
        gattServer.setServiceUuid(serviceUuid)
        gattServer.setCharacteristicUuid(characteristicUuid)
        gattServer.createServerCommand = createServerCommand
        gattServer.destroyServerCommand = destroyServerCommand
        gattServer.createServerCallback = createServerCallback
        gattServer.destroyServerCallback = destroyServerCallback
    }

    override fun startBleService() {
        gattServer.startBleService()
    }

    override fun stopBleService() {
        gattServer.stopBleService()
    }

    override fun configBleScanner(
        serviceUuid: UUID,
        onListUpdate: (deviceList: List<DeviceInfoCommon>) -> Unit
    ) {
        gattScanner.setServiceUuid(serviceUuid)
        gattScanner.onDeviceListUpdate = onListUpdate
    }

    override fun startBleScanner() {
        gattScanner.startScanPeriodic()
    }

    override fun stopBleScanner() {
        gattScanner.stopScan()
    }

    override fun configBleClient(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        callback: (flag: Boolean) -> Unit
    ) {
        gattClient.setServiceUuid(serviceUuid)
        gattClient.setCharacteristicUuid(characteristicUuid)
        gattClient.callbackOnDataSend = callback

    }

    override fun setBleClientDataToSend(data: String) {
        gattClient.dataToSend = data
    }

    override fun connectBleClientImpl(index: Int) {
        val info = gattScanner.getDevice(index)
        if (info != null) {
            gattClient.connect(info.bleScanResult!!)
        }
    }

    override fun sendMessageBleClientImpl(message: String) {
        gattClient.sendMessage(message)
    }

    override fun disconnectBleClientImpl() {
        gattClient.disconnect()
    }

}

actual fun getBleManager(): BleManager = AndroidBleManager()