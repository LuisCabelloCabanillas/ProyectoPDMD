package com.example.proyectopmdm

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectopmdm.models.Receta
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import android.util.Base64
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.io.ByteArrayOutputStream

class EditarRecetaActivity : AppCompatActivity() {

    private lateinit var editNombre: TextInputEditText
    private lateinit var editInstrucciones: TextInputEditText
    private lateinit var editIngredientes: TextInputEditText
    private lateinit var editDuracion: TextInputEditText
    private lateinit var editDificultad: AutoCompleteTextView
    private lateinit var imgRecetaEdit: ImageView
    private lateinit var btnSeleccionarImagen: Button
    private lateinit var btnGuardar: Button

    private var fotoNuevaUri: Uri? = null
    private var fotoBase64Actual: String? = null

    private var documentId: String? = null

    private var posicionReceta: Int = -1

    private fun comprimirYConvertirABase64(context: Context, imageUri: Uri): String?{
        val MAX_SIZE_BYTES = 800 * 1024
        val MAX_DIMENSION = 1024
        // ... (Implementación de la función de compresión/conversión de Base64) ...
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

    private val seleccionarImagenLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            fotoNuevaUri = uri // Guardamos el nuevo Uri
            imgRecetaEdit.setImageURI(uri) // Mostramos la nueva imagen
            fotoBase64Actual = null // Marcamos el Base64 actual como obsoleto/pendiente de calcular
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_receta)

        editNombre = findViewById(R.id.editNombre)
        editInstrucciones = findViewById(R.id.editInstrucciones)
        editIngredientes = findViewById(R.id.editIngredientes)
        editDuracion = findViewById(R.id.editDuracion)
        editDificultad = findViewById(R.id.editDificultad)
        imgRecetaEdit = findViewById(R.id.imgRecetaEdit)
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagenEdit)
        btnGuardar = findViewById(R.id.btnGuardarRecetaEditada)


        documentId = intent.getStringExtra("id")
        posicionReceta = intent.getIntExtra("posicion", -1)

        val nombre = intent.getStringExtra("nombre") ?: ""
        val instrucciones = intent.getStringExtra("instrucciones") ?: ""
        val duracion = intent.getIntExtra("duracion", 0)
        val dificultad = intent.getStringExtra("dificultad") ?: ""
        val ingredientes = intent.getStringArrayListExtra("ingredientes") ?: arrayListOf()
        val fotoBase64Intent = intent.getStringExtra("fotoBase64")

        fotoBase64Actual = fotoBase64Intent

        // Rellenar formulario
        editNombre.setText(nombre)
        editInstrucciones.setText(instrucciones)
        editIngredientes.setText(ingredientes.joinToString("\n"))
        editDuracion.setText(duracion.toString())
        editDificultad.setText(dificultad)

        val opciones = listOf("Fácil", "Media", "Alta")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opciones)
        editDificultad.setAdapter(adapter)
        editDificultad.setOnClickListener {
            editDificultad.showDropDown()
        }

        if (!fotoBase64Actual.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(fotoBase64Actual, Base64.DEFAULT)
                Glide.with(this)
                    .load(imageBytes)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(imgRecetaEdit)
            } catch (e: IllegalArgumentException) {
                imgRecetaEdit.setImageResource(R.drawable.image_placeholder_bg) // Placeholder
            }
        } else {
            imgRecetaEdit.setImageResource(R.drawable.image_placeholder_bg) // Placeholder
        }

        btnSeleccionarImagen.setOnClickListener {
            seleccionarImagenLauncher.launch("image/*")
        }

        btnGuardar.setOnClickListener {
            guardarCambios()
        }
    }

    private fun guardarCambios(){
        if (documentId == null) {
            Toast.makeText(this, "Error: ID de receta no encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar y Obtener nuevos valores
        val nuevoNombre = editNombre.text.toString()
        val nuevaInstrucciones = editInstrucciones.text.toString()
        val nuevaDificultad = editDificultad.text.toString()

        val nuevaDuracionStr = editDuracion.text.toString()
        val nuevaDuracion = nuevaDuracionStr.toIntOrNull()

        if (nuevoNombre.isEmpty() || nuevaDuracion == null) {
            Toast.makeText(this, "Nombre y Duración son obligatorios.", Toast.LENGTH_SHORT).show()
            return
        }

        val ingredientesLista = editIngredientes.text.toString()
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        // Procesar Foto Base64 si se seleccionó una nueva
        var base64ParaGuardar = fotoBase64Actual

        if (fotoNuevaUri != null) {
            val nuevoBase64 = comprimirYConvertirABase64(this, fotoNuevaUri!!)
            if (nuevoBase64 == null) {
                return
            }
            base64ParaGuardar = nuevoBase64
        }

        // Preparar datos para Firestore
        val datosActualizados = hashMapOf<String, Any?>(
            "nombre" to nuevoNombre,
            "instrucciones" to nuevaInstrucciones,
            "duracion" to nuevaDuracion,
            "dificultad" to nuevaDificultad,
            "ingredientes" to ingredientesLista,
            "fotoBase64" to (base64ParaGuardar ?: "")
        )

        // Actualizar receta en Firestore
        val db = Firebase.firestore
        db.collection("recetas").document(documentId!!)
            .set(datosActualizados)
            .addOnSuccessListener {
                Toast.makeText(this, "Receta actualizada", Toast.LENGTH_SHORT).show()

                // Devolver resultado a PantallaInicioApp
                val intentResultado = Intent().apply {
                    putExtra("posicion", posicionReceta) // Devolvemos la posición
                    putExtra("nombre", nuevoNombre)
                    putExtra("instrucciones", nuevaInstrucciones)
                    putExtra("duracion", nuevaDuracion)
                    putExtra("dificultad", nuevaDificultad)
                    putStringArrayListExtra("ingredientes", ArrayList(ingredientesLista))
                    putExtra("fotoBase64", base64ParaGuardar)
                }

                setResult(Activity.RESULT_OK, intentResultado)
                finish()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Error al actualizar receta: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
