package com.example.food.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.food.model.User

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    suspend fun login(email: String, password: String): User?

    @Insert
    suspend fun register(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email")
    fun getUserByEmailSync(email: String): User?
}
