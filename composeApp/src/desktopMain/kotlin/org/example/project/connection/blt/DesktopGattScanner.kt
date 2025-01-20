package org.example.project.connection.blt

import com.welie.blessed.BluetoothCentralManager
import com.welie.blessed.BluetoothCentralManagerCallback
import com.welie.blessed.BluetoothCommandStatus
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.ScanResult
import java.util.UUID


class DesktopGattScanner {

     val bluetoothCentralManagerCallback = object: BluetoothCentralManagerCallback() {

            /**
             * Successfully connected with the device.
             *
             * @param peripheral the peripheral that was connected.
             */
            override fun onConnectedPeripheral(peripheral: BluetoothPeripheral) {
                println("bluetoothCentralManagerCallback, onConnectedPeripheral")
            }

            /**
             * Connecting with the device has failed.
             *
             * @param peripheral the peripheral for which the connection was attempted
             * @param status the status code for the connection failure
             */
            override fun onConnectionFailed(
                peripheral: BluetoothPeripheral,
                status: BluetoothCommandStatus
            ) {
                println("bluetoothCentralManagerCallback, onConnectionFailed")
            }

            /**
             * Device disconnected
             *
             * @param peripheral the peripheral that disconnected.
             * @param status the status code for the disconnection
             */
            override fun onDisconnectedPeripheral(
                peripheral: BluetoothPeripheral,
                status: BluetoothCommandStatus
            ) {
                println("bluetoothCentralManagerCallback, onDisconnectedPeripheral")
            }

            /**
             * Discovered a peripheral
             *
             * @param peripheral the peripheral that was found
             * @param scanResult the scanResult describing the peripheral
             */
            override fun onDiscoveredPeripheral(
                peripheral: BluetoothPeripheral,
                scanResult: ScanResult
            ) {
                println("bluetoothCentralManagerCallback, onDiscoveredPeripheral")
            }

            /**
             * A scan has started
             */
            override fun onScanStarted() {
                println("bluetoothCentralManagerCallback, onScanStarted")
            }

            /**
             * A scan has stopped
             */
            override fun onScanStopped() {
                println("bluetoothCentralManagerCallback, onScanStopped")
            }

            /**
             * Scanning failed
             *
             * @param errorCode the status code for the scanning failure
             */
            override fun onScanFailed(errorCode: Int) {
                println("bluetoothCentralManagerCallback, onScanFailed")
            }

            override fun onPinRequest(peripheral: BluetoothPeripheral): String {
                println("bluetoothCentralManagerCallback, onPinRequest")
                return ""
            }
//        Override void onDiscoveredPeripheral(BluetoothPeripheral peripheral, ScanResult scanResult) {
//            central.stopScan();
//            central.connectPeripheral(peripheral, peripheralCallback);
//        }
    };

    private val central = BluetoothCentralManager(bluetoothCentralManagerCallback);

    fun startScanner() {

        // Create BluetoothCentralManager


        // Define blood pressure service UUID
        val BLOODPRESSURE_SERVICE_UUID = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb");

        // Scan for peripherals with a certain service UUID
        // central.scanForPeripheralsWithServices(arrayOf<UUID>(BLOODPRESSURE_SERVICE_UUID));
        central.scanForPeripherals()

    }

    fun stopScanner() {
        central.stopScan()
    }
}