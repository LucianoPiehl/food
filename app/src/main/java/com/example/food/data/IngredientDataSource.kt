package com.example.food.data

import com.example.food.model.IngredientDTO
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class IngredientDataSource {

    companion object {
        @Volatile
        private var instance: IngredientDataSource? = null

        fun getInstance(): IngredientDataSource {
            return instance ?: synchronized(this) {
                instance ?: IngredientDataSource().also { instance = it }
            }
        }
    }
    private val api: RecipesAPI

    init {
        val gson = GsonBuilder()
            .setLenient()
            .create()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.spoonacular.com")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        api = retrofit.create(RecipesAPI::class.java)
    }

    fun getIngredientById(id: Int): Call<String> {
        return api.getIngredientsWidget(id)
    }
}