package org.example.project.connection.mcdns

import com.appstractive.dnssd.DiscoveredService
import com.appstractive.dnssd.DiscoveryEvent
import com.appstractive.dnssd.discoverServices
import com.appstractive.dnssd.key
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.example.project.connection.mcdns.McDnsService.Companion.SERVICE_TYPE
import java.util.regex.Pattern

class McDnsScanner {

    private var callbackIsUsed = false
    private var showResolvedServices = true

    private var scanJob: Job? = null
    private var scannedServices = mutableMapOf<String, DiscoveredService>()

    private var referenceServiceName = ""
    var callbackOnRefServiceFind: ((serviceInfo: DiscoveredService)-> Unit)? = null

    private var addressPattern: Pattern = Pattern.compile(
        "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\." +
        "(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\." +
        "(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\." +
        "(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9]))")

    fun startScan() {
        callbackIsUsed = false
        println("McDnsScanner, start startScan()")

        if (scanJob != null) {
            return
        }

        println("McDnsScanner, run discoverServices(), type = $SERVICE_TYPE")
        scanJob = CoroutineScope(Dispatchers.IO).launch {
            discoverServices(SERVICE_TYPE).collect { it ->
                when (it) {
                    is DiscoveryEvent.Discovered -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            println("discovered service ${it.service.key} - ${it.service}")
                            it.resolve()
                        }
                    }

                    is DiscoveryEvent.Removed -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            println("removed service ${it.service.key} - ${it.service}")
                            scannedServices.remove(it.service.key)
                        }
                    }

                    is DiscoveryEvent.Resolved -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            println("resolved service ${it.service.key} - ${it.service}")
                            scannedServices[it.service.key] = it.service

                            var matchFlag = false
                            it.service.addresses.forEach({ address ->
                                if (addressPattern.matcher(address).matches()) {
                                    println("address $address is a correct IP address")
                                    matchFlag = true
                                } else {
                                    println("address $address is not a correct IP address")
                                }
                            })
                            matchFlag = matchFlag && !callbackIsUsed

                            if (referenceServiceName.isNotEmpty() && matchFlag) {
                                if (referenceServiceName == it.service.name) {
                                    callbackIsUsed = true
                                    callbackOnRefServiceFind?.invoke(it.service)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun stopScan() {
        println("McDnsScanner, start stopScan()")

        scanJob?.cancel()
        scanJob = null
    }

    fun scanForService(name: String) {
        println("McDnsScanner, start findService()")
        referenceServiceName = name
        showResolvedServices = false
        startScan()
    }
}







