package com.example.food.ui.adaptor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.food.R
import com.example.food.databinding.ItemRecipeBinding
import com.example.food.model.Recipe
import com.squareup.picasso.Picasso

interface RecipeItemActions {
    fun onRecipeSelected(recipe: Recipe)
    fun requestFavoriteState(recipe: Recipe, onComplete: (Boolean) -> Unit)
    fun onFavoriteToggleRequested(
        recipe: Recipe,
        onSuccess: (Boolean) -> Unit,
        onLoginRequired: () -> Unit,
        onError: () -> Unit
    )
}

class RecipesAdapter(
    private val itemActions: RecipeItemActions,
    private val onFavoriteLoginRequired: () -> Unit,
    private val onFavoriteError: () -> Unit
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
        return RecipeViewHolder(
            binding,
            itemActions,
            onFavoriteLoginRequired,
            onFavoriteError
        )
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount(): Int = recipes.size

    class RecipeViewHolder(
        private val binding: ItemRecipeBinding,
        private val itemActions: RecipeItemActions,
        private val onFavoriteLoginRequired: () -> Unit,
        private val onFavoriteError: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

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
            itemActions.requestFavoriteState(recipe) { isFavorite ->
                if (binding.recipe?.id == boundRecipeId) {
                    updateFavoriteIcon(isFavorite)
                }
            }

            binding.favoriteIcon.setOnClickListener {
                itemActions.onFavoriteToggleRequested(
                    recipe = recipe,
                    onSuccess = { newState ->
                        if (binding.recipe?.id == boundRecipeId) {
                            updateFavoriteIcon(newState)
                        }
                    },
                    onLoginRequired = onFavoriteLoginRequired,
                    onError = onFavoriteError
                )
            }

            binding.executePendingBindings()

            binding.root.setOnClickListener {
                itemActions.onRecipeSelected(recipe)
            }
        }

        private fun updateFavoriteIcon(isFavorite: Boolean) {
            binding.favoriteIcon.setImageResource(
                if (isFavorite) R.drawable.estrella_on else R.drawable.estrella_off
            )
        }
    }
}
