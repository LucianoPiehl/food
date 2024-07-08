package com.example.food.ui

import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.food.R
import com.example.food.data.IngredientRepository
import com.example.food.data.RecipesRepository
import com.example.food.databinding.ActivityRecipeDetailBinding
import com.example.food.model.RecipeDTO
import com.example.food.model.SingleRecipeDTO
import com.example.food.util.sustraer_html
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeDetailBinding
    private val repository = RecipesRepository.getInstance()
    private val repository2 = IngredientRepository.getInstance()
    private val viewModel: RecipesViewModel by viewModels {
        RecipesViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getIntExtra("RECIPE_ID", 0)

        val recipe: Call<SingleRecipeDTO> = repository.getRecipeById(id)

        recipe.enqueue(object : Callback<SingleRecipeDTO> {
            override fun onResponse(call: Call<SingleRecipeDTO>, response: Response<SingleRecipeDTO>) {
                if (response.isSuccessful) {
                    val recipeDTO = response.body()
                    recipeDTO?.let { detail ->
                        binding.recipeTitle.text = detail.title
                        Picasso.get().load(detail.image).into(binding.recipeImage)

                        val ingredients:Call<String> = repository2.getIngredientXID(id)
                        ingredients.enqueue(object : Callback<String> {
                            override  fun onResponse(call:Call<String>, response2:Response<String>){
                                if (response2.isSuccessful) {
                                    val ingredientDTO = response2.body()
                                    ingredientDTO?.let { detail2 ->
                                        Log.d("DEBUG DETAIL", detail2.toString())
                                        //Log.d("DEBUG ING",detail.ingredients.toString())

                                        binding.webView.loadDataWithBaseURL(
                                            null,
                                            sustraer_html().removeIngredientList(detail2),
                                            "text/html",
                                            "UTF-8",
                                            null
                                        )
                                        binding.webView.settings.javaScriptEnabled = true
                                        binding.webView.webViewClient = object : WebViewClient() {
                                            override fun onPageFinished(view: WebView?, url: String?) {
                                                super.onPageFinished(view, url)
                                                adjustImageSizesInWebView()
                                            }
                                        }
                                    }
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
        })
    }

    private fun adjustImageSizesInWebView() {
        binding.webView.evaluateJavascript(
            "(function() {" +
                    "  var images = document.getElementsByTagName('img');" +
                    "  for (var i = 0; i < images.length; i++) {" +
                    "    images[i].style.maxWidth = '100%';" + // Ajusta el ancho máximo al 100% del contenedor
                    "    images[i].style.height = 'auto';" +    // Ajusta la altura automáticamente
                    "  }" +
                    "})();" +
                    "window.scrollTo(0, 0);", // Desplázate al principio del WebView después de ajustar las imágenes
            null
        )
    }
}
