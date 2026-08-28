package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object SupabaseClient {

    val rawSupabaseUrl: String
        get() = try { BuildConfig.SUPABASE_URL } catch (_: Exception) { "https://ftasqoewtueewpspebux.supabase.co/" }

    val rawRestUrl: String
        get() = try {
            val url = BuildConfig.SUPABASE_REST_URL
            if (url.isNotBlank()) url else "https://ftasqoewtueewpspebux.supabase.co/rest/v1/"
        } catch (_: Exception) {
            "https://ftasqoewtueewpspebux.supabase.co/rest/v1/"
        }

    val anonKey: String
        get() = try {
            val key = BuildConfig.SUPABASE_ANON_KEY
            if (key.isNotBlank()) key else "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZ0YXNxb2V3dHVlZXdwc3BlYnV4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc4NDc3NTYsImV4cCI6MjEwMzQyMzc1Nn0.BlXjz5b65TdMCeaQ5-zXZVUiudjzrnCQlgqjdsgatRY"
        } catch (_: Exception) {
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZ0YXNxb2V3dHVlZXdwc3BlYnV4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc4NDc3NTYsImV4cCI6MjEwMzQyMzc1Nn0.BlXjz5b65TdMCeaQ5-zXZVUiudjzrnCQlgqjdsgatRY"
        }

    val publishableKey: String
        get() = try {
            val key = BuildConfig.SUPABASE_PUBLISHABLE_KEY
            if (key.isNotBlank()) key else "sb_publishable_QNVwidbA1axJiN_J2reuZg_M7ULq61Z"
        } catch (_: Exception) {
            "sb_publishable_QNVwidbA1axJiN_J2reuZg_M7ULq61Z"
        }

    val isConfigured: Boolean
        get() = anonKey.isNotBlank() && !anonKey.contains("your_supabase_anon_key")

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("apikey", anonKey)
            .header("Authorization", "Bearer $anonKey")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")

        chain.proceed(requestBuilder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val normalizedBaseUrl: String
        get() {
            var url = rawRestUrl
            if (!url.endsWith("/")) {
                url += "/"
            }
            return url
        }

    private val normalizedRootUrl: String
        get() {
            var url = rawSupabaseUrl
            if (!url.endsWith("/")) {
                url += "/"
            }
            return url
        }

    val apiService: SupabaseApiService by lazy {
        Retrofit.Builder()
            .baseUrl(normalizedBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseApiService::class.java)
    }

    val authService: SupabaseAuthService by lazy {
        Retrofit.Builder()
            .baseUrl(normalizedRootUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseAuthService::class.java)
    }
}
