package org.example.project.connection.wifi

import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener
import android.util.Log
import android.view.View
import android.widget.TextView
//import org.example.project.NotificationInterface
import org.example.project.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.project.data.NotificationInterface

class WifiP2pConnector(
    private var notifier: NotificationInterface,
) : ConnectionInfoListener {

    companion object {
        private const val TAG = "WifiP2pController"
    }

    override fun onConnectionInfoAvailable(info: WifiP2pInfo?) {
        Log.d(TAG, "onConnectionInfoAvailable(), info:\n    $info")
        if (info == null || !info.groupFormed) {
            return
        }

        // notifier.onWifiP2pConnection(info)
    }
}