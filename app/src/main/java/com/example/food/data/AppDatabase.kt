package com.example.food.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.food.model.FavoriteRecipe
import com.example.food.model.FavoriteRecipeDao
import com.example.food.model.Recipe
import com.example.food.model.RecipeDao
import com.example.food.model.User

@Database(entities = [User::class, Recipe::class, FavoriteRecipe::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun userDao(): UserDao
    abstract fun favoriteRecipeDao(): FavoriteRecipeDao // Nuevo DAO para recetas favoritas

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigrationFrom(4) // Cambiar el número de versión según sea necesario
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
