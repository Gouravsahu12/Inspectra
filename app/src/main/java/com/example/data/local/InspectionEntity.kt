package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.ComplianceStatus
import com.example.model.ProductCategory

@Entity(tableName = "inspections")
data class InspectionEntity(
    @PrimaryKey
    val id: String, // e.g. INS-2026-000124
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
    val isReportGenerated: Boolean,
    val manufacturerRiskLevel: String
)
