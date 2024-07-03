package com.example.food.ui
import android.os.Bundle

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView;
import com.example.food.databinding.ActivityRecipesBinding
import com.example.food.ui.adaptor.RecipesAdapter
class RecipesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipesBinding
    private val viewModel: RecipesViewModel by viewModels() {
        RecipesViewModelFactory(applicationContext)
    }
    private lateinit var recipesAdapter: RecipesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        val email = intent.getStringExtra("USER_EMAIL")
        if (email != null) {
            viewModel.setUserEmail(email)

        }


        super.onCreate(savedInstanceState)
        binding = ActivityRecipesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        recipesAdapter = RecipesAdapter(viewModel,this@RecipesActivity, email?:"")
        viewModel.recipesAdapter = recipesAdapter
        binding.recipesRecyclerView.apply {
            layoutManager = GridLayoutManager(this@RecipesActivity, 2)
            adapter = recipesAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                        // Llegamos al final del RecyclerView, cargamos más recetas
                        viewModel.loadMoreRecipes()
                    }
                }
            })
        }

        viewModel.recipes.observe(this, Observer { recipes ->
            recipesAdapter.updateRecipes(recipes)


        })

    }
}