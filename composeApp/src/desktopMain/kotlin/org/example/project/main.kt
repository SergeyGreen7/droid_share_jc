package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.example.project.ui.FileShareViewModel
import org.example.project.ui.FileShareManiUI

val vm = FileShareViewModel()
val fileShareBlock = FileShareBlockDesktop(
    vm,
    "C:/Users/Sergey/Downloads/fs_test/",
    )

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
    FileShareManiUI(vm)
}
