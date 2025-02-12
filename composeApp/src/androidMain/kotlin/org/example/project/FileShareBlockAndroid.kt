package org.example.project

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log

import io.github.vinceglb.filekit.core.PlatformFiles

import org.example.project.utils.TxFileDescriptor
import org.example.project.fragments.FileShareBlockCommon
import org.example.project.ui.*
import java.io.InputStream

class FileShareBlockAndroid (
    vm: FileShareViewModel,
    saveFileDir: String,
) : FileShareBlockCommon(
    vm, saveFileDir
) {

    companion object {
        private const val TAG = "FileShareFragment"
    }

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "FileShareFragment, start onCreate()")

        val sb = StringBuilder("MODEL: "+android.os.Build.MODEL
        +"\nDEVICE: "+android.os.Build.DEVICE
        +"\nBRAND: "+android.os.Build.BRAND
        +"\nDISPLAY: "+android.os.Build.DISPLAY
        +"\nBOARD: "+android.os.Build.BOARD
        +"\nHOST: "+android.os.Build.HOST
        +"\nMANUFACTURER: "+android.os.Build.MANUFACTURER
        +"\nPRODUCT: "+android.os.Build.PRODUCT)
        Log.d(TAG, "$sb")
    }

        fun resolveNewIntent(intent: Intent) {
        println("start resolveNewIntent, intent = $intent")
        if (intent.action !in listOf(Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE)){
            return
        }
        println("run geFilePacketDescriptorFromIntent()")
        geFilePacketDescriptorFromIntent(intent)
    }

    private fun geFilePacketDescriptorFromIntent(intent: Intent) {
        Log.d(TAG, "start resolveIntent()")
        val uriList = mutableListOf<Uri>()

        if (intent.action !in listOf(Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE)){
            return
        }
        Log.d(TAG, "ACTION_SEND")

        if (intent.clipData != null) {
            for (i in 0..<intent.clipData!!.itemCount) {
                val uri = intent.clipData!!.getItemAt(i).uri
                Log.d(TAG, "multiple URIs: = $uri")
                uriList.add(uri)
            }
        } else {
            if (intent.data != null) {
                Log.d(TAG, "single URI: = ${intent.data}")
                uriList.add(intent.data!!)
            }
        }

        txFiles.clear()
        for (uri in uriList) {
            addFileDescriptor(uri)
        }

        if (txFiles.isNotEmpty()) {
            vm.onDataSelection()
        }
    }

    override fun getFileDescriptorFromPickerImpl( files: PlatformFiles? ) {
        txFiles.clear()
        if (!files.isNullOrEmpty()) {
            println("$files")

            files.forEach { file ->
                println("file = $file")
                addFileDescriptor(file.uri)
            }
            vm.onDataSelection()
        }
    }

    @SuppressLint("Range")
    private fun addFileDescriptor(uri: Uri) {
        val cursor = appContext.contentResolver?.
        query(uri, null, null, null, null) as Cursor
        cursor.moveToFirst()
        val fileName = cursor.getString(
            cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        val fileSize = cursor.getString(
            cursor.getColumnIndex(OpenableColumns.SIZE))?.toInt() as Int
        cursor.close()
        val inputStream = appContext.contentResolver?.openInputStream(uri) as InputStream

        println("fileName = $fileName")
        println("fileSize = $fileSize")
        println("inputStream = $inputStream")
        txFiles.add(TxFileDescriptor(fileName, fileSize, inputStream))
    }
}