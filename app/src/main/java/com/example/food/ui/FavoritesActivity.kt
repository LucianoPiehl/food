package com.example.food.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.example.food.R
import com.example.food.databinding.ActivityFavoritesBinding
import com.example.food.ui.adaptor.FavoritesAdapter
import com.example.food.util.configureFeedMotion
import com.example.food.util.playEntranceMotion
import com.example.food.util.playFeedRefreshMotion

class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private val viewModel: FavoritesViewModel by viewModels {
        FavoritesViewModelFactory(applicationContext)
    }
    private lateinit var favoritesAdapter: FavoritesAdapter
    private var hasPlayedFeedIntro = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val email = intent.getStringExtra("USER_EMAIL").orEmpty()
        viewModel.setUserEmail(email)

        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.userEmailText.text = if (email.isBlank()) {
            getString(R.string.recipes_email_guest)
        } else {
            getString(R.string.recipes_email, email)
        }
        binding.volverButton.setOnClickListener {
            finish()
        }
        binding.searchEditText.doAfterTextChanged { editable ->
            viewModel.searchFavorites(editable?.toString().orEmpty())
        }

        favoritesAdapter = FavoritesAdapter(email, viewModel, this@FavoritesActivity)
        viewModel.favoritesAdapter = favoritesAdapter

        binding.favoritesRecyclerView.apply {
            layoutManager = GridLayoutManager(this@FavoritesActivity, 2)
            adapter = favoritesAdapter
            configureFeedMotion()
        }
        binding.headerAppBar.setExpanded(true, false)
        binding.root.doOnPreDraw {
            binding.headerAppBar.playEntranceMotion()
            binding.favoritesRecyclerView.playEntranceMotion(delayMillis = 80L, offsetDp = 20f)
            binding.volverButton.playEntranceMotion(delayMillis = 140L, offsetDp = 26f)
        }

        viewModel.favorites.observe(this, Observer { favorites ->
            favoritesAdapter.updateFavorites(favorites)
            val searchActive = binding.searchEditText.text?.isNotBlank() == true
            if ((!hasPlayedFeedIntro && favorites.isNotEmpty()) || searchActive) {
                binding.favoritesRecyclerView.playFeedRefreshMotion()
                hasPlayedFeedIntro = favorites.isNotEmpty()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadMoreFavorites()
    }
}
