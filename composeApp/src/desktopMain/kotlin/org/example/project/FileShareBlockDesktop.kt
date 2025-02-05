package org.example.project

import ShowDiscoveredDevicesCallback
import WinBleNativeApi
import io.github.vinceglb.filekit.core.PlatformFiles
import org.example.project.connection.mcdns.McDnsService
import org.example.project.data.DeviceInfoCommon
import org.example.project.utils.TxFileDescriptor
import org.example.project.fragments.FileShareBlockCommon
import org.example.project.ui.*
import java.io.FileInputStream
import java.nio.file.Files
import java.util.UUID
import kotlin.io.path.Path

class FileShareBlockDesktop (
    saveFileDir: String,
) : FileShareBlockCommon(
    saveFileDir
) {

    private var mcDnsService = McDnsService("desktop-")
    private val winBle = WinBleNativeApi()
    private var devices = mutableListOf<DeviceInfoCommon>()

    override fun onCreate() {
        super.onCreate()

        nameStr.value = "Your name: ${winBle.hostName}"
        connectionManager.setTransmitterName(winBle.hostName)
    }

    override fun config() {

        val showDiscoveredDevicesCallback = object: ShowDiscoveredDevicesCallback {
            override fun run(names: Array<out String>?, addresses: Array<out String>?) {
                println("ShowDiscoveredDevicesCallback start" +
                    ", names.size = ${names?.size}, addresses.size = ${addresses?.size}")
                if (names == null || addresses == null) {
                    return
                }

                if (selectedDeviceIndex >= names.size) {
                    selectedDeviceIndex = -1
                }

                devices.clear()
                for (i in names.indices) {
                    devices.add(DeviceInfoCommon(names[i], addresses[i]))
                    println("name: ${names[i]}, address: ${addresses[i]}")
                }
                notifier.onDeviceListUpdate(devices)
            }
        }
        winBle.configShowDiscoveredDevicesCallback(showDiscoveredDevicesCallback)
    }

    override val setDeviceInfoCommon = { device: DeviceInfoCommon, index: Int ->
        println("setDeviceInfo(), selectedDeviceInfoCommon = $device, index = $index")
        selectedDeviceInfo = devices[index]
        selectedDeviceIndex = index
    }

    override fun configBleService(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        createServerCommand: String,
        destroyServerCommand: String,
        createServerCallback: (name: String) -> Unit,
        destroyServerCallback: () -> Unit,
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
        println("start winBle.startService()")
        winBle.startService()
    }

    override fun stopBleService() {
        winBle.stopService()
    }

    override fun configBleScanner(serviceUuid: UUID) {
        winBle.setServiceUuid(serviceUuid.toString())
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

    override fun connectBleClient(info: DeviceInfoCommon) {
        println("selectedDeviceIndex = $selectedDeviceIndex")
        if (selectedDeviceIndex >= 0) {
            winBle.connectBleClient(selectedDeviceIndex)
        }
    }

    override fun disconnectBleClient() {
        winBle.disconnectBleClient()
    }

    override fun setMcDnsServiceName(name: String) {
        println("FileShareBlockDesktop, setServiceName(), name = $name")
        mcDnsService.serviceName = name
    }

    override fun registerMcDnsService() {
        println("start mcDnsService.registerService()")
        mcDnsService.registerService()
    }

    override fun unregisterMcDnsService() {
        println("start mcDnsService.unregisterService()")
        mcDnsService.unregisterService()
    }


    override fun sendMessageBleClient(message: String) {
        winBle.sendMessageBleClient(message)
    }

    override var getFileDescriptorFromPicker = { files: PlatformFiles? ->

        enableBleScannerCallback(false)
        sendDataButtonIsActive.value = false
        deviceList.clear()

        txFiles.clear()
        if (!files.isNullOrEmpty()) {
            println("$files")

            files.forEach { file ->
                println("file = $file")

                val f = file.file
                val fileName = f.name
                val fileSize = Files.size(Path(f.path)).toInt()
                val inputStream = FileInputStream(f)
                println("fileName = $fileName")
                println("fileSize = $fileSize")
                println("inputStream = $inputStream")

                txFiles.add(
                    TxFileDescriptor(fileName, fileSize, inputStream)
                )
            }
            fileStr.value = "file(s) selected"
            println("fileStr = ${fileStr.value}")
        }

        enableBleScannerCallback(true)
        sendDataButtonIsActive.value = true
    }
}