package org.example.project.data

//import org.example.project.NotificationInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.project.utils.NotificationInterface
import org.example.project.utils.TxFilePackDescriptor
import java.net.InetAddress

class WifiDataTransceiver(
    notifier : NotificationInterface,
    saveFileDir: String
) : BaseDataTransceiver()
{
    companion object {
        private const val PORT_NUMBER = 8888
        private val CLIENT_CONNECTION_TIMEOUT_MS = 30000
    }

    private var wifiClientServer: TcpP2pConnector? = null

    init {
        dataTransceiver = DataTransceiver(notifier, saveFileDir)
        wifiClientServer = TcpP2pConnector()
    }

    fun isConnectionEstablished(): Boolean {
        return wifiClientServer?.isConnectionEstablished() ?: false
    }

    suspend fun createSocket(isGroupOwner: Boolean, address: InetAddress,
                             txFilePack: TxFilePackDescriptor
    ) {
        println("WifiDataTransceiver, start createSocket(), " +
                "!wifiClientServer!!.isSocketCreated() = ${!wifiClientServer!!.isSocketCreated()}")
        this.txFilePack = txFilePack.copy()

        if (!isJobActive(rxJob)) {

            socketJob = CoroutineScope(Dispatchers.IO).launch {
                if (isGroupOwner) {
                    wifiClientServer!!.createServer(PORT_NUMBER)
                } else {
                    wifiClientServer!!.createClient(address,
                        PORT_NUMBER, CLIENT_CONNECTION_TIMEOUT_MS
                    )
                }
            }
            socketJob!!.join()
            println("socketJob!!.join()")

            if (wifiClientServer!!.isClientConnected()) {
                println("wifiClientServer!!.isSocketCreated() = true")

                dataTransceiver!!.setStreams(
                    wifiClientServer!!.getInputStream(),
                    wifiClientServer!!.getOutputStream()
                )
                txJob = CoroutineScope(Dispatchers.IO).launch {
                    dataTransceiver!!.transmissionFlow(txFilePack)
                }
                rxJob = CoroutineScope(Dispatchers.IO).launch {
                    dataTransceiver!!.receptionFlow()
                }
            }
        }
    }

    fun destroySocket() {
        CoroutineScope(Dispatchers.IO).launch {
            stopActiveJobs()
            dataTransceiver!!.shutdown()
            wifiClientServer!!.shutdown()
        }
    }

}