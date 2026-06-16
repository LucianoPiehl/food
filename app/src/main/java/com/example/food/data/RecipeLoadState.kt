package com.example.food.data

import com.example.food.model.SingleRecipeDTO

enum class RecipeDataErrorType {
    API_QUOTA_EXCEEDED,
    NO_CONNECTION,
    RECIPE_NOT_FOUND,
    INVALID_API_CONFIGURATION,
    REMOTE_UNAVAILABLE
}

class RecipeDataException(
    val type: RecipeDataErrorType,
    cause: Throwable? = null
) : Exception(type.name, cause)

enum class RecipeUserMessage {
    SHOWING_SAVED_DETAIL_API_LIMIT,
    SHOWING_SAVED_DETAIL_OFFLINE,
    SHOWING_SAVED_DETAIL_SERVICE_ISSUE
}

data class RecipeDetailResult(
    val recipe: SingleRecipeDTO,
    val userMessage: RecipeUserMessage? = null,
    val isFallback: Boolean = false
)
