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
    private val appContext2:Context = appContext
    private val _recipes = MutableLiveData<List<Recipe>>()

    // LiveData para recetas favoritas
    val recipes: LiveData<List<Recipe>> get() = _recipes
    lateinit var recipesAdapter: RecipesAdapter
    private val _userEmail = MutableLiveData<String>()

    private var loadedRecipes = mutableListOf<Recipe>()


    fun setUserEmail(email: String) {
        _userEmail.value = email
    }

    init {
        _recipes.value = emptyList()
        // Inicializar la lista de recetas favoritas
        loadMoreRecipes()
    }

    fun loadMoreRecipes() {
        repository.loadRecipes(appContext2, loadedRecipes, _recipes)
    }

}

