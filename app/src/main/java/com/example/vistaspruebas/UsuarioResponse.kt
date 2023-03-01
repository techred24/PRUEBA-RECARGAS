package com.example.vistaspruebas
import com.google.gson.annotations.SerializedName
//import com.fasterxml.jackson.annotation.JsonProperty

/*
data class UsuarioResponse(
    @SerializedName("usuario") var usuario: Usuario,
    @SerializedName("empresa") var empresa: String,
    @SerializedName("success") var success: Boolean,
    @SerializedName("message") var message: String,
    @SerializedName("token") var token: String,
)
*/
//data class DogsResponse(
//    @SerializedName("status") var status: String,
//    @SerializedName("message") var images: List<String>
//) {

//}

data class UsuarioResponse(
    @SerializedName("status") var status: Boolean,
    @SerializedName("data") var data: Data,
    @SerializedName("message") var message: String,
)

data class Data(
    val usuario: Usuario,
    val token: String,
)

data class Usuario(
    val activo: Boolean,
    @SerializedName("_id") val id: String,
    val nombre: String,
    val telefono: String,
    val email: String,
    val usuario: String,
    val fechanac: String,
    val domicilio: String,
    val genero: String,
    val rol: Rol,
    val ruta: Any?,
    val fechaalta: String,
    val usuarioalta: String,
    val permisos: List<Any?>,
    val fechaactu: String,
    val tokenfb: Any?,
    val conDocument: Any?,
    @SerializedName("__v") val v: Long,
)

data class Rol(
    @SerializedName("_id") val id: String,
    val nombre: String,
    val codigo: String,
)
