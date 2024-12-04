package edu.uark.ahnelson.openstreetmap2024.Repository

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface JSONPlaceHolderService {

    @GET("pins")
    suspend fun getPins(): Response<List<Pin>>

    @GET("pins/{id}")
    suspend fun getPin(@Path("id")id:Int): Response<Pin>

    @GET("pins/")
    suspend fun getPinsByUserId(@Query("userId")userId:Int): Response<List<Pin>>

    @GET("users/{email}")
    suspend fun getUser(@Path("email")email:String): Response<User>

    @POST("users")
    fun insertUser(@Body user: User): Call<User>

    @POST("pins")
    fun insertPin(@Body pin: Pin): Call<Pin>

    @PUT("pins")
    fun updatePin(@Body pin:Pin): Call<Pin>

    @DELETE("pins")
    fun deletePin(@Path("id")id:Int): Call<Void>

    @GET("users")
    fun getUsers(): Response<List<User>>

}