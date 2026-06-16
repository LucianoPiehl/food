package com.example.food.model

data class ComplexSearchResponse(
    val results: List<SearchRecipeDTO> = emptyList()
)

data class SearchRecipeDTO(
    val id: Int = 0,
    val title: String = "",
    val image: String = "",
    val extendedIngredients: List<RecipeIngredient>? = emptyList(),
    val usedIngredients: List<RecipeIngredient>? = emptyList(),
    val missedIngredients: List<RecipeIngredient>? = emptyList()
)
