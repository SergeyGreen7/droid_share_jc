package org.example.project.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FuncCheckUI(
    vm: FileShareViewModel,
    showDebugButtons: Boolean
) {
    val enableBleServiceStr = "Enable Receiver"
    val disableBleServiceStr = "Disable Receiver"
    val enableBleScannerStr = "Turn BLE Scanner ON"
    val disableBleScannerStr = "Turn BLE Scanner OFF"

    val enableDnsServiceStr = "Enable DNS Service"
    val disableDnsServiceStr = "Disable DNS Service"
    val enableDnsScannerStr = "Turn DNS Scanner ON"
    val disableDnsScannerStr = "Turn DNS Scanner OFF"

    val enableFlag by remember { mutableStateOf(showDebugButtons) }
    val bleServiceButtonText = if (!vm.bleServiceEnabledDebug.value) {
        disableBleServiceStr
    } else {
        enableBleServiceStr
    }
    val dnsScannerButtonText = if (!vm.dnsScannerEnabledDebug.value) {
        disableDnsScannerStr
    } else {
        enableDnsScannerStr
    }

    val dnsServiceButtonText = if (!vm.dnsServiceEnabledDebug.value) {
        disableDnsServiceStr
    } else {
        enableDnsServiceStr
    }
    val bleScannerButtonText = if (!vm.bleScannerEnabledDebug.value) {
        disableBleScannerStr
    } else {
        enableBleScannerStr
    }

    val coroutineScope = rememberCoroutineScope()
    var progress = 0.0f
    var increaseProgressFlag by remember { mutableStateOf(false) }
    val increaseProgressTask = suspend {
        progress = 0.0f
        while (increaseProgressFlag && progress < 100.0f) {
            println("progress = $progress")
            delay(50)
            progress += 1f
            vm.setProgressWindowValue(progress)
        }
        println("end of while loop, increaseProgressFlag = $increaseProgressFlag")
        increaseProgressFlag = false
        vm.closeProgressWindow()
    }

    if (enableFlag) {
        Row() {
            Button(
                onClick = {
                    vm.bleServiceEnabledDebug.value = !vm.bleServiceEnabledDebug.value
                    vm.bleServiceCallback(vm.bleServiceEnabledDebug.value)
                },
            ) {
                Text(text = bleServiceButtonText)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Button(
                onClick = {
                    vm.bleScannerEnabledDebug.value = !vm.bleScannerEnabledDebug.value
                    vm.bleScannerCallback(vm.bleScannerEnabledDebug.value)
                },
            ) {
                Text(text = bleScannerButtonText)
            }
        }
        Spacer(modifier = Modifier.width(15.dp))
        Row {
            Button(
                onClick = {
                    vm.dnsScannerEnabledDebug.value =!vm.dnsScannerEnabledDebug.value
                    vm.enableMcDnsScanner(vm.dnsScannerEnabledDebug.value)
                },
            ) {
                Text(text = dnsScannerButtonText)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Button(
                onClick = {
                    vm.dnsServiceEnabledDebug.value =!vm.dnsServiceEnabledDebug.value
                    vm.enableMcDnsService(vm.dnsServiceEnabledDebug.value)
                },
            ) {
                Text(text = dnsServiceButtonText)
            }
        }
        Spacer(modifier = Modifier.width(15.dp))
        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    vm.showProgressWindow("Test Progress Window") {
                        increaseProgressFlag = false
                        println("Cancel button pressed")
                    }

                    increaseProgressFlag = true
                    coroutineScope.launch { increaseProgressTask() }
                },
            ) {
                Text(text = "Show Progress Window")
            }
            Spacer(modifier = Modifier.width(15.dp))
            Button(
                onClick = {
                    vm.showDialogWindow(
                        title = "Test Title",
                        message = "Test Message",
                        confirmCallback = { println("Confirm button pressed") },
                        dismissCallback = { println("Dismiss button pressed") },
                        )
                },
            ) {
                Text(text = "Show Dialog Window")
            }
            Spacer(modifier = Modifier.width(15.dp))
            Button(
                onClick = {
                    vm.showNotificationWindow("Some text in notification window")
                },
            ) {
                Text(text = "Show Notification")
            }
            Spacer(modifier = Modifier.width(15.dp))
            Button(
                onClick = {
                    vm.showPairingWindow(
                        "Title Text",
                        "Message text",
                        { println("you pushed dismiss button")})
                },
            ) {
                Text(text = "Show Notification")
            }
        }


    }
}