package com.example.food.ui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.data.RecipesRepository
import com.example.food.model.Recipe
import com.example.food.ui.adaptor.RecipesAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipesViewModel(private val appContext: Context) : ViewModel() {

    private val repository = RecipesRepository.getInstance()
    private val appContext2: Context = appContext
    private val _recipes = MutableLiveData<List<Recipe>>()

    val recipes: LiveData<List<Recipe>> get() = _recipes
    lateinit var recipesAdapter: RecipesAdapter
    private val _userEmail = MutableLiveData<String>()

    private var loadedRecipes = mutableListOf<Recipe>()
    private var activeSearchQuery = ""
    private var searchJob: Job? = null

    fun setUserEmail(email: String) {
        _userEmail.value = email
    }

    init {
        _recipes.value = emptyList()
        loadMoreRecipes()
    }

    fun loadMoreRecipes() {
        if (activeSearchQuery.isNotBlank()) {
            return
        }
        repository.loadRecipes(appContext2, loadedRecipes, _recipes) {
            activeSearchQuery.isBlank()
        }
    }

    fun searchRecipes(query: String) {
        val normalizedQuery = query.trim()
        activeSearchQuery = normalizedQuery

        searchJob?.cancel()
        if (normalizedQuery.isBlank()) {
            _recipes.value = loadedRecipes.toList()
            if (loadedRecipes.isEmpty()) {
                loadMoreRecipes()
            }
            return
        }

        searchJob = viewModelScope.launch {
            delay(350)
            val results = repository.searchRecipes(normalizedQuery, loadedRecipes.toList())
            if (activeSearchQuery != normalizedQuery) {
                return@launch
            }
            val email = _userEmail.value.orEmpty()

            results.forEach { recipe ->
                recipe.isFavorite = email.isNotBlank() && repository.isFavorite(email, recipe.id)
            }

            _recipes.postValue(results)
        }
    }

    fun syncFavoriteState(recipe: Recipe, onComplete: (Boolean) -> Unit) {
        val email = _userEmail.value.orEmpty()
        if (email.isBlank()) {
            recipe.isFavorite = false
            onComplete(false)
            return
        }

        viewModelScope.launch {
            val isFavorite = repository.isFavorite(email, recipe.id)
            recipe.isFavorite = isFavorite
            withContext(Dispatchers.Main) {
                onComplete(isFavorite)
            }
        }
    }

    fun toggleFavorite(recipe: Recipe, onComplete: (Boolean?) -> Unit) {
        val email = _userEmail.value.orEmpty()
        if (email.isBlank()) {
            onComplete(null)
            return
        }

        viewModelScope.launch {
            val newState = !recipe.isFavorite
            val updated = repository.setFavorite(email, recipe, newState)
            if (updated) {
                recipe.isFavorite = newState
            }
            withContext(Dispatchers.Main) {
                onComplete(if (updated) newState else null)
            }
        }
    }

}

