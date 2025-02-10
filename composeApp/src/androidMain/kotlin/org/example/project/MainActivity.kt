package org.example.project

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.example.project.ui.GetMainView


class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var fileShareBlock: FileShareBlockAndroid
    private var startIntentResolved = false

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestForPermissions()

        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        if (!bluetoothManager.adapter.isEnabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            btEnableLauncher.launch(intent)
        } else {
            initApp()
        }
    }

    @SuppressLint("MissingPermission")
    private fun initApp() {

        // getServiceNotification(this)

        fileShareBlock = FileShareBlockAndroid(
            ContextFactory(this),
            Environment.getExternalStorageDirectory().toString() + "/Download/",
        )
        fileShareBlock.init()

        LocalBroadcastManager.getInstance(this).registerReceiver(
            fileShareBlock.bluetoothController.receiver,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )

        setContent {
            GetMainView(
                fileShareBlock.enableBleServiceCallback,
                fileShareBlock.enableBleScannerCallback,
                fileShareBlock.sendDataCallback,
                fileShareBlock.createPairCallback,
                fileShareBlock.setDeviceInfoCommon,
                fileShareBlock.getFileDescriptorFromPicker,
                fileShareBlock.registerMcDnsServiceDebug,
                fileShareBlock.enableMcDnsScannerDebug,
            )
        }

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

        fileShareBlock.resolveNewIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        fileShareBlock.stopAndDestroy()

    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestForPermissions() {
        val permissions = listOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
        )
        requestPermissions(permissions.toTypedArray(), 100)
    }

    private var btEnableLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            initApp()
        } else {
            finish()
        }
    }

//    @SuppressLint("MissingPermission")
//    private fun createNotification() {
//        createNotificationChannel()
//
//        // Create an explicit intent for an Activity in your app.
//        val intent = Intent(this, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
//
//        val ACTION_SNOOZE = "stop"
//        val EXTRA_NOTIFICATION_ID = "123123"
//        val snoozeIntent = Intent(this, MainActivity::class.java).apply {
//            action = ACTION_SNOOZE
//            putExtra(EXTRA_NOTIFICATION_ID, 0)
//        }
//        val snoozePendingIntent: PendingIntent =
//            PendingIntent.getBroadcast(this, 0, snoozeIntent, 0)
//
//        val builder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
//            setContentTitle("Picture Download")
//            setContentText("Download in progress")
//            setSmallIcon(R.drawable.baseline_add_reaction_24)
//            setPriority(NotificationCompat.PRIORITY_LOW)
//            setContentIntent(pendingIntent)
//            setAutoCancel(true)
//            setOnlyAlertOnce(true)
//            addAction(R.drawable.baseline_back_hand_24,
//                "pause",
//                snoozePendingIntent)
//        }
//        val PROGRESS_MAX = 100
//        val PROGRESS_CURRENT = 0
//
//        NotificationManagerCompat.from(this).apply {
//            // Issue the initial notification with zero progress.
//            builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false)
//            notify(notificationId, builder.build())
//
//            // Do the job that tracks the progress here.
//            // Usually, this is in a worker thread.
//            // To show progress, update PROGRESS_CURRENT and update the notification with:
//            // builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
//            // notificationManager.notify(notificationId, builder.build());
//
//            CoroutineScope(Dispatchers.IO).launch {
//                for (i in 0..100) {
//                    delay(1000)
//                    builder.setProgress(PROGRESS_MAX, i, false)
//                    notify(notificationId, builder.build())
//                }
//
//                // When done, update the notification once more to remove the progress bar.
//                builder.setContentText("Download complete")
//                    .setProgress(0, 0, false)
//                notify(notificationId, builder.build())
//            }
//        }
//
//        with(NotificationManagerCompat.from(this)) {
//            // notificationId is a unique int for each notification that you must define.
//            notify(notificationId, builder.build())
//        }
//    }

//    private fun createNotificationChannel() {
//        // Create the NotificationChannel, but only on API 26+ because
//        // the NotificationChannel class is not in the Support Library.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name = "notification_channel"
//            val descriptionText = "notification_channel_descriptor"
//            val importance = NotificationManager.IMPORTANCE_DEFAULT
//            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
//                description = descriptionText
//            }
//            // Register the channel with the system.
//            val notificationManager: NotificationManager =
//                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
}
