package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ComplianceStatus
import com.example.model.Severity
import com.example.ui.theme.CompliantGreen
import com.example.ui.theme.CompliantGreenBg
import com.example.ui.theme.CompliantGreenBorder
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
import com.example.ui.theme.ViolationRedBg
import com.example.ui.theme.ViolationRedBorder
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberBg
import com.example.ui.theme.WarningAmberBorder

@Composable
fun InspectraGovBadge(
    modifier: Modifier = Modifier,
    isDark: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isDark) Navy900 else Slate100)
            .border(1.dp, if (isDark) Navy700 else Slate200, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = "Government Official",
            tint = if (isDark) GoldAccent else Navy700,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "MINISTRY OF CONSUMER AFFAIRS • LEGAL METROLOGY",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            ),
            color = if (isDark) Color(0xFFE2E8F0) else Slate700
        )
    }
}

@Composable
fun StatusChip(
    status: ComplianceStatus,
    modifier: Modifier = Modifier
) {
    val (bg, border, textCol, label, icon) = when (status) {
        ComplianceStatus.COMPLIANT -> Tuple5(
            CompliantGreenBg,
            CompliantGreenBorder,
            CompliantGreen,
            "COMPLIANT",
            Icons.Default.CheckCircle
        )
        ComplianceStatus.WARNING -> Tuple5(
            WarningAmberBg,
            WarningAmberBorder,
            WarningAmber,
            "WARNING",
            Icons.Default.Warning
        )
        ComplianceStatus.NON_COMPLIANT -> Tuple5(
            ViolationRedBg,
            ViolationRedBorder,
            ViolationRed,
            "NON-COMPLIANT",
            Icons.Default.Error
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textCol,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = textCol
        )
    }
}

@Composable
fun SeverityChip(
    severity: Severity,
    modifier: Modifier = Modifier
) {
    val (bg, textCol, label) = when (severity) {
        Severity.HIGH -> Triple(ViolationRedBg, ViolationRed, "HIGH SEVERITY")
        Severity.MEDIUM -> Triple(WarningAmberBg, WarningAmber, "MEDIUM SEVERITY")
        Severity.LOW -> Triple(Slate100, Slate700, "LOW SEVERITY")
    }

    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        ),
        color = textCol,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun ConfidenceIndicator(
    confidence: Float,
    modifier: Modifier = Modifier
) {
    val pct = (confidence * 100).toInt()
    val isHigh = confidence >= 0.85f
    val isMed = confidence >= 0.70f

    val (color, text) = when {
        isHigh -> Pair(CompliantGreen, "$pct% confidence")
        isMed -> Pair(WarningAmber, "$pct% confidence")
        else -> Pair(ViolationRed, "$pct% low confidence")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = if (isHigh) Slate500 else color
        )
    }
}

@Composable
fun ComplianceScoreMeter(
    score: Int,
    status: ComplianceStatus,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(score) {
        animatedProgress.animateTo(
            targetValue = score / 100f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    val meterColor = when (status) {
        ComplianceStatus.COMPLIANT -> CompliantGreen
        ComplianceStatus.WARNING -> WarningAmber
        ComplianceStatus.NON_COMPLIANT -> ViolationRed
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 12.dp.toPx()
            // Background track
            drawArc(
                color = Color(0xFFE2E8F0),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            // Progress arc
            drawArc(
                color = meterColor,
                startAngle = 135f,
                sweepAngle = 270f * animatedProgress.value,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${(animatedProgress.value * 100).toInt()}",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                Text(
                    text = "/100",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Slate500,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                )
            }
            Text(
                text = "Compliance Score",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = Slate600,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Slate600
                    )
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(iconColor.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = Slate500
                )
            )
        }
    }
}

private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
