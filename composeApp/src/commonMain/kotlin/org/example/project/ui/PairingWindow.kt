package org.example.project.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun PairingWindow(
    vm: FileShareViewModel
) {
    if (vm.showPairingWindow.value) {
        Dialog(
            onDismissRequest = {
                vm.showPairingWindow.value = false
            },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside= false,
                usePlatformDefaultWidth = true,
            )
        ) {
            Column {
                Text(text = vm.pairingWindowTitle.value)
                Text(text = vm.pairingWindowMessage.value)
                Button(
                    onClick = {
                        vm.showPairingWindow.value = false
                        vm.pairingWindowDismissCallback.value()
                    }
                ) {
                    Text(
                        text = "Dismiss",
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }
            }

        }
    }
}