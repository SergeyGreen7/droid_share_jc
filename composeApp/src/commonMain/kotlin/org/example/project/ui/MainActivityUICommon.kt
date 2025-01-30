package org.example.project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PlatformFiles
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.project.connection.mcdns.McDnsScanner
import org.example.project.connection.mcdns.McDnsService
import org.example.project.data.DeviceInfoCommon

var bleButtonVisible = false
var bleServiceEnabled = mutableStateOf(false)
var bleScannerEnabled = mutableStateOf(false)
var nameStr = mutableStateOf("")
var fileStr = mutableStateOf("file(s) not selected")
var deviceList = mutableStateListOf<DeviceInfoCommon>()

var bleScannerButtonText = mutableStateOf("Turn Scanner ON")

@Composable
fun GetMainView(
    bleServiceCallback: (isEnabled:Boolean) -> Unit,
    bleScannerCallback: (isEnabled:Boolean) -> Unit,
    sendDataCallback: () -> Unit,
    setDeviceInfoCallback: (device: DeviceInfoCommon, index: Int) -> Unit,
    createFileDscrList: (files: PlatformFiles?) -> Unit
) {
    MyAlertDialog()
    MyProgressDialog()

    Column {
        GetGrid(setDeviceInfoCallback)
        Spacer(modifier = Modifier.weight(1f))
        GetButtons(
            bleServiceCallback,
            bleScannerCallback,
            sendDataCallback,
            createFileDscrList)
    }

    ShowSnackBar(true, 3000)
}

@Composable
fun GetButtons(
    bleEnableServiceCallback: (isEnabled:Boolean) -> Unit,
    bleEnableScannerCallback: (isEnabled:Boolean) -> Unit,
    sendDataCallback: () -> Unit,
    createFileDscrList: (files: PlatformFiles?) -> Unit
) {
    val enableBleServiceStr = "Enable Receiver"
    val disableBleServiceStr = "Disable Receiver"
    // val enableBleScannerStr = "Enable BLE Scanner"
    // val disableBleScannerStr = "Disable BLE Scanner"

    // val bleButtonVisible by remember { mutableStateOf(bleButtonVisible) }
    var bleServiceEnabled by remember{ bleServiceEnabled }
    var bleScannerEnabled by remember{ bleScannerEnabled }
    val nameStr by remember{ nameStr }
    val fileStr by remember{ fileStr }
    var bleServiceButtonText by remember{ mutableStateOf(enableBleServiceStr) }
    val bleScannerButtonText by remember{ mutableStateOf(bleScannerButtonText) }

    // tmp
    val showDebugButtons = false
    var scannerButtonText by remember { mutableStateOf("Start MC DNS Discovery") }
    val serviceScanner by remember { mutableStateOf(McDnsScanner()) }
    var serviceButtonText by remember { mutableStateOf("Start MC DNS Service") }
    val service by remember { mutableStateOf(McDnsService("android-")) }


    val filePicker = rememberFilePickerLauncher(mode = PickerMode.Multiple()) { files ->
        println("launcher, files = $files")
        createFileDscrList(files)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        Row {
            Button(
                onClick = {
                    filePicker.launch()
                },
            ) {
                Text(text = "Select file")
            }
            Spacer(modifier = Modifier.width(20.dp))
            Button(
                onClick = {
                    bleServiceButtonText = if (!bleServiceEnabled) {
                        disableBleServiceStr
                    } else {
                        enableBleServiceStr
                    }
                    bleServiceEnabled = !bleServiceEnabled
                    bleEnableServiceCallback(bleServiceEnabled)
                },
            ) {
                Text(text = bleServiceButtonText)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Button(
                onClick = {
//                    if (!bleScannerEnabled) {
//                        bleScannerButtonText = disableBleScannerStr
//                    } else {
//                        bleScannerButtonText = enableBleScannerStr
//                    }
                    bleScannerEnabled = !bleScannerEnabled
                    bleEnableScannerCallback(bleScannerEnabled)
                },
            ) {
                Text(text = bleScannerButtonText.value)
            }
        }

        if (showDebugButtons) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        progressDialogTitle.value = "Test progressDialogTitle"
                        shouldShowProgressDialog.value = true
                        progressDialogCancelCallback.value = { println("Cancel button pressed") }
                        progressDialogProgressValue.floatValue = 0.0F
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Show Progress Dialog")
                }
                Spacer(modifier = Modifier.width(15.dp))
                Button(
                    onClick = {
                        alertDialogTitle.value = "Test alertDialogTitle"
                        alertDialogText.value = "Test alertDialogText"
                        alertDialogConfirmCallback.value = { println("Confirm button pressed") }
                        alertDialogDismissCallback.value = { println("Dismiss button pressed") }
                        shouldShowAlertDialog.value = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Show Alert Dialog")
                }
                Spacer(modifier = Modifier.width(15.dp))
                Button(
                    onClick = {
                        // showNotificationCallback()
                        snackbarMessage.value = "Some text in snackbar, ho ho"
                        showSnackbar.value = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Show Notification")
                }
            }

            Spacer(modifier = Modifier.width(15.dp))
            Row {
                Button(
                    onClick = {
                        if (scannerButtonText == "Start MC DNS Discovery") {
                            scannerButtonText = "Stop MC DNS Discovery"
                            serviceScanner.startScan()
                        } else {
                            scannerButtonText = "Start MC DNS Discovery"
                            serviceScanner.stopScan()
                        }
                    },
                ) {
                    Text(text = scannerButtonText)
                }
                Spacer(modifier = Modifier.width(15.dp))
                Button(
                    onClick = {
                        if (serviceButtonText == "Start MC DNS Service") {
                            serviceButtonText = "Stop MC DNS Service"

                            CoroutineScope(Dispatchers.IO).launch {
                                service.registerService()
                            }
                        } else {
                            serviceButtonText = "Start MC DNS Service"
                            service.unregisterService()
                        }
                    },
                ) {
                    Text(text = serviceButtonText)
                }
            }
        }

        Button(
            onClick = {
                sendDataCallback()
            },
        ) {
            Text(text = "Send Data")
        }

        Spacer(modifier = Modifier.width(15.dp))
        Row {
            Text(
                text = nameStr,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = fileStr,
            )
        }
        Spacer(modifier = Modifier.width(15.dp))
    }
}

@Composable
fun GetGrid(
    setDeviceInfoCallback: (device: DeviceInfoCommon, index: Int) -> Unit,
) {
    var selectedIndex by remember { mutableStateOf(-1) }
    val devices = remember { deviceList }

    println("run getGrid(), devices.size = ${devices.size}")

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
    ) {
        itemsIndexed(devices) { index, item ->
            Column(
                modifier = Modifier
                    .selectable(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                            setDeviceInfoCallback(devices[index], index)
                        }
                    )
                    .background(
                        color = if (selectedIndex == index) {
                            Color.Green
                        } else {
                            Color.White
                        }
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(15.dp)
                        .align(alignment = Alignment.CenterHorizontally)
                ) {
                    Text("Name: ${item.deviceName}")
                    Text("Info: ${item.deviceInfo}")
                }
            }
        }
    }
}
