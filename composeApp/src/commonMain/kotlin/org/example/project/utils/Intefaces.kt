package org.example.project.utils

import org.example.project.data.DeviceInfoCommon

interface NotificationInterface {
    fun showProgressDialog(title: String,
                           cancelCallback: () -> Unit)
    fun updateProgressDialog(progress: Float)
    fun dismissProgressDialog()
    fun showNotification(message: String)
    fun showAlertDialog(message: String,
                        confirmCallback: () -> Unit,
                        dismissCallback: () -> Unit)
    fun dismissAlertDialog()
    fun closeConnection()
    // fun disconnect()
    fun onDeviceListUpdate(deviceList: List<DeviceInfoCommon>)
}
