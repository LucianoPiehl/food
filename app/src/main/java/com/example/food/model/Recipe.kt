package com.example.food.model

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val id: Int,
    val title: String,
    val image: String,
    val ingredients: String,
    var isFavorite: Boolean = false
)
