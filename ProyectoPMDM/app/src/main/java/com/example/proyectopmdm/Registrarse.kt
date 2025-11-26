package com.example.proyectopmdm

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class Registrarse : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrarse)

        auth = FirebaseAuth.getInstance()

        val emailTexto = findViewById<EditText>(R.id.edtCorreoRe)
        val contraTexto = findViewById<EditText>(R.id.edtContraRe)
        val botonRegis = findViewById<Button>(R.id.btnRegi)

        botonRegis.setOnClickListener {
            val email = emailTexto.text.toString().trim()
            val contrasenha = contraTexto.text.toString().trim()

            if (validarEmailYContrasenha(email,contrasenha)){
                createCuenta(email, contrasenha)
            }

        }

    }

    private fun validarEmailYContrasenha(email: String, contrasenha: String): Boolean{
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

    private fun createCuenta(email: String, contrasenha: String){
        auth.createUserWithEmailAndPassword(email, contrasenha)
            .addOnCompleteListener(this) { task ->
                if ( task.isSuccessful){
                    Toast.makeText(this, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, PantallaInicioApp::class.java))
                } else {
                    if (task.exception is FirebaseAuthUserCollisionException){
                        Toast.makeText(this, "El email ya está en uso", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error al crear la cuenta: ${task.exception?.message}",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}