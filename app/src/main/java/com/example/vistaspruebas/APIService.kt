package com.example.vistaspruebas

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
//import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Part

interface APIService {
    //@Multipart
    @FormUrlEncoded
    @POST("usuario/loginV2/")
    suspend fun login(@Field("usuario") usuario:String, @Field("contrasena") contrasena:String): Response<UsuarioResponse>
    //suspend fun login(@Part("usuario") usuario:String, @Part("contrasena") contrasena:String): Response<*>

    @GET("config/tarjeta")
    suspend fun getTarjetaData() : Response<TarjetaResponse>
}