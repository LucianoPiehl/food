package com.example.food.data
import com.example.food.model.Ingredient
import com.example.food.model.IngredientDTO
import com.example.food.model.RecipeDTO
import com.example.food.model.SingleRecipeDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RecipesAPI {

    @GET("/recipes/{id}/information?apiKey=206b9f8f0fa14aeb88db2d0b9200b6cf")
    fun getRecipesXIDAsync(
        @Path("id") id: Int
    ): Call<SingleRecipeDTO>

    @GET("/recipes/random?apiKey=206b9f8f0fa14aeb88db2d0b9200b6cf")
    fun getRandomRecipeAsync(): Call<RecipeDTO>


    @GET("/recipes/{id}/ingredientWidget?apiKey=206b9f8f0fa14aeb88db2d0b9200b6cf")
    fun getIngredientsWidget(
        @Path("id") id: Int,
        @Query("measure") measure: String = "us"
    ): Call<String>

}
