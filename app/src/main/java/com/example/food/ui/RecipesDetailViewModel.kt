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


}
