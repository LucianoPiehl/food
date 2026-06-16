package com.example.food.model

data class RecipeDTO(
    val recipes: List<RecipeDetail> = emptyList()
)

data class RecipeDetail(
    val id: Int = 0,
    val title: String = "",
    val image: String = "",
    val extendedIngredients: List<RecipeIngredient>? = emptyList()
)
