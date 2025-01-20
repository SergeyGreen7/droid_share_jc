package org.example.project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

var shouldShowProgressDialog = mutableStateOf<Boolean>(false)
var progressDialogProgressValue = mutableFloatStateOf(value = 0F)
var progressDialogTitle = mutableStateOf("")
var progressDialogCancelCallback = mutableStateOf({ -> })

@Composable
fun MyProgressDialog() {
    val shouldShowDialog by remember { shouldShowProgressDialog }
    val progress by remember { progressDialogProgressValue }
    val title by remember { progressDialogTitle }
    val cancelCallback by remember { progressDialogCancelCallback }

    if (shouldShowDialog) {
        Dialog(
            onDismissRequest = { shouldShowProgressDialog.value = false },
            DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                    .padding(40.dp)
            ) {
                Column() {
                    Text(text = title, fontSize = 24.sp)
                    Row() {
                        LinearProgressIndicator(
                            progress = progress,
                            color = Color.Black,
                        )
                        Button(onClick = {
                            shouldShowProgressDialog.value = false
                            cancelCallback()
                        }) {
                            Text(text = "Cancel")
                        }
                    }
                }
            }

        }
    }

}
