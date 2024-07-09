package com.example.food.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.food.databinding.ActivityFavoritesBinding
import com.example.food.ui.adaptor.FavoritesAdapter

class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private val viewModel: FavoritesViewModel by viewModels {
        FavoritesViewModelFactory(applicationContext)
    }
    private lateinit var favoritesAdapter: FavoritesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        val email = intent.getStringExtra("USER_EMAIL")
        if (email != null) {
            viewModel.setUserEmail(email)
        } else {
            viewModel.setUserEmail("")
        }

        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        favoritesAdapter = FavoritesAdapter(email.toString(), viewModel, this@FavoritesActivity)
        viewModel.favoritesAdapter = favoritesAdapter

        binding.favoritesRecyclerView.apply {
            layoutManager = GridLayoutManager(this@FavoritesActivity, 2)
            adapter = favoritesAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                        viewModel.loadMoreFavorites()
                    }
                }
            })
        }

        viewModel.favorites.observe(this, Observer { favorites ->
            favoritesAdapter.updateFavorites(favorites)
        })
    }
}
