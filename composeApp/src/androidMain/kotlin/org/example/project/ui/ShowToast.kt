package org.example.project.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

var shouldShowToast = mutableStateOf<Boolean>(false)
var toastMessage = mutableStateOf("")

@Composable
fun ShowToast() {
    val shouldShow by remember { shouldShowToast }
    val message by remember { toastMessage }

    if (shouldShow) {
        Toast.makeText(LocalContext.current, message, Toast.LENGTH_LONG).show()
        shouldShowToast.value = false
    }
}
