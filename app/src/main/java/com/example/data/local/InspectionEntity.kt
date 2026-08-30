package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inspections")
data class InspectionEntity(
    @PrimaryKey
    val id: String, // e.g. INS-2026-000124
    val clientUuid: String = id,
    val userId: String = "",
    val productName: String,
    val brandManufacturer: String,
    val category: String, // ProductCategory name
    val locationName: String,
    val facilityType: String,
    val storeName: String,
    val inspectorId: String,
    val inspectorName: String,
    val timestamp: Long,
    val complianceScore: Int,
    val status: String, // ComplianceStatus name
    val extractedFieldsJson: String, // JSON serialized fields
    val violationsJson: String, // JSON serialized violations
    val imageDrawableNames: String, // Comma separated
    val officerNotes: String,
    val isReportGenerated: Boolean = false,
    val reportUrl: String? = null,
    val manufacturerRiskLevel: String = "LOW",
    val isPendingSync: Boolean = false
)
