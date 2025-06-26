package pt.isel.keepmyplanet.ui.components

import androidx.compose.runtime.Composable
import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.files.FileReader
import org.w3c.files.get

@Composable
actual fun rememberPhotoPicker(
    onImageSelected: (imageData: ByteArray, filename: String) -> Unit,
): () -> Unit =
    {
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = "image/*"

        input.onchange = { event: Event ->
            val file = (event.target as? HTMLInputElement)?.files?.get(0)
            if (file != null) {
                val reader = FileReader()
                reader.onload = { loadEvent ->
                    val result = (loadEvent.target as? FileReader)?.result as? ArrayBuffer
                    if (result != null) {
                        val jsArray = Int8Array(result)
                        val kotlinByteArray = ByteArray(jsArray.length) { i -> jsArray[i] }
                        onImageSelected(kotlinByteArray, file.name)
                    }
                }
                reader.readAsArrayBuffer(file)
            }
        }
        input.click()
    }
