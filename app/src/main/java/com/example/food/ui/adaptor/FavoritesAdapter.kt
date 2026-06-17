package com.example.food.ui.adaptor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.food.databinding.ItemFavoriteBinding
import com.example.food.model.SingleRecipeDTO
import com.squareup.picasso.Picasso

class FavoritesAdapter(
    private val onRecipeSelected: (SingleRecipeDTO) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    private var favorites: List<SingleRecipeDTO > = emptyList()

    fun updateFavorites(newFavorites: List<SingleRecipeDTO>) {
        val previousFavorites = favorites
        favorites = newFavorites

        val appendedItems =
            newFavorites.size > previousFavorites.size &&
                previousFavorites == newFavorites.take(previousFavorites.size)

        if (appendedItems) {
            notifyItemRangeInserted(
                previousFavorites.size,
                newFavorites.size - previousFavorites.size
            )
        } else {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding, onRecipeSelected)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(favorites[position])
    }

    override fun getItemCount(): Int = favorites.size

    class FavoriteViewHolder(
        private val binding: ItemFavoriteBinding,
        private val onRecipeSelected: (SingleRecipeDTO) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(recipe: SingleRecipeDTO) {
            binding.recipe = recipe
            binding.recipeTitle.text = recipe.title
            Picasso.get()
                .load(recipe.image)
                .fit()
                .centerCrop()
                .placeholder(com.example.food.R.drawable.bg_image_placeholder)
                .into(binding.recipeImage)
            binding.executePendingBindings()

            binding.root.setOnClickListener {
                onRecipeSelected(recipe)
            }
        }
    }
}
