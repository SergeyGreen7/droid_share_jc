package org.example.project.fragments

import com.appstractive.dnssd.DiscoveredService
import io.github.vinceglb.filekit.core.PlatformFiles
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.project.connection.BleManager
import org.example.project.connection.LocalNetworkServiceManager
import org.example.project.connection.getBleManager
import org.example.project.data.DeviceInfoCommon
import org.example.project.utils.NotificationInterface
import org.example.project.data.P2pConnectionManager
import org.example.project.getPlatform
import org.example.project.ui.alertDialogConfirmCallback
import org.example.project.ui.alertDialogDismissCallback
import org.example.project.ui.alertDialogText
import org.example.project.ui.alertDialogTitle
import org.example.project.utils.TxFilesDescriptor
import org.example.project.ui.fileStr
import org.example.project.ui.nameStr
import org.example.project.ui.notificationDialogDismissCallback
import org.example.project.ui.notificationDialogTitle
import org.example.project.ui.progressDialogCancelCallback
import org.example.project.ui.progressDialogProgressValue
import org.example.project.ui.progressDialogTitle
import org.example.project.ui.selectedIndex
import org.example.project.ui.sendDataButtonIsActive
import org.example.project.ui.shouldShowAlertDialog
import org.example.project.ui.shouldShowNotificationDialog
import org.example.project.ui.shouldShowProgressDialog
import org.example.project.ui.showSnackbar
import org.example.project.ui.snackbarMessage
import java.net.InetAddress
import java.util.UUID


