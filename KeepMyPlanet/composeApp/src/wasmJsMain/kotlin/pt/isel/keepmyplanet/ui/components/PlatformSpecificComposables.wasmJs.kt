package pt.isel.keepmyplanet.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

actual val isQrScanningAvailable: Boolean = false

@Composable
actual fun QrCodeScannerView(
    modifier: Modifier,
    onQrCodeScanned: (String) -> Unit,
) {
    FeatureNotAvailable(modifier, "QR Code display is not available on Web.")
}

@Composable
actual fun QrCodeDisplay(
    data: String,
    modifier: Modifier,
) {
    FeatureNotAvailable(modifier, "QR Code scanning is not available on Web.")
}

@Composable
actual fun QrCodeIconButton(
    onClick: () -> Unit,
    contentDescription: String,
) {
}
