package org.example.project.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.example.project.getClipboardHandler
import org.example.project.utils.NotificationInterface
import org.example.project.utils.TxFilesDescriptor
import java.net.InetAddress

class P2pConnectionManager(
    notifier: NotificationInterface,
    saveFileDir: String,
) {
    companion object {
        private const val CLIENT_CONNECTION_TIMEOUT_MS = 30000
    }

    private var transmissionIsActive = false
    private var pairConnectionIsActive = false
    private var socketJob: Job? = null
    private var rxJob: Job? = null
    private var txJob: Job? = null
    private var pairJob: Job? = null

    private var dataTransceiver: DataTransceiver =
        DataTransceiver(notifier, saveFileDir, getClipboardHandler())
    private var clientServer: TcpP2pClientServer = TcpP2pClientServer()

    fun cancelDataTransmission() {
        println("start cancelDataTransmission(), txJob.isActive? = ${txJob?.isActive}")
        dataTransceiver.cancelDataTransmission()
    }

    fun destroyPairConnection() {
        println("start destroyPairConnection()")
        dataTransceiver.cancelPairConnection()
    }

    suspend fun createServer(port: Int) {
        createSocket(true, port, InetAddress.getLocalHost())
    }

    suspend fun createClient(port: Int, address: InetAddress) {
        createSocket(false, port, address)
    }

    private suspend fun createSocket(serverFlag: Boolean,
                             port: Int,
                             address: InetAddress
    ) {
        if (clientServer.isSocketNotCreated()) {
            if (isJobActive(socketJob)) {
                println("socketJob is active, skip createSocket function")
                return
            }

            socketJob = CoroutineScope(Dispatchers.IO).launch {
                if (serverFlag) {
                    println("createSocket(), run createServer, port = $port")
                    clientServer.createServer(port)
                } else {
                    println("createClient(), run createServer, address = $address, port = $port")
                    clientServer.createClient(address, port, CLIENT_CONNECTION_TIMEOUT_MS)
                }
            }
            socketJob!!.join()
            println("socketJob.join()")
            println("nsdClientServer.isClientConnected() = ${clientServer.isClientConnected()}")

            if (clientServer.isClientConnected()) {
                println("wifiClientServer!!.isSocketCreated() = true")

                dataTransceiver.configureStreams(
                    clientServer.getInputStream(),
                    clientServer.getOutputStream()
                )

                rxJob = CoroutineScope(Dispatchers.IO).launch {
                    dataTransceiver.startReceptionFlow()
                }
            }
        } else {
            println("socket is not created")
        }
    }

    fun destroySocket() {
        println("start destroySocket()")
        dataTransceiver.reset()
        stopActiveJobs()
        clientServer.shutdown()
    }

    fun sendData(txFiles: TxFilesDescriptor) {
        if (isJobActive(txJob) || transmissionIsActive) {
            println("sendData(), transmission is already active")
            return
        }

        txJob = CoroutineScope(Dispatchers.IO).launch {
            println("sendData(), start txJob, txFiles.isNotEmpty() = ${txFiles.isNotEmpty()}")
            transmissionIsActive = true
            dataTransceiver.transmissionFlow(txFiles)
        }

        println("txJob.isActive = ${txJob?.isActive}")
    }

    fun createPair() {
        if (pairConnectionIsActive) {
            println("createPair(), pair is already created")
            return
        }

        pairJob = CoroutineScope(Dispatchers.IO).launch {
            pairConnectionIsActive = true
            dataTransceiver.startPairCreation()
        }

        println("txJob.isActive = ${txJob?.isActive}")
    }

    fun setTransmitterName(name: String) {
        dataTransceiver.setTransmitterName(name)
    }

    fun isActiveTransmission() : Boolean {
        println("start isActiveTransmission()")
        println("    txJob.isActive? = ${txJob?.isActive}")
        println("    rxJob.isActive? = ${rxJob?.isActive}")
        println("    transmissionIsActive = $transmissionIsActive")
        return isJobActive(txJob) || transmissionIsActive
    }

    fun isPairConnection() : Boolean {
        return pairConnectionIsActive
    }

    private fun isJobActive(job: Job?) : Boolean {
        return (job != null) && job.isActive
    }

    private fun stopActiveJobs() {
        println("stopActiveJobs(), start")
        if (isJobActive(socketJob)) {
            socketJob?.cancel()
            println("stopActiveJobs, stop socketJob")
        }
        if (isJobActive(txJob)) {
            txJob?.cancel()
            println("stopActiveJobs, stop txJob")
        }
        if (isJobActive(rxJob)) {
            rxJob?.cancel()
            println("stopActiveJobs, stop rxJob")
        }
        transmissionIsActive = false
        pairConnectionIsActive = false
    }
}