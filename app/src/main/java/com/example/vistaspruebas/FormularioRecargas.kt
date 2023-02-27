package com.example.vistaspruebas

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.ACTION_TECH_DISCOVERED
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.vistaspruebas.databinding.ActivityFormularioRecargasBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.nio.charset.StandardCharsets

class FormularioRecargas : AppCompatActivity() {
    private lateinit var binding: ActivityFormularioRecargasBinding
    private lateinit var nfcAdapter: NfcAdapter
    //private var context: Context? = this

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
            var preferences = dataStore.data.first()
            userToken = preferences[stringPreferencesKey("token")] ?: ""
            val call = getRetrofit().create(APIService::class.java).getTarjetaData()
            println(call.body())
            println("LA RESPUESTA")
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
        println("Actividad reanudada")
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter != null){
            try {
                nfcAdapter.isEnabled
                println("${nfcAdapter.isEnabled} ESTA HABILITADO?")
            } catch (e: Exception) {
                println("HAY UN ERRORRRRRRRRRRRRRRRRRRRRRRRRRR")
            }
            if (nfcAdapter.isEnabled) {
                val launchIntent = Intent(this, this.javaClass)
                //launchIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

                val pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_CANCEL_CURRENT)
                val filters = arrayOf(IntentFilter(ACTION_TECH_DISCOVERED))
                val techTypes = arrayOf(arrayOf(MifareClassic::class.java.name))
                nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, techTypes)

                //nfcAdapter.enableReaderMode()
            }
        } else {
            startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
        }

    }

    override fun onRestart() {
        super.onRestart()
        println("Se reinicio la actividad")
    }
    override fun onPause() {
        super.onPause()
        nfcAdapter.disableForegroundDispatch(this)
        //var manager = this.getSystemService(Context.NFC_SERVICE)
    }

    override fun onDestroy() {
        super.onDestroy()
        println("Actividad destruida")
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
        println("ALCANZO LA FUNCION PARA LEER")
        if (tag == null) return
        var mifareClassicTag: MifareClassic = MifareClassic.get(tag)
        if (mifareClassicTag.isConnected) {
            println("YA ESTA CONECTADA ESTA MADRE")
        } else {
            println("NO ESTA CONECTADA. CONECTANDO")
            mifareClassicTag.connect()
        }


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
        } finally {
            if (mifareClassicTag.isConnected) {
                println("DESCONECTANDO")
                mifareClassicTag.close()
            }
        }
    }

}