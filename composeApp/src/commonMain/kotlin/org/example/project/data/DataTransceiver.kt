package org.example.project.data

//import org.example.project.data.DataConverter
//import org.example.project.data.FileManager
//import org.example.project.NotificationInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.min
import kotlin.math.round


class DataTransceiver(
    private var notifier : NotificationInterface,
    saveFileDir: String
) {

    companion object {
        private const val TAG = "DataTransceiver"

        const val PORT_NUMBER = 8888
        const val NUM_BYTES_PER_SIZE = 4
        const val CHUNK_SIZE = 4096
//         const val CHUNK_SIZE = 4096 * 4096

        private const val CONTINUE_TX_STR = "CONTINUE_TX"
        private val CONTINUE_TX_BYTES = DataConverter.string2Utf8(CONTINUE_TX_STR)
        private const val CANCEL_TX_STR = "CANCEL_TX"
        private val CANCEL_TX_BYTES = DataConverter.string2Utf8(CANCEL_TX_STR)

        private val txBuffer = ByteArray(CHUNK_SIZE)
        private val rxBuffer = ByteArray(CHUNK_SIZE)
    }

    var txState = DataTransferState.IDLE
    var rxState = DataTransferState.IDLE

    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    private var receptionProgressValue = 0f
    private var cancellationAfterRequestFlag = false

    private var txFilePackDscr: TxFilePackDescriptor? = null
    private var rxFilePackDscr: RxFilePackDescriptor? = null

    private lateinit var fm: FileManager

    init {
      fm = FileManager(saveFileDir)
    }

    fun shutdown() {
        inputStream = null
        outputStream = null
    }

    fun setStreams(inStream: InputStream, outStream : OutputStream) {
//        val inputFlow = inStream.bufferedReader().lineSequence().asFlow()
//
//        inputFlow.collectLatest { it->
//            println("setStreams(), ut: $it")
//        }

        println("start setStreams()")
        inputStream = inStream
        outputStream = outStream
    }

    suspend fun initiateDataTransmission(filePack: TxFilePackDescriptor) {
        println("start initiateDataTransmission, filePack.isEmpty() = ${filePack.isEmpty()}")
        println("start initiateDataTransmission, " +
                "cancellationAfterRequestFlag = $cancellationAfterRequestFlag")
        txFilePackDscr = filePack.copy()
        println("initiateDataTransmission, txFilePackDscr: ")
        println("$txFilePackDscr")

        txState = DataTransferState.READY_TO_TRANSMIT

//        notifier.showProgressDialog("Sending data", "Sending data 0.00 %") { dialog, _ ->
//            CoroutineScope(Dispatchers.IO).launch {
//                sendCancelTx()
//                notifier.disconnect()
//            }
//            notifier.showToast("Data transmission is canceled")
//
//            dialog.dismiss()
//        }

        sendTxRequest()
        if (cancellationAfterRequestFlag) {
            cancellationAfterRequestFlag = false
            cancelDataTransmission()
        }
    }

    private suspend fun sendReceptionProgress(rxProgress: String) {
        if (outputStream == null) {
            return
        }
        println("sending reception progress update, rxProgress = '$rxProgress'")

        // Add message type
        writeMessageType(MessageType.PROGRESS_RX)
        writeStringUtf8(rxProgress)
        flushOutput()
    }

    fun cancelDataTransmission() {
        println("start cancelDataTransmission(), txState = $txState")

        if (txState == DataTransferState.IDLE) {
            cancellationAfterRequestFlag = true
        } else if (txState == DataTransferState.READY_TO_TRANSMIT) {
            txState = DataTransferState.IDLE
            sendCancelTx()
            CoroutineScope(Dispatchers.IO).launch {
                notifier.cancelConnection()
            }
        } else if (txState == DataTransferState.ACTIVE) {
            txState = DataTransferState.CANCEL_BY_TX
        }
    }

    private fun sendCancelTx() {
        sendControlData(MessageType.CANCEL_TX)
    }

    private fun sendCancelRx() {
        sendControlData(MessageType.CANCEL_RX)
    }

    private fun sendReceptionDone() {
        sendControlData(MessageType.RECEPTION_DONE)
    }

    private fun sendTxRequest() {
        sendControlData(MessageType.TX_REQUEST)
    }

    private fun sendAcceptTx() {
        sendControlData(MessageType.ACCEPT_TX)
    }

    private fun sendDismissTx() {
        sendControlData(MessageType.DISMISS_TX)
    }

    private fun sendControlData(type: MessageType) {
        if (!isMessageControl(type)) {
            throw Exception("Message type $type is not a control message type")
        }
        if (outputStream == null) {
            return
        }
//        val outputStream = withContext(Dispatchers.IO) {
//            client.getOutputStream()
//        }
        println("sending message type: $type")
        writeMessageType(type)
        flushOutput()
    }

    private suspend fun sendFilePack() {
        println("start sendFilePack(), txFilePackDscr!!.isEmpty() = ${txFilePackDscr!!.isEmpty()}")
        if (outputStream == null ||
            txFilePackDscr == null ||
            txFilePackDscr!!.isEmpty() ) {
            return
        }

//        val outputStream = withContext(Dispatchers.IO) {
//            client.getOutputStream()
//        }

        sendFilePackDscr()

        run breakLabel@ {
            txFilePackDscr!!.dscrs.forEach { txFileDscr ->
                val status = sendFile(txFileDscr)

                when (status) {
                    DataTransferStatus.DONE -> {
                        println("the file '${txFileDscr.fileName}' is sent")
                        notifier.showToast("File '${txFileDscr.fileName}' is sent")
                    }

                    DataTransferStatus.CANCELED_BY_TX -> {
                        println("File transferring is canceled by transmitter side")
                        notifier.showToast("File transferring is canceled")
                        CoroutineScope(Dispatchers.IO).launch {
                            notifier.cancelConnection()
                        }
                        return@breakLabel
                    }

                    DataTransferStatus.CANCELED_BY_RX -> {
                        println("File transferring is canceled by receiver side")
                        notifier.dismissProgressDialog()
                        notifier.showToast("File transferring is canceled by receiver")
                        CoroutineScope(Dispatchers.IO).launch {
                            notifier.cancelConnection()
                        }
                        return@breakLabel
                    }

                    DataTransferStatus.ERROR -> {
                        println("unknown error occurred during data transmission")
                        CoroutineScope(Dispatchers.IO).launch {
                            notifier.cancelConnection()
                        }
                        throw Exception("unknown error occurred during data transmission")
                    }
                }
            }
        }
    }

    private fun sendFilePackDscr() {
        // Add message type
        writeMessageType(MessageType.FILE_PACK_DSCR)
        writeSize(txFilePackDscr!!.size)
        writeSize(txFilePackDscr!!.dscrs.size)
        println("txFilePackDscr.size = ${txFilePackDscr!!.size}")
        println("txFilePackDscr.dscrs.size = ${txFilePackDscr!!.dscrs.size}")
    }

    private suspend fun sendFile(txFileDscr: TxFileDescriptor): DataTransferStatus {
        println("sending file: '${txFileDscr.fileName}', file size: ${txFileDscr.fileSize}")
        // Add message type
        writeMessageType(MessageType.FILE)
        // Add file metadata
        writeFileMetadata(txFileDscr)
        // Add file chunk by chunk
        val status = copyDataToStream(txFileDscr)

        withContext(Dispatchers.IO) {
            txFileDscr.inputStream.close()
        }
        return status
    }

    private suspend fun receiveMessage() {
        if (inputStream == null) {
            return
        }

        when (readMessageType()) {
            MessageType.FILE_PACK_DSCR -> {
                receiveFilePackDscr()
            }
            MessageType.FILE -> {
                receiveFile()
            }
            MessageType.PROGRESS_RX -> {
                receiveProgressRx()
            }
            MessageType.CANCEL_TX -> {
                receiveCancelTx()
            }
            MessageType.CANCEL_RX -> {
                receiveCancelRx()
            }
            MessageType.RECEPTION_DONE -> {
                receiveReceptionDone()
            }
            MessageType.TX_REQUEST -> {
                receiveTxRequest()
            }
            MessageType.ACCEPT_TX -> {
                receiveAcceptTx()
            }
            MessageType.DISMISS_TX -> {
                receiveDismissTx()
            }
        }
    }

    private suspend fun receiveFilePackDscr() {
        println("receive file pack descriptor, rxState = $rxState")
        if (rxState == DataTransferState.READY_TO_RECEIVE)
        {
            rxState = DataTransferState.ACTIVE

            rxFilePackDscr = RxFilePackDescriptor()
            rxFilePackDscr!!.sizeTotal = readSize()
            rxFilePackDscr!!.numFiles = readSize()
            println("rxFilePackDscr.sizeTotal = ${rxFilePackDscr!!.sizeTotal}")
            println("rxFilePackDscr.numFiles = ${rxFilePackDscr!!.numFiles}")

            notifier.showProgressDialog("Receiving data") {
                CoroutineScope(Dispatchers.IO).launch {
                    sendCancelRx()
                }
                rxState = DataTransferState.CANCEL_BY_RX
                println("cancel button is pressed, rxState = $rxState")
            }
        }
    }

    private suspend fun receiveFile() {
        println("receive file, rxState = $rxState")

        if (rxState != DataTransferState.ACTIVE) {
            throw Exception("File reception on rx state = $rxState")
        }

        val rxFileDscr = RxFileDescriptor()
        rxFileDscr.fileNameReceived = readStringUtf8()
        if (rxFileDscr.fileNameReceived.isEmpty()) {
            throw Exception("empty file name")
        }
        println("rxFileDscr.fileNameReceived = ${rxFileDscr.fileNameReceived}")
        rxFileDscr.fileNameSaved = fm.getSaveFileName(rxFileDscr.fileNameReceived)
        println("rxFileDscr.fileNameSaved = ${rxFileDscr.fileNameSaved}")
        rxFileDscr.fileSize = readSize()
        println("rxFileDscr.fileSize = ${rxFileDscr.fileSize}")

        println("receiving file: '${rxFileDscr.fileNameReceived}', file size: ${rxFileDscr.fileSize}")

        val outputFileStream = fm.getOutFileStream(rxFileDscr.fileNameSaved)
        when (copyStreamToData(outputFileStream, rxFileDscr.fileSize)) {
            DataTransferStatus.DONE -> {
                println("the file '${{rxFileDscr.fileNameReceived}}' is received")
                println("new file '${rxFileDscr.fileNameReceived}' received ands saved as '${rxFileDscr.fileNameSaved}'")
                // notifier.showToast("File '${rxFileDscr.fileNameReceived}' received")
                rxFilePackDscr!!.add(rxFileDscr)

                if (rxFilePackDscr!!.isReceptionFinished()) {
                    notifier.dismissProgressDialog()
                    sendReceptionDone()
                    rxState = DataTransferState.IDLE

                    CoroutineScope(Dispatchers.IO).launch {
                        notifier.disconnect()
                    }
                }
            }
            DataTransferStatus.CANCELED_BY_TX -> {
                println("File transferring '${rxFileDscr.fileNameReceived}' is canceled by transmitter side")
                notifier.showToast("Data transmission is canceled by transmitter")
                notifier.dismissProgressDialog()
                fm.deleteReceivedFiles(rxFilePackDscr!!.dscrs)
                fm.deleteFile(rxFileDscr.fileNameReceived)
                rxState = DataTransferState.IDLE
                CoroutineScope(Dispatchers.IO).launch {
                    notifier.cancelConnection()
                }
            }
            DataTransferStatus.CANCELED_BY_RX -> {
                println("File transferring '${rxFileDscr.fileNameReceived}' is canceled by receiver side")
                notifier.showToast("Data transmission is canceled by receiver")
                notifier.dismissProgressDialog()
                fm.deleteReceivedFiles(rxFilePackDscr!!.dscrs)
                fm.deleteFile(rxFileDscr.fileNameReceived)
                rxState = DataTransferState.IDLE
                CoroutineScope(Dispatchers.IO).launch {
                    notifier.cancelConnection()
                }
            }
            DataTransferStatus.ERROR -> {
                println("unknown data transfer status error during data reception")
                fm.deleteFile(rxFileDscr.fileNameReceived)
                CoroutineScope(Dispatchers.IO).launch {
                    notifier.cancelConnection()
                }
                throw Exception("unknown data transfer status error during data reception")
            }
        }

        withContext(Dispatchers.IO) {
            outputFileStream.close()
        }
    }

    private suspend fun receiveProgressRx() {
        val rxProgress = readStringUtf8()
        receptionProgressValue = rxProgress.toFloat()
        println("receive reception progress update, receptionProgressValue = $receptionProgressValue")
        if (txState == DataTransferState.ACTIVE) {
            notifier.updateProgressDialog(receptionProgressValue)
        }
    }

    private fun receiveCancelTx() {
        println("Receive cancel rx flag, rxState = $rxState")
        if (rxState == DataTransferState.READY_TO_RECEIVE) {
            CoroutineScope(Dispatchers.IO).launch {
                notifier.dismissAlertDialog()
                // notifier.dismissProgressDialog()
                notifier.cancelConnection()
            }
        } else if (rxState == DataTransferState.ACTIVE) {
            txState = DataTransferState.CANCEL_BY_RX
        }
        notifier.showToast("Data transmission is canceled by receiver")
        rxState = DataTransferState.IDLE
    }

    private fun receiveCancelRx() {
        println("Receive cancel rx flag, txState = $txState")
        if (txState == DataTransferState.ACTIVE) {
            txState = DataTransferState.CANCEL_BY_RX
            notifier.showToast("Data transmission is canceled by receiver")
        }
    }

    private suspend fun receiveReceptionDone() {
        println("receive reception flag, txState = $txState")
        if (txState == DataTransferState.ACTIVE) {
            notifier.dismissProgressDialog()
            txState = DataTransferState.IDLE
            txFilePackDscr = null
            notifier.disconnect()
        }
    }

    private suspend fun receiveTxRequest() {
        println("receive tx request, rxState = $rxState")
        if (rxState == DataTransferState.IDLE) {
            rxState = DataTransferState.READY_TO_RECEIVE
            notifier.showAlertDialog(
                message = "Do you want to receive data from 'transmitter'?",
                dismissCallback = {
                    rxState = DataTransferState.IDLE
                    CoroutineScope(Dispatchers.IO).launch {
                        sendDismissTx()
                        notifier.cancelConnection()
                    }
                },
                confirmCallback = {
                    CoroutineScope(Dispatchers.IO).launch {
                        sendAcceptTx()
                    }
                }
            )
        }
    }

    private suspend fun receiveAcceptTx() {
        println("receive accept tx, txState = $txState")
        if (txState == DataTransferState.READY_TO_TRANSMIT) {
            txState = DataTransferState.ACTIVE
            CoroutineScope(Dispatchers.IO).launch {
                sendFilePack()
            }
        }
    }

    private suspend fun receiveDismissTx() {
        println("receive dismiss tx, txState = $txState")
        if (txState == DataTransferState.READY_TO_TRANSMIT) {
            txState = DataTransferState.IDLE
            txFilePackDscr = null

            notifier.dismissProgressDialog()
            notifier.cancelConnection()
        }
    }

    suspend fun transmissionFlow(txFilePack: TxFilePackDescriptor) {
        if (txFilePack.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                initiateDataTransmission(txFilePack)
            }
        }
    }

    suspend fun receptionFlow() {
        while (true) {
            try {
                receiveMessage()
            } catch (e: Exception) {
                println("receptionFlow(), reception process exception: " + e.message.toString())
                break
            }
        }
    }

    private fun readMessageType() : MessageType {
        val str = readStringUtf8()
//        println("received message type = $str")
        return MessageType.valueOf(str)
    }

    private fun readSize() : Int {
        return DataConverter.bytes2value(readData(NUM_BYTES_PER_SIZE))
    }

    private fun readStringUtf8() : String {
        return readData(readSize()).toString(Charsets.UTF_8)
    }

    private fun readData(numBytes :Int) : ByteArray {
        if (numBytes < 0) {
            throw Exception("Negative number of read bytes: $numBytes")
        }
        if (numBytes > CHUNK_SIZE) {
            throw Exception("Number of read bytes is greater than CHUNK_SIZE," +
                    "numBytes = $numBytes, CHUNK_SIZE = $CHUNK_SIZE")
        }
//        println("start readData(), numBytes = $numBytes")

        var numBytesRead = 0
        while (numBytesRead < numBytes) {
            val num = inputStream!!.read(rxBuffer, numBytesRead, numBytes - numBytesRead)
//            println("num read byte: $num, numBytesRead: $numBytesRead, numBytes: $numBytes")
//            if (num <= 0) {
//                throw Exception("Something is wrong with the connection...")
//            }
            numBytesRead += num
        }

        return if (rxBuffer.size == numBytes) {
            rxBuffer
        } else {
            rxBuffer.slice(0..<numBytes).toByteArray()
        }
    }

    private suspend fun copyDataToStream(txFileDscr: TxFileDescriptor): DataTransferStatus {
        val fileSize = txFileDscr.fileSize
        val inputFileStream = txFileDscr.inputStream
        var status = DataTransferStatus.DONE
        var numBytesTotal = 0

        try {
            while (numBytesTotal < fileSize) {
                when (txState) {
                    DataTransferState.CANCEL_BY_TX,
                    DataTransferState.CANCEL_BY_RX -> {
                        println("cancelling file transmission")
                        writeBytes(CANCEL_TX_BYTES)

                        status = getDataTransferStatus(txState)
                        break
                    }
                    else -> {
                        val num = min(fileSize - numBytesTotal, CHUNK_SIZE)
                        withContext(Dispatchers.IO) {
                            inputFileStream.read(txBuffer, 0, num)
                        }
                        writeFileChunk(num)
                        numBytesTotal += num
                    }
                }
                flushOutput()
            }

        } catch (e: IOException) {
            println("copyDataToStream exception: $e")
            status = getDataTransferStatus(txState)
        }

        return status
    }

    private suspend fun copyStreamToData(outputFileStream: OutputStream,
                                         fileSize: Int): DataTransferStatus {
        var status = DataTransferStatus.DONE
        var numBytesTotal = 0
        var prevRatio = -1f
        try {
            while (numBytesTotal < fileSize) {
                val tag = readStringUtf8()
                if (tag == CONTINUE_TX_STR) {
                    val num = readSize()
                    readData(num)
                    withContext(Dispatchers.IO) {
                        outputFileStream.write(rxBuffer, 0, num)
                    }
                    numBytesTotal += num
                    rxFilePackDscr!!.sizeReceived += num
                } else if (tag == CANCEL_TX_STR) {
                    println("file transmission is canceled")
                    rxState = DataTransferState.CANCEL_BY_TX
                    status = getDataTransferStatus(rxState)
                    break
                }

                val ratio = rxFilePackDscr!!.getReceivedPercent()
                if (round(ratio) > prevRatio) {
                    sendReceptionProgress(ratio.toString())
                    prevRatio = round(ratio)
                    notifier.updateProgressDialog(ratio)
                }
            }
        } catch (e: IOException) {
            println("copyStreamToData exception: $e")
            status = getDataTransferStatus(rxState)
        }

        return status
    }

    private fun writeFileMetadata(txFileDscr: TxFileDescriptor) {
        writeStringUtf8(txFileDscr.fileName)
        writeSize(txFileDscr.fileSize)
    }

    private fun writeMessageType(type: MessageType) {
        val typeStr = type.toString()
        writeStringUtf8(typeStr)
    }

    private fun writeSize(value: Int) {
        outputStream!!.write(DataConverter.value2bytes(value, NUM_BYTES_PER_SIZE))
    }

    private fun writeStringUtf8(string: String) {
        val strUtf8 = DataConverter.string2Utf8((string))
        writeSize(strUtf8.size)
        outputStream!!.write(strUtf8)
    }

    private fun writeBytes(bytes: ByteArray) {
        writeBytes(bytes, bytes.size)
    }

    private fun writeBytes(bytes: ByteArray, numBytes: Int) {
        writeSize(numBytes)
        outputStream!!.write(bytes)
    }

    private fun writeFileChunk(numBytes: Int) {
        writeBytes(CONTINUE_TX_BYTES)
        writeSize(numBytes)
        outputStream!!.write(txBuffer, 0, numBytes)
        flushOutput()
    }

    private fun flushOutput() {
        outputStream?.flush()
    }

    private fun getDataTransferStatus(state: DataTransferState) : DataTransferStatus {
        if (state == DataTransferState.CANCEL_BY_TX) {
            return DataTransferStatus.CANCELED_BY_TX
        } else if (state == DataTransferState.CANCEL_BY_RX) {
            return DataTransferStatus.CANCELED_BY_RX
        } else {
            return DataTransferStatus.ERROR
        }
    }
}