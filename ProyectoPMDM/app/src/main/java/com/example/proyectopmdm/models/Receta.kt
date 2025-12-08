package com.example.proyectopmdm.models

import android.net.Uri

data class Receta (
    val id: String? = null,
    val nombre: String,
    val instrucciones: String,
    val duracion: Int?,
    val dificultad: String,
    val ingredientes: List<String> = listOf(),
    val fotoBase64: String?,
)