package com.example.proyectopmdm

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectopmdm.models.Receta
import com.example.proyectopmdm.models.RecetaFirebase
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.io.File


class Anhadir_receta : AppCompatActivity() {
    private lateinit var imgReceta: ImageView
    private var fotoSeleccionadaUri: Uri? = null
    private val listaIngredientes = mutableListOf<String>()

    private val storage = Firebase.storage

    private val seleccionarImagenLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Guardar la imagen en almacenamiento interno
            val savedUri = guardarImagenInterna(uri)
            fotoSeleccionadaUri = savedUri
            imgReceta.setImageURI(savedUri)
        }
    }
    private fun guardarImagenInterna(uri: Uri): Uri? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(filesDir, "receta_${System.currentTimeMillis()}.jpg")
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
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
        val edtInstrucciones = findViewById<EditText>(R.id.edtInstruccionesForm)
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
            val instrucciones = edtInstrucciones.text.toString()
            val duracionStr = edtDuracion.text.toString()
            val dificultad = spnDificultad.text.toString()

            if (nombre.isEmpty()) {
                Toast.makeText(this, "El nombre de la receta es obligatorio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (duracionStr.isEmpty()) {
                Toast.makeText(this, "La duracion de la receta es obligatoria", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val duracion = duracionStr.toIntOrNull()

            if (duracion == null) {
                Toast.makeText(this, "La duración debe de ser un número válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val recetaLocal = Receta (
                nombre = nombre,
                instrucciones = instrucciones,
                duracion = duracion,
                dificultad = dificultad,
                ingredientes = listaIngredientes,
                fotoUri = fotoSeleccionadaUri
            )

            subirRecetaAFirebase(recetaLocal)

        }
    }

    private fun subirRecetaAFirebase(recetaLocal: Receta) {

        Toast.makeText(this, "Guardando Receta...", Toast.LENGTH_SHORT).show()

        if (recetaLocal.fotoUri != null) {
            val storageRef = storage.reference.child("recetas_fotos/${recetaLocal.nombre}_${System.currentTimeMillis()}")

            storageRef.putFile(recetaLocal.fotoUri).addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    val fotoUrl = uri.toString()

                    guardarDocumentoReceta(recetaLocal, fotoUrl)
                }
            } .addOnFailureListener {
                exception ->
                Toast.makeText(this, "Error al subir la imagen : ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            guardarDocumentoReceta(recetaLocal, null)
        }
    }

    private fun guardarDocumentoReceta(recetaLocal: Receta, fotoUrl: String?) {
        val bd = FirebaseFirestore.getInstance()

        val recetaFinal = mapOf(
        "nombre" to recetaLocal.nombre,
        "instrucciones" to recetaLocal.instrucciones,
        "duracion" to recetaLocal.duracion,
        "dificultad" to recetaLocal.dificultad,
        "ingredientes" to recetaLocal.ingredientes,
        "fotoUrl" to fotoUrl
        )

        bd.collection("recetas")
            .add(recetaFinal)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Receta '${recetaLocal.nombre}' guardada con éxito", Toast.LENGTH_LONG).show()

                val intentResultado = Intent().apply {
                    putExtra("nombre", recetaLocal.nombre)
                    putExtra("instrucciones", recetaLocal.instrucciones)
                    putExtra("duracion", recetaLocal.duracion)
                    putExtra("dificultad", recetaLocal.dificultad)
                    putStringArrayListExtra("ingredientes", ArrayList(recetaLocal.ingredientes))
                    putExtra("fotoUri", fotoUrl)
                    putExtra("documentId", documentReference.id)

                }
                setResult(Activity.RESULT_OK, intentResultado)
                finish()
            }
            .addOnFailureListener { exception -> Toast.makeText(this, "Error al guardar: ${exception.message}",
                Toast.LENGTH_LONG).show() }
    }

}