package com.example.proyectopmdm

import android.net.Uri

data class Receta(
    val titulo: String,
    val fotoUri: Uri?,
    val descripcion: String,
    val ingredientes: String
)