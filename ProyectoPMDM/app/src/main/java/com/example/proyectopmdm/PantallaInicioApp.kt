package com.example.proyectopmdm

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectopmdm.models.Receta
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class PantallaInicioApp : AppCompatActivity() {

    private lateinit var recyclerRecetas: RecyclerView
    private lateinit var adaptador: AdaptadorRecetas
    private lateinit var etBuscar: TextInputEditText

    private val listaRecetas = mutableListOf<Receta>()
    private val listaRecetasBuscar = mutableListOf<Receta>()

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

            listaRecetas.add(recetaNueva)

            buscarRecetas(etBuscar.text?.toString() ?: "")
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

        etBuscar = findViewById(R.id.etBuscar)

        recyclerRecetas.layoutManager = LinearLayoutManager(this)

        adaptador = AdaptadorRecetas(
            itemsList = listaRecetasBuscar,
            onEditClicked = ::abrirEdicionReceta,
            onDeleteClicked = ::mostrarDialogoBorrado
        )

        recyclerRecetas.adapter = adaptador

        val btnAddReceta = findViewById<FloatingActionButton>(R.id.btnAddReceta)
        btnAddReceta.setOnClickListener {
            val intent = Intent(this, Anhadir_receta::class.java)
            lanzarFormulario.launch(intent)
        }

        setupBuscador()

        cargarRecetasFirebase()
    }


    private fun setupBuscador() {
        etBuscar.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Llamar a la función de buscar recetas con el nombre actual
                buscarRecetas(s.toString())
            }
        })
    }

    private fun buscarRecetas(nombre: String) {

        val nombreMinuscula = nombre.lowercase().trim()

        if (nombreMinuscula.isEmpty()) {
            listaRecetasBuscar.clear()

            listaRecetasBuscar.addAll(listaRecetas)
        } else {

            val reusltados = listaRecetas.filter { receta ->
                receta.nombre.lowercase().contains(nombreMinuscula)
            }

            listaRecetasBuscar.clear()
            listaRecetasBuscar.addAll(reusltados)

        }

        adaptador.notifyDataSetChanged()

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

                listaRecetasBuscar.clear()
                listaRecetasBuscar.addAll(listaRecetas)

                adaptador.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar recetas: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun manejarResultadoEdicion(data: Intent?) {
        //Aseguramos que el Intent no sea nulo antes de continuar
        if (data == null) return

        val pos = data.getIntExtra("posicion", -1)
        if (pos == -1 || pos >= listaRecetasBuscar.size) return

        //Obtener el ID
        val docId = listaRecetasBuscar[pos].id

        //Obtener los datos
        val nombre = data.getStringExtra("nombre") ?: ""
        val instrucciones = data.getStringExtra("instrucciones") ?: ""
        val duracion = data.getIntExtra("duracion", 0)
        val dificultad = data.getStringExtra("dificultad") ?: ""
        val ingredientes = data.getStringArrayListExtra("ingredientes") ?: arrayListOf()
        val fotoBase64String = data.getStringExtra("fotoBase64")

        //Crear la receta actualizada con el ID
        val recetaActualizada = Receta(
            id = docId,
            nombre = nombre,
            instrucciones = instrucciones,
            duracion = duracion,
            dificultad = dificultad,
            ingredientes = ingredientes,
            fotoBase64 = fotoBase64String,
        )

        //Subir a Firestore
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

                    val posReceta = listaRecetas.indexOfFirst{
                        it.id == docId
                    }

                    if (posReceta != -1) {
                        listaRecetas[posReceta] = recetaActualizada
                    }

                    buscarRecetas(etBuscar.text?.toString() ?: "")
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

                listaRecetas.removeAll { it.id == documentId }

                buscarRecetas(etBuscar.text?.toString() ?: "")
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
                // Llama a la función de borrado
                borrarReceta(posicion, documentId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

}