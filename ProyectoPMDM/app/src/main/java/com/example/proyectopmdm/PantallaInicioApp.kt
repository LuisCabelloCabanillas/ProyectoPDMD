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
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class PantallaInicioApp : AppCompatActivity() {

    private lateinit var recyclerRecetas: RecyclerView
    private lateinit var adaptador: AdaptadorRecetas

    private val listaRecetas = mutableListOf<Receta>()

    private val bd = Firebase.firestore


    private val lanzarFormulario = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
            resultado ->
        if (resultado.resultCode == RESULT_OK) {
            val data = resultado.data

            val id = data?.getStringExtra("documentId")

            val nombre = data?.getStringExtra("nombre") ?: ""
            val instrucciones = data?.getStringExtra("instrucciones") ?: ""
            val duracion = data?.getIntExtra("duracion", 0)
            val dificultad = data?.getStringExtra("dificultad") ?: ""

            val ingredientes = data?.getStringArrayListExtra("ingredientes") ?: arrayListOf()

            val fotoUriString = data?.getStringExtra("fotoUri")
            val fotoUri = fotoUriString?.let { Uri.parse(it) }


            val recetaNueva = Receta(
                id = id,
                nombre = nombre,
                fotoUri = fotoUri,
                instrucciones = instrucciones,
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

        cargarRecetasFirebase()
    }

    private fun cargarRecetasFirebase(){
        bd.collection("recetas").get()
            .addOnSuccessListener { documentos ->
                listaRecetas.clear()

                for (docs in documentos){
                    val receta = Receta(
                        nombre = docs.getString("nombre") ?: "",
                        instrucciones = docs.getString("instrucciones") ?: "",
                        duracion = docs.getLong("duracion")?.toInt(),
                        dificultad = docs.getString("dificultad") ?: "",
                        ingredientes = docs.get("ingredientes") as? List<String> ?: emptyList(),
                        fotoUri = docs.getString("fotoUrl")?.let { Uri.parse(it) },
                        documentId = docs.id
                    )
                    listaRecetas.add(receta)
                }
                adaptador.notifyDataSetChanged()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {

            val pos = data?.getIntExtra("posicion", -1) ?: return
            val nombre = data.getStringExtra("nombre")!!
            val instrucciones = data.getStringExtra("instrucciones")!!
            val duracion = data.getIntExtra("duracion", 0)!!
            val dificultad = data.getStringExtra("dificultad")!!
            val ingredientes = data.getStringArrayListExtra("ingredientes")!!
            val fotoUriString = data.getStringExtra("fotoUri")
            val fotoUri = fotoUriString?.let { Uri.parse(it) }

            val recetaActualizada = Receta(
                nombre = nombre,
                instrucciones = instrucciones,
                duracion = duracion,
                dificultad = dificultad,
                ingredientes = ingredientes,
                fotoUri =fotoUri,
                documentId = listaRecetas[pos].documentId
            )

            recetaActualizada.documentId?.let { docId ->
                val datos = hashMapOf(
                    "nombre" to recetaActualizada.nombre,
                    "instrucciones" to recetaActualizada.instrucciones,
                    "duracion" to recetaActualizada.duracion,
                    "dificultad" to recetaActualizada.dificultad,
                    "ingredientes" to recetaActualizada.ingredientes,
                    "fotoUrl" to recetaActualizada.fotoUri?.toString()
                )
                bd.collection("recetas").document(docId)
                    .set(datos).addOnSuccessListener { adaptador.actualizarReceta(pos, recetaActualizada) }
                    .addOnFailureListener { it.printStackTrace() }
            }


        }
    }

}