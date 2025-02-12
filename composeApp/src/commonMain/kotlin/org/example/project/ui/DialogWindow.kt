package org.example.project.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

@Composable
fun DialogWindow(
    vm: FileShareViewModel
) {
    if (vm.showDialogWindow.value) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(text = vm.dialogWindowTitle.value) },
            text = { Text(text = vm.dialogWindowMessage.value) },
            confirmButton = {
                Button(
                    onClick = {
                        vm.showDialogWindow.value = false
                        vm.dialogWindowConfirmCallback.value()
                    }
                ) {
                    Text(
                        text = "Confirm",
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }
            },
            dismissButton = {
            Button(
                onClick = {
                    vm.showDialogWindow.value = false
                    vm.dialogWindowDismissCallback.value()
                }
            ) {
                Text(
                    text = "Dismiss",
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = true)
        )
    }
}