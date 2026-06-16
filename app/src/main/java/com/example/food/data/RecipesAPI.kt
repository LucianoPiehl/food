package com.example.food.data
import com.example.food.model.ComplexSearchResponse
import com.example.food.model.RecipeDTO
import com.example.food.model.SearchRecipeDTO
import com.example.food.model.SingleRecipeDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private const val SPOONACULAR_API_KEY = "e1c1f15c5e1d4d65ac24014479f26daa"

interface RecipesAPI {

    @GET("/recipes/{id}/information")
    fun getRecipesXIDAsync(
        @Path("id") id: Int,
        @Query("apiKey") apiKey: String = SPOONACULAR_API_KEY
    ): Call<SingleRecipeDTO>

    @GET("/recipes/random")
    fun getRandomRecipeAsync(
        @Query("number") number: Int = 1,
        @Query("apiKey") apiKey: String = SPOONACULAR_API_KEY
    ): Call<RecipeDTO>

    @GET("/recipes/complexSearch")
    suspend fun searchRecipes(
        @Query("query") query: String,
        @Query("number") number: Int = 12,
        @Query("addRecipeInformation") addRecipeInformation: Boolean = true,
        @Query("fillIngredients") fillIngredients: Boolean = true,
        @Query("apiKey") apiKey: String = SPOONACULAR_API_KEY
    ): ComplexSearchResponse

    @GET("/recipes/findByIngredients")
    suspend fun searchRecipesByIngredients(
        @Query("ingredients") ingredients: String,
        @Query("number") number: Int = 12,
        @Query("ranking") ranking: Int = 1,
        @Query("ignorePantry") ignorePantry: Boolean = true,
        @Query("apiKey") apiKey: String = SPOONACULAR_API_KEY
    ): List<SearchRecipeDTO>
}
