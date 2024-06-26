package com.example.food.data

import android.content.Context
import com.example.food.model.User

class UserRepository private constructor(private val dataSource: UserDataSource) {

    companion object {
        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(context: Context): UserRepository {
            return instance ?: synchronized(this) {
                instance ?: UserRepository(UserDataSource.getInstance(context)).also { instance = it }
            }
        }
    }

    suspend fun login(email: String, password: String): User? {
        return dataSource.login(email, password)
    }

    suspend fun register(user: User): Boolean {
        return dataSource.register(user)
    }
}

