package com.example.food.ui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.data.RecipesRepository
import com.example.food.model.SingleRecipeDTO
import com.example.food.ui.adaptor.FavoritesAdapter
import kotlinx.coroutines.launch

class FavoritesViewModel(private val context: Context) : ViewModel() {
    private val repository = RecipesRepository.getInstance()
    private val _favorites = MutableLiveData<List<SingleRecipeDTO>>()
    val favorites: LiveData<List<SingleRecipeDTO>> get() = _favorites
    var _userEmail = String()
    lateinit var favoritesAdapter: FavoritesAdapter
    private val favoriteRecipes = mutableListOf<SingleRecipeDTO>()

    fun setUserEmail(email: String) {
        _userEmail = email
    }

    fun loadMoreFavorites() {
        viewModelScope.launch {
            repository.getFavorites(_userEmail, favoriteRecipes, _favorites)
            /*context, _userEmail, favoriteRecipes, _favorites*/
        }
    }

    init {
        _favorites.value = emptyList()
        // Inicializar la lista de recetas favoritas
        loadMoreFavorites()
    }
}
