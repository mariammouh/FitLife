package com.example.fitlife

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    /** 
     * BASE_URL Configuration:
     * - Emulator: "http://10.0.2.2/fitlife/"
     * - Physical Device: Replace with your PC's IPv4 address, e.g., "http://192.168.1.5/fitlife/"
     */
    private const val BASE_URL = "http://192.168.0.102/fitlife/"

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
