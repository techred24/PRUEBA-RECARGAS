package com.example.vistaspruebas
import android.nfc.tech.MifareClassic

class CardNFC {

    companion object {
        var  mifareClassicTag: MifareClassic? = null
        fun read(bloque: Int, sectoresArgumento: List<Sectore>): String? {
            var stringResponse: String? = null
            val sector = bloque / 4
            println("Numero de bloque: $bloque")
            println("Numero de sector en el que se busca: $sector")
            try {
                mifareClassicTag?.connect()
                //System.out.println(bloque%4 + " El bloque en el sector del 0 al 3");
                //System.out.println(bloque/4 + " El sector en el que se encuentra el bloque");
                for (sectorArgumento in sectoresArgumento) {
                    //println(sector)
                    //println(sectorArgumento.sector)
                    //println("EL SECTOR Y EL SECTORARGUMENTO.SECTOR")
                    if (sector == sectorArgumento.sector) {
                        println(sector)
                        println(sectorArgumento.sector)
                        println(sectorArgumento.keyA)
                        println("EN EL IF HUBO COINCIDENCIAS")
                        val keyString = sectorArgumento.keyA
                        val len = keyString!!.length
                        var authKeyData = ByteArray(len / 2)
                        /*var i = 0
                         while (i < len) {
                             authKeyData[i / 2] = ((((keyString[i].digitToIntOrNull(16)
                                 ?: (-1 shl 4)) + keyString[i + 1].digitToIntOrNull(16)!!) ?: -1)).toByte()
                             i += 2
                         }*/
                        for (i in 0 until len step 2) {
                            authKeyData[i / 2] = (((Character.digit(keyString[i], 16).shl(4))
                                    + Character.digit(keyString[i+1], 16)).toByte());
                        }
                        var authenticated = mifareClassicTag?.authenticateSectorWithKeyA(sector, authKeyData)
                        println("Is it Authenticated? $authenticated")
                        if (authenticated == true) {
                            var block = mifareClassicTag?.readBlock(bloque)
                            stringResponse = block?.let { String(it, Charsets.US_ASCII) }
                            mifareClassicTag?.close()
                        }
                    }
                }
            } catch (e: Exception) {
                println(e.message)
                println(e.stackTrace.toString())
                println("ERROOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOR");
            }
            return  stringResponse
        }
        fun cardIsNew(sectoresArgumento: List<Sectore>): Boolean? {
            //var respuesta = read(5, sectoresArgumento)
            var respuesta = read(16, sectoresArgumento)
            respuesta = respuesta?.replace("\u0000.*".toRegex(), "")
            //respuesta.replace(Regex("\u0000.*"), "")
            if (respuesta == null) {
                return null
            }
            if (respuesta.length <= 15) {
                return true
            }
             return false
        }
    }
}