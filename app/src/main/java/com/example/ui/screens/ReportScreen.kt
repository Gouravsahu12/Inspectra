package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ComplianceStatus
import com.example.model.FieldStatus
import com.example.model.UserRole
import com.example.ui.components.InspectraGovBadge
import com.example.ui.components.StatusChip
import com.example.ui.theme.CompliantGreen
import com.example.ui.theme.Cyan600
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.Navy700
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
import com.example.viewmodel.InspectraViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportScreen(
    viewModel: InspectraViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val inspectionId by viewModel.activeInspectionId.collectAsState()
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    val isOfficer = currentUserProfile.role == UserRole.OFFICER
    val location by viewModel.locationName.collectAsState()
    val store by viewModel.storeName.collectAsState()
    val category by viewModel.selectedCategory.collectAsState()
    val score by viewModel.complianceScore.collectAsState()
    val status by viewModel.complianceStatus.collectAsState()
    val violations by viewModel.violations.collectAsState()
    val extractedFields by viewModel.extractedFields.collectAsState()
    val noticeIssued by viewModel.reportNoticeIssued.collectAsState()

    var showPdfDownloadedNotification by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
    val currentDateStr = dateFormat.format(Date())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .testTag("report_screen")
    ) {
        // Top Toolbar
        Surface(
            color = Navy900,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
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
                            text = "Inspection Report Preview",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = if (isOfficer) "Form LM-IR/2026 • Official Government Record" else "Packaging Verification Report • User Record",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = {
                            showPdfDownloadedNotification = true
                            Toast.makeText(context, "PDF Report Exported: $inspectionId.pdf", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF Export",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Official Notice shared via Enforcement Registry", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Notification Banner when exported
        AnimatedVisibility(visible = showPdfDownloadedNotification) {
            Surface(
                color = CompliantGreen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Report generated and archived: $inspectionId.pdf",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // Document Paper Canvas
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                shadowElevation = 3.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Document Header
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_icon),
                            contentDescription = "Emblem",
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (isOfficer) {
                            Text(
                                text = "GOVERNMENT OF INDIA",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate700,
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = "DEPARTMENT OF CONSUMER AFFAIRS",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                            )
                            Text(
                                text = "LEGAL METROLOGY ENFORCEMENT DIVISION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate600,
                                    fontSize = 10.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "OFFICIAL PACKAGED COMMODITIES INSPECTION REPORT (FORM LM-IR/2026)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Navy700,
                                    fontSize = 10.sp
                                )
                            )
                        } else {
                            Text(
                                text = "LEGAL METROLOGY COMPLIANCE VERIFICATION",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                            )
                            Text(
                                text = "PACKAGED COMMODITIES DIGITAL AUDIT RECORD",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Navy700,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Slate200)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Inspection Details & User Details Metadata Grid
                    ReportRow(label = "Inspection ID", value = inspectionId, isBold = true)
                    ReportRow(label = "Date & Time", value = currentDateStr)
                    ReportRow(label = "Full Name", value = currentUserProfile.name, isBold = true)
                    ReportRow(label = "User ID", value = currentUserProfile.id)
                    ReportRow(label = "Role", value = currentUserProfile.role.displayName)

                    if (isOfficer) {
                        ReportRow(label = "Inspecting Officer", value = "${currentUserProfile.name} (${currentUserProfile.id})")
                        ReportRow(label = "Jurisdiction Zone", value = currentUserProfile.jurisdiction)
                    }

                    if (store.isNotBlank()) {
                        ReportRow(label = "Establishment", value = store)
                    }
                    if (location.isNotBlank()) {
                        ReportRow(label = "Premises Address", value = location)
                    }
                    ReportRow(label = "Commodity Category", value = "${category.displayName} (${category.code})")

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Slate200)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Compliance Score & Status Summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "STATUTORY COMPLIANCE STATUS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate600
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            StatusChip(status = status)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "COMPLIANCE SCORE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate600
                                )
                            )
                            Text(
                                text = "$score / 100",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (score >= 90) CompliantGreen else ViolationRed
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "EXTRACTED STATUTORY DECLARATIONS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate800,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    extractedFields.forEach { field ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${field.ruleReference} ${field.fieldName}:",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Slate600,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.weight(0.45f)
                            )
                            Text(
                                text = if (field.extractedValue.isBlank()) "NOT DETECTED" else field.extractedValue,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (field.extractedValue.isBlank()) ViolationRed else Slate900,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.weight(0.55f)
                            )
                        }
                    }

                    if (violations.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "STATUTORY VIOLATIONS & CHARGES IDENTIFIED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ViolationRed,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        violations.forEachIndexed { index, violation ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ViolationRedBg.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "${index + 1}. ${violation.ruleNumber}: ${violation.ruleTitle}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = ViolationRed
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Finding: ${violation.explanation}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Slate800,
                                            fontSize = 11.sp
                                        )
                                    )
                                    Text(
                                        text = "Statutory Ref: ${violation.legalActRef}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Slate600,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = Slate200)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Digital Verification / Signature Box
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .border(1.dp, Slate300, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (isOfficer) "DIGITALLY SIGNED" else "VERIFIED AUDIT",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CompliantGreen
                                        )
                                    )
                                    Text(
                                        text = if (isOfficer) "DSC Token: GOI-LM-8492-2026" else "Record ID: ${currentUserProfile.id}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 9.sp,
                                            color = Slate500
                                        )
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = currentUserProfile.name,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Text(
                                text = if (isOfficer) "Senior Legal Metrology Officer" else "${currentUserProfile.role.displayName} • Verified User",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    color = Slate500
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            if (isOfficer && !noticeIssued && violations.isNotEmpty()) {
                Button(
                    onClick = {
                        viewModel.issueEnforcementNotice()
                        Toast.makeText(context, "Show Cause Notice (Form LM-SC/26) Issued", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ViolationRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("issue_compound_notice_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ISSUE SECTION 36 SHOW-CAUSE NOTICE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Button(
                onClick = {
                    showPdfDownloadedNotification = true
                    Toast.makeText(context, "Inspection Report Saved & PDF Exported", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Navy700),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("download_pdf_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DOWNLOAD OFFICIAL PDF REPORT",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ReportRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Slate500,
                fontSize = 11.sp
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Slate900,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp
            )
        )
    }
}
