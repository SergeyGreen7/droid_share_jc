package org.example.project

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Context.NSD_SERVICE
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.net.nsd.NsdManager
import android.net.wifi.WifiManager
import android.provider.OpenableColumns
import android.util.Log

import androidx.core.app.ActivityCompat
import com.darkrockstudios.libraries.mpfilepicker.MPFile

import org.example.project.connection.BluetoothController
import org.example.project.connection.GattClient
import org.example.project.connection.GattScanner
import org.example.project.connection.GattServer
import org.example.project.connection.LnsService
import org.example.project.data.DeviceInfoCommon
import org.example.project.utils.NotificationInterface
import org.example.project.utils.TxFileDescriptor
import org.example.project.fragments.FileShareBlockCommon
import org.example.project.fragments.FileSharingRole
import org.example.project.ui.*
import org.example.project.utils.BleClientInterface
import org.example.project.utils.BleScannerInterface
import org.example.project.utils.BleServiceInterface
import java.io.InputStream
import java.util.UUID

open class FileShareBlockAndroid (
    private val context: Context,
    private val activity: Activity,
    role: FileSharingRole,
    saveFileDir: String,
) : FileShareBlockCommon(
    role, saveFileDir
) {

    companion object {
        private const val TAG = "FileShareFragment"
        const val REQUEST_ENABLE_BT = 1
    }

    private lateinit var gattClient: GattClient
    private lateinit var gattServer: GattServer
    private lateinit var gattScanner: GattScanner

    private lateinit var lnsService: LnsService

    lateinit var bluetoothController: BluetoothController

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "FileShareFragment, start onCreate()")

        val sb = StringBuilder("MODEL: "+android.os.Build.MODEL
        +"\nDEVICE: "+android.os.Build.DEVICE
        +"\nBRAND: "+android.os.Build.BRAND
        +"\nDISPLAY: "+android.os.Build.DISPLAY
        +"\nBOARD: "+android.os.Build.BOARD
        +"\nHOST: "+android.os.Build.HOST
        +"\nMANUFACTURER: "+android.os.Build.MANUFACTURER
        +"\nPRODUCT: "+android.os.Build.PRODUCT)
        Log.d(TAG, "$sb")

        val nsdManager = activity.getSystemService(NSD_SERVICE) as NsdManager
        lnsService = LnsService(nsdManager)

        val bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
         bluetoothController = BluetoothController(context, bluetoothManager, notifier)
