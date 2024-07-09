package com.example.food.data
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.food.model.Recipe
import com.example.food.model.RecipeDTO
import com.example.food.model.SingleRecipeDTO
import com.example.food.util.sustraer_html
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipesRepository() {
    private val dataSource = RecipeDataSource.getInstance()
    private val repository2 = IngredientRepository.getInstance()
    private val extract_html= sustraer_html()
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
    fun loadRecipes(appContext: Context, loadedRecipes: MutableList<Recipe>,recipes: LiveData<List<Recipe>>,_recipes: MutableLiveData<List<Recipe>>){
        val calls = mutableListOf<Call<RecipeDTO>>()
        repeat(6) {
            calls.add(getRandomRecipe())
        }

        calls.forEach { call ->
            call.enqueue(object : Callback<RecipeDTO> {
                override fun onResponse(call: Call<RecipeDTO>, response: Response<RecipeDTO>) {

                    if (response.isSuccessful) {
                        val recipeDTO = response.body()
                        recipeDTO?.let {
                            if (it.recipes.isNotEmpty()) {

                                it.recipes.forEach { recipeDetail ->
                                    // Realizamos una llamada adicional para obtener más datos
                                    repository2.getIngredientXID(recipeDetail.id).enqueue(object :
                                        Callback<String> {
                                        override fun onResponse(call: Call<String>, detailResponse: Response<String>) {
                                            if (detailResponse.isSuccessful) {
                                                val detail = detailResponse.body()
                                                detail?.let {
                                                    // Combinar datos y crear el objeto Recipe
                                                    val newRecipe = Recipe(recipeDetail.id, recipeDetail.title, recipeDetail.image,
                                                        extract_html.removeIngredientGrid(detail))
                                                    try {
                                                        GlobalScope.launch {
                                                            withContext(Dispatchers.IO) {
                                                                val db = AppDatabase.getDatabase(appContext)
                                                                // Insertar las recetas en la base de datos
                                                                db.recipeDao().insertRecipes((newRecipe))
                                                            }
                                                        }

                                                    } catch (e: Exception) {
                                                        Log.e("RecipesViewModel", "Error inserting recipes into local database", e)
                                                    }
                                                    // Agregamos la nueva receta a la lista cargada
                                                    loadedRecipes.add(newRecipe)
                                                    // Actualizamos el LiveData con todas las recetas cargadas
                                                    _recipes.postValue(loadedRecipes)
                                                } ?: run {
                                                    Log.d("DEBUG2", "El cuerpo del detalle es nulo")
                                                }
                                            } else {
                                                Log.d("DEBUG2", "Error en la respuesta del servidor de detalle")
                                            }
                                        }

                                        override fun onFailure(call: Call<String>, t: Throwable) {
                                            // Handle failure
                                            Log.d("DEBUG2", t.toString())
                                        }
                                    })
                                }
                            } else {
                                Log.d("DEBUG", "La lista de recetas está vacía")
                            }
                        } ?: run {
                            Log.d("DEBUG", "El cuerpo de la respuesta es nulo")
                        }
                    } else {
                        Log.d("DEBUG_DE LA API", "Error en la respuesta del servidor")
                        getRecipesFromLocalDatabase(appContext,loadedRecipes,_recipes)


                    }
                }
                private fun getRecipesFromLocalDatabase(appContext: Context, loadedRecipes: MutableList<Recipe>, _recipes: MutableLiveData<List<Recipe>>) {
                    try {
                        GlobalScope.launch {
                            withContext(Dispatchers.IO) {
                                val db = AppDatabase.getDatabase(appContext)
                                val recipes = db.recipeDao().getAllRecipesSync()
                                withContext(Dispatchers.Main) {
                                    loadedRecipes.clear()
                                    loadedRecipes.addAll(recipes)
                                    _recipes.postValue(loadedRecipes)

                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("RecipesViewModel", "Error retrieving recipes from local database", e)
                    }
                }
                override fun onFailure(call: Call<RecipeDTO>, t: Throwable) {
                    // Handle failure
                    Log.d("DEBUG", t.toString())
                }
            })
        }
    }
    suspend fun getRecipeById(id: Int):SingleRecipeDTO {
        return dataSource.getRecipeById(id)
    }
}