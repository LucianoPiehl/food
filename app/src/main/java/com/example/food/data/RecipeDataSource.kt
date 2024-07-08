package com.example.food.data

import android.util.Log
import com.example.food.model.RecipeDTO
import com.example.food.model.SingleRecipeDTO
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RecipeDataSource {

    companion object {
        @Volatile
        private var instance: RecipeDataSource? = null

        fun getInstance(): RecipeDataSource {
            return instance ?: synchronized(this) {
                instance ?: RecipeDataSource().also { instance = it }
            }
        }
    }
    private val api: RecipesAPI

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.spoonacular.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(RecipesAPI::class.java)
    }

    fun getRandomRecipe(): Call<RecipeDTO> {
        return api.getRandomRecipeAsync()
    }

    fun getRecipeById(id: Int): Call<SingleRecipeDTO> {
        return api.getRecipesXIDAsync(id)
    }
}
