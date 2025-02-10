package org.example.project.connection

import WinBleNativeApi
import org.example.project.data.DeviceInfoCommon
import java.util.UUID

class DesktopBleManager : BleManager {

    private val winBle = WinBleNativeApi()

    override fun getName(): String {
        return winBle.hostName
    }

    override fun configBleService(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        createServerCommand: String,
        destroyServerCommand: String,
        createServerCallback: (name: String) -> Unit,
        destroyServerCallback: () -> Unit
    ) {
        winBle.setServiceName(serviceUuid.toString())
        winBle.setCharacterisitcUuid(characteristicUuid.toString())
        winBle.setCreateServerCommand(createServerCommand)
        winBle.setDestroyServerCommand(destroyServerCommand)
        winBle.setCreateServerCallback { name ->
            createServerCallback(name)
        }
        winBle.setDestroyServerCallback {
            destroyServerCallback()
        }
    }

    override fun startBleService() {
        winBle.startService()
    }

    override fun stopBleService() {
        winBle.stopService()
    }


    override fun configBleScanner(
        serviceUuid: UUID,
        onListUpdate: (deviceList: List<DeviceInfoCommon>) -> Unit
    ) {
        winBle.setServiceUuid(serviceUuid.toString())
        winBle.configShowDiscoveredDevicesCallback() { names, addresses ->
            println("ShowDiscoveredDevicesCallback start" +
                    ", names.size = ${names?.size}, addresses.size = ${addresses?.size}")

            val deviceList = mutableListOf<DeviceInfoCommon>()
            if (names != null && addresses != null) {
                for (i in names.indices) {
                    deviceList.add(DeviceInfoCommon(names[i], addresses[i]))
                }
            }
            onListUpdate(deviceList)
        }
    }

    override fun startBleScanner() {
        winBle.startScanner()
    }

    override fun stopBleScanner() {
        winBle.stopScanner()
    }

    override fun configBleClient(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        callback: (flag: Boolean) -> Unit
    ) {
        winBle.setServiceName(serviceUuid.toString())
        winBle.setCharacterisitcUuid(characteristicUuid.toString())
        winBle.configDataSendCallback { flag -> callback(flag) }
    }

    override fun setBleClientDataToSend(data: String) {
        winBle.setDataToSend(data)
    }

    override fun connectBleClient(index: Int) {
     winBle.connectBleClient(index)
    }

    override fun sendMessageBleClient(message: String) {
        winBle.sendMessageBleClient(message)
    }

    override fun disconnectBleClient() {
        winBle.disconnectBleClient()
    }

}

actual fun getBleManager(): BleManager = DesktopBleManager()
