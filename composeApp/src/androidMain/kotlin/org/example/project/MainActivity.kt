package org.example.project

import android.app.ProgressDialog
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pInfo
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.project.data.DeviceInfoCommon
import org.example.project.data.NotificationInterface
import org.example.project.fragments.FileShareBlock
import org.example.project.fragments.FileSharingRole
import org.example.project.grid.DeviceInfo
import org.example.project.ui.HelloWorld
import org.example.project.ui.alertDialogConfirmCallback
import org.example.project.ui.alertDialogDismissCallback
import org.example.project.ui.alertDialogText
import org.example.project.ui.alertDialogTitle
import org.example.project.ui.GetAndroidMainView
import org.example.project.ui.progressDialogCancelCallback
import org.example.project.ui.progressDialogProgressValue
import org.example.project.ui.progressDialogTitle
import org.example.project.ui.shouldShowAlertDialog
import org.example.project.ui.shouldShowProgressDialog
import org.example.project.ui.shouldShowToast
import org.example.project.ui.toastMessage

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_ENABLE_BT = 1
        // var txFilePackDscr = TxFilePackDescriptor()
    }

    private var progressDialog: ProgressDialog? = null
    private var alertDialog: AlertDialog? = null

    var fsBlock: FileShareBlock? = null
    private var startIntentResolved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fsBlock = FileShareBlock(
            this,
            this,
            getFileSharingRole(intent))
        fsBlock?.config(notifier)
        fsBlock?.onCreate()

        LocalBroadcastManager.getInstance(this).
            registerReceiver(fsBlock?.bluetoothController!!.receiver, IntentFilter(
            BluetoothDevice.ACTION_FOUND)
        )

        setContent {
            GetAndroidMainView(
                fsBlock!!.enableBleServiceCallback,
                fsBlock!!.enableBleScannerCallback,
                fsBlock!!.sendDataCallback,
                fsBlock!!.setDeviceInfoCommon)
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

        fsBlock?.resolveNewIntent(intent)
    }


//    private fun requestPerms() {
//        val requestCode = 0
//        val permissions = arrayListOf(
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//        )
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
//        }
//        ComponentActivity.requestPermissions(this@MainActivity,
//            permissions.toTypedArray(), requestCode)
//    }

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

        override fun showToast(message: String) {
            CoroutineScope(Dispatchers.Main).launch {
                toastMessage.value = message
                shouldShowToast.value = true
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

//        override fun onWifiP2pConnection(info: WifiP2pInfo) {
//            TODO("Not yet implemented")
//        }
    }
}

@Preview(
    showBackground = true
)
@Composable
fun AppAndroidPreview() {
    HelloWorld()
}
