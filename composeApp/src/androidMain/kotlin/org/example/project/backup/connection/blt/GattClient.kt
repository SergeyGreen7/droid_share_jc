package org.example.project.connection

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.*
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import org.example.project.connection.GattServer.Companion.DEFAULT_UUID
import java.util.UUID

class GattClient(
    private val context: Context,
) {

    companion object {
        private const val TAG = "GattClient"
    }

    var dataToSend: String = ""
    var callbackOnDataSend : ((flag: Boolean) -> Unit)? = null

    private var isConnected = false
    private var scanResultInternal: ScanResult? = null
    private var connectCounter = 0

    private var gattClient: BluetoothGatt? = null
    private var gattCharacteristics = mutableListOf<BluetoothGattCharacteristic>()

    private var serviceUuid = DEFAULT_UUID
    private var characteristicUuid = DEFAULT_UUID

    private var targetGatt: BluetoothGatt? = null

    // tmp
    private var initialized = false

    private val gattClientCallback = object : BluetoothGattCallback() {
        override fun onPhyUpdate( gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
            Log.d(TAG, "GattClient, BluetoothGattCallback(), onPhyUpdate")
        }

        override fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
            Log.d(TAG, "GattClient, onPhyRead")
        }

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            Log.d(TAG, "GattClient, BluetoothGattCallback(), onConnectionStateChange"
                    + ", status ${GattUtils.getStatus(status)}, newState: $newState")

            // if (status == BluetoothGatt.GATT_SUCCESS) {
                // disconnect()
            // }

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // successfully connected to the GATT Server
                    // broadcastUpdate(ACTION_GATT_CONNECTED)
                    // connectionState = STATE_CONNECTED
                    // Attempts to discover services after successful connection.
                    isConnected = true
                    // gatt?.discoverServices()
//                gatt?.mt
                    gatt?.requestMtu(512)
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // disconnected from the GATT Server
                    // broadcastUpdate(ACTION_GATT_DISCONNECTED)
                    // connectionState = STATE_DISCONNECTED
                    disconnect()
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            Log.d(TAG, "GattClient, onServicesDiscovered(), status = $status")

            if (status != BluetoothGatt.GATT_SUCCESS) {
                tryToConnect()
                return
            }

            if (gatt == null) {
                return
            }

            Log.d(TAG, "GattClient, run getService()")
            val service = gatt.getService(serviceUuid)
            if (service == null) {
                Log.d(TAG,"Could not find service: $serviceUuid")
            } else {
                Log.d(TAG, "GattClient, run getCharacteristic()")
                val characteristic = service.getCharacteristic(characteristicUuid)
                if (characteristic == null) {
                    Log.d(TAG,"Could not find characteristic: ${characteristicUuid}")
                } else {
                    targetGatt = gatt

                    Log.d(TAG, "GattClient, set writeType to 'BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT'")
                    Log.d(TAG, "GattClient, run setCharacteristicNotification()")
                    initialized = gatt.setCharacteristicNotification(characteristic, true)
                    // sendMessage(gatt, "0123456789 Hello, BLE server! ljjiwuefhuiefifhwbefkhbefhjsebfkhsbfhsbfehEfjhse")
                    if (dataToSend.isNotEmpty()) {
                        sendMessage(gatt, dataToSend)
                    }
                }
            }

//            for (s in gatt.getServices()) {
//                Log.i(TAG, "Service: $s")
//                    for (c in s.characteristics) {
//                    Log.i(TAG, "    Characteristic: $c")
//                    if (hasPermission(c, PERMISSION_READ)) {
//                        gattCharacteristics.add(c)
//                    }
//                }
//            }

            // tmp
//            if (gattCharacteristics.isNotEmpty()) {
//                gatt.readCharacteristic(gattCharacteristics[0])
//                gattCharacteristics.removeAt(0)
//            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            // onCharacteristicRead(gatt, characteristic, status)
            Log.d(TAG, "GattClient, onCharacteristicRead")
            Log.d(TAG,"characteristic: $characteristic")
//            if (gattCharacteristics.isNotEmpty()) {
//                gatt?.readCharacteristic(gattCharacteristics[0])
//                gattCharacteristics.removeAt(0)
//            }
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            // onCharacteristicRead(gatt, characteristic, status)
            Log.d(TAG, "GattClient, onCharacteristicRead")
            Log.d(TAG,"characteristic: $characteristic")
            if (gattCharacteristics.isNotEmpty()) {
                gatt.readCharacteristic(gattCharacteristics[0])
                gattCharacteristics.removeAt(0)
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            Log.d(TAG, "GattClient, onCharacteristicWrite()")
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?
        ) {
            Log.d(TAG, "GattClient, onCharacteristicChanged() *DEPRECATED")
            val bytes = characteristic?.value
            val message = bytes?.toString(Charsets.UTF_8)
            Log.d(TAG, "updated message = '$message'")
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            Log.d(TAG, "GattClient, onCharacteristicChanged()")
            onCharacteristicChanged(gatt, characteristic)
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int
        ) {
            Log.d(TAG, "GattClient, onDescriptorRead() *DEPRECATED")
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int,
            value: ByteArray
        ) {
            Log.d(TAG, "GattClient, onDescriptorRead()")
            onDescriptorRead(gatt, descriptor, status)
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int
        ) {
            Log.d(TAG, "GattClient, onDescriptorWrite()")
        }

        override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
            Log.d(TAG, "GattClient, onReliableWriteCompleted()")
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            Log.d(TAG, "GattClient, onReadRemoteRssi()")
        }

        @SuppressLint("MissingPermission")
        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            Log.d(TAG, "GattClient, onMtuChanged(), status = ${GattUtils.getStatus(status)}")
            Log.d(TAG, "mtu = $mtu")
            gatt?.discoverServices()
        }

        fun onConnectionUpdated(
            gatt: BluetoothGatt?,
            interval: Int,
            latency: Int,
            timeout: Int,
            status: Int
        ) {
            Log.d(TAG, "GattClient, onConnectionUpdated()")
        }

        override fun onServiceChanged(gatt: BluetoothGatt) {
            Log.d(TAG, "GattClient, onServiceChanged()")
        }

        fun onSubrateChange(
            gatt: BluetoothGatt?,
            subrateFactor: Int,
            latency: Int,
            contNum: Int,
            timeout: Int,
            status: Int
        ) {
            Log.d(TAG, "GattClient, onSubrateChange()")
        }
    }

    fun setServiceUuid(uuid: UUID) {
        serviceUuid = uuid
    }

    fun setCharacteristicUuid(uuid: UUID) {
        characteristicUuid = uuid
    }

    fun connect(scanResult: ScanResult) {
        Log.d(TAG, "GattClient, connect()")
        scanResultInternal = scanResult
        tryToConnect()
//        gattClient = scanResultInternal?.device?.connectGatt(
//            context, false, gattClientCallback, BluetoothDevice.TRANSPORT_LE)
//        Log.d(TAG, "gatt: $gattClient")
    }

    @SuppressLint("MissingPermission")
    private fun tryToConnect() {
        if (connectCounter > 10) {
            disconnect()
        }

        Log.d(TAG, "GattClient, tryToConnect()")
        if (scanResultInternal == null) {
            return
        }
        gattClient = scanResultInternal!!.device.connectGatt(
            context, false, gattClientCallback, BluetoothDevice.TRANSPORT_LE)
        Log.d(TAG, "gatt: $gattClient")
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        Log.d(TAG, "GattClient, disconnect()")
        isConnected = false
        if (gattClient != null) {
            gattClient?.disconnect()
            gattClient?.close()
            scanResultInternal = null
            connectCounter = 0
            targetGatt = null
        }
    }

    fun sendMessage(message: String) {
        println("start sendMessage()")
        if (targetGatt == null) {
            println("sendMessage(), no target GATT for message sending")
            return
        }
        sendMessage(targetGatt!!, message)
    }

    private fun hasPermission(c: BluetoothGattCharacteristic, permission: Int): Boolean {
        return c.permissions and permission != 0
    }

    @SuppressLint("MissingPermission")
    private fun sendMessage(gatt: BluetoothGatt, message: String) {
        Log.d(TAG, "send message : '$message")
        val service = gatt.getService(serviceUuid)
        if (service == null) {
            Log.d(TAG, "service is null")
            return
        }
        val characteristic = service.getCharacteristic(characteristicUuid)
        if (characteristic == null) {
            Log.d(TAG, "characteristic is null")
            return
        }
        val bytes = message.toByteArray(Charsets.UTF_8)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val result = gatt.writeCharacteristic(characteristic, bytes, WRITE_TYPE_DEFAULT)
            if (result != BluetoothStatusCodes.SUCCESS
            ) {
                Log.d(TAG, "GattClient, sendMessage(), failed to write data")
                callbackOnDataSend?.invoke(false)
            } else {
                callbackOnDataSend?.invoke(true)
            }
        } else {
            characteristic.writeType = WRITE_TYPE_DEFAULT
            characteristic.value = bytes
            if (!gatt.writeCharacteristic(characteristic)) {
                Log.d(TAG, "GattClient, sendMessage(), failed to write data")
                callbackOnDataSend?.invoke(false)
            } else {
                callbackOnDataSend?.invoke(true)
            }
        }

    }
}