package com.example.food.ui.adaptor

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.food.R
import com.example.food.databinding.ItemRecipeBinding
import com.example.food.model.Recipe
import com.example.food.ui.RecipeDetailActivity
import com.example.food.ui.RecipesViewModel
import com.squareup.picasso.Picasso

class RecipesAdapter(
    private val email: String,
    private val viewModel2: RecipesViewModel,
    private val cont: Context
) : RecyclerView.Adapter<RecipesAdapter.RecipeViewHolder>() {

    private var recipes: List<Recipe> = emptyList()

    fun updateRecipes(newRecipes: List<Recipe>) {
        val previousRecipes = recipes
        recipes = newRecipes

        val appendedItems =
            newRecipes.size > previousRecipes.size &&
                previousRecipes == newRecipes.take(previousRecipes.size)

        if (appendedItems) {
            notifyItemRangeInserted(
                previousRecipes.size,
                newRecipes.size - previousRecipes.size
            )
        } else {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(email, binding, viewModel2, cont)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount(): Int = recipes.size

    class RecipeViewHolder(
        private val email2: String,
        private val binding: ItemRecipeBinding,
        viewModel3: RecipesViewModel,
        context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        private val viewModel = viewModel3
        private val appContext = context

        fun bind(recipe: Recipe) {
            binding.recipe = recipe
            binding.recipeTitle.text = recipe.title
            Picasso.get()
                .load(recipe.image)
                .fit()
                .centerCrop()
                .placeholder(R.drawable.bg_image_placeholder)
                .into(binding.recipeImage)

            updateFavoriteIcon(recipe.isFavorite)
            val boundRecipeId = recipe.id
            viewModel.syncFavoriteState(recipe) { isFavorite ->
                if (binding.recipe?.id == boundRecipeId) {
                    updateFavoriteIcon(isFavorite)
                }
            }

            binding.favoriteIcon.setOnClickListener {
                if (email2.isBlank()) {
                    Toast.makeText(
                        appContext,
                        appContext.getString(R.string.detail_favorite_login_required),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                viewModel.toggleFavorite(recipe) { newState ->
                    if (binding.recipe?.id != boundRecipeId) {
                        return@toggleFavorite
                    }

                    if (newState == null) {
                        Toast.makeText(
                            appContext,
                            appContext.getString(R.string.favorite_error_generic),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        updateFavoriteIcon(newState)
                    }
                }
            }

            binding.executePendingBindings()

            binding.root.setOnClickListener {
                val context = it.context
                val intent = Intent(context, RecipeDetailActivity::class.java).apply {
                    putExtra("RECIPE_ID", recipe.id)
                    putExtra("EMAIL", email2)
                }

                context.startActivity(intent)
            }
        }

        private fun updateFavoriteIcon(isFavorite: Boolean) {
            binding.favoriteIcon.setImageResource(
                if (isFavorite) R.drawable.estrella_on else R.drawable.estrella_off
            )
        }
    }
}
