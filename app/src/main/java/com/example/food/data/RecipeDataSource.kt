package com.example.food.data

import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.food.model.Recipe
import com.example.food.model.RecipeDTO
import com.example.food.model.SingleRecipeDTO
import com.example.food.util.sustraer_html
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RecipeDataSource {

    companion object {
        @Volatile
        private var instance: RecipeDataSource? = null

        fun getInstance(): RecipeDataSource {
            return instance ?: synchronized(this) {
                instance ?: RecipeDataSource().also { instance = it }
            }
        }
    }
    private val db = FirebaseFirestore.getInstance()
    private val api: RecipesAPI
    private val repository2 = IngredientRepository.getInstance()
    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.spoonacular.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(RecipesAPI::class.java)
    }

    fun getRandomRecipe(): Call<RecipeDTO> {
        return api.getRandomRecipeAsync()
    }



    suspend fun getRecipeById(id: Int): SingleRecipeDTO {
        var recipe = suspendCoroutine<SingleRecipeDTO?>{ continuation ->
            db.collection("recipes").document(id.toString()).get().addOnCompleteListener{
                if (it.isSuccessful) {
                    val document = it.result.toObject(SingleRecipeDTO::class.java)
                    continuation.resume(document)
                }else{
                    continuation.resume(null)

                }            }
        }
        if (recipe != null) {
            return recipe

        }else{
            var recipe = suspendCoroutine { continuation ->
                val recipeCall: Call<SingleRecipeDTO> =api.getRecipesXIDAsync(id)

                recipeCall.enqueue(object : Callback<SingleRecipeDTO> {
                    override fun onResponse(call: Call<SingleRecipeDTO>, response: Response<SingleRecipeDTO>) {

                        if (response.isSuccessful) {
                            val recipeDTO = response.body()
                            recipeDTO?.let { detail ->
                                val ingredients:Call<String> = repository2.getIngredientXID(id)
                                ingredients.enqueue(object : Callback<String> {
                                    override  fun onResponse(call:Call<String>, response2: Response<String>){
                                        if (response2.isSuccessful) {
                                            val ingredientDTO = response2.body()
                                            if (ingredientDTO != null) {
                                                recipeDTO.ingredients = ingredientDTO
                                                db.collection("recipes").document(id.toString()).set(recipeDTO)
                                            }
                                            continuation.resume(recipeDTO)

                                        } else {
                                            continuation.resume(null)
                                        }
                                    }

                                    override fun  onFailure(call:Call<String>, t:Throwable){
                                        Log.d("DEBUG Ingredients","Error: ${t.message}")
                                    }
                                })



                            } ?: run {
                                Log.d("DEBUG", "Recipe detail is null")
                            }
                        } else {
                            Log.d("DEBUG", "Error: ${response.code()}")
                        }
                    }


                    override fun onFailure(call: Call<SingleRecipeDTO>, t: Throwable) {
                        Log.d("DEBUG","Error: ${t.message}")

                    }
                }
                )
            }
            return recipe!!
        }

    }
}
