package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ComplianceStatus
import com.example.model.UserRole
import com.example.ui.components.InspectraGovBadge
import com.example.ui.components.StatCard
import com.example.ui.theme.CompliantGreen
import com.example.ui.theme.CompliantGreenBg
import com.example.ui.theme.Cyan600
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: InspectraViewModel
) {
    val inspections by viewModel.inspections.collectAsState()
    val rpcAnalytics by viewModel.analyticsData.collectAsState()
    val isLoading by viewModel.analyticsLoading.collectAsState()
    val errorMessage by viewModel.analyticsError.collectAsState()
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    val isOfficer = currentUserProfile.role == UserRole.OFFICER

    val totalScans = rpcAnalytics?.totalScans ?: inspections.size
    val compliantScans = rpcAnalytics?.compliantScans ?: inspections.count { it.status == ComplianceStatus.COMPLIANT }
    val warningScans = rpcAnalytics?.warningScans ?: inspections.count { it.status == ComplianceStatus.WARNING }
    val violationScans = rpcAnalytics?.violationScans ?: inspections.count { it.status == ComplianceStatus.NON_COMPLIANT }

    val complianceRate = if (totalScans > 0) ((compliantScans * 100f) / totalScans).toInt() else 100
    val averageScore = if (totalScans > 0 && inspections.isNotEmpty()) inspections.map { it.complianceScore }.average().toInt() else 100

    // Violations summary from RPC or computed from local database
    val violationSummaryList = remember(rpcAnalytics, inspections) {
        val currentRpc = rpcAnalytics
        if (currentRpc != null && !currentRpc.topViolations.isNullOrEmpty()) {
            val totalV = currentRpc.violationScans.coerceAtLeast(1)
            currentRpc.topViolations.map { v ->
                val pct = if (v.percentage > 0) v.percentage else ((v.count * 100f) / totalV).toInt().coerceIn(1, 100)
                Triple(v.ruleTitle, v.count, pct)
            }
        } else {
            val allViolations = inspections.flatMap { it.violations }
            val totalViolationsCount = allViolations.size
            if (totalViolationsCount == 0) {
                emptyList()
            } else {
                allViolations.groupBy { it.ruleTitle }
                    .map { (title, list) ->
                        val percentage = (list.size * 100) / totalViolationsCount
                        Triple(title, list.size, percentage)
                    }
                    .sortedByDescending { it.second }
            }
        }
    }

    // Scans over time data points (last 7 days or chronological timestamps)
    val timeSeriesData = remember(rpcAnalytics, inspections) {
        val currentRpc = rpcAnalytics
        if (currentRpc != null && !currentRpc.scansOverTime.isNullOrEmpty()) {
            currentRpc.scansOverTime.map { it.date.takeLast(5) to it.count }
        } else {
            val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
            val sorted = inspections.sortedBy { it.timestamp }
            if (sorted.isEmpty()) {
                emptyList()
            } else {
                sorted.groupBy { dateFormat.format(Date(it.timestamp)) }
                    .map { (dateStr, list) -> dateStr to list.size }
            }
        }
    }

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isOfficer) "Compliance Dashboard" else "Scan Analytics",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    Text(
                        text = if (isOfficer)
                            "Real-time statutory audits and AI compliance analytics"
                        else
                            "Summary of your scanned packages and compliance history",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Slate600)
                    )
                }

                IconButton(
                    onClick = { viewModel.fetchAnalytics() },
                    modifier = Modifier.testTag("analytics_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Analytics",
                        tint = Navy700
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // State Handling: Loading, Error, Empty, or Content
            when {
                isLoading && totalScans == 0 -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = Navy700,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loading analytics from Supabase...",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Slate600)
                            )
                        }
                    }
                }

                errorMessage != null && totalScans == 0 -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ViolationRedBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ViolationRed.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = ViolationRed,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Unable to load cloud analytics",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ViolationRed
                                )
                            )
                            Text(
                                text = errorMessage ?: "Network connection issue",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Slate700,
                                    textAlign = TextAlign.Center
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.fetchAnalytics() },
                                colors = ButtonDefaults.buttonColors(containerColor = Navy700),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }

                totalScans == 0 -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Slate100),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assessment,
                                    contentDescription = null,
                                    tint = Slate500,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No Scans Yet",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Start an inspection or product scan to see real-time charts and compliance analytics here.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Slate500,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }

                else -> {
                    // 4 Core KPI Summary Cards
                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatCard(
                            title = if (isOfficer) "Total Audits" else "Total Scanned",
                            value = "$totalScans",
                            subtitle = if (totalScans == 1) "1 product" else "$totalScans products",
                            icon = Icons.Default.Assessment,
                            iconColor = Navy700,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        StatCard(
                            title = "Safe / Passed",
                            value = "$compliantScans",
                            subtitle = "$complianceRate% pass rate",
                            icon = Icons.Default.CheckCircle,
                            iconColor = CompliantGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatCard(
                            title = "Violations Found",
                            value = "$violationScans",
                            subtitle = if (violationScans == 0) "No violations" else "$violationScans products flagged",
                            icon = Icons.Default.Error,
                            iconColor = ViolationRed,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        StatCard(
                            title = "Average Score",
                            value = "$averageScore%",
                            subtitle = if (averageScore >= 90) "Excellent" else if (averageScore >= 75) "Moderate" else "Action Needed",
                            icon = Icons.Default.TrendingUp,
                            iconColor = Cyan600,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 1. SCANS OVER TIME (Line Chart)
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
                                        imageVector = Icons.Default.ShowChart,
                                        contentDescription = null,
                                        tint = Navy700,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Scans Over Time",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Slate900
                                        )
                                    )
                                }
                                Text(
                                    text = "Trend View",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Slate500,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            ScansLineChart(
                                dataPoints = timeSeriesData,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 2. PASS / FAIL RATE (Pie / Donut Chart)
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
                                    tint = Cyan600,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pass / Fail Distribution",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            PassFailPieChart(
                                compliantCount = compliantScans,
                                warningCount = warningScans,
                                violationCount = violationScans,
                                totalCount = totalScans
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3. TOP VIOLATION TYPES (List)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ReportProblem,
                                    contentDescription = null,
                                    tint = if (violationSummaryList.isNotEmpty()) ViolationRed else CompliantGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Top Violation Types",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (violationSummaryList.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Zero violations detected! All inspected products complied with packaging rules.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = CompliantGreen,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            } else {
                                violationSummaryList.take(6).forEachIndexed { idx, item ->
                                    val barColor = when (idx) {
                                        0 -> ViolationRed
                                        1 -> WarningAmber
                                        else -> Cyan600
                                    }
                                    InfractionBarItem(
                                        rule = item.first,
                                        percentage = item.third,
                                        count = item.second,
                                        barColor = barColor
                                    )
                                    if (idx < violationSummaryList.size - 1) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ScansLineChart(
    dataPoints: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No chronological data available", color = Slate400, fontSize = 12.sp)
        }
        return
    }

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(dataPoints) {
        animatedProgress.animateTo(1f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
    }

    val maxCount = (dataPoints.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val width = size.width
            val height = size.height
            val stepX = if (dataPoints.size > 1) width / (dataPoints.size - 1) else width / 2

            // Draw grid lines
            for (i in 0..3) {
                val y = height * (i / 3f)
                drawLine(
                    color = Color(0xFFF1F5F9),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val points = dataPoints.mapIndexed { index, pair ->
                val x = if (dataPoints.size == 1) width / 2f else index * stepX
                val normalizedY = (pair.second.toFloat() / maxCount) * animatedProgress.value
                val y = height - (normalizedY * (height - 16.dp.toPx())) - 8.dp.toPx()
                Offset(x, y)
            }

            if (points.size >= 2) {
                val path = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        val controlX = (prev.x + curr.x) / 2f
                        cubicTo(controlX, prev.y, controlX, curr.y, curr.x, curr.y)
                    }
                }

                // Fill area gradient under curve
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(points.last().x, height)
                    lineTo(points.first().x, height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Navy700.copy(alpha = 0.25f), Color.Transparent),
                        startY = 0f,
                        endY = height
                    )
                )

                // Draw curve line
                drawPath(
                    path = path,
                    color = Navy700,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Draw circular markers
            points.forEach { point ->
                drawCircle(color = Color.White, radius = 5.dp.toPx(), center = point)
                drawCircle(color = Navy700, radius = 3.dp.toPx(), center = point)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // X-axis label row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dataPoints.forEach { point ->
                Text(
                    text = point.first,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate500,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
fun PassFailPieChart(
    compliantCount: Int,
    warningCount: Int,
    violationCount: Int,
    totalCount: Int
) {
    val total = if (totalCount > 0) totalCount.toFloat() else 1f
    val compliantAngle = (compliantCount / total) * 360f
    val warningAngle = (warningCount / total) * 360f
    val violationAngle = (violationCount / total) * 360f

    val anim = remember { Animatable(0f) }
    LaunchedEffect(totalCount) {
        anim.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(110.dp)) {
                val strokeWidth = 20.dp.toPx()
                var startAngle = -90f

                if (compliantCount > 0) {
                    val sweep = compliantAngle * anim.value
                    drawArc(
                        color = CompliantGreen,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    startAngle += sweep
                }

                if (warningCount > 0) {
                    val sweep = warningAngle * anim.value
                    drawArc(
                        color = WarningAmber,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    startAngle += sweep
                }

                if (violationCount > 0) {
                    val sweep = violationAngle * anim.value
                    drawArc(
                        color = ViolationRed,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$totalCount",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate500,
                        fontSize = 10.sp
                    )
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val passPct = if (totalCount > 0) ((compliantCount * 100f) / totalCount).toInt() else 0
            val warnPct = if (totalCount > 0) ((warningCount * 100f) / totalCount).toInt() else 0
            val failPct = if (totalCount > 0) ((violationCount * 100f) / totalCount).toInt() else 0

            LegendItem(color = CompliantGreen, label = "Compliant", count = "$compliantCount ($passPct%)")
            LegendItem(color = WarningAmber, label = "Warning", count = "$warningCount ($warnPct%)")
            LegendItem(color = ViolationRed, label = "Non-Compliant", count = "$violationCount ($failPct%)")
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, count: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                color = Slate700,
                fontSize = 11.5.sp
            )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = count,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Slate900,
                fontSize = 11.5.sp
            )
        )
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
                    fontSize = 12.sp
                ),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$percentage% ($count case${if (count > 1) "s" else ""})",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate700,
                    fontSize = 11.sp
                )
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Slate100)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((percentage.coerceIn(5, 100)) / 100f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )
        }
    }
}
