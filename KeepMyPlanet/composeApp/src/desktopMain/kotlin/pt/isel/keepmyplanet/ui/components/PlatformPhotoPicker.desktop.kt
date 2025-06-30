package pt.isel.keepmyplanet.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun rememberPhotoPicker(
    onImageSelected: (imageData: ByteArray, filename: String) -> Unit,
): () -> Unit {
    val scope = rememberCoroutineScope()
    return {
        val fileDialog = FileDialog(null as Frame?, "Select a Photo", FileDialog.LOAD)
        fileDialog.isVisible = true

        val file = fileDialog.file
        val dir = fileDialog.directory

        if (file != null && dir != null) {
            scope.launch(Dispatchers.IO) {
                val path = File(dir, file)
                val bytes = path.readBytes()
                withContext(Dispatchers.Main) {
                    onImageSelected(bytes, path.name)
                }
            }
        }
    }
}
