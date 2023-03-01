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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormularioRecargasBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        CoroutineScope(Dispatchers.IO).launch {
            /*dataStore.edit {preferences->
                println(preferences[stringPreferencesKey("token")])
                println("RETRIEVING TOKEN")
            }*/
            try {
                var preferences = dataStore.data.first()
                userToken = preferences[stringPreferencesKey("token")] ?: ""
                val call = getRetrofit().create(APIService::class.java).getTarjetaData()
                var respuestaConfiguracionTarjeta = call.body()
                sectoresInfo = respuestaConfiguracionTarjeta?.config?.get(0)?.sectores
                //println(respuestaConfiguracionTarjeta?.config?.get(0)?.sectores)
                println("LA RESPUESTA")
            } catch (e: Exception) {
                e.printStackTrace()
                println("ERROR LA HACER FETCH PROBABLEMENTE")
            }
        }
    }
    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://demo.bustrack.mx/apsmg/api/")
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
        val bloquesParaAccesar = intArrayOf(12, 13, 14, 20, 10, 0, 16)
        CardNFC.mifareClassicTag = MifareClassic.get(tag)
        try {
            if (sectoresInfo != null) {
                var cardIsNew = CardNFC.cardIsNew(sectoresInfo!!)
                println("LA TARJETA ES NUEVA? $cardIsNew")
                if (cardIsNew == true) {

                } else {
                    readUsedCard(bloquesParaAccesar)
                }
            }
        } catch (e: Exception) {
            println(e.message)
            println(e.stackTrace.toString())
            println("ERROOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOR");
        }
    }
    private fun readUsedCard(bloques: IntArray) {
        for (bloque in bloques) {
            println("NUMERO DEL BLOQUE A ACCESAR CUANDO LA TARJETA ES NUEVA: $bloque")
        }
    }

}