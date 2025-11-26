package com.example.proyectopmdm

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Registrarse : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registrarse)

        val botonRegis = findViewById<Button>(R.id.btnInSe)

        botonRegis.setOnClickListener {
            startActivity(Intent(this, PantallaInicioApp::class.java))
        }

    }
}