package com.example.proyectopmdm

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import com.google.android.material.textfield.TextInputEditText
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectopmdm.models.Receta

class EditarRecetaActivity : AppCompatActivity() {

    private lateinit var editNombre: TextInputEditText
    private lateinit var editInstrucciones: TextInputEditText
    private lateinit var editIngredientes: TextInputEditText
    private lateinit var btnGuardar: Button

    private var fotoUri: Uri? = null
    private var posicion: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_receta)

        editNombre = findViewById(R.id.editNombre)
        editInstrucciones = findViewById(R.id.editInstrucciones)
        editIngredientes = findViewById(R.id.editIngredientes)
        btnGuardar = findViewById(R.id.btnGuardarRecetaEditada)

        // Recibir datos
        posicion = intent.getIntExtra("posicion", -1)
        val nombre = intent.getStringExtra("nombre") ?: ""
        val instrucciones = intent.getStringExtra("instrucciones") ?: ""
        val duracion = intent.getIntExtra("duracion", 0)
        val dificultad = intent.getStringExtra("dificultad") ?: ""
        val ingredientes = intent.getStringArrayListExtra("ingredientes") ?: arrayListOf()
        val fotoUriString = intent.getStringExtra("fotoUri")

        fotoUri = fotoUriString?.let { Uri.parse(it) }

        // Rellenar formulario
        editNombre.setText(nombre)
        editInstrucciones.setText(instrucciones)
        editIngredientes.setText(ingredientes.joinToString("\n"))

        btnGuardar.setOnClickListener {

            val ingredientesLista = editIngredientes.text.toString()
                .split("\n").map { it.trim() } .filter { it.isNotEmpty() }

            val recetaEditada = Receta(
                nombre = editNombre.text.toString(),
                fotoUri = fotoUri,
                instrucciones = editInstrucciones.text.toString(),
                duracion = duracion,
                dificultad = dificultad,
                ingredientes = ingredientesLista
            )

            val resultIntent = Intent().apply {
                putExtra("posicion", posicion)
                putExtra("nombre", recetaEditada.nombre)
                putExtra("instrucciones", recetaEditada.instrucciones)
                putExtra("duracion", recetaEditada.duracion)
                putExtra("dificultad", recetaEditada.dificultad)
                putStringArrayListExtra("ingredientes", ArrayList(recetaEditada.ingredientes))
                recetaEditada.fotoUri?.let { putExtra("fotoUri", it.toString()) }
            }

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
