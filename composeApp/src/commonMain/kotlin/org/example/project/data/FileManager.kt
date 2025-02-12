package org.example.project.data

//import android.os.Environment
import org.example.project.utils.RxFileDescriptor
import java.io.File
import java.io.FileOutputStream

class FileManager (
     private val saveFileDir: String
) {
    fun getSaveFileName(fileName: String) : String{
        var fileNameUpd = ""
        val pair = getFileNameAndExtension(fileName)
        val name = pair.first
        val extension = pair.second
        var cntr = 0
        do {
            fileNameUpd = name +
                    if (cntr++ > 0) { "($cntr)" } else { "" } +
                    if (extension.isNotEmpty()) { ".$extension" } else { "" }
            val filePath = getFullPath(fileNameUpd)
        } while (File(filePath).exists())

        return fileNameUpd
    }

    fun getOutFileStream(fileName: String) : FileOutputStream{
        val filePath = getFullPath(fileName)
        val file = File(filePath)
        if (!file.createNewFile()) {
            throw Exception("name '$filePath' is already exists")
        }
        return FileOutputStream(file)
    }

    fun deleteReceivedFiles(dscrs : MutableList<RxFileDescriptor>) {
        dscrs.forEach() { dscr ->
            deleteFile(dscr.fileNameSaved)
        }
    }

    fun deleteFile(fileName: String) {
        val file = File(getFullPath(fileName))
        if (file.exists()) {
            file.delete()
        }
    }

    private fun getFullPath(fileName : String) : String {
        return saveFileDir + fileName
    }

    private fun getFileNameAndExtension(fileName : String) : Pair<String, String> {
        var name = fileName
        var extension = ""
        val id = fileName.lastIndexOf(".")
        if (id != -1) {
            name = fileName.substring(0, id)
            extension = fileName.substring(id+1)
        }
        return Pair(name, extension)
    }

}