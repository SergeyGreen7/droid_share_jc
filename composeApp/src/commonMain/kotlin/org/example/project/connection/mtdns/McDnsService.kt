package org.example.project.connection.mtdns

//import android.annotation.SuppressLint
//import android.net.nsd.NsdManager
//import android.net.nsd.NsdServiceInfo
//import android.text.TextUtils
//import android.util.Log
//import org.example.project.NotificationInterface
//import org.example.project.grid.DeviceInfo
import com.appstractive.dnssd.NetService
import com.appstractive.dnssd.createNetService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class McDnsService (
    namePrefix: String
) {
    companion object {
        const val SERVICE_NAME = "NSD_AQUARIUS"
        const val SERVICE_TYPE = "_example._tcp"
        // const val NSD_PROTOCOL = NsdManager.PROTOCOL_DNS_SD

        private const val TAG = "LnsService"
        private const val SERVICE_PORT = 8889
    }

    private var isRegistered = false
    var serviceName = namePrefix + SERVICE_NAME
    lateinit var service: NetService

    fun registerService() {
        service = createNetService(
            type = SERVICE_TYPE,
            name = serviceName,
            port = SERVICE_PORT,
            txt =
            mapOf(
                "key1" to "value1",
                "key2" to "value2",
            ),
        )

        CoroutineScope(Dispatchers.IO).launch {
            println("McDnsService, start registerService()")
            if (!isRegistered) {
                service.register()
                isRegistered = true
                println("McDnsService, service is registered")
                println("   service type: ${service.type}")
                println("   service name: ${service.name}")
            }
        }
    }

    fun getPort(): Int {
        return SERVICE_PORT
    }

    fun unregisterService() {
        CoroutineScope(Dispatchers.IO).launch {
            println("McDnsService, start unregisterService()")
            if (isRegistered) {
                service.unregister()
            }
        }
    }
}



