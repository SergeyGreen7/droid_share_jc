package org.example.project.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog

var shouldShowNotificationDialog = mutableStateOf<Boolean>(false)
var notificationDialogTitle = mutableStateOf("")
var notificationDialogText = mutableStateOf("")
var notificationDialogDismissCallback = mutableStateOf( { -> } )

@Composable
fun MyNotificationDialog() {
    var shouldShowDialog by remember { shouldShowNotificationDialog }
    val title by remember { notificationDialogTitle }
    val text by remember { notificationDialogText }
    val dismissCallback by remember { notificationDialogDismissCallback }

    if (shouldShowDialog) {
        Dialog(
            onDismissRequest = {
                shouldShowDialog = false
            },
        ) {
            Column {
                Text(text = title)
                Text(text = text)
                Button(
                    onClick = {
                        dismissCallback()
                        shouldShowDialog = false
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