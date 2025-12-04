package com.example.proyectopmdm

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectopmdm.models.Receta
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PantallaInicioApp : AppCompatActivity() {

    private lateinit var recyclerRecetas: RecyclerView
    private lateinit var adaptador: AdaptadorRecetas

    private val listaRecetas = mutableListOf<Receta>()


    private val lanzarFormulario = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
            resultado ->
        if (resultado.resultCode == RESULT_OK) {
            val data = resultado.data
            val nombre = data?.getStringExtra("nombre") ?: ""
            val descripcion = data?.getStringExtra("descripcion") ?: ""
            val duracion = data?.getStringExtra("duracion") ?: ""
            val dificultad = data?.getStringExtra("dificultad") ?: ""

            val ingredientes = data?.getStringArrayListExtra("ingredientes") ?: arrayListOf()

            val fotoUriString = data?.getStringExtra("fotoUri")
            val fotoUri = fotoUriString?.let { Uri.parse(it) }


            val recetaNueva = Receta(
                nombre = nombre,
                fotoUri = fotoUri,
                descripcion = descripcion,
                duracion = duracion,
                dificultad = dificultad,
                ingredientes = ingredientes
            )

            adaptador.agregarReceta(recetaNueva)

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pantalla_inicio_app)

        recyclerRecetas = findViewById(R.id.recyclerRecetas)
        recyclerRecetas.layoutManager = LinearLayoutManager(this)
        adaptador = AdaptadorRecetas(listaRecetas, this)
        recyclerRecetas.adapter = adaptador

        val btnAddReceta = findViewById<FloatingActionButton>(R.id.btnAddReceta)
        btnAddReceta.setOnClickListener {
            val intent = Intent(this, Anhadir_receta::class.java)
            lanzarFormulario.launch(intent)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {

            val pos = data?.getIntExtra("posicion", -1) ?: return

            val nombre = data.getStringExtra("nombre")!!
            val descripcion = data.getStringExtra("descripcion")!!
            val duracion = data.getStringExtra("duracion")!!
            val dificultad = data.getStringExtra("dificultad")!!
            val ingredientes = data.getStringArrayListExtra("ingredientes")!!
            val fotoUriString = data.getStringExtra("fotoUri")
            val fotoUri = fotoUriString?.let { Uri.parse(it) }

            val recetaActualizada = Receta(
                nombre,
                fotoUri,
                descripcion,
                duracion,
                dificultad,
                ingredientes
            )

            adaptador.actualizarReceta(pos, recetaActualizada)
        }
    }

}