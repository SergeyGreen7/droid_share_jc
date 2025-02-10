package org.example.project

abstract class ClipboardHandler {

    private var setDataHash = 0
    protected var newTextEvent = {}

    abstract fun enable()

    abstract fun disable()

    fun getData() : String {
        println("ClipboardHandler, getData() start")
        return getDataFromClipboard()
    }

    fun setData(data: String) {
        println("ClipboardHandler, setData() start")
        if (setDataHash != data.hashCode()) {
            setDataHash = data.hashCode()
            putDataIntoClipboard(data)
        }
    }

    fun setNewDataHandler(handler: () -> Unit) {
        newTextEvent = handler
    }

    protected abstract fun getDataFromClipboard(): String
    protected abstract fun putDataIntoClipboard(data: String)
}

expect fun getClipboardHandler(): ClipboardHandler