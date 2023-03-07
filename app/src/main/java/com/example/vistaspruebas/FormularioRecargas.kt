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
    private var sectoresInfo: List<Sectore>? = null
    private var tipoTarjeta: Array<String>? = null
    private lateinit var subsidiosInfo: List<Subsidio>
    var cardIsNew = false
    //var tipoTarjeta: Array<String>? = Array<String>(3) { "" }
    //val x: IntArray = intArrayOf(1, 2, 3)
    //val nums = arrayOf<Int>(1, 2, 3, 4, 5)
    //var arr = IntArray(3) { 10 * (it + 1) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormularioRecargasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //tipoTarjeta = resources.getStringArray(R.array.tipo_tarjeta)
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
                sectoresInfo = respuestaConfiguracionTarjeta?.data?.sectores
                subsidiosInfo = respuestaConfiguracionTarjeta?.data?.subsidios ?: return@launch
                var nombreSubsidios: MutableList<String> = mutableListOf()
                for (element in subsidiosInfo) {
                    nombreSubsidios.add(element.nombre)
                }
                // myArray.toList()
                tipoTarjeta = nombreSubsidios.toTypedArray()
                if (tipoTarjeta!!.size == null) return@launch
                println("LA RESPUESTA DE LA CONFIGURACION DE LAS TARJETAS")
                runOnUiThread {
                    val spinner: Spinner = binding.sTipo
                    if(spinner != null) {
                        val adapter = ArrayAdapter(this@FormularioRecargas, android.R.layout.simple_spinner_item, tipoTarjeta ?: arrayOf())
                        spinner.adapter = adapter
                        /*spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                //Toast.makeText(this@FormularioRecargas, "${getString(R.string.selected_item)} ${tipoTarjeta!![position]}", Toast.LENGTH_SHORT).show()
                            }
                            override fun onNothingSelected(parent: AdapterView<*>?) {

                            }
                        }*/
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
        println("EN ONRESUME---------------------------")
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter != null){
            try {
                nfcAdapter.isEnabled
            } catch (e: Exception) {
                println("ERROR")
                return
            }
            if (nfcAdapter.isEnabled) {
                if (binding.etNombre.text.toString().isNullOrEmpty()) {
                    println("ESTA VACIO EL CAMPO NOMBRE")
                    if (cardIsNew) return
                    enableNFCReader()
                }
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
        binding.buttonCancelar.setOnClickListener {
            binding.etNombre.setText("")
            binding.etApellidoPaterno.setText("")
            binding.etApellidoMaterno.setText("")
            binding.etCelular.setText("")
            binding.etSaldoDisponible.setText("")
            binding.etSaldoAgregar.setText("")
            binding.etCortesia.setText("")
            binding.etFolio.setText("")
            binding.etID.setText("")
            binding.sTipo.setSelection(0)
            enableNFCReader()
        }
        binding.buttonGuardar.setOnClickListener {
            if (sectoresInfo != null) {
                //                      31 caracteres
                var datosUsuario = "${binding.etCelular.text.toString()} ${binding.etNombre.text.toString()} ${binding.etApellidoPaterno.text.toString()} ${binding.etApellidoMaterno.text.toString()}"
                println(datosUsuario.length)
                // 0 -15, 16- 31, 32-47
                var infoUser1 = ""
                var infoUser2 = ""
                var infoUser3 = ""
                if (binding.etCelular.text.toString().isNullOrEmpty() || binding.etNombre.text.toString().isNullOrEmpty() || binding.etApellidoPaterno.text.toString().isNullOrEmpty() || binding.etApellidoMaterno.text.toString().isNullOrEmpty()) {
                    Toast.makeText(applicationContext, "Faltan campos", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (datosUsuario.length <= 32) {
                    infoUser1 = datosUsuario.substring(0, 16)
                    infoUser2 = datosUsuario.substring(16)
                    println("$infoUser1: ${infoUser1.length}")
                    println("$infoUser2: ${infoUser2.length}")
                    CardNFC.write(12, infoUser1, sectoresInfo!!, ::muestraMensajeNoHayTarjeta)
                    //CardNFC.write(13, infoUser2, sectoresInfo!!)
                } else {
                    infoUser1 = datosUsuario.substring(0, 16)
                    infoUser2 = datosUsuario.substring(16, 32)
                    infoUser3 = datosUsuario.substring(32)
                    println("$infoUser1: ${infoUser1.length}")
                    println("$infoUser2: ${infoUser2.length}")
                    println("$infoUser3: ${infoUser3.length}")
                    //CardNFC.write(12, infoUser1, sectoresInfo!!)
                    //CardNFC.write(13, infoUser2, sectoresInfo!!)
                    //CardNFC.write(14, infoUser3, sectoresInfo!!)
                }
                //CardNFC.write(12, "1122334455",sectoresInfo!!)
                var saldoAgregar = binding.etSaldoAgregar.text.toString()
                var cortesia = binding.etCortesia.text.toString()
                var folio = binding.etFolio.text.toString()
                var tipoTarjetaSeleccionada = binding.sTipo.selectedItem.toString()
                println("EL TIPO DE TARJETA SELECCIONADA: $tipoTarjetaSeleccionada")
            }
        }
    }
    fun enableNFCReader() {
        val launchIntent = Intent(this, this.javaClass)
        //launchIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val filters = arrayOf(IntentFilter(ACTION_TECH_DISCOVERED))
        val techTypes = arrayOf(arrayOf(MifareClassic::class.java.name))
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, techTypes)
        //nfcAdapter.enableReaderMode()
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
                cardIsNew = CardNFC.cardIsNew()
                cardIsNew = true
                CardNFC.isCardNew = cardIsNew
                println("LA TARJETA ES NUEVA? $cardIsNew")
                if (cardIsNew == true) {
                    binding.etNombre.isEnabled = true
                    binding.etApellidoPaterno.isEnabled = true
                    binding.etApellidoMaterno.isEnabled = true
                    binding.etCelular.isEnabled = true
                    binding.etSaldoAgregar.isEnabled = true
                    binding.etCortesia.isEnabled = true
                    binding.etFolio.isEnabled = true
                    binding.sTipo.isEnabled = true
                } else {
                    if (sectoresInfo == null) return
                    CardNFC.readUsedCard(binding, applicationContext, sectoresInfo!!, subsidiosInfo)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR");
        }
    }
    private fun muestraMensajeNoHayTarjeta() {
        Toast.makeText(applicationContext, "Ponga la tarjeta en el lector", Toast.LENGTH_SHORT).show()
    }
    /*private fun readUsedCard() {
        val bloques = intArrayOf(12, 13, 14, 20, 10, 0, 16)
        var informacionUsuario = ""
        var nombre = ""
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
    }*/

}