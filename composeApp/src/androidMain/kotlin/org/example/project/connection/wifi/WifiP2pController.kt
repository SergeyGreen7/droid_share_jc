package org.example.project.connection

import android.annotation.SuppressLint
import android.net.MacAddress
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.*
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
//import org.example.project.NotificationInterface
import org.example.project.Utils
import org.example.project.connection.WifiDirectServiceScanner.Companion
import org.example.project.connection.wifi.WifiP2pConnector
import org.example.project.data.NotificationInterface

class WifiP2pController(
    private val manager: WifiP2pManager,
    private val channel: Channel,
    private val notifier: NotificationInterface,
)
{

    companion object {
        private const val TAG = "WifiP2pController"
//        private const val SERVICE_TYPE = "_wifip2p._tcp"
    }

    private val SERVER_PORT = 8888;

    val peerScanner = WifiDirectPeerScanner(manager, channel, notifier)
    private val serviceScanner = WifiDirectServiceScanner(manager, channel, notifier)
    private val service = WifiDirectService(manager, channel)
    private val connector = WifiP2pConnector(notifier)

    private var isWifiP2pEnabled = false
    private val buddies = mutableMapOf<String, String>()

    fun registerP2pService() {
        service.registerP2pService()
    }

    fun unregisterP2pService() {
        service.unregisterP2pService()
    }

    fun startDiscoverP2pService() {
        serviceScanner.startScan()
    }

    fun stopDiscoverP2pServices() {
        serviceScanner.stopScan()
    }

    @SuppressLint("MissingPermission")
    fun startDiscoverP2pPeers() {
        peerScanner.startScan()
    }

    fun stopDiscoverP2pPeers() {
        peerScanner.stopScan()
    }

    @SuppressLint("MissingPermission")
    fun requestPeers(listener: PeerListListener) {
        manager.requestPeers(channel, listener)
    }

    @SuppressLint("MissingPermission")
    fun connectP2pDevice(device: WifiP2pDevice, deviceAddress: String, name: String) {
        Log.d(TAG, "start connectP2pDevice()")
        val config = WifiP2pConfig()
        config.deviceAddress = deviceAddress
        config.wps.setup = WpsInfo.PBC
        config.groupOwnerIntent = 0

        if (device.status == WifiP2pDevice.INVITED ||
            device.status == WifiP2pDevice.AVAILABLE) {
            cancelP2pConnection()
        } else if (device.status == WifiP2pDevice.CONNECTED) {
            manager.requestGroupInfo(channel) { group ->
                if (group != null) {
                    manager.removeGroup(channel, object : ActionListener {
                        override fun onSuccess() {
                            Log.d(TAG, "WifiP2pController, removeGroup - onSuccess()")
                            connectP2pDevice(device, deviceAddress, name)
                        }
                        override fun onFailure(reason: Int) {
                            Log.d(TAG, "WifiP2pController, removeGroup - onFailure() - ${WifiUtils.getErrorCodeDescription(reason)}")
                        }
                    })
                }
            }
            return
        }

        manager.connect(channel, config, object : ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "WifiP2pController, connect(), onSuccess")
            }
            override fun onFailure(code: Int) {
                Log.d(TAG, "connect(), onFailure, ${WifiUtils.getErrorCodeDescription(code)}")
            }
        })
    }

    @SuppressLint("MissingPermission")
    fun disconnect(device: WifiP2pDevice?) {
        Log.d(TAG, "WifiP2pController, disconnect(), device.status = ${device?.status}")
        if (device == null) {
            return
        }

        if (
            device.status == WifiP2pDevice.CONNECTED ||
            device.status == WifiP2pDevice.UNAVAILABLE ||
            device.status == WifiP2pDevice.FAILED){
            cancelP2pConnection()
            manager.requestGroupInfo(channel) { group ->
                if (group != null) {
                    manager.removeGroup(channel, object : ActionListener {
                        override fun onSuccess() {
                            Log.d(TAG, "WifiP2pController, removeGroup - onSuccess()")
                        }
                        override fun onFailure(reason: Int) {
                            Log.d(TAG, "WifiP2pController, removeGroup - onFailure() - ${WifiUtils.getErrorCodeDescription(reason)}")
                        }
                    })
                }
            }
        } else if (
            device.status == WifiP2pDevice.INVITED ||
            device.status == WifiP2pDevice.AVAILABLE) {
            cancelP2pConnection()
        }
    }

    private fun cancelP2pConnection() {
        manager.cancelConnect(channel, object : ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "cancelConnect(), onSuccess")
            }
            override fun onFailure(code: Int) {
                Log.d(TAG, "cancelConnect(), onFailure, ${WifiUtils.getErrorCodeDescription(code)}")
            }
        })
    }

    fun requestConnectionInfo() {
        manager.requestConnectionInfo(channel, connector)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    private fun createP2pGroup(deviceAddress: String, name: String) {

        val builder = WifiP2pConfig.Builder()
            .setNetworkName("DIRECT-AQShare_Group111")
            .setPassphrase("1234567890")

//        val config = WifiP2pConfig()
//        config.deviceAddress =
//        config.wps.setup = WpsInfo.PBC
//        config.groupOwnerIntent = 0

        manager.createGroup(channel, builder.build(), object : ActionListener {
        //manager.createGroup(channel, object : ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "createGroup(), onSuccess")
            }

            override fun onFailure(code: Int) {
                Log.d(TAG, "createGroup(), onFailure, ${WifiUtils.getErrorCodeDescription(code)}")
            }
        })
    }

    private fun removeP2pGroup() {
        manager.removeGroup(channel, object : ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "removeGroup, onSuccess")
            }
            override fun onFailure(code: Int) {
                Log.d(TAG, "removeGroup(), onFailure, ${WifiUtils.getErrorCodeDescription(code)}")
            }
        })
    }

}