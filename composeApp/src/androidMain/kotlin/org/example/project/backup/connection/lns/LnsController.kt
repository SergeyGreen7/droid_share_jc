package org.example.project.connection

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log

class LnsController (
    manager: NsdManager
){
    companion object {
        private const val TAG = "NsdController"
    }

    private val service = LnsService(manager)
    private val scanner = LnsScanner(manager)

    fun getServiceInfo(): NsdServiceInfo {
        return service.serviceInfo
    }

    fun registerLocalNetworkService() {
        Log.d(TAG, "LncController, registerLocalNetworkService()")
        service.registerService()
    }

    fun unregisterLocalNetworkService() {
        Log.d(TAG, "LncController, unregisterLocalNetworkService()")
        service.unregisterService()
    }

    fun startDiscoverLocalNetworkServices() {
        Log.d(TAG, "LncController, startDiscoverLocalNetworkServices()")
        scanner.startScan()
    }

    fun stopDiscoverLocalNetworkServices() {
        Log.d(TAG, "LncController, stopDiscoverLocalNetworkServices()")
        scanner.stopScan()
    }
}



