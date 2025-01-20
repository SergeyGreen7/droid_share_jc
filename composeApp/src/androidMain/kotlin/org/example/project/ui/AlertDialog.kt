package org.example.project.ui

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

var shouldShowAlertDialog = mutableStateOf<Boolean>(false)
var alertDialogTitle = mutableStateOf("")
var alertDialogText = mutableStateOf("")
var alertDialogConfirmCallback = mutableStateOf( { -> } )
var alertDialogDismissCallback = mutableStateOf( { -> } )

@Composable
fun MyAlertDialog() {
    var shouldShowDialog by remember { shouldShowAlertDialog }
    val title by remember { alertDialogTitle }
    val text by remember { alertDialogText }
    val confirmCallback by remember { alertDialogConfirmCallback }
    val dismissCallback by remember { alertDialogDismissCallback }

    if (shouldShowDialog) {
        AlertDialog(
            onDismissRequest = {
                shouldShowDialog = false
            },
            title = { Text(text = title) },
            text = { Text(text = text) },
            confirmButton = {
                Button(
                    onClick = {
                        confirmCallback()
                        shouldShowDialog = false
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
                    dismissCallback()
                    shouldShowDialog = false
                }
            ) {
                Text(
                    text = "Dismiss",
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        },
        )
    }
}