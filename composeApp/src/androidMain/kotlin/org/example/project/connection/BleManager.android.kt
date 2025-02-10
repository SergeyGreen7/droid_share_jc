package org.example.project.connection

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.content.Context
import android.net.wifi.WifiManager
import org.example.project.ContextFactory
import org.example.project.data.DeviceInfoCommon
import java.util.UUID

@SuppressLint("MissingPermission")
class AndroidBleManager(
    contextFactory: ContextFactory
) : BleManager {

    private var gattClient: GattClient
    private var gattServer: GattServer
    private var gattScanner: GattScanner
    private var name = ""

    init {
        val bluetoothManager = (contextFactory.getActivity() as Activity)
            .getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        gattScanner = GattScanner(bluetoothManager.adapter.bluetoothLeScanner)
        gattServer = GattServer(contextFactory.getContext() as Context, bluetoothManager)
        gattClient = GattClient(contextFactory.getContext() as Context)

        val wifi = (contextFactory.getContext() as Context).getSystemService(Context.WIFI_SERVICE) as WifiManager
        val multicastLock = wifi.createMulticastLock("multicastLock")
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

    override fun connectBleClient(index: Int) {
        val info = gattScanner.getDevice(index)
        if (info != null) {
            gattClient.connect(info.bleScanResult!!)
        }
    }

    override fun sendMessageBleClient(message: String) {
        gattClient.sendMessage(message)
    }

    override fun disconnectBleClient() {
        gattClient.disconnect()
    }

}

actual fun getBleManager(
    contextFactory: ContextFactory
): BleManager = AndroidBleManager(contextFactory)