package com.example.ui.camera

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.CapturedProductImage
import com.example.model.PackageSide
import com.example.ui.theme.CompliantGreen
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy900
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberBg
import com.example.ui.theme.WarningAmberBorder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ImageReviewScreen(
    image: CapturedProductImage,
    onRetake: (PackageSide) -> Unit,
    onUsePhoto: (CapturedProductImage) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var currentSide by remember { mutableStateOf(image.side) }
    var showSideDropdown by remember { mutableStateOf(false) }

    // Client-side image validation metrics
    val isImageValid = image.fileSizeKb > 20 && image.uri.isNotBlank()
    val isLowResolution = image.fileSizeKb < 40 && !image.isSampleAsset
    var showLowQualityWarning by remember { mutableStateOf(isLowResolution) }

    // Pan & Zoom state for interactive image inspection
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val formattedDate = remember(image.capturedAt) {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
        sdf.format(Date(image.capturedAt))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy900)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("image_review_screen")
    ) {
        // Top App Bar
        Surface(
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Captured Image",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Review photograph quality before verification",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Side Selector Tag
                Box {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Navy700,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Cyan400),
                        modifier = Modifier.testTag("review_side_tag")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { showSideDropdown = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = currentSide.shortName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
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
                                        color = if (side == currentSide) Cyan400 else Color.White,
                                        fontWeight = if (side == currentSide) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    currentSide = side
                                    showSideDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Low Detail Quality Warning Banner if triggered
        if (showLowQualityWarning) {
            Surface(
                color = WarningAmberBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmberBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = WarningAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "This image may not provide enough detail for reliable analysis.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate900,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "Ensure text declarations are sharp, unobstructed, and well-lit.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Slate600,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // Image Viewport with interactive Pan & Zoom gesture handling
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 4f)
                            if (scale > 1f) {
                                offsetX += pan.x
                                offsetY += pan.y
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (image.isSampleAsset || image.uri.startsWith("img_")) {
                    val drawableRes = when {
                        image.uri.contains("oil") -> R.drawable.img_edible_oil_package
                        image.uri.contains("snack") || image.uri.contains("biscuit") -> R.drawable.img_snack_package
                        else -> R.drawable.img_detergent_package
                    }
                    Image(
                        painter = painterResource(id = drawableRes),
                        contentDescription = "Package ${currentSide.label}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(Uri.parse(image.uri))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Package ${currentSide.label}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Pinch-to-zoom hint overlay
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.65f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (scale > 1f) "Zoom: ${(scale * 100).toInt()}% • Tap to Reset" else "Pinch to Zoom",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Slate300,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.clickable {
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        }
                    )
                }
            }
        }

        // Image Metadata Details Strip
        Surface(
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = currentSide.label,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = currentSide.description,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        )
                    }

                    // Metadata Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${image.resolution} • ${image.fileSizeKb} KB",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Cyan400,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons: [ Retake ] & [ Use Photo ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onRetake(currentSide) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate600),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("review_retake_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Retake",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Button(
                        onClick = {
                            val updatedImage = image.copy(side = currentSide)
                            onUsePhoto(updatedImage)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Cyan500
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(52.dp)
                            .testTag("review_use_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Navy900,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showLowQualityWarning) "Continue Anyway" else "Use Photo",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                        )
                    }
                }
            }
        }
    }
}
