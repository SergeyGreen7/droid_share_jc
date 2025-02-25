package org.example.project.connection.gatt

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class GattServer(
    private val context: Context,
    private val manager: BluetoothManager,
) {
    companion object {
        private const val TAG = "GattServer"
        val DEFAULT_UUID: UUID = UUID.fromString("aaaabbbb-aabb-aabb-aabb-aaaabbbbaaaa")
    }

    private var isActive = false
    private var isConnected = false
    private var advertiser: BluetoothLeAdvertiser? = null
    private var gattServer: BluetoothGattServer? = null
    private var devices: MutableList<BluetoothDevice>
    private var pseudoRandomData: ByteArray

    private var serviceUuid = DEFAULT_UUID
    private var characteristicUuid = DEFAULT_UUID

    var createServerCommand = ""
    var destroyServerCommand = ""
    var createServerCallback: ((name:String) -> Unit)? = null
    var destroyServerCallback: (() -> Unit)? = null

    init {
        if (manager.adapter.isEnabled) {
            advertiser = manager.adapter.bluetoothLeAdvertiser
            advertiser
        }
        devices = mutableListOf()

        pseudoRandomData = ByteArray(8)
        Random.nextBytes(pseudoRandomData, 0, 7)
        Log.d(TAG, "pseudoRandomData.size = ${pseudoRandomData.size}")
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.d(
                TAG, "GattServer, AdvertiseCallback-onStartSuccess(), " +
                        "Peripheral advertising started."
            )
        }
        override fun onStartFailure(errorCode: Int) {
            Log.d(
                TAG, "GattServer, onStartFailure(), " +
                        "Peripheral advertising failed: $errorCode - " +
                        " ${GattUtils.getAdvertiseFailure(errorCode)}"
            )
        }
    }

    fun setServiceUuid(uuid: UUID) {
        serviceUuid = uuid
    }

    fun setCharacteristicUuid(uuid: UUID) {
        characteristicUuid = uuid
    }

    fun startBleService() {
        if (!isActive) {
            isActive = true

            setupServer()
            startAdvertising()
        }
    }

    @SuppressLint("MissingPermission")
    fun stopBleService() {
        Log.d(TAG, "GattClient, start stopBleService")
        if (isActive) {
            Log.d(TAG, "    do stopBleService")
            isActive = false
            isConnected = false
            gattServer?.clearServices()
            gattServer?.close()
            advertiser?.stopAdvertising(advertiseCallback)
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupServer() {
        gattServer = manager.openGattServer(context, GattServerCallback())
        gattServer?.clearServices()

        val service = BluetoothGattService(
            serviceUuid,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )

        val serviceConfigCharacteristic = BluetoothGattCharacteristic(
            characteristicUuid,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(serviceConfigCharacteristic)

//        val connectStateCharacteristic = BluetoothGattCharacteristic(
//            GATT_CHARACTER_CONNECTION_STATE_UUID,
//            BluetoothGattCharacteristic.PROPERTY_WRITE,
//            BluetoothGattCharacteristic.PERMISSION_WRITE)
//        service.addCharacteristic(connectStateCharacteristic)

         gattServer?.addService(service)
    }

    @SuppressLint("MissingPermission")
    private fun startAdvertising() {
        if (advertiser == null) {
            throw Exception("GattServer, advertiser is not created")
        }
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()

        val parcelUuid = ParcelUuid(serviceUuid)
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(parcelUuid)
            // .addManufacturerData(0, pseudoRandomData)
//            .addTransportDiscoveryData()
//            .addServiceData(parcelUuid, pseudoRandomData)
//            .addManufacturerData(0, "0123456789".toByteArray(Charsets.US_ASCII))
            .build()

        for (d in data.serviceData) {
            Log.d(TAG, "${d.key} = ${d.value.size}")
        }

        advertiser?.startAdvertising(settings, data, null, advertiseCallback)
    }

    private inner class GattServerCallback : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(
            device: BluetoothDevice?,
            status: Int,
            newState: Int
        ) {
            super.onConnectionStateChange(device, status, newState)

            Log.d(
                TAG, "GattServer, onConnectionStateChange(), " +
                        "device: $device, status = ${GattUtils.getStatus(status)}, newState = $newState"
            )
            if (device == null) {
                return
            }

            if (status == BluetoothGatt.GATT_FAILURE) {
                stopBleService()
                return
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                stopBleService()
                return
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                isConnected = true
                devices.add(device)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                devices.remove(device)
            }
        }

        override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
            Log.d(TAG, "GattServer, onServiceAdded()")
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
            Log.d(TAG, "GattServer, onCharacteristicReadRequest() *DEPRECATED")
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            Log.d(TAG, "GattServer, onCharacteristicWriteRequest()")
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)

            if (characteristic == null || value == null) {
                return
            }

            Log.d(TAG, "value.size = ${value.size}")

            if (characteristic.uuid == characteristicUuid) {
                Log.d(TAG, "GattServer, onCharacteristicWriteRequest, sendResponse")
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                val length = value.size
                val reversed = ByteArray(length)
                for (i in 0..<length) {
                    reversed[i] = value[length - (i+1)]
                }

                val message = value.toString(Charsets.UTF_8)
                Log.d(TAG, "received message: '$message'")

                val prefix =  message.substringBefore("@")
                Log.d(TAG, "prefix = $prefix")

                CoroutineScope(Dispatchers.IO).launch {
                    if (prefix == createServerCommand) {
                        val serviceName = message.substringAfter("@")

                        Log.d(TAG, "serviceName = $serviceName")
                        createServerCallback?.invoke(serviceName)
                    } else if (prefix == destroyServerCommand) {
                        destroyServerCallback?.invoke()
                    }
                }

                Log.d(TAG, "GattServer, onCharacteristicWriteRequest, notifyCharacteristicChanged")
                for (d in devices) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        gattServer?.notifyCharacteristicChanged(d, characteristic, false, reversed)
                    } else {
                        characteristic.value = reversed
                        gattServer?.notifyCharacteristicChanged(d, characteristic, false)
                    }
                }
            }
        }

        override fun onDescriptorReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            descriptor: BluetoothGattDescriptor?
        ) {
            Log.d(TAG, "GattServer, onDescriptorReadRequest()")
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            descriptor: BluetoothGattDescriptor?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            Log.d(TAG, "GattServer, onDescriptorWriteRequest()")
        }

        override fun onExecuteWrite(device: BluetoothDevice?, requestId: Int, execute: Boolean) {
            Log.d(TAG, "GattServer, onExecuteWrite()")
        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            Log.d(TAG, "GattServer, onNotificationSent()")
        }

        override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
            Log.d(TAG, "GattServer, onMtuChanged()")
        }

        override fun onPhyUpdate(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
            Log.d(TAG, "GattServer, onPhyUpdate()")
        }

        override fun onPhyRead(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
            Log.d(TAG, "GattServer, onPhyRead()")
        }

        fun onConnectionUpdated(
            device: BluetoothDevice?, interval: Int, latency: Int, timeout: Int, status: Int
        ) {
            Log.d(TAG, "GattServer, onConnectionUpdated(), device: $device status = $status")
        }

        fun onSubrateChange(
            device: BluetoothDevice?,
            subrateFactor: Int,
            latency: Int,
            contNum: Int,
            timeout: Int,
            status: Int
        ) {
        }
    }
}