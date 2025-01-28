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
import org.example.project.connection.mtdns.McDnsScanner
import org.example.project.connection.mtdns.McDnsService
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
        CoroutineScope(Dispatchers.Main).launch {
            snackbarMessage.value = message
            showSnackbar.value = true
        }
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

//        override fun onWifiP2pConnection(info: WifiP2pInfo) {
//            TODO("Not yet implemented")
//        }
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
fun App1() {

    val service by remember { mutableStateOf(McDnsService("desktop-")) }
    var buttonServiceText by remember { mutableStateOf("Start DNS SD Service") }
    val scanner by remember { mutableStateOf(McDnsScanner()) }
    var buttonScannerText by remember { mutableStateOf("Start DNS SD Scanner") }

    var buttonBleMethodText by remember { mutableStateOf("Start BLE Method") }
    var buttonBleServiceText by remember { mutableStateOf("Start BLE Service") }
    var buttonBleScannerText by remember { mutableStateOf("Start BLE Scanner") }

    var showFilePicker by remember { mutableStateOf(false) }
    val fileType = listOf("jpg", "png")

//    val callbacks = object : BleCallbackInterface {
//        override fun method() {
//            println("method()")
//        }
//        override fun CallbackOnReferenceDataReception(flag: Boolean, name: String) {
//            println("CallbackOnReferenceDataReception(), flag = " +
//                        (if (flag) "true" else "false") + ", name = " + name
//            )
//        }
//        override fun CallbackOnDataSend(flag: Boolean) {
//            println("CallbackOnDataSend(), flag = " +
//                        (if (flag) "true" else "false")
//            )
//        }
//
//        override fun showDiscoveredDevices(p0: Array<out String>?, p1: Array<out String>?) {
//            if (p0 == null || p1 == null) {
//                return
//            }
//
//            for (i in p0.indices) {
//                println("name: ${p0[i]}, address: ${p1[i]}")
//            }
//        }
//    }
    val winBle by remember { mutableStateOf(WinBleNativeApi()) }

//    val gattScanner by remember { mutableStateOf(DesktopGattScanner()) }
//    var gattScannerText by remember { mutableStateOf("Start Gatt Scanner") }

    Column {
//        Row() {
//            Button(
//                onClick = {
//                    winBle.runBleMethod()
//                }
//            ) {
//                Text(text = buttonBleMethodText)
//            }
//            Spacer(modifier = Modifier.width(50.dp))
//            Button(
//                onClick = {
//                    if (buttonBleScannerText == "Start BLE Scanner") {
//                        buttonBleScannerText = "Stop BLE Scanner"
//                        winBle.runBleScanner()
//                    } else {
//                        buttonBleScannerText = "Start BLE Scanner"
//                        winBle.stopBleScanner()
//                    }
//                }
//            ) {
//                Text(text = buttonBleScannerText)
//            }
//            Spacer(modifier = Modifier.width(50.dp))
//            Button(
//                onClick = {
//                    if (buttonBleServiceText == "Start BLE Service") {
//                        buttonBleServiceText = "Stop BLE Service"
//                        winBle.runBleService()
//                    } else {
//                        buttonBleServiceText = "Start BLE Service"
//                        winBle.stopBleService()
//                    }
//                }
//            ) {
//                Text(text = buttonBleServiceText)
//            }
//        }
        Spacer(modifier = Modifier.height(25.dp))
        Button(
            onClick = {
                showFilePicker = !showFilePicker
            }
        ) {
            Text(text = "Select file")
        }
        Spacer(modifier = Modifier.height(25.dp))
        Row() {
            Button(
                onClick = {
                    if (buttonServiceText == "Start DNS SD Service") {
                        buttonServiceText = "Stop DNS SD Service";
                        CoroutineScope(Dispatchers.IO).launch {
                            service.registerService()
                        }
                    } else {
                        buttonServiceText = "Start DNS SD Service";
                        service.unregisterService()
                    }
                }
            ) {
                Text(text = buttonServiceText)
            }
            Spacer(modifier = Modifier.width(100.dp))
            Button(
                onClick = {
                    if (buttonScannerText == "Start DNS SD Scanner") {
                        buttonScannerText = "Stop DNS SD Scanner";
                        scanner.startScan()
                    } else {
                        buttonScannerText = "Start DNS SD Scanner";
                        scanner.stopScan()
                    }
                }
            ) {
                Text(text = buttonScannerText)
            }
        }
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