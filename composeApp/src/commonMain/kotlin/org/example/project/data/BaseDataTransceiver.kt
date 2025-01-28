package org.example.project.data

import org.example.project.utils.TxFilePackDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

open class BaseDataTransceiver {

    protected var socketJob: Job? = null
    protected var txJob: Job? = null
    protected var rxJob: Job? = null

    protected var dataTransceiver: DataTransceiver? = null
    protected var txFilePack: TxFilePackDescriptor? = null

    protected fun isJobActive(job: Job?) : Boolean {
        return (job != null) && job.isActive
    }

    fun sendData(filePack: TxFilePackDescriptor) {
        CoroutineScope(Dispatchers.IO).launch {
            dataTransceiver!!.initiateDataTransmission(filePack)
        }
    }

    protected fun stopActiveJobs() {
        if (isJobActive(socketJob)) {
            println("stopActiveJobs, stop socketJob")
            socketJob!!.cancel()
        }
        if (isJobActive(txJob)) {
            println("stopActiveJobs, stop txJob")
            txJob!!.cancel()
        }
        if (isJobActive(rxJob)) {
            println("stopActiveJobs, stop rxJob")
            rxJob!!.cancel()
        }

    }
}