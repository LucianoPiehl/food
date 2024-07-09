
package com.example.food.ui.adaptor

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT

import androidx.recyclerview.widget.RecyclerView
import com.example.food.R
import com.example.food.databinding.ItemRecipeBinding
import com.example.food.model.Recipe
import com.example.food.ui.RecipeDetailActivity
import com.example.food.ui.RecipesViewModel
import com.squareup.picasso.Picasso

class RecipesAdapter(email3:String,viewModel: RecipesViewModel, cont2: Context): RecyclerView.Adapter<RecipesAdapter.RecipeViewHolder>() {

    private var recipes: List<Recipe> = emptyList()
    private var viewModel2 = viewModel
    private val cont = cont2
    private var email = email3
    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(email,binding, viewModel2, cont)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount(): Int = recipes.size

    class RecipeViewHolder(private val email2:String, private val binding: ItemRecipeBinding, viewModel3: RecipesViewModel, context: Context) : RecyclerView.ViewHolder(binding.root) {
        private val viewModel = viewModel3
        private val appContext = context
        fun bind(recipe: Recipe) {
            binding.recipeTitle.text = recipe.title
            Picasso.get().load(recipe.image).into(binding.recipeImage)


            // Set click listener
            binding.root.setOnClickListener {
                val context = it.context
                val intent = Intent(context, RecipeDetailActivity::class.java).apply {
                    putExtra("RECIPE_ID", recipe.id)
                    putExtra("EMAIL",email2)
                }

                context.startActivity(intent)
            }
        }


    }
}
