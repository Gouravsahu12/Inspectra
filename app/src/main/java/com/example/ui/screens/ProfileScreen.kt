package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InspectorProfile
import com.example.ui.components.InspectraGovBadge
import com.example.ui.theme.CompliantGreen
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
import com.example.ui.theme.Slate900
import com.example.ui.theme.ViolationRed
import com.example.viewmodel.InspectraViewModel

@Composable
fun ProfileScreen(
    viewModel: InspectraViewModel,
    onLogout: () -> Unit
) {
    val profile by viewModel.inspectorProfile.collectAsState()
    var showRuleDirectory by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("profile_screen")
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            InspectraGovBadge()

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Inspector Credentials & Settings",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Officer ID Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Navy900,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E293B))
                                    .border(2.dp, GoldAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "RS",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = GoldAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = profile.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = profile.designation,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Slate400,
                                        fontSize = 11.sp
                                    )
                                )
                                Text(
                                    text = "Badge ID: ${profile.id}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = GoldAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "JURISDICTION ZONE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    color = Slate400,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = profile.jurisdiction,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "AUTH STATUS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    color = Slate400,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(CompliantGreen)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ACTIVE DSC TOKEN",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = CompliantGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Statutory Reference Manual (LMPC 2011)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showRuleDirectory = !showRuleDirectory }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = Navy700,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "LMPC Rules (2011) Statutory Directory",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                )
                                Text(
                                    text = if (showRuleDirectory) "Tap to collapse" else "Tap to view mandatory declaration rules",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Slate500,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    if (showRuleDirectory) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Slate200)
                        Spacer(modifier = Modifier.height(10.dp))

                        LmpcRuleReferenceItem(
                            rule = "Rule 6(1)(a)",
                            title = "Manufacturer / Packer / Importer Info",
                            desc = "Name and complete address of manufacturer, packer or importer must be clearly printed."
                        )
                        LmpcRuleReferenceItem(
                            rule = "Rule 6(1)(b)",
                            title = "Common or Generic Commodity Name",
                            desc = "The generic name of the commodity contained in the package must be prominently displayed."
                        )
                        LmpcRuleReferenceItem(
                            rule = "Rule 6(1)(c)",
                            title = "Net Quantity Declaration",
                            desc = "Net quantity in terms of standard metric units (kg, g, L, ml, N) with minimum required font height."
                        )
                        LmpcRuleReferenceItem(
                            rule = "Rule 6(1)(d)",
                            title = "Month & Year of Manufacture / Packing",
                            desc = "Clear indication in MM/YYYY format for domestic packages and imported goods."
                        )
                        LmpcRuleReferenceItem(
                            rule = "Rule 6(1)(e)",
                            title = "Maximum Retail Price (MRP)",
                            desc = "MRP format: 'MRP ₹ xx.xx (incl. of all taxes)' or 'Maximum Retail Price ₹ xx.xx (inclusive of all taxes)'."
                        )
                        LmpcRuleReferenceItem(
                            rule = "Rule 6(1)(f)",
                            title = "Consumer Care & Grievance Redressal",
                            desc = "Name, address, phone number and email address of person/office for consumer complaints."
                        )
                        LmpcRuleReferenceItem(
                            rule = "Rule 6(1)(da)",
                            title = "Country of Origin",
                            desc = "Mandatory for all imported packages and commodities sold in retail format."
                        )
                        LmpcRuleReferenceItem(
                            rule = "Section 36",
                            title = "Legal Metrology Act, 2009 Penalty",
                            desc = "Penalty for selling non-standard packages up to ₹25,000 for first offence, ₹50,000 for second offence."
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // System Engine & Local Database Status Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "System Diagnostics & Encryption",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ProfileStatusRow(label = "Local Room Database", value = "Encrypted SQLite (Active)")
                    ProfileStatusRow(label = "Compliance Engine Version", value = "LMPC-Engine v2.4 (2026.08)")
                    ProfileStatusRow(label = "Neural OCR Model", value = "Antigravity On-Device Tensor")
                    ProfileStatusRow(label = "Evidence Cryptographic Hash", value = "SHA-256 Verified")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Logout Button
            OutlinedButton(
                onClick = onLogout,
                shape = RoundedCornerShape(10.dp),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = ViolationRed
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = ViolationRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LOGOUT OF INSPECTOR SESSION",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ViolationRed
                    )
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun LmpcRuleReferenceItem(rule: String, title: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = rule,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Navy700
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Slate900
                )
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                color = Slate600
            )
        )
    }
}

@Composable
fun ProfileStatusRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 12.sp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = Slate900,
                fontSize = 12.sp
            )
        )
    }
}
