package org.example.project.connection

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
//import org.example.project.NotificationInterface

class LnsService (
    private val manager: NsdManager,
    )
{
    companion object {
        const val SERVICE_TYPE = "_aq_fs_service._tcp"
        const val NSD_PROTOCOL = NsdManager.PROTOCOL_DNS_SD

        private const val TAG = "LnsService"
        private const val SERVICE_PORT = 8889
    }

    // private var serviceList = mutableListOf<NsdServiceInfo>()
    private var serviceRegistered = false

    var serviceInfo: NsdServiceInfo = NsdServiceInfo()
    var serviceName = "lns-default-service-name"
    var serviceType = SERVICE_TYPE

    init {
        serviceInfo.serviceName = "lns_AQUARIUS-" + (Math.random() * 1000).toInt().toString()

        serviceInfo.serviceType = serviceType
        serviceInfo.port        = SERVICE_PORT
    }

    private val registrationListener = object: NsdManager.RegistrationListener {
        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
            Log.d(TAG, "registerService(), onRegistrationFailed, errorCode = $errorCode")
        }

        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
            Log.d(TAG, "registerService(), onUnregistrationFailed, errorCode = $errorCode")
        }

        override fun onServiceRegistered(serviceInfo: NsdServiceInfo?) {
            Log.d(TAG, "registerService(), onServiceRegistered")
        }

        override fun onServiceUnregistered(serviceInfo: NsdServiceInfo?) {
            Log.d(TAG, "registerService(), onServiceUnregistered")
        }
    }

    fun registerService() {
        if (serviceRegistered) {
            return
        }

        serviceInfo.serviceName = serviceName
        serviceInfo.serviceType = serviceType
        serviceRegistered = true

        println("run manager.registerService, serviceName = $serviceName, serviceType = $SERVICE_TYPE")
        manager.registerService(serviceInfo, NSD_PROTOCOL, registrationListener)
        println("manager.registerService done")
    }

    fun unregisterService() {
        Log.d(TAG, "start unregisterService()")
        if (serviceRegistered) {
            Log.d(TAG, "    do unregisterService()")
            try {
                manager.unregisterService(registrationListener)
                serviceRegistered = false
            } catch (e: Exception) {
                println("Exception: $e")
            }
        }
    }
}



