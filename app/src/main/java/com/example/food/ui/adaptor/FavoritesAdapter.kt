package com.example.food.ui.adaptor

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.food.databinding.ItemFavoriteBinding
import com.example.food.databinding.ItemRecipeBinding
import com.example.food.model.SingleRecipeDTO
import com.example.food.ui.RecipeDetailActivity
import com.example.food.ui.FavoritesViewModel
import com.squareup.picasso.Picasso

class FavoritesAdapter(email3: String, viewModel: FavoritesViewModel, cont2: Context) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    private var favorites: List<SingleRecipeDTO > = emptyList()
    private val viewModel2 = viewModel
    private val cont = cont2
    private val email = email3

    fun updateFavorites(newFavorites: List<SingleRecipeDTO>) {
        favorites = newFavorites
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(email, binding, viewModel2, cont)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(favorites[position])
    }

    override fun getItemCount(): Int = favorites.size

    class FavoriteViewHolder(private val email2: String, private val binding: ItemFavoriteBinding, viewModel3: FavoritesViewModel, context: Context) : RecyclerView.ViewHolder(binding.root) {
        private val viewModel = viewModel3
        private val appContext = context
        fun bind(recipe: SingleRecipeDTO) {
            binding.recipeTitle.text = recipe.title
            Picasso.get().load(recipe.image).into(binding.recipeImage)

            // Set click listener
            binding.root.setOnClickListener {
                val context = it.context
                val intent = Intent(context, RecipeDetailActivity::class.java).apply {
                    putExtra("RECIPE_ID", recipe.id)
                    putExtra("EMAIL", email2)
                }
                context.startActivity(intent)
            }
        }
    }
}
