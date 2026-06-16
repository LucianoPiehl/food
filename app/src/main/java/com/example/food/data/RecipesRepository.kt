package com.example.food.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.food.model.Recipe
import com.example.food.model.RecipeDTO
import com.example.food.model.RecipeIngredient
import com.example.food.model.SingleRecipeDTO
import com.example.food.util.buildIngredientSummary
import com.example.food.util.normalizeSearchText
import com.example.food.util.parseIngredientSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipesRepository {
    private val dataSource = RecipeDataSource.getInstance()

    companion object {
        @Volatile
        private var instance: RecipesRepository? = null

        fun getInstance(): RecipesRepository {
            return instance ?: synchronized(this) {
                instance ?: RecipesRepository().also { instance = it }
            }
        }
    }

    private fun getRandomRecipe(): Call<RecipeDTO> {
        return dataSource.getRandomRecipe()
    }

    fun loadRecipes(
        appContext: Context,
        loadedRecipes: MutableList<Recipe>,
        recipesLiveData: MutableLiveData<List<Recipe>>,
        canPublish: () -> Boolean
    ) {
        val calls = mutableListOf<Call<RecipeDTO>>()
        repeat(6) {
            calls.add(getRandomRecipe())
        }

        calls.forEach { call ->
            call.enqueue(object : Callback<RecipeDTO> {
                override fun onResponse(call: Call<RecipeDTO>, response: Response<RecipeDTO>) {
                    if (response.isSuccessful) {
                        val recipeDTO = response.body()
                        recipeDTO?.recipes.orEmpty().forEach { recipeDetail ->
                            val newRecipe = Recipe(
                                id = recipeDetail.id,
                                title = recipeDetail.title,
                                image = recipeDetail.image,
                                ingredients = buildIngredientSummary(
                                    recipeDetail.extendedIngredients.orEmpty()
                                )
                            )

                            if (loadedRecipes.none { recipe -> recipe.id == newRecipe.id }) {
                                persistRecipe(appContext, newRecipe)
                                loadedRecipes.add(newRecipe)
                                if (canPublish()) {
                                    recipesLiveData.postValue(loadedRecipes.toList())
                                }
                            }
                        }
                    } else {
                        Log.d("DEBUG_DE_LA_API", "Error en la respuesta del servidor")
                        loadRecipesFromLocalDatabase(
                            appContext,
                            loadedRecipes,
                            recipesLiveData,
                            canPublish
                        )
                    }
                }

                override fun onFailure(call: Call<RecipeDTO>, t: Throwable) {
                    Log.d("DEBUG", t.toString())
                    loadRecipesFromLocalDatabase(
                        appContext,
                        loadedRecipes,
                        recipesLiveData,
                        canPublish
                    )
                }
            })
        }
    }

    private fun persistRecipe(appContext: Context, recipe: Recipe) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(appContext)
                db.recipeDao().insertRecipes(recipe)
            }
        } catch (exception: Exception) {
            Log.e("RecipesRepository", "Error al guardar recetas localmente", exception)
        }
    }

    private fun loadRecipesFromLocalDatabase(
        appContext: Context,
        loadedRecipes: MutableList<Recipe>,
        recipesLiveData: MutableLiveData<List<Recipe>>,
        canPublish: () -> Boolean
    ) {
        try {
            CoroutineScope(Dispatchers.Main).launch {
                val recipes = withContext(Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(appContext)
                    db.recipeDao().getAllRecipesSync()
                }

                loadedRecipes.clear()
                loadedRecipes.addAll(recipes)
                if (canPublish()) {
                    recipesLiveData.postValue(loadedRecipes.toList())
                }
            }
        } catch (exception: Exception) {
            Log.e("RecipesRepository", "Error al recuperar recetas locales", exception)
        }
    }

    suspend fun getRecipeDetail(
        appContext: Context,
        email: String,
        id: Int
    ): RecipeDetailResult {
        return try {
            RecipeDetailResult(recipe = dataSource.getRecipeById(id))
        } catch (exception: RecipeDataException) {
            val fallbackRecipe = getCachedRecipe(appContext, id)
                ?: dataSource.getFavoriteRecipeSnapshot(email, id)?.withParsedIngredients()

            if (fallbackRecipe != null) {
                RecipeDetailResult(
                    recipe = fallbackRecipe,
                    userMessage = resolveDetailFallbackMessage(exception.type),
                    isFallback = true
                )
            } else {
                throw exception
            }
        }
    }

    suspend fun setFavorite(email: String, recipe: Recipe, isFavorite: Boolean): Boolean {
        return dataSource.setFavorite(
            email = email,
            id = recipe.id,
            title = recipe.title,
            image = recipe.image.orEmpty(),
            ingredients = recipe.ingredients,
            isFavorite = isFavorite
        )
    }

    suspend fun setFavorite(email: String, recipe: SingleRecipeDTO, isFavorite: Boolean): Boolean {
        return dataSource.setFavorite(
            email = email,
            id = recipe.id,
            title = recipe.title,
            image = recipe.image,
            ingredients = recipe.ingredients,
            isFavorite = isFavorite
        )
    }

    suspend fun isFavorite(email: String, id: Int): Boolean {
        return dataSource.isFavorite(email, id)
    }

    suspend fun getFavoriteIds(email: String): Set<Int> {
        return dataSource.getFavoriteIds(email)
    }

    suspend fun getFavorites(email: String): List<SingleRecipeDTO> {
        return dataSource.getFavorites(email)
    }

    suspend fun searchRecipes(query: String, cachedRecipes: List<Recipe>): List<Recipe> {
        return try {
            dataSource.searchRecipes(query)
        } catch (exception: Exception) {
            Log.d("DEBUG_SEARCH", "Error buscando recetas remotas: ${exception.message}")
            filterRecipesLocally(cachedRecipes, query)
        }
    }

    private fun filterRecipesLocally(recipes: List<Recipe>, query: String): List<Recipe> {
        val normalizedQuery = normalizeSearchText(query)
        if (normalizedQuery.isBlank()) {
            return recipes
        }

        return recipes.filter { recipe ->
            normalizeSearchText(recipe.title).contains(normalizedQuery) ||
                normalizeSearchText(recipe.ingredients).contains(normalizedQuery)
        }
    }

    private suspend fun getCachedRecipe(appContext: Context, id: Int): SingleRecipeDTO? {
        return withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(appContext)
            db.recipeDao().getRecipeById(id)?.toSingleRecipeDTO()
        }
    }

    private fun Recipe.toSingleRecipeDTO(): SingleRecipeDTO {
        val parsedIngredients = parseIngredientSummary(ingredients)
        return SingleRecipeDTO(
            id = id,
            title = title,
            image = image.orEmpty(),
            ingredients = ingredients,
            extendedIngredients = parsedIngredients
        )
    }

    private fun SingleRecipeDTO.withParsedIngredients(): SingleRecipeDTO {
        val parsedIngredients = if (extendedIngredients.isNullOrEmpty()) {
            parseIngredientSummary(ingredients)
        } else {
            extendedIngredients
        }

        return copy(extendedIngredients = parsedIngredients)
    }

    private fun resolveDetailFallbackMessage(
        errorType: RecipeDataErrorType
    ): RecipeUserMessage {
        return when (errorType) {
            RecipeDataErrorType.API_QUOTA_EXCEEDED ->
                RecipeUserMessage.SHOWING_SAVED_DETAIL_API_LIMIT
            RecipeDataErrorType.NO_CONNECTION ->
                RecipeUserMessage.SHOWING_SAVED_DETAIL_OFFLINE
            else ->
                RecipeUserMessage.SHOWING_SAVED_DETAIL_SERVICE_ISSUE
        }
    }
}