abstract class FileShareBlockCommon (
    private val saveFileDir: String,
) {

    private val CREATE_SERVER_COMMAND = "CREATE_SERVER"
    private val DESTROY_SERVER_COMMAND = "DESTROY_SERVER"
    private val FS_SERVICE_NAME_BASE = "fs_service"
    private val FS_SERVICE_PORT = 8889
    private val GATT_SERVICE_UUID = UUID.fromString("5116c812-ad72-449f-a503-f8662bc21cde")
    private val GATT_CHARACTER_SERVICE_CONFIG_UUID = UUID.fromString("330fb1d7-afb6-4b00-b5da-3b0feeef9816")

    private lateinit var bleManager: BleManager
    private var devices = mutableListOf<DeviceInfoCommon>()

    private var fsServiceName = ""
    private lateinit var lnsManager: LocalNetworkServiceManager
    private lateinit var connectionManager: P2pConnectionManager

    protected var txFiles = TxFilesDescriptor()
    private var selectedDeviceIndex = -1
    private var selectedDeviceInfo: DeviceInfoCommon? = null

    val setDeviceInfoCommon = { device: DeviceInfoCommon, index: Int ->
        println("setDeviceInfo(), index = $index")
        println("devices = $devices")
        selectedDeviceInfo = devices[index]
        selectedDeviceIndex = index
    }

    abstract var getFileDescriptorFromPicker: (files: PlatformFiles?) -> Unit

    protected val notifier = object: NotificationInterface {
        override fun updateProgressDialog(progress: Float) {
            progressDialogProgressValue.floatValue = progress
        }

        override fun dismissProgressDialog() {
            shouldShowProgressDialog.value = false
        }

        override fun showNotificationDialog(title: String, cancelCallback: () -> Unit) {
            shouldShowNotificationDialog.value = true
            notificationDialogTitle.value = title
            // var notificationDialogText = mutableStateOf("")
            notificationDialogDismissCallback.value = cancelCallback
        }

        override fun updateNotificationDialogTitle(title: String) {
            notificationDialogTitle.value = title
        }

        override fun dismissNotificationDialog() {
            shouldShowNotificationDialog.value = false
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

        override fun closePairConnection() {
            dismissNotificationDialog()
            disableConnectionState()
            closePairConnectionFunc()
        }

        override fun onDeviceListUpdate(deviceList: List<DeviceInfoCommon>) {
//            println("onDeviceListUpdate, deviceList.size = ${deviceList.size}")
//
//            devices.clear()
//            devices = deviceList.toMutableList()
//
//            org.example.project.ui.deviceList.clear()
//            deviceList.forEach {
//                org.example.project.ui.deviceList.add(it)
//            }
        }
    }

    fun init() {
        bleManager = getBleManager()
        onCreate()
        bleManager.startBleService()
        bleManager.startBleScanner()

        println("getPlatform() = ${getPlatform()}")
    }

    fun stopAndDestroy() {
        stopConnection()
        bleManager.stopBleScanner()
        bleManager.stopBleService()
        bleManager.disconnectBleClient()
    }

    private val sendCallback = { service: DiscoveredService ->
        println("run callbackOnRefServiceFind")
        lnsManager.stopScan()
        CoroutineScope(Dispatchers.IO).launch {
            connectionManager.createClient(
                service.port,
                InetAddress.getByName(service.addresses[0]))
            println("createClient is finished")

            println("run connectionManager.sendData(txFiles), txFiles.isNotEmpty() = ${txFiles.isNotEmpty()}")
            connectionManager.sendData(txFiles.clone())
        }
        Unit
    }

    private val pairConnectionCallback = { service: DiscoveredService ->
        println("run callbackOnRefServiceFind")
        lnsManager.stopScan()
        CoroutineScope(Dispatchers.IO).launch {
            connectionManager.createClient(
                service.port,
                InetAddress.getByName(service.addresses[0]))
            println("createClient is finished")

            println("run connectionManager.createPair()")
            connectionManager.createPair()
        }
        Unit
    }

    protected open fun onCreate() {
        println("FileShareFragment, start onCreate()")

        connectionManager = P2pConnectionManager(notifier, saveFileDir)
        lnsManager = LocalNetworkServiceManager("local")

        // Configure BLE client
        bleManager.configBleClient(
            GATT_SERVICE_UUID,
            GATT_CHARACTER_SERVICE_CONFIG_UUID
        ) { flag: Boolean ->
            if (flag) {
                notifier.showNotification("GATT data is sent")

                println("run lnsManager.scanForService(), serviceName = $fsServiceName")
                lnsManager.scanForService(fsServiceName)
            } else {
                notifier.showNotification("GATT data is failed to send")
            }
        }

        // Configure BLE service
        bleManager.configBleService(
            GATT_SERVICE_UUID,
            GATT_CHARACTER_SERVICE_CONFIG_UUID,
            CREATE_SERVER_COMMAND,
            DESTROY_SERVER_COMMAND,
            createServerCallback = { name: String ->
                println("start createServerCallback, name = $name")
                setLocalNetworkServiceName(name)

                println("run registerService(), serviceName = $name")

                registerLocalNetworkService()

                CoroutineScope(Dispatchers.IO).launch {
                    connectionManager.createServer(FS_SERVICE_PORT)
                    println("createServer is finished")
                }
            },
            destroyServerCallback = {
                println("start destroyServerCallback")

                unregisterLocalNetworkService()
                connectionManager.destroySocket()
            }
        )
        bleManager.configBleScanner(
            GATT_SERVICE_UUID) { deviceList: List<DeviceInfoCommon> ->
            println("onDeviceListUpdate, deviceList.size = ${deviceList.size}")

            devices.clear()
            devices = deviceList.toMutableList()

            org.example.project.ui.deviceList.clear()
            deviceList.forEach {
                org.example.project.ui.deviceList.add(it)
            }
        }

        val name = bleManager.getName()
        nameStr.value = "Your name: $name"
        connectionManager.setTransmitterName(name)
    }

    private fun closeConnectionFunc() {
        println("start closeConnectionFunc()")
        connectionManager.destroySocket()
        bleManager.startBleScanner()
        lnsManager.stopScan()
        unregisterLocalNetworkService()
        bleManager.disconnectBleClient()
    }

    private fun closePairConnectionFunc() {
        println("start closePairConnectionFunc()")
        connectionManager.destroySocket()
        bleManager.startBleScanner()
        lnsManager.stopScan()
        unregisterLocalNetworkService()
        bleManager.disconnectBleClient()
    }

    val enableBleServiceCallback = { isEnabled: Boolean ->
        if (isEnabled) {
            bleManager.startBleService()
        } else {
            bleManager.stopBleService()
            unregisterLocalNetworkService()
        }
    }

    val enableBleScannerCallback = { isEnabled: Boolean ->
        if (isEnabled) {
            bleManager.startBleScanner()
        } else {
            bleManager.stopBleScanner()
        }
    }

    val sendDataCallback = sendDataCallback@ {

        if (selectedDeviceIndex == -1) {
            println("do return@sendDataCallback")
            notifier.showNotification("Target device is not selected")
            return@sendDataCallback
        }

        if (txFiles.isEmpty()) {
            notifier.showNotification("Please select file(s) for transmission")
            return@sendDataCallback
        }

        bleManager.stopBleScanner()

        notifier.showProgressDialog("Sending data") {
            stopConnection()
        }

        lnsManager.callbackOnRefServiceFind = sendCallback
        fsServiceName = "$FS_SERVICE_NAME_BASE-${(Math.random() * 10000).toInt()}"
        bleManager.setBleClientDataToSend("$CREATE_SERVER_COMMAND@$fsServiceName")
        bleManager.connectBleClient(selectedDeviceIndex)
    }

    val createPairCallback = createPairCallback@ {

        if (selectedDeviceIndex == -1) {
            println("do return@sendDataCallback")
            notifier.showNotification("Target device is not selected")
            return@createPairCallback
        }

        bleManager.stopBleScanner()

        notifier.showNotificationDialog("Pair connection...") {
            stopConnection()
        }

        lnsManager.callbackOnRefServiceFind = pairConnectionCallback
        fsServiceName = "$FS_SERVICE_NAME_BASE-${(Math.random() * 10000).toInt()}"
        bleManager.setBleClientDataToSend("$CREATE_SERVER_COMMAND@$fsServiceName")
        bleManager.connectBleClient(selectedDeviceIndex)

    }

    private fun stopConnection() {
        println("start stopConnection(), " +
                "isActiveConnection = ${connectionManager.isActiveConnection()}")

        if (connectionManager.isActiveConnection()) {
            connectionManager.cancelConnection()
        } else if (bleManager.isBleClientConnected()) {
            bleManager.sendMessageBleClient("$DESTROY_SERVER_COMMAND@")
            notifier.closeConnection()
        }
        disableConnectionState()
    }

//    private fun stopDataTransmission() {
//        println("start stopDataTransmission(), " +
//                "activeDataTransmission = ${connectionManager.isActiveTransmission()}")
//
//        if (connectionManager.isActiveTransmission()) {
//            connectionManager.cancelDataTransmission()
//        } else {
//            bleManager.sendMessageBleClient("$DESTROY_SERVER_COMMAND@")
//            notifier.closeConnection()
//        }
//        disableConnectionState()
//    }
//
//    private fun destroyPairConnection() {
//        println("start stopDataTransmission(), " +
//                "activeDataTransmission = ${connectionManager.isActiveTransmission()}")
//
//        if (connectionManager.isPairConnection()) {
//            connectionManager.destroyPairConnection()
//        } else {
//            bleManager.sendMessageBleClient("$DESTROY_SERVER_COMMAND@")
//            notifier.closePairConnection()
//        }
//        disableConnectionState()
//    }

    protected fun disableConnectionState() {
        txFiles.clear()
        selectedIndex.value = -1
        sendDataButtonIsActive.value = false
        fileStr.value = "file(s) not selected"
    }

    private fun setLocalNetworkServiceName(name: String) {
        println("start setServiceName(), name = $name")
        lnsManager.setName(name)
    }

    private fun registerLocalNetworkService() {
        println("start registerMcDnsService()")
        lnsManager.registerService()
    }

    private fun unregisterLocalNetworkService() {
        println("start unregisterService()")
        lnsManager.unregisterService()
    }

    var registerMcDnsServiceDebug = { flag: Boolean ->
        if (flag) {
            registerLocalNetworkService()
        } else {
            unregisterLocalNetworkService()
        }
    }

    var enableMcDnsScannerDebug = { flag: Boolean ->
        if (flag) {
            lnsManager.startScan()
        } else {
            lnsManager.stopScan()
        }
    }

}