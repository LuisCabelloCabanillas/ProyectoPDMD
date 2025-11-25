package com.example.proyectopmdm

import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import android.widget.ImageView
import android.widget.TextView

class Detalles_Recetas : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalles_recetas)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarDetalle)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val imgReceta = findViewById<ImageView>(R.id.imgRecetaDetalle)
        val txtTitulo = findViewById<TextView>(R.id.txtTituloDetalle)
        val txtDescripcion = findViewById<TextView>(R.id.txtDescripcionDetalle)
        val txtIngredientes = findViewById<TextView>(R.id.txtIngredientesDetalle)

        val titulo = intent.getStringExtra("titulo")
        val descripcion = intent.getStringExtra("descripcion")
        val ingredientes = intent.getStringExtra("ingredientes")
        val fotoUriString = intent.getStringExtra("fotoUri")
        val fotoUri = fotoUriString?.let { Uri.parse(it) }

        txtTitulo.text = titulo
        txtDescripcion.text = descripcion
        txtIngredientes.text = ingredientes
        Glide.with(this).load(fotoUri).into(imgReceta)
    }
}
