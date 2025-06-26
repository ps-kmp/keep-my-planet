package pt.isel.keepmyplanet.ui.components

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberPhotoPicker(
    onImageSelected: (imageData: ByteArray, filename: String) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri ->
            uri?.let {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                var filename = "profile_image.jpg"
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            filename = cursor.getString(nameIndex)
                        }
                    }
                }

                bytes?.let {
                    onImageSelected(it, filename)
                }
            }
        }

    return { launcher.launch("image/*") }
}
