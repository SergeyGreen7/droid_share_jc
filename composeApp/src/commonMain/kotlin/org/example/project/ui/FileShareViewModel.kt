package org.example.project.ui

import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.github.vinceglb.filekit.core.PlatformFiles
import org.example.project.data.DeviceInfoCommon

class FileShareViewModel : ViewModel() {
    var sendDataButtonIsActive = mutableStateOf(false)
    private var selectedIndex = mutableStateOf(-1)
    var nameStr = mutableStateOf("")
    var fileStr = mutableStateOf("file(s) not selected")
    var discoveredDeviceList = mutableStateListOf<DeviceInfoCommon>()

    var bleServiceCallback  = { isEnabled:Boolean -> }
    var bleScannerCallback  = { isEnabled:Boolean -> }
    var sendDataCallback = { -> }
    var pairCreationCallback = { -> }
    var setDeviceInfoCallback = {device: DeviceInfoCommon, index: Int -> }
    var createFileDscrList = { files: PlatformFiles? -> }
    var enableMcDnsService = { flag: Boolean -> }
    var enableMcDnsScanner = { flag: Boolean -> }

    var showNotificationWindow = mutableStateOf(false)
    var notificationWindowMessage = mutableStateOf("")
    var notificationWindowAutoclose = true
    var notificationWindowTimeoutMillis = 3000L

    var showDialogWindow = mutableStateOf<Boolean>(false)
    var dialogWindowTitle = mutableStateOf("")
    var dialogWindowMessage = mutableStateOf("")
    var dialogWindowConfirmCallback = mutableStateOf( { -> } )
    var dialogWindowDismissCallback = mutableStateOf( { -> } )

    var showProgressWindow = mutableStateOf<Boolean>(false)
    var progressWindowTitle = mutableStateOf("")
    var progressWindowProgressValue = mutableFloatStateOf(value = 0F)
    var progressWindowCancelCallback = mutableStateOf({ -> })

    var showPairingWindow = mutableStateOf<Boolean>(false)
    var pairingWindowTitle = mutableStateOf("")
    var pairingWindowMessage = mutableStateOf("")
    var pairingWindowDismissCallback = mutableStateOf( { -> } )

    var bleServiceEnabledDebug = mutableStateOf(false)
    var bleScannerEnabledDebug = mutableStateOf(false)
    var dnsServiceEnabledDebug = mutableStateOf(false)
    var dnsScannerEnabledDebug = mutableStateOf(false)

    fun reset() {
        onDataRemove()
        dropSelectedIndex()
    }

    fun setHostName(name: String) {
        nameStr.value = "Your name is $name"
    }

    fun onDataSelection() {
        sendDataButtonIsActive.value = true
        setFileIndicator()
    }

    fun onDataRemove() {
        sendDataButtonIsActive.value = false
        dropFileIndicator()
    }

    fun setSelectedIndex(index: Int) {
        selectedIndex.value = index
    }

    fun dropSelectedIndex() {
        selectedIndex.value = -1
    }

    fun checkSelectedIndexForDrop() {
        if (selectedIndex.value >= discoveredDeviceList.size) {
            dropSelectedIndex()
        }
    }

    fun isIndexSelected(index: Int): Boolean {
        return selectedIndex.value == index
    }

    fun setDeviceList(deviceList: List<DeviceInfoCommon>) {
        discoveredDeviceList.clear()
        deviceList.forEach {
            discoveredDeviceList.add(it)
        }
    }

    private fun setFileIndicator() {
        fileStr.value = "file(s) selected"
    }

    private fun dropFileIndicator() {
        fileStr.value = "file(s) not selected"
    }

    fun showProgressWindow(title: String, cancelCallback: () -> Unit) {
        showProgressWindow.value = true
        progressWindowTitle.value = title
        progressWindowProgressValue.floatValue = 0f
        progressWindowCancelCallback.value = cancelCallback
    }

    fun setProgressWindowValue(value: Float) {
        progressWindowProgressValue.floatValue = value
    }

    fun closeProgressWindow() {
        showProgressWindow.value = false
    }

    fun showNotificationWindow(message: String) {
        showNotificationWindow.value = true
        notificationWindowMessage.value = message
    }

    fun showDialogWindow(title: String,
                         message: String,
                         confirmCallback: () -> Unit,
                         dismissCallback: () -> Unit
    ) {
        showDialogWindow.value = true
        dialogWindowTitle.value = title
        dialogWindowMessage.value = message
        dialogWindowConfirmCallback.value = confirmCallback
        dialogWindowDismissCallback.value = dismissCallback
    }

    fun closeDialogWindow() {
        showDialogWindow.value = false
    }

    fun showPairingWindow(title: String,
                          message: String,
                          dismissCallback: () -> Unit
    ) {
        showPairingWindow.value = true
        pairingWindowTitle.value = title
        pairingWindowMessage.value = message
        pairingWindowDismissCallback.value = dismissCallback
    }

    fun closePairingWindow() {
        showPairingWindow.value = false
    }

    fun updateTitlePairingWindow(message: String) {
        pairingWindowMessage.value = message
    }
}