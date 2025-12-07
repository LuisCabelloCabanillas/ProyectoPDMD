package com.example.proyectopmdm.models

import android.net.Uri

data class Receta (
    val nombre: String,
    val instrucciones: String,
    val duracion: Int?,
    val dificultad: String,
    val ingredientes: List<String> = listOf(),
    val fotoUri: Uri?,
    val documentId: String? = null
)