package com.example.vistaspruebas
import android.content.Context
import android.nfc.tech.MifareClassic
import android.widget.Toast
import com.example.vistaspruebas.databinding.ActivityFormularioRecargasBinding
import kotlin.properties.Delegates

class CardNFC {

    companion object {
        var mifareClassicTag: MifareClassic? = null
        var isCardNew = false

        /*fun tarjetaColocada(fn: () -> Unit): Boolean {
            if ((mifareClassicTag?.isConnected == true)) {
                return true
            }
            fn()
            return false
        }*/
        fun reasignaSectores(sectores: List<Sectore>) {
            // 3, 7, 11, 15, 19, 23, 27, 31, 35, 39, 43, 47, 51, 55, 59, 63
            try {
                if (mifareClassicTag?.isConnected != true) {
                    println("No esta conectado. Esto tiene la variable de la tag: $mifareClassicTag")
                    return
                }
                mifareClassicTag?.connect()
                sectores.forEach { sector ->
                    var nuevaInformacionLlaves = sector.keyA + sector.accessBits + sector.keyB
                    println("Longitud de la cadena: ${nuevaInformacionLlaves.length}")
                    //blockToSector ( bloque )  -> Regresa el sector
                    println("Ultimo bloque del sector ${sector.sector}: ${(mifareClassicTag?.sectorToBlock(sector.sector)
                        ?.plus(3))}  --------- ESTE SERIA EL QUE NECESITO")
                    val len = nuevaInformacionLlaves.length
                    var newAuthKeyData = ByteArray(len / 2)
                    for (i in 0 until len step 2) {
                        newAuthKeyData[i / 2] = (((Character.digit(nuevaInformacionLlaves[i], 16).shl(4))
                                + Character.digit(nuevaInformacionLlaves[i+1], 16)).toByte());
                    }
                    println("La longitud del ByteArray con las llaves: ${newAuthKeyData.size}  ---- Deberia ser de 16")
                    //MifareClassic.KEY_DEFAULT
                    var authenticated = mifareClassicTag?.authenticateSectorWithKeyB(sector.sector, MifareClassic.KEY_DEFAULT)
                    println("Esta autenticado para cambiar la llave en el bloque ${(mifareClassicTag?.sectorToBlock(sector.sector)
                        ?.plus(3))} del sector ${sector.sector}: $authenticated")
                    if (authenticated == true) {
                        mifareClassicTag?.sectorToBlock(sector.sector)?.plus(3)
                            ?.let {bloque->
                                println("Escribiendo las nuevas llaves en el bloque $bloque")
                                //mifareClassicTag?.writeBlock(bloque, newAuthKeyData)
                            }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
            mifareClassicTag?.close()
        }
        fun write(bloque: Int, nuevaInformacion: String, sectoresArgumento: List<Sectore>): Boolean {
            val sector = bloque / 4
            println("Informacion llegando clase NFC escribir: $nuevaInformacion")

            try {
                mifareClassicTag?.connect()
                for (sectorArgumento in sectoresArgumento) {
                    if (sector == sectorArgumento.sector) {
                        val keyString = sectorArgumento.keyB
                        val len = keyString!!.length
                        var authKeyData = ByteArray(len / 2)
                        var data = nuevaInformacion.toByteArray()
                        for (i in 0 until len step 2) {
                            authKeyData[i / 2] = (((Character.digit(keyString[i], 16).shl(4))
                                    + Character.digit(keyString[i+1], 16)).toByte());
                        }
                        //if (isCardNew) authKeyData = MifareClassic.KEY_DEFAULT
                        var authenticated = mifareClassicTag?.authenticateSectorWithKeyB(sector, authKeyData)
                        println("ESTA AUTENTICADO PARA ESCRIBIR? $authenticated")
                        if (authenticated == null) {
                            //mifareClassicTag?.writeBlock(bloque,data)
                        }
                    }
                }
                mifareClassicTag?.close()
            } catch (e: Exception) {
                e.printStackTrace()
                println(e.message)
            }
            return  false
        }
        fun read(bloque: Int, sectoresArgumento: List<Sectore>): String? {
            if (sectoresArgumento == null) return null
            var stringResponse: String? = null
            val sector = bloque / 4
            var block: ByteArray? = null
            try {
                mifareClassicTag?.connect()
                //System.out.println(bloque%4 + " El bloque en el sector del 0 al 3");
                //System.out.println(bloque/4 + " El sector en el que se encuentra el bloque");
                for (sectorArgumento in sectoresArgumento) {
                    if (sector == sectorArgumento.sector) {
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
                        }
                    }
                }
                mifareClassicTag?.close()
            } catch (e: Exception) {
                e.printStackTrace()
                println("ERROR")
            }
            if (bloque == 0) {
                val hexStringBuffer = StringBuffer()
                for (j in block!!.indices) {
                    hexStringBuffer.append(byteToHex(block[j]))
                }
                return hexStringBuffer.toString().substring(0, 8)
            }
            //respuesta?.replace("\u0000.*".toRegex(), "")
            return  stringResponse?.replace(Regex("\u0000.*"), "")
        }
        fun readUsedCard(binding: ActivityFormularioRecargasBinding, applicationContext: Context, sectoresInfo: List<Sectore>, subsidiosInfo:List<Subsidio>) {
            val bloques = intArrayOf(12, 13, 14, 20, 10, 0, 16)
            var informacionUsuario = ""
            var nombre = ""
            if (sectoresInfo == null) {
                Toast.makeText(applicationContext, "No se pudo leer la tarjeta", Toast.LENGTH_LONG).show()
                return
            }
            for (bloque in bloques) {
                val bloqueLeido = read(bloque, sectoresInfo!!)
                if (bloque == 12 || bloque == 13 || bloque == 14) {
                    informacionUsuario += bloqueLeido
                }
                if (bloque == 20) {
                    binding.etSaldoDisponible.setText("$$bloqueLeido")
                }
                if (bloque == 10) {
                    binding.etFolio.setText(bloqueLeido)
                }
                if (bloque == 0) {
                    binding.etID.setText(bloqueLeido)
                }
                if (bloque == 16) {
                    // This (i in 0 until subsidiosInfo.size) is the same as:
                    for (i in subsidiosInfo.indices) {
                        if (bloqueLeido == subsidiosInfo[i].clave) {
                            binding.sTipo.setSelection(i)
                        }
                    }
                }
            }
            for (j in 1 until informacionUsuario.split(" ").size - 2) {
                nombre += " " + informacionUsuario.split(" ")[j]
                nombre = nombre.trim()
            }
            binding.etCelular.setText(informacionUsuario.split(" ")[0])
            binding.etNombre.setText(nombre)
            binding.etApellidoPaterno.setText(informacionUsuario.split(" ")[informacionUsuario.split(" ").size - 2])
            binding.etApellidoMaterno.setText(informacionUsuario.split(" ")[informacionUsuario.split(" ").size - 1])
            binding.etSaldoAgregar.isEnabled = true
            binding.etCortesia.isEnabled = true
        }
        fun cardIsNew(): Boolean {
            var isAuthenticated: Boolean? = null
            try {
                mifareClassicTag?.connect()







/*
                sectores.forEach { sector ->
                    var nuevaInformacionLlaves = sector.keyA + sector.accessBits + sector.keyB
                    println("Longitud de la cadena: ${nuevaInformacionLlaves.length}")
                    //blockToSector ( bloque )  -> Regresa el sector
                    println("Numero de bloques en el sector ${sector.sector}: ${mifareClassicTag?.getBlockCountInSector(sector.sector)}")
                    println("Primer bloque del sector ${sector.sector}: ${mifareClassicTag?.sectorToBlock(sector.sector)}")
                    println("Ultimo bloque del sector ${sector.sector}: ${(mifareClassicTag?.sectorToBlock(sector.sector)
                        ?.plus(3))}  --------- ESTE SERIA EL QUE NECESITO")
                    val len = nuevaInformacionLlaves.length
                    var newAuthKeyData = ByteArray(len / 2)
                    for (i in 0 until len step 2) {
                        newAuthKeyData[i / 2] = (((Character.digit(nuevaInformacionLlaves[i], 16).shl(4))
                                + Character.digit(nuevaInformacionLlaves[i+1], 16)).toByte());
                    }

                    val keyString = sector.keyB
                    val length = keyString!!.length
                    var authKeyData = ByteArray(length / 2)
                    //var data = nuevaInformacion.toByteArray()
                    for (i in 0 until length step 2) {
                        authKeyData[i / 2] = (((Character.digit(keyString[i], 16).shl(4))
                                + Character.digit(keyString[i+1], 16)).toByte());
                    }


                    println("La longitud del ByteArray con las llaves: ${newAuthKeyData.size}  ---- Deberia ser de 16")
                    //MifareClassic.KEY_DEFAULT
                    //var authenticated = mifareClassicTag?.authenticateSectorWithKeyB(sector.sector, MifareClassic.KEY_DEFAULT)
                    var authenticated = mifareClassicTag?.authenticateSectorWithKeyB(sector.sector, authKeyData)

                    println("Esta autenticado para cambiar la llave en el bloque ${(mifareClassicTag?.sectorToBlock(sector.sector)
                        ?.plus(3))} del sector ${sector.sector}: $authenticated")
                    if (authenticated == true) {
                        mifareClassicTag?.sectorToBlock(sector.sector)?.plus(3)
                            ?.let {bloque->
                                println("Escribiendo las nuevas llaves en el bloque $bloque")
                                //mifareClassicTag?.writeBlock(bloque, newAuthKeyData)
                            }
                    }
                }*/











                val DEFAULT_KEY = MifareClassic.KEY_DEFAULT
                isAuthenticated = mifareClassicTag?.authenticateSectorWithKeyA(0, DEFAULT_KEY)
                mifareClassicTag?.close()
            } catch (e:Exception) {
              e.printStackTrace()
            }
            return (isAuthenticated == true)
        }
        fun byteToHex(num: Byte): String? {
            val hexDigits = CharArray(2)
            hexDigits[0] = Character.forDigit((num.toInt().shr(4).and(0xF)), 16)
            hexDigits[1] = Character.forDigit((num.toInt().and(0xF)), 16)
            return String(hexDigits)
        }
    }
}