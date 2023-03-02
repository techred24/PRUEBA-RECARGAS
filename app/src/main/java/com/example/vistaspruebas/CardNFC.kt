package com.example.vistaspruebas
import android.nfc.tech.MifareClassic

class CardNFC {

    companion object {
        var  mifareClassicTag: MifareClassic? = null
        fun read(bloque: Int, sectoresArgumento: List<Sectore>): String? {
            if (sectoresArgumento == null) return null
            var stringResponse: String? = null
            val sector = bloque / 4
            var block: ByteArray? = null
            //println("Numero de bloque: $bloque")
            //println("Numero de sector en el que se busca: $sector")
            try {
                mifareClassicTag?.connect()
                //System.out.println(bloque%4 + " El bloque en el sector del 0 al 3");
                //System.out.println(bloque/4 + " El sector en el que se encuentra el bloque");
                for (sectorArgumento in sectoresArgumento) {
                    if (sector == sectorArgumento.sector) {
                        /*println(sector)
                        println(sectorArgumento.sector)
                        println(sectorArgumento.keyA)
                        println("EN EL IF HUBO COINCIDENCIAS")*/
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
                        //println("Is it Authenticated? $authenticated")
                        if (authenticated == true) {
                            block = mifareClassicTag?.readBlock(bloque)
                            stringResponse = block?.let { String(it, Charsets.US_ASCII) }
                            mifareClassicTag?.close()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("ERROR")
            }
            if (bloque == 0) {
                val hexStringBuffer = StringBuffer()
                for (j in block!!.indices) {
                    hexStringBuffer.append(byteToHex(block[j]))
                }
                /*var hexadecimalString = hexStringBuffer.toString().substring(0, 8).uppercase()
                var hexadecimalStringCopy = hexStringBuffer.toString().substring(0, 8).uppercase()
                var UID: MutableList<String> = mutableListOf()
                var ID = hexStringBuffer.toString().substring(0, 8).uppercase().split("")
                for (i in 0..ID.size) {
                    println(" El tamanio del ID: ${ID.size}")
                    println("El numero del rango: $i")
                    if (i % 2 != 0) {
                        println("Dentro del if. Es impar")
                        hexadecimalString = hexadecimalStringCopy.replaceRange(i, i+1, "${hexadecimalString[i]}:")
                       //if (i == 7)  null else UID.add("${ID[i]}:")
                    }
                }
                return hexadecimalString*/
                //return UID.joinToString("")
                return hexStringBuffer.toString().substring(0, 8).uppercase()
            }
            return  stringResponse?.replace(Regex("\u0000.*"), "")
        }
        fun cardIsNew(sectoresArgumento: List<Sectore>): Boolean? {
            var respuesta = read(5, sectoresArgumento)
            respuesta = respuesta?.replace("\u0000.*".toRegex(), "")
            if (respuesta == null) {
                return null
            }
            if (respuesta.length <= 15) {
                return true
            }
             return false
        }
        fun byteToHex(num: Byte): String? {
            val hexDigits = CharArray(2)
            hexDigits[0] = Character.forDigit((num.toInt().shr(4).and(0xF)), 16)
            hexDigits[1] = Character.forDigit((num.toInt().and(0xF)), 16)
            return String(hexDigits)
        }
    }
}