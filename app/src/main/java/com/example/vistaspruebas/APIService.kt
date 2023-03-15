package com.example.vistaspruebas

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
//import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Part

interface APIService {
    //@Multipart
    //@POST("usuario/loginV2/")
    //@FormUrlEncoded

    @Headers("Content-Type:application/json")
    @POST("session/login")
    suspend fun login(@Body credentials: MutableMap<String, String>): Response<UsuarioResponse>
    //suspend fun login(@Field("usuario") usuario:String, @Field("contrasena") contrasena:String): Response<*>
    //suspend fun login(@Part("usuario") usuario:String, @Part("contrasena") contrasena:String): Response<*>  <UsuarioResponse>

    @GET("configuraciontarjetas/pc")
    //@GET("config/tarjeta")
    suspend fun getTarjetaData() : Response<TarjetaResponse>


    @POST("tarjeta")
    suspend fun requestToAddAndChargeCard(@Body cardData: MutableMap<String, String>): Response<TarjetaCreadaResponse>

    @POST("recargasaldo")
    suspend fun requestToChargeCard(@Body cardData: MutableMap<String, String>): Response<*>
}