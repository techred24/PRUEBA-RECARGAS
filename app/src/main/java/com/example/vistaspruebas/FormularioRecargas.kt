package com.example.vistaspruebas

//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.ACTION_TECH_DISCOVERED
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.vistaspruebas.databinding.ActivityFormularioRecargasBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FormularioRecargas : AppCompatActivity() {
    private lateinit var binding: ActivityFormularioRecargasBinding
    private lateinit var nfcAdapter: NfcAdapter
    var sectoresInfo: List<Sectore>? = null
    var tipoTarjeta: Array<String>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormularioRecargasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //tipoTarjeta = resources.getStringArray(R.array.tipo_tarjeta)
/*
        val spinner: Spinner = binding.sTipo
        if(spinner != null) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipoTarjeta ?: arrayOf())
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
        }*/

        CoroutineScope(Dispatchers.IO).launch {
            /*dataStore.edit {preferences->
                println(preferences[stringPreferencesKey("token")])
                println("RETRIEVING TOKEN")
            }*/
            try {
                var preferences = dataStore.data.first()
                userToken = preferences[stringPreferencesKey("token")] ?: ""
                val call = getRetrofit().create(APIService::class.java).getTarjetaData()
                println(call.body())
                var respuestaConfiguracionTarjeta = call.body()
                var subsidiosInfo = respuestaConfiguracionTarjeta?.data?.subsidios ?: return@launch
                sectoresInfo = respuestaConfiguracionTarjeta?.data?.sectores
                var nombreSubsidios: MutableList<String> = mutableListOf()
                for (element in subsidiosInfo) {
                    nombreSubsidios.add(element.nombre)
                }
                tipoTarjeta = nombreSubsidios.toTypedArray()
                println("LA RESPUESTA DE LA CONFIGURACION DE LAS TARJETAS")
                runOnUiThread {
                    val spinner: Spinner = binding.sTipo
                    if(spinner != null) {
                        val adapter = ArrayAdapter(this@FormularioRecargas, android.R.layout.simple_spinner_item, tipoTarjeta ?: arrayOf())
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
            } catch (e: Exception) {
                e.printStackTrace()
                println("ERROR LA HACER FETCH PROBABLEMENTE")
            }
        }
    }
    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://demo.bustrack.mx/rec/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(getClient())
            .build()
    }
    private fun getClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HeaderInterceptor())
        .build()

    override fun onStart() {
        super.onStart()
        binding.etNombre.isEnabled = false
        binding.etApellidoPaterno.isEnabled = false
        binding.etApellidoMaterno.isEnabled = false
        binding.etCelular.isEnabled = false
        binding.etSaldoAgregar.isEnabled = false
        binding.etCortesia.isEnabled = false
        binding.etFolio.isEnabled = false
        binding.etID.isEnabled = false
        binding.sTipo.isEnabled = false
        //focusable
        //enabled
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter != null){
            try {
                nfcAdapter.isEnabled
            } catch (e: Exception) {
                println("ERROR")
                return
            }
            if (nfcAdapter.isEnabled) {
                val launchIntent = Intent(this, this.javaClass)
                //launchIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                val pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_CANCEL_CURRENT)
                val filters = arrayOf(IntentFilter(ACTION_TECH_DISCOVERED))
                val techTypes = arrayOf(arrayOf(MifareClassic::class.java.name))
                nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, techTypes)
                //nfcAdapter.enableReaderMode()
            } else {
                MainScope().launch {
                    Toast.makeText(applicationContext, "NFC Apagado", Toast.LENGTH_LONG).show()
                    delay(2500)
                    startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                }
            }
        } else {
            Toast.makeText(applicationContext, "El dispositivo no soporta NFC", Toast.LENGTH_LONG).show()
        }
    }
    override fun onPause() {
        super.onPause()
        nfcAdapter.disableForegroundDispatch(this)
        //var manager = this.getSystemService(Context.NFC_SERVICE)
    }
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        println("En onNewIntent")
        if (intent != null) {
            if (ACTION_TECH_DISCOVERED == intent.action) {
                val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
                readMifareClassic(tag)
            }
        }
    }
    private fun readMifareClassic(tag: Tag?) {
        if (tag == null) return
        CardNFC.mifareClassicTag = MifareClassic.get(tag)
        try {
            if (sectoresInfo != null) {
                var cardIsNew = CardNFC.cardIsNew(sectoresInfo!!)
                println("LA TARJETA ES NUEVA? $cardIsNew")
                if (cardIsNew == true) {

                } else {
                    readUsedCard()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR");
        }
    }
    private fun readUsedCard() {
        val bloques = intArrayOf(12, 13, 14, 20, 10, 0, 16)
        var informacionUsuario = ""
        var nombre = ""
        var saldo = "$"
        var folio = ""
        var ID = ""
        if (sectoresInfo == null) {
            Toast.makeText(applicationContext, "No se pudo leer la tarjeta", Toast.LENGTH_LONG).show()
            return
        }
        for (bloque in bloques) {
            val bloqueLeido = CardNFC.read(bloque, sectoresInfo!!)
            if (bloque == 12 || bloque == 13 || bloque == 14) {
                informacionUsuario += bloqueLeido
            }
            if (bloque == 20) {
                saldo += bloqueLeido
            }
            if (bloque == 10) {
                folio += bloqueLeido
            }
            if (bloque == 0) {
                ID += bloqueLeido
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
        binding.etSaldoDisponible.setText(saldo)
        binding.etFolio.setText(folio)
        binding.etID.setText(ID)
        binding.etSaldoAgregar.isEnabled = true
        binding.etCortesia.isEnabled = true
        //binding.sTipo.isEnabled = true
    }

}