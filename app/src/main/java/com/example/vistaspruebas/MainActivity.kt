package com.example.vistaspruebas

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.vistaspruebas.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.datastore.preferences.preferencesDataStore
import retrofit2.Response

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "USER_TOKEN")
var userToken = ""

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_main)

        //intent = Intent(this, RecargasFormulario::class.java)
        //startActivity(intent)
        //finish()
    }

    override fun onResume() {
        super.onResume()
        binding.buttonLogin.setOnClickListener {
            //usuario: vendedordorado
            //contrasena:
            login()
        }
    }
    fun login() {
        var user: String = binding.etUser.text.toString()
        var password: String = binding.etPassword.text.toString()
        //Toast.makeText(this, "user:$user, passaword: $password", Toast.LENGTH_LONG).show()
        var call: Response<UsuarioResponse>
        CoroutineScope(Dispatchers.IO).launch {
            try {
                call = getRetrofit().create(APIService::class.java).login(user, password)
                println(call)
                println("Lo que contiene call")
                /*if (call.status) {
                    /*if (call.body()?.success != false) {
                        saveToken(call.body()?.token)
                        accessApp()
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Usuario y/o contraseña incorrecto(s)", Toast.LENGTH_LONG).show()
                        }
                    }*/
                } else {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Ocurrió un eror al intentar ingresar", Toast.LENGTH_LONG).show()
                    }
                }*/
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(applicationContext, "Ocurrió un error al intentar acceder", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun accessApp() {
        intent = Intent(this, LeerTarjeta::class.java)
        startActivity(intent)
        finish()
    }

    private suspend fun saveToken(token: String?) {
        dataStore.edit {preferences ->
            //println(token)
            //println("EL TOKEN QUE SE VA A GUARDAR")
            preferences[stringPreferencesKey("token")] = token ?: ""
        }
    }
    fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://demo.bustrack.mx/rec/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}