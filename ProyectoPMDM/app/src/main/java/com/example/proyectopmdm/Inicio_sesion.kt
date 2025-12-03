package com.example.proyectopmdm

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class Inicio_sesion : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio_sesion)

        auth = FirebaseAuth.getInstance()

        val campoEmail = findViewById<EditText>(R.id.edtCorreo)
        val campoContra = findViewById<EditText>(R.id.edtContra)
        val btnInsesi = findViewById<Button>(R.id.btnInSe)

        btnInsesi.setOnClickListener {
            val email = campoEmail.text.toString().trim()
            val contrasenha = campoContra.text.toString().trim()

            if (validarCorreoYContrasenha(email,contrasenha)){
                iniciarSesion(email,contrasenha)
            }
        }
    }

    private fun validarCorreoYContrasenha(email: String, contrasenha: String): Boolean{
        if (email.isEmpty()){
            Toast.makeText(this, "Ingresa un email", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Ingresa un email válido", Toast.LENGTH_SHORT).show()
            return false
        }
        if (contrasenha.isEmpty()){
            Toast.makeText(this, "Ingresa una contraseña", Toast.LENGTH_SHORT).show()
            return false
        }
        if (contrasenha.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun iniciarSesion(email: String,contrasenha: String){
        auth.signInWithEmailAndPassword(email, contrasenha)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful){
                    Toast.makeText(this, "Inicio de sesión correcto", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, PantallaInicioApp::class.java))
                } else {
                    Toast.makeText(this, "Contraseña o Correo INCORRECTO",
                        Toast.LENGTH_LONG).show()
                }
            }
    }

    
}