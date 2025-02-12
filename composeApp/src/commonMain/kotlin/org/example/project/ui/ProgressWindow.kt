package org.example.project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ProgressWindow(
    vm: FileShareViewModel
){
    if (vm.showProgressWindow.value) {
        Dialog(
            onDismissRequest = { },
            DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            val progressStr = "%.0f".format(vm.progressWindowProgressValue.floatValue)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                    .padding(40.dp)
            ) {
                Column() {
                    Text(text = vm.progressWindowTitle.value, fontSize = 24.sp)
                    Row() {
//                        LinearProgressIndicator(
//                            progress = progress,
//                            color = Color.Black,
//                        )
                        Text(text = "Progress $progressStr %")
                        Spacer(modifier = Modifier.weight(1f))
                        Button(onClick = {
                            vm.showProgressWindow.value = false
                            vm.progressWindowCancelCallback.value.invoke()
                        }) {
                            Text(text = "Cancel")
                        }
                    }
                }
            }

        }
    }

}
