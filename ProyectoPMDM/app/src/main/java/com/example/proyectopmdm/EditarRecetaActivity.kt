package com.example.proyectopmdm

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectopmdm.models.Receta
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class EditarRecetaActivity : AppCompatActivity() {

    private lateinit var editNombre: TextInputEditText
    private lateinit var editInstrucciones: TextInputEditText
    private lateinit var editIngredientes: TextInputEditText
    private lateinit var btnGuardar: Button

    private var fotoUri: Uri? = null
    private var duracion: Int = 0
    private var dificultad: String = ""
    private var documentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_receta)

        editNombre = findViewById(R.id.editNombre)
        editInstrucciones = findViewById(R.id.editInstrucciones)
        editIngredientes = findViewById(R.id.editIngredientes)
        btnGuardar = findViewById(R.id.btnGuardarRecetaEditada)

        // Recibir datos
        val nombre = intent.getStringExtra("nombre") ?: ""
        val instrucciones = intent.getStringExtra("instrucciones") ?: ""
        duracion = intent.getIntExtra("duracion", 0)
        dificultad = intent.getStringExtra("dificultad") ?: ""
        val ingredientes = intent.getStringArrayListExtra("ingredientes") ?: arrayListOf()
        val fotoUriString = intent.getStringExtra("fotoUri")

        fotoUri = fotoUriString?.let { Uri.parse(it) }

        // Rellenar formulario
        editNombre.setText(nombre)
        editInstrucciones.setText(instrucciones)
        editIngredientes.setText(ingredientes.joinToString("\n"))

        btnGuardar.setOnClickListener {
            val ingredientesLista = editIngredientes.text.toString()
                .split("\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            val recetaEditada = Receta(
                nombre = editNombre.text.toString(),
                instrucciones = editInstrucciones.text.toString(),
                duracion = duracion,
                dificultad = dificultad,
                ingredientes = ingredientesLista,
                fotoUri = fotoUri
            )

            // Actualizar receta directamente en Firestore
            val db = Firebase.firestore
            db.collection("recetas")
                .whereEqualTo("nombre", nombre)
                .get()
                .addOnSuccessListener { archivos ->
                    if (archivos.isEmpty) {
                        Toast.makeText(this, "Receta no encontrada", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    for ( archivo in archivos){
                        val datos = hashMapOf(
                            "nombre" to recetaEditada.nombre,
                            "instrucciones" to recetaEditada.instrucciones,
                            "duracion" to recetaEditada.duracion,
                            "dificultad" to recetaEditada.dificultad,
                            "ingredientes" to recetaEditada.ingredientes,
                            "fotoUrl" to recetaEditada.fotoUri?.toString()
                        )
                        archivo.reference.set(datos)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Receta actualizada", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                e.printStackTrace()
                                Toast.makeText(this, "Error al actualizar receta", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    Toast.makeText(this, "Error al buscar receta", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
