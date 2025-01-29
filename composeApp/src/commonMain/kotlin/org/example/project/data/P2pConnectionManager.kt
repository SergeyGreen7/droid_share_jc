package org.example.project.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.example.project.utils.NotificationInterface
import org.example.project.utils.TxFilesDescriptor
import java.net.InetAddress

class P2pConnectionManager(
    notifier: NotificationInterface,
    saveFileDir: String
) {
    companion object {
        private const val CLIENT_CONNECTION_TIMEOUT_MS = 30000
    }

    private var socketJob: Job? = null
    private var rxJob: Job? = null
    private var txJob: Job? = null

    private var dataTransceiver: DataTransceiver = DataTransceiver(notifier, saveFileDir)
    private var txFiles: TxFilesDescriptor = TxFilesDescriptor()
    private var clientServer: TcpP2pClientServer = TcpP2pClientServer()

    fun cancelDataTransmission() {
        println("cancelDataTransmission")
        dataTransceiver.cancelDataTransmission()
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

                dataTransceiver.setStreams(
                    clientServer.getInputStream(),
                    clientServer.getOutputStream()
                )

                rxJob = CoroutineScope(Dispatchers.IO).launch {
                    dataTransceiver.receptionFlow()
                }
            }
        } else {
            println("socket is not created")
        }
    }

    fun destroySocket() {
        println("NsdDataTransceiver, destroySocket(), run stopActiveJobs()")
        stopActiveJobs()
        println("NsdDataTransceiver, destroySocket(), run dataTransceiver!!.shutdown()")
        dataTransceiver.shutdown()
        println("NsdDataTransceiver, destroySocket(), run tcpP2pConnector!!.shutdown()")
        clientServer.shutdown()
    }

    fun sendData(txFiles: TxFilesDescriptor) {
        if (isJobActive(txJob)) {
            println("sendData(), txJob is currently active")
            return
        }

        this.txFiles = txFiles.copy()
        txJob = CoroutineScope(Dispatchers.IO).launch {
            dataTransceiver.transmissionFlow(txFiles)
        }
    }

    private fun isJobActive(job: Job?) : Boolean {
        return (job != null) && job.isActive
    }

    private fun stopActiveJobs() {
        if (isJobActive(socketJob)) {
            println("stopActiveJobs, stop socketJob")
            socketJob?.cancel()
        }
        if (isJobActive(txJob)) {
            println("stopActiveJobs, stop txJob")
            txJob?.cancel()
        }
        if (isJobActive(rxJob)) {
            println("stopActiveJobs, stop rxJob")
            rxJob?.cancel()
        }

    }
}