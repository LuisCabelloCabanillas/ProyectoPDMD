package com.example.proyectopmdm

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class Detalle_receta : AppCompatActivity() {

    private lateinit var imgReceta: ImageView
    private lateinit var txtTitulo: TextView
    private lateinit var txtDescripcion: TextView
    private lateinit var txtIngredientes: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_descrip_recetas)

        imgReceta = findViewById(R.id.imgRecetaDetalle)
        txtTitulo = findViewById(R.id.txtTituloDetalle)
        txtDescripcion = findViewById(R.id.txtDescripcionDetalle)
        txtIngredientes = findViewById(R.id.txtIngredientesDetalle)

        val nombre = intent.getStringExtra("nombre") ?: ""
        val descripcion = intent.getStringExtra("descripcion") ?: ""
        val ingredientes = intent.getStringArrayListExtra("ingredientes") ?: arrayListOf()
        val fotoUriString = intent.getStringExtra("fotoUri")

        txtTitulo.text = nombre
        txtDescripcion.text = descripcion
        txtIngredientes.text = ingredientes.joinToString(separator = "\n")

        if (!fotoUriString.isNullOrEmpty()) {
            val fotoUri = Uri.parse(fotoUriString)
            Glide.with(this)
                .load(fotoUri)
                .into(imgReceta)
        } else {
            imgReceta.setImageResource(R.drawable.ic_launcher_background) // fallback
        }
    }
}
