package org.example.project.data

// import android.net.wifi.p2p.WifiP2pInfo
//import org.example.project.grid.DeviceInfo

//interface StatusUpdater {
//    fun onDeviceInfoUpdate(newDeviceInfo: DeviceInfo?)
//}

interface NotificationInterface {
    suspend fun showProgressDialog(title: String,
                                   cancelCallback: () -> Unit)
    suspend fun updateProgressDialog(progress: Float)
    suspend fun dismissProgressDialog()
    fun showToast(message: String)
    suspend fun showAlertDialog(message: String,
                                confirmCallback: () -> Unit,
                                dismissCallback: () -> Unit)
    fun dismissAlertDialog()
    fun cancelConnection()
    suspend fun disconnect()
    fun onDeviceListUpdate(deviceList: List<DeviceInfoCommon>)
    // fun onWifiP2pConnection(info: WifiP2pInfo)
}