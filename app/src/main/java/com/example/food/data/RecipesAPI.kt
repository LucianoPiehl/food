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

    @GET("/recipes/{id}/information?apiKey=fe384e0c291649e8b3a07c19ba71972a")
    fun getRecipesXIDAsync(
        @Path("id") id: Int
    ): Call<SingleRecipeDTO>

    @GET("/recipes/random?apiKey=fe384e0c291649e8b3a07c19ba71972a")
    fun getRandomRecipeAsync(): Call<RecipeDTO>


    @GET("/recipes/{id}/ingredientWidget?apiKey=fe384e0c291649e8b3a07c19ba71972a")
    fun getIngredientsWidget(
        @Path("id") id: Int,
        @Query("measure") measure: String = "us"
    ): Call<String>

}
