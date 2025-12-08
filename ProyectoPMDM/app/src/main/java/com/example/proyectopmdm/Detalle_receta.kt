package com.example.proyectopmdm

import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.lang.IllegalArgumentException

class Detalle_receta : AppCompatActivity() {

    // Vistas inicializadas
    private lateinit var imgReceta: ImageView
    private lateinit var tvNombre: TextView
    private lateinit var tvDuracion: TextView
    private lateinit var tvDificultad: TextView
    private lateinit var tvIngredientes: TextView
    private lateinit var tvInstrucciones: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_receta)

        imgReceta = findViewById(R.id.imgDetalleReceta)
        tvNombre = findViewById(R.id.tvDetalleNombre)
        tvDuracion = findViewById(R.id.tvDetalleDuracion)
        tvDificultad = findViewById(R.id.tvDetalleDificultad)
        tvIngredientes = findViewById(R.id.tvDetalleIngredientes)
        tvInstrucciones = findViewById(R.id.tvDetalleInstrucciones)

        //Obtener datos del Intent
        val intent = intent

        val nombre = intent.getStringExtra("nombre") ?: "Receta sin nombre"
        val instrucciones = intent.getStringExtra("instrucciones") ?: "Instrucciones no disponibles."
        val duracion = intent.getIntExtra("duracion", 0)
        val dificultad = intent.getStringExtra("dificultad") ?: "N/A"
        val ingredientesList = intent.getStringArrayListExtra("ingredientes") ?: arrayListOf()
        val fotoBase64String = intent.getStringExtra("fotoBase64")

        //Asignar Datos a la Interfaz
        tvNombre.text = nombre
        tvInstrucciones.text = instrucciones

        //Asignar Duración y Dificultad
        tvDuracion.text = "$duracion min"
        tvDificultad.text = dificultad

        //Formatear la lista de ingredientes (añadiendo viñetas)
        tvIngredientes.text = ingredientesList.joinToString("\n") { "• $it" }


        //Cargar Imagen Base64
        if (!fotoBase64String.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(fotoBase64String, Base64.DEFAULT)

                Glide.with(this)
                    .load(imageBytes) // Carga el array de bytes
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(imgReceta)
            } catch (e: IllegalArgumentException) {
                imgReceta.setImageResource(R.drawable.image_placeholder_bg)
            }
        } else {
            //Placeholder si no hay imagen
            imgReceta.setImageResource(R.drawable.image_placeholder_bg)
        }

        //Configurar la Toolbar para el botón de regreso (opcional)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
    }

    //Función para manejar el clic en el botón de regreso de la Toolbar
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}