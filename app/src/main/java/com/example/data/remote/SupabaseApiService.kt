package com.example.data.remote

import com.example.data.remote.dto.SupabaseInspectionDto
import com.example.data.remote.dto.SupabaseOfficerDto
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SupabaseApiService {

    @GET("inspections")
    suspend fun getInspections(
        @Query("select") select: String = "*",
        @Query("order") order: String = "timestamp.desc",
        @Query("limit") limit: Int = 100
    ): Response<List<SupabaseInspectionDto>>

    @POST("inspections")
    @Headers("Prefer: return=representation")
    suspend fun insertInspection(
        @Body inspection: SupabaseInspectionDto
    ): Response<List<SupabaseInspectionDto>>

    @GET("officers")
    suspend fun getOfficers(
        @Query("select") select: String = "*"
    ): Response<List<SupabaseOfficerDto>>

    @POST("storage/v1/object/{bucket}/{path}")
    suspend fun uploadStorageFile(
        @Path("bucket") bucket: String,
        @Path("path") path: String,
        @Body fileBytes: RequestBody
    ): Response<ResponseBody>
}
