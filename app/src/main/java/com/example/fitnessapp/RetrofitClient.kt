package com.example.fitnessapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    /** 
     * BASE_URL Configuration:
     * - Emulator: "http://10.0.2.2/fitlife/"
     * - Physical Device: Replace with your PC's IPv4 address, e.g., "http://192.168.1.5/fitlife/"
     */
    private const val BASE_URL = "http://192.168.0.102/fitlife/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
