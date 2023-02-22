package com.example.vistaspruebas

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.ACTION_TECH_DISCOVERED
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.vistaspruebas.databinding.ActivityFormularioRecargasBinding
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

class FormularioRecargas : AppCompatActivity() {
    private lateinit var binding: ActivityFormularioRecargasBinding
    private lateinit var mifare: MifareClassic
    private var nfcAdapter: NfcAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormularioRecargasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        //println(nfcAdapter)
        //println("LA VARIABLE NFC ADAPTER --------------------------------------")
        //println("${nfcAdapter!!.isEnabled} LO QUE CONTIENE EL IS ENABLED")

        //if (nfcAdapter!!.isEnabled) {
        //    Toast.makeText(this, "Listo para escanear", Toast.LENGTH_SHORT).show()
        //}


        val tipoTarjeta = resources.getStringArray(R.array.tipo_tarjeta)

        val spinner: Spinner = binding.sTipo
        if(spinner != null) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipoTarjeta)
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    //Toast.makeText(this@FormularioRecargas, "${getString(R.string.selected_item)} ${tipoTarjeta[position]}", Toast.LENGTH_SHORT).show()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.etNombre.isEnabled = false
        binding.etApellidoPaterno.isEnabled = false
        binding.etApellidoMaterno.isEnabled = false
        //binding.etCelular.isEnabled = false
        //binding.etSaldoAgregar.isEnabled = false
        //binding.etCortesia.isEnabled = false
        binding.etFolio.isEnabled = false
        binding.etID.isEnabled = false
        binding.sTipo.isEnabled = false
        //focusable
        //enabled
    }

    override fun onResume() {
        super.onResume()
        NfcAdapter.getDefaultAdapter(this).let {nfcAdapter ->
            if (!nfcAdapter!!.isEnabled) {
                Toast.makeText(this, "NFC apagado", Toast.LENGTH_SHORT).show()
                return
            }

            val launchIntent = Intent(this, this.javaClass)
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

            val pendingIntent = PendingIntent.getActivity(
                this, 0, launchIntent, PendingIntent.FLAG_CANCEL_CURRENT
            )
            val filters = arrayOf(IntentFilter(ACTION_TECH_DISCOVERED))
            val techTypes = arrayOf(arrayOf(MifareClassic::class.java.name))
            nfcAdapter.enableForegroundDispatch(
                this, pendingIntent, filters, techTypes
            )
        }

    }
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
                val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
                MifareClassic.get(tag)?.let{mifareClassicTag ->
                    mifare = mifareClassicTag
                    mifareClassicTag.connect()

                    try {
                        var keyString = "C9855A4DA3E0";
                        var length = keyString.length;
                        var authKeyData = ByteArray(length / 2);
                        for (i in 0 until length step 2) {
                            authKeyData[i / 2] = (((Character.digit(keyString[i], 16).shl(4))
                                    + Character.digit(keyString[i+1], 16)).toByte());
                        }
                        var authenticated = mifareClassicTag.authenticateSectorWithKeyA(3, authKeyData);
                        println("$authenticated is Authenticated");
                        if (authenticated) {

                            var block = mifareClassicTag.readBlock(12);
                            var stringResponse = String(block, Charsets.US_ASCII);
                            println("$stringResponse LA RESPUESTA DEL BLOQUE EN STRING. EN FORMULARIO RECARGAS");
                        }
                    } catch (e: Exception) {
                        println(e.message)
                        println(e.stackTrace.toString())
                        println("ERROOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOR");
                    }
                    //mifareClassicTag.close()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mifare.close()
    }

    @SuppressLint("NewApi")
    private fun readMifareClassic(tag: Tag) {
        println("ALCANZO LA FUNCION PARA LEER")
        var mifareClassicTag: MifareClassic = MifareClassic.get(tag)
        mifareClassicTag.connect()

        try {
            var keyString = "C9855A4DA3E0";
            var length = keyString.length;
            var authKeyData = ByteArray(length / 2);
            for (i in 0 until length step 2) {
                //println("$i EL ITERADOR");
                authKeyData[i / 2] = (((Character.digit(keyString[i], 16).shl(4))
                        + Character.digit(keyString[i+1], 16)).toByte());
                //println("AQUI ABAJO. SIN ERROR HASTA EL FINAL DE LA ITERACION");
            }
            var authenticated = mifareClassicTag.authenticateSectorWithKeyA(3, authKeyData);
            println("$authenticated is Authenticated");
            if (authenticated) {
                //println("HEREEEEEEEEEEEEEEEEE INSIDE");
                var block = mifareClassicTag.readBlock(12);
                var stringResponse = String(block, StandardCharsets.US_ASCII);
                println("$stringResponse LA RESPUESTA DEL BLOQUE EN STRING. EN FORMULARIO RECARGAS");
            }
        } catch (e: Exception) {
            println(e.message)
            println(e.stackTrace.toString())
            println("ERROOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOR");
        }
        mifareClassicTag.close()
    }

}