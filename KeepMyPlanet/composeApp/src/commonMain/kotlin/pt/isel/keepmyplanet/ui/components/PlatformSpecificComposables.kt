package pt.isel.keepmyplanet.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

expect val isQrScanningAvailable: Boolean

@Composable
expect fun ManageAttendanceButton(onClick: () -> Unit)

@Composable
expect fun QrCodeScannerView(
    modifier: Modifier,
    onQrCodeScanned: (String) -> Unit,
)

@Composable
expect fun QrCodeDisplay(
    data: String,
    modifier: Modifier,
)

@Composable
expect fun QrCodeIconButton(
    onClick: () -> Unit,
    contentDescription: String,
)
