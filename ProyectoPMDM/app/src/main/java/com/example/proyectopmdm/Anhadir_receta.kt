package com.example.proyectopmdm

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Anhadir_receta : AppCompatActivity() {
    private lateinit var imgReceta: ImageView
    private var fotoSeleccionadaUri: Uri? = null

    private val seleccionarImagenLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            fotoSeleccionadaUri = uri
            imgReceta.setImageURI(uri)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_anhadir_receta)

        imgReceta = findViewById(R.id.imgRecetaForm)
        val botonSeleccionar = findViewById<Button>(R.id.btnSeleccionarImagenForm)
        val edtNombre = findViewById<EditText>(R.id.edtNombreForm)
        val edtDescripcion = findViewById<EditText>(R.id.edtDescripcionForm)
        val edtIngredientes = findViewById<EditText>(R.id.edtIngredientesForm)
        val botonAñadir = findViewById<Button>(R.id.btnAñadirForm)

        botonSeleccionar.setOnClickListener {
            seleccionarImagenLauncher.launch("image/*")
        }

        botonAñadir.setOnClickListener {
            val nombre = edtNombre.text.toString()
            val descripcion = edtDescripcion.text.toString()
            val ingredientes = edtIngredientes.text.toString()

            if (nombre.isNotEmpty()) {
                val resultado = Intent()
                resultado.putExtra("nombre", nombre)
                resultado.putExtra("descripcion", descripcion)
                resultado.putExtra("ingredientes", ingredientes)

                fotoSeleccionadaUri?.let { uri ->
                    resultado.putExtra("fotoUri", uri.toString())
                }

                setResult(RESULT_OK, resultado)
                finish()
            } else {
                Toast.makeText(this, "Debes escribir un título", Toast.LENGTH_SHORT).show()
            }
        }
    }


}