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

class McDnsScanner (
    // private val manager: NsdManager,
    // private val notifier: NotificationInterface
)
{
    companion object {
        private const val TAG = "McDnsScanner"
    }

    private var isActive = false
    private var showResolvedServices = true
    private var resolveListenerBusy = AtomicBoolean(false)
//    private var pendingNsdServices = ConcurrentLinkedQueue<NsdServiceInfo>()

    private var scanJob: Job? = null
    private var scannedServices = mutableMapOf<String, DiscoveredService>()

//    private var services = HashMap<String, NsdServiceInfo>()
    var referenceServiceName = ""
    var callbackOnRefServiceFind: ((serviceInfo: DiscoveredService)-> Unit)? = null

    fun startScan() {
        println("McDnsScanner, start startScan()")

        if (scanJob != null) {
            return
        }

        println("McDnsScanner, run discoverServices(), type = $SERVICE_TYPE")
        scanJob = CoroutineScope(Dispatchers.IO).launch {
            discoverServices(SERVICE_TYPE).collect {
                when (it) {
                    is DiscoveryEvent.Discovered -> {
                        println("discovered service ${it.service.key} - ${it.service}")
                        // scannedServices[it.service.key] = it.service
                        it.resolve()
                    }

                    is DiscoveryEvent.Removed -> {
                        println("removed service ${it.service.key} - ${it.service}")
                        scannedServices.remove(it.service.key)
                    }

                    is DiscoveryEvent.Resolved -> {
                        println("resolved service ${it.service.key} - ${it.service}")
                        scannedServices[it.service.key] = it.service

                        if (referenceServiceName.isNotEmpty()) {
                            if (referenceServiceName == it.service.name) {
                                callbackOnRefServiceFind?.invoke(it.service)
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







