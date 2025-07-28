package pt.isel.keepmyplanet.ui.components

import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.concurrent.Executors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual val isQrScanningAvailable: Boolean = true

@ExperimentalGetImage
@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun QrCodeScannerView(
    modifier: Modifier,
    onQrCodeScanned: (String) -> Unit,
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    LaunchedEffect(key1 = true) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    if (cameraPermissionState.status.isGranted) {
        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                val previewView =
                    PreviewView(ctx).apply {
                        layoutParams =
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            )
                    }
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener(
                    {
                        val cameraProvider = cameraProviderFuture.get()

                        val preview =
                            Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                        val imageAnalyzer =
                            ImageAnalysis
                                .Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                                        val mediaImage = imageProxy.image
                                        if (mediaImage != null) {
                                            val image =
                                                InputImage.fromMediaImage(
                                                    mediaImage,
                                                    imageProxy.imageInfo.rotationDegrees,
                                                )
                                            val scanner = BarcodeScanning.getClient()

                                            scanner
                                                .process(image)
                                                .addOnSuccessListener { barcodes ->
                                                    if (barcodes.isNotEmpty()) {
                                                        barcodes
                                                            .firstNotNullOfOrNull { it.rawValue }
                                                            ?.let { result ->
                                                                onQrCodeScanned(result)
                                                                cameraProvider.unbindAll()
                                                            }
                                                    }
                                                }.addOnFailureListener { e ->
                                                    Log.e(
                                                        "QrCodeScannerView",
                                                        "Barcode scanning failed",
                                                        e,
                                                    )
                                                }.addOnCompleteListener {
                                                    imageProxy.close()
                                                }
                                        }
                                    }
                                }

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalyzer,
                            )
                        } catch (e: Exception) {
                            Log.e("QrCodeScannerView", "Use case binding failed", e)
                        }
                    },
                    ContextCompat.getMainExecutor(ctx),
                )

                previewView
            },
        )
    } else {
        Box(modifier) {
            Text("Camera permission is required to scan QR codes.")
        }
    }
}

private suspend fun generateQrBitmap(
    data: String,
    width: Int,
    height: Int,
    foregroundColor: Color,
    backgroundColor: Color,
): Bitmap? =
    withContext(Dispatchers.Default) {
        try {
            val hints =
                mapOf(
                    EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.Q,
                    EncodeHintType.MARGIN to 2,
                )
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, width, height, hints)
            val bmp = createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp[x, y] =
                        if (bitMatrix[x, y]) foregroundColor.toArgb() else backgroundColor.toArgb()
                }
            }
            bmp
        } catch (e: Exception) {
            Log.e("QrCodeDisplay", "Error generating QR code", e)
            null
        }
    }

@Composable
actual fun QrCodeDisplay(
    data: String,
    modifier: Modifier,
) {
    val qrColor = MaterialTheme.colorScheme.onSurface
    val qrBackgroundColor = MaterialTheme.colorScheme.surface
    val qrBitmap by produceState<Bitmap?>(initialValue = null, data, qrColor, qrBackgroundColor) {
        value =
            generateQrBitmap(
                data = data,
                width = 512,
                height = 512,
                foregroundColor = qrColor,
                backgroundColor = qrBackgroundColor,
            )
    }

    if (qrBitmap != null) {
        Image(
            bitmap = qrBitmap!!.asImageBitmap(),
            contentDescription = "User QR Code",
            modifier = modifier,
        )
    } else {
        Box(
            modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text("Generating QR code...")
        }
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
