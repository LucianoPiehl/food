package com.example.food.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.food.model.FavoriteRecipe

@Dao
interface FavoriteRecipeDao {

    @Query("SELECT * FROM favorite_recipes WHERE userEmail = :userEmail")
    fun getFavoriteRecipesForUser(userEmail: String): List<FavoriteRecipe>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFavoriteRecipe(favoriteRecipe: FavoriteRecipe)

    @Query("DELETE FROM favorite_recipes WHERE userEmail = :userEmail AND recipeId = :recipeId")
    fun deleteFavoriteRecipe(userEmail: String, recipeId: Int)

    @Query("SELECT COUNT(*) FROM favorite_recipes WHERE userEmail = :userEmail AND recipeId = :recipeId")
    fun isFavorite(userEmail: String, recipeId: Int): Int
}
