package org.example.project.connection

import com.appstractive.dnssd.DiscoveredService
import com.appstractive.dnssd.DiscoveryEvent
import com.appstractive.dnssd.NetService
import com.appstractive.dnssd.createNetService
import com.appstractive.dnssd.discoverServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class LocalNetworkServiceManager(
    preffix: String
) {

    var callbackOnRefServiceFind: ((serviceInfo: DiscoveredService)-> Unit)? = null

    private val SERVICE_NAME = "NSD_AQUARIUS"
    private val SERVICE_TYPE = "_aq_fs_service._tcp"
    private val SERVICE_PORT = 8889

    private var service: NetService? = null

    private var serviceName = preffix + SERVICE_NAME
    private var serviceType = SERVICE_TYPE
    private var scanJob: Job? = null
    private var callbackIsUsed = false
    private var referenceServiceName = ""

    private var addressPattern: Pattern = Pattern.compile(
        "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\." +
        "(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\." +
        "(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\." +
        "(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9]))")

    fun setName(name: String) {
        serviceName = name
    }

    fun setType(type: String) {
        serviceType = type
    }

    fun registerService() {
        service = createNetService(
            type = SERVICE_TYPE,
            name = serviceName,
            port = SERVICE_PORT,
        )

        if (!service!!.isRegistered.value) {
            println("LocalNetworkServiceManager, start registerService()")
            CoroutineScope(Dispatchers.IO).launch {
                service!!.register()
                println("LocalNetworkServiceManager, service is registered")
            }
        } else {
            println("service is already registered!")
        }
        println("   service type: ${service!!.type}")
        println("   service name: ${service!!.name}")
        println("   service port: ${service!!.port}")
    }

    fun unregisterService() {
        CoroutineScope(Dispatchers.IO).launch {
            println("LocalNetworkServiceManager, start unregisterService()")
            service?.unregister()
        }
    }

    fun startScan() {
        callbackIsUsed = false
        println("McDnsScanner, start startScan()")

        if (scanJob != null) {
            return
        }

        println("McDnsScanner, run discoverServices(), type = $SERVICE_TYPE")
        scanJob = CoroutineScope(Dispatchers.IO).launch {
            discoverServices(SERVICE_TYPE).collect {
                println("discoverServices, it = $it")
                when (it) {
                    is DiscoveryEvent.Discovered -> {
                        onAdd(it.service)
                    }

                    is DiscoveryEvent.Removed -> {
                        onRemove(it.service)
                    }

                    is DiscoveryEvent.Resolved -> {
                        onResolve(it.service)
                    }
                }
            }
        }

        println("scanJob.isActive = ${scanJob?.isActive}")

    }

    fun scanForService(name: String) {
        println("McDnsScanner, start findService()")
        referenceServiceName = name
        startScan()
    }

    fun stopScan() {
        println("LocalNetworkServiceManager, start stopScan()")
        scanJob?.cancel()
        scanJob = null
    }

    private fun onAdd(service: DiscoveredService) {
        println("discovered service $service")
    }

    private fun onRemove(service: DiscoveredService) {
        println("discovered service $service")
    }

    private fun onResolve(service: DiscoveredService) {
        CoroutineScope(Dispatchers.IO).launch {
            println("resolved service $service")

            var matchFlag = false
            service.addresses.forEach({ address ->
                if (addressPattern.matcher(address).matches()) {
                    println("address $address is a correct IP address")
                    matchFlag = true
                } else {
                    println("address $address is not a correct IP address")
                }
            })
            matchFlag = matchFlag && !callbackIsUsed

            if (referenceServiceName.isNotEmpty() && matchFlag) {
                if (referenceServiceName == service.name) {
                    callbackIsUsed = true
                    callbackOnRefServiceFind?.invoke(service)
                }
            }
        }
    }
}

