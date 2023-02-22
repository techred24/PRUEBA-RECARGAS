package com.example.vistaspruebas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.vistaspruebas.databinding.ActivityLeerTarjetasBinding


class LeerTarjeta : AppCompatActivity() {
    private lateinit var binding: ActivityLeerTarjetasBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeerTarjetasBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        binding.buttonLeerTarjeta.setOnClickListener {
            //startActivity(Intent(this, FormularioRecargas::class.java))
            intent = Intent(this, FormularioRecargas::class.java)
            startActivity(intent)
            finish()
        }
    }
}