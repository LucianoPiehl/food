package com.example.food.data

import android.util.Log
import com.example.food.model.Recipe
import com.example.food.model.RecipeDTO
import com.example.food.model.SearchRecipeDTO
import com.example.food.model.SingleRecipeDTO
import com.example.food.util.buildIngredientSummary
import com.example.food.util.mergeIngredients
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RecipeDataSource {

    companion object {
        @Volatile
        private var instance: RecipeDataSource? = null

        fun getInstance(): RecipeDataSource {
            return instance ?: synchronized(this) {
                instance ?: RecipeDataSource().also { instance = it }
            }
        }
    }
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val api: RecipesAPI

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.spoonacular.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(RecipesAPI::class.java)
    }

    fun getRandomRecipe(): Call<RecipeDTO> {
        return api.getRandomRecipeAsync()
    }

    suspend fun getRecipeById(id: Int): SingleRecipeDTO {
        val recipe = suspendCoroutine<SingleRecipeDTO?> { continuation ->
            val recipeCall: Call<SingleRecipeDTO> = api.getRecipesXIDAsync(id)

            recipeCall.enqueue(object : Callback<SingleRecipeDTO> {
                override fun onResponse(
                    call: Call<SingleRecipeDTO>,
                    response: Response<SingleRecipeDTO>
                ) {
                    if (response.isSuccessful) {
                        val recipeDTO = response.body()
                        recipeDTO?.let {
                            if (it.ingredients.isBlank()) {
                                it.ingredients = buildIngredientSummary(it.extendedIngredients.orEmpty())
                            }
                            continuation.resume(it)
                        } ?: run {
                            Log.d("DEBUG", "Recipe detail is null")
                            continuation.resume(null)
                        }
                    } else {
                        val errorCode = response.code()
                        Log.d("DEBUG", "Error: $errorCode")
                        continuation.resumeWithException(
                            buildApiException(errorCode, response.errorBody()?.string())
                        )
                    }
                }

                override fun onFailure(call: Call<SingleRecipeDTO>, t: Throwable) {
                    Log.d("DEBUG", "Error: ${t.message}")
                    continuation.resumeWithException(buildNetworkException(t))
                }
            })
        }

        return recipe ?: throw RecipeDataException(RecipeDataErrorType.REMOTE_UNAVAILABLE)
    }

    suspend fun searchRecipes(query: String): List<Recipe> {
        val titleMatches = api.searchRecipes(query = query).results
        val ingredientQuery = query
            .split(',', ' ')
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(",")

        val ingredientMatches = if (ingredientQuery.isBlank()) {
            emptyList()
        } else {
            api.searchRecipesByIngredients(ingredients = ingredientQuery)
        }

        val mergedResults = linkedMapOf<Int, SearchRecipeDTO>()
        titleMatches.forEach { recipe ->
            mergedResults[recipe.id] = recipe
        }
        ingredientMatches.forEach { recipe ->
            val existing = mergedResults[recipe.id]
            mergedResults[recipe.id] = if (existing == null) {
                recipe
            } else {
                existing.copy(
                    image = existing.image.ifBlank { recipe.image },
                    extendedIngredients = mergeIngredients(
                        existing.extendedIngredients.orEmpty(),
                        recipe.extendedIngredients.orEmpty()
                    ),
                    usedIngredients = mergeIngredients(
                        existing.usedIngredients.orEmpty(),
                        recipe.usedIngredients.orEmpty()
                    ),
                    missedIngredients = mergeIngredients(
                        existing.missedIngredients.orEmpty(),
                        recipe.missedIngredients.orEmpty()
                    )
                )
            }
        }

        return mergedResults.values.map { recipe ->
            val ingredients = mergeIngredients(
                recipe.extendedIngredients.orEmpty(),
                recipe.usedIngredients.orEmpty(),
                recipe.missedIngredients.orEmpty()
            )

            Recipe(
                id = recipe.id,
                title = recipe.title,
                image = recipe.image,
                ingredients = buildIngredientSummary(ingredients)
            )
        }
    }

    suspend fun setFavorite(
        email: String,
        id: Int,
        title: String,
        image: String,
        ingredients: String,
        isFavorite: Boolean
    ): Boolean {
        val ownerId = resolveFavoriteOwnerId(email) ?: return false
        val favoriteRef = db.collection("FavUsers")
            .document(ownerId)
            .collection("Favorites")
            .document(id.toString())

        return try {
            if (isFavorite) {
                favoriteRef.set(
                    hashMapOf(
                        "id" to id,
                        "recipeId" to id,
                        "ownerId" to ownerId,
                        "userEmail" to (auth.currentUser?.email ?: email),
                        "title" to title,
                        "image" to image,
                        "ingredients" to ingredients
                    )
                ).await()
            } else {
                favoriteRef.delete().await()
            }
            true
        } catch (exception: Exception) {
            Log.d("DEBUG Favorites", "Error updating favorite for owner $ownerId: ${exception.message}")
            false
        }
    }

    suspend fun isFavorite(email: String, id: Int): Boolean {
        val ownerId = resolveFavoriteOwnerId(email) ?: return false
        return try {
            db.collection("FavUsers")
                .document(ownerId)
                .collection("Favorites")
                .document(id.toString())
                .get()
                .await()
                .exists()
        } catch (exception: Exception) {
            Log.d("DEBUG Favorites", "Error checking favorite for owner $ownerId: ${exception.message}")
            false
        }
    }

    suspend fun getFavoriteIds(email: String): Set<Int> {
        val ownerId = resolveFavoriteOwnerId(email) ?: return emptySet()

        return try {
            db.collection("FavUsers")
                .document(ownerId)
                .collection("Favorites")
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    document.getLong("recipeId")?.toInt()
                        ?: document.getLong("id")?.toInt()
                        ?: document.id.toIntOrNull()
                }
                .toSet()
        } catch (exception: Exception) {
            Log.d("DEBUG Favorites", "Error loading favorite ids for owner $ownerId: ${exception.message}")
            emptySet()
        }
    }

    suspend fun getFavorites(email: String): List<SingleRecipeDTO> {
        val ownerId = resolveFavoriteOwnerId(email) ?: return emptyList()

        return try {
            db.collection("FavUsers")
                .document(ownerId)
                .collection("Favorites")
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    val recipeId = document.getLong("recipeId")?.toInt()
                        ?: document.getLong("id")?.toInt()
                        ?: document.id.toIntOrNull()
                        ?: return@mapNotNull null

                    val title = document.getString("title").orEmpty()
                    val image = document.getString("image").orEmpty()
                    val ingredients = document.getString("ingredients").orEmpty()

                    if (title.isNotBlank() && image.isNotBlank() && ingredients.isNotBlank()) {
                        SingleRecipeDTO(
                            id = recipeId,
                            title = title,
                            image = image,
                            ingredients = ingredients
                        )
                    } else {
                        try {
                            getRecipeById(recipeId)
                        } catch (exception: Exception) {
                            Log.d(
                                "DEBUG Favorites",
                                "Error loading recipe $recipeId: ${exception.message}"
                            )
                            null
                        }
                    }
                }
                .distinctBy { it.id }
                .sortedBy { it.title.lowercase() }
        } catch (exception: Exception) {
            Log.d("DEBUG Favorites", "Error loading favorites for owner $ownerId: ${exception.message}")
            emptyList()
        }
    }

    suspend fun getFavoriteRecipeSnapshot(email: String, id: Int): SingleRecipeDTO? {
        val ownerId = resolveFavoriteOwnerId(email) ?: return null

        return try {
            val document = db.collection("FavUsers")
                .document(ownerId)
                .collection("Favorites")
                .document(id.toString())
                .get()
                .await()

            if (!document.exists()) {
                return null
            }

            val title = document.getString("title").orEmpty()
            val image = document.getString("image").orEmpty()
            val ingredients = document.getString("ingredients").orEmpty()

            if (title.isBlank() || image.isBlank()) {
                return null
            }

            SingleRecipeDTO(
                id = id,
                title = title,
                image = image,
                ingredients = ingredients
            )
        } catch (exception: Exception) {
            Log.d("DEBUG Favorites", "Error reading cached favorite $id: ${exception.message}")
            null
        }
    }

    private fun resolveFavoriteOwnerId(email: String): String? {
        val currentUser = auth.currentUser
        return when {
            !currentUser?.uid.isNullOrBlank() -> currentUser?.uid
            email.isNotBlank() -> email
            else -> null
        }
    }

    private fun buildApiException(code: Int, rawError: String?): RecipeDataException {
        val normalizedError = rawError.orEmpty().lowercase()

        val type = when {
            code == 402 || "daily points limit" in normalizedError || "quota" in normalizedError ->
                RecipeDataErrorType.API_QUOTA_EXCEEDED
            code == 404 ->
                RecipeDataErrorType.RECIPE_NOT_FOUND
            code == 401 || code == 403 ->
                RecipeDataErrorType.INVALID_API_CONFIGURATION
            else ->
                RecipeDataErrorType.REMOTE_UNAVAILABLE
        }

        return RecipeDataException(type)
    }

    private fun buildNetworkException(throwable: Throwable): RecipeDataException {
        val type = when (throwable) {
            is UnknownHostException, is SocketTimeoutException, is ConnectException ->
                RecipeDataErrorType.NO_CONNECTION
            else ->
                RecipeDataErrorType.REMOTE_UNAVAILABLE
        }

        return RecipeDataException(type, throwable)
    }
}

