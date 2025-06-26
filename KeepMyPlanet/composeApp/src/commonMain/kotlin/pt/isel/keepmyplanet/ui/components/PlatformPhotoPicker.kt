package pt.isel.keepmyplanet.ui.components

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPhotoPicker(
    onImageSelected: (imageData: ByteArray, filename: String) -> Unit,
): () -> Unit
