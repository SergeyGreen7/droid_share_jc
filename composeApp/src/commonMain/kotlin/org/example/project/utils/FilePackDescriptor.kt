package org.example.project.utils

import java.io.InputStream

data class RxFileDescriptor (
    val fileNameReceived: String,
    val fileNameSaved: String,
    val fileSize: Int
) {}

data class TxFileDescriptor(
    val fileName: String,
    val fileSize: Int,
    val inputStream: InputStream
) {}

class TxFilesDescriptor() {
    var size = 0
    var dscrs  = mutableListOf<TxFileDescriptor>()

    fun clone(): TxFilesDescriptor {
        val clone = TxFilesDescriptor()
        this.dscrs.forEach {
            clone.add(TxFileDescriptor(
                it.fileName,
                it.fileSize,
                it.inputStream))
        }
        return clone
    }
    fun clear() {
        size = 0
        dscrs.clear()
    }
    fun add(dscr: TxFileDescriptor) {
        size += dscr.fileSize
        dscrs.add(dscr)
    }
    fun isEmpty(): Boolean {
        return dscrs.isEmpty()
    }
    fun isNotEmpty(): Boolean {
        return dscrs.isNotEmpty()
    }
    override fun toString(): String {
        val sb = StringBuilder()
            .append("size = $size\n")
        for (dscr in dscrs) {
            sb.append(" - $dscr\n")
        }
        return sb.toString()
    }
}

class RxFilesDescriptor() {
    var numFiles = 0
    var sizeTotal = 0
    var sizeReceived = 0
    var dscrs = mutableListOf<RxFileDescriptor>()

    fun clear() {
        numFiles = 0
        sizeTotal = 0
        sizeReceived = 0
        dscrs.clear()
    }
    fun add(dscr: RxFileDescriptor) {
        dscrs.add(dscr)
    }
    fun isReceptionFinished(): Boolean {
        return dscrs.size == numFiles
    }
    fun getReceivedPercentFloat(): Float {
        return sizeReceived.toFloat() / sizeTotal.toFloat() * 100f
    }
}