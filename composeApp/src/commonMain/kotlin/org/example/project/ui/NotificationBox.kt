package org.example.project.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

var snackbarMessage = mutableStateOf("")
var showSnackbar = mutableStateOf(false)

@Composable
fun SnackBars(
    autoDismiss: Boolean,
    timeout: Long
) {
    val snackbarMessage by remember { snackbarMessage }

    if (showSnackbar.value) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                 verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(0.5f))
                Snackbar(
                    modifier =  Modifier
                        .weight(1f),
                ) {
                    Text(text = snackbarMessage)
                }
                Spacer(modifier = Modifier.weight(0.5f))
            }
        }
    }

    if (autoDismiss && showSnackbar.value) {
        CoroutineScope(Dispatchers.IO).launch {
            delay(timeout)
            showSnackbar.value = false
        }
    }
}
