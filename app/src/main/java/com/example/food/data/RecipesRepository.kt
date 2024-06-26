package com.example.food.data
import android.util.Log
import com.example.food.model.RecipeDTO
import retrofit2.Call

class RecipesRepository() {
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
    fun getRandomRecipe(): Call<RecipeDTO> {
        return dataSource.getRandomRecipe()
    }

    fun getRecipeById(id: Int): Call<RecipeDTO> {
        return dataSource.getRecipeById(id)
    }
}