package org.example.project.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.example.project.R
import org.example.project.connection.mtdns.McDnsScanner
import org.example.project.connection.mtdns.McDnsService
import org.example.project.data.DeviceInfoCommon
import org.example.project.grid.DeviceInfo

var bleButtonVisible = false
var bleServiceEnabled = mutableStateOf(false)
var bleScannerEnabled = mutableStateOf(false)
var nameStr = ""
var deviceList = mutableStateListOf<DeviceInfoCommon>()

@Composable
fun GetButtons_test(
    bleEnableServiceCallback: (isEnabled:Boolean) -> Unit,
    bleEnableScannerCallback: (isEnabled:Boolean) -> Unit,
    sendDataCallback: () -> Unit,
) {
    val enableBleServiceStr =  stringResource(R.string.button_text_ble_enable_service)
    val disableBleServiceStr =  stringResource(R.string.button_text_ble_disable_service)
    val enableBleScannerStr =  stringResource(R.string.button_text_ble_enable_scanner)
    val disableBleScannerStr =  stringResource(R.string.button_text_ble_disable_scanner)

    val bleButtonVisible by remember { mutableStateOf(bleButtonVisible) }
    var bleServiceEnabled by remember{ bleServiceEnabled }
    var bleScannerEnabled by remember{ bleScannerEnabled }
    val nameStr by remember{  mutableStateOf(nameStr) }
    var bleServiceButtonText by remember{ mutableStateOf(enableBleServiceStr) }
    var bleScannerButtonText by remember{ mutableStateOf(enableBleScannerStr) }

    // tmp
    var scannerButtonText by remember { mutableStateOf("Start MC DNS Discovery") }
    var serviceScanner by remember { mutableStateOf(McDnsScanner()) }
    var servciceButtonText by remember { mutableStateOf("Start MC DNS Service") }
    var service by remember { mutableStateOf(McDnsService("android-")) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
//        if (bleButtonVisible) {
        Row {
            Button(
                onClick = {
                    if (!bleServiceEnabled) {
                        bleServiceButtonText = disableBleServiceStr
                    } else {
                        bleServiceButtonText = enableBleServiceStr
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
                    if (!bleScannerEnabled) {
                        bleScannerButtonText = disableBleScannerStr
                    } else {
                        bleScannerButtonText = enableBleScannerStr
                    }
                    bleScannerEnabled = !bleScannerEnabled
                    bleEnableScannerCallback(bleScannerEnabled)
                },
            ) {
                Text(text = bleScannerButtonText)
            }
        }
//        }

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
                    alertDialogConfirmCallback.value = { -> println("Confirm button pressed") }
                    alertDialogDismissCallback.value = { -> println("Dismiss button pressed") }
                    shouldShowAlertDialog.value = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Show Alert Dialog")
            }
            Spacer(modifier = Modifier.width(15.dp))
            Button(
                onClick = {
                    toastMessage.value = "Test toastMessage"
                    shouldShowToast.value = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Show Toast")
            }
        }

        Spacer(modifier = Modifier.width(15.dp))
        Row() {
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
                    if (servciceButtonText == "Start MC DNS Service") {
                        servciceButtonText = "Stop MC DNS Service"
                        service.registerService()
                    } else {
                        servciceButtonText = "Start MC DNS Service"
                        service.unregisterService()
                    }
                },
            ) {
                Text(text = servciceButtonText)
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
        Text(text = nameStr)
        Spacer(modifier = Modifier.width(15.dp))
        Row {
            Text(
                text = "Current number = ${deviceList.size}"
            )
            Spacer(modifier = Modifier.width(15.dp))
            Column {
                Button(onClick = {
                    deviceList.add(DeviceInfoCommon(
                        "test_name_${deviceList.size}",
                        "test_info_${deviceList.size}"))
                    println("devices size = ${deviceList.size}")
                }) {
                    Text("Increase number")
                }
                Spacer(modifier = Modifier.width(15.dp))
                Button(onClick = {
                    if (deviceList.isNotEmpty()) {
                        deviceList.removeAt(deviceList.lastIndex)
                    }
                    println("devices size = ${deviceList.size}")
                }) {
                    Text("Decrease number")
                }
            }
        }
    }
}

@Composable
fun GetAndroidMainView(
    bleServiceCallback: (isEnabled:Boolean) -> Unit,
    bleScannerCallback: (isEnabled:Boolean) -> Unit,
    sendDataCallback: () -> Unit,
    setDeviceInfoCallback: (device: DeviceInfoCommon, index: Int) -> Unit,
) {
    MyAlertDialog()
    MyProgressDialog()
    ShowToast()

    Column(
//        verticalArrangement = Arrangement.Bottom,
//        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        GetGrid_test(setDeviceInfoCallback)
        Spacer(modifier = Modifier.weight(1f))
        GetButtons_test(bleServiceCallback, bleScannerCallback, sendDataCallback)
    }
}

@SuppressLint("RememberReturnType", "MutableCollectionMutableState")
@Composable
fun GetGrid_test(
    setDeviceInfoCallback: (device: DeviceInfoCommon, index: Int) -> Unit,
) {
    var selectedIndex by remember { mutableStateOf(-1) }
    val devices = remember { deviceList }

    println("run getGrid_test(), devices.size = ${devices.size}")

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
                    .background(color = if (selectedIndex == index)
                        { Color.Green } else { Color.White } )
            ) {
                Text("Name: ${item.deviceName}")
                Text("Info: ${item.deviceInfo}")
                Spacer(modifier = Modifier.width(10.dp))
                Button(onClick = {}) {
                    Text("Button")
                }
            }
        }
    }
}
