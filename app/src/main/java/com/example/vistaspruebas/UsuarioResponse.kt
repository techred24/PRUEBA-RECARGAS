package com.example.vistaspruebas
import com.google.gson.annotations.SerializedName
//import com.fasterxml.jackson.annotation.JsonProperty


data class UsuarioResponse(
    @SerializedName("usuario") var usuario: Usuario,
    @SerializedName("empresa") var empresa: String,
    @SerializedName("success") var success: Boolean,
    @SerializedName("message") var message: String,
    @SerializedName("token") var token: String,
)

data class Usuario(
    //@JsonProperty("_id")
    val id: String,
    val nombre: String,
    val telefono: String,
    val email: String,
    val usuario: String,
    val contrasena: String,
    val fechanac: String,
    val domicilio: String,
    val genero: String,
    val rol: Rol,
    val ruta: Any?,
    val fechaalta: String,
    val usuarioalta: String,
    //@JsonProperty("__v")
    val v: String,
    val permisos: List<Any?>,
    val fechaactu: String,
    val tokenfb: Any?,
    val activo: Boolean,
    val conDocument: Any?,
)

data class Rol(
    val codigo: Long,
)


//data class DogsResponse(
//    @SerializedName("status") var status: String,
//    @SerializedName("message") var images: List<String>
//) {

//}