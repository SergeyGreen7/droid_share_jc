package org.example.project.fragments

import com.appstractive.dnssd.DiscoveredService
import com.darkrockstudios.libraries.mpfilepicker.MPFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.project.connection.mcdns.McDnsScanner
import org.example.project.data.DeviceInfoCommon
import org.example.project.utils.NotificationInterface
import org.example.project.data.P2pConnectionManager
import org.example.project.utils.TxFilesDescriptor
import org.example.project.ui.bleButtonVisible
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
    protected val role: FileSharingRole,
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

    private lateinit var dataTransceiver: P2pConnectionManager

    protected var txFiles = TxFilesDescriptor()
    protected open lateinit var notifier: NotificationInterface
    protected var selectedDeviceInfo: DeviceInfoCommon? = null

    abstract fun setMcDnsServiceName(name: String)
    abstract suspend fun registerMcDnsService()
    abstract fun unregisterMcDnsService()

    protected abstract fun connectBleClient(info: DeviceInfoCommon)

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

        dataTransceiver = P2pConnectionManager(notifier, saveFileDir)

        mcDnsScanner = McDnsScanner()

        bleClient.setServiceUuid(GATT_SERVICE_UUID)
        bleClient.setCharacteristicUuid(GATT_CHARACTER_SERVICE_CONFIG_UUID)
        bleService.setServiceUuid(GATT_SERVICE_UUID)
        bleService.setCharacteristicUuid(GATT_CHARACTER_SERVICE_CONFIG_UUID)
        bleScanner.setServiceUuid(GATT_SERVICE_UUID)

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

                    dataTransceiver.createServer(FS_SERVICE_PORT)
                }
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

    abstract fun config(notifier: NotificationInterface)

    protected fun disconnect() {
        println("FileShareFragment, disconnect()")
        bleScanner.stopScan()
        dataTransceiver.destroySocket()
        unregisterMcDnsService()
        mcDnsScanner.stopScan()
    }

    protected fun cancelConnection() {
        println("FileShareFragment, start cancelConnection(), role = $role")
        dataTransceiver.destroySocket()
        when (role) {
            FileSharingRole.FILE_TRANSMITTER -> {
                println("run bleScanner.startScan()")
                bleScanner.startScan()
                println("mcDnsScanner.stopScan()")
                mcDnsScanner.stopScan()
            }
            FileSharingRole.FILE_RECEIVER -> {
                println("mcDnsService.unregisterService()")
                unregisterMcDnsService()
            }
        }
        println("FileShareFragment, finish cancelConnection()")
    }

    abstract val setDeviceInfoCommon: (device: DeviceInfoCommon, index: Int) -> Unit
    abstract var getFileDescriptorFromPicker: ( files: List<MPFile<Any>>?) -> Unit

    val enableBleServiceCallback = { isEnabled: Boolean ->
        if (isEnabled) {
            bleService.startService()
        } else {
            bleService.stopService()
            unregisterMcDnsService()
        }
    }

    val enableBleScannerCallback = { isEnabled: Boolean ->
        if (isEnabled) {
            bleScanner.startScan()
        } else {
            bleScanner.stopScan()
        }
    }

    val sendDataCallback = {
        if (selectedDeviceInfo != null) {

            bleScanner.stopScan()
            val info = selectedDeviceInfo!!

            CoroutineScope(Dispatchers.Main).launch {
                notifier.showProgressDialog("Sending data" ) {
                    CoroutineScope(Dispatchers.IO).launch {
                        println("run cancelDataTransmission()")
                        dataTransceiver.cancelDataTransmission()
                    }
                    notifier.showNotification("Data transmission is canceled")
                }
            }

            val serviceName = "$FS_SERVICE_NAME_BASE-${(Math.random() * 10000).toInt().toString()}"
            bleClient.dataToSend = "$REFERENCE_DATA@$serviceName"
            bleClient.callbackOnDataSend = { flag: Boolean ->
                if (flag) {
                    notifier.showNotification("GATT data is sent")
                    bleClient.disconnect()

                    mcDnsScanner.referenceServiceName = serviceName
                    mcDnsScanner.callbackOnRefServiceFind = { service: DiscoveredService ->
                        println("run callbackOnRefServiceFind")
                        mcDnsScanner.stopScan()
                        CoroutineScope(Dispatchers.IO).launch {
//                            delay(1000)
                            dataTransceiver.createClient(
                                service.port,
                                InetAddress.getByName(service.addresses[0]))

                            dataTransceiver.sendData(txFiles)
                        }
                    }

                    println("run mcDnsScanner.scanForService(), serviceName = $serviceName")
                    mcDnsScanner.scanForService(serviceName)
                } else {
                    notifier.showNotification("GATT data is failed to send")
                }
            }
            connectBleClient(info)
        }
    }
}