package com.example.proyectopmdm
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyectopmdm.models.Receta




class AdaptadorRecetas(private val listaRecetas: MutableList<Receta>) :
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
        Glide.with(holder.itemView.context)
            .load(receta.fotoUri) // Uri? funciona
            .into(holder.imagen)
        holder.nombre.text = receta.nombre

        holder.duracion.text = "${receta.duracion}"
        holder.dificultad.text = receta.dificultad

        holder.btnMenu.setOnClickListener {
            val popupMenuReceta = android.widget.PopupMenu(holder.itemView.context, holder.btnMenu)
            popupMenuReceta.menuInflater.inflate(R.menu.menu_receta, popupMenuReceta.menu)
            popupMenuReceta.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.btnMenuRecetaEditar -> {
                        // Aquí lanza la acción editar, por ejemplo:
                        // Lanza una función que tengas para editar o lanza un intent con la receta
                        // Por ahora solo un Toast
                        android.widget.Toast.makeText(holder.itemView.context,
                            "Editar ${receta.nombre}", android.widget.Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.btnMenuRecetaEliminar -> {
                        // Acción eliminar
                        android.widget.Toast.makeText(holder.itemView.context,
                            "Eliminar ${receta.nombre}", android.widget.Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
            popupMenuReceta.show()
        }
    }

    override fun getItemCount(): Int = listaRecetas.size

    fun agregarReceta(receta: Receta) {
        listaRecetas.add(receta)
        notifyItemInserted(listaRecetas.size - 1)
    }
}