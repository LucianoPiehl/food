package com.example.food.model

data class SingleRecipeDTO(
    val id: Int = 0,
    val title: String = "",
    val image: String = "",
    var ingredients: String = "",
    val extendedIngredients: List<RecipeIngredient>? = emptyList()
){
    // Constructor sin argumentos requerido por Firestore
    constructor() : this(0, "", "", "", emptyList())
}
