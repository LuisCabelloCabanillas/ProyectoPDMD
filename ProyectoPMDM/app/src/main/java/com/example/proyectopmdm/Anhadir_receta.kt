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
import com.google.android.gms.common.internal.Objects
import kotlin.time.Duration

class Anhadir_receta : AppCompatActivity() {
    private lateinit var imgReceta: ImageView
    private var fotoSeleccionadaUri: Uri? = null

    private val listaIngredientes = mutableListOf<String>()

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
        val edtDuracion = findViewById<EditText>(R.id.edtDuracionForm)
        val edtIngrediente = findViewById<EditText>(R.id.etIngredienteForm)
        val btnAgregarIng = findViewById<Button>(R.id.btnAgregarIngrediente)
        val layoutIngredientes = findViewById<LinearLayout>(R.id.layoutIngredientes)
        val spnDificultad = findViewById<AutoCompleteTextView>(R.id.spnDificultad)
        val botonAñadir = findViewById<Button>(R.id.btnAñadirForm)

        botonSeleccionar.setOnClickListener {
            seleccionarImagenLauncher.launch("image/*")
        }

        val opciones = listOf("Fácil", "Media", "Alta")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opciones)
        spnDificultad.setAdapter(adapter)

        spnDificultad.setOnClickListener {
            spnDificultad.showDropDown()
        }




        btnAgregarIng.setOnClickListener {
            val texto = edtIngrediente.text.toString().trim()

            if (texto.isNotEmpty()) {
                listaIngredientes.add(texto)

                val tv = TextView(this)
                tv.text = "- $texto"
                layoutIngredientes.addView(tv)

                edtIngrediente.setText("")
            }
        }

        botonAñadir.setOnClickListener {
            val nombre = edtNombre.text.toString()
            val descripcion = edtDescripcion.text.toString()
            val duracion = edtDuracion.text.toString()
            val dificultad = spnDificultad.text.toString()

            if (nombre.isEmpty()) {
                Toast.makeText(this, "El nombre de la receta es obligatorio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (duracion.isEmpty()) {
                Toast.makeText(this, "La duracion de la receta es obligatoria", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val resultado = Intent()

            resultado.putExtra("nombre", nombre)
            resultado.putExtra("descripcion", descripcion)
            resultado.putExtra("duracion", duracion)
            resultado.putExtra("dificultad", dificultad)
            resultado.putStringArrayListExtra("ingredientes", ArrayList(listaIngredientes))

            fotoSeleccionadaUri?.let { uri ->
                resultado.putExtra("fotoUri", uri.toString())
            }

            setResult(RESULT_OK, resultado)
            finish()

        }
    }


}