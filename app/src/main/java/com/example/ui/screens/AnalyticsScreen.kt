package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ComplianceStatus
import com.example.ui.components.InspectraGovBadge
import com.example.ui.components.StatCard
import com.example.ui.theme.CompliantGreen
import com.example.ui.theme.Cyan600
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.Navy700
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
import com.example.ui.theme.ViolationRedBg
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberBg
import com.example.viewmodel.InspectraViewModel

@Composable
fun AnalyticsScreen(
    viewModel: InspectraViewModel
) {
    val inspections by viewModel.inspections.collectAsState()

    val total = inspections.size
    val compliant = inspections.count { it.status == ComplianceStatus.COMPLIANT }
    val complianceRate = if (total > 0) (compliant * 100) / total else 75

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("analytics_screen")
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            InspectraGovBadge()

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Enforcement Intelligence & Analytics",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            )

            Text(
                text = "Zonal Compliance Metrics & High-Risk Manufacturer Surveillance",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4 KPI Summary Cards
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Compliance Rate",
                    value = "$complianceRate%",
                    subtitle = "Zone 4 Average",
                    icon = Icons.Default.TrendingUp,
                    iconColor = CompliantGreen,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                StatCard(
                    title = "Notices Issued",
                    value = "18",
                    subtitle = "Section 36 Act",
                    icon = Icons.Default.Gavel,
                    iconColor = Navy700,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Flagged Brands",
                    value = "4",
                    subtitle = "High recidivism",
                    icon = Icons.Default.Warning,
                    iconColor = ViolationRed,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                StatCard(
                    title = "Targeted Audits",
                    value = "46",
                    subtitle = "Completed MTD",
                    icon = Icons.Default.Assessment,
                    iconColor = Cyan600,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Top Repeated LMPC Violations Breakdown
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = Navy700,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Top Statutory Rule Infractions (Zone 4)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    InfractionBarItem(
                        rule = "Rule 6(1)(f) Consumer Care Address/Email",
                        percentage = 42,
                        count = 14,
                        barColor = ViolationRed
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    InfractionBarItem(
                        rule = "Rule 6(1)(e) MRP inclusive declaration format",
                        percentage = 28,
                        count = 9,
                        barColor = WarningAmber
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    InfractionBarItem(
                        rule = "Rule 6(1)(c) Non-standard Net Qty unit font",
                        percentage = 18,
                        count = 6,
                        barColor = Cyan600
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    InfractionBarItem(
                        rule = "Rule 6(1)(d) Date of Packing MM/YYYY absent",
                        percentage = 12,
                        count = 4,
                        barColor = Slate600
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // High-Risk Manufacturer Surveillance Intelligence
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = ViolationRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "High-Risk Manufacturer Watchlist",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ManufacturerWatchlistItem(
                        name = "ABC Chemicals Pvt Ltd",
                        category = "Household Detergents",
                        infractions = "3 Violations (Repeated)",
                        riskLevel = "HIGH RISK",
                        riskColor = ViolationRed,
                        riskBg = ViolationRedBg,
                        statusAction = "Show Cause Issued"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ManufacturerWatchlistItem(
                        name = "Delight Bakers LLP",
                        category = "Packaged Bakery",
                        infractions = "1 Borderline Font Defect",
                        riskLevel = "MEDIUM",
                        riskColor = WarningAmber,
                        riskBg = WarningAmberBg,
                        statusAction = "Warning Issued"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ManufacturerWatchlistItem(
                        name = "Kriti Agro Industries Ltd",
                        category = "Edible Oils & Fats",
                        infractions = "0 Violations",
                        riskLevel = "LOW RISK",
                        riskColor = CompliantGreen,
                        riskBg = Color(0xFFDCFCE7),
                        statusAction = "Fully Compliant"
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Category Compliance Comparison
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Category Compliance Breakdown",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    CategoryRow(name = "Edible Oils & Ghee", compliance = "94% Compliant", color = CompliantGreen)
                    CategoryRow(name = "Food & Beverages", compliance = "82% Compliant", color = CompliantGreen)
                    CategoryRow(name = "Cosmetics & Toiletries", compliance = "64% Compliant", color = WarningAmber)
                    CategoryRow(name = "Household Cleaning / Chemicals", compliance = "48% Compliant", color = ViolationRed)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun InfractionBarItem(
    rule: String,
    percentage: Int,
    count: Int,
    barColor: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = rule,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = Slate800,
                    fontSize = 11.sp
                ),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$percentage% ($count cases)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate600,
                    fontSize = 11.sp
                )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Slate100)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage / 100f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )
        }
    }
}

@Composable
fun ManufacturerWatchlistItem(
    name: String,
    category: String,
    infractions: String,
    riskLevel: String,
    riskColor: Color,
    riskBg: Color,
    statusAction: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Slate100,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                Text(
                    text = "$category • $infractions",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate500,
                        fontSize = 11.sp
                    )
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(riskBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = riskLevel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = riskColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = statusAction,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate600,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
fun CategoryRow(name: String, compliance: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                color = Slate700
            )
        )
        Text(
            text = compliance,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}
