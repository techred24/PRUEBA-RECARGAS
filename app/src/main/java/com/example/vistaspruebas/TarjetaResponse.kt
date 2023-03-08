package com.example.vistaspruebas

import com.google.gson.annotations.SerializedName

data class TarjetaResponse(
    @SerializedName("status") var status: Boolean,
    @SerializedName("data") var data: Datos,
    @SerializedName("message") var message: String,
)

data class Datos(
    val saldoMaximo: Long,
    val promocionTarjetasNuevas: Long,
    val margenSubsidio: Long,
    //@JsonProperty("_id")
    val id: String,
    val sectores: List<Sectore>,
    val gratuito: List<Gratuito>,
    //@JsonProperty("__v")
    val v: Int,
    val subsidios: List<Subsidio>,
)

data class Sectore(
    val dataAccess: DataAccess,
    val accessBits: String,
    //@JsonProperty("_id")
    val id: String,
    val sector: Int,
    val nombre: String,
    val keyA: String,
    val keyB: String,
    val keyAold: String,
    val keyBold: String,
)

data class DataAccess(
    val bloc0: Bloc0,
    val bloc1: Bloc1,
    val bloc2: Bloc2,
    val security: Security,
)

data class Bloc0(
    val read: String,
    val write: String,
)

data class Bloc1(
    val read: String,
    val write: String,
)

data class Bloc2(
    val read: String,
    val write: String,
)

data class Security(
    val read: String,
    val write: String,
)

data class Gratuito(
    val conteo: Conteo,
    val boletos: Boletos,
    //@JsonProperty("_id")
    val id: String,
    val empresa: String,
    val sector: Long,
)

data class Conteo(
    val bloque: Long,
    val byteInicial: Long,
    val byteFinal: Long,
)

data class Boletos(
    val bloque: Long,
    val byteInicial: Long,
    val byteFinal: Long,
)

data class Subsidio(
    //@JsonProperty("_id")
    val id: String,
    val clave: String,
    val nombre: String,
    val diasUtiles: Int,
)









/*
data class TarjetaResponse(
    val empresa: String,
    val config: List<Config>,
)

data class Config(
    val promocionTarjetasNuevas: Long,
    val margenSubsidio: Long,
    //@JsonProperty("_id")
    val id: String,
    val sectores: List<Sectore>,
    val gratuito: List<Gratuito>,
    //@JsonProperty("__v")
    val v: Long,
    val subsidios: List<Subsidio>,
)

data class Sectore(
    val dataAccess: DataAccess,
    val accessBits: String,
    //@JsonProperty("_id")
    val id: String,
    val sector: Int,
    val nombre: String,
    val keyA: String,
    val keyB: String,
    val keyAold: String,
    val keyBold: String,
)

data class DataAccess(
    val bloc0: Bloc0,
    val bloc1: Bloc1,
    val bloc2: Bloc2,
    val security: Security,
)

data class Bloc0(
    val read: String,
    val write: String,
)

data class Bloc1(
    val read: String,
    val write: String,
)

data class Bloc2(
    val read: String,
    val write: String,
)

data class Security(
    val read: String,
    val write: String,
)

data class Gratuito(
    val conteo: Conteo,
    val boletos: Boletos,
    //@JsonProperty("_id")
    val id: String,
    val empresa: String,
    val sector: Long,
)

data class Conteo(
    val bloque: Long,
    val byteInicial: Long,
    val byteFinal: Long,
)

data class Boletos(
    val bloque: Long,
    val byteInicial: Long,
    val byteFinal: Long,
)

data class Subsidio(
    //@JsonProperty("_id")
    val id: String,
    val clave: String,
    val nombre: String,
    val diasUtiles: Long,
)
*/