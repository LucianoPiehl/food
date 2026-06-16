package com.example.food.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.food.R
import com.example.food.data.RecipeDataErrorType
import com.example.food.data.RecipeDataException
import com.example.food.data.RecipeUserMessage
import com.example.food.data.RecipesRepository
import com.example.food.databinding.ActivityRecipeDetailBinding
import com.example.food.model.SingleRecipeDTO
import com.example.food.ui.adaptor.IngredientsAdapter
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeDetailBinding
    private val repository = RecipesRepository.getInstance()
    private val ingredientsAdapter = IngredientsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getIntExtra("RECIPE_ID", 0)
        val email = intent.getStringExtra("EMAIL").orEmpty()

        binding.ingredientsRecyclerView.apply {
            layoutManager = GridLayoutManager(this@RecipeDetailActivity, 3)
            adapter = ingredientsAdapter
        }

        lifecycleScope.launch {
            try {
                val detailResult = repository.getRecipeDetail(applicationContext, email, id)
                val recipe: SingleRecipeDTO = detailResult.recipe
                var isFavorite = repository.isFavorite(email, recipe.id)

                updateFavoriteUi(isFavorite)
                binding.favoriteIcon.setOnClickListener {
                    toggleFavorite(recipe, email, isFavorite) { newState ->
                        isFavorite = newState
                    }
                }
                binding.favoriteButton.setOnClickListener {
                    toggleFavorite(recipe, email, isFavorite) { newState ->
                        isFavorite = newState
                    }
                }

                binding.recipeTitle.text = recipe.title
                Picasso.get()
                    .load(recipe.image)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.bg_image_placeholder)
                    .into(binding.recipeImage)

                val ingredients = recipe.extendedIngredients.orEmpty()
                ingredientsAdapter.updateIngredients(ingredients)
                binding.ingredientsEmptyText.isVisible = ingredients.isEmpty()

                detailResult.userMessage?.let { messageType ->
                    showMessage(resolveUserMessage(messageType), useSnackbar = true)
                }
            } catch (exception: RecipeDataException) {
                showMessage(resolveFatalDetailMessage(exception.type))
                finish()
                return@launch
            } catch (_: Exception) {
                showMessage(getString(R.string.detail_loading_error))
                finish()
                return@launch
            }
        }
    }

    private fun toggleFavorite(
        recipe: SingleRecipeDTO,
        email: String,
        currentState: Boolean,
        onStateChanged: (Boolean) -> Unit
    ) {
        if (email.isBlank()) {
            showMessage(getString(R.string.detail_favorite_login_required))
            return
        }

        lifecycleScope.launch {
            val newState = !currentState
            val updated = repository.setFavorite(email, recipe, newState)
            if (updated) {
                onStateChanged(newState)
                updateFavoriteUi(newState)
                showMessage(
                    getString(
                        if (newState) {
                            R.string.detail_favorite_saved
                        } else {
                            R.string.detail_favorite_removed
                        }
                    )
                )
            } else {
                showMessage(getString(R.string.favorite_error_generic))
            }
        }
    }

    private fun updateFavoriteUi(isFavorite: Boolean) {
        val iconRes = if (isFavorite) R.drawable.estrella_on else R.drawable.estrella_off
        val textRes = if (isFavorite) {
            R.string.detail_favorite_remove
        } else {
            R.string.detail_favorite_add
        }

        binding.favoriteIcon.setImageResource(iconRes)
        binding.favoriteButton.text = getString(textRes)
        binding.favoriteButton.setIconResource(iconRes)
    }

    private fun resolveUserMessage(messageType: RecipeUserMessage): String {
        return when (messageType) {
            RecipeUserMessage.SHOWING_SAVED_DETAIL_API_LIMIT ->
                getString(R.string.detail_loading_fallback_quota)
            RecipeUserMessage.SHOWING_SAVED_DETAIL_OFFLINE ->
                getString(R.string.detail_loading_fallback_offline)
            RecipeUserMessage.SHOWING_SAVED_DETAIL_SERVICE_ISSUE ->
                getString(R.string.detail_loading_fallback_service)
        }
    }

    private fun resolveFatalDetailMessage(errorType: RecipeDataErrorType): String {
        return when (errorType) {
            RecipeDataErrorType.API_QUOTA_EXCEEDED ->
                getString(R.string.detail_loading_error_quota)
            RecipeDataErrorType.NO_CONNECTION ->
                getString(R.string.detail_loading_error_offline)
            RecipeDataErrorType.RECIPE_NOT_FOUND ->
                getString(R.string.detail_loading_error_not_found)
            RecipeDataErrorType.INVALID_API_CONFIGURATION ->
                getString(R.string.detail_loading_error_configuration)
            RecipeDataErrorType.REMOTE_UNAVAILABLE ->
                getString(R.string.detail_loading_error_service)
        }
    }

    private fun showMessage(message: String, useSnackbar: Boolean = false) {
        if (useSnackbar) {
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}
