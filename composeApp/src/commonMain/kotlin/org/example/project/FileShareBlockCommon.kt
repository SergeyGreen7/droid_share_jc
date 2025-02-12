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
import org.example.project.ui.FileShareViewModel
import org.example.project.utils.TxFilesDescriptor
import java.net.InetAddress
import java.util.UUID


abstract class FileShareBlockCommon (
    protected val vm: FileShareViewModel,
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

    var getFileDescriptorFromPicker = { files: PlatformFiles? ->
        getFileDescriptorFromPickerImpl(files)
    }
    abstract fun getFileDescriptorFromPickerImpl(files: PlatformFiles?)

    protected val notifier = object: NotificationInterface {
        override fun updateProgressDialog(progress: Float) {
            vm.setProgressWindowValue(progress)
        }

        override fun dismissProgressDialog() {
            vm.closeProgressWindow()
        }

        override fun showNotificationDialog(title: String, cancelCallback: () -> Unit) {
            vm.showPairingWindow(title, "", cancelCallback)
        }

        override fun updateNotificationDialogTitle(title: String) {
            vm.updateTitlePairingWindow(title)
        }

        override fun dismissNotificationDialog() {
            vm.closePairingWindow()
        }

        override fun showProgressDialog(
            title: String,
            cancelCallback: () -> Unit
        ) {
            vm.showProgressWindow(title, cancelCallback)
        }

        override fun showNotification(message: String) {
            vm.showNotificationWindow(message)
        }

        override fun showAlertDialog(
            message: String,
            confirmCallback: () -> Unit,
            dismissCallback: () -> Unit
        ) {
            vm.showDialogWindow("", message, confirmCallback, dismissCallback)
        }

        override fun dismissAlertDialog() {
            vm.closeDialogWindow()
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
        enableBleScannerCallback(true)

        println("getPlatform() = ${getPlatform()}")
    }

    fun stopAndDestroy() {
        stopConnection()
        enableBleScannerCallback(false)
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

            vm.setDeviceList(deviceList)
        }

        val name = bleManager.getName()
        vm.setHostName(name)
        connectionManager.setTransmitterName(name)
    }

    private fun closeConnectionFunc() {
        println("start closeConnectionFunc()")
        connectionManager.destroySocket()
        enableBleScannerCallback(true)
        lnsManager.stopScan()
        unregisterLocalNetworkService()
        bleManager.disconnectBleClient()
    }

    private fun closePairConnectionFunc() {
        println("start closePairConnectionFunc()")
        connectionManager.destroySocket()
        enableBleScannerCallback(true)
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
        vm.bleServiceEnabledDebug.value = isEnabled
    }

    val enableBleScannerCallback = { isEnabled: Boolean ->
        if (isEnabled) {
            bleManager.startBleScanner()
        } else {
            bleManager.stopBleScanner()
        }
        vm.bleScannerEnabledDebug.value = isEnabled
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

        enableBleScannerCallback(false)

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

    protected fun disableConnectionState() {
        txFiles.clear()
        vm.dropSelectedIndex()
        vm.onDataRemove()
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

    private var enableMcDnsServiceDebug = { flag: Boolean ->
        if (flag) {
            registerLocalNetworkService()
        } else {
            unregisterLocalNetworkService()
        }
    }

    private var enableMcDnsScannerDebug = { flag: Boolean ->
        if (flag) {
            lnsManager.startScan()
        } else {
            lnsManager.stopScan()
        }
    }

    init {
        vm.bleServiceCallback = enableBleServiceCallback
        vm.bleScannerCallback = enableBleScannerCallback
        vm.sendDataCallback = sendDataCallback
        vm.pairCreationCallback = createPairCallback
        vm.setDeviceInfoCallback = setDeviceInfoCommon
        vm.createFileDscrList = getFileDescriptorFromPicker
        vm.enableMcDnsService = enableMcDnsServiceDebug
        vm.enableMcDnsScanner = enableMcDnsScannerDebug
    }

}