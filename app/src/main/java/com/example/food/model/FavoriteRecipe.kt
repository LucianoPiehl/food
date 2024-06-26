package com.example.food.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_recipes",
    primaryKeys = ["userEmail", "recipeId"],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["email"],
            childColumns = ["userEmail"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userEmail"]),
        Index(value = ["recipeId"])
    ]
)
data class FavoriteRecipe(
    val userEmail: String,
    val recipeId: Int
)
