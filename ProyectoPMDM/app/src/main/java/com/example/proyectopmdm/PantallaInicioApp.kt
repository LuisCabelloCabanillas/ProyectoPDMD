package com.example.proyectopmdm

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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

    //AÑADIR RECETA
    private val lanzarFormulario = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == RESULT_OK) {
            val data = resultado.data
            // Lógica para crear la nueva Receta (correcta)
            val id = data?.getStringExtra("documentId")
            val nombre = data?.getStringExtra("nombre") ?: ""
            val instrucciones = data?.getStringExtra("instrucciones") ?: ""
            val duracion = data?.getIntExtra("duracion", 0)
            val dificultad = data?.getStringExtra("dificultad") ?: ""
            val ingredientes = data?.getStringArrayListExtra("ingredientes") ?: arrayListOf()
            val fotoBase64String = data?.getStringExtra("fotoBase64")
            val fotoBase64 = fotoBase64String?.let { Uri.parse(it) }

            val recetaNueva = Receta(
                id = id,
                nombre = nombre,
                fotoBase64 = fotoBase64String,
                instrucciones = instrucciones,
                duracion = duracion,
                dificultad = dificultad,
                ingredientes = ingredientes
            )

            adaptador.agregarReceta(recetaNueva)
        }
    }

    //EDITAR RECETA
    private val lanzarEdicion = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == RESULT_OK) {
            manejarResultadoEdicion(resultado.data)
        }
    }

    fun abrirEdicionReceta(receta: Receta, posicion: Int){
        val intent = Intent(this, EditarRecetaActivity::class.java).apply {
            putExtra("posicion", posicion)
            putExtra("id", receta.id)

            putExtra("nombre", receta.nombre)
            putExtra("instrucciones", receta.instrucciones)
            receta.duracion?.let { putExtra("duracion", it) } // Solo si no es nulo
            putExtra("dificultad", receta.dificultad)
            putStringArrayListExtra("ingredientes", ArrayList(receta.ingredientes))
            receta.fotoBase64?.let { base64 -> putExtra("fotoBase64", base64)}
        }
        lanzarEdicion.launch(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pantalla_inicio_app)

        recyclerRecetas = findViewById(R.id.recyclerRecetas)
        recyclerRecetas.layoutManager = LinearLayoutManager(this)

        adaptador = AdaptadorRecetas(
            listaRecetas = listaRecetas,
            onEditClicked = ::abrirEdicionReceta,
            onDeleteClicked = ::mostrarDialogoBorrado
        )

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
                        id = docs.id,
                        nombre = docs.getString("nombre") ?: "",
                        instrucciones = docs.getString("instrucciones") ?: "",
                        duracion = docs.getLong("duracion")?.toInt(),
                        dificultad = docs.getString("dificultad") ?: "",
                        ingredientes = docs.get("ingredientes") as? List<String> ?: emptyList(),
                        fotoBase64 = docs.getString("fotoBase64"),
                    )
                    listaRecetas.add(receta)
                }
                adaptador.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar recetas: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun manejarResultadoEdicion(data: Intent?) {
        // Aseguramos que el Intent no sea nulo antes de continuar
        if (data == null) return

        val pos = data.getIntExtra("posicion", -1)
        if (pos == -1 || pos >= listaRecetas.size) return

        // 1. Obtener el ID del modelo ya cargado en la lista (seguro)
        val docId = listaRecetas[pos].id

        // 2. Obtener los datos de forma segura (sin !!)
        val nombre = data.getStringExtra("nombre") ?: ""
        val instrucciones = data.getStringExtra("instrucciones") ?: ""
        //getIntExtra no puede ser nulo, pero la conversión a Int en el modelo sí (duracion: Int?)
        val duracion = data.getIntExtra("duracion", 0)
        val dificultad = data.getStringExtra("dificultad") ?: ""
        val ingredientes = data.getStringArrayListExtra("ingredientes") ?: arrayListOf()
        val fotoBase64String = data.getStringExtra("fotoBase64")

        // 3. Crear la receta actualizada con el ID
        val recetaActualizada = Receta(
            id = docId,
            nombre = nombre,
            instrucciones = instrucciones,
            duracion = duracion,
            dificultad = dificultad,
            ingredientes = ingredientes,
            fotoBase64 = fotoBase64String,
        )

        // 4. Subir a Firestore
        recetaActualizada.id?.let { idActualizada ->
            val datos = hashMapOf<String, Any?>(
                "nombre" to recetaActualizada.nombre,
                "instrucciones" to recetaActualizada.instrucciones,
                "duracion" to recetaActualizada.duracion,
                "dificultad" to recetaActualizada.dificultad,
                "ingredientes" to recetaActualizada.ingredientes,
                "fotoBase64" to recetaActualizada.fotoBase64
            )
            bd.collection("recetas").document(idActualizada)
                .set(datos)
                .addOnSuccessListener {
                    Toast.makeText(this, "Receta actualizada", Toast.LENGTH_SHORT).show()
                    adaptador.actualizarReceta(pos, recetaActualizada)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
        }
    }


    fun borrarReceta(posicion: Int, documentId: String) {
        bd.collection("recetas").document(documentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Receta eliminada con éxito", Toast.LENGTH_SHORT).show()
                adaptador.eliminarReceta(posicion)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    fun mostrarDialogoBorrado(posicion: Int, documentId: String) {
        val receta = listaRecetas.getOrNull(posicion)
        val nombreReceta = receta?.nombre ?: "esta receta"

        if (documentId.isEmpty()) {
            Toast.makeText(this, "Error: ID de receta no válido.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Eliminar Receta")
            .setMessage("¿Estás seguro de que deseas eliminar la receta '${nombreReceta}'?")
            .setPositiveButton("Sí, eliminar") { dialog, which ->
                // Llama a la función de borrado de la actividad principal
                borrarReceta(posicion, documentId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }



}