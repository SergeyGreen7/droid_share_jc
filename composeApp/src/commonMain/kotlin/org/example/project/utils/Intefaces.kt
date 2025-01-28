package org.example.project.utils

import org.example.project.data.DeviceInfoCommon


interface NotificationInterface {
    suspend fun showProgressDialog(title: String,
                                   cancelCallback: () -> Unit)
    suspend fun updateProgressDialog(progress: Float)
    suspend fun dismissProgressDialog()
    fun showNotification(message: String)
    suspend fun showAlertDialog(message: String,
                                confirmCallback: () -> Unit,
                                dismissCallback: () -> Unit)
    fun dismissAlertDialog()
    fun cancelConnection()
    suspend fun disconnect()
    fun onDeviceListUpdate(deviceList: List<DeviceInfoCommon>)
    // fun onWifiP2pConnection(info: WifiP2pInfo)
}