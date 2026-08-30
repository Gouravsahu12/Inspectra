package com.example.ui.camera

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.model.CapturedProductImage
import com.example.model.PackageSide
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.Navy900
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.ViolationRed
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.Executors

enum class FlashState {
    AUTO,
    ON,
    OFF
}

@Composable
fun RealCameraScreen(
    initialSide: PackageSide = PackageSide.FRONT,
    onImageCaptured: (CapturedProductImage) -> Unit,
    onOpenGallery: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionDeniedCount by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            permissionDeniedCount++
        }
    }

    if (!hasCameraPermission) {
        CameraPermissionDeniedCard(
            deniedCount = permissionDeniedCount,
            onRequestPermission = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onOpenSettings = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            },
            onOpenGallery = onOpenGallery,
            onClose = onClose
        )
    } else {
        CameraPreviewContent(
            initialSide = initialSide,
            onImageCaptured = onImageCaptured,
            onOpenGallery = onOpenGallery,
            onClose = onClose
        )
    }
}

@Composable
private fun CameraPermissionDeniedCard(
    deniedCount: Int,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenGallery: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy900)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
            .testTag("camera_permission_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF1E293B),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Permission Required",
                        tint = if (deniedCount > 1) ViolationRed else GoldAccent,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Camera Permission Required",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (deniedCount > 1) {
                    "Camera permission is required for product inspection. Access appears to be disabled in system settings. Please enable it in Settings or upload package photos from your device gallery."
                } else if (deniedCount == 1) {
                    "Camera permission is required for product inspection."
                } else {
                    "Camera access is required to capture product packaging."
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Slate400,
                    lineHeight = 22.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (deniedCount > 1) {
                Button(
                    onClick = onOpenSettings,
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan500),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("open_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Navy900,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Open Settings",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                    )
                }
            } else if (deniedCount == 1) {
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan500),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("try_again_permission_button")
                ) {
                    Text(
                        text = "Try Again",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                    )
                }
            } else {
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan500),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("allow_camera_access_button")
                ) {
                    Text(
                        text = "Allow Camera Access",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onOpenGallery,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("camera_fallback_gallery_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Upload From Gallery",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Cancel & Return",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Slate400,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .clickable { onClose() }
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun CameraPreviewContent(
    initialSide: PackageSide,
    onImageCaptured: (CapturedProductImage) -> Unit,
    onOpenGallery: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var selectedSide by remember { mutableStateOf(initialSide) }
    var flashState by remember { mutableStateOf(FlashState.AUTO) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var isCapturing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSideDropdown by remember { mutableStateOf(false) }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(
                when (flashState) {
                    FlashState.AUTO -> ImageCapture.FLASH_MODE_AUTO
                    FlashState.ON -> ImageCapture.FLASH_MODE_ON
                    FlashState.OFF -> ImageCapture.FLASH_MODE_OFF
                }
            )
            .build()
    }

    LaunchedEffect(flashState) {
        imageCapture.flashMode = when (flashState) {
            FlashState.AUTO -> ImageCapture.FLASH_MODE_AUTO
            FlashState.ON -> ImageCapture.FLASH_MODE_ON
            FlashState.OFF -> ImageCapture.FLASH_MODE_OFF
        }
    }

    DisposableEffect(context) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        onDispose {
            try {
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
            } catch (e: Exception) {
                // ignore
            }
            cameraExecutor.shutdown()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("real_camera_screen")
    ) {
        // CameraX Live Preview Surface
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (exc: Exception) {
                        errorMessage = "Camera initialization failed: ${exc.localizedMessage ?: "Camera not available"}"
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            update = { previewView ->
                // Update camera binding if lens facing changed
                val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        // ignore or handle
                    }
                }, ContextCompat.getMainExecutor(previewView.context))
            }
        )

        // Subtle Rectangular Viewfinder Scanning Frame & Grid Overlay
        CameraViewfinderOverlay(sideLabel = selectedSide.label)

        // Top HUD Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .testTag("camera_close_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Close Camera",
                    tint = Color.White
                )
            }

            // Side Selector Pill (e.g. "Front Panel")
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(1.dp, Cyan400.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .clickable { showSideDropdown = true }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("camera_side_selector_pill")
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Cyan400)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedSide.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    )
                }

                DropdownMenu(
                    expanded = showSideDropdown,
                    onDismissRequest = { showSideDropdown = false },
                    modifier = Modifier.background(Navy900)
                ) {
                    PackageSide.values().forEach { side ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = side.label,
                                    color = if (side == selectedSide) Cyan400 else Color.White,
                                    fontWeight = if (side == selectedSide) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                selectedSide = side
                                showSideDropdown = false
                            }
                        )
                    }
                }
            }

            // Flip Camera button
            IconButton(
                onClick = {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        CameraSelector.LENS_FACING_FRONT
                    } else {
                        CameraSelector.LENS_FACING_BACK
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .testTag("camera_flip_lens_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Switch Camera",
                    tint = Color.White
                )
            }
        }

        // Bottom Controls HUD Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black.copy(alpha = 0.95f)
                        )
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live scanning sample badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Cyan500.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, Cyan400.copy(alpha = 0.6f)),
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .clickable {
                        // Quick scan sample package
                        val sampleImg = CapturedProductImage(
                            id = "img_scan_${System.currentTimeMillis()}",
                            uri = "img_detergent_package",
                            side = selectedSide,
                            isSampleAsset = true,
                            resolution = "1920x1080",
                            fileSizeKb = 284L
                        )
                        onImageCaptured(sampleImg)
                    }
                    .testTag("camera_sample_scan_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Cyan400)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "⚡ Tap to Scan Sample Packaging",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Cyan400,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    )
                }
            }

            // Side description tip
            Text(
                text = selectedSide.description,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Slate200,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(bottom = 14.dp)
            )

            // Shutter & Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flash Toggle
                IconButton(
                    onClick = {
                        flashState = when (flashState) {
                            FlashState.AUTO -> FlashState.ON
                            FlashState.ON -> FlashState.OFF
                            FlashState.OFF -> FlashState.AUTO
                        }
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .testTag("camera_flash_toggle_button")
                ) {
                    Icon(
                        imageVector = when (flashState) {
                            FlashState.AUTO -> Icons.Default.FlashAuto
                            FlashState.ON -> Icons.Default.FlashOn
                            FlashState.OFF -> Icons.Default.FlashOff
                        },
                        contentDescription = "Flash ${flashState.name}",
                        tint = if (flashState == FlashState.ON) GoldAccent else Color.White
                    )
                }

                // Shutter Capture Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                        .padding(6.dp)
                        .clickable(enabled = !isCapturing) {
                            if (!isCapturing) {
                                isCapturing = true
                                capturePhoto(
                                    context = context,
                                    imageCapture = imageCapture,
                                    cameraExecutor = cameraExecutor,
                                    side = selectedSide,
                                    onSuccess = { captured ->
                                        try {
                                            val provider = ProcessCameraProvider.getInstance(context)
                                            if (provider.isDone) {
                                                provider.get().unbindAll()
                                            }
                                        } catch (e: Exception) {
                                            // ignore
                                        }
                                        isCapturing = false
                                        onImageCaptured(captured)
                                    },
                                    onError = { err ->
                                        isCapturing = false
                                        errorMessage = err
                                    }
                                )
                            }
                        }
                        .testTag("camera_shutter_button")
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            color = Cyan400,
                            modifier = Modifier.size(42.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }

                // Gallery Button
                IconButton(
                    onClick = onOpenGallery,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .testTag("camera_gallery_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Open Gallery",
                        tint = Color.White
                    )
                }
            }
        }

        // Error snackbar if capture fails
        errorMessage?.let { err ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ViolationRed,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 70.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = err,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraViewfinderOverlay(sideLabel: String) {
    val scanAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scanAnim.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 90.dp)
    ) {
        val frameWidth = maxWidth
        val frameHeight = maxHeight * 0.72f

        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = frameWidth, height = frameHeight)
        ) {
            val cornerLength = 36.dp.toPx()
            val strokeWidth = 3.dp.toPx()
            val cornerColor = androidx.compose.ui.graphics.Color(0xFF38BDF8)

            // Top-Left Corner
            drawLine(
                color = cornerColor,
                start = Offset(0f, 0f),
                end = Offset(cornerLength, 0f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = cornerColor,
                start = Offset(0f, 0f),
                end = Offset(0f, cornerLength),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Top-Right Corner
            drawLine(
                color = cornerColor,
                start = Offset(size.width, 0f),
                end = Offset(size.width - cornerLength, 0f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = cornerColor,
                start = Offset(size.width, 0f),
                end = Offset(size.width, cornerLength),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Bottom-Left Corner
            drawLine(
                color = cornerColor,
                start = Offset(0f, size.height),
                end = Offset(cornerLength, size.height),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = cornerColor,
                start = Offset(0f, size.height),
                end = Offset(0f, size.height - cornerLength),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Bottom-Right Corner
            drawLine(
                color = cornerColor,
                start = Offset(size.width, size.height),
                end = Offset(size.width - cornerLength, size.height),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = cornerColor,
                start = Offset(size.width, size.height),
                end = Offset(size.width, size.height - cornerLength),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Subtle bounding border
            drawRect(
                color = cornerColor.copy(alpha = 0.25f),
                size = size,
                style = Stroke(width = 1.dp.toPx())
            )

            // Horizontal scanning line
            val scanY = size.height * scanAnim.value
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        cornerColor.copy(alpha = 0.85f),
                        Color.White,
                        cornerColor.copy(alpha = 0.85f),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, scanY),
                end = Offset(size.width, scanY),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Viewfinder Instruction Pill
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.Black.copy(alpha = 0.65f),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 12.dp)
        ) {
            Text(
                text = "Align the package inside the frame",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
    }
}

private fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture,
    cameraExecutor: java.util.concurrent.Executor,
    side: PackageSide,
    onSuccess: (CapturedProductImage) -> Unit,
    onError: (String) -> Unit
) {
    val photoFile = File(
        context.cacheDir,
        "inspectra_${side.name.lowercase()}_${System.currentTimeMillis()}.jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        cameraExecutor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                // Calculate dimensions & file size
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(photoFile.absolutePath, options)
                val width = options.outWidth.coerceAtLeast(1080)
                val height = options.outHeight.coerceAtLeast(1920)
                val sizeKb = (photoFile.length() / 1024).coerceAtLeast(120)

                val capturedImage = CapturedProductImage(
                    id = UUID.randomUUID().toString(),
                    uri = Uri.fromFile(photoFile).toString(),
                    side = side,
                    capturedAt = System.currentTimeMillis(),
                    isFromGallery = false,
                    resolution = "${width}x${height}",
                    fileSizeKb = sizeKb,
                    isSampleAsset = false
                )

                ContextCompat.getMainExecutor(context).execute {
                    onSuccess(capturedImage)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                ContextCompat.getMainExecutor(context).execute {
                    onError(exception.localizedMessage ?: "Failed to capture image")
                }
            }
        }
    )
}
