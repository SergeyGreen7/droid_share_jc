package org.example.project.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.InputStream
import java.io.OutputStream

class BluetoothClientServer {

    companion object {
        private const val TAG = "BluetoothClientServer"

        private const val BLUETOOTH_SERVER_NAME = "ns220re_bluetooth_server"
    }
    private var client: BluetoothSocket? = null
    private var server: BluetoothServerSocket? = null

    fun shutdown() {
        if (server != null) {
            try {
                server?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Could not close the server socket, $e")
            }
        }
        if (client != null) {
            try {
                client!!.close()
            } catch (e: Exception) {
                Log.e(TAG, "Could not close the client socket, $e")
            }
        }
    }

    fun getInputStream(): InputStream {
        return client!!.inputStream
    }

    fun getOutputStream(): OutputStream {
        return client!!.outputStream
    }

    fun isClientConnected(): Boolean {
        return client != null && client!!.isConnected
    }

    fun runServer(bluetoothServerSocket: BluetoothServerSocket) {
        server = bluetoothServerSocket
        try {
            client = server?.accept()
            Log.d(TAG,"bluetooth server connection done")
        } catch (e: Exception) {
            Log.d(TAG, "BluetoothClientServer, runServer, exception happened: $e")
        }
    }

    @SuppressLint("MissingPermission")
    fun runClient(bluetoothSocket: BluetoothSocket) {
        try {
            client = bluetoothSocket
            Log.d(TAG,"run connect")
            client!!.connect()
            Log.d(TAG,"bluetooth client connection done")
        } catch (e: Exception) {
            Log.e(TAG, "Client socket's connect method failed, $e")
        }
    }

}