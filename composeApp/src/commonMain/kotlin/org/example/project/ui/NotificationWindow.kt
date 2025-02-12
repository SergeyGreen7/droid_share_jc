package org.example.project.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NotificationWindow(
    vm: FileShareViewModel,
) {
    if (vm.showNotificationWindow.value) {
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
                    Text(text = vm.notificationWindowMessage.value)
                }
                Spacer(modifier = Modifier.weight(0.5f))
            }
        }
    }

    if (vm.notificationWindowAutoclose && vm.showNotificationWindow.value) {
        CoroutineScope(Dispatchers.IO).launch {
            delay(vm.notificationWindowTimeoutMillis)
            vm.showNotificationWindow.value = false
        }
    }
}
