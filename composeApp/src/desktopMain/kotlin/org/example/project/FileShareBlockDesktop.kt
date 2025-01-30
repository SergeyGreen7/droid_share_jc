package org.example.project

import ShowDiscoveredDevicesCallback
import WinBleNativeApi
import io.github.vinceglb.filekit.core.PlatformFiles
import org.example.project.connection.mcdns.McDnsService
import org.example.project.data.DeviceInfoCommon
import org.example.project.utils.NotificationInterface
import org.example.project.utils.TxFileDescriptor
import org.example.project.fragments.FileShareBlockCommon
import org.example.project.fragments.FileSharingRole
import org.example.project.ui.*
import org.example.project.utils.BleClientInterface
import org.example.project.utils.BleScannerInterface
import org.example.project.utils.BleServiceInterface
import java.io.FileInputStream
import java.nio.file.Files
import java.util.UUID
import kotlin.io.path.Path

class FileShareBlockDesktop (
    role: FileSharingRole,
    saveFileDir: String,
) : FileShareBlockCommon(
    role, saveFileDir
) {

    private var selectedDeviceIndex = -1
    private var mcDnsService: McDnsService
    private val winBle = WinBleNativeApi()
    private var devices = mutableListOf<DeviceInfoCommon>()

    init {
        val showDiscoveredDevicesCallback = object: ShowDiscoveredDevicesCallback {
            override fun run(names: Array<out String>?, addresses: Array<out String>?) {
                println("showDiscoveredDevicesCallback")
            }
        }
        winBle.configShowDiscoveredDevicesCallback(showDiscoveredDevicesCallback)

        winBle.setServiceUuid("5116c812-ad72-449f-a503-f8662bc21cde")
        winBle.setCharacterisitcUuid("330fb1d7-afb6-4b00-b5da-3b0feeef9816")
        winBle.setReferenceData("1234567890")
        winBle.setDataToSend("1234567890@fs_service-777")

        mcDnsService = McDnsService("default-")
    }

    override fun onCreate() {
        super.onCreate()

//        Log.d(TAG, "FileShareFragment, start onCreate()")
        // super.onCreate(savedInstanceState)

//        val sb = StringBuilder("MODEL: "+android.os.Build.MODEL
//        +"\nDEVICE: "+android.os.Build.DEVICE
//        +"\nBRAND: "+android.os.Build.BRAND
//        +"\nDISPLAY: "+android.os.Build.DISPLAY
//        +"\nBOARD: "+android.os.Build.BOARD
//        +"\nHOST: "+android.os.Build.HOST
//        +"\nMANUFACTURER: "+android.os.Build.MANUFACTURER
//        +"\nPRODUCT: "+android.os.Build.PRODUCT);
//        Log.d(TAG, "$sb")

        // val nsdManager = activity.getSystemService(NSD_SERVICE) as NsdManager
        // lnsService = LnsService(nsdManager)

//        val bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
//         bluetoothController = BluetoothController(context, bluetoothManager, notifier)
//        activity.registerReceiver(bluetoothController.receiver, IntentFilter(
//            BluetoothDevice.ACTION_FOUND)
//        )

//        if (!bluetoothController.isBluetoothEnabled()) {
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            ActivityCompat.startActivityForResult(activity, enableBtIntent, REQUEST_ENABLE_BT, null)
//        }
//
//        Log.d(TAG, "FileShareFragment, start onViewCreated()")
        nameStr.value = "Your name: ${winBle.hostName}"
        connectionManager.setTransmitterName(winBle.hostName)
    }

    override fun config(notifier: NotificationInterface) {
        this.notifier = object: NotificationInterface {
            override suspend fun showProgressDialog(
                title: String,
                cancelCallback: () -> Unit
            ) {
                notifier.showProgressDialog(title, cancelCallback)
            }

            override suspend fun updateProgressDialog(progress: Float) {
                notifier.updateProgressDialog(progress)
            }

            override suspend fun dismissProgressDialog() {
                notifier.dismissProgressDialog()
            }

            override fun showNotification(message: String) {
                notifier.showNotification(message)
            }

            override suspend fun showAlertDialog(
                message: String,
                confirmCallback: () -> Unit,
                dismissCallback: () -> Unit
            ) {
                notifier.showAlertDialog(message, confirmCallback, dismissCallback)
            }

            override fun dismissAlertDialog() {
                notifier.dismissAlertDialog()
            }

            override fun cancelConnection() {
                this@FileShareBlockDesktop.cancelConnection()
            }

            override suspend fun disconnect() {
                this@FileShareBlockDesktop.disconnect()
            }

            override fun onDeviceListUpdate(deviceList: List<DeviceInfoCommon>) {
                println("onDeviceListUpdate, deviceList.size = ${org.example.project.ui.deviceList.size}")
                org.example.project.ui.deviceList.clear()
                deviceList.forEach {
                    org.example.project.ui.deviceList.add(it)
                }
            }
        }

        val bleScanner = object: BleScannerInterface {
            override fun setServiceUuid(uuid: UUID) {
                winBle.setServiceUuid(uuid.toString())
            }

            override fun startScan() {
                winBle.startScanner()
            }

            override fun stopScan() {
                winBle.stopScanner()
            }
        }

        val bleService = object: BleServiceInterface {
            override var referenceData: String
                get() { return winBle.referenceData }
                set(value) { winBle.setReferenceData(value) }
            override var callbackOnReferenceDataReception: ( flag: Boolean, name: String ) -> Unit
                get() { return {_: Boolean, _: String -> } }
                set(value) {
                    winBle.configReferenceDataReceptionCallback {
                        flag, name -> value(flag, name)
                    }
                }

            override fun setServiceUuid(uuid: UUID) {
                winBle.setServiceName(uuid.toString())
            }

            override fun setCharacteristicUuid(uuid: UUID) {
                winBle.setCharacterisitcUuid(uuid.toString())
            }

            override fun startService() {
                winBle.startService()
            }

            override fun stopService() {
                winBle.stopService()
            }
        }

        val bleClient = object: BleClientInterface {
            override var dataToSend: String
                get() { return winBle.dataToSend }
                set(value) { winBle.setDataToSend(value) }
            override var callbackOnDataSend: ( _: Boolean) -> Unit
                get() { return {  _: Boolean -> } }
                set(value) {
                    winBle.configDataSendCallback { flag -> value(flag) }
                }

            override fun setServiceUuid(uuid: UUID) {
                winBle.setServiceName(uuid.toString())
            }

            override fun setCharacteristicUuid(uuid: UUID) {
                winBle.setCharacterisitcUuid(uuid.toString())
            }

            override fun disconnect() {
                winBle.stopScanner()
            }
        }
        super.configureBle(bleScanner, bleService, bleClient)

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

    override fun setMcDnsServiceName(name: String) {
        println("FileShareBlockDesktop, setServiceName(), name = $name")
        mcDnsService.serviceName = name
    }

    override suspend fun registerMcDnsService() {
        mcDnsService.registerService()
    }

    override fun unregisterMcDnsService() {
        mcDnsService.unregisterService()
    }

    override fun connectBleClient(info: DeviceInfoCommon) {
        println("selectedDeviceIndex = $selectedDeviceIndex")
        if (selectedDeviceIndex >= 0) {
            winBle.connectService(selectedDeviceIndex)
        }
    }

    override var getFileDescriptorFromPicker = { files: PlatformFiles? ->
        txFiles.clear()
        if (!files.isNullOrEmpty()) {
            println("$files")

            files.forEach { file ->
//                println("javaClass = ${file.javaClass}")
                println("file = $file")
//                println("path = ${file.path}")

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
    }
}