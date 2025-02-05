package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.example.project.fragments.FileSharingRole
import org.example.project.ui.GetMainView

fun main() = application {

    val fileShareBlock = FileShareBlockDesktop(
        FileSharingRole.FILE_RECEIVER,
        "C:/Users/Sergey/Downloads/fs_test/")
    fileShareBlock.init()

    Window(
        onCloseRequest = ::exitApplication,
        title = "Droid share (desktop)",
    ) {
    //       DesktopApp()

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
}

@Composable
fun DesktopApp() {
//    val fileShareBlock = FileShareBlockDesktop(
//        FileSharingRole.FILE_RECEIVER,
//        "C:/Users/Sergey/Downloads/fs_test/")
//    fileShareBlock.init()
//
//    GetMainView(
//        fileShareBlock.enableBleServiceCallback,
//        fileShareBlock.enableBleScannerCallback,
//        fileShareBlock.sendDataCallback,
//        fileShareBlock.setDeviceInfoCommon,
//        fileShareBlock.getFileDescriptorFromPicker,
//        fileShareBlock.registerMcDnsServiceDebug,
//        fileShareBlock.enableMcDnsScannerDebug,
//    )
}