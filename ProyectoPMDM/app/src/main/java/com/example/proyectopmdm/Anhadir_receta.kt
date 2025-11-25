package com.example.proyectopmdm

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView



class Anhadir_receta : AppCompatActivity() {

    private lateinit var recyclerRecetas: RecyclerView
    private lateinit var adaptador: AdaptadorRecetas
    private val listaRecetas = mutableListOf<Receta>()
    private val lanzarFormulario = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == RESULT_OK) {
            val data = resultado.data
            val titulo = data?.getStringExtra("titulo") ?: ""
            val descripcion = data?.getStringExtra("descripcion") ?: ""
            val ingredientes = data?.getStringExtra("ingredientes") ?: ""
            val fotoUriString = data?.getStringExtra("fotoUri")
            val fotoUri = fotoUriString?.let { Uri.parse(it) }

            val recetaNueva = Receta(titulo, fotoUri, descripcion, ingredientes)
            adaptador.agregarReceta(recetaNueva)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pantalla_inicio)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        recyclerRecetas = findViewById(R.id.recyclerRecetas)
        recyclerRecetas.layoutManager = LinearLayoutManager(this)
        adaptador = AdaptadorRecetas(listaRecetas)
        recyclerRecetas.adapter = adaptador

        val botonAgregar = findViewById<Button>(R.id.btnAdd)
        botonAgregar.setOnClickListener {
            val intent = Intent(this, Formulario_Anhadir::class.java)
            lanzarFormulario.launch(intent)
        }
    }
}
