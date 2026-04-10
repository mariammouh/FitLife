package com.example.fitlife

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("register.php")
    fun registerUser(
        @Field("full_name") fullName: String,
        @Field("email") email: String,
        @Field("password") pass: String,
        @Field("height") height: String,
        @Field("weight") weight: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("ai_coach.php")
    fun getAiStrategy(@Field("email") email: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("login.php")
    fun loginUser(
        @Field("email") email: String,
        @Field("password") pass: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("get_profile.php")
    fun getProfile(@Field("email") email: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("update_profile.php")
    fun updateProfile(
        @Field("email") email: String,
        @Field("full_name") name: String,
        @Field("age") age: String,
        @Field("goal") goal: String
    ): Call<ResponseBody>

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

    @FormUrlEncoded
    @POST("save_activity.php")
    fun saveActivity(
        @Field("user_id") userId: Int,
        @Field("name") name: String,
        @Field("type") type: String,
        @Field("duration") duration: String,
        @Field("distance") distance: String,
        @Field("calories") calories: String,
        @Field("date") date: String,
        @Field("start") start: String,
        @Field("end") end: String,
        @Field("location") location: String,
        @Field("notes") notes: String,
        @Field("intensity") intensity: String,
        @Field("avg_heart") avgHeart: String,
        @Field("max_heart") maxHeart: String,
        @Field("sets") sets: String,
        @Field("reps") reps: String,
        @Field("weight") weight: String,
        @Field("mood_before") moodBefore: String,
        @Field("mood_after") moodAfter: String
    ): Call<ResponseBody>
}
