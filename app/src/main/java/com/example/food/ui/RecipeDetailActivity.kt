package com.example.food.ui
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.food.databinding.ActivityRecipeDetailBinding
import com.squareup.picasso.Picasso


class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the data from the intent
        val recipeTitle = intent.getStringExtra("RECIPE_TITLE")
        val recipeImage = intent.getStringExtra("RECIPE_IMAGE")
        val recipeIngredients = intent.getStringExtra("INGREDIENTS")

        // Set the data to the views
        binding.recipeTitle.text = recipeTitle
        Picasso.get().load(recipeImage).into(binding.recipeImage)

        // Load HTML content into WebView
        binding.webView.loadDataWithBaseURL(null, recipeIngredients ?: "", "text/html", "UTF-8", null)

        // Enable JavaScript (if necessary)
        binding.webView.settings.javaScriptEnabled = true

        // Adjust image sizes after loading HTML
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                adjustImageSizesInWebView()
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
