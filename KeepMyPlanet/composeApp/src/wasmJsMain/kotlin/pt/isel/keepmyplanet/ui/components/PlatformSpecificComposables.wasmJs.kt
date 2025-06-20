package pt.isel.keepmyplanet.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun ManageAttendanceButton(onClick: () -> Unit) {
}

@Composable
actual fun QrCodeScannerView(
    modifier: Modifier,
    onQrCodeScanned: (String) -> Unit
) {
}

@Composable
actual fun QrCodeDisplay(data: String, modifier: Modifier) {
}

@Composable
actual fun QrCodeIconButton(onClick: () -> Unit, contentDescription: String) {
}
