package com.example.food.ui.adaptor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.food.R
import com.example.food.databinding.ItemIngredientBinding
import com.example.food.model.RecipeIngredient
import com.squareup.picasso.Picasso

class IngredientsAdapter : RecyclerView.Adapter<IngredientsAdapter.IngredientViewHolder>() {

    private var ingredients: List<RecipeIngredient> = emptyList()

    fun updateIngredients(newIngredients: List<RecipeIngredient>) {
        ingredients = newIngredients
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val binding = ItemIngredientBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IngredientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(ingredients[position])
    }

    override fun getItemCount(): Int = ingredients.size

    class IngredientViewHolder(
        private val binding: ItemIngredientBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ingredient: RecipeIngredient) {
            binding.ingredientName.text = ingredient.displayText()

            Picasso.get()
                .load(ingredient.imageUrl())
                .placeholder(R.drawable.bg_image_placeholder)
                .fit()
                .centerInside()
                .into(binding.ingredientImage)
        }
    }
}
