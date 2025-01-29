package org.example.project

import WinBleNativeApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
// import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.project.connection.mcdns.McDnsScanner
import org.example.project.connection.mcdns.McDnsService
import org.example.project.data.DeviceInfoCommon
import org.example.project.utils.NotificationInterface
import org.example.project.fragments.FileSharingRole
import org.example.project.ui.GetMainView
import org.example.project.ui.alertDialogConfirmCallback
import org.example.project.ui.alertDialogDismissCallback
import org.example.project.ui.alertDialogText
import org.example.project.ui.alertDialogTitle
import org.example.project.ui.progressDialogCancelCallback
import org.example.project.ui.progressDialogProgressValue
import org.example.project.ui.progressDialogTitle
import org.example.project.ui.shouldShowAlertDialog
import org.example.project.ui.shouldShowProgressDialog
import org.example.project.ui.showSnackbar
import org.example.project.ui.snackbarMessage

// import org.example.project.ui.GetAndroidMainView_main

private val notifier = object: NotificationInterface {
    override suspend fun updateProgressDialog(progress: Float) {
        withContext(Dispatchers.Main) {
            progressDialogProgressValue.floatValue = progress
        }
    }

    override suspend fun dismissProgressDialog() {
        withContext(Dispatchers.Main) {
            shouldShowProgressDialog.value = false
        }
    }

    override suspend fun showProgressDialog(
        title: String,
        cancelCallback: () -> Unit
    ) {
        withContext(Dispatchers.Main) {
            progressDialogTitle.value = title
            progressDialogCancelCallback.value = cancelCallback
            progressDialogProgressValue.floatValue = 0.0F
            shouldShowProgressDialog.value = true
        }
    }

    override fun showNotification(message: String) {
//        CoroutineScope(Dispatchers.Main).launch {
            snackbarMessage.value = message
            showSnackbar.value = true
//        }
    }

    override suspend fun showAlertDialog(
        message: String,
        confirmCallback: () -> Unit,
        dismissCallback: () -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            alertDialogTitle.value = ""
            alertDialogText.value = message
            alertDialogConfirmCallback.value = confirmCallback
            alertDialogDismissCallback.value = dismissCallback
            shouldShowAlertDialog.value = true
        }
    }

    override fun dismissAlertDialog() {
        CoroutineScope(Dispatchers.Main).launch {
            shouldShowAlertDialog.value = false
        }
    }

    override fun cancelConnection() {
        TODO("Not yet implemented")
    }

    override suspend fun disconnect() {
        // TODO("Not yet implemented")
    }

    override fun onDeviceListUpdate(deviceList: List<DeviceInfoCommon>) {
        println("onDeviceListUpdate, deviceList.size = ${org.example.project.ui.deviceList.size}")
        org.example.project.ui.deviceList.clear()
        deviceList.forEach {
            org.example.project.ui.deviceList.add(it)
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Desktop App",
    ) {
        // getLazyColumnTestModel()
        // ScaffoldSample()
        // val model = MyViewModel(3)

        // App1()

        App2()
        // getView()
        // helloWorld4()
    }
}

@Composable
fun App2() {

    var fileShareBlock by remember { mutableStateOf(
        FileShareBlockDesktop(
            FileSharingRole.FILE_RECEIVER, "C:/Users/Sergey/Downloads/fs_test/"
        )
    ) }

    fileShareBlock.config(notifier)
    fileShareBlock.onCreate()

    GetMainView(
        fileShareBlock.enableBleServiceCallback,
        fileShareBlock.enableBleScannerCallback,
        fileShareBlock.sendDataCallback,
        fileShareBlock.setDeviceInfoCommon,
        fileShareBlock.getFileDescriptorFromPicker
    )
}