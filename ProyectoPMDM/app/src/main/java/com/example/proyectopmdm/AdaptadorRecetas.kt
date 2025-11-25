package com.example.proyectopmdm
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class AdaptadorRecetas(private val listaRecetas: MutableList<Receta>) :
    RecyclerView.Adapter<AdaptadorRecetas.RecetaViewHolder>() {

    class RecetaViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val imagen = v.findViewById<ImageView>(R.id.imgReceta)
        val titulo = v.findViewById<TextView>(R.id.txtTitulo)
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
        holder.titulo.text = receta.titulo
    }

    override fun getItemCount(): Int = listaRecetas.size

    fun agregarReceta(receta: Receta) {
        listaRecetas.add(receta)
        notifyItemInserted(listaRecetas.size - 1)
    }
}