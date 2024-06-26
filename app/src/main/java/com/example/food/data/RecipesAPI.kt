package com.example.food.data
import com.example.food.model.Ingredient
import com.example.food.model.IngredientDTO
import com.example.food.model.RecipeDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RecipesAPI {

    @GET("/recipes/{id}/information?apiKey=6e43e90f07fc40edb523754146c74c64")
    fun getRecipesXIDAsync(
        @Path("id") id: Int
    ): Call<RecipeDTO>

    @GET("/recipes/random?apiKey=6e43e90f07fc40edb523754146c74c64")
    fun getRandomRecipeAsync(): Call<RecipeDTO>


    @GET("/recipes/{id}/ingredientWidget?apiKey=6e43e90f07fc40edb523754146c74c64")
    fun getIngredientsWidget(
        @Path("id") id: Int,
        @Query("measure") measure: String = "us"
    ): Call<String>

}
