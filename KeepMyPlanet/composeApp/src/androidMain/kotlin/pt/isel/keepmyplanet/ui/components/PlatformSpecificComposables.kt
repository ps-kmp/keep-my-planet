package pt.isel.keepmyplanet.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import com.gigamole.compose.qrscanner.QrScanner
import io.github.g0dkar.qrcode.QRCode

@Composable
actual fun ManageAttendanceButton(onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text("Manage Attendance (QR)")
    }
}

@Composable
actual fun QrCodeScannerView(
    modifier: Modifier,
    onQrCodeScanned: (String) -> Unit,
) {
    QrScanner(
        modifier = modifier,
        onResult = {
            it.value?.let(onQrCodeScanned)
            true
        },
    )
}

@Composable
actual fun QrCodeDisplay(
    data: String,
    modifier: Modifier,
) {
    // Gera o QR Code como um Bitmap
    val qrCodeBitmap = QRCode(data).render().nativeImage()

    if (qrCodeBitmap != null) {
        Image(
            bitmap = qrCodeBitmap.asImageBitmap(),
            contentDescription = "User QR Code",
            modifier = modifier,
        )
    } else {
        Box(modifier)
    }
}

@Composable
actual fun QrCodeIconButton(
    onClick: () -> Unit,
    contentDescription: String,
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = contentDescription,
        )
    }
}
