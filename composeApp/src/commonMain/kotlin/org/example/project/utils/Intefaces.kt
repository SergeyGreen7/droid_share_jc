package org.example.project.utils

import org.example.project.data.DeviceInfoCommon

interface NotificationInterface {
    fun showNotificationDialog(title: String,
                            cancelCallback: () -> Unit)
    fun updateNotificationDialogTitle(title: String)
    fun dismissNotificationDialog()

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
    fun closePairConnection()
    fun onDeviceListUpdate(deviceList: List<DeviceInfoCommon>)
}
