package org.example.project.data

import org.example.project.utils.DataConverter
import org.example.project.utils.MessageType


open class ControlMessage(
    val type: MessageType
) {

    private val NUM_BYTES_PER_SIZE = 4
    var txName = ""
    var progressStr = ""

    fun serialize(): ByteArray {
        var bytes = typeToByteArray()

        when(type) {
            MessageType.CANCEL_TX -> {}
            MessageType.CANCEL_RX -> {}
            MessageType.PROGRESS_RX -> {
                bytes += stringUtf8ToByteArray(progressStr)
            }
            MessageType.RECEPTION_DONE -> {}
            MessageType.TX_REQUEST -> {
                bytes += stringUtf8ToByteArray(txName)
            }
            MessageType.ACCEPT_TX -> {}
            MessageType.DISMISS_TX -> {}
            MessageType.TEST_MESSAGE -> {}
            MessageType.FILE_COMMON_DSCR -> {}
            MessageType.FILE -> {}
        }

//        println("serialize, bytes:")
//        for (b in bytes) {
//            println("${b.toInt()}")
//        }
        return bytes
    }

    private fun getSize(): Int {
        val size = type.toString().length
        return size + when(type) {
            MessageType.CANCEL_TX -> 0
            MessageType.CANCEL_RX -> 0
            MessageType.PROGRESS_RX -> {
                progressStr.length
            }
            MessageType.RECEPTION_DONE -> 0
            MessageType.TX_REQUEST -> {
                txName.length
            }
            MessageType.ACCEPT_TX -> 0
            MessageType.DISMISS_TX -> 0
            MessageType.TEST_MESSAGE -> 0
            MessageType.FILE_COMMON_DSCR -> 0
            MessageType.FILE -> 0
        }
    }

    private fun typeToByteArray(): ByteArray {
        return stringUtf8ToByteArray(type.toString())
    }

    private fun stringUtf8ToByteArray(string: String): ByteArray {
        val strUtf8 = DataConverter.string2Utf8((string))
        return sizeToByteArray(strUtf8.size) + strUtf8
    }

    private fun sizeToByteArray(value: Int): ByteArray {
       return DataConverter.value2bytes(value, NUM_BYTES_PER_SIZE)
    }
}

class TxRequestMessage(
    name: String
) : ControlMessage(MessageType.TX_REQUEST) {
    init {
        txName = name
    }
}

class RxProgressMessage(
    progress: String
) : ControlMessage(MessageType.PROGRESS_RX) {
    init {
        progressStr = progress
    }
}