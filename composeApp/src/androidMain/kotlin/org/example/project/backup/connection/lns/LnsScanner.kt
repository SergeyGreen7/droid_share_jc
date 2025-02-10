package org.example.project.connection

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

class LnsScanner (
    private val manager: NsdManager
)
{
    companion object {
        private const val TAG = "LnsScanner"
    }

    var serviceType = ""

    private var isActive = false
    private var referenceServiceName = ""
    private var showResolvedServices = true
    private var resolveListenerBusy = AtomicBoolean(false)
    private var pendingNsdServices = ConcurrentLinkedQueue<NsdServiceInfo>()

    private var services = HashMap<String, NsdServiceInfo>()
    var callbackOnRefServiceFind: ((serviceInfo: NsdServiceInfo)-> Unit)? = null

    private val discoveryListener = object: NsdManager.DiscoveryListener {
        override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Log.d(TAG, "discoverServices(), onStartDiscoveryFailed " +
            "serviceType = $serviceType, errorCode = $errorCode")
        }

        override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Log.d(TAG, "discoverServices(), onStopDiscoveryFailed")
        }

        override fun onDiscoveryStarted(serviceType: String?) {
            Log.d(TAG, "discoverServices(), onDiscoveryStarted")
        }

        override fun onDiscoveryStopped(serviceType: String?) {
            Log.d(TAG, "discoverServices(), onDiscoveryStopped")
        }

        override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
            Log.d(TAG, "discoverServices(), onServiceFound")
            if (serviceInfo != null) {
                Log.d(TAG, "serviceInfo: $serviceInfo")
            }
            Log.d(TAG, "discoverServices(), serviceInfo!!.serviceName = ${serviceInfo!!.serviceName}")

            if (resolveListenerBusy.compareAndSet(false, true)) {
                // manager.registerServiceInfoCallback(serviceInfo!!, executor, resolveListener2)
                manager.resolveService(serviceInfo, resolveListener)
            } else {
                pendingNsdServices.add(serviceInfo)
            }

        }

        override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
            Log.d(TAG, "discoverServices(), onServiceLost")
        }
    }

    private val resolveListener = object : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Called when the resolve fails. Use the error code to debug.
            Log.d(TAG, "Resolve failed: $errorCode")
        }
        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.d(TAG, "Resolve Succeeded. $serviceInfo")
            services[serviceInfo.serviceType] = serviceInfo

            if (referenceServiceName.isNotEmpty()) {
                if (referenceServiceName == serviceInfo.serviceName) {
                    callbackOnRefServiceFind?.invoke(serviceInfo)
                }
            }

            resolveNextInQueue()

            if (referenceServiceName.isEmpty()) {
                showDiscoveredDevices()
            }
        }
    }

    // Resolve next NSD service pending resolution
    private fun resolveNextInQueue() {
        // Get the next NSD service waiting to be resolved from the queue
        val nextNsdService = pendingNsdServices.poll()
        if (nextNsdService != null) {
            // There was one. Send to be resolved.
            manager.resolveService(nextNsdService, resolveListener)
        }
        else {
            // There was no pending service. Release the flag
            resolveListenerBusy.set(false)
        }
    }

//    private val resolveListener2 = object : NsdManager.ServiceInfoCallback {
//        override fun onServiceInfoCallbackRegistrationFailed(errorCode: Int) {
//            Log.d(TAG, "NsdController, ServiceInfoCallback, onServiceInfoCallbackRegistrationFailed(): $errorCode")
//        }
//
//        override fun onServiceUpdated(serviceInfo: NsdServiceInfo) {
//            Log.d(TAG, "NsdController, ServiceInfoCallback, onServiceUpdated()")
//        }
//
//        override fun onServiceLost() {
//            Log.d(TAG, "NsdController, ServiceInfoCallback, onServiceLost()")
//        }
//
//        override fun onServiceInfoCallbackUnregistered() {
//            Log.d(TAG, "NsdController, ServiceInfoCallback, onServiceInfoCallbackUnregistered()")
//        }
//    }

    fun startScan() {
        if (isActive) {
            return
        }

        services.clear()
        isActive = true
        showDiscoveredDevices()

        Log.d(TAG, "LnsScanner, startScan()")
        if (serviceType.isEmpty()) {
            manager.discoverServices(
                LnsService.SERVICE_TYPE,
                LnsService.NSD_PROTOCOL,
                discoveryListener
            )
        } else {
            manager.discoverServices(
                serviceType,
                LnsService.NSD_PROTOCOL,
                discoveryListener
            )
        }
    }

    fun stopScan() {
        if (!isActive) {
            return
        }
        isActive = false
        Log.d(TAG, "LnsScanner, stopScan()")
        manager.stopServiceDiscovery(discoveryListener)
    }

    private fun showDiscoveredDevices() {
//        if (showResolvedServices) {
//            notifier.onDeviceListUpdate(services.map { DeviceInfo(it.value) })
//        }
    }

    fun scanForService(name: String) {
        referenceServiceName = name
        showResolvedServices = false
        Log.d(TAG, "LnsScanner, findService()")
        startScan()
    }
}



