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
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.vistaspruebas.databinding.ActivityFormularioRecargasBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetAddress
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class FormularioRecargas : AppCompatActivity() {
    private lateinit var binding: ActivityFormularioRecargasBinding
    private lateinit var nfcAdapter: NfcAdapter
    private var sectoresInfo: List<Sectore>? = null
    private var tipoTarjeta: Array<String>? = null
    private lateinit var subsidiosInfo: List<Subsidio>
    var cardIsNew = false
    var nfcWritingMode = false
    var nfcActivado = false
    //var tipoTarjeta: Array<String>? = Array<String>(3) { "" }
    //val x: IntArray = intArrayOf(1, 2, 3)
    //val nums = arrayOf<Int>(1, 2, 3, 4, 5)
    //var arr = IntArray(3) { 10 * (it + 1) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        binding = ActivityFormularioRecargasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CoroutineScope(Dispatchers.IO).launch {
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
        binding.buttonCancelar.setOnClickListener {
            if (!binding.etSaldoAgregar.isEnabled) return@setOnClickListener
            limpiarCampos()

            desabilitaCampos()
            nfcWritingMode = false
            println("TIENE HABILITADO LEER EN PRIMER PLANO: ${nfcActivado}")
            if (nfcActivado) return@setOnClickListener
            println("Habilitando el lector desde boton cancelar")
            enableNFCReader()
        }
        binding.buttonGuardar.setOnClickListener {
            if (!binding.etSaldoAgregar.isEnabled) return@setOnClickListener
            nfcWritingMode = true
            if (nfcActivado) return@setOnClickListener
            println("Habilitando de nuevo el lector")
            enableNFCReader()
        }
    }

    override fun onResume() {
        super.onResume()
        println("EN ONRESUME---------------------------")
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null){
            Toast.makeText(applicationContext, "El dispositivo no soporta NFC", Toast.LENGTH_LONG).show()
            return
        }
            if (!nfcAdapter.isEnabled) {
                MainScope().launch {
                    Toast.makeText(applicationContext, "NFC Apagado", Toast.LENGTH_LONG).show()
                    delay(2500)
                    startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                }
            }
            if (nfcActivado) return
                // Al inicio llega aqui y el campo nombre está vacío. Si la tarjeta es nueva tambien entra en el if porque los campos estan vacios
                if (binding.etSaldoAgregar.text.toString().isNullOrEmpty()) {
                    println("ESTA VACIO EL CAMPO NOMBRE")
                    // cardIsNew es false desde un inicio y activa el nfc. Cuando detecta una tarjeta nueva ya no vuelve a activar el lector NFC
                    //if (cardIsNew) return
                    enableNFCReader()
                }

    }

    private fun getFechaVencimientoSubsidio(diasUtiles: Int): String? {
        if (diasUtiles == 0) {
            return "00000000"
        }
        val timeZone = TimeZone.getTimeZone("UTC")
        val dateFormat: DateFormat = SimpleDateFormat("yyyyMMdd")
        dateFormat.timeZone = timeZone
        val date = Date()
        date.hours = diasUtiles * 24
        return dateFormat.format(date)
    }
    fun enableNFCReader() {
        val launchIntent = Intent(this, this.javaClass)
        //launchIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val filters = arrayOf(IntentFilter(ACTION_TECH_DISCOVERED))
        val techTypes = arrayOf(arrayOf(MifareClassic::class.java.name))
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, techTypes)
        nfcActivado = true
        //nfcAdapter.enableReaderMode()
    }
    fun  disableNFCReader () {
        nfcAdapter.disableForegroundDispatch(this)
        nfcActivado = false
    }
    override fun onPause() {
        super.onPause()
        disableNFCReader()
        //var manager = this.getSystemService(Context.NFC_SERVICE)
    }
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        println("En onNewIntent")
        //if (intent == null) return
            if (ACTION_TECH_DISCOVERED == intent?.action) {
                val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
                if (tag == null) {
                    Toast.makeText(this, "No se pudo leer la tarjeta", Toast.LENGTH_SHORT).show()
                    return
                }
                CardNFC.mifareClassicTag = MifareClassic.get(tag)
                if (nfcWritingMode) {
                    writeMifareClassic()
                    return
                }
                readMifareClassic()
            }
    }
    private fun readMifareClassic() {
        try {
            if (sectoresInfo != null) {
                cardIsNew = CardNFC.cardIsNew()
                //cardIsNew = true
                CardNFC.isCardNew = cardIsNew
                println("LA TARJETA ES NUEVA? $cardIsNew")
                if (cardIsNew) {
                    habilitaCampos()
                    //disableNFCReader()
                } else {
                    //if (sectoresInfo == null) return
                    CardNFC.readUsedCard(binding, applicationContext, sectoresInfo!!, subsidiosInfo)
                    //disableNFCReader()
                }
                disableNFCReader()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR");
        }
    }
    private fun writeMifareClassic() {
        if (cardIsNew) {
            writeNewCard()
            return
        }
        writeUsedCard()
    }
    private fun writeUsedCard () {
        if (sectoresInfo == null) null
        var saldoActualEnTarjeta =  if (CardNFC.read(20, sectoresInfo!!) == null) { return } else CardNFC.read(20, sectoresInfo!!)?.toFloat()
        var saldoAgregar = binding.etSaldoAgregar.text.toString()
        var cortesia = binding.etCortesia.text.toString()
        if (saldoAgregar.isNullOrEmpty()) {
            Toast.makeText(this, "Faltan campos por llenar", Toast.LENGTH_LONG).show()
        }
        if (saldoAgregar.toFloat() < 1) {
            Toast.makeText(this, "El saldo a agregar debe ser igual o mayor a un peso", Toast.LENGTH_LONG).show()
            return
        }
        if (cortesia.isNullOrEmpty()) cortesia = "0"
        if (cortesia.toFloat() < 0) {
            Toast.makeText(this, "Saldo de cortesia invalido", Toast.LENGTH_SHORT).show()
            return
        }
        if (cortesia.toFloat() > 0 && saldoAgregar.toFloat() < 100) {
            Toast.makeText(this, "Saldo de cortesia solo valido desde 100 pesos en adelante", Toast.LENGTH_LONG).show()
            return
        }

        var saldoParaAgregar = saldoAgregar.toFloat()
        var saldoParaCortesia = cortesia.toFloat()
        var saldoTotal = saldoParaAgregar + saldoParaCortesia
        var saldoEscribir = (saldoActualEnTarjeta?.plus(saldoTotal))
        if (saldoEscribir == null) return
        CardNFC.write(20, saldoEscribir.toString(), sectoresInfo!!)
    }
    private fun writeNewCard() {
        if (sectoresInfo != null) {
            //                      31 caracteres
            var datosUsuario = "${binding.etCelular.text.toString()} ${binding.etNombre.text.toString()} ${binding.etApellidoPaterno.text.toString()} ${binding.etApellidoMaterno.text.toString()}"
            println(datosUsuario.length)
            // 0 -15, 16- 31, 32-47
            var infoUser1 = ""
            var infoUser2 = ""
            var infoUser3 = ""

            var saldoAgregar = binding.etSaldoAgregar.text.toString()
            var cortesia = binding.etCortesia.text.toString()
            var folio = binding.etFolio.text.toString()
            var tipoTarjetaSeleccionada = binding.sTipo.selectedItem.toString()

            if (binding.sTipo.selectedItem.toString() == "Adulto") {
                Toast.makeText(applicationContext, "Faltan campos por llenar", Toast.LENGTH_SHORT).show()
                return
            }
            if (binding.etCelular.text.toString().isNullOrEmpty() ||
                binding.etNombre.text.toString().isNullOrEmpty() ||
                binding.etApellidoPaterno.text.toString().isNullOrEmpty() ||
                binding.etApellidoMaterno.text.toString().isNullOrEmpty() ||
                saldoAgregar.isNullOrEmpty()
            ) {
                Toast.makeText(applicationContext, "Faltan campos por llenar", Toast.LENGTH_SHORT).show()
                return
            }

            var localMachine: InetAddress? = null
            try {
                localMachine = InetAddress.getLocalHost()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (localMachine?.hostName?.length == null) {
                Toast.makeText(this, "Algo salio mal al intentar escribir en la tarjeta", Toast.LENGTH_LONG).show()
                desabilitaCampos()
                limpiarCampos()
                nfcWritingMode = false
                return
            }
            var hostname = localMachine?.hostName ?: ""
            println("Local machine: $localMachine")
            if (hostname.length > 16) hostname = hostname.substring(0,16)

            println("Hostname: $hostname")


            val dateFormatISO: DateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'")
            val dateISO = Date()
            var nowISOReduce: String = dateFormatISO.format(dateISO)



            var usuarioPC = System.getProperty("user.name")
            if (usuarioPC.length > 16) usuarioPC = usuarioPC.substring(0, 16)


            var idUsuario = ""
            dataStore.data.map { pref ->
                idUsuario = pref[stringPreferencesKey("idUsuario")] ?: ""
            }



            var claveSubsidioFechaEscribir = ""
            for (i in subsidiosInfo.indices) {
                if (tipoTarjetaSeleccionada == subsidiosInfo[i].nombre) {
                    claveSubsidioFechaEscribir += subsidiosInfo[i].clave
                    claveSubsidioFechaEscribir += getFechaVencimientoSubsidio(subsidiosInfo[i].diasUtiles)
                }
            }
            if (claveSubsidioFechaEscribir.isEmpty()) {
                Toast.makeText(this, "Algo salio mal al intentar escribir en la tarjeta", Toast.LENGTH_LONG).show()
                desabilitaCampos()
                limpiarCampos()
                nfcWritingMode = false
                return
            }


            if (saldoAgregar.toFloat() < 1) {
                Toast.makeText(this, "El saldo a agregar debe ser igual o mayor a un peso", Toast.LENGTH_LONG).show()
                return
            }
            if (cortesia.isNullOrEmpty()) cortesia = "0"
            if (cortesia.toFloat() < 0) {
                Toast.makeText(this, "Saldo de cortesia invalido", Toast.LENGTH_SHORT).show()
                return
            }
            if (cortesia.toFloat() > 0 && saldoAgregar.toFloat() < 100) {
                Toast.makeText(this, "Saldo de cortesia solo valido desde 100 pesos en adelante", Toast.LENGTH_LONG).show()
                return
            }

            var saldoParaAgregar = saldoAgregar.toFloat()
            var saldoParaCortesia = cortesia.toFloat()
            var saldoTotal = saldoParaAgregar + saldoParaCortesia
            var saldoEscribir = saldoTotal.toString()



            CardNFC.reasignaSectores(sectoresInfo!!)


            var seEscribioBien = true
            /*if (datosUsuario.length <= 32) {
                infoUser1 = datosUsuario.substring(0, 16)
                infoUser2 = datosUsuario.substring(16)
                //println("$infoUser1: ${infoUser1.length}")
                //println("$infoUser2: ${infoUser2.length}")
                seEscribioBien = CardNFC.write(12, infoUser1, sectoresInfo!!)
                seEscribioBien = CardNFC.write(13, infoUser2, sectoresInfo!!)
            } else {
                infoUser1 = datosUsuario.substring(0, 16)
                infoUser2 = datosUsuario.substring(16, 32)
                infoUser3 = datosUsuario.substring(32)
                //println("$infoUser1: ${infoUser1.length}")
                //println("$infoUser2: ${infoUser2.length}")
                //println("$infoUser3: ${infoUser3.length}")
                seEscribioBien = CardNFC.write(12, infoUser1, sectoresInfo!!)
                seEscribioBien = CardNFC.write(13, infoUser2, sectoresInfo!!)
                seEscribioBien = CardNFC.write(14, infoUser3, sectoresInfo!!)
            }
            seEscribioBien = CardNFC.write(4, hostname, sectoresInfo!!)
            seEscribioBien = CardNFC.write(5, nowISOReduce, sectoresInfo!!)
            seEscribioBien = CardNFC.write(6, usuarioPC, sectoresInfo!!)
            if (!idUsuario.isNullOrEmpty()) {
                seEscribioBien = CardNFC.write(8, idUsuario.substring(0, 16), sectoresInfo!!)
                seEscribioBien = CardNFC.write(9, idUsuario.substring(16), sectoresInfo!!)
            }
            // Guardar después que se genere la respuesta del servidor
            seEscribioBien = CardNFC.write(10, folio, sectoresInfo!!)
            seEscribioBien = CardNFC.write(16, claveSubsidioFechaEscribir, sectoresInfo!!)
            seEscribioBien = CardNFC.write(20, saldoEscribir, sectoresInfo!!)
            //println("EL TIPO DE TARJETA SELECCIONADA: $tipoTarjetaSeleccionada")*/
            desabilitaCampos()
            limpiarCampos()
            nfcWritingMode = false
            Toast.makeText(applicationContext, "Tarjeta Dada de alta correctamente", Toast.LENGTH_LONG).show()
        }
    }

    private fun solicitaCrearTarjeta() {
        /*val userCredencials: MutableMap<String, String> = mutableMapOf(
            "usuario" to user,
            "contrasena" to password
        )*/
        //CoroutineScope(Dispatchers.IO).launch
        CoroutineScope(Dispatchers.IO).launch {

        }
    }

    private fun habilitaCampos() {
        binding.etNombre.isEnabled = true
        binding.etApellidoPaterno.isEnabled = true
        binding.etApellidoMaterno.isEnabled = true
        binding.etCelular.isEnabled = true
        binding.etSaldoAgregar.isEnabled = true
        binding.etCortesia.isEnabled = true
        binding.etFolio.isEnabled = true
        binding.sTipo.isEnabled = true
    }
    private fun desabilitaCampos() {
        binding.etNombre.isEnabled = false
        binding.etApellidoPaterno.isEnabled = false
        binding.etApellidoMaterno.isEnabled = false
        binding.etCelular.isEnabled = false
        binding.etSaldoAgregar.isEnabled = false
        binding.etCortesia.isEnabled = false
        binding.etFolio.isEnabled = false
        binding.sTipo.isEnabled = false
    }

    private fun limpiarCampos() {
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
    }
}