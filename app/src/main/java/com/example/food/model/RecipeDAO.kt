package com.example.food.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipes(recipes: Recipe)

    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    fun getRecipeById(recipeId: Int): Recipe

    @Query("SELECT * FROM recipes limit 6")
    fun getAllRecipesSync(): List<Recipe>
}