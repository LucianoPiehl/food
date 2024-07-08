package com.example.food.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.food.model.Recipe
import com.example.food.model.RecipeDao

@Database(entities = [Recipe::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigrationFrom(5) // Cambiar el número de versión según sea necesario
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
