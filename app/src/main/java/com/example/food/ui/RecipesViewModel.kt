package com.example.food.ui
import android.content.Context
import com.example.food.util.sustraer_html
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.data.AppDatabase
import com.example.food.data.IngredientRepository
import com.example.food.data.RecipesRepository
import com.example.food.model.FavoriteRecipe
import com.example.food.model.Recipe
import com.example.food.model.RecipeDTO
import com.example.food.ui.adaptor.RecipesAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class RecipesViewModel(private val appContext: Context) : ViewModel() {

    private val repository = RecipesRepository.getInstance()
    private val repository2 = IngredientRepository.getInstance()
    private val _recipes = MutableLiveData<List<Recipe>>()
    private val extract_html= sustraer_html()
    private val _favoriteRecipes = MutableLiveData<List<FavoriteRecipe>>() // LiveData para recetas favoritas
    val recipes: LiveData<List<Recipe>> get() = _recipes
    lateinit var recipesAdapter: RecipesAdapter
    private val _userEmail = MutableLiveData<String>()

    private var loadedRecipes = mutableListOf<Recipe>()


    fun setUserEmail(email: String) {
        _userEmail.value = email
    }

    init {
        _recipes.value = emptyList()
        _favoriteRecipes.value = emptyList() // Inicializar la lista de recetas favoritas
        loadMoreRecipes()
        loadFavoriteRecipes()

    }
    private fun loadFavoriteRecipes() {

    }


    fun toggleFavorite(recipeId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                val db = AppDatabase.getDatabase(appContext)

                // Obtener el email del usuario actual
                val userEmail = _userEmail.value ?: return@withContext
                val userDao = db.userDao()
                val user = userDao.getUserByEmailSync(userEmail) ?: return@withContext

                // Verificar si la receta está marcada como favorita
                val isFavorite = db.favoriteRecipeDao().isFavorite(user.email, recipeId) > 0
                Log.d("DEBUG FAV","La receta es fav?: "+isFavorite)
                // Cambiar el estado de favorito
                if (isFavorite) {
                    db.favoriteRecipeDao().deleteFavoriteRecipe(user.email, recipeId)


                } else {
                    db.favoriteRecipeDao().insertFavoriteRecipe(FavoriteRecipe(user.email, recipeId))

                }

                // Actualizar el estado de isFavorite en la lista de recetas cargadas
                loadedRecipes.find { it.id == recipeId }?.isFavorite = !isFavorite

                // Notificar cambios en la lista de recetas
                _recipes.postValue(loadedRecipes)


            }
        }
    }
    fun esFav(recipeId: Int, email: String): Boolean {
        var isFavorite = false
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(appContext)
                // Verificar si hay registros en la tercera tabla con los parámetros dados
                isFavorite = db.favoriteRecipeDao().isFavorite(email, recipeId) > 0
            }
        }
        return isFavorite
    }
    fun loadMoreRecipes() {
        val calls = mutableListOf<Call<RecipeDTO>>()
        repeat(6) {
            calls.add(repository.getRandomRecipe())
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
                                    repository2.getIngredientXID(recipeDetail.id).enqueue(object : Callback<String> {
                                        override fun onResponse(call: Call<String>, detailResponse: Response<String>) {
                                            if (detailResponse.isSuccessful) {
                                                val detail = detailResponse.body()
                                                Log.d("DEBUG NUEVO",detail.toString())
                                                detail?.let {
                                                    // Combinar datos y crear el objeto Recipe
                                                    val isRecipeFavorite = esFav(recipeDetail.id, _userEmail.value ?: "")
                                                    val newRecipe = Recipe(recipeDetail.id, recipeDetail.title, recipeDetail.image,
                                                        extract_html.removeIngredientGrid(detail), isRecipeFavorite)
                                                    try {
                                                        viewModelScope.launch {
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
                        Log.d("DEBUG_DE LA API", "Error en la respuesta del servidor") //NO DEBERIA IR ACA EL CODIGO DE USAR LA BASE DE DATOS?
                        val localRecipes = getRecipesFromLocalDatabase()

                    }
                }

                override fun onFailure(call: Call<RecipeDTO>, t: Throwable) {
                    // Handle failure
                    Log.d("DEBUG", t.toString())
                }
            })
        }
    }






    private fun getRecipesFromLocalDatabase() {
        try {
            // Ejecutar en un hilo secundario
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(appContext)
                    val recipes = db.recipeDao().getAllRecipesSync() // Asegúrate de obtener las recetas correctamente
                    loadedRecipes.clear()
                    loadedRecipes.addAll(recipes)
                    _recipes.postValue(loadedRecipes)

                }
            }
        } catch (e: Exception) {
            Log.e("RecipesViewModel", "Error retrieving recipes from local database", e)
        }
    }

}

