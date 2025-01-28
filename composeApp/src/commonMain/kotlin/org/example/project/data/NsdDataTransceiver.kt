package org.example.project.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.project.utils.NotificationInterface
import org.example.project.utils.TxFilePackDescriptor
import java.net.InetAddress

class NsdDataTransceiver(
    notifier: NotificationInterface,
    saveFileDir: String
) : BaseDataTransceiver()
{
    companion object {
        private const val CLIENT_CONNECTION_TIMEOUT_MS = 30000
    }

    private var tcpP2pConnector: TcpP2pConnector? = null

    init {
        dataTransceiver = DataTransceiver(notifier, saveFileDir)
        tcpP2pConnector = TcpP2pConnector()
    }

    fun isConnectionEstablished(): Boolean {
        println("isConnectionEstablished(), isJobActive(rxJob) = ${isJobActive(rxJob)}")
        return isJobActive(rxJob)
    }

    fun cancelDataTransmission() {
        println("cancelDataTransmission")
        dataTransceiver?.cancelDataTransmission()
    }

    suspend fun createSocket(serverFlag: Boolean, port: Int, address: InetAddress) {
        createSocket(serverFlag, port, address, TxFilePackDescriptor())
    }

    suspend fun createSocket(serverFlag: Boolean, port: Int, address: InetAddress,
                             txFilePack: TxFilePackDescriptor
    ) {
        this.txFilePack = txFilePack.copy()

        if (!tcpP2pConnector!!.isSocketCreated()) {
            if (isJobActive(socketJob)) {
                println("socketJob is active, skip createSocket function")
                return
            }

            socketJob = CoroutineScope(Dispatchers.IO).launch {
                if (serverFlag) {
                    println("createSocket(), run createServer, port = $port")
                    tcpP2pConnector!!.createServer(port);
                } else {
                    println("createClient(), run createServer, address = $address, port = $port")
                    tcpP2pConnector!!.createClient(address, port, CLIENT_CONNECTION_TIMEOUT_MS);
                }
            }
            socketJob!!.join()
            println("socketJob!!.join()")
            println("nsdClientServer!!.isClientConnected() = ${tcpP2pConnector!!.isClientConnected()}")


            if (tcpP2pConnector!!.isClientConnected()) {
                println("wifiClientServer!!.isSocketCreated() = true")

                dataTransceiver!!.setStreams(
                    tcpP2pConnector!!.getInputStream(),
                    tcpP2pConnector!!.getOutputStream()
                )
                CoroutineScope(Dispatchers.IO).launch {
                    dataTransceiver!!.transmissionFlow(txFilePack)
                }
                rxJob = CoroutineScope(Dispatchers.IO).launch {
                    dataTransceiver!!.receptionFlow()
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
        dataTransceiver!!.shutdown()
        println("NsdDataTransceiver, destroySocket(), run tcpP2pConnector!!.shutdown()")
        tcpP2pConnector!!.shutdown()
    }
}