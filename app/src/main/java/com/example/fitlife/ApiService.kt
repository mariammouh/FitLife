package com.example.fitlife

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("register.php")
    fun registerUser(
        @Field("full_name") fullName: String,
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("password") pass: String,
        @Field("phone") phone: String,
        @Field("gender") gender: String,
        @Field("dob") dob: String,
        @Field("height") height: String,
        @Field("start_weight") startWeight: String,
        @Field("current_weight") currentWeight: String,
        @Field("goal_weight") goalWeight: String,
        @Field("goal") goal: String,
        @Field("fitness_level") fitnessLevel: String
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
    @POST("change_password.php")
    fun changePassword(
        @Field("user_id") userId: Int,
        @Field("new_password") newPass: String
    ): Call<UpdateUserResponse>

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