//        activity.registerReceiver(bluetoothController.receiver, IntentFilter(
//            BluetoothDevice.ACTION_FOUND)
//        )

        if (!bluetoothController.isBluetoothEnabled()) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            ActivityCompat.startActivityForResult(activity, enableBtIntent, REQUEST_ENABLE_BT, null)
        }

        Log.d(TAG, "FileShareFragment, start onViewCreated()")
        nameStr.value = "Your name: ${bluetoothController.getName()}"
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
                this@FileShareBlockAndroid.cancelConnection()
            }

            override suspend fun disconnect() {
                this@FileShareBlockAndroid.disconnect()
            }

            override fun onDeviceListUpdate(deviceList: List<DeviceInfoCommon>) {
                Log.d(TAG, "onDeviceListUpdate, deviceList.size = ${org.example.project.ui.deviceList.size}")
                org.example.project.ui.deviceList.clear()
                deviceList.forEach {
                    org.example.project.ui.deviceList.add(it)
                }
            }
        }

        val bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        gattScanner = GattScanner(bluetoothManager.adapter.bluetoothLeScanner, this.notifier)
        val bleScanner = object: BleScannerInterface {
            override fun setServiceUuid(uuid: UUID) {
                gattScanner.setServiceUuid(uuid)
            }

            override fun startScan() {
                gattScanner.startScanPeriodic()
            }

            override fun stopScan() {
                gattScanner.stopScan()
            }
        }

        gattServer = GattServer(context, bluetoothManager)
        val bleService = object: BleServiceInterface {
            override var referenceData: String
                get() { return gattServer.referenceData }
                set(value) { gattServer.referenceData = value }
            override var callbackOnReferenceDataReception: ( flag: Boolean, name: String ) -> Unit
                get() { return {_: Boolean, _: String -> } }
                set(value) { gattServer.callbackOnReferenceDataReception = value }

            override fun setServiceUuid(uuid: UUID) {
                gattServer.setServiceUuid(uuid)
            }

            override fun setCharacteristicUuid(uuid: UUID) {
                gattServer.setCharacteristicUuid(uuid)
            }

            override fun startService() {
                gattServer.startBleService()
            }

            override fun stopService() {
                gattServer.stopBleService()
            }
        }

        gattClient = GattClient(context, bluetoothManager)
        val bleClient = object: BleClientInterface {
            override var dataToSend: String
                get() { return gattClient.dataToSend }
                set(value) { gattClient.dataToSend = value }
            override var callbackOnDataSend: ( _: Boolean) -> Unit
                get() { return {  _: Boolean -> } }
                set(value) { gattClient.callbackOnDataSend = value }

            override fun setServiceUuid(uuid: UUID) {
                gattClient.setServiceUuid(uuid)
            }

            override fun setCharacteristicUuid(uuid: UUID) {
                gattClient.setCharacteristicUuid(uuid)
            }

            override fun disconnect() {
                gattClient.disconnect()
            }
        }
        super.configureBle(bleScanner, bleService, bleClient)

        val wifi = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val multicastLock = wifi.createMulticastLock("multicastLock")
        multicastLock.setReferenceCounted(true)
        multicastLock.acquire()
    }

    override val setDeviceInfoCommon = { device: DeviceInfoCommon, index: Int ->
        Log.d(TAG,"setDeviceInfo(), selectedDeviceInfoCommon = $device, index = $index")
        selectedDeviceInfo = gattScanner.getDevice(index)
    }

    override fun setMcDnsServiceName(name: String) {
        lnsService.serviceName = name
    }

    override suspend fun registerMcDnsService() {
        lnsService.registerService()
    }

    override fun unregisterMcDnsService() {
        lnsService.unregisterService()
    }

    fun resolveNewIntent(intent: Intent) {
        if (intent.action !in listOf(Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE)){
            return
        }
        geFilePacketDescriptorFromIntent(intent)
        gattScanner.startScanPeriodic()
    }

    override fun connectBleClient(info: DeviceInfoCommon) {
        val deviceInfoAndroid = info as DeviceInfoAndroid
        Log.d(TAG, "deviceInfoAndroid = $deviceInfoAndroid")
        gattClient.connect((info).bleScanResult!!)
    }

    @SuppressLint("Range")
    private fun geFilePacketDescriptorFromIntent(intent: Intent) {
        Log.d(TAG, "start resolveIntent()")
        val uriList = mutableListOf<Uri>()

        if (intent.action !in listOf(Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE)){
            return
        }
        Log.d(TAG, "ACTION_SEND")

        if (intent.clipData != null) {
            for (i in 0..<intent.clipData!!.itemCount) {
                val uri = intent.clipData!!.getItemAt(i).uri
                Log.d(TAG, "multiple URIs: = $uri")
                uriList.add(uri)
            }
        } else {
            if (intent.data != null) {
                Log.d(TAG, "single URI: = ${intent.data}")
                uriList.add(intent.data!!)
            }
        }

        txFiles.clear()
        for (uri in uriList) {
            val cursor = context.contentResolver
                ?.query(uri, null, null, null, null)  as Cursor
            cursor.moveToFirst()
            val fileName = cursor.getString(
                cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)) as String
            val fileSize = cursor.getString(
                cursor.getColumnIndex(OpenableColumns.SIZE))?.toInt() as Int
            cursor.close()
            val inputStream = context.contentResolver?.openInputStream(uri) as InputStream

            txFiles.add(TxFileDescriptor(fileName, fileSize, inputStream))
        }
    }

    @SuppressLint("Range")
    override var getFileDescriptorFromPicker = { files: List<MPFile<Any>>? ->
        txFiles.clear()
        if (!files.isNullOrEmpty()) {
            println("$files")

            files.forEach { file ->
                println("javaClass = ${file.javaClass}")
                println("platformFile = ${file.platformFile}")
                println("path = ${file.path}")

                val uri = file.platformFile as Uri
                val cursor = context.contentResolver?.
                    query(uri, null, null, null, null) as Cursor
                cursor.moveToFirst()
                val fileName = cursor.getString(
                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                val fileSize = cursor.getString(
                    cursor.getColumnIndex(OpenableColumns.SIZE))?.toInt() as Int
                cursor.close()
                val inputStream = context.contentResolver?.openInputStream(uri) as InputStream

                println("fileName = $fileName")
                println("fileSize = $fileSize")
                println("inputStream = $inputStream")
                txFiles.add(TxFileDescriptor(fileName, fileSize, inputStream))
            }
            fileStr.value = "file(s) selected"
        }
    }
}