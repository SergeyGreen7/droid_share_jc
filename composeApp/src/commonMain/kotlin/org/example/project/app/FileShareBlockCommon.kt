package org.example.project.fragments

import com.appstractive.dnssd.DiscoveredService
import io.github.vinceglb.filekit.core.PlatformFiles
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.project.connection.mcdns.McDnsScanner
import org.example.project.data.DeviceInfoCommon
import org.example.project.utils.NotificationInterface
import org.example.project.data.P2pConnectionManager
import org.example.project.utils.TxFilesDescriptor
import org.example.project.ui.bleButtonVisible
import org.example.project.ui.bleScannerButtonText
import org.example.project.utils.BleClientInterface
import org.example.project.utils.BleScannerInterface
import org.example.project.utils.BleServiceInterface
import java.net.InetAddress
import java.util.UUID

enum class FileSharingRole {
    FILE_TRANSMITTER,
    FILE_RECEIVER
}

abstract class FileShareBlockCommon (
    private var role: FileSharingRole,
    private val saveFileDir: String,
) {

    private val REFERENCE_DATA = "1234567890"
    private val FS_SERVICE_NAME_BASE = "fs_service"
    private val FS_SERVICE_PORT = 8889
    private val GATT_SERVICE_UUID = UUID.fromString("5116c812-ad72-449f-a503-f8662bc21cde")
    private val GATT_CHARACTER_SERVICE_CONFIG_UUID = UUID.fromString("330fb1d7-afb6-4b00-b5da-3b0feeef9816")

    private lateinit var bleScanner: BleScannerInterface
    private lateinit var bleService: BleServiceInterface
    private lateinit var bleClient: BleClientInterface

    private lateinit var mcDnsScanner: McDnsScanner

    protected lateinit var connectionManager: P2pConnectionManager

    protected var txFiles = TxFilesDescriptor()
    protected open lateinit var notifier: NotificationInterface
    protected var selectedDeviceInfo: DeviceInfoCommon? = null

    abstract fun config(notifier: NotificationInterface)
    abstract fun setMcDnsServiceName(name: String)
    abstract suspend fun registerMcDnsService()
    abstract fun unregisterMcDnsService()
    protected abstract fun connectBleClient(info: DeviceInfoCommon)

    abstract val setDeviceInfoCommon: (device: DeviceInfoCommon, index: Int) -> Unit
    abstract var getFileDescriptorFromPicker: (files: PlatformFiles?) -> Unit

    protected fun configureBle(
        bleScanner: BleScannerInterface,
        bleService: BleServiceInterface,
        bleClient: BleClientInterface) {
        this.bleScanner = bleScanner
        this.bleService = bleService
        this.bleClient = bleClient
    }

    open fun onCreate() {
        println("FileShareFragment, start onCreate()")

        connectionManager = P2pConnectionManager(notifier, saveFileDir)

        mcDnsScanner = McDnsScanner()

        bleClient.setServiceUuid(GATT_SERVICE_UUID)
        bleClient.setCharacteristicUuid(GATT_CHARACTER_SERVICE_CONFIG_UUID)
        bleService.setServiceUuid(GATT_SERVICE_UUID)
        bleService.setCharacteristicUuid(GATT_CHARACTER_SERVICE_CONFIG_UUID)
        bleScanner.setServiceUuid(GATT_SERVICE_UUID)

        // Configure BLE service
        bleService.referenceData = REFERENCE_DATA
        bleService.callbackOnReferenceDataReception = { flag: Boolean, name:String ->
            println("start callbackOnDataReception(), flag = $flag, name = $name")

            if (flag) {
                setMcDnsServiceName(name)
                println("run registerService(), serviceName = $name")

                CoroutineScope(Dispatchers.IO).launch {
                    CoroutineScope(Dispatchers.IO).launch {
                        registerMcDnsService()
                    }.join()

                    connectionManager.createServer(FS_SERVICE_PORT)
                }
            }
        }

        // Configure BLE client
        bleClient.callbackOnDataSend = { flag: Boolean ->
            if (flag) {
                notifier.showNotification("GATT data is sent")
                bleClient.disconnect()

                val id = bleClient.dataToSend.indexOf("@")
                val serviceName = bleClient.dataToSend.substring(id+1)
                println("run mcDnsScanner.scanForService(), serviceName = $serviceName")
                mcDnsScanner.scanForService(serviceName)
            } else {
                notifier.showNotification("GATT data is failed to send")
            }
        }

        // Configure MC DNS scanner
        mcDnsScanner.callbackOnRefServiceFind = { service: DiscoveredService ->
            println("run callbackOnRefServiceFind")
            mcDnsScanner.stopScan()
            CoroutineScope(Dispatchers.IO).launch {
                connectionManager.createClient(
                    service.port,
                    InetAddress.getByName(service.addresses[0]))

                println("run dataTransceiver.sendData(txFiles), " +
                        "txFiles.isNotEmpty() = ${txFiles.isNotEmpty()}")
                connectionManager.sendData(txFiles.clone())
                txFiles.clear()
            }
        }

        bleButtonVisible = when (role) {
            FileSharingRole.FILE_TRANSMITTER -> {
                false
            }
            FileSharingRole.FILE_RECEIVER -> {
                true
            }
        }
    }

    protected fun disconnect() {
        println("start disconnect()")
//        when (role) {
//            FileSharingRole.FILE_TRANSMITTER -> {
//                stopBleScanner()
//                mcDnsScanner.stopScan()
//            }
//            FileSharingRole.FILE_RECEIVER -> {
//                unregisterMcDnsService()
//            }
//        }
        stopBleScanner()
        mcDnsScanner.stopScan()
        unregisterMcDnsService()
        connectionManager.destroySocket()
    }

    protected fun cancelConnection() {
        println("start cancelConnection(), role = $role")
        connectionManager.destroySocket()
        when (role) {
            FileSharingRole.FILE_TRANSMITTER -> {
                startBleScanner()
                mcDnsScanner.stopScan()
            }
            FileSharingRole.FILE_RECEIVER -> {
                unregisterMcDnsService()
            }
        }
    }

    val enableBleServiceCallback = { isEnabled: Boolean ->
        if (isEnabled) {
            role = FileSharingRole.FILE_RECEIVER
            bleService.startService()
        } else {
            bleService.stopService()
            unregisterMcDnsService()
        }
    }

    val enableBleScannerCallback = { isEnabled: Boolean ->
        if (isEnabled) {
            startBleScanner()
        } else {
            stopBleScanner()
        }
    }

    val sendDataCallback = sendDataCallback@ {
        // CoroutineScope(Dispatchers.IO).launch {
            if (selectedDeviceInfo == null) {
                println("do return@sendDataCallback")
                notifier.showNotification("Target device is not selected")
                return@sendDataCallback
            }

            if (txFiles.isEmpty()) {
                notifier.showNotification("Please select file(s) for transmission")
                return@sendDataCallback
            }

            stopBleScanner()
            val info = selectedDeviceInfo!!

            CoroutineScope(Dispatchers.Main).launch {
                notifier.showProgressDialog("Sending data") {
                    stopDataTransmission()
                }
            }

            val serviceName = "$FS_SERVICE_NAME_BASE-${(Math.random() * 10000).toInt()}"
            bleClient.dataToSend = "$REFERENCE_DATA@$serviceName"
            connectBleClient(info)
//        }
//        Unit
    }

    private fun startBleScanner() {
            bleScannerButtonText.value = "Turn Scanner OFF"
            bleScanner.startScan()
    }

    private fun stopBleScanner() {
        bleScannerButtonText.value = "Turn Scanner ON"
        bleScanner.stopScan()
    }
    private fun stopDataTransmission() {
        // CoroutineScope(Dispatchers.IO).launch {
            println("run cancelDataTransmission()")
            connectionManager.cancelDataTransmission()
        // }
        notifier.showNotification("Data transmission is canceled")
    }
}

