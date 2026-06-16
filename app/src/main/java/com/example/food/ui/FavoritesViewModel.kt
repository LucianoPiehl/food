package com.example.food.ui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.data.RecipesRepository
import com.example.food.model.SingleRecipeDTO
import com.example.food.ui.adaptor.FavoritesAdapter
import com.example.food.util.normalizeSearchText
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FavoritesViewModel(private val context: Context) : ViewModel() {
    private val repository = RecipesRepository.getInstance()
    private val _favorites = MutableLiveData<List<SingleRecipeDTO>>()
    val favorites: LiveData<List<SingleRecipeDTO>> get() = _favorites
    var _userEmail = String()
    lateinit var favoritesAdapter: FavoritesAdapter
    private var loadFavoritesJob: Job? = null
    private var allFavorites: List<SingleRecipeDTO> = emptyList()
    private var currentSearchQuery = ""

    fun setUserEmail(email: String) {
        _userEmail = email
    }

    fun loadMoreFavorites() {
        if (_userEmail.isBlank()) {
            _favorites.value = emptyList()
            return
        }

        loadFavoritesJob?.cancel()
        loadFavoritesJob = viewModelScope.launch {
            allFavorites = repository.getFavorites(_userEmail)
            applyFilter(currentSearchQuery)
        }
    }

    fun searchFavorites(query: String) {
        currentSearchQuery = query
        applyFilter(query)
    }

    private fun applyFilter(query: String) {
        val normalizedQuery = normalizeSearchText(query)
        if (normalizedQuery.isBlank()) {
            _favorites.postValue(allFavorites)
            return
        }

        val filteredFavorites = allFavorites.filter { recipe ->
            normalizeSearchText(recipe.title).contains(normalizedQuery) ||
                normalizeSearchText(recipe.ingredients).contains(normalizedQuery)
        }
        _favorites.postValue(filteredFavorites)
    }

    init {
        _favorites.value = emptyList()
    }
}
