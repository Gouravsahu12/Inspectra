package com.example.data.remote

import com.example.data.remote.dto.AnalyticsRpcResponse
import com.example.data.remote.dto.CheckComplianceRequest
import com.example.data.remote.dto.CheckComplianceResponse
import com.example.data.remote.dto.ProcessScanImageRequest
import com.example.data.remote.dto.ProcessScanImageResponse
import com.example.data.remote.dto.SupabaseInspectionDto
import com.example.data.remote.dto.SupabaseOfficerDto
import com.example.data.remote.dto.SupabaseScanHistoryDto
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SupabaseApiService {

    // 1. Scan History Table (CRUD / Upsert by client_uuid or id)
    @GET("scan_history")
    suspend fun getScanHistory(
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc",
        @Query("limit") limit: Int = 100
    ): Response<List<SupabaseScanHistoryDto>>

    @POST("scan_history")
    @Headers("Prefer: resolution=merge-duplicates, return=representation")
    suspend fun upsertScanHistory(
        @Body scan: SupabaseScanHistoryDto
    ): Response<List<SupabaseScanHistoryDto>>

    @POST("scan_history")
    @Headers("Prefer: resolution=merge-duplicates, return=representation")
    suspend fun upsertScanHistoryBatch(
        @Body scans: List<SupabaseScanHistoryDto>
    ): Response<List<SupabaseScanHistoryDto>>

    // Inspections Table
    @GET("inspections")
    suspend fun getInspections(
        @Query("select") select: String = "*",
        @Query("order") order: String = "timestamp.desc",
        @Query("limit") limit: Int = 100
    ): Response<List<SupabaseInspectionDto>>

    @POST("inspections")
    @Headers("Prefer: resolution=merge-duplicates, return=representation")
    suspend fun insertInspection(
        @Body inspection: SupabaseInspectionDto
    ): Response<List<SupabaseInspectionDto>>

    @GET("officers")
    suspend fun getOfficers(
        @Query("select") select: String = "*"
    ): Response<List<SupabaseOfficerDto>>

    // 2. Storage Upload into "scan-images" bucket
    @POST("storage/v1/object/{bucket}/{path}")
    suspend fun uploadStorageFile(
        @Path("bucket") bucket: String,
        @Path(value = "path", encoded = true) path: String,
        @Body fileBytes: RequestBody,
        @Header("x-upsert") upsert: String = "true"
    ): Response<ResponseBody>

    @PUT("storage/v1/object/{bucket}/{path}")
    suspend fun uploadStorageFilePut(
        @Path("bucket") bucket: String,
        @Path(value = "path", encoded = true) path: String,
        @Body fileBytes: RequestBody,
        @Header("x-upsert") upsert: String = "true"
    ): Response<ResponseBody>

    // 3. RPC Functions (PostgREST)
    @POST("rpc/process_scan_image")
    suspend fun processScanImageRpc(
        @Body request: ProcessScanImageRequest
    ): Response<ProcessScanImageResponse>

    @POST("rpc/check_compliance")
    suspend fun checkComplianceRpc(
        @Body request: CheckComplianceRequest
    ): Response<CheckComplianceResponse>

    @POST("rpc/get_analytics")
    suspend fun getAnalyticsRpc(
        @Body params: Map<String, String> = emptyMap()
    ): Response<AnalyticsRpcResponse>

    @POST("rpc/get_scan_analytics")
    suspend fun getScanAnalyticsRpc(
        @Body params: Map<String, String> = emptyMap()
    ): Response<AnalyticsRpcResponse>
}

interface SupabaseFunctionsService {
    @POST("functions/v1/process-scan-image")
    suspend fun processScanImage(
        @Body request: ProcessScanImageRequest
    ): Response<ProcessScanImageResponse>

    @POST("functions/v1/check-compliance")
    suspend fun checkCompliance(
        @Body request: CheckComplianceRequest
    ): Response<CheckComplianceResponse>

    @POST("functions/v1/generate-report")
    suspend fun generateReport(
        @Body request: Map<String, String>
    ): Response<ResponseBody>
}
