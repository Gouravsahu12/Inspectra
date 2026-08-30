package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InspectionDao {
    @Query("SELECT * FROM inspections ORDER BY timestamp DESC")
    fun getAllInspections(): Flow<List<InspectionEntity>>

    @Query("SELECT * FROM inspections WHERE id = :id OR clientUuid = :id")
    suspend fun getInspectionById(id: String): InspectionEntity?

    @Query("SELECT * FROM inspections WHERE status = :status ORDER BY timestamp DESC")
    fun getInspectionsByStatus(status: String): Flow<List<InspectionEntity>>

    @Query("SELECT * FROM inspections WHERE isPendingSync = 1 ORDER BY timestamp ASC")
    suspend fun getPendingSyncInspections(): List<InspectionEntity>

    @Query("UPDATE inspections SET isPendingSync = 0 WHERE id = :id OR clientUuid = :id")
    suspend fun markAsSynced(id: String)

    @Query("UPDATE inspections SET isReportGenerated = 1, reportUrl = :reportUrl WHERE id = :id OR clientUuid = :id")
    suspend fun updateReport(id: String, reportUrl: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: InspectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(inspections: List<InspectionEntity>)

    @Query("SELECT COUNT(*) FROM inspections")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM inspections WHERE isPendingSync = 1")
    fun getPendingSyncCount(): Flow<Int>

    @Query("DELETE FROM inspections WHERE id = :id OR clientUuid = :id")
    suspend fun deleteById(id: String)
}
