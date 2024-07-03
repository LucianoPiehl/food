package com.example.food.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.data.AppDatabase
import com.example.food.model.Recipe
import com.example.food.model.FavoriteRecipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipesDetailViewModel(private val appContext: Context) : ViewModel(){

    private val db = AppDatabase.getDatabase(appContext)
    private val _recipe = MutableLiveData<Recipe>()
    val recipe: LiveData<Recipe> get() = _recipe

    fun getRecipeById(recipeId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val recipeFromDb = db.recipeDao().getRecipeById(recipeId)
                _recipe.postValue(recipeFromDb)
            }
        }
    }

    fun toggleFavorite(recipeId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val userEmail = "example@example.com" // Aquí debes obtener el email del usuario activo
                val isFavorite = db.favoriteRecipeDao().isFavorite(userEmail, recipeId) > 0

                if (isFavorite) {
                    db.favoriteRecipeDao().deleteFavoriteRecipe(userEmail, recipeId)
                } else {
                    db.favoriteRecipeDao().insertFavoriteRecipe(FavoriteRecipe(userEmail, recipeId))
                }

                // Actualizar el estado de favorito en el objeto Recipe
                _recipe.value?.isFavorite = !isFavorite
                _recipe.postValue(_recipe.value)
            }
        }
    }
}
