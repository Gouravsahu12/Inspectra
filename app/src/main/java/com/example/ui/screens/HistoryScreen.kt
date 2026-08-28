package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.model.InspectionRecord
import com.example.ui.components.InspectraGovBadge
import com.example.ui.theme.Cyan600
import com.example.ui.theme.Navy700
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.viewmodel.InspectraViewModel

@Composable
fun HistoryScreen(
    viewModel: InspectraViewModel,
    onSelectInspection: (InspectionRecord) -> Unit
) {
    val inspections by viewModel.inspections.collectAsState()
    val searchQuery by viewModel.historySearchQuery.collectAsState()
    val statusFilter by viewModel.historyStatusFilter.collectAsState()

    val filteredInspections = inspections.filter { item ->
        val matchesSearch = searchQuery.isBlank() ||
                item.productName.contains(searchQuery, ignoreCase = true) ||
                item.storeName.contains(searchQuery, ignoreCase = true) ||
                item.id.contains(searchQuery, ignoreCase = true) ||
                item.brandManufacturer.contains(searchQuery, ignoreCase = true)

        val matchesFilter = statusFilter == null || item.status == statusFilter

        matchesSearch && matchesFilter
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("history_screen")
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            InspectraGovBadge()

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Enforcement History Registry",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            )

            Text(
                text = "Archived Legal Metrology inspection records and statutory notices",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setHistorySearchQuery(it) },
                placeholder = { Text("Search by Product, Brand, Store, ID...", fontSize = 13.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setHistorySearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Slate400,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_search_bar"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Navy700,
                    unfocusedBorderColor = Slate300
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Status Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    HistoryFilterChip(
                        title = "All (${inspections.size})",
                        isSelected = statusFilter == null,
                        onClick = { viewModel.setHistoryStatusFilter(null) }
                    )
                }
                item {
                    HistoryFilterChip(
                        title = "Compliant (${inspections.count { it.status == ComplianceStatus.COMPLIANT }})",
                        isSelected = statusFilter == ComplianceStatus.COMPLIANT,
                        onClick = { viewModel.setHistoryStatusFilter(ComplianceStatus.COMPLIANT) }
                    )
                }
                item {
                    HistoryFilterChip(
                        title = "Non-Compliant (${inspections.count { it.status == ComplianceStatus.NON_COMPLIANT }})",
                        isSelected = statusFilter == ComplianceStatus.NON_COMPLIANT,
                        onClick = { viewModel.setHistoryStatusFilter(ComplianceStatus.NON_COMPLIANT) }
                    )
                }
                item {
                    HistoryFilterChip(
                        title = "Warning (${inspections.count { it.status == ComplianceStatus.WARNING }})",
                        isSelected = statusFilter == ComplianceStatus.WARNING,
                        onClick = { viewModel.setHistoryStatusFilter(ComplianceStatus.WARNING) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Showing ${filteredInspections.size} records",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate500
                )
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (filteredInspections.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No inspection records match the filter",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            )
                        )
                        Text(
                            text = "Try adjusting your search keywords or status filter.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                        )
                    }
                }
            }
        } else {
            items(filteredInspections) { item ->
                InspectionItemCard(
                    inspection = item,
                    onClick = { onSelectInspection(item) }
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
fun HistoryFilterChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Navy700 else Slate100)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Slate700,
                fontSize = 11.sp
            )
        )
    }
}
