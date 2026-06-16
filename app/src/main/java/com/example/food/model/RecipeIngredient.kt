package com.example.food.model

import com.google.gson.annotations.SerializedName

data class RecipeIngredient(
    val id: Int = 0,
    val name: String = "",
    val image: String? = null,
    @SerializedName("originalName")
    val originalName: String = "",
    val original: String = ""
) {
    fun displayText(): String {
        return when {
            original.isNotBlank() -> original
            originalName.isNotBlank() -> originalName
            else -> name
        }
    }

    fun normalizedName(): String {
        return when {
            name.isNotBlank() -> name
            originalName.isNotBlank() -> originalName
            else -> original
        }
    }

    fun imageUrl(): String? {
        val imageName = image?.takeIf { it.isNotBlank() } ?: return null
        return "https://img.spoonacular.com/ingredients_100x100/$imageName"
    }
}
