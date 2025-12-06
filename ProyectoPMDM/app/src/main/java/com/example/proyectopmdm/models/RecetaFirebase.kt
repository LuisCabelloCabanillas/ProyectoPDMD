package com.example.proyectopmdm.models

data class RecetaFirebase(
    val nombre: String = "",
    val fotoUrl: String? = null,
    val instrucciones: String = "",
    val duracion: Int? = 0,
    val dificultad: String = "",
    val ingredientes: List<String> = listOf()
)
