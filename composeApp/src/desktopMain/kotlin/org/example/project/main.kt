package org.example.project

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
import org.example.project.connection.blt.DesktopGattScanner
import org.example.project.connection.mtdns.McDnsScanner
import org.example.project.connection.mtdns.McDnsService
import org.example.project.ui.HelloWorld
import org.example.project.ui.Icon
import org.example.project.ui.ScaffoldSample
import org.example.project.ui.getLazyColumnTestModel
import org.example.project.ui.getView
import org.example.project.ui.helloWorld4
import org.example.project.ui.lazyListSample

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Desktop App",
    ) {
        // getLazyColumnTestModel()
        // ScaffoldSample()
        // val model = MyViewModel(3)

        App()
        // getView()
        // helloWorld4()
    }
}

@Composable
fun App() {

    val service by remember { mutableStateOf(McDnsService("desktop-")) }
    var buttonServiceText by remember { mutableStateOf("Start DNS SD Service") }
    val scanner by remember { mutableStateOf(McDnsScanner()) }
    var buttonScannerText by remember { mutableStateOf("Start DNS SD Scanner") }

//    val gattScanner by remember { mutableStateOf(DesktopGattScanner()) }
//    var gattScannerText by remember { mutableStateOf("Start Gatt Scanner") }

    Column {
        Row() {
            Button(
                onClick = {
                    if (buttonServiceText == "Start DNS SD Service") {
                        buttonServiceText = "Stop DNS SD Service";
                        service.registerService()
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