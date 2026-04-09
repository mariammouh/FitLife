package com.example.fitnessapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @FormUrlEncoded
    @POST("login.php")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @GET("get_user.php")
    fun getUser(@Query("user_id") userId: Int): Call<UserResponse>

    @GET("get_dashboard_stats.php")
    fun getDashboardStats(@Query("user_id") userId: Int): Call<DashboardStatsResponse>

    @GET("get_activities.php")
    fun getActivities(@Query("user_id") userId: Int): Call<ActivityResponse>

    @POST("update_user.php")
    fun updateUser(@Body body: Map<String, String>): Call<UpdateUserResponse>

    @POST("use_try.php")
    fun useTry(@Body body: Map<String, String>): Call<UseTryResponse>
}