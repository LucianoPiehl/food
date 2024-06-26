package com.example.food.data
import android.util.Log
import com.example.food.model.Ingredient
import com.example.food.model.IngredientDTO
import retrofit2.Call

class IngredientRepository() {
    private val dataSource = IngredientDataSource.getInstance()
    companion object {
        @Volatile
        private var instance: IngredientRepository? = null

        fun getInstance(): IngredientRepository {
            return instance ?: synchronized(this) {
                instance ?: IngredientRepository().also { instance = it }
            }
        }
    }
    fun getIngredientXID(id: Int): Call<String> {
        return dataSource.getIngredientById(id)
    }
}
