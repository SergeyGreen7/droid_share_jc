package org.example.project

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE

@SuppressLint("ServiceCast")
class AndroidClipboardHandler: ClipboardHandler() {
    private var clipboardManager : ClipboardManager = appContext
        .getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

    private var clipboardContentMonitor = {
        newTextEvent()
    }

    override fun enable() {
        clipboardManager.addPrimaryClipChangedListener () {
            clipboardContentMonitor()
        }
    }

    override fun disable() {
        clipboardManager.removePrimaryClipChangedListener {
            clipboardContentMonitor()
        }
    }

    override fun getDataFromClipboard(): String {
        val item = clipboardManager.primaryClip?.getItemAt(0)
        return item?.text?.toString() ?: ""
    }

    override fun putDataIntoClipboard(data: String) {
        val clipData = ClipData.newPlainText("new data", data)
        clipboardManager.setPrimaryClip(clipData)
    }

}

actual fun getClipboardHandler(): ClipboardHandler = AndroidClipboardHandler()