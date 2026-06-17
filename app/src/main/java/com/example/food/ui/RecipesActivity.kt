package com.example.food.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.food.R
import com.example.food.databinding.ActivityRecipesBinding
import com.example.food.model.Recipe
import com.example.food.ui.RecipeDetailActivity
import com.example.food.ui.adaptor.RecipeItemActions
import com.example.food.ui.adaptor.RecipesAdapter
import com.example.food.util.configureFeedMotion
import com.example.food.util.playEntranceMotion
import com.example.food.util.playFeedRefreshMotion

class RecipesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipesBinding
    private val viewModel: RecipesViewModel by viewModels {
        RecipesViewModelFactory(applicationContext)
    }
    private lateinit var recipesAdapter: RecipesAdapter
    private var hasPlayedFeedIntro = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val email = intent.getStringExtra("USER_EMAIL").orEmpty()
        viewModel.setUserEmail(email)

        binding = ActivityRecipesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.userEmailText.text = if (email.isBlank()) {
            getString(R.string.recipes_email_guest)
        } else {
            getString(R.string.recipes_email, email)
        }

        binding.favoritesButton.setOnClickListener {
            val intent = Intent(this@RecipesActivity, FavoritesActivity::class.java).apply {
                putExtra("USER_EMAIL", email)
            }
            startActivity(intent)
        }
        binding.searchEditText.doAfterTextChanged { editable ->
            viewModel.searchRecipes(editable?.toString().orEmpty())
        }

        recipesAdapter = RecipesAdapter(
            object : RecipeItemActions {
                override fun onRecipeSelected(recipe: Recipe) {
                    val intent = Intent(
                        this@RecipesActivity,
                        RecipeDetailActivity::class.java
                    ).apply {
                        putExtra("RECIPE_ID", recipe.id)
                        putExtra("EMAIL", email)
                    }
                    startActivity(intent)
                }

                override fun requestFavoriteState(
                    recipe: Recipe,
                    onComplete: (Boolean) -> Unit
                ) {
                    viewModel.syncFavoriteState(recipe, onComplete)
                }

                override fun onFavoriteToggleRequested(
                    recipe: Recipe,
                    onSuccess: (Boolean) -> Unit,
                    onLoginRequired: () -> Unit,
                    onError: () -> Unit
                ) {
                    if (email.isBlank()) {
                        onLoginRequired()
                        return
                    }

                    viewModel.toggleFavorite(recipe) { newState ->
                        when (newState) {
                            null -> onError()
                            else -> onSuccess(newState)
                        }
                    }
                }
            }
            ,
            onFavoriteLoginRequired = {
                Toast.makeText(
                    this@RecipesActivity,
                    getString(R.string.detail_favorite_login_required),
                    Toast.LENGTH_SHORT
                ).show()
            },
            onFavoriteError = {
                Toast.makeText(
                    this@RecipesActivity,
                    getString(R.string.favorite_error_generic),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        binding.recipesRecyclerView.apply {
            layoutManager = GridLayoutManager(this@RecipesActivity, 2)
            adapter = recipesAdapter
            configureFeedMotion()
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount &&
                        firstVisibleItemPosition >= 0
                    ) {
                        viewModel.loadMoreRecipes()
                    }
                }
            })
        }
        binding.headerAppBar.setExpanded(true, false)
        binding.root.doOnPreDraw {
            binding.headerAppBar.playEntranceMotion()
            binding.recipesRecyclerView.playEntranceMotion(delayMillis = 80L, offsetDp = 20f)
            binding.favoritesButton.playEntranceMotion(delayMillis = 140L, offsetDp = 26f)
        }

        viewModel.recipes.observe(this, Observer { recipes ->
            recipesAdapter.updateRecipes(recipes)
            val searchActive = binding.searchEditText.text?.isNotBlank() == true
            if ((!hasPlayedFeedIntro && recipes.isNotEmpty()) || searchActive) {
                binding.recipesRecyclerView.playFeedRefreshMotion()
                hasPlayedFeedIntro = recipes.isNotEmpty()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (::recipesAdapter.isInitialized) {
            recipesAdapter.notifyDataSetChanged()
        }
    }
}
