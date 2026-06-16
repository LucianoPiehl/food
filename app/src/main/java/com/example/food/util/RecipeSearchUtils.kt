package com.example.food.util

import com.example.food.model.RecipeIngredient
import java.text.Normalizer

fun buildIngredientSummary(ingredients: List<RecipeIngredient>): String {
    return ingredients
        .map { it.displayText().trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString(", ")
}

fun mergeIngredients(vararg ingredientLists: List<RecipeIngredient>): List<RecipeIngredient> {
    val merged = linkedMapOf<String, RecipeIngredient>()

    ingredientLists.forEach { ingredients ->
        ingredients.forEach { ingredient ->
            val key = ingredient.normalizedName()
                .ifBlank { ingredient.displayText() }
                .trim()
                .lowercase()

            if (key.isNotBlank() && key !in merged) {
                merged[key] = ingredient
            }
        }
    }

    return merged.values.toList()
}

fun normalizeSearchText(value: String): String {
    return Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
        .lowercase()
        .trim()
}

fun parseIngredientSummary(summary: String): List<RecipeIngredient> {
    return summary
        .split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinctBy { normalizeSearchText(it) }
        .map { ingredientText ->
            RecipeIngredient(
                name = ingredientText,
                original = ingredientText,
                originalName = ingredientText
            )
        }
}
