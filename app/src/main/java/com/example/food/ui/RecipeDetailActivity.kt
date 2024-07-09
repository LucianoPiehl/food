package com.example.food.ui

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.food.R
import com.example.food.data.RecipesRepository
import com.example.food.databinding.ActivityRecipeDetailBinding
import com.example.food.model.SingleRecipeDTO
import com.example.food.util.sustraer_html
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeDetailBinding
    private val repository = RecipesRepository.getInstance()

    private val viewModel: RecipesViewModel by viewModels {
        RecipesViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getIntExtra("RECIPE_ID", 0)
        val email= intent.getStringExtra("EMAIL")
        lifecycleScope.launch {
            val recipe: SingleRecipeDTO = repository.getRecipeById(id)
            recipe.let {
                binding.favoriteIcon.setOnClickListener(){
                    repository.setFavorite(email.toString(),recipe.id)
                    val drawable=binding.favoriteIcon.drawable
                    if (drawable.toString() == "estrella_off"){
                        binding.favoriteIcon.setImageResource(R.drawable.estrella_on)
                    }else{
                        binding.favoriteIcon.setImageResource(R.drawable.estrella_off)
                    }
                }
                binding.recipeTitle.text = recipe.title
                Picasso.get().load(recipe.image).into(binding.recipeImage)
                binding.webView.loadDataWithBaseURL(
                    null,
                    sustraer_html().removeIngredientList(recipe.ingredients),
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
