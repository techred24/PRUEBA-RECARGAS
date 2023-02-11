package com.example.vistaspruebas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.vistaspruebas.databinding.ActivityMainBinding

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
        var user = binding.etUser.text
        var password = binding.etPassword.text
        Toast.makeText(this, "user:$user, passaword: $password", Toast.LENGTH_LONG).show()
        intent = Intent(this, RecargasFormulario::class.java)
        startActivity(intent)
        finish()
    }
}