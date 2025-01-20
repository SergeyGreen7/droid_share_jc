package org.example.project.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.nsd.NsdManager
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log

import androidx.appcompat.app.AppCompatActivity.NSD_SERVICE
import androidx.core.app.ActivityCompat
import com.appstractive.dnssd.DiscoveredService

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.project.connection.BluetoothController
import org.example.project.connection.GattClient
import org.example.project.connection.GattScanner
import org.example.project.connection.GattServer
import org.example.project.connection.LnsController
import org.example.project.connection.mtdns.McDnsScanner
import org.example.project.connection.mtdns.McDnsService
import org.example.project.data.DeviceInfoCommon
import org.example.project.data.NotificationInterface
import org.example.project.grid.DeviceInfo
import org.example.project.data.NsdDataTransceiver
import org.example.project.data.TxFileDescriptor
import org.example.project.data.TxFilePackDescriptor
import org.example.project.ui.*
import java.net.InetAddress

enum class FileSharingRole {
    FILE_TRANSMITTER,
    FILE_RECEIVER
}

class FileShareBlock(
    private val context: Context,
    private val activity: Activity,
    private val role: FileSharingRole
) {

    companion object {
        private const val TAG = "FileShareFragment"
        const val REQUEST_ENABLE_BT = 1
        private const val REFERENCE_DATA = "1234567890"
        private const val FS_SERVICED_NAME_BASE = "fs_service"
    }

    private lateinit var gattClient: GattClient
    private lateinit var gattServer: GattServer
    private lateinit var gattScanner: GattScanner


//    private lateinit var lnsService: LnsService
    private lateinit var mcDnsService: McDnsService
//    private lateinit var lnsScanner: LnsScanner
    private lateinit var mcDnsScanner: McDnsScanner

    // private lateinit var lnsController: LnsController
    lateinit var bluetoothController: BluetoothController

    private lateinit var nsdDataTransceiver: NsdDataTransceiver

    ///
    protected var txFilePackDscr = TxFilePackDescriptor()
    private var uriList = mutableListOf<Uri>()
    protected lateinit var notifier: NotificationInterface
    protected var selectedDeviceInfo: DeviceInfo? = null
    ///

//    private val statusUpdater = object : StatusUpdater {
//        override fun onDeviceInfoUpdate(newDeviceInfo: DeviceInfo?) {
//            selectedDeviceInfo = newDeviceInfo
//
//            if (selectedDeviceInfo == null) {
////                binding.fileShareFragmentBtnSend.isEnabled = false
//                return
//            }
//
//            val info = selectedDeviceInfo as DeviceInfo
////            when (info.type){
//////                InfoType.BLE -> {
//////                    binding.fileShareFragmentBtnSend.isEnabled = true
//////                }
////
////                else -> {
////                    throw Exception("Unknown info type for File Share fragment")
////                }
////            }
//        }
//    }

    fun onCreate() {
        Log.d(TAG, "FileShareFragment, start onCreate()")
        // super.onCreate(savedInstanceState)

        val sb = StringBuilder("MODEL: "+android.os.Build.MODEL
        +"\nDEVICE: "+android.os.Build.DEVICE
        +"\nBRAND: "+android.os.Build.BRAND
        +"\nDISPLAY: "+android.os.Build.DISPLAY
        +"\nBOARD: "+android.os.Build.BOARD
        +"\nHOST: "+android.os.Build.HOST
        +"\nMANUFACTURER: "+android.os.Build.MANUFACTURER
        +"\nPRODUCT: "+android.os.Build.PRODUCT);
        Log.d(TAG, "$sb")

        nsdDataTransceiver = NsdDataTransceiver(notifier,
            Environment.getExternalStorageDirectory().toString() + "/Download/")

        val nsdManager = activity.getSystemService(NSD_SERVICE) as NsdManager
        // lnsController = LnsController(nsdManager, notifier)

        mcDnsService = McDnsService("default-")
        mcDnsScanner = McDnsScanner()

        val bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
         bluetoothController = BluetoothController(context, bluetoothManager, notifier)
//        activity.registerReceiver(bluetoothController.receiver, IntentFilter(
//            BluetoothDevice.ACTION_FOUND)
//        )

        gattClient = GattClient(context, bluetoothManager)
        gattServer = GattServer(context, bluetoothManager)
        gattScanner = GattScanner(bluetoothManager.adapter.bluetoothLeScanner, notifier)

//        if (activity.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//            Log.d(TAG, "current device doesn't support BLE functionality")
//            activity.finish();
//        }

        if (!bluetoothController.isBluetoothEnabled()) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            ActivityCompat.startActivityForResult(activity, enableBtIntent, REQUEST_ENABLE_BT, null)
        }

        Log.d(TAG, "FileShareFragment, start onViewCreated()")

//        gridView = DeviceGridView(requireContext(), requireActivity()
//            .findViewById(R.id.wifi_fragment_recycler_list), statusUpdater, 3)

        when (role) {
            FileSharingRole.FILE_TRANSMITTER -> {
                // fileShareFragmentBtnSend.isEnabled = false
                bleButtonVisible = false
            }

            FileSharingRole.FILE_RECEIVER -> {
                // fileShareFragmentBtnSend.visibility = View.GONE
                bleButtonVisible = true
            }
        }
        nameStr = "Your name: ${bluetoothController.getName()}"

//        with (binding) {
//            fileShareFragmentBtnSend.setOnClickListener {
//                if (selectedDeviceInfo == null) {
//                    return@setOnClickListener
//                }
//
//                bluetoothController.stopBleDiscovery()
//                val info = selectedDeviceInfo as DeviceInfo
//
//                CoroutineScope(Dispatchers.Main).launch {
//                    notifier.showProgressDialog("Sending data", "Sending data 0.00 %") { dialog, _ ->
//                        CoroutineScope(Dispatchers.IO).launch {
//                            Log.d(TAG, "run cancelDataTransmission()")
//                            nsdDataTransceiver.cancelDataTransmission()
//                        }
//                        notifier.showToast("Data transmission is canceled")
//                        dialog.dismiss()
//                    }
//                }
//
//                val serviceName = "$FS_SERVICED_NAME_BASE-${(Math.random() * 10000).toInt().toString()}"
//                gattClient.dataToSend = "$REFERENCE_DATA@$serviceName"
//                gattClient.callbackOnDataSend = { flag:Boolean ->
//                    if (flag) {
//                        notifier.showToast("GATT data is sent")
//                        gattClient.disconnect()
//
//                        lnsScanner.callbackOnRefServiceFind = { serviceInfo: NsdServiceInfo ->
//                            Log.d(TAG, "run callbackOnRefServiceFind")
//                            lnsScanner.stopScan()
//                            CoroutineScope(Dispatchers.IO).launch {
//                                getFilePackDscr()
//                                delay(1000)
//                                nsdDataTransceiver.createSocket(false, serviceInfo, txFilePackDscr)
//                            }
//                        }
//
//                        lnsScanner.scanForService(serviceName)
//                    } else {
//                        notifier.showToast("GATT data is failed to send")
//                    }
//                }
//
//                gattClient.connect(info.bleScanResult!!)
////                    requireActivity().findViewById<View>(R.id.bluetooth_fragment_btn_send_file).isEnabled = true
//            }
//
//            fileShareFragmentBtnCancel.setOnClickListener {
//                bluetoothController.stopBleDiscovery()
//                lnsScanner.stopScan()
//                nsdDataTransceiver.destroySocket()
//                requireActivity().finish()
//            }
//

//        }
    }

    fun config(notifier: NotificationInterface) {
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

            override fun showToast(message: String) {
                notifier.showToast(message)
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
                Log.d(TAG, "FileShareFragment, start cancelConnection(), role = $role")
                nsdDataTransceiver.destroySocket()
                when (role) {
                    FileSharingRole.FILE_TRANSMITTER -> {
                        Log.d(TAG, "run gattScanner.startScanPeriodic()")
                        gattScanner.startScanPeriodic()
                        Log.d(TAG, "mcDnsScanner.stopScan()")
                        mcDnsScanner.stopScan()
                    }
                    FileSharingRole.FILE_RECEIVER -> {
                        Log.d(TAG, "mcDnsService.unregisterService()")
                        mcDnsService.unregisterService()
                    }
                }
                Log.d(TAG, "FileShareFragment, finish cancelConnection()")
            }

            override suspend fun disconnect() {
                Log.d(TAG, "FileShareFragment, disconnect()")
                gattScanner.stopScan()
                nsdDataTransceiver.destroySocket()
                mcDnsService.unregisterService()
                mcDnsScanner.stopScan()
            }

            override fun onDeviceListUpdate(devices: List<DeviceInfoCommon>) {
                Log.d(TAG, "onDeviceListUpdate, deviceList.size = ${deviceList.size}")
                deviceList.clear()
                devices.forEach {
                    deviceList.add(it)
                }
            }

//            override fun onWifiP2pConnection(info: WifiP2pInfo) {
//                // TODO("Not yet implemented")
//            }
        }
    }

    fun resolveActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {

    }

    val setDeviceInfoCommon = { device: DeviceInfoCommon, index: Int -> Unit
        Log.d(TAG,"setDeviceInfo(), selectedDeviceInfoCommon = $device, index = $index")
        selectedDeviceInfo = gattScanner.getDevice(index)
    }

    fun resolveNewIntent(intent: Intent) {
        if (intent.action !in listOf(Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE)){
            return
        }
        getUriFromIntent(intent)
        gattScanner.startScanPeriodic()
    }

    val enableBleServiceCallback = { isEnabled: Boolean ->
        if (isEnabled) {
            gattServer.referenceData = REFERENCE_DATA
            gattServer.callbackOnDataReception = { flag: Boolean, name:String ->
                Log.d(TAG, "start callbackOnDataReception(), flag = $flag")

                if (flag) {
                    mcDnsService.serviceName = name
                    Log.d(TAG, "run registerService(), serviceName = ${mcDnsService.serviceName}")
                    mcDnsService.registerService()

                    CoroutineScope(Dispatchers.IO).launch {
                        nsdDataTransceiver.createSocket(
                            true,
                            mcDnsService.getPort(),
                            InetAddress.getByName("127.0.0.1")
                        )
                    }
                }
            }

            gattServer.startBleService()
        } else {
            gattServer.stopBleService()
            mcDnsService.unregisterService()
        }
    }

    val enableBleScannerCallback = { isEnabled: Boolean ->
        if (isEnabled) {
            gattScanner.startScanPeriodic()
        } else {
            gattScanner.stopScan()
        }
    }

    val sendDataCallback = {
        if (selectedDeviceInfo != null) {

            gattScanner.stopScan()
            val info = selectedDeviceInfo as DeviceInfo

            CoroutineScope(Dispatchers.Main).launch {
                notifier.showProgressDialog("Sending data" ) {
                    CoroutineScope(Dispatchers.IO).launch {
                        Log.d(TAG, "run cancelDataTransmission()")
                        nsdDataTransceiver.cancelDataTransmission()
                    }
                    notifier.showToast("Data transmission is canceled")
                }
            }

            val serviceName = "$FS_SERVICED_NAME_BASE-${(Math.random() * 10000).toInt().toString()}"
            gattClient.dataToSend = "$REFERENCE_DATA@$serviceName"
            gattClient.callbackOnDataSend = { flag: Boolean ->
                if (flag) {
                    notifier.showToast("GATT data is sent")
                    gattClient.disconnect()

                    mcDnsScanner.referenceServiceName = serviceName
                    mcDnsScanner.callbackOnRefServiceFind = { service: DiscoveredService ->
                        Log.d(TAG, "run callbackOnRefServiceFind")
                        mcDnsScanner.stopScan()
                        CoroutineScope(Dispatchers.IO).launch {
                            getFilePackDscr()
                            delay(1000)
                            nsdDataTransceiver.createSocket(
                                false,
                                service.port,
                                InetAddress.getByName(service.addresses[0]),
                                txFilePackDscr)
                        }
                    }

                    Log.d(TAG, "run mcDnsScanner.scanForService(), serviceName = $serviceName")
                    mcDnsScanner.scanForService(serviceName)
                } else {
                    notifier.showToast("GATT data is failed to send")
                }
            }

            gattClient.connect(info.bleScanResult!!)
        }
    }

    private fun getUriFromIntent(intent: Intent) {
        Log.d(TAG, "start resolveIntent()")
        uriList.clear()
        txFilePackDscr.clear()

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
    }

    @SuppressLint("Range", "Recycle")
    private fun getFilePackDscr() {
        txFilePackDscr.clear()
        for (uri in uriList) {
            val cursor = context.contentResolver?.query(uri, null, null, null, null)
            cursor?.moveToFirst()
            val fileName = cursor?.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            val fileSize = cursor?.getString(cursor.getColumnIndex(OpenableColumns.SIZE))?.toInt()
            cursor?.close()
            val inputStream = context.contentResolver?.openInputStream(uri)

            txFilePackDscr.add(TxFileDescriptor(fileName!!, fileSize!!, inputStream!!))
        }
    }
}