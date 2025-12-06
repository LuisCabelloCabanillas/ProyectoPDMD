package com.example.proyectopmdm.models

import android.net.Uri

data class Receta (

    val nombre: String,
    val fotoUri: Uri?,
    val instrucciones: String,
    val duracion: Int?,
    val dificultad: String,
    val ingredientes: List<String> = listOf()

)