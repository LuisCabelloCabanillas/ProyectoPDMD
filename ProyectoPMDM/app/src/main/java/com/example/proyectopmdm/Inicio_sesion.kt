package com.example.proyectopmdm

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class Inicio_sesion : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio_sesion)

        val btnRegistrar = findViewById<Button>(R.id.btnRegis)

        btnRegistrar.setOnClickListener {
            val intent = Intent(this, PantallaInicioApp::class.java)
            startActivity(intent)
        }
    }
}
