package org.example.project.connection.gatt

import android.annotation.SuppressLint
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.project.DeviceInfoAndroid
import org.example.project.connection.gatt.GattServer.Companion.DEFAULT_UUID
import org.example.project.data.DeviceInfoCommon
import java.util.UUID

class GattScanner (
    private val scanner: BluetoothLeScanner,
) {

    companion object {
        private const val TAG = "GattScanner"
        private val BLE_SCAN_PERIOD_SINGLE = 5000L
        private val BLE_SCAN_PERIOD_MULTIPLE = 1000L
        private val LIST_UPDATE_TIME = 5000L
        private val NUM_SCAN_PERIODS = 1000
    }

    var onDeviceListUpdate = { _: List<DeviceInfoCommon> -> Unit}

    private var isActive = false
    private var serviceUuid = DEFAULT_UUID
    private var stopTimer: Job? = null
    private var periodicScan: Job? = null
    private var devices = HashMap<String, ScanResult>()

    private val scanCallback: ScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            if (result == null) {
                return
            }

            Log.d(TAG, "onScanResult: ${result}")
            if (result.device.name != null) {
                Log.d(TAG, "GattScanner, onScanResult()")
                Log.d(TAG, "    result: $result")
                devices[result.device.address] = result
                Log.d(TAG, "    devices.size = ${devices.size}")
            }

//            gridUpdater.onDeviceListUpdate(devices.map{ DeviceInfo(it.value) })
        }

        @SuppressLint("MissingPermission")
        override fun onBatchScanResults(results: List<ScanResult?>?) {
            Log.d(TAG, "GattScanner, onBatchScanResults()")
            if (results == null) {
                return
            }

            for (result in results) {
                if (result == null) {
                    continue
                }
                Log.d(TAG, "onBatchScanResults: ${result}")
                if (result.device.name != null) {
                    devices[result.device.address] = result
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.d(TAG, "GattScanner, onScanFailed(), errorCode = $errorCode")
        }
//        override fun onScanResult(callbackType: Int, result: ScanResult) {
//            super.onScanResult(callbackType, result)
//            // leDeviceListAdapter.addDevice(result.device)
//            // leDeviceListAdapter.notifyDataSetChanged()
//        }
    }

    fun setServiceUuid(uuid: UUID) {
        serviceUuid = uuid
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        Log.d(TAG, "GattScanner, startScan()")
        if (!isActive) {

            devices.clear()
            showDiscoveredDevices()

            configureAndRunScanner()
            isActive = true

            stopTimer = CoroutineScope(Dispatchers.IO).launch {
                delay(BLE_SCAN_PERIOD_SINGLE)
                stopScan()
            }
//            CoroutineScope(Dispatchers.IO).launch {
//                notifier.showNotification("BLE services discovery started. " +
//                        "Please wait for ${BLE_SCAN_PERIOD_SINGLE/1000} seconds ")
//            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startScanPeriodic() {
        Log.d(TAG, "GattScanner, startScanPeriodic()")
        if (!isActive) {

            devices.clear()

            configureAndRunScanner()
            isActive = true

            periodicScan = CoroutineScope(Dispatchers.IO).launch {
                var cntr = 0
                for (i in 0..NUM_SCAN_PERIODS) {
                    delay(BLE_SCAN_PERIOD_MULTIPLE)
                    Log.d(
                        TAG, "startScanPeriodic, show found BLE nodes, " +
                                "devices.size = ${devices.size}, cntr = $cntr"
                    )
                    if (cntr++ % 5 == 0) {
                        withContext(Dispatchers.Main) {
                            Log.d(TAG, "startScanPeriodic, run onDeviceListUpdate()")
                            onDeviceListUpdate(devices.map { DeviceInfoAndroid(it.value).toCommon() })
                            devices.clear()
                        }
                    }
                }
            }
//            CoroutineScope(Dispatchers.IO).launch {
//                notifier.showToast("BLE services discovery started. " +
//                        "Please wait for ${BLE_SCAN_PERIOD/1000} seconds ")
//            }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        Log.d(TAG, "GattScanner, stopScan()")
        stopTimer?.cancel()
        periodicScan?.cancel()

        if (isActive) {
            scanner.stopScan(scanCallback)
            isActive = false
        }
        showDiscoveredDevices()
    }

    @SuppressLint("MissingPermission")
    private fun configureAndRunScanner() {
        println("start configureAndRunScanner(), serviceUuid = $serviceUuid")
        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(serviceUuid))
            .build()
        val filters = mutableListOf<ScanFilter>(scanFilter)
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        scanner.startScan(filters, settings, scanCallback)
    }

    private fun showDiscoveredDevices() {
        CoroutineScope(Dispatchers.Main).launch {
            onDeviceListUpdate(devices.map{ DeviceInfoAndroid(it.value).toCommon() })
        }
    }

    fun getDevice(index: Int): DeviceInfoAndroid? {
        return if (index < devices.size) {
            devices.map { DeviceInfoAndroid(it.value) }[index]
        } else {
            null
        }
    }

}