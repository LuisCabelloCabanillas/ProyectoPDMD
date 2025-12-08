package com.example.proyectopmdm


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
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
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.io.ByteArrayOutputStream


class Anhadir_receta : AppCompatActivity() {
    private lateinit var imgReceta: ImageView
    private var fotoSeleccionadaUri: Uri? = null
    private val listaIngredientes = mutableListOf<String>()

    private val db = Firebase.firestore


    private val seleccionarImagenLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            fotoSeleccionadaUri = uri
            imgReceta.setImageURI(uri)
        }
    }

    private fun comprimirYConvertirABase64(context: Context, imageUri: Uri): String?{
        val MAX_SIZE_BYTES = 800 * 1024
        val MAX_DIMENSION = 1024

        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(context.contentResolver.openInputStream(imageUri), null, options)

            var scaleFactor = 1
            if (options.outHeight > MAX_DIMENSION || options.outWidth > MAX_DIMENSION) {
                val heightRatio = Math.round(options.outHeight.toFloat() / MAX_DIMENSION)
                val widthRatio = Math.round(options.outWidth.toFloat() / MAX_DIMENSION)
                scaleFactor = if (heightRatio > widthRatio) heightRatio else widthRatio
            }

            options.inJustDecodeBounds = false
            options.inSampleSize = scaleFactor

            val inputStream = context.contentResolver.openInputStream(imageUri)
            val scaledBitmap = BitmapFactory.decodeStream(inputStream, null, options) ?: return null

            val outputStream = ByteArrayOutputStream()
            var quality = 70
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

            while (outputStream.toByteArray().size > MAX_SIZE_BYTES && quality > 10) {
                outputStream.reset()
                quality -= 10
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }

            val imageBytes = outputStream.toByteArray()

            if (imageBytes.size > MAX_SIZE_BYTES) {
                Toast.makeText(context, "Imagen demasiado grande (${imageBytes.size / 1024}KB).", Toast.LENGTH_LONG).show()
                return null
            }

            return Base64.encodeToString(imageBytes, Base64.DEFAULT)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al procesar la imagen: ${e.message}", Toast.LENGTH_LONG).show()
            return null
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
                fotoBase64 = null
            )

            subirRecetaAFirebase(recetaLocal)

        }
    }

    private fun subirRecetaAFirebase(recetaLocal: Receta) {

        Toast.makeText(this, "Guardando Receta...", Toast.LENGTH_SHORT).show()

        var fotoBase64: String? = null

        if (fotoSeleccionadaUri != null) {

            fotoBase64 = comprimirYConvertirABase64(this, fotoSeleccionadaUri!!)

            if (fotoBase64 == null) {
                return
            }
        }

        guardarDocumentoReceta(recetaLocal, fotoBase64)
    }


    private fun guardarDocumentoReceta(recetaLocal: Receta, fotoBase64: String?) {

        val recetaFinal = mapOf(
        "nombre" to recetaLocal.nombre,
        "instrucciones" to recetaLocal.instrucciones,
        "duracion" to recetaLocal.duracion,
        "dificultad" to recetaLocal.dificultad,
        "ingredientes" to recetaLocal.ingredientes,
        "fotoBase64" to (fotoBase64 ?: "")
        )

        db.collection("recetas")
            .add(recetaFinal)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Receta '${recetaLocal.nombre}' guardada con éxito", Toast.LENGTH_LONG).show()

                val intentResultado = Intent().apply {
                    putExtra("nombre", recetaLocal.nombre)
                    putExtra("instrucciones", recetaLocal.instrucciones)
                    putExtra("duracion", recetaLocal.duracion)
                    putExtra("dificultad", recetaLocal.dificultad)
                    putStringArrayListExtra("ingredientes", ArrayList(recetaLocal.ingredientes))
                    putExtra("fotoBase64", fotoBase64)
                    putExtra("documentId", documentReference.id)

                }
                setResult(Activity.RESULT_OK, intentResultado)
                finish()
            }
            .addOnFailureListener { exception -> Toast.makeText(this, "Error al guardar: ${exception.message}",
                Toast.LENGTH_LONG).show() }
    }

}