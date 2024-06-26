package com.example.food.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class RecipesViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipesViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}