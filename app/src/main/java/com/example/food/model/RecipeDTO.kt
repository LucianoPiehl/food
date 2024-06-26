package com.example.food.model

data class RecipeDTO(
    val recipes: List<RecipeDetail>
)

data class RecipeDetail(
    val id: Int,
    val title: String,
    val image: String,
    val ingredients: String

)
