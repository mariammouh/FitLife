package com.example.fitlife

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

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

    // جلب معلومات البروفايل
    @FormUrlEncoded
    @POST("get_profile.php")
    fun getProfile(@Field("email") email: String): Call<ResponseBody>

    // تحديث معلومات البروفايل
    @FormUrlEncoded
    @POST("update_profile.php")
    fun updateProfile(
        @Field("email") email: String,
        @Field("full_name") name: String,
        @Field("age") age: String,
        @Field("goal") goal: String
    ): Call<ResponseBody>
}