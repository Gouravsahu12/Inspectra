package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.R
import com.example.model.ExtractedField
import com.example.model.FieldStatus
import com.example.model.Violation
import com.example.ui.components.InspectraGovBadge
import com.example.ui.components.SeverityChip
import com.example.ui.theme.CompliantGreen
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Cyan600
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy900
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.ViolationRed
import com.example.ui.theme.ViolationRedBg
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.InspectraViewModel

@Composable
fun EvidenceViewerScreen(
    viewModel: InspectraViewModel,
    onBack: () -> Unit
) {
    val sampleType by viewModel.selectedSampleType.collectAsState()
    val violations by viewModel.violations.collectAsState()
    val extractedFields by viewModel.extractedFields.collectAsState()
    val productImages by viewModel.productImages.collectAsState()
    var selectedLayer by remember { mutableStateOf("all") } // "all", "violations", "ocr"

    val capturedUserImageUri = productImages.firstOrNull()?.uri
    val isRealCapturedImage = !capturedUserImageUri.isNullOrBlank() &&
            (capturedUserImageUri.startsWith("content://") || capturedUserImageUri.startsWith("file://") || capturedUserImageUri.startsWith("/"))

    val sampleImgRes = when (sampleType) {
        "oil" -> R.drawable.img_edible_oil_package
        "biscuits" -> R.drawable.img_snack_package
        else -> R.drawable.img_detergent_package
    }

    BackHandler {
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .testTag("evidence_viewer_screen")
    ) {
        // Top Header
        Surface(
            color = Navy900,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Visual Evidence Viewer",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "LMPC Statutory Overlay • Proof of Violation",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Cyan500,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "100%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // Layer Filter Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "all" to "All Regions (7)",
                "violations" to "Violations (${violations.size})",
                "ocr" to "OCR Verified (${extractedFields.count { it.status == FieldStatus.VERIFIED }})"
            ).forEach { (key, label) ->
                val isSelected = selectedLayer == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) Navy700 else Color(0xFF334155))
                        .clickable { selectedLayer = key }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Slate400,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Interactive Package Canvas with Bounding Box Overlays
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (isRealCapturedImage && capturedUserImageUri != null) {
                AsyncImage(
                    model = capturedUserImageUri,
                    contentDescription = "User Captured Package Evidence",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = sampleImgRes),
                    contentDescription = "Package Evidence",
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Bounding Box Overlays Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                if (selectedLayer == "all" || selectedLayer == "ocr") {
                    // MRP Bounding Box (Top Right)
                    drawRoundRect(
                        color = CompliantGreen,
                        topLeft = Offset(w * 0.58f, h * 0.12f),
                        size = Size(w * 0.38f, h * 0.18f),
                        cornerRadius = CornerRadius(6.dp.toPx()),
                        style = Stroke(width = 2.5.dp.toPx())
                    )

                    // Net Qty Box (Bottom Right)
                    drawRoundRect(
                        color = CompliantGreen,
                        topLeft = Offset(w * 0.52f, h * 0.68f),
                        size = Size(w * 0.42f, h * 0.16f),
                        cornerRadius = CornerRadius(6.dp.toPx()),
                        style = Stroke(width = 2.5.dp.toPx())
                    )

                    // Manufacturer Block (Bottom Left)
                    drawRoundRect(
                        color = CompliantGreen,
                        topLeft = Offset(w * 0.05f, h * 0.65f),
                        size = Size(w * 0.42f, h * 0.22f),
                        cornerRadius = CornerRadius(6.dp.toPx()),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }

                if (selectedLayer == "all" || selectedLayer == "violations") {
                    if (violations.isNotEmpty()) {
                        // Consumer Care Missing Region Box (Red dashed/solid highlight)
                        drawRoundRect(
                            color = ViolationRed,
                            topLeft = Offset(w * 0.08f, h * 0.35f),
                            size = Size(w * 0.84f, h * 0.24f),
                            cornerRadius = CornerRadius(6.dp.toPx()),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }
            }

            // Overlay Bounding Box Tags
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (selectedLayer != "violations") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CompliantGreen.copy(alpha = 0.85f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Rule 6(1)(e): MRP ₹120 [96%]",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                if (violations.isNotEmpty() && (selectedLayer == "all" || selectedLayer == "violations")) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clip(RoundedCornerShape(6.dp))
                            .background(ViolationRed.copy(alpha = 0.9f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "VIOLATION: Missing Rule 6(1)(f) Consumer Care",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (selectedLayer != "violations") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CompliantGreen.copy(alpha = 0.85f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Rule 6(1)(a): Mfg Info [92%]",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CompliantGreen.copy(alpha = 0.85f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Rule 6(1)(c): Net Qty 1kg [98%]",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }

        // Bottom Evidence Summary Card
        Surface(
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Statutory Evidence Chain",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Digitally timestamped & GPS geo-tagged with Officer Token",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Navy700),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Return", color = Color.White)
                    }
                }
            }
        }
    }
}
