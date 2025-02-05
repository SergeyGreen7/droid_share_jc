package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.example.project.ui.GetMainView

val fileShareBlock = FileShareBlockDesktop(
    "C:/Users/Sergey/Downloads/fs_test/")

fun main() = application {

    fileShareBlock.init()

    Window(
        onCloseRequest = {
            fileShareBlock.stopAndDestroy()
            exitApplication()
        },
        title = "Droid share (desktop)",
    ) {
        DesktopApp()
    }
}

@Composable
fun DesktopApp() {
    GetMainView(
        fileShareBlock.enableBleServiceCallback,
        fileShareBlock.enableBleScannerCallback,
        fileShareBlock.sendDataCallback,
        fileShareBlock.setDeviceInfoCommon,
        fileShareBlock.getFileDescriptorFromPicker,
        fileShareBlock.registerMcDnsServiceDebug,
        fileShareBlock.enableMcDnsScannerDebug,
    )
}