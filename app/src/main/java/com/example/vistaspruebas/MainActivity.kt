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

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "USER_TOKEN")

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
            login()
        }
    }
    fun login() {
        var user: String = binding.etUser.text.toString()
        var password: String = binding.etPassword.text.toString()
        Toast.makeText(this, "user:$user, passaword: $password", Toast.LENGTH_LONG).show()

        CoroutineScope(Dispatchers.IO).launch {
            val call = getRetrofit().create(APIService::class.java).login(user, password)
            if (call.isSuccessful) {
                //var response: UsuarioResponse? = null
                //runOnUiThread {
                    //println(call.body())
                    //println(call.body()?.token)
                    //response = call.body()
                    saveToken(call.body()?.token)
                    println("IMPRIMIENDO EL TOKEN")
                //}
            }
        }
        intent = Intent(this, RecargasFormulario::class.java)
        startActivity(intent)
        finish()
    }

    private suspend fun saveToken(token: String?) {
        dataStore.edit {preferences ->
            preferences[stringPreferencesKey("token")] = token ?: ""
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
}