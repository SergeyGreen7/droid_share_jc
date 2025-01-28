package org.example.project.connection.mtdns

import com.appstractive.dnssd.DiscoveredService
import com.appstractive.dnssd.DiscoveryEvent
import com.appstractive.dnssd.discoverServices
import com.appstractive.dnssd.key
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.example.project.connection.mtdns.McDnsService.Companion.SERVICE_TYPE
import java.util.concurrent.atomic.AtomicBoolean
import java.util.regex.Pattern

class McDnsScanner (
    // private val manager: NsdManager,
    // private val notifier: NotificationInterface
)
{
    companion object {
        private const val TAG = "McDnsScanner"
    }

    private var isActive = false
    private var callbackIsUsed = false
    private var showResolvedServices = true
    private var resolveListenerBusy = AtomicBoolean(false)
//    private var pendingNsdServices = ConcurrentLinkedQueue<NsdServiceInfo>()

    private var scanJob: Job? = null
    private var scannedServices = mutableMapOf<String, DiscoveredService>()

//    private var services = HashMap<String, NsdServiceInfo>()
    var referenceServiceName = ""
    var callbackOnRefServiceFind: ((serviceInfo: DiscoveredService)-> Unit)? = null

    lateinit private var addressPattern: Pattern

    init {
        addressPattern = Pattern.compile(
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\." +
            "(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\." +
            "(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\." +
            "(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9]))");
    }

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
                            // scannedServices[it.service.key] = it.service
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

                            // Matcher matcher = IP_ADDRESS.matcher("127.0.0.1");
                            var flag = false
                            it.service.addresses.forEach({ address ->
                                if (addressPattern.matcher(address).matches()) {
                                    println("address $address is a correct IP address")
                                    flag = true
                                } else {
                                    println("address $address is not a correct IP address")
                                }
                            })

                            if (referenceServiceName.isNotEmpty() && flag && !callbackIsUsed) {
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
//
//        services.clear()
//        isActive = true
//        showDiscoveredDevices()
//
//        Log.d(TAG, "LnsScanner, startScan()")
//        manager.discoverServices(LnsService.SERVICE_TYPE, LnsService.NSD_PROTOCOL, discoveryListener)
    }

    fun stopScan() {
        println("McDnsScanner, start stopScan()")

        scanJob?.cancel()
        scanJob = null
        // manager.stopServiceDiscovery(discoveryListener)
    }

//    private fun showDiscoveredDevices() {
//        if (showResolvedServices) {
//            notifier.onDeviceListUpdate(services.map { DeviceInfo(it.value) })
//        }
//    }

    fun scanForService(name: String) {
        println("McDnsScanner, start findService()")
        referenceServiceName = name
        showResolvedServices = false
        startScan()
    }
}







