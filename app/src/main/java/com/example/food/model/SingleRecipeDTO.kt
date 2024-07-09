package com.example.food.model

data class SingleRecipeDTO(
    val id: Int,
    val title: String,
    val image: String,
    var ingredients: String
){
    // Constructor sin argumentos requerido por Firestore
    constructor() : this(0, "", "", "")
}