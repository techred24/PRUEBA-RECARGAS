package com.example.vistaspruebas

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
    }

    override fun onResume() {
        super.onResume()
        binding.buttonLogin.setOnClickListener {
            login()
        }
    }
    fun login() {
        Toast.makeText(this, "TESTING", Toast.LENGTH_LONG).show()
    }
}