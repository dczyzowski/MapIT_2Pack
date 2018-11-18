package com.czyzowsk.mapsit.interfaces

import com.czyzowsk.mapsit.models.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface GetGeocodings {
    @GET("suggest.json")
    fun getSuggestions(@Query("app_id") appId: String,
                 @Query("app_code") appCode: String,
                 @Query("query") query: String,
                 @Query("beginHighlight") beginHighlight: String,
                 @Query("endHighlight") endHighlight: String) : Call<Suggestions>
}


interface Login {
    @POST("login/")
    fun login(@Body user : User): Call<UserAuth>
}

interface GetUserData {
    @GET("users/me")
    fun getUser(@Header("x-access-token") token: String): Call<User>
}

interface CreateUser {
    @POST("users/")
    fun createUser(@Body user: User) : Call<String> }

interface GetPackages {
    @GET("packages/")
    fun getPackages(@Header("x-access-token") token: String) : Call<ArrayList<Packages>>
}