package org.example.project

import android.app.Service
import android.content.ClipboardManager
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import androidx.core.content.getSystemService

class ClipboardHandlerService: Service() {

    private lateinit var clipboardManager: ClipboardManager
        // getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

    override fun onBind(intent: Intent?): IBinder? {
        println("ClipboardHandlerService, onBind() start")
        return null
    }

    override fun onCreate() {
        println("ClipboardHandlerService, onCreate() start")
        super.onCreate()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("ClipboardHandlerService, onStartCommand() start")
        clipboardManager = getSystemService<ClipboardManager>() as ClipboardManager

        clipboardManager.addPrimaryClipChangedListener {
            val item = clipboardManager.primaryClip?.getItemAt(0)
            val data = item?.text?.toString() ?: ""

            println("clipboard was changed")
            println("clipboard buffer is: '$data'")
        }

        Toast.makeText(this, "service started", Toast.LENGTH_LONG).show()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        println("ClipboardHandlerService, onDestroy() start")
        super.onCreate()

    }

}