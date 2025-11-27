package com.example.proyectopmdm.models

import android.net.Uri

data class Receta (

    val nombre: String,
    val fotoUri: Uri?,
    val descripcion: String,
    val ingredientes: String

)