package com.example.food.data

import android.content.Context
import com.example.food.model.User

class UserDataSource private constructor(private val userDao: UserDao) {

    companion object {
        @Volatile
        private var instance: UserDataSource? = null

        fun getInstance(context: Context): UserDataSource {
            return instance ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context)
                instance ?: UserDataSource(db.userDao()).also { instance = it }
            }
        }
    }

    suspend fun login(email: String, password: String): User? {
        return userDao.login(email, password)
    }

    suspend fun register(user: User): Boolean {
        return try {
            userDao.register(user)
            true
        } catch (e: Exception) {
            false
        }
    }
}
