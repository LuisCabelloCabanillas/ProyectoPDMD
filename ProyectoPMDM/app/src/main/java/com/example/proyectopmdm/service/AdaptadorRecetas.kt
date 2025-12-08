package com.example.proyectopmdm

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.proyectopmdm.models.Receta
import android.util.Base64

typealias OnRecetaAction = (Receta, Int) -> Unit
typealias OnRecetaDelete = (Int, String) -> Unit

class AdaptadorRecetas(private val listaRecetas: MutableList<Receta>,
                       private val onEditClicked: OnRecetaAction,
                       private val onDeleteClicked: OnRecetaDelete) :
    RecyclerView.Adapter<AdaptadorRecetas.RecetaViewHolder>() {

    class RecetaViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val imagen = v.findViewById<ImageView>(R.id.fotoReceta)
        val nombre = v.findViewById<TextView>(R.id.cardNombreReceta)
        val duracion = v.findViewById<TextView>(R.id.cardDuracion)
        val dificultad = v.findViewById<TextView>(R.id.cardDificultad)
        val btnMenu = v.findViewById<ImageView>(R.id.btnMenuReceta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetaViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_receta, parent, false)
        return RecetaViewHolder(vista)
    }

    override fun onBindViewHolder(holder: RecetaViewHolder, position: Int) {
        val receta = listaRecetas[position]

        if (!receta.fotoBase64.isNullOrEmpty()) {
            try {
                // Decodifica el String Base64 a un array de bytes
                val imageBytes = Base64.decode(receta.fotoBase64, Base64.DEFAULT)

                Glide.with(holder.itemView.context)
                    .load(imageBytes) // Carga el array de bytes
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Opcional, para evitar caché de imágenes dinámicas
                    .into(holder.imagen)
            } catch (e: IllegalArgumentException) {
                // Manejar error si el String Base64 es inválido o corrupto
                holder.imagen.setImageResource(R.drawable.image_placeholder_bg) // Cambia esto por un placeholder de error
                e.printStackTrace()
            }
        } else {
            // Mostrar una imagen por defecto si no hay Base64
            holder.imagen.setImageResource(R.drawable.image_placeholder_bg) // Asegúrate de tener un placeholder
        }

        holder.nombre.text = receta.nombre
        holder.duracion.text = "${receta.duracion} min"
        holder.dificultad.text = receta.dificultad

        holder.btnMenu.setOnClickListener {
            val popupMenu = android.widget.PopupMenu(holder.itemView.context, holder.btnMenu)
            popupMenu.menuInflater.inflate(R.menu.menu_receta, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {

                    // BORRAR
                    R.id.btnMenuRecetaEliminar -> {
                        val docId = listaRecetas[position].id
                        if (docId != null) {
                            onDeleteClicked(position, docId)
                        } else {
                            Toast.makeText(holder.itemView.context, "ID no encontrado", Toast.LENGTH_SHORT).show()
                        }
                        true
                    }

                    // EDITAR
                    R.id.btnMenuRecetaEditar -> {
                        onEditClicked(receta, position)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        // Abrir detalles al pulsar tarjeta
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, Detalle_receta::class.java).apply {
                putExtra("nombre", receta.nombre)
                putExtra("instrucciones", receta.instrucciones)
                putExtra("duracion", receta.duracion)
                putExtra("dificultad", receta.dificultad)
                putStringArrayListExtra("ingredientes", ArrayList(receta.ingredientes))
                receta.fotoBase64?.let { base64 -> putExtra("fotoBase64", base64) }
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = listaRecetas.size

    fun actualizarReceta(pos: Int, nuevaReceta: Receta) {
        listaRecetas[pos] = nuevaReceta
        notifyItemChanged(pos)
    }

    fun agregarReceta(receta: Receta) {
        listaRecetas.add(receta)
        notifyItemInserted(listaRecetas.size - 1)
    }

    fun eliminarReceta(pos: Int){
        listaRecetas.removeAt(pos)
        notifyItemRemoved(pos)
        notifyItemRangeChanged(pos, listaRecetas.size)
    }
}
