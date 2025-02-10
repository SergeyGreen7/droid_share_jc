package org.example.project

import io.github.vinceglb.filekit.core.PlatformFiles
import org.example.project.utils.TxFileDescriptor
import org.example.project.fragments.FileShareBlockCommon
import org.example.project.ui.*
import java.io.FileInputStream
import java.nio.file.Files
import kotlin.io.path.Path

class FileShareBlockDesktop (
    saveFileDir: String,
) : FileShareBlockCommon(
    saveFileDir
) {

    // private var mcDnsService = McDnsService("desktop-")
    // private val winBle = WinBleNativeApi()

    override fun onCreate() {
        super.onCreate()

        // nameStr.value = "Your name: ${winBle.hostName}"
//        connectionManager.setTransmitterName(winBle.hostName)
    }

//    override fun config() {
//        val showDiscoveredDevicesCallback = object: ShowDiscoveredDevicesCallback {
//            override fun run(names: Array<out String>?, addresses: Array<out String>?) {
//                println("ShowDiscoveredDevicesCallback start" +
//                    ", names.size = ${names?.size}, addresses.size = ${addresses?.size}")
//                if (names == null || addresses == null) {
//                    return
//                }
//
//                if (selectedDeviceIndex >= names.size) {
//                    selectedDeviceIndex = -1
//                }
//
//                devices.clear()
//                for (i in names.indices) {
//                    devices.add(DeviceInfoCommon(names[i], addresses[i]))
//                    println("name: ${names[i]}, address: ${addresses[i]}")
//                }
//                notifier.onDeviceListUpdate(devices)
//            }
//        }
        // winBle.configShowDiscoveredDevicesCallback(showDiscoveredDevicesCallback)
//    }

//    override val setDeviceInfoCommon = { device: DeviceInfoCommon, index: Int ->
//        println("setDeviceInfo(), index = $index")
//        println("devices = $devices")
//        selectedDeviceInfo = devices[index]
//        selectedDeviceIndex = index
//    }

//    override fun setMcDnsServiceName(name: String) {
//        println("FileShareBlockDesktop, setServiceName(), name = $name")
//        mcDnsService.serviceName = name
//    }

//    override fun registerMcDnsService() {
//        println("start mcDnsService.registerService()")
//        mcDnsService.registerService()
//    }
//
//    override fun unregisterMcDnsService() {
//        println("start mcDnsService.unregisterService()")
//        mcDnsService.unregisterService()
//    }

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