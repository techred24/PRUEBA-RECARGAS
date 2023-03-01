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
    var usuario: Usuario,
    var token: String,
)

data class Usuario(
    var activo: Boolean,
    @SerializedName("_id") val id: String,
    var nombre: String,
    var telefono: String,
    var email: String,
    var usuario: String,
    var fechanac: String,
    var domicilio: String,
    var genero: String,
    var rol: Rol,
    var ruta: Any?,
    var fechaalta: String,
    var usuarioalta: String,
    var permisos: List<Any?>,
    var fechaactu: String,
    var tokenfb: Any?,
    var conDocument: Any?,
    @SerializedName("__v") val v: Long,
)

data class Rol(
    @SerializedName("_id") val id: String,
    var nombre: String,
    var codigo: String,
)
