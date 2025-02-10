package org.example.project

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

class DesktopClipboardHandler : ClipboardHandler() {

    private var clipboardContentHash = 0
    private var monitorClipboard = false
    private val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    private var clipboardContentMonitor = {
        CoroutineScope(Dispatchers.IO).launch {
            while (monitorClipboard) {
                try {
                    val content = clipboard.getData(DataFlavor.stringFlavor) as String
                    if (clipboardContentHash != content.hashCode()) {
                        clipboardContentHash = content.hashCode()
                        newTextEvent()
                    }
                } catch (_: Exception) {
                }
                delay(100)
            }
        }
    }

    override fun enable() {
        if (!monitorClipboard) {
            monitorClipboard = true
            clipboardContentMonitor()
        }
    }

    override fun disable() {
        monitorClipboard = false
    }

    override fun getDataFromClipboard(): String {
        return clipboard.getData(DataFlavor.stringFlavor) as String
    }

    override fun putDataIntoClipboard(data: String) {
        clipboard.setContents(StringSelection(data), null)
    }

}

actual fun getClipboardHandler(
    contextFactory: ContextFactory
): ClipboardHandler = DesktopClipboardHandler()
