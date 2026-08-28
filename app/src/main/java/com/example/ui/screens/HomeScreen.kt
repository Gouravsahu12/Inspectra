package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ComplianceStatus
import com.example.model.InspectionRecord
import com.example.ui.components.InspectraGovBadge
import com.example.ui.components.StatCard
import com.example.ui.components.StatusChip
import com.example.ui.theme.CompliantGreen
import com.example.ui.theme.Cyan600
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.ViolationRed
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.InspectraViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: InspectraViewModel,
    onStartNewInspection: () -> Unit,
    onSelectInspection: (InspectionRecord) -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    val userProfile by viewModel.currentUserProfile.collectAsState()
    val inspections by viewModel.inspections.collectAsState()

    val totalCount = inspections.size
    val compliantCount = inspections.count { it.status == ComplianceStatus.COMPLIANT }
    val violationCount = inspections.count { it.status == ComplianceStatus.NON_COMPLIANT }
    val warningCount = inspections.count { it.status == ComplianceStatus.WARNING }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("home_screen")
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Government Header Badge
            InspectraGovBadge()

            Spacer(modifier = Modifier.height(14.dp))

            // Top User Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(Navy800, Navy700))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userProfile.name.take(2).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Welcome, ${userProfile.role.displayName}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate500,
                                fontSize = 12.sp
                            )
                        )
                        Text(
                            text = userProfile.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isSyncing by viewModel.isSyncing.collectAsState()
                    val syncMessage by viewModel.syncStatusMessage.collectAsState()

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFF0FDF4))
                            .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(20.dp))
                            .clickable { viewModel.syncWithCloud() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isSyncing) WarningAmber else CompliantGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSyncing) "Syncing..." else "Supabase Live",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF166534),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFEFF6FF))
                            .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(CompliantGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Zone-4 Active",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Navy700,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Large Hero CTA Button for NEW INSPECTION
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Navy900,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStartNewInspection() }
                    .testTag("start_inspection_hero_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LEGAL METROLOGY INSPECTION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GoldAccent,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "+ Start New Inspection",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Multi-angle package scan & LMPC 2011 AI verification",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate400,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Cyan600)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera Scan",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Today's Overview Title
            Text(
                text = "Today's Field Overview",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            // 4 Stats Cards Grid (2x2)
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Inspections",
                    value = "$totalCount",
                    subtitle = "Executed today",
                    icon = Icons.Default.Description,
                    iconColor = Navy700,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_card_total")
                )
                Spacer(modifier = Modifier.width(12.dp))
                StatCard(
                    title = "Compliant",
                    value = "$compliantCount",
                    subtitle = "LMPC verified",
                    icon = Icons.Default.CheckCircle,
                    iconColor = CompliantGreen,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_card_compliant")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Violations",
                    value = "$violationCount",
                    subtitle = "Defects identified",
                    icon = Icons.Default.Error,
                    iconColor = ViolationRed,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_card_violations")
                )
                Spacer(modifier = Modifier.width(12.dp))
                StatCard(
                    title = "Warnings / Risk",
                    value = "$warningCount",
                    subtitle = "Under observation",
                    icon = Icons.Default.Warning,
                    iconColor = WarningAmber,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_card_warnings")
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Quick Actions Bar
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionItem(
                    title = "New Scan",
                    icon = Icons.Default.CameraAlt,
                    color = Navy700,
                    onClick = onStartNewInspection
                )
                QuickActionItem(
                    title = "History",
                    icon = Icons.Default.History,
                    color = Cyan600,
                    onClick = { onNavigateToTab(1) }
                )
                QuickActionItem(
                    title = "Analytics",
                    icon = Icons.Default.Analytics,
                    color = Color(0xFF7C3AED),
                    onClick = { onNavigateToTab(2) }
                )
                QuickActionItem(
                    title = "LMPC Rules",
                    icon = Icons.Default.Gavel,
                    color = GoldAccent,
                    onClick = { onNavigateToTab(3) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Inspections Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Recent Inspections",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                Text(
                    text = "View All (${inspections.size})",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Navy700,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.clickable { onNavigateToTab(1) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        if (inspections.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Slate100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = Navy700,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Inspections Yet",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Live database connected to Supabase PostgreSQL. Tap below to scan your first product package.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate500,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onStartNewInspection() },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Navy900),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New Scan", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }

                            Button(
                                onClick = { viewModel.syncWithCloud() },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Slate100),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Slate800
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync Supabase", style = MaterialTheme.typography.labelSmall.copy(color = Slate800, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        } else {
            items(inspections.take(5)) { inspection ->
                InspectionItemCard(
                    inspection = inspection,
                    onClick = { onSelectInspection(inspection) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun QuickActionItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.12f))
                .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = Slate700,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun InspectionItemCard(
    inspection: InspectionRecord,
    onClick: () -> Unit
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(inspection.timestamp))

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("inspection_card_${inspection.id}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail or category icon
            val imgRes = when (inspection.imageDrawableNames.firstOrNull()) {
                "img_edible_oil_package" -> R.drawable.img_edible_oil_package
                "img_snack_package" -> R.drawable.img_snack_package
                else -> R.drawable.img_detergent_package
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate100)
            ) {
                Image(
                    painter = painterResource(id = imgRes),
                    contentDescription = inspection.productName,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = inspection.id,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate500
                        )
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = inspection.productName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900
                    ),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = inspection.storeName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = Slate600
                        ),
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatusChip(status = inspection.status)

                    Text(
                        text = "Score: ${inspection.complianceScore}/100",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (inspection.complianceScore >= 90) CompliantGreen else if (inspection.complianceScore >= 75) WarningAmber else ViolationRed
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "View Details",
                tint = Slate400,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
