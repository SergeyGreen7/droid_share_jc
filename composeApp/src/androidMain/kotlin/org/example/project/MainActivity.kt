package org.example.project

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.project.data.DeviceInfoCommon
import org.example.project.utils.NotificationInterface
import org.example.project.fragments.FileSharingRole
import org.example.project.ui.alertDialogConfirmCallback
import org.example.project.ui.alertDialogDismissCallback
import org.example.project.ui.alertDialogText
import org.example.project.ui.alertDialogTitle
import org.example.project.ui.GetMainView
import org.example.project.ui.progressDialogCancelCallback
import org.example.project.ui.progressDialogProgressValue
import org.example.project.ui.progressDialogTitle
import org.example.project.ui.shouldShowAlertDialog
import org.example.project.ui.shouldShowProgressDialog
import org.example.project.ui.showSnackbar
import org.example.project.ui.snackbarMessage

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
        // private const val REQUEST_ENABLE_BT = 1
        // var txFilePackDscr = TxFilePackDescriptor()
    }

    // private var progressDialog: ProgressDialog? = null
    // private var alertDialog: AlertDialog? = null

    private var fileShareBlock: FileShareBlockAndroid? = null
    private var startIntentResolved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fileShareBlock = FileShareBlockAndroid(
            this,
            this,
            getFileSharingRole(intent),
            Environment.getExternalStorageDirectory().toString() + "/Download/")
        fileShareBlock?.config(notifier)
        fileShareBlock?.onCreate()

        LocalBroadcastManager.getInstance(this).
        registerReceiver(fileShareBlock?.bluetoothController!!.receiver, IntentFilter(
            BluetoothDevice.ACTION_FOUND)
        )

        setContent {
            GetMainView(
                fileShareBlock!!.enableBleServiceCallback,
                fileShareBlock!!.enableBleScannerCallback,
                fileShareBlock!!.sendDataCallback,
                fileShareBlock!!.setDeviceInfoCommon,
                fileShareBlock!!.getFileDescriptorFromPicker,
            )
        }
    }

    override fun onStart() {
        super.onStart()

        if (!startIntentResolved) {
            onNewIntent(intent)
            startIntentResolved = true
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        Log.d(TAG, "MainActivity, start on onNewIntent()")
        Log.d(TAG, "MainActivity, intent.action = ${intent.action}")

        fileShareBlock?.resolveNewIntent(intent)
    }

        private fun getFileSharingRole(intent: Intent) : FileSharingRole {
        return if (intent.action in listOf(Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE)) {
            FileSharingRole.FILE_TRANSMITTER
        } else {
            FileSharingRole.FILE_RECEIVER
        }
    }

    private val channel = Channel<Job>(capacity = Channel.UNLIMITED).apply {
        CoroutineScope(Dispatchers.Main).launch {
            consumeEach { it.join() }
        }
    }

    private val notifier = object: NotificationInterface {
        override suspend fun updateProgressDialog(progress: Float) {
            withContext(Dispatchers.Main) {
                progressDialogProgressValue.floatValue = progress
            }
        }

        override suspend fun dismissProgressDialog() {
            withContext(Dispatchers.Main) {
                shouldShowProgressDialog.value = false
            }
        }

        override suspend fun showProgressDialog(
            title: String,
            cancelCallback: () -> Unit
        ) {
            withContext(Dispatchers.Main) {
                progressDialogTitle.value = title
                progressDialogCancelCallback.value = cancelCallback
                progressDialogProgressValue.floatValue = 0.0F
                shouldShowProgressDialog.value = true
            }
        }

        override fun showNotification(message: String) {
            CoroutineScope(Dispatchers.Main).launch {
                snackbarMessage.value = message
                showSnackbar.value = true
            }
        }

        override suspend fun showAlertDialog(
            message: String,
            confirmCallback: () -> Unit,
            dismissCallback: () -> Unit
        ) {
            channel.trySend(
                CoroutineScope(Dispatchers.Main).launch {
                    alertDialogTitle.value = ""
                    alertDialogText.value = message
                    alertDialogConfirmCallback.value = confirmCallback
                    alertDialogDismissCallback.value = dismissCallback
                    shouldShowAlertDialog.value = true
                }
            )
        }

        override fun dismissAlertDialog() {
            channel.trySend(
                CoroutineScope(Dispatchers.Main).launch {
                    shouldShowAlertDialog.value = false
                }
            )
        }

        override fun cancelConnection() {
            TODO("Not yet implemented")
        }

        override suspend fun disconnect() {
            // TODO("Not yet implemented")
        }

        override fun onDeviceListUpdate(deviceList: List<DeviceInfoCommon>) {
            TODO("Not yet implemented")
        }
    }
}
