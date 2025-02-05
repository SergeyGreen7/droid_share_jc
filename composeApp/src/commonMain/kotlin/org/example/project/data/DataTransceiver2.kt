package org.example.project.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.project.utils.DataConverter
import org.example.project.utils.DataTransferState
import org.example.project.utils.DataTransferStatus
import org.example.project.utils.MessageType
import org.example.project.utils.NotificationInterface
import org.example.project.utils.RxFileDescriptor
import org.example.project.utils.RxFilesDescriptor
import org.example.project.utils.TxFileDescriptor
import org.example.project.utils.TxFilesDescriptor
import org.example.project.utils.isMessageControl
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.min
import kotlin.math.round

class DataTransceiver2(
    private var notifier : NotificationInterface,
    saveFileDir: String
) {
    private val NUM_BYTES_PER_SIZE = 4
    private val CHUNK_SIZE = 4096
//        private const val CHUNK_SIZE = 4096 * 4096
    private val CONTINUE_TX_STR = "CONTINUE_TX"
    private val CONTINUE_TX_BYTES = DataConverter.string2Utf8(CONTINUE_TX_STR)
    private val CANCEL_TX_STR = "CANCEL_TX"
    private val CANCEL_TX_BYTES = DataConverter.string2Utf8(CANCEL_TX_STR)

    private var transmitterName = ""
    private var continueRxJob = true

    private val txBuffer = ByteArray(CHUNK_SIZE)
    private val rxBuffer = ByteArray(CHUNK_SIZE)

    private var rxState = DataTransferState.IDLE
    private var txState = DataTransferState.IDLE

    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    private var receptionProgressValue = 0f
    private var cancellationFlag = false

    private var txFiles: TxFilesDescriptor? = null
    private var rxFiles: RxFilesDescriptor? = null

    private var fileManager: FileManager = FileManager(saveFileDir)

//    println("inputStream = $inputStream")
//    println("outputStream = $outputStream")
//    println("rxState = $rxState")
//    println("txState = $txState")
//    println("txFiles = $txFiles")
//    println("rxFiles = $rxFiles")

    fun setTransmitterName(name: String) {
        transmitterName = name
    }

    fun reset() {
        continueRxJob = false
        inputStream = null
        outputStream = null
        rxState = DataTransferState.IDLE
        txState = DataTransferState.IDLE
        txFiles = null
        rxFiles = null
    }

    fun configureStreams(inStream: InputStream, outStream : OutputStream) {
        println("start configureStreams(), txState = $txState, rxState = $rxState")
        inputStream = inStream
        outputStream = outStream
    }

    fun initiateDataTransmission(filePack: TxFilesDescriptor) {
        println("start initiateDataTransmission, filePack.isEmpty() = ${filePack.isEmpty()}")
        println("   txState = $txState, rxState = $rxState")
        println("   cancellationAfterRequestFlag = $cancellationFlag")
        txFiles = filePack.clone()
        println("   txFiles: ")
        println("   $txFiles")

        txState = DataTransferState.READY_TO_TRANSMIT

        sendTxRequest()
        if (cancellationFlag) {
            cancellationFlag = false
            cancelDataTransmission()
        }
    }

    private fun sendReceptionProgress(rxProgress: String) {
        sendControlData(RxProgressMessage(rxProgress))
    }

    fun cancelDataTransmission() {
        println("start cancelDataTransmission(), txState = $txState, rxState = $rxState")

        when (txState) {
            DataTransferState.IDLE -> {
                cancellationFlag = true
            }
            DataTransferState.READY_TO_TRANSMIT -> {
                cancellationFlag = true

                txState = DataTransferState.IDLE
                CoroutineScope(Dispatchers.IO).launch {
                    sendCancelTx()
                }
                println("run notifier.cancelConnection()")
                notifier.cancelConnection()
            }
            DataTransferState.ACTIVE -> {
                txState = DataTransferState.CANCEL_BY_TX
            }
            else -> {
                error("Wrong txState")
            }
        }
    }

    private fun sendCancelTx() {
        sendControlData(ControlMessage(MessageType.CANCEL_TX))
    }

    private fun sendCancelRx() {
        sendControlData(ControlMessage(MessageType.CANCEL_RX))
    }

    private fun sendReceptionDone() {
        sendControlData(ControlMessage(MessageType.RECEPTION_DONE))
    }

    private fun sendTxRequest() {
        sendControlData(TxRequestMessage(transmitterName))
    }

    private fun sendAcceptTx() {
        sendControlData(ControlMessage(MessageType.ACCEPT_TX))
    }

    private fun sendDismissTx() {
        sendControlData(ControlMessage(MessageType.DISMISS_TX))
    }

    private fun sendControlData(message: ControlMessage) {
        if (!isMessageControl(message.type)) {
            throw Exception("Message type ${message.type} is not a control message type")
        }
        if (outputStream == null) {
            return
        }
        println("sending message type: ${message.type}, txState = $txState, rxState = $rxState")
        writeToOutStream(message.serialize())
        flushOutput()
    }

    private suspend fun sendFiles() {
        println("start sendFilePack(), txState = $txState, rxState = $rxState")
        if (outputStream == null ||
            txFiles == null ||
            txFiles!!.isEmpty() ) {
            return
        }

        sendFileCommonDscr()

        run breakLabel@ {
            txFiles!!.dscrs.forEach { txFileDscr ->
                val status = sendFile(txFileDscr)

                when (status) {
                    DataTransferStatus.DONE -> {
                        println("the file '${txFileDscr.fileName}' is sent")
                        notifier.showNotification("File '${txFileDscr.fileName}' is sent")
                    }

                    DataTransferStatus.CANCELED_BY_TX -> {
                        println("File transferring is canceled by transmitter side")
                        notifier.showNotification("File transferring is canceled")
                        // CoroutineScope(Dispatchers.IO).launch {
                        notifier.cancelConnection()
                        //}
                        return@breakLabel
                    }

                    DataTransferStatus.CANCELED_BY_RX -> {
                        println("File transferring is canceled by receiver side")
                        notifier.dismissProgressDialog()
                        notifier.showNotification("File transferring is canceled by receiver")
                        // CoroutineScope(Dispatchers.IO).launch {
                        notifier.cancelConnection()
                        //}
                        return@breakLabel
                    }

                    DataTransferStatus.ERROR -> {
                        println("unknown error occurred during data transmission")
                        // CoroutineScope(Dispatchers.IO).launch {
                        notifier.cancelConnection()
                        // }
                        throw Exception("unknown error occurred during data transmission")
                    }
                }
            }
        }
    }

    private fun sendFileCommonDscr() {
        println("start sendFileCommonDscr(), txState = $txState, rxState = $rxState")
        writeMessageType(MessageType.FILE_COMMON_DSCR)
        writeSize(txFiles!!.size)
        writeSize(txFiles!!.dscrs.size)
        println("txFiles.size = ${txFiles!!.size}")
        println("txFiles.dscrs.size = ${txFiles!!.dscrs.size}")
    }

    private suspend fun sendFile(txFileDscr: TxFileDescriptor): DataTransferStatus {
        println("sendFile(), file name: '${txFileDscr.fileName}', " +
                "file size: ${txFileDscr.fileSize}" +
                ", txState = $txState, rxState = $rxState")
        writeMessageType(MessageType.FILE)
        writeFileMetadata(txFileDscr)
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
            MessageType.FILE_COMMON_DSCR -> {
                receiveFileCommonDscr()
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
            MessageType.TEST_MESSAGE -> {
                receiveTestMessage()
            }
        }
    }

    private suspend fun receiveFileCommonDscr() {
        println("receive file common descriptor, txState = $txState, rxState = $rxState")
        if (rxState == DataTransferState.READY_TO_RECEIVE)
        {
            rxState = DataTransferState.ACTIVE

            rxFiles = RxFilesDescriptor()
            rxFiles!!.sizeTotal = readSize()
            rxFiles!!.numFiles = readSize()
            println("rxFilePackDscr.sizeTotal = ${rxFiles!!.sizeTotal}")
            println("rxFilePackDscr.numFiles = ${rxFiles!!.numFiles}")

            notifier.showProgressDialog("Receiving data") {
                // CoroutineScope(Dispatchers.IO).launch {
                sendCancelRx()
                // }
                rxState = DataTransferState.CANCEL_BY_RX
                println("cancel button is pressed, txState = $txState, rxState = $rxState")
            }
        }
    }

    private suspend fun receiveFile() {
        println("receive file, txState = $txState, rxState = $rxState")

        if (rxState != DataTransferState.ACTIVE) {
            throw Exception("File reception on rx state = $rxState")
        }

        val fileNameReceived = readStringUtf8()
        val fileNameSaved = fileManager.getSaveFileName(fileNameReceived)
        val fileSize = readSize()
        if (fileNameReceived.isEmpty()) {
            throw Exception("empty file name")
        }

        val rxFileDscr = RxFileDescriptor(fileNameReceived, fileNameSaved, fileSize)
        println("fileNameReceived = ${rxFileDscr.fileNameReceived}")
        println("fileNameSaved = ${rxFileDscr.fileNameSaved}")
        println("fileSize = ${rxFileDscr.fileSize}")

        val outputFileStream = fileManager.getOutFileStream(rxFileDscr.fileNameSaved)
        when (copyStreamToData(outputFileStream, rxFileDscr.fileSize)) {
            DataTransferStatus.DONE -> {
                println("the file '${{rxFileDscr.fileNameReceived}}' is received")
                println("new file '${rxFileDscr.fileNameReceived}' received and saved as '${rxFileDscr.fileNameSaved}'")
                notifier.showNotification("File '${rxFileDscr.fileNameReceived}' received")
                rxFiles!!.add(rxFileDscr)

                if (rxFiles!!.isReceptionFinished()) {
                    notifier.dismissProgressDialog()
                    sendReceptionDone()
                    // notifier.disconnect()

                    // CoroutineScope(Dispatchers.IO).launch {
                    notifier.disconnect()
                    //}
                }
            }
            DataTransferStatus.CANCELED_BY_TX -> {
                println("File transferring '${rxFileDscr.fileNameReceived}' is canceled by transmitter side")
                notifier.showNotification("Data transmission is canceled by transmitter")
                notifier.dismissProgressDialog()
                fileManager.deleteReceivedFiles(rxFiles!!.dscrs)
                fileManager.deleteFile(rxFileDscr.fileNameReceived)
                rxState = DataTransferState.IDLE
                // CoroutineScope(Dispatchers.IO).launch {
                notifier.cancelConnection()
                // }
            }
            DataTransferStatus.CANCELED_BY_RX -> {
                println("File transferring '${rxFileDscr.fileNameReceived}' is canceled by receiver side")
                notifier.showNotification("Data transmission is canceled by receiver")
                notifier.dismissProgressDialog()
                fileManager.deleteReceivedFiles(rxFiles!!.dscrs)
                fileManager.deleteFile(rxFileDscr.fileNameReceived)
                rxState = DataTransferState.IDLE
                // CoroutineScope(Dispatchers.IO).launch {
                notifier.cancelConnection()
                // }
            }
            DataTransferStatus.ERROR -> {
                println("unknown data transfer status error during data reception")
                fileManager.deleteFile(rxFileDscr.fileNameReceived)
                // CoroutineScope(Dispatchers.IO).launch {
                notifier.cancelConnection()
                // }
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
        println("receive reception progress update, " +
                "receptionProgressValue = $receptionProgressValue" +
                ", txState = $txState, rxState = $rxState")
        if (txState == DataTransferState.ACTIVE) {
            notifier.updateProgressDialog(receptionProgressValue)
        }
    }

    private fun receiveCancelTx() {
        println("Receive cancel rx flag, txState = $txState, rxState = $rxState")
        if (rxState == DataTransferState.READY_TO_RECEIVE) {
            // CoroutineScope(Dispatchers.IO).launch {
            notifier.dismissAlertDialog()
            notifier.cancelConnection()
            // }
        } else if (rxState == DataTransferState.ACTIVE) {
            txState = DataTransferState.CANCEL_BY_RX
        }
        notifier.showNotification("Data transmission is canceled by receiver")
        rxState = DataTransferState.IDLE
    }

    private fun receiveCancelRx() {
        println("Receive cancel rx flag, txState = $txState, rxState = $rxState")
        if (txState == DataTransferState.ACTIVE) {
            txState = DataTransferState.CANCEL_BY_RX
            notifier.showNotification("Data transmission is canceled by receiver")
        }
    }

    private suspend fun receiveReceptionDone() {
        println("receive reception flag, txState = $txState, rxState = $rxState")
        if (txState == DataTransferState.ACTIVE) {
            notifier.dismissProgressDialog()
            txState = DataTransferState.IDLE
            txFiles = null
            notifier.disconnect()
        }
    }

    private suspend fun receiveTxRequest() {
        println("receive tx request, txState = $txState, rxState = $rxState")

        val transmitterName = readStringUtf8()

        if (rxState == DataTransferState.IDLE) {
            rxState = DataTransferState.READY_TO_RECEIVE
            notifier.showAlertDialog(
                message = "Do you want to receive data from '$transmitterName'?",
                dismissCallback = {
                    rxState = DataTransferState.IDLE
                    CoroutineScope(Dispatchers.IO).launch {
                        sendDismissTx()
                    }
                    notifier.cancelConnection()
                },
                confirmCallback = {
                    CoroutineScope(Dispatchers.IO).launch {
                        sendAcceptTx()
                    }
                }
            )
        }
    }

    private fun receiveAcceptTx() {
        println("receive accept tx, txState = $txState, rxState = $rxState")
        if (txState == DataTransferState.READY_TO_TRANSMIT) {

            if (cancellationFlag) {

            } else {
                txState = DataTransferState.ACTIVE
                CoroutineScope(Dispatchers.IO).launch {
                    sendFiles()
                }
            }
        }
    }

    private suspend fun receiveDismissTx() {
        println("receive dismiss tx, txState = $txState, rxState = $rxState")
        if (txState == DataTransferState.READY_TO_TRANSMIT) {
            txState = DataTransferState.IDLE
            txFiles = null

            notifier.dismissProgressDialog()
            notifier.cancelConnection()
        }
    }

    private fun receiveTestMessage() {
        println("receive test message, txState = $txState, rxState = $rxState")
    }

    fun transmissionFlow(txFiles: TxFilesDescriptor) {
        println("transmissionFlow(), txFiles.isNotEmpty() = ${txFiles.isNotEmpty()}")
        if (txFiles.isNotEmpty()) {
            initiateDataTransmission(txFiles)
        }
    }

    suspend fun startReceptionFlow() {
        continueRxJob = true
        while (continueRxJob) {
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
        val messageType = MessageType.valueOf(str)
        println("received messageType = $messageType")
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
            error("Number of read bytes is greater than CHUNK_SIZE," +
                    "numBytes = $numBytes, CHUNK_SIZE = $CHUNK_SIZE")
        }

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
        var prevPercent = -1f
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
                    rxFiles!!.sizeReceived += num
                } else if (tag == CANCEL_TX_STR) {
                    println("file transmission is canceled")
                    rxState = DataTransferState.CANCEL_BY_TX
                    status = getDataTransferStatus(rxState)
                    break
                }

                val percent = rxFiles!!.getReceivedPercentFloat()
                if (round(percent) > prevPercent) {
                    prevPercent = round(percent)
                    val progressStr = "%.0f".format(percent)
                    sendReceptionProgress(progressStr)
                    notifier.updateProgressDialog(percent)
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
        if (outputStream == null) {
            println("outputStream is null")
        }
        writeToOutStream(DataConverter.value2bytes(value, NUM_BYTES_PER_SIZE))
    }

    private fun writeStringUtf8(string: String) {
        val strUtf8 = DataConverter.string2Utf8((string))
        writeSize(strUtf8.size)
        println("writeStringUtf8, size = ${strUtf8.size}, string = $string")
        writeToOutStream(strUtf8)
    }

    private fun writeBytes(bytes: ByteArray) {
        writeBytes(bytes, bytes.size)
    }

    private fun writeBytes(bytes: ByteArray, numBytes: Int) {
        writeSize(numBytes)
        writeToOutStream(bytes)
    }

    private fun writeFileChunk(numBytes: Int) {
        writeBytes(CONTINUE_TX_BYTES)
        writeSize(numBytes)
        outputStream!!.write(txBuffer, 0, numBytes)
        flushOutput()
    }

    private fun writeToOutStream(bytes: ByteArray) {
        if (outputStream == null) {
            println("writeToOutStream(), outputStream is null!")
        }
        outputStream!!.write(bytes)
    }

    private fun flushOutput() {
        outputStream?.flush()
    }

    private fun getDataTransferStatus(state: DataTransferState) : DataTransferStatus {
        return when (state) {
            DataTransferState.CANCEL_BY_TX -> DataTransferStatus.CANCELED_BY_TX
            DataTransferState.CANCEL_BY_RX -> DataTransferStatus.CANCELED_BY_RX
            else -> DataTransferStatus.ERROR
        }
    }
}