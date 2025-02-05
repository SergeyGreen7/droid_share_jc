package org.example.project.utils

import org.example.project.data.DeviceInfoCommon
import java.util.UUID

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
    fun cancelConnection()
    fun disconnect()
    fun onDeviceListUpdate(deviceList: List<DeviceInfoCommon>)
}

interface BleScannerInterface {
    fun setServiceUuid(uuid: UUID)
    fun startScan()
    fun stopScan()
}

interface BleServiceInterface {
    var referenceData: String
    var callbackOnReferenceDataReception: (flag: Boolean, name: String) -> Unit
    fun setServiceUuid(uuid: UUID)
    fun setCharacteristicUuid(uuid: UUID)
    fun startService()
    fun stopService()
}

interface BleClientInterface {
    var dataToSend: String
    var callbackOnDataSend: (flag: Boolean) -> Unit
    fun setServiceUuid(uuid: UUID)
    fun setCharacteristicUuid(uuid: UUID)
    fun disconnect()
}