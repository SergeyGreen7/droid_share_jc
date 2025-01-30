package org.example.project.connection.mcdns

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
        const val SERVICE_TYPE = "_aq_fs_service._tcp"

        private const val SERVICE_PORT = 8889
    }

    private var isRegistered = false
    var serviceName = namePrefix + SERVICE_NAME
    lateinit var service: NetService

    suspend fun registerService() {
        service = createNetService(
            type = SERVICE_TYPE,
            name = serviceName,
            port = SERVICE_PORT,
        )

        println("McDnsService, start registerService()")
        if (!isRegistered) {
            service.register()
            isRegistered = true
            println("McDnsService, service is registered")
            println("   service type: ${service.type}")
            println("   service name: ${service.name}")
        }
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



