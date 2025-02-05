package org.example.project

import android.annotation.SuppressLint
import android.app.Activity
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

import io.github.vinceglb.filekit.core.PlatformFiles

import org.example.project.connection.BluetoothController
import org.example.project.connection.GattClient
import org.example.project.connection.GattScanner
import org.example.project.connection.GattServer
import org.example.project.connection.LnsService
import org.example.project.data.DeviceInfoCommon
import org.example.project.utils.TxFileDescriptor
import org.example.project.fragments.FileShareBlockCommon
import org.example.project.fragments.FileSharingRole
import org.example.project.ui.*
import org.example.project.utils.BleClientInterface
import org.example.project.utils.BleScannerInterface
import org.example.project.utils.BleServiceInterface
import java.io.InputStream
import java.util.UUID

class FileShareBlockAndroid (
    private val context: Context,
    private val activity: Activity,
    role: FileSharingRole,
    saveFileDir: String,
) : FileShareBlockCommon(
    role, saveFileDir
) {

    companion object {
        private const val TAG = "FileShareFragment"
    }

    private lateinit var gattClient: GattClient
    private lateinit var gattServer: GattServer
    private lateinit var gattScanner: GattScanner

    private lateinit var lnsService: LnsService

    private var bluetoothManager: BluetoothManager =
        activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    lateinit var bluetoothController: BluetoothController

    @SuppressLint("MissingPermission")
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

        bluetoothController = BluetoothController(context, bluetoothManager, notifier)

        val nsdManager = activity.getSystemService(NSD_SERVICE) as NsdManager
        lnsService = LnsService(nsdManager)

        val name = bluetoothManager.adapter.name
        nameStr.value = "Your name: $name"
        connectionManager.setTransmitterName(name)
    }

    override fun config() {

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

    // override suspend fun registerMcDnsService() {
    override fun registerMcDnsService() {
        lnsService.registerService()
    }

    override fun unregisterMcDnsService() {
        lnsService.unregisterService()
    }

    fun resolveNewIntent(intent: Intent) {
        println("start resolveNewIntent, intent = $intent")
        if (intent.action !in listOf(Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE)){
            return
        }
        println("run geFilePacketDescriptorFromIntent()")
        geFilePacketDescriptorFromIntent(intent)
        gattScanner.startScanPeriodic()
    }

    override fun connectBleClient(info: DeviceInfoCommon) {
        val deviceInfoAndroid = info as DeviceInfoAndroid
        Log.d(TAG, "deviceInfoAndroid = $deviceInfoAndroid")
        gattClient.connect((info).bleScanResult!!)
    }

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
            addFileDescriptor(uri)
        }

        if (txFiles.isNotEmpty()) {
            sendDataButtonIsActive.value = true
            fileStr.value = "file(s) selected"
        }
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
                addFileDescriptor(file.uri)
            }
            fileStr.value = "file(s) selected"
        }
        
        enableBleScannerCallback(true)
        sendDataButtonIsActive.value = true
    }

    @SuppressLint("Range")
    private fun addFileDescriptor(uri: Uri) {
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
}