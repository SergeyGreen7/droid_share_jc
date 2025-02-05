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
import org.example.project.ui.alertDialogConfirmCallback
import org.example.project.ui.alertDialogDismissCallback
import org.example.project.ui.alertDialogText
import org.example.project.ui.alertDialogTitle
import org.example.project.utils.TxFilesDescriptor
import org.example.project.ui.fileStr
import org.example.project.ui.progressDialogCancelCallback
import org.example.project.ui.progressDialogProgressValue
import org.example.project.ui.progressDialogTitle
import org.example.project.ui.selectedIndex
import org.example.project.ui.sendDataButtonIsActive
import org.example.project.ui.shouldShowAlertDialog
import org.example.project.ui.shouldShowProgressDialog
import org.example.project.ui.showSnackbar
import org.example.project.ui.snackbarMessage
import java.net.InetAddress
import java.util.UUID

enum class FileSharingRole {
    FILE_TRANSMITTER,
    FILE_RECEIVER
}

abstract class FileShareBlockCommon (
    private val saveFileDir: String,
) {

    private val CREATE_SERVER_COMMAND = "CREATE_SERVER"
    private val DESTROY_SERVER_COMMAND = "DESTROY_SERVER"
    private val FS_SERVICE_NAME_BASE = "fs_service"
    private val FS_SERVICE_PORT = 8889
    private val GATT_SERVICE_UUID = UUID.fromString("5116c812-ad72-449f-a503-f8662bc21cde")
    private val GATT_CHARACTER_SERVICE_CONFIG_UUID = UUID.fromString("330fb1d7-afb6-4b00-b5da-3b0feeef9816")

    private var fsServiceName = ""
    private lateinit var mcDnsScanner: McDnsScanner
    protected lateinit var connectionManager: P2pConnectionManager

    protected var selectedDeviceIndex = -1
    protected var txFiles = TxFilesDescriptor()
    protected var selectedDeviceInfo: DeviceInfoCommon? = null

    protected abstract fun configBleService(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        createServerCommand: String,
        destroyServerCommand: String,
        createServerCallback: (name: String) -> Unit,
        destroyServerCallback: () -> Unit)
    protected abstract fun startBleService()
    protected abstract fun stopBleService()

    protected abstract fun configBleScanner(serviceUuid: UUID)
    protected abstract fun startBleScanner()
    protected abstract fun stopBleScanner()

    protected abstract fun configBleClient(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        callback: (flag: Boolean) -> Unit)
    protected abstract fun setBleClientDataToSend(data: String)
    protected abstract fun connectBleClient(info: DeviceInfoCommon)
    protected abstract fun sendMessageBleClient(message: String)
    protected abstract fun disconnectBleClient()

    abstract fun setMcDnsServiceName(name: String)
    abstract fun registerMcDnsService()
    abstract fun unregisterMcDnsService()

    protected abstract fun config()

    abstract val setDeviceInfoCommon: (device: DeviceInfoCommon, index: Int) -> Unit
    abstract var getFileDescriptorFromPicker: (files: PlatformFiles?) -> Unit

    protected val notifier = object: NotificationInterface {
        override fun updateProgressDialog(progress: Float) {
            progressDialogProgressValue.floatValue = progress
        }

        override fun dismissProgressDialog() {
            shouldShowProgressDialog.value = false
        }

        override fun showProgressDialog(
            title: String,
            cancelCallback: () -> Unit
        ) {
            progressDialogTitle.value = title
            progressDialogCancelCallback.value = cancelCallback
            progressDialogProgressValue.floatValue = 0.0F
            shouldShowProgressDialog.value = true
        }

        override fun showNotification(message: String) {
            snackbarMessage.value = message
            showSnackbar.value = true
        }

        override fun showAlertDialog(
            message: String,
            confirmCallback: () -> Unit,
            dismissCallback: () -> Unit
        ) {
            alertDialogTitle.value = ""
            alertDialogText.value = message
            alertDialogConfirmCallback.value = confirmCallback
            alertDialogDismissCallback.value = dismissCallback
            shouldShowAlertDialog.value = true
        }

        override fun dismissAlertDialog() {
            shouldShowAlertDialog.value = false
        }

        override fun closeConnection() {
            disableConnectionState()
            closeConnectionFunc()
        }

//        override fun disconnect() {
//            disableConnection()
//            disconnectFunc()
//        }

        override fun onDeviceListUpdate(deviceList: List<DeviceInfoCommon>) {
            println("onDeviceListUpdate, deviceList.size = ${org.example.project.ui.deviceList.size}")
            org.example.project.ui.deviceList.clear()
            deviceList.forEach {
                org.example.project.ui.deviceList.add(it)
            }
        }
    }

    fun init() {
        config()
        onCreate()
        startBleService()
    }

    fun stopAndDestroy() {
        stopBleScanner()
        stopBleService()
        disconnectBleClient()
    }

    protected open fun onCreate() {
        println("FileShareFragment, start onCreate()")

        connectionManager = P2pConnectionManager(notifier, saveFileDir)

        mcDnsScanner = McDnsScanner()

        // Configure BLE client
        configBleClient(
            GATT_SERVICE_UUID,
            GATT_CHARACTER_SERVICE_CONFIG_UUID
        ) { flag: Boolean ->
            if (flag) {
                notifier.showNotification("GATT data is sent")

                println("run mcDnsScanner.scanForService(), serviceName = $fsServiceName")
                mcDnsScanner.scanForService(fsServiceName)
            } else {
                notifier.showNotification("GATT data is failed to send")
            }
        }

        // Configure BLE service
        configBleService(
            GATT_SERVICE_UUID,
            GATT_CHARACTER_SERVICE_CONFIG_UUID,
            CREATE_SERVER_COMMAND,
            DESTROY_SERVER_COMMAND,
            createServerCallback = { name: String ->
                println("start createServerCallback, name = $name")

                setMcDnsServiceName(name)
                println("run registerService(), serviceName = $name")

                registerMcDnsService()

                CoroutineScope(Dispatchers.IO).launch {
                    connectionManager.createServer(FS_SERVICE_PORT)
                    println("createServer is finished")
                }
            },
            destroyServerCallback = {
                println("start destroyServerCallback")

                unregisterMcDnsService()
                connectionManager.destroySocket()
            }
        )
        configBleScanner(GATT_SERVICE_UUID)

        // Configure MC DNS scanner
        mcDnsScanner.callbackOnRefServiceFind = { service: DiscoveredService ->
            println("run callbackOnRefServiceFind")
            mcDnsScanner.stopScan()
            CoroutineScope(Dispatchers.IO).launch {
                connectionManager.createClient(
                    service.port,
                    InetAddress.getByName(service.addresses[0]))
                println("createClient is finished")

                println("run connectionManager.sendData(txFiles), txFiles.isNotEmpty() = ${txFiles.isNotEmpty()}")
                connectionManager.sendData(txFiles.clone())
            }
        }
    }

    private fun closeConnectionFunc() {
        println("start cancelConnectionFunc()")
        connectionManager.destroySocket()
        startBleScanner()
        mcDnsScanner.stopScan()
        unregisterMcDnsService()
        disconnectBleClient()
    }

    val enableBleServiceCallback = { isEnabled: Boolean ->
        if (isEnabled) {
            startBleService()
        } else {
            stopBleService()
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

        notifier.showProgressDialog("Sending data") {
            stopDataTransmission()
        }

        fsServiceName = "$FS_SERVICE_NAME_BASE-${(Math.random() * 10000).toInt()}"
        setBleClientDataToSend("$CREATE_SERVER_COMMAND@$fsServiceName")
        connectBleClient(info)
    }

    private fun stopDataTransmission() {
        println("start stopDataTransmission(), " +
                "activeDataTransmission = ${connectionManager.isActiveTransmission()}")

        if (connectionManager.isActiveTransmission()) {
            connectionManager.cancelDataTransmission()
        } else {
            sendMessageBleClient("$DESTROY_SERVER_COMMAND@")
            notifier.closeConnection()
        }
        disableConnectionState()
    }

    protected fun disableConnectionState() {
        txFiles.clear()
        selectedIndex.value = -1
        sendDataButtonIsActive.value = false
        fileStr.value = "file(s) not selected"
    }

    var registerMcDnsServiceDebug = { flag: Boolean ->
        if (flag) {
            registerMcDnsService()
        } else {
            unregisterMcDnsService()
        }
    }

    var enableMcDnsScannerDebug = { flag: Boolean ->
        if (flag) {
            mcDnsScanner.startScan()
        } else {
            mcDnsScanner.stopScan()
        }
    }
}