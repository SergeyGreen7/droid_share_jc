package org.example.project

import io.github.vinceglb.filekit.core.PlatformFiles
import org.example.project.utils.TxFileDescriptor
import org.example.project.fragments.FileShareBlockCommon
import org.example.project.ui.*
import java.io.FileInputStream
import java.nio.file.Files
import kotlin.io.path.Path

class FileShareBlockDesktop (
    vm: FileShareViewModel,
    saveFileDir: String,
) : FileShareBlockCommon(
    vm, saveFileDir
) {

    override fun getFileDescriptorFromPickerImpl(files: PlatformFiles?) {

        txFiles.clear()
        if (!files.isNullOrEmpty()) {

            files.forEach { file ->

                val f = file.file
                val fileName = f.name
                val fileSize = Files.size(Path(f.path)).toInt()
                val inputStream = FileInputStream(f)

                txFiles.add(
                    TxFileDescriptor(fileName, fileSize, inputStream)
                )
            }
            vm.onDataSelection()
        }
    }
}