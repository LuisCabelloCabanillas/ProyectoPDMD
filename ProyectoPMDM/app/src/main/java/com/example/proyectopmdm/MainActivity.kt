package com.example.proyectopmdm

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        val botonlog = findViewById<Button>(R.id.BotonLogin)
        val botonin = findViewById<Button>(R.id.BotonCrearCuenta)


        botonlog.setOnClickListener {
            startActivity(Intent(this, Inicio_sesion::class.java))
        }

        botonin.setOnClickListener {
            startActivity(Intent(this, Registrarse::class.java))
        }
        
    }
}