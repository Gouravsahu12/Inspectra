package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.CapturedProductImage
import com.example.model.ExtractedField
import com.example.model.FieldStatus
import com.example.model.OCRResult
import com.example.ui.components.ConfidenceIndicator
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Cyan600
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.ViolationRed
import com.example.ui.theme.ViolationRedBg
import com.example.ui.theme.ViolationRedBorder
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberBg
import com.example.ui.theme.WarningAmberBorder

/**
 * Visual Evidence Inspector:
 * Shows the captured package panel with bounding box overlay around the
 * detected statutory declaration zone.
 */
@Composable
fun OcrEvidenceViewerDialog(
    field: ExtractedField,
    capturedImages: List<CapturedProductImage>,
    onDismiss: () -> Unit,
    onEditField: () -> Unit
) {
    val context = LocalContext.current
    val targetImage = capturedImages.firstOrNull { it.side == field.detectedSide }
        ?: capturedImages.firstOrNull()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Navy900.copy(alpha = 0.95f))
                .testTag("ocr_evidence_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CropFree,
                                contentDescription = null,
                                tint = Cyan400,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "OCR Evidence Inspector",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Text(
                            text = "${field.ruleReference} • ${field.fieldName}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Image Container with Dynamic Bounding Box Overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black)
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (targetImage != null) {
                        if (targetImage.isSampleAsset || targetImage.uri.startsWith("img_")) {
                            val drawableRes = when (targetImage.uri) {
                                "img_detergent_package" -> R.drawable.img_detergent_package
                                "img_edible_oil_package" -> R.drawable.img_edible_oil_package
                                "img_snack_package" -> R.drawable.img_snack_package
                                else -> R.drawable.img_detergent_package
                            }
                            Image(
                                painter = painterResource(id = drawableRes),
                                contentDescription = "Package photo evidence",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(targetImage.uri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Package photo evidence",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Glowing OCR Bounding Box Region
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(90.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Cyan500.copy(alpha = 0.18f))
                                .border(2.dp, Cyan400, RoundedCornerShape(8.dp))
                                .padding(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.align(Alignment.TopStart),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Cyan500)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = field.boundingBoxLabel.ifBlank { "DETECTED ZONE" },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Navy900,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No evidence image available",
                            color = Slate400,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Detail Evidence Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "RAW EXTRACTED TEXT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Cyan400,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )

                            ConfidenceIndicator(confidence = field.confidence)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (field.extractedValue.isNotBlank()) field.extractedValue else "No declaration text detected in this zone.",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = if (field.extractedValue.isNotBlank()) Color.White else ViolationRed,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Detected on: ${field.detectedSide} Panel",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Slate400,
                                    fontSize = 11.sp
                                )
                            )

                            Button(
                                onClick = {
                                    onDismiss()
                                    onEditField()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Cyan600),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Edit Field Value",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Multi-Angle Conflict Resolution Dialog:
 * When front and back panels show contradictory statutory declarations,
 * the inspector selects the authoritative declaration or inputs verified text.
 */
@Composable
fun ConflictResolutionDialog(
    field: ExtractedField,
    onResolve: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var customValue by remember { mutableStateOf(field.extractedValue) }
    var selectedOption by remember { mutableStateOf(field.extractedValue) }
    var isCustomSelected by remember { mutableStateOf(false) }

    val candidates = field.conflictingValues.ifEmpty {
        listOf(field.aiExtractedValue, field.extractedValue).filter { it.isNotBlank() }.distinct()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Conflicting Declarations",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Text(
                            text = "${field.ruleReference} • ${field.fieldName}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Multiple packaging angles detected conflicting statutory values. Select the authoritative declaration verified on the physical commodity:",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate700, fontSize = 12.sp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Detected Candidate Choices
                candidates.forEach { candidate ->
                    val isChecked = !isCustomSelected && selectedOption == candidate
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isChecked) Color(0xFFEFF6FF) else Slate100,
                        border = BorderStroke(1.dp, if (isChecked) Navy700 else Slate200),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedOption = candidate
                                isCustomSelected = false
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = candidate,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isChecked) Navy900 else Slate800
                                )
                            )
                            if (isChecked) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Navy700,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Or Custom Override
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isCustomSelected) Color(0xFFEFF6FF) else Slate100,
                    border = BorderStroke(1.dp, if (isCustomSelected) Navy700 else Slate200),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCustomSelected = true }
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Manual Inspector Override",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isCustomSelected) Navy900 else Slate700
                            )
                        )
                        if (isCustomSelected) {
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = customValue,
                                onValueChange = { customValue = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Navy700,
                                    unfocusedBorderColor = Slate300
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Slate600)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val resolved = if (isCustomSelected) customValue else selectedOption
                            onResolve(resolved)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Navy700),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Confirm Resolution", color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * Raw OCR Audit Trail Modal:
 * Allows enforcement officers to view the unformatted raw text extracted by ML Kit
 * across each captured angle.
 */
@Composable
fun RawOcrAuditSheet(
    ocrResults: List<OCRResult>,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .testTag("raw_ocr_audit_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Cyan400,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Raw OCR Audit Trail",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Unprocessed ML Kit Latin & Devanagari blocks",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (ocrResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No OCR data available yet. Run analysis first.",
                            color = Slate400,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(ocrResults) { result ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "PANEL: ${result.side.name}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Cyan400,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )

                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(result.rawText))
                                                Toast.makeText(context, "Raw OCR copied to clipboard", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy text",
                                                tint = Slate400,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = result.rawText.ifBlank { "(No text detected in this panel)" },
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Slate200,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "${result.blocks.size} text blocks identified • Processing time: ${result.processingTimeMs}ms",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Slate500,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Navy700),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Audit Trail", color = Color.White)
                }
            }
        }
    }
}
